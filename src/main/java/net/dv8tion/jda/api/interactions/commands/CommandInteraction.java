package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Interaction of a generic command
 */
public interface CommandInteraction extends Interaction
{
    /**
     * The command name.
     * <br>This can be useful for abstractions.
     *
     * @return The command name
     */
    @Nonnull
    String getName();

    /**
     * The command id
     *
     * @return The command id
     */
    long getCommandIdLong();

    /**
     * The command id
     *
     * @return The command id
     */
    @Nonnull
    default String getCommandId()
    {
        return Long.toUnsignedString(getCommandIdLong());
    }

    @NotNull
    @Override
    default InteractionType getType()
    {
        return InteractionType.COMMAND;
    }
}
