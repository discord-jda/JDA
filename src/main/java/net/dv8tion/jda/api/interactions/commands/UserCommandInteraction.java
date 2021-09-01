package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;

public interface UserCommandInteraction extends CommandInteraction
{
    /**
     * The user clicked on.
     *
     * @return The user clicked on
     */
    @Nonnull
    User getInteractedUser();
}
