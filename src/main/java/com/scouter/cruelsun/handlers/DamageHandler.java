package com.scouter.cruelsun.handlers;

import com.scouter.cruelsun.Configs;
import com.scouter.cruelsun.CruelSun;
import com.scouter.cruelsun.commands.CommandSetBurn;
import com.scouter.cruelsun.helper.Time;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = CruelSun.MODID)
public class DamageHandler {

    DamageSource damageSource = new DamageSource("cruelsun").setDamageBypassesArmor().setDifficultyScaled();
    private final double FIRE_SPAWN_PER_SECOND = 1;
    private final int TPS = 20;
    private final int CHUNK_SIZE = 16;
    Random random = new Random();

    @SubscribeEvent
    public void onWorldTickEvent(TickEvent.WorldTickEvent event)
    {
        /* Global Checks */
        //only run every second. Pretty efficient
        if (!(event.world.getDayTime() % TPS == 0)) return;

        //check what side this method is being run on
        if (event.world.isRemote()) return;
        if (event.phase == TickEvent.Phase.END) return;

        //check if the current world is on the whitelist from the configs
        boolean doesThisWorldBurn = false;
        String currentWorld = event.world.getDimensionKey().getLocation().toString();
        for (String worlds : Configs.CONFIGS.getAllowedWorlds())
        {
            if (currentWorld.equals(worlds))
            {
                doesThisWorldBurn = true;
                break;
            }
        }
        if (!doesThisWorldBurn) return;

        if (Configs.CONFIGS.isDebugMode())
            System.out.println("*** Start of Tick " + event.world.getDayTime() + "/" +event.world.getGameTime() + " ***");

        //check if command has been activated this session
        if (CommandSetBurn.getCommandState() == CommandSetBurn.CommandState.PAUSE) return;

        for (PlayerEntity player : event.world.getPlayers())
        {
            //first day protection and notification
            if ((player.world.getGameTime() < Configs.CONFIGS.ticksToFirstBurn()) && CommandSetBurn.getCommandState() == CommandSetBurn.CommandState.NORMAL)
            {
                int secondsToBurn = (int)(Configs.CONFIGS.ticksToFirstBurn() - (player.world.getGameTime()))/TPS;
                if (Configs.CONFIGS.isDebugMode()) System.out.println("Seconds until burn: " + secondsToBurn);
                if (secondsToBurn % Configs.CONFIGS.getFirstDayProtectionWarningInterval() == 0)
                    player.sendMessage(new TranslationTextComponent("cruelsun.timer.firstday.safety.status", secondsToBurn), player.getUniqueID());
                if (secondsToBurn == 0)
                    player.sendMessage(new TranslationTextComponent("cruelsun.timer.firstday.safety.start"), player.getUniqueID());
            }
        }

        //check if the world should be burning now
        if ((event.world.getGameTime() < Configs.CONFIGS.ticksToFirstBurn()) &&
                (CommandSetBurn.getCommandState() != CommandSetBurn.CommandState.START)) return;

        //check if it is night time, and if the configs call for damage during only the day
        if (event.world.isNightTime() && Configs.CONFIGS.doDayDamageOnly()) return;

        //check if it is a current new moon
        if ((Configs.CONFIGS.isNewMoonSafe() && Time.getMoonPhaseString(event.world.getDayTime()).equals("new")) &&
                event.world.isNightTime()) return;

        //start a check for each loaded entity
        List<Entity> entities = getLoadedEntities(event.world);
        for (Entity entity : entities)
        {
            //check if the entity is wet and if the configs care if they are wet
            if (Configs.CONFIGS.doesWaterStopBurn() && entity.isWet()) continue;

            //check if the entity is a player
            if (entity instanceof PlayerEntity)
            {
                //perform checks for player-type entities
                PlayerEntity player = (PlayerEntity) entity;
                if (!Configs.CONFIGS.doPlayerDamage()) continue;
                if (player.isCreative() || player.isSpectator()) continue;

                //spawn protection and notification
                if (player.ticksExisted <= Configs.CONFIGS.getBurnSafetyTime() * TPS) {
                    int secondsToBurn = Configs.CONFIGS.getBurnSafetyTime() - (player.ticksExisted / TPS);
                    if (secondsToBurn % Configs.CONFIGS.getSpawnProtectionWarningInterval() == 0)
                        player.sendMessage(new TranslationTextComponent("cruelsun.timer.spawn.safety.status", secondsToBurn), player.getUniqueID());
                    if (secondsToBurn <= 1)
                        player.sendMessage(new TranslationTextComponent("cruelsun.timer.spawn.safety.start"), player.getUniqueID());
                    continue;
                }

                if (isSafeLocation(entity)) continue;

                //if we get here, the player has no protections of any kind, and should get damaged
                if (Configs.CONFIGS.isDebugMode())
                    System.out.println("P:" + entity.getEntityWorld() + " " + entity.ticksExisted + " " + entity.getDisplayName().getString());
                damagePlayerAndArmor(player);
            }
            else //otherwise, they are a mob
            {
                if (isSafeLocation(entity)) continue;
                if (Configs.CONFIGS.isDebugMode())
                    System.out.println("E:" + entity.getEntityWorld() + " " + entity.ticksExisted + " " + entity.getDisplayName().getString());
                entity.setFire(Configs.CONFIGS.getBurnTimeMultiplier());
            }

        }

        //if the configs call for fire damage to the surface
        if (Configs.CONFIGS.doWorldDamage())
        {
            List<Chunk> loadedChunks = new ArrayList<>();
            List<PlayerEntity> players = (List<PlayerEntity>) event.world.getPlayers();

            for (PlayerEntity player : players)
                loadedChunks.addAll(getRadiusChunks(event.world, player, 8));

            loadedChunks = loadedChunks.stream().distinct().collect(Collectors.toList());
            if (loadedChunks.isEmpty()) return;
            Chunk c = loadedChunks.get(random.nextInt(loadedChunks.size()));
            if (random.nextDouble() > FIRE_SPAWN_PER_SECOND) return;
            int x = ((c.getPos().x) * CHUNK_SIZE) + random.nextInt(CHUNK_SIZE);
            int z = ((c.getPos().z) * CHUNK_SIZE) + random.nextInt(CHUNK_SIZE);
            int y = 255;
            BlockPos pos = new BlockPos(x, y, z);
            while (event.world.isAirBlock(pos)) pos = pos.down(); //moves the placement position downward until it is not in the air anymore
            pos = pos.up(); //places on the surface of the aforementioned position
            event.world.setBlockState(pos, Blocks.FIRE.getDefaultState());
            if (Configs.CONFIGS.isDebugMode()) System.out.println("Added fire block to: "+c.getPos()+": "+x+","+z);
        }
    }

