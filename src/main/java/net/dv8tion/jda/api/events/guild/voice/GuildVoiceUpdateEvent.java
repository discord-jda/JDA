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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.UpdateEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.Member Member} joined or left an {@link AudioChannel}.
 * <p>Can be used to detect when a Member leaves/joins an AudioChannel.
 *
 * <p><b>Example</b><br>
 * <pre>{@code
 * AudioChannelUnion joinedChannel = event.getChannelJoined();
 * AudioChannelUnion leftChannel = event.getChannelLeft();
 *
 * if (joinedChannel != null) {
 *   // the member joined an audio channel
 * }
 * if (leftChannel != null) {
 *   // the member left an audio channel
 * }
 * if (joinedChannel != null && leftChannel != null) {
 *   // the member moved between two audio channels in the same guild
 * }
 * }</pre>
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.utils.cache.CacheFlag#VOICE_STATE VOICE_STATE} CacheFlag to be enabled, which requires
 * the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_VOICE_STATES GUILD_VOICE_STATES} intent.
 *
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disables that CacheFlag by default!
 *
 * <p>Additionally, this event requires the {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
 * to cache the updated members. Discord does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 *
 * <p>Identifier: {@code audio-channel}
 */
public class GuildVoiceUpdateEvent extends GenericGuildVoiceEvent implements UpdateEvent<Member, AudioChannel>
{
    public static final String IDENTIFIER = "audio-channel";

    private final AudioChannel previous;
    private final AudioChannel next;

    public GuildVoiceUpdateEvent(@Nonnull JDA api, long responseNumber, @Nonnull Member member, @Nullable AudioChannel previous)
    {
        super(api, responseNumber, member);
        this.previous = previous;
        this.next = member.getVoiceState().getChannel();
    }

    /**
     * The {@link AudioChannelUnion} that the {@link Member} is moved from
     *
     * @return The {@link AudioChannelUnion}, or {@code null} if the member was not connected to a channel before
     */
    @Nullable
    public AudioChannelUnion getChannelLeft()
    {
        return (AudioChannelUnion) previous;
    }

    /**
     * The {@link AudioChannelUnion} that was joined
     *
     * @return The {@link AudioChannelUnion}, or {@code null} if the member has disconnected
     */
    @Nullable
    public AudioChannelUnion getChannelJoined()
    {
        return (AudioChannelUnion) next;
    }

    @Nonnull
    @Override
    public String getPropertyIdentifier()
    {
        return IDENTIFIER;
    }

    @Nonnull
    @Override
    public Member getEntity()
    {
        return member;
    }

    @Nullable
    @Override
    public AudioChannel getOldValue()
    {
        return previous;
    }

    @Nullable
    @Override
    public AudioChannel getNewValue()
    {
        return next;
    }
}
