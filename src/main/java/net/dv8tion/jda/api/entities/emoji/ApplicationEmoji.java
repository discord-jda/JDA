package net.dv8tion.jda.api.entities.emoji;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.managers.ApplicationEmojiManager;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a Custom Emoji hosted on the Bot Account.
 *
 * <p><b>This does not represent unicode emojis like they are used in the official client!
 * The format {@code :smiley:} is a client-side alias which is replaced by the unicode emoji, not a custom emoji.</b>
 *
 * @see JDA#retrieveApplicationEmojiById(long)
 * @see JDA#retrieveApplicationEmojis()
 */
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

    /**
     * Deletes this emoji.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>If this emoji was already removed</li>
     * </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction}
     *         The RestAction to delete this emoji.
     */
    RestAction<Void> delete();

    /**
     * The {@link ApplicationEmojiManager Manager} for this emoji, used to modify
     * properties of the emoji like name.
     *
     * @return The ApplicationEmojiManager for this emoji
     */
    @Nonnull
    @CheckReturnValue
    ApplicationEmojiManager getManager();
}