    //get a list of all of the living entities that are relatively close to players
    public static List<Entity> getLoadedEntities(World world)
    {
        List<Chunk> loadedChunks = new ArrayList<>();
        List<PlayerEntity> players = (List<PlayerEntity>) world.getPlayers();
        List<Entity> livingEntities = new ArrayList<>();

        for (PlayerEntity player : players)
            loadedChunks.addAll(getRadiusChunks(world, player, 8));

        loadedChunks = loadedChunks.stream().distinct().collect(Collectors.toList());
        for (Chunk c : loadedChunks)
        {
            for (ClassInheritanceMultiMap<Entity> entityList : c.getEntityLists())
            {
                for (Entity e : entityList)
                {
                    if ((e instanceof LivingEntity))
                        livingEntities.add(e);
                }
            }
        }
        return livingEntities;
    }

    //get a list of all of the chunks that are within a radius of a player
    public static ArrayList<Chunk> getRadiusChunks(World w, PlayerEntity p, int radius)
    {
        int chunkPosX = p.chunkCoordX;
        int chunkPosZ = p.chunkCoordZ;
		ArrayList<Chunk> chunks = new ArrayList<>();
		  for (int x = chunkPosX - radius; x <= chunkPosX + radius; x++) {
	            for (int z = chunkPosZ - radius; z <= chunkPosZ + radius; z++) {
	            	if(w.chunkExists(x, z))
	            		chunks.add(w.getChunk(x,z));
	            }
		  }
		return chunks;
	}

	//damage the player and their armor based on a lot of factors and config
    private void damagePlayerAndArmor(PlayerEntity player)
    {
        if (Configs.CONFIGS.isDebugMode()) System.out.println("*** Starting damageConditionCheck ***");

        //damage each armor piece
        damageSingleArmor(player.getItemStackFromSlot(EquipmentSlotType.HEAD), player);
        damageSingleArmor(player.getItemStackFromSlot(EquipmentSlotType.CHEST), player);
        damageSingleArmor(player.getItemStackFromSlot(EquipmentSlotType.LEGS), player);
        damageSingleArmor(player.getItemStackFromSlot(EquipmentSlotType.FEET), player);

        //if the player is not protected by armor, and not protected by potions, then they are damaged
        if (!isProtectedByArmor(player) && !isProtectedByPotion(player)) damagePlayer(player);
    }

