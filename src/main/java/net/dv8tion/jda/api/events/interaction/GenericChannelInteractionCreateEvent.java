package net.dv8tion.jda.api.events.interaction;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.interactions.ChannelInteraction;
import net.dv8tion.jda.api.interactions.ChannelInteractionHook;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * Indicates that an {@link ChannelInteraction} was created in a channel.
 *
 * <h2>Requirements</h2>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://discord.com/developers/applications" target="_blank">Discord Developers Portal</a>.
 */
public class GenericChannelInteractionCreateEvent extends GenericInteractionCreateEvent implements ChannelInteraction
{
    private final ChannelInteraction interaction;

    public GenericChannelInteractionCreateEvent(@Nonnull JDA api, long responseNumber, @Nonnull ChannelInteraction interaction)
    {
        super(api, responseNumber, interaction);
        this.interaction = interaction;
    }

    @Nullable
    @Override
    public Channel getChannel()
    {
        return interaction.getChannel();
    }

    @NotNull
    @Override
    public ChannelInteractionHook getHook()
    {
        return interaction.getHook();
    }

    @Override
    public boolean isAcknowledged()
    {
        return interaction.isAcknowledged();
    }

    @NotNull
    @Override
    public ReplyAction deferReply()
    {
        return interaction.deferReply();
    }
}
