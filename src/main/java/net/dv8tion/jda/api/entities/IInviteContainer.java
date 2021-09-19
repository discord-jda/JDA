package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.InviteAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;

//TODO-v5: Need Docs
//TODO-v5: Should this actually be extending GuildChannel?
public interface IInviteContainer extends GuildChannel
{
    /**
     * Creates a new {@link InviteAction InviteAction} which can be used to create a
     * new {@link net.dv8tion.jda.api.entities.Invite Invite}.
     * <br>Requires {@link net.dv8tion.jda.api.Permission#CREATE_INSTANT_INVITE CREATE_INSTANT_INVITE} in this channel.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the account does not have {@link net.dv8tion.jda.api.Permission#CREATE_INSTANT_INVITE CREATE_INSTANT_INVITE} in this channel
     * @throws java.lang.IllegalArgumentException
     *         If this is an instance of a {@link net.dv8tion.jda.api.entities.Category Category}
     *
     * @return A new {@link InviteAction InviteAction}
     *
     * @see    InviteAction
     */
    @Nonnull
    @CheckReturnValue
    InviteAction createInvite();

    /**
     * Returns all invites for this channel.
     * <br>Requires {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in this channel.
     * Will throw an {@link net.dv8tion.jda.api.exceptions.InsufficientPermissionException InsufficientPermissionException} otherwise.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         if the account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in this channel
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction} - Type: List{@literal <}{@link net.dv8tion.jda.api.entities.Invite Invite}{@literal >}
     *         <br>The list of expanded Invite objects
     *
     * @see    net.dv8tion.jda.api.entities.Guild#retrieveInvites()
     */
    @Nonnull
    @CheckReturnValue
    RestAction<List<Invite>> retrieveInvites();
}
