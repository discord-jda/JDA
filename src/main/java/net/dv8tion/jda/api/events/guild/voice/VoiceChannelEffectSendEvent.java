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
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.Member Member} sent an effect in a voice channel.
 * <br>This includes emoji reactions and soundboard sounds.
 *
 * <p>Can be used to detect when a member uses a soundboard sound or sends an emoji reaction in a voice channel.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_VOICE_STATES GUILD_VOICE_STATES} intent to be enabled.
 * <br>Additionally, the bot must be connected to the voice channel to receive this event.
 *
 * <p><b>Example</b><br>
 * <pre>{@code
 * public void onVoiceChannelEffectSend(VoiceChannelEffectSendEvent event)
 * {
 *     if (event.isSoundboard())
 *     {
 *         System.out.printf("%s used soundboard sound %s%n",
 *             event.getMember().getEffectiveName(), event.getSoundId());
 *     }
 * }
 * }</pre>
 *
 * @see #isSoundboard()
 * @see #getSoundId()
 * @see #getEmoji()
 */
public class VoiceChannelEffectSendEvent extends GenericGuildVoiceEvent {
    private final AudioChannelUnion channel;
    private final EmojiUnion emoji;
    private final AnimationType animationType;
    private final long animationId;
    private final long soundId;
    private final double soundVolume;

    public VoiceChannelEffectSendEvent(
            @Nonnull JDA api,
            long responseNumber,
            @Nonnull Member member,
            @Nonnull AudioChannelUnion channel,
            @Nullable EmojiUnion emoji,
            @Nullable AnimationType animationType,
            long animationId,
            long soundId,
            double soundVolume) {
        super(api, responseNumber, member);
        this.channel = channel;
        this.emoji = emoji;
        this.animationType = animationType;
        this.animationId = animationId;
        this.soundId = soundId;
        this.soundVolume = soundVolume;
    }

    /**
     * The {@link AudioChannelUnion} where the effect was sent.
     *
     * @return The audio channel
     */
    @Nonnull
    public AudioChannelUnion getChannel() {
        return channel;
    }

    /**
     * The emoji sent with this effect, if any.
     * <br>This is present for both emoji reactions and soundboard effects that have an associated emoji.
     *
     * @return The {@link EmojiUnion}, or {@code null} if no emoji was included
     */
    @Nullable
    public EmojiUnion getEmoji() {
        return emoji;
    }

    /**
     * The {@link AnimationType} of the emoji animation, if any.
     *
     * @return The animation type, or {@code null} if no animation was included
     */
    @Nullable
    public AnimationType getAnimationType() {
        return animationType;
    }

    /**
     * The ID of the emoji animation, or {@code 0} if no animation was included.
     *
     * @return The animation id
     */
    public long getAnimationId() {
        return animationId;
    }

    /**
     * The ID of the soundboard sound, or {@code 0} if this is not a soundboard effect.
     * <br>Default soundboard sounds have integer IDs, while guild soundboard sounds have snowflake IDs.
     *
     * @return The sound id, or {@code 0}
     *
     * @see #isSoundboard()
     */
    public long getSoundId() {
        return soundId;
    }

    /**
     * The volume of the soundboard sound, from 0 to 1.
     * <br>Returns {@code 0} if this is not a soundboard effect.
     *
     * @return The sound volume
     *
     * @see #isSoundboard()
     */
    public double getSoundVolume() {
        return soundVolume;
    }

    /**
     * Whether this effect is a soundboard sound.
     *
     * @return True, if this effect is a soundboard sound
     */
    public boolean isSoundboard() {
        return soundId != 0;
    }

    /**
     * The type of emoji animation.
     */
    public enum AnimationType {
        /** A fun animation, sent by a Nitro subscriber */
        PREMIUM(0),
        /** The standard animation */
        BASIC(1),
        /** Placeholder for unsupported types */
        UNKNOWN(-1);

        private final int key;

        AnimationType(int key) {
            this.key = key;
        }

        /**
         * The raw value used by Discord.
         *
         * @return The raw value
         */
        public int getKey() {
            return key;
        }

        /**
         * Resolves the provided raw value to the corresponding enum constant.
         *
         * @param  key
         *         The raw value
         *
         * @return The corresponding enum constant, or {@link #UNKNOWN}
         */
        @Nonnull
        public static AnimationType fromKey(int key) {
            for (AnimationType type : values()) {
                if (type.key == key) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }
}
