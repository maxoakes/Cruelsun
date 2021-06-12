package com.scouter.cruelsun.items;

import com.scouter.cruelsun.CruelSun;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemRegister
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CruelSun.MODID);

    public static final RegistryObject<Item> ALMANAC = ITEMS.register(Almanac.name, ()
            -> new Almanac(new Item.Properties().maxStackSize(1).group(ItemGroup.TOOLS)));
}
