package net.dv8tion.jda.api.entities.channel;

import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.SoundboardSound;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

    @Nonnull
    public VoiceChannel getChannel()
    {
        return channel;
    }

    @Nonnull
    public User getUser()
    {
        return user;
    }

    @Nullable
    public EmojiUnion getEmoji()
    {
        return emoji;
    }

    @Nullable
    public Animation getAnimation()
    {
        return animation;
    }

    @Nullable
    public SoundboardSound getSoundboardSound()
    {
        return soundboardSound;
    }

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

        @Nonnull
        public Animation.Type getType()
        {
            return type;
        }

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

            public int getValue()
            {
                return value;
            }

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
