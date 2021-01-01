package com.scouter.cruelsun.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.scouter.cruelsun.Configs;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class CommandSetBurn
{
    public enum CommandState {NORMAL,START,PAUSE}
    private static CommandState state = CommandState.NORMAL;

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> commandBuilder = Commands.literal("cruelsun")
                .requires(s -> s.hasPermissionLevel(2))
                .then(Commands.literal("help")
                        .executes(CommandSetBurn::printHelp))
                .then(Commands.literal("start")
                        .executes(CommandSetBurn::commandStart))
                .then(Commands.literal("pause")
                        .executes(CommandSetBurn::commandPause))
                .then(Commands.literal("resume")
                        .executes(CommandSetBurn::commandResume));

        LiteralCommandNode<CommandSource> command = dispatcher.register(commandBuilder);
        dispatcher.register(Commands.literal("cruelsun").redirect(command));
        dispatcher.register(Commands.literal("cs").redirect(command));
    }

    private static int printHelp(CommandContext<CommandSource> ctx) {
        ctx.getSource().sendFeedback(new TranslationTextComponent("cruelsun.command.start.help.start"), false);
        ctx.getSource().sendFeedback(new TranslationTextComponent("cruelsun.command.start.help.pause"), false);
        ctx.getSource().sendFeedback(new TranslationTextComponent("cruelsun.command.start.help.resume"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int commandStart(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        state = CommandState.START;
        PlayerEntity player = ctx.getSource().asPlayer();
        ctx.getSource().getServer().getPlayerList().sendMessageToTeamOrAllPlayers(player, new TranslationTextComponent("cruelsun.command.start"));
        return Command.SINGLE_SUCCESS;
    }
    private static int commandPause(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        state = CommandState.PAUSE;
        PlayerEntity player = ctx.getSource().asPlayer();
        ctx.getSource().getServer().getPlayerList().sendMessageToTeamOrAllPlayers(player, new TranslationTextComponent("cruelsun.command.pause"));
        return Command.SINGLE_SUCCESS;
    }
    private static int commandResume(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        state = CommandState.NORMAL;
        PlayerEntity player = ctx.getSource().asPlayer();
        ctx.getSource().getServer().getPlayerList().sendMessageToTeamOrAllPlayers(player,
                (
                        new TranslationTextComponent("cruelsun.command.resume")
                                .append(new StringTextComponent(". Current Tick: "+player.world.getGameTime()+"/"+ Configs.CONFIGS.ticksToFirstBurn()))
                )
        );
        return Command.SINGLE_SUCCESS;
    }
    public static CommandState getCommandState() {return state;}
}
