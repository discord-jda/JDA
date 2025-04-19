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
public class VoiceChannelEffect
{
    private final VoiceChannel channel;
    private final User user;
    private final EmojiUnion emoji;
    private final Animation animation;
    private final SoundboardSound soundboardSound;

    public VoiceChannelEffect(VoiceChannel channel, User user, EmojiUnion emoji, Animation animation, SoundboardSound soundboardSound)
    {
        this.channel = channel;
        this.user = user;
        this.emoji = emoji;
        this.animation = animation;
        this.soundboardSound = soundboardSound;
    }

    /**
     * The voice channel this effect was sent to.
     *
     * @return The voice channel this effect was sent to.
     */
    @Nonnull
    public VoiceChannel getChannel()
    {
        return channel;
    }

    /**
     * The user which sent this effect.
     *
     * @return The user which sent this effect.
     */
    @Nonnull
    public User getUser()
    {
        return user;
    }

    /**
     * The emoji sent with the effect, this is only present for emoji effects.
     *
     * @return The emoji sent with the effect, or {@code null}
     */
    @Nullable
    public EmojiUnion getEmoji()
    {
        return emoji;
    }

    /**
     * The animation of the emoji, this is only present for emoji effects.
     *
     * @return The animation of the emoji, or {@code null}
     */
    @Nullable
    public Animation getAnimation()
    {
        return animation;
    }

    /**
     * The soundboard sound sent with the effect, this is only present for soundboard sound effects.
     *
     * @return The soundboard sound sent with the effect, or {@code null}
     */
    @Nullable
    public SoundboardSound getSoundboardSound()
    {
        return soundboardSound;
    }

    /**
     * Represents the animation used in emoji effects.
     */
    public static class Animation implements ISnowflake
    {
        public Animation(long id, Animation.Type type)
        {
            this.id = id;
            this.type = type;
        }

        private final long id;
        private final Animation.Type type;

        @Override
        public long getIdLong()
        {
            return id;
        }

        /**
         * The type of animation
         *
         * @return The type of animation
         */
        @Nonnull
        public Animation.Type getType()
        {
            return type;
        }

        /**
         * Represents the animation type used in emoji effects.
         */
        public enum Type
        {
            UNKNOWN(-1),
            PREMIUM(0),
            BASIC(1);

            private final int value;

            Type(int value)
            {
                this.value = value;
            }

            /**
             * The raw value of this animation type.
             *
             * @return The raw value
             */
            public int getValue()
            {
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
            public static Animation.Type fromValue(int value)
            {
                for (Animation.Type type : values())
                {
                    if (type.value == value)
                        return type;
                }

                return UNKNOWN;
            }
        }
    }
}
