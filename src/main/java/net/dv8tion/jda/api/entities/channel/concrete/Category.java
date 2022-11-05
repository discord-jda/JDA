/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.api.entities.channel.concrete;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.attribute.ICopyableChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IMemberContainer;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.attribute.IPositionableChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.managers.channel.concrete.CategoryManager;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction;
import net.dv8tion.jda.api.requests.restaction.order.ChannelOrderAction;
import net.dv8tion.jda.api.requests.restaction.order.OrderAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a channel category in the official Discord API.
 * <br>Categories are used to keep order in a Guild by dividing the channels into groups.
 *
 * @since 3.4.0
 *
 * @see   Guild#getCategoryCache()
 * @see   Guild#getCategories()
 * @see   Guild#getCategoriesByName(String, boolean)
 * @see   Guild#getCategoryById(long)
 *
 * @see   JDA#getCategoryCache()
 * @see   JDA#getCategories()
 * @see   JDA#getCategoriesByName(String, boolean)
 * @see   JDA#getCategoryById(long)
 */
public interface Category extends GuildChannel, ICopyableChannel, IPositionableChannel, IPermissionContainer, IMemberContainer
{
    /**
     * All {@link GuildChannel Channels} listed for this Category.
     * <br>Includes all types of channels, except for threads.
     *
     * @return Immutable list of all child channels
     */
    @Nonnull
    default List<GuildChannel> getChannels()
    {
        List<GuildChannel> channels = new ArrayList<>();
        channels.addAll(getTextChannels());
        channels.addAll(getVoiceChannels());
        channels.addAll(getStageChannels());
        channels.addAll(getNewsChannels());
        channels.addAll(getForumChannels());
        Collections.sort(channels);

        return Collections.unmodifiableList(channels);
    }

    /**
     * All {@link TextChannel TextChannels}
     * listed for this Category
     *
     * @return Immutable list of all child TextChannels
     */
    @Nonnull
    default List<TextChannel> getTextChannels()
    {
        return Collections.unmodifiableList(getGuild().getTextChannelCache().applyStream(stream ->
            stream.filter(channel -> equals(channel.getParentCategory()))
                  .sorted()
                  .collect(Collectors.toList())
        ));
    }

    /**
     * All {@link NewsChannel NewsChannels}
     * listed for this Category
     *
     * @return Immutable list of all child NewsChannels
     */
    @Nonnull
    default List<NewsChannel> getNewsChannels()
    {
        return Collections.unmodifiableList(getGuild().getNewsChannelCache().applyStream(stream ->
            stream.filter(channel -> equals(channel.getParentCategory()))
                  .sorted()
                  .collect(Collectors.toList())
        ));
    }

    /**
     * All {@link net.dv8tion.jda.api.entities.channel.concrete.ForumChannel ForumChannels} listed for this Category
     *
     * @return Immutable list of all child ForumChannels
     */
    @Nonnull
    default List<ForumChannel> getForumChannels()
    {
        return Collections.unmodifiableList(getGuild().getForumChannelCache().applyStream(stream ->
            stream.filter(channel -> equals(channel.getParentCategory()))
                  .sorted()
                  .collect(Collectors.toList())
        ));
    }

    /**
     * All {@link VoiceChannel VoiceChannels}
     * listed for this Category
     *
     * @return Immutable list of all child VoiceChannels
     */
    @Nonnull
    default List<VoiceChannel> getVoiceChannels()
    {
        return Collections.unmodifiableList(getGuild().getVoiceChannelCache().applyStream(stream ->
            stream.filter(channel -> equals(channel.getParentCategory()))
                  .sorted()
                  .collect(Collectors.toList())
        ));
    }

    /**
     * All {@link StageChannel StageChannel}
     * listed for this Category
     *
     * @return Immutable list of all child StageChannel
     */
    @Nonnull
    default List<StageChannel> getStageChannels()
    {
        return Collections.unmodifiableList(getGuild().getStageChannelCache().applyStream(stream ->
            stream.filter(channel -> equals(channel.getParentCategory()))
                  .sorted()
                  .collect(Collectors.toList())
        ));
    }