    private boolean isSafeLocation(Entity entity)
    {
        //if the player is in a block that is "dark" enough, eg. in a light level less than what is in the configs, then they are "safe"
        return entity.world.getLightFor(LightType.SKY,entity.getPosition()) < Configs.CONFIGS.getMinLightToDamagePlayer();
    }

    //check if they are protected by armor or potions
    private boolean isProtectedByPotion(PlayerEntity player)
    {
        //check if the configs allow for potions to be useful
        if (Configs.CONFIGS.doPotionsWork())
        {
            //iterate through list of active player potion effects and check if the fire resistance is one of them
            for (Map.Entry<Effect, EffectInstance> buff : player.getActivePotionMap().entrySet()) {
                if (Configs.CONFIGS.isDebugMode()) System.out.println("PotionEffect: "+buff.getKey().getDisplayName());
                if (buff.getKey() == Effects.FIRE_RESISTANCE) {
                    return true;
                }
            }
        }
        //if we get here, there are no potions applied, or no fire resistance potions applied, or the configs say that potions are useless
        return false;
    }

    private boolean isProtectedByArmor(PlayerEntity player)
    {
        //check if the player is wearing armor, and check the configs to see if armor is useful
        return player.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem() instanceof ArmorItem &&
                player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() instanceof ArmorItem &&
                player.getItemStackFromSlot(EquipmentSlotType.LEGS).getItem() instanceof ArmorItem &&
                player.getItemStackFromSlot(EquipmentSlotType.FEET).getItem() instanceof ArmorItem &&
                Configs.CONFIGS.doesArmorWork();
    }

