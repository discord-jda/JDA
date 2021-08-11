package net.dv8tion.jda.api.interactions.commands.interactions;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface UserCommandInteraction extends ContextMenuInteraction
{
    /**
     * Gets the targeted user
     *
     * @return The targeted user
     */
    @Nonnull
    User getTargetUser();


    /**
     * Gets the targeted member
     *
     * @return The targeted member. If the command was not run in a guild, the member will return as null
     */
    @Nullable
    Member getTargetMember();
}
