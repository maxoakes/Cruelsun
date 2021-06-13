package com.scouter.cruelsun.handlers;

import com.scouter.cruelsun.Configs;
import com.scouter.cruelsun.CruelSun;
import com.scouter.cruelsun.commands.CommandSetBurn;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = CruelSun.MODID)
public class WorldBurnHandler {

    private final int CHUNK_SIZE = 16;
    private final double fireSpawnChancePerSecond = 1;
    private final int TPS = 20;
    private final int radius = 10;
    Random random = new Random();

    @SubscribeEvent
    public void onWorldTickEvent(TickEvent.WorldTickEvent event)
    {
        /* Global Checks */
        //only run every second. Pretty efficient
        if (!(event.world.getDayTime()%TPS==0)) return;



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
                if (Configs.CONFIGS.isDebugMode()) System.out.println("Current world is in whitelist");
                doesThisWorldBurn = true;
                break;
            }
            if (Configs.CONFIGS.isDebugMode()) System.out.println("Current world is not on whitelist");
        }
        if (!doesThisWorldBurn) return;

        //check if command has been activated this session
        if (CommandSetBurn.getCommandState() == CommandSetBurn.CommandState.PAUSE) return;

        //check if the world should be burning now
        if ((event.world.getGameTime() < Configs.CONFIGS.ticksToFirstBurn()) &&
                (CommandSetBurn.getCommandState() != CommandSetBurn.CommandState.START)) return;

        //check if it is a current new moon
        if ((Configs.CONFIGS.isNewMoonSafe() && event.world.getMoonPhase() == 4) && event.world.isNightTime()) return;

        //for each loaded entity
        List<PlayerEntity> players = new ArrayList<>();
        players = (List<PlayerEntity>) event.world.getPlayers();
        long time = event.world.getDayTime()%24000;

        if (event.world.isNightTime() && Configs.CONFIGS.doDayDamageOnly()) return; //does night cause damage too?

        List<Chunk> loadedChunks = new ArrayList<>();
        for (PlayerEntity p : event.world.getPlayers()) loadedChunks.addAll(getRadiusChunks(event.world, p, radius));
        loadedChunks = loadedChunks.stream().distinct().collect(Collectors.toList()); //if this is run on the server, this will remove overlapping chunks if there are multiple players... I think

        /*
         * The following code is only used if the configs call for mobs to be damaged similar to the player
         * */
        if (Configs.CONFIGS.doMobDamage())
        {
            List<Entity> livingMobs = new ArrayList<>();
            for (Chunk c : loadedChunks) {

                for (ClassInheritanceMultiMap<Entity> entityList : c.getEntityLists()) {
                    for (Entity e : entityList)
                    {
                        //check to see if the type of entity that we see is the type of mob that we want to damage
                        if ((e instanceof LivingEntity) && !(e instanceof PlayerEntity) && !e.isWet())
                        {
                            //check to see if the mob is on the surface
                            if (e.world.getLightFor(LightType.SKY, e.getPosition()) >= Configs.CONFIGS.getMinLightToDamagePlayer())
                            {
                                livingMobs.add(e);
                            }
                        }
                    }
                }
            }

            //damage each mob
            for (Entity e : livingMobs) damageEntity(e);
        }
    }

    public static ArrayList<Chunk> getRadiusChunks(World w, PlayerEntity p,int radius)
    {
        int chunkPosX = p.chunkCoordX;
        int chunkPosZ = p.chunkCoordZ;
		ArrayList<Chunk> chunks = new ArrayList();
		  for (int x = chunkPosX - radius; x <= chunkPosX + radius; x++) {
	            for (int z = chunkPosZ - radius; z <= chunkPosZ + radius; z++) {
	            	if(w.chunkExists(x, z))
	            		chunks.add(w.getChunk(x,z));
	            }
		  }
		return chunks;
	}

	public void damageEntity(Entity mob)
    {
        mob.setFire(Configs.CONFIGS.getBurnTimeMultiplier());
    }
}