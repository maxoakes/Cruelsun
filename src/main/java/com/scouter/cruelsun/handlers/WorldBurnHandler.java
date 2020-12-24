package com.scouter.cruelsun.handlers;

import com.scouter.cruelsun.Configs;
import com.scouter.cruelsun.CruelSun;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.BlockPos;
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

    private final int chunkSize = 16;
    private final double fireSpawnChancePerSecond = 1;
    private final int TPS = 20;
    private final int radius = 10;
    Random random = new Random();

    @SubscribeEvent
    public void onWorldTickEvent(TickEvent.WorldTickEvent event)
    {
        if (event.world.getDimensionKey() != World.OVERWORLD || event.side.isClient() || event.phase == TickEvent.Phase.END) return;

        long time = event.world.getDayTime()%24000;
        if (!(time%TPS==0)) return;

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
                //System.out.println(c.getPos());
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
            for (Entity e : livingMobs)
            {
                damageEntity(e);
            }
        }

        /*
         * The following code is only used if the configs call for flammable blocks to be damaged
         * */
        if (Configs.CONFIGS.doWorldDamage())
        {
            if (loadedChunks.isEmpty()) return;
            Chunk c = loadedChunks.get(random.nextInt(loadedChunks.size()));
            if (random.nextDouble()>fireSpawnChancePerSecond) return;
            int x = ((c.getPos().x)*chunkSize)+random.nextInt(chunkSize);
            int z = ((c.getPos().z)*chunkSize)+random.nextInt(chunkSize);
            int y = 255;
            BlockPos pos = new BlockPos(x, y, z);
            while (event.world.isAirBlock(pos)) pos = pos.down(); //moves the block downward until it is not in the air anymore
            pos = pos.up(); //places on the surface of the aforementioned position
            event.world.setBlockState(pos,Blocks.FIRE.getDefaultState());
            if (Configs.CONFIGS.isDebugMode()) System.out.println("Added fire block to: "+c.getPos()+": "+x+","+z);
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