package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.entities.AbstractChannel;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.Interaction;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

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

    @Nullable
    @Override
    MessageChannel getChannel();
}
