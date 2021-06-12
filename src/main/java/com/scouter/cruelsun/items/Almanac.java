package com.scouter.cruelsun.items;

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
            String phaseString = "null";
            switch (world.getMoonPhase()) {
                case 0:
                    phaseString = "full";
                    break;
                case 1:
                    phaseString = "waxing gibbous";
                    break;
                case 2:
                    phaseString = "first quarter";
                    break;
                case 3:
                    phaseString = "waxing crescent";
                    break;
                case 4:
                    phaseString = "new";
                    break;
                case 5:
                    phaseString = "waning crescent";
                    break;
                case 6:
                    phaseString = "third quarter";
                    break;
                case 7:
                    phaseString = "waning gibbous";
                    break;
            }

            String timeOfDayString = "some time";
            int time = (int) (player.getEntityWorld().getDayTime() % 24000);
            if (time >= 0 && time <= 2000)
                timeOfDayString = "morning";
            else if (time > 2000 && time < 6000)
                timeOfDayString = "late morning";
            else if (time > 6000 && time < 9000)
                timeOfDayString = "early afternoon";
            else if (time > 9000 && time < 12000)
                timeOfDayString = "late afternoon";
            else if (time > 12000 && time < 14000)
                timeOfDayString = "evening";
            else if (time > 14000 && time < 22000)
                timeOfDayString = "the middle of the night";
            else
                timeOfDayString = "early morning";

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
