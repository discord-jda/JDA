package net.dv8tion.jda.api.interactions.commands.commandInteractions;

import net.dv8tion.jda.api.entities.Message;

public interface MessageCommandInteraction extends ContextMenuInteraction
{
    /**
     * Gets the targeted message
     *
     * @return The targeted message
     */
    Message getTargetMessage();
}
