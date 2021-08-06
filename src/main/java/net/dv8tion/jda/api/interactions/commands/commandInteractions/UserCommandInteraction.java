package net.dv8tion.jda.api.interactions.commands.commandInteractions;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public interface UserCommandInteraction extends ContextMenuInteraction
{
    /**
     * Gets the targeted user
     *
     * @return The targeted user
     */
    User getTargetUser();


    /**
     * Gets the targeted member
     *
     * @return The targeted member
     */
    Member getTargetMember();
}
