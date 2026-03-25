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

package net.dv8tion.jda.api.entities.channel;

import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.SoundboardSound;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents an emoji effect or a soundboard sound effect.
 */
public class VoiceChannelEffect {
    private final VoiceChannel channel;
    private final long userId;
    private final EmojiUnion emoji;
    private final Animation animation;
    private final SoundboardSound soundboardSound;
    private final double soundVolume;

    public VoiceChannelEffect(
            VoiceChannel channel,
            long userId,
            EmojiUnion emoji,
            Animation animation,
            SoundboardSound soundboardSound,
            double soundVolume) {
        this.channel = channel;
        this.userId = userId;
        this.emoji = emoji;
        this.animation = animation;
        this.soundboardSound = soundboardSound;
        this.soundVolume = soundVolume;
    }

    /**
     * The voice channel this effect was sent to.
     *
     * @return The voice channel this effect was sent to.
     */
    @Nonnull
    public VoiceChannel getChannel() {
        return channel;
    }

    /**
     * The ID of the user which sent this effect.
     *
     * @return ID of the user which sent this effect.
     */
    @Nonnull
    public String getUserId() {
        return Long.toUnsignedString(userId);
    }

    /**
     * The user which sent this effect.
     *
     * @return ID of the user which sent this effect.
     */
    public long getUserIdLong() {
        return userId;
    }

    /**
     * The user which sent this effect.
     * <br>This may be {@code null} if the user is not {@linkplain net.dv8tion.jda.api.utils.MemberCachePolicy cached}.
     *
     * @return The user which sent this effect, or {@code null}.
     */
    @Nullable
    public User getUser() {
        return channel.getJDA().getUserById(userId);
    }

    /**
     * The emoji sent with the effect, this is only present for emoji effects.
     *
     * @return The emoji sent with the effect, or {@code null}
     */
    @Nullable
    public EmojiUnion getEmoji() {
        return emoji;
    }

    /**
     * The animation of the emoji, this is only present for emoji effects.
     *
     * @return The animation of the emoji, or {@code null}
     */
    @Nullable
    public Animation getAnimation() {
        return animation;
    }

    /**
     * The soundboard sound sent with the effect, this is only present for soundboard sound effects.
     *
     * @return The soundboard sound sent with the effect, or {@code null}
     */
    @Nullable
    public SoundboardSound getSoundboardSound() {
        return soundboardSound;
    }

    /**
     * The volume at which a soundboard sound was sent, this is {@code 0} if this isn't a soundboard sound effect.
     *
     * @return The soundboard sound's volume, or {@code 0}
     */
    public double getSoundVolume() {
        return soundVolume;
    }

    /**
     * Represents the animation used in emoji effects.
     */
    public static class Animation implements ISnowflake {
        public Animation(long id, Animation.Type type) {
            this.id = id;
            this.type = type;
        }

        private final long id;
        private final Animation.Type type;

        @Override
        public long getIdLong() {
            return id;
        }

        /**
         * The type of animation
         *
         * @return The type of animation
         */
        @Nonnull
        public Animation.Type getType() {
            return type;
        }

        /**
         * Represents the animation type used in emoji effects.
         */
        public enum Type {
            UNKNOWN(-1),
            PREMIUM(0),
            BASIC(1);

            private final int value;

            Type(int value) {
                this.value = value;
            }

            /**
             * The raw value of this animation type.
             *
             * @return The raw value
             */
            public int getValue() {
                return value;
            }

            /**
             * Retrieves the animation type from the raw value.
             *
             * @param  value
             *         The raw value of the animation type
             *
             * @return The animation type, or {@link #UNKNOWN} for invalid values
             */
            @Nonnull
            public static Animation.Type fromValue(int value) {
                for (Animation.Type type : values()) {
                    if (type.value == value) {
                        return type;
                    }
                }

                return UNKNOWN;
            }
        }
    }
}