    //try to damage the armor that is passed in
    private void damageSingleArmor(ItemStack armor, PlayerEntity player)
    {
        long time = player.getEntityWorld().getDayTime() % 24000;
        //check if the things in the armor slot is actually armor, otherwise it is air (or a pumpkin...) and nothing should be done
        if (!(armor.getItem() instanceof ArmorItem)) return;

        String armorName = armor.getItem().toString();

        //check if armor is damageable
        if (armor.isDamageable())
        {
            //if the armor is damageable...
            if (Configs.CONFIGS.isDebugMode()) System.out.println(armorName + " is damageable.");

            //base amount that durability will be taken from the armor each tick check
            int protectionAmount = 1;

            //check if it is a modded hazmat-type armor piece
            if (Configs.CONFIGS.isDebugMode()) System.out.println("Config list of keywords: " + Configs.CONFIGS.getHazmatStrings());
            for (String keyword : Configs.CONFIGS.getHazmatStrings()) {
                if (Configs.CONFIGS.isDebugMode())
                    System.out.println("Checking for keyword '" + keyword + "' in '" + armorName + "'");
                if (armorName.contains(keyword)) {
                    //if the armor is a hazmat-type armor piece, increase its protection value
                    protectionAmount = 3;
                    if (Configs.CONFIGS.isDebugMode()) System.out.println("Confirmed " + armorName + " isHazmat type because config:" + keyword);
                }
            }

            //check if the armor is enchanted with fire protection
            //do configs allow enchantments to work
            if (Configs.CONFIGS.doEnchantmentsWork()) {
                ListNBT armorEnchantments = armor.getEnchantmentTagList();
                for (INBT enchantment : armorEnchantments) {
                    if (Configs.CONFIGS.isDebugMode()) System.out.println("ArmorEnch: " + armorName + ": " + enchantment.getString());
                    if (enchantment.getString().contains("fire_protection")) {
                        try {
                            //extract the level of the enchantment from the toString, and add it to the base protection amount of the armor
                            protectionAmount += Integer.parseInt(enchantment.getString().replaceAll("\\D+", ""));
                            if (Configs.CONFIGS.isDebugMode())
                                System.out.println("Fire protection found on " + armorName);
                        } catch (Exception e) {
                            System.out.println("Error parsing int in enchantment substring. How did we get here?!");
                        }
                    }
                }
            }

            //every 'protectionAmount' seconds, the armor will take armorDamageRate damage
            if (time % (TPS * ((long) protectionAmount * Configs.CONFIGS.getEnchantmentProtectionMultiplier())) == 0)
            {
                armor.setDamage(armor.getDamage() + Configs.CONFIGS.getArmorDamageRate());
                if (Configs.CONFIGS.isDebugMode()) System.out.println("Damaging armor: "+armorName + ": " + armor.getDamage() + "/" + armor.getMaxDamage() + " (protection:" + protectionAmount + ")");
                if (armor.getMaxDamage() <= armor.getDamage())
                    armor.shrink(1); //if we are at the maximum damage level, destroy the armor
            }
        }
        else
        {
            //if the armor is not damageable...
            if (Configs.CONFIGS.isDebugMode()) System.out.println("A:"+armorName+" is not damageable");

            //check if it uses Forge Energy
            if (armor.getCapability(CapabilityEnergy.ENERGY).isPresent())
            {
                try {
                    if (Configs.CONFIGS.isDebugMode()) System.out.println(armorName + " has energy");

                    //if it uses energy, try to drain an amount from it
                    LazyOptional<IEnergyStorage> optional = armor.getCapability(CapabilityEnergy.ENERGY);
                    if (optional.isPresent())
                    {
                        IEnergyStorage energyStorage = optional.orElseThrow(IllegalStateException::new);
                        if (Configs.CONFIGS.isDebugMode()) System.out.println("canReceive:"+energyStorage.canReceive()+" canExtract:"+energyStorage.canExtract());

                        //if the armor can have power extracted, 'damage' it by taking away some stored energy
                        if (energyStorage.canExtract())
                        {
                            if (Configs.CONFIGS.isDebugMode()) System.out.println("canExtract:"+energyStorage.canExtract()+". Extracting energy from armor.");
                            energyStorage.extractEnergy(Configs.CONFIGS.getEnergyArmorDrainRate(), false);
                            if (Configs.CONFIGS.isDebugMode()) System.out.println("Damaged " + Configs.CONFIGS.getEnergyArmorDrainRate() + ". Now " + energyStorage.getEnergyStored() + "/" + energyStorage.getMaxEnergyStored());
                        }
                        //if the armor cannot have power extracted, give damage to player... *cough* Mekanism Mekasuit
                        else
                        {
                            if (Configs.CONFIGS.isDebugMode()) System.out.println("canExtract:"+energyStorage.canExtract()+". Damaging player directly.");
                            catchFireToPlayer(player, 2); //player.attackEntityFrom(new DamageSource("cruelsun.energyarmor"), 2);
                        }

                        //if after the damaging of the armor, it becomes fully drained, catch the player on fire
                        if (energyStorage.getEnergyStored() == 0)
                        {
                            if (Configs.CONFIGS.isDebugMode()) System.out.println(armorName+" is out of charge. Player is not protected. Setting fire.");
                            catchFireToPlayer(player, 2);
                        }
                    }
                }
                //if we get here, something weird happened
                catch (Exception e)
                {
                    System.out.println("Exception caught trying to drain armor");
                    catchFireToPlayer(player, 2);
                }
            }
            else
            {
                //if the armor does not have energy storage and is not damageable, set fire to the player
                if (Configs.CONFIGS.isDebugMode()) System.out.println(armorName+" does not have energy, and is not damageable. Just set fire to player");
                catchFireToPlayer(player, 2);
            }
        }
    }

    private void damagePlayer(PlayerEntity player)
    {
        long time = player.getEntityWorld().getDayTime()%24000;
        //define what solar intensity is when it comes to damaging the player
        float solarIntensity = Math.max(6000 - Math.abs((int) time - 6000), 0);
        int burnTime = Math.round(solarIntensity / 1000f);
        double sunDamage = (Math.ceil(solarIntensity / 3000) + 1) * Configs.CONFIGS.getDamageMultiplier();
        if (Configs.CONFIGS.isDebugMode()) System.out.println("Damaging player @DayTick:" + time + " Intensity:" + solarIntensity + " Burntime:" + burnTime + " Damage:" + sunDamage);

        player.attackEntityFrom(damageSource, (float) sunDamage);
        if (time <= 12000) catchFireToPlayer(player, burnTime);
    }

    private void catchFireToPlayer(PlayerEntity player, int duration)
    {
        player.setFire(duration * Configs.CONFIGS.getBurnTimeMultiplier());
    }
}