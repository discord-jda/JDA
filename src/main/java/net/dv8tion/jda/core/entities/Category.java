package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.requests.restaction.ChannelAction;

import javax.annotation.CheckReturnValue;
import java.util.List;

/**
 * Represents a channel category in the official Discord API.
 * <br>Categories are used to keep order in a Guild by dividing the channels into groups.
 */
public interface Category extends Channel, Comparable<Category>
{
    /**
     * All {@link net.dv8tion.jda.core.entities.Channel Channels} listed
     * for this Category
     * <br>This may contain both {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}
     * and {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}!
     *
     * @return Immutable list of all child channels
     */
    List<Channel> getChannels();

    /**
     * All {@link net.dv8tion.jda.core.entities.TextChannel TextChannels}
     * listed for this Category
     *
     * @return Immutable list of all child TextChannels
     */
    List<TextChannel> getTextChannels();

    /**
     * All {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannels}
     * listed for this Category
     *
     * @return Immutable list of all child VoiceChannels
     */
    List<VoiceChannel> getVoiceChannels();

    /**
     * Creates a new {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} with this Category as parent.
     * For this to be successful, the logged in account has to have the
     * {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission in the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * <p>This will copy all {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides} of this Category!
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  name
     *         The name of the TextChannel to create
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL} permission
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or less than 2 characters or greater than 100 characters in length
     *
     * @return A specific {@link net.dv8tion.jda.core.requests.restaction.ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new TextChannel before creating it
     */
    @CheckReturnValue
    ChannelAction createTextChannel(String name);

    /**
     * Creates a new {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} with this Category as parent.
     * For this to be successful, the logged in account has to have the
     * {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission in the {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * <p>This will copy all {@link net.dv8tion.jda.core.entities.PermissionOverride PermissionOverrides} of this Category!
     *
     * <p>Possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.core.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  name
     *         The name of the VoiceChannel to create
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.core.Permission#MANAGE_CHANNEL} permission
     * @throws net.dv8tion.jda.core.exceptions.GuildUnavailableException
     *         If the guild is temporarily not {@link net.dv8tion.jda.core.entities.Guild#isAvailable() available}
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or less than 2 characters or greater than 100 characters in length
     *
     * @return A specific {@link net.dv8tion.jda.core.requests.restaction.ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new VoiceChannel before creating it
     */
    @CheckReturnValue
    ChannelAction createVoiceChannel(String name);
}