    /**
     * Creates a new {@link TextChannel TextChannel} with this Category as parent.
     * For this to be successful, the logged in account has to have the
     * {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission in this Category.
     *
     * <p>This will copy all {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides} of this Category!
     * Unless the bot is unable to sync it with this category due to permission escalation.
     * See {@link IPermissionHolder#canSync(IPermissionContainer, IPermissionContainer)} for details.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} permission was removed</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_CHANNELS MAX_CHANNELS}
     *     <br>The maximum number of channels were exceeded</li>
     * </ul>
     *
     * @param  name
     *         The name of the TextChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL} permission
     * @throws IllegalArgumentException
     *         If the provided name is {@code null}, empty, or longer than {@value Channel#MAX_NAME_LENGTH} characters
     *
     * @return A specific {@link ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new TextChannel before creating it
     */
    @Nonnull
    @CheckReturnValue
    ChannelAction<TextChannel> createTextChannel(@Nonnull String name);

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.channel.concrete.NewsChannel NewsChannel} with this Category as parent.
     * For this to be successful, the logged in account has to have the
     * {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission in this Category.
     *
     * <p>This will copy all {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides} of this Category!
     * Unless the bot is unable to sync it with this category due to permission escalation.
     * See {@link IPermissionHolder#canSync(IPermissionContainer, IPermissionContainer)} for details.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} permission was removed</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_CHANNELS MAX_CHANNELS}
     *     <br>The maximum number of channels were exceeded</li>
     * </ul>
     *
     * @param  name
     *         The name of the NewsChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL} permission
     * @throws IllegalArgumentException
     *         If the provided name is {@code null}, empty, or longer than {@value Channel#MAX_NAME_LENGTH} characters
     *
     * @return A specific {@link ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new NewsChannel before creating it
     */
    @Nonnull
    @CheckReturnValue
    ChannelAction<NewsChannel> createNewsChannel(@Nonnull String name);

    /**
     * Creates a new {@link VoiceChannel VoiceChannel} with this Category as parent.
     * For this to be successful, the logged in account has to have the
     * {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission in this Category.
     *
     * <p>This will copy all {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides} of this Category!
     * Unless the bot is unable to sync it with this category due to permission escalation.
     * See {@link IPermissionHolder#canSync(IPermissionContainer, IPermissionContainer)} for details.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} permission was removed</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_CHANNELS MAX_CHANNELS}
     *     <br>The maximum number of channels were exceeded</li>
     * </ul>
     *
     * @param  name
     *         The name of the VoiceChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL} permission
     * @throws IllegalArgumentException
     *         If the provided name is {@code null}, empty, or longer than {@value Channel#MAX_NAME_LENGTH} characters
     *
     * @return A specific {@link ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new VoiceChannel before creating it
     */
    @Nonnull
    @CheckReturnValue
    ChannelAction<VoiceChannel> createVoiceChannel(@Nonnull String name);

    /**
     * Creates a new {@link StageChannel StageChannel} with this Category as parent.
     * For this to be successful, the logged in account has to have the
     * {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission in this Category.
     *
     * <p>This will copy all {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides} of this Category!
     * Unless the bot is unable to sync it with this category due to permission escalation.
     * See {@link IPermissionHolder#canSync(IPermissionContainer, IPermissionContainer)} for details.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} permission was removed</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_CHANNELS MAX_CHANNELS}
     *     <br>The maximum number of channels were exceeded</li>
     * </ul>
     *
     * @param  name
     *         The name of the StageChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL} permission
     * @throws IllegalArgumentException
     *         If the provided name is {@code null}, empty, or longer than {@value Channel#MAX_NAME_LENGTH} characters
     *
     * @return A specific {@link ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new StageChannel before creating it
     */
    @Nonnull
    @CheckReturnValue
    ChannelAction<StageChannel> createStageChannel(@Nonnull String name);

    /**
     * Creates a new {@link ForumChannel} with this Category as parent.
     * For this to be successful, the logged in account has to have the
     * {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission in this Category.
     *
     * <p>This will copy all {@link net.dv8tion.jda.api.entities.PermissionOverride PermissionOverrides} of this Category!
     * Unless the bot is unable to sync it with this category due to permission escalation.
     * See {@link IPermissionHolder#canSync(IPermissionContainer, IPermissionContainer)} for details.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The {@link net.dv8tion.jda.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} permission was removed</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_CHANNELS MAX_CHANNELS}
     *     <br>The maximum number of channels were exceeded</li>
     * </ul>
     *
     * @param  name
     *         The name of the ForumChannel to create (up to {@value Channel#MAX_NAME_LENGTH} characters)
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL} permission
     * @throws IllegalArgumentException
     *         If the provided name is {@code null}, empty, or longer than {@value Channel#MAX_NAME_LENGTH} characters
     *
     * @return A specific {@link ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new ForumChannel before creating it
     */
    @Nonnull
    @CheckReturnValue
    ChannelAction<ForumChannel> createForumChannel(@Nonnull String name);

    /**
     * Modifies the positional order of this Category's nested {@link #getTextChannels() TextChannels} and {@link #getNewsChannels() NewsChannels}.
     * <br>This uses an extension of {@link ChannelOrderAction ChannelOrderAction}
     * specialized for ordering the nested {@link TextChannel TextChannels}
     * and {@link NewsChannel NewsChannels} of this {@link Category Category}.
     * <br>Like {@link ChannelOrderAction}, the returned {@link CategoryOrderAction CategoryOrderAction}
     * can be used to move TextChannels/NewsChannels {@link OrderAction#moveUp(int) up},
     * {@link OrderAction#moveDown(int) down}, or
     * {@link OrderAction#moveTo(int) to} a specific position.
     * <br>This uses <b>ascending</b> order with a 0 based index.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNNKOWN_CHANNEL}
     *     <br>One of the channels has been deleted before the completion of the task.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild.</li>
     * </ul>
     *
     * @return A {@link CategoryOrderAction CategoryOrderAction} for
     *         ordering the Category's {@link TextChannel TextChannels}
     *         and {@link NewsChannel NewsChannels}.
     */
    @Nonnull
    @CheckReturnValue
    CategoryOrderAction modifyTextChannelPositions();

    /**
     * Modifies the positional order of this Category's nested {@link #getVoiceChannels() VoiceChannels} and {@link #getStageChannels() StageChannels}.
     * <br>This uses an extension of {@link ChannelOrderAction ChannelOrderAction}
     * specialized for ordering the nested {@link VoiceChannel VoiceChannels}
     * and {@link StageChannel StageChannels} of this {@link Category Category}.
     * <br>Like {@link ChannelOrderAction}, the returned {@link CategoryOrderAction CategoryOrderAction}
     * can be used to move VoiceChannels/StageChannels {@link OrderAction#moveUp(int) up},
     * {@link OrderAction#moveDown(int) down}, or
     * {@link OrderAction#moveTo(int) to} a specific position.
     * <br>This uses <b>ascending</b> order with a 0 based index.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNNKOWN_CHANNEL}
     *     <br>One of the channels has been deleted before the completion of the task.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild.</li>
     * </ul>
     *
     * @return A {@link CategoryOrderAction CategoryOrderAction} for
     *         ordering the Category's {@link VoiceChannel VoiceChannels}
     *         and {@link StageChannel StageChannels}.
     */
    @Nonnull
    @CheckReturnValue
    CategoryOrderAction modifyVoiceChannelPositions();

    @Nonnull
    @Override
    default List<Member> getMembers()
    {
        return Collections.unmodifiableList(getChannels().stream()
            .filter(IMemberContainer.class::isInstance)
            .map(IMemberContainer.class::cast)
            .map(IMemberContainer::getMembers)
            .flatMap(List::stream)
            .distinct()
            .collect(Collectors.toList()));
    }

    @Nonnull
    @Override
    ChannelAction<Category> createCopy(@Nonnull Guild guild);

    @Nonnull
    @Override
    ChannelAction<Category> createCopy();

    @Nonnull
    @Override
    CategoryManager getManager();
}
