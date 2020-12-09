package com.scouter.cruelsun.biomes;

import com.scouter.cruelsun.CruelSun;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BiomeRegister
{
    public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(ForgeRegistries.BIOMES, CruelSun.MODID);

    public static final RegistryObject<Biome> BIOME_SCORCHED = BIOMES.register("biome_scorched", ()
            -> new BiomeScorched().getBiome());

}
