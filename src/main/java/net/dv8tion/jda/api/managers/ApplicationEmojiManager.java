package net.dv8tion.jda.api.managers;

import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface ApplicationEmojiManager extends Manager<ApplicationEmojiManager>
{
    long NAME = 1;

    @Nonnull
    @Override
    ApplicationEmojiManager reset(long fields);

    @Nonnull
    @Override
    ApplicationEmojiManager reset(long... fields);
    
    @Nonnull
    ApplicationEmoji getEmoji();
    
    @Nonnull
    @CheckReturnValue
    ApplicationEmojiManager setName(@Nonnull String name);
}
