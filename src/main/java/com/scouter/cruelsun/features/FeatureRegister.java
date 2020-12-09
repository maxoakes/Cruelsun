package com.scouter.cruelsun.features;

import com.scouter.cruelsun.CruelSun;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class FeatureRegister
{
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, CruelSun.MODID);

    public static final Feature<NoFeatureConfig> FALLEN_LOG_LARGE = register("fallen_log_large", new FallenLogLarge(NoFeatureConfig.field_236558_a_));
    public static final Feature<NoFeatureConfig> FALLEN_LOG_SMALL = register("fallen_log_small", new FallenLogSmall(NoFeatureConfig.field_236558_a_));
    public static final Feature<NoFeatureConfig> STUMP_SMALL = register("stump_small", new StumpSmall(NoFeatureConfig.field_236558_a_));

    private static <C extends IFeatureConfig, F extends Feature<C>> F register(String key, F value)
    {
        value.setRegistryName(new ResourceLocation(CruelSun.MODID, key));
        ForgeRegistries.FEATURES.register(value);
        return value;
    }
}
