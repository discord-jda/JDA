package net.dv8tion.jda.api.managers;

import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Manager providing functionality to update the name field for an {@link ApplicationEmoji}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setName("minn")
 *        .queue();
 * }</pre>
 *
 * @see ApplicationEmoji#getManager()
 */
public interface ApplicationEmojiManager extends Manager<ApplicationEmojiManager>
{
    /** Used to reset the name field */
    long NAME = 1;

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return ApplicationEmojiManager for chaining convenience.
     */
    @Nonnull
    @Override
    ApplicationEmojiManager reset(long fields);

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #NAME}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return ApplicationEmojiManager for chaining convenience.
     */
    @Nonnull
    @Override
    ApplicationEmojiManager reset(long... fields);

    /**
     * The target {@link ApplicationEmoji} that will be modified by this Manager
     *
     * @return The target emoji
     */
    @Nonnull
    ApplicationEmoji getEmoji();

    /**
     * Sets the <b><u>name</u></b> of the selected {@link ApplicationEmoji}.
     *
     * <p>An emoji name <b>must</b> be between 2-32 characters long!
     * <br>Emoji names may only be populated with alphanumeric (with underscore and dash).
     *
     * <p><b>Example</b>: {@code tatDab} or {@code fmgSUP}
     *
     * @param  name
     *         The new name for the selected {@link ApplicationEmoji}
     *
     * @return ApplicationEmojiManager for chaining convenience.
     */
    @Nonnull
    @CheckReturnValue
    ApplicationEmojiManager setName(@Nonnull String name);
}
