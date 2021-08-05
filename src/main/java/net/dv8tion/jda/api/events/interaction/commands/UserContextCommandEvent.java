package net.dv8tion.jda.api.events.interaction.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.interactions.UserCommandInteraction;
import net.dv8tion.jda.internal.interactions.commands.UserCommandInteractionImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a user command was used in a {@link MessageChannel}.
 *
 * <h2>Requirements</h2>
 * To receive these events, you must unset the <b>Interactions Endpoint URL</b> in your application dashboard.
 * You can simply remove the URL for this endpoint in your settings at the <a href="https://discord.com/developers/applications" target="_blank">Discord Developers Portal</a>.
 */
public class UserContextCommandEvent extends GenericCommandEvent implements UserCommandInteraction
{
    private final UserCommandInteractionImpl commandInteraction;

    public UserContextCommandEvent(@NotNull JDA api, long responseNumber, @NotNull UserCommandInteractionImpl interaction)
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
    @Nullable
    public Member getTargetMember()
    {
        return commandInteraction.getTargetMember();
    }
}
