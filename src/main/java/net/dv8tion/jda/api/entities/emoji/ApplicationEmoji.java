package net.dv8tion.jda.api.entities.emoji;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.managers.ApplicationEmojiManager;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ApplicationEmoji extends CustomEmoji
{
    /**
     * The {@link net.dv8tion.jda.api.JDA JDA} instance of this emoji
     *
     * @return The JDA instance of this emoji
     */
    @Nonnull
    JDA getJDA();

    /**
     * The user who created this emoji
     *
     * @return The user who created this emoji
     */
    @Nullable
    User getOwner();

    RestAction<Void> delete();
    
    @Nonnull
    @CheckReturnValue
    ApplicationEmojiManager getManager();
}
