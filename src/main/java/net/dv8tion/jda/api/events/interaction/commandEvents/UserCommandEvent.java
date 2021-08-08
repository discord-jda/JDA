package net.dv8tion.jda.api.events.interaction.commandEvents;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.commandInteractions.UserCommandInteraction;
import net.dv8tion.jda.internal.interactions.commandInteractionImpls.UserCommandInteractionImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Indicates that a user command was used in a {@link MessageChannel}.
 *
 * <h2>Requirements</h2>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://discord.com/developers/applications" target="_blank">Discord Developers Portal</a>.
 */
public class UserCommandEvent extends GenericCommandEvent implements UserCommandInteraction
{
    private final UserCommandInteractionImpl commandInteraction;

    public UserCommandEvent(@NotNull JDA api, long responseNumber, @NotNull UserCommandInteractionImpl interaction)
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
    public User getTargetUser()
    {
        return commandInteraction.getTargetUser();
    }

    @Override
    public Member getTargetMember()
    {
        return commandInteraction.getTargetMember();
    }
}
