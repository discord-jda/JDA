package net.dv8tion.jda.api.events.interaction.commandEvents;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.commandInteractions.MessageCommandInteraction;
import net.dv8tion.jda.internal.interactions.commandInteractionImpls.MessageCommandInteractionImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Indicates that a message command was used in a {@link MessageChannel}.
 *
 * <h2>Requirements</h2>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://discord.com/developers/applications" target="_blank">Discord Developers Portal</a>.
 */
public class MessageCommandEvent extends GenericCommandEvent implements MessageCommandInteraction
{
    private final MessageCommandInteractionImpl commandInteraction;

    public MessageCommandEvent(@NotNull JDA api, long responseNumber, @NotNull MessageCommandInteractionImpl interaction)
    {
        super(api, responseNumber, interaction);
        this.commandInteraction = interaction;
    }

    @Override
    public long getTargetIdLong()
    {
        return commandInteraction.getTargetIdLong();
    }

    @Override
    @Nonnull
    public Message getTargetMessage()
    {
        return commandInteraction.getTargetMessage();
    }
}
