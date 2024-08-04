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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.StageInstance;
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel;
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.channel.concrete.StageChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.StageInstanceAction;
import net.dv8tion.jda.internal.requests.restaction.StageInstanceActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Represents a Stage Channel.
 *
 * <p>This is a specialized AudioChannel that can be used to host events with speakers and listeners.
 */
public interface StageChannel extends StandardGuildChannel, GuildMessageChannel, AudioChannel, IWebhookContainer, IAgeRestrictedChannel, ISlowmodeChannel
{
    /**
     * The maximum limit you can set with {@link StageChannelManager#setUserLimit(int)}. ({@value})
     */
    int MAX_USERLIMIT = 10000;

    /**
     * {@link StageInstance} attached to this stage channel.
     *
     * <p>This indicates whether a stage channel is currently "live".
     *
     * @return The {@link StageInstance} or {@code null} if this stage is not live
     */
    @Nullable
    StageInstance getStageInstance();

    /**
     * Create a new {@link StageInstance} for this stage channel.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#STAGE_ALREADY_OPEN STAGE_ALREADY_OPEN}
     *     <br>If there already is an active {@link StageInstance} for this channel</li>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If the channel was deleted</li>
     * </ul>
     *
     * @param  topic
     *         The topic of this stage instance, must be 1-120 characters long
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the self member is not a stage moderator. (See {@link #isModerator(Member)})
     * @throws IllegalArgumentException
     *         If the topic is null, empty, or longer than 120 characters
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link StageInstanceAction}
     */
    @Nonnull
    @CheckReturnValue
    default StageInstanceAction createStageInstance(@Nonnull String topic)
    {
        Checks.checkAttached(this);
        EnumSet<Permission> permissions = getGuild().getSelfMember().getPermissions(this);
        EnumSet<Permission> required = EnumSet.of(Permission.MANAGE_CHANNEL, Permission.VOICE_MUTE_OTHERS, Permission.VOICE_MOVE_OTHERS);
        for (Permission perm : required)
        {
            if (!permissions.contains(perm))
                throw new InsufficientPermissionException(this, perm, "You must be a stage moderator to create a stage instance! Missing Permission: " + perm);
        }

        return new StageInstanceActionImpl(this).setTopic(topic);
    }

    /**
     * Whether this member is considered a moderator for this stage channel.
     * <br>Moderators can modify the {@link #getStageInstance() Stage Instance} and promote speakers.
     * To promote a speaker you can use {@link GuildVoiceState#inviteSpeaker()} or {@link GuildVoiceState#approveSpeaker()} if they have already raised their hand (indicated by {@link GuildVoiceState#getRequestToSpeakTimestamp()}).
     * A stage moderator can move between speaker and audience without raising their hand. This can be done with {@link Guild#requestToSpeak()} and {@link Guild#cancelRequestToSpeak()} respectively.
     *
     * <p>A member is considered a stage moderator if they have these permissions in the stage channel:
     * <ul>
     *     <li>{@link Permission#MANAGE_CHANNEL}</li>
     *     <li>{@link Permission#VOICE_MUTE_OTHERS}</li>
     *     <li>{@link Permission#VOICE_MOVE_OTHERS}</li>
     * </ul>
     *
     * @param  member
     *         The member to check
     *
     * @throws IllegalArgumentException
     *         If the provided member is null or not from this guild
     *
     * @return True, if the provided member is a stage moderator
     */
    default boolean isModerator(@Nonnull Member member)
    {
        Checks.notNull(member, "Member");
        return member.hasPermission(this, Permission.MANAGE_CHANNEL, Permission.VOICE_MUTE_OTHERS, Permission.VOICE_MOVE_OTHERS);
    }

    @Nonnull
    @Override
    ChannelAction<StageChannel> createCopy(@Nonnull Guild guild);

    @Nonnull
    @Override
    default ChannelAction<StageChannel> createCopy()
    {
        return createCopy(getGuild());
    }

    @Nonnull
    @Override
    StageChannelManager getManager();

    /**
     * Sends a {@link GuildVoiceState#getRequestToSpeakTimestamp() request-to-speak} indicator to the stage instance moderators.
     * <p>If the self member has {@link Permission#VOICE_MUTE_OTHERS} this will immediately promote them to speaker.
     *
     * @throws IllegalStateException
     *         If the self member is not currently connected to the channel
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link RestAction}
     *
     * @see    #cancelRequestToSpeak()
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> requestToSpeak();

    /**
     * Cancels the {@link #requestToSpeak() Request-to-Speak}.
     * <br>This can also be used to move back to the audience if you are currently a speaker.
     *
     * <p>If there is no request to speak or the member is not currently connected to an active {@link StageInstance}, this does nothing.
     *
     * @throws IllegalStateException
     *         If the self member is not currently connected to the channel
     * @throws net.dv8tion.jda.api.exceptions.DetachedEntityException
     *         if the bot {@link Guild#isDetached() isn't in the guild}.
     *
     * @return {@link RestAction}
     *
     * @see    #requestToSpeak()
     */
    @Nonnull
    @CheckReturnValue
    RestAction<Void> cancelRequestToSpeak();
}
