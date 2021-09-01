package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.entities.Message;

import javax.annotation.Nonnull;

public interface MessageCommandInteraction extends CommandInteraction
{
    /**
     * The message clicked on.
     *
     * @return The message clicked on
     */
    @Nonnull
    Message getInteractedMessage();
}
