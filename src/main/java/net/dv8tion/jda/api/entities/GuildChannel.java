package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.managers.channel.ChannelManager;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Represents a {@link net.dv8tion.jda.api.entities.Guild Guild} channel.
 *
 * @see Guild#getGuildChannelById(long)
 * @see Guild#getGuildChannelById(ChannelType, long)
 *
 * @see JDA#getGuildChannelById(long)
 * @see JDA#getGuildChannelById(ChannelType, long)
 */
public interface GuildChannel extends Channel, Comparable<GuildChannel>
{
    /**
     * Returns the {@link net.dv8tion.jda.api.entities.Guild Guild} that this GuildChannel is part of.
     *
     * @return Never-null {@link net.dv8tion.jda.api.entities.Guild Guild} that this GuildChannel is part of.
     */
    @Nonnull
    Guild getGuild();

    /**
     * Returns the {@link ChannelManager ChannelManager} for this GuildChannel.
     * <br>In the ChannelManager, you can modify the name, topic and position of this GuildChannel.
     * You modify multiple fields in one request by chaining setters before calling {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction.queue()}.
     *
     * <p>This is a lazy idempotent getter. The manager is retained after the first call.
     * This getter is not thread-safe and would require guards by the user.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL Permission.MANAGE_CHANNEL}
     *
     * @return The ChannelManager of this GuildChannel
     */
    @Nonnull
    ChannelManager<?, ?> getManager();

    /**
     * TODO-v5: this override might not be needed anymore if we remove AuditableRestAction and instead place auditable hooks onto RestAction itself.
     * Deletes this GuildChannel.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If this channel was already deleted</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost
     *         {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL Permission.MANAGE_CHANNEL} in the channel.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the Guild</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         if the currently logged in account doesn't have {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL}
     *         for the channel.
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Override
    @Nonnull
    @CheckReturnValue
    AuditableRestAction<Void> delete();

    //TODO-v5: Docs
    IPermissionContainer getPermissionContainer();
}
