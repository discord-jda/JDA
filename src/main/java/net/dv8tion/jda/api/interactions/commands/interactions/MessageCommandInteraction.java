package net.dv8tion.jda.api.interactions.commands.interactions;

import net.dv8tion.jda.api.entities.Message;

import javax.annotation.Nonnull;

public interface MessageCommandInteraction extends ContextMenuInteraction
{
    /**
     * Gets the targeted message
     *
     * @return The targeted message
     */
    @Nonnull
    Message getTargetMessage();
}
