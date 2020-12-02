package com.scouter.cruelsun;

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
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = CruelSun.MODID)
public class WorldBurnHandler {

    private final int TPS = 20;

    @SubscribeEvent
    public void onWorldTickEvent(TickEvent.WorldTickEvent event)
    {
        if (event.world.getDimensionKey() != World.OVERWORLD || event.side.isClient() || event.phase == TickEvent.Phase.END) return;

        long time = event.world.getDayTime()%24000;
        if (!(time%TPS==0)) return;
        //System.out.println("****in onWorldTickEvent:" + time);

        /*
         * The following code is only used if the configs call for mobs to be damaged similar to the player
         * */
        if (Configs.CONFIGS.doMobDamage())
        {
            System.out.println();
            List<Chunk> loadedChunks = new ArrayList<>();
            for (PlayerEntity p : event.world.getPlayers()) loadedChunks.addAll(getRadiusChunks(event.world, p, 10));
            loadedChunks = loadedChunks.stream().distinct().collect(Collectors.toList()); //if this is run on the server, this will remove overlapping chunks if there are multiple players... I think
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
                System.out.println(e);
                damageEntity(e);
            }
        }
        if (Configs.CONFIGS.doWorldDamage()) return;
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
        mob.setGlowing(true);
        mob.setFire(1);
    }
}