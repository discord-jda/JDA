package net.dv8tion.jda.api.interactions.commands.commandInteractions;

import javax.annotation.Nonnull;

public interface ContextMenuInteraction extends CommandInteraction
{
    /**
     * The target id
     *
     * @return The command id
     */
    long getTargetIdLong();

    /**
     * The command id
     *
     * @return The command id
     */
    @Nonnull
    default String getTargetId()
    {
        return Long.toUnsignedString(getTargetIdLong());
    }
}

