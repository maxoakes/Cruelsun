package com.scouter.cruelsun.items;

import com.scouter.cruelsun.helper.Time;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class Almanac extends Item
{
    public static final String name = "almanac";

    public Almanac(Properties properties)
    {
        super(properties);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand)
    {
        if(world.isRemote) {
            String phaseString = Time.getMoonPhaseString(world.getDayTime());

            long time = world.getDayTime();
            String timeOfDayString = Time.getApproximateTimeString(time);

            if (time < 12000)
                player.sendMessage(new TranslationTextComponent("cruelsun.almanac.day", timeOfDayString, phaseString), player.getUniqueID());
            else
                player.sendMessage(new TranslationTextComponent("cruelsun.almanac.night", timeOfDayString, phaseString), player.getUniqueID());
        }
        return new ActionResult<>(ActionResultType.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context)
    {
        return ActionResultType.PASS;
    }

    @Override
    public boolean isBookEnchantable(final ItemStack itemstack1, final ItemStack itemstack2) {
        return false;
    }
}
