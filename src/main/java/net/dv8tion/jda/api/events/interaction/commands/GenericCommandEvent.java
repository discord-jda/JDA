package net.dv8tion.jda.api.events.interaction.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.commands.interactions.CommandInteraction;
import net.dv8tion.jda.internal.interactions.commands.CommandInteractionImpl;

import javax.annotation.Nonnull;

/**
 * Indicates that a command was used in a {@link MessageChannel}.
 *
 * <h2>Requirements</h2>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://discord.com/developers/applications" target="_blank">Discord Developers Portal</a>.
 */
public class GenericCommandEvent extends GenericInteractionCreateEvent implements CommandInteraction
{
    private final CommandInteractionImpl commandInteraction;

    public GenericCommandEvent(@Nonnull JDA api, long responseNumber, @Nonnull CommandInteractionImpl interaction)
    {
        super(api, responseNumber, interaction);
        this.commandInteraction = interaction;
    }

    @Nonnull
    @Override
    public MessageChannel getChannel()
    {
        return commandInteraction.getChannel();
    }

    @Nonnull
    @Override
    public String getName()
    {
        return commandInteraction.getName();
    }

    @Override
    public long getCommandIdLong()
    {
        return commandInteraction.getCommandIdLong();
    }
}
