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

package net.dv8tion.jda.api.events.guild.voice;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;

/**
 * Indicates that a guild member has updated their {@link GuildVoiceState#getRequestToSpeakTimestamp() Request-to-Speak}.
 *
 * <p>If {@link #getNewTime()} is non-null, this means the member has <em>raised their hand</em> and wants to speak.
 * You can use {@link #approveSpeaker()} or {@link #declineSpeaker()} to handle this request if you have {@link net.dv8tion.jda.api.Permission#VOICE_MUTE_OTHERS Permission.VOICE_MUTE_OTHERS}.
 *
 * <h2>Requirements</h2>
 *
 * <p>These events require the {@link net.dv8tion.jda.api.utils.cache.CacheFlag#VOICE_STATE VOICE_STATE} CacheFlag to be enabled, which requires
 * the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_VOICE_STATES GUILD_VOICE_STATES} intent.
 *
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disables that CacheFlag by default!
 *
 * <p>Additionally, these events require the {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
 * to cache the updated members. Discord does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire specific events like these we
 * need to have the old member cached to compare against.
 */
public class GuildVoiceRequestToSpeakEvent extends GenericGuildVoiceEvent
{
    private final OffsetDateTime oldTime, newTime;

    public GuildVoiceRequestToSpeakEvent(@Nonnull JDA api, long responseNumber, @Nonnull Member member,
                                         @Nullable OffsetDateTime oldTime, @Nullable OffsetDateTime newTime)
    {
        super(api, responseNumber, member);
        this.oldTime = oldTime;
        this.newTime = newTime;
    }

    /**
     * The old {@link GuildVoiceState#getRequestToSpeakTimestamp()}
     *
     * @return The old timestamp, or null if this member did not request to speak before
     */
    @Nullable
    public OffsetDateTime getOldTime()
    {
        return oldTime;
    }

    /**
     * The new {@link GuildVoiceState#getRequestToSpeakTimestamp()}
     *
     * @return The new timestamp, or null if the request to speak was declined or cancelled
     */
    @Nullable
    public OffsetDateTime getNewTime()
    {
        return newTime;
    }

    /**
     * Promote the member to speaker.
     * <p>This requires a non-null {@link #getNewTime()}.
     * You can use {@link GuildVoiceState#inviteSpeaker()} to invite the member to become a speaker if they haven't requested to speak.
     *
     * <p>This does nothing if the member is not connected to a {@link StageChannel}.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#VOICE_MUTE_OTHERS Permission.VOICE_MUTE_OTHERS}
     *         in the associated {@link StageChannel}
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    public RestAction<Void> approveSpeaker()
    {
        return getVoiceState().approveSpeaker();
    }

    /**
     * Reject this members {@link GuildVoiceState#getRequestToSpeakTimestamp() request to speak}.
     * <p>This requires a non-null {@link #getNewTime()}.
     * The member will have to request to speak again.
     *
     * <p>This does nothing if the member is not connected to a {@link StageChannel}.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#VOICE_MUTE_OTHERS Permission.VOICE_MUTE_OTHERS}
     *         in the associated {@link StageChannel}
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    public RestAction<Void> declineSpeaker()
    {
        return getVoiceState().declineSpeaker();
    }
}
