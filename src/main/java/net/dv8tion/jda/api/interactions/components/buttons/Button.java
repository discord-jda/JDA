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

package net.dv8tion.jda.api.interactions.components.buttons;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.internal.interactions.component.ButtonImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a Message Button.
 * <br>These buttons are located below the message in {@link ActionRow ActionRows}.
 *
 * <p>Each button has either a {@code custom_id} or URL attached.
 * The id has to be provided by the user and can be used to identify the button in the {@link ButtonInteractionEvent ButtonInteractionEvent}.
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * public class HelloBot extends ListenerAdapter {
 *   public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
 *       if (event.getName().equals("hello")) {
 *           event.reply("Click the button to say hello")
 *               .addActionRow(
 *                 Button.primary("hello", "Click Me"), // Button with only a label
 *                 Button.success("emoji", Emoji.fromMarkdown("<:minn:245267426227388416>"))) // Button with only an emoji
 *               .queue();
 *       } else if (event.getName().equals("info")) {
 *           event.reply("Click the buttons for more info")
 *               .addActionRow( // link buttons don't send events, they just open a link in the browser when clicked
 *                   Button.link("https://github.com/DV8FromTheWorld/JDA", "GitHub")
 *                     .withEmoji(Emoji.fromMarkdown("<:github:849286315580719104>")), // Link Button with label and emoji
 *                   Button.link("https://ci.dv8tion.net/job/JDA/javadoc/", "Javadocs")) // Link Button with only a label
 *               .queue();
 *       }
 *   }
 *
 *   public void onButtonInteraction(ButtonInteractionEvent event) {
 *       if (event.getComponentId().equals("hello")) {
 *           event.reply("Hello :)").queue();
 *       }
 *   }
 * }
 * }</pre>
 *
 * To see what each button looks like here is an example cheatsheet:
 * <br>
 * <img alt="ButtonExample" src="https://raw.githubusercontent.com/DV8FromTheWorld/JDA/52377f69d1f3bfba909c51a449ac6b258f606956/assets/wiki/interactions/ButtonExamples.png">
 *
 * @see ReplyCallbackAction#addActionRow(ItemComponent...)
 * @see ReplyCallbackAction#addActionRows(ActionRow...)
 */
public interface Button extends ActionComponent
{
    /**
     * The maximum length a button label can have
     */
    int LABEL_MAX_LENGTH = 80;

    /**
     * The maximum length a button id can have
     */
    int ID_MAX_LENGTH = 100;

    /**
     * The maximum length a button url can have
     */
    int URL_MAX_LENGTH = 512;

    /**
     * The visible text on the button.
     *
     * @return The button label
     */
    @Nonnull
    String getLabel();

    /**
     * The style of this button.
     *
     * @return {@link ButtonStyle}
     */
    @Nonnull
    ButtonStyle getStyle();

    /**
     * The target URL for this button, if it is a {@link ButtonStyle#LINK LINK}-Style Button.
     *
     * @return The target URL or null
     */
    @Nullable
    String getUrl();

    /**
     * The emoji attached to this button.
     * <br>This can be either {@link Emoji#isUnicode() unicode} or {@link Emoji#isCustom()} custom.
     *
     * <p>You can use {@link #withEmoji(Emoji)} to create a button with an Emoji.
     *
     * @return {@link Emoji} for this button
     */
    @Nullable
    Emoji getEmoji();

    @Nonnull
    @CheckReturnValue
    default Button asDisabled()
    {
        return withDisabled(true);
    }

    @Nonnull
    @CheckReturnValue
    default Button asEnabled()
    {
        return withDisabled(false);
    }

    @Nonnull
    @CheckReturnValue
    default Button withDisabled(boolean disabled)
    {
        return new ButtonImpl(getId(), getLabel(), getStyle(), getUrl(), disabled, getEmoji());
    }

    /**
     * Returns a copy of this button with the attached Emoji.
     *
     * @param  emoji
     *         The emoji to use
     *
     * @return New button with emoji
     */
    @Nonnull
    @CheckReturnValue
    default Button withEmoji(@Nullable Emoji emoji)
    {
        return new ButtonImpl(getId(), getLabel(), getStyle(), getUrl(), isDisabled(), emoji);
    }

    /**
     * Returns a copy of this button with the provided label.
     *
     * @param  label
     *         The label to use
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided {@code label} is null or empty.</li>
     *             <li>If the character limit for {@code label}, defined by {@link #LABEL_MAX_LENGTH} as {@value #LABEL_MAX_LENGTH},
     *             is exceeded.</li>
     *         </ul>
     *
     * @return New button with the changed label
     */
    @Nonnull
    @CheckReturnValue
    default Button withLabel(@Nonnull String label)
    {
        Checks.notEmpty(label, "Label");
        Checks.notLonger(label, LABEL_MAX_LENGTH, "Label");
        return new ButtonImpl(getId(), label, getStyle(), getUrl(), isDisabled(), getEmoji());
    }

    /**
     * Returns a copy of this button with the provided id.
     *
     * @param  id
     *         The id to use
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided {@code id} is null or empty.</li>
     *             <li>If the character limit for {@code id}, defined by {@link #ID_MAX_LENGTH} as {@value #ID_MAX_LENGTH},
     *             is exceeded.</li>
     *         </ul>
     *
     * @return New button with the changed id
     */
    @Nonnull
    @CheckReturnValue
    default Button withId(@Nonnull String id)
    {
        Checks.notEmpty(id, "ID");
        Checks.notLonger(id, ID_MAX_LENGTH, "ID");
        return new ButtonImpl(id, getLabel(), getStyle(), null, isDisabled(), getEmoji());
    }

    /**
     * Returns a copy of this button with the provided url.
     *
     * @param  url
     *         The url to use
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided {@code url} is null or empty.</li>
     *             <li>If the character limit for {@code url}, defined by {@link #URL_MAX_LENGTH} as {@value #URL_MAX_LENGTH},
     *             is exceeded.</li>
     *         </ul>
     *
     * @return New button with the changed url
     */
    @Nonnull
    @CheckReturnValue
    default Button withUrl(@Nonnull String url)
    {
        Checks.notEmpty(url, "URL");
        Checks.notLonger(url, URL_MAX_LENGTH, "URL");
        return new ButtonImpl(null, getLabel(), ButtonStyle.LINK, url, isDisabled(), getEmoji());
    }

    /**
     * Returns a copy of this button with the provided style.
     *
     * <p>You cannot use this convert link buttons.
     *
     * @param  style
     *         The style to use
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the provided {@code style} is null.</li>
     *             <li>If the provided {@code style} tries to change whether this button is a {@link ButtonStyle#LINK LINK} button.</li>
     *         </ul>
     *
     * @return New button with the changed style
     */
    @Nonnull
    @CheckReturnValue
    default Button withStyle(@Nonnull ButtonStyle style)
    {
        Checks.notNull(style, "Style");
        Checks.check(style != ButtonStyle.UNKNOWN, "Cannot make button with unknown style!");
        if (getStyle() == ButtonStyle.LINK && style != ButtonStyle.LINK)
            throw new IllegalArgumentException("You cannot change a link button to another style!");
        if (getStyle() != ButtonStyle.LINK && style == ButtonStyle.LINK)
            throw new IllegalArgumentException("You cannot change a styled button to a link button!");
        return new ButtonImpl(getId(), getLabel(), style, getUrl(), isDisabled(), getEmoji());
    }

    /**
     * Creates a button with {@link ButtonStyle#PRIMARY PRIMARY} Style.
     * <br>The button is enabled and has no emoji attached by default.
     * You can use {@link #asDisabled()} and {@link #withEmoji(Emoji)} to further configure it.
     *
     * @param  id
     *         The custom button ID
     * @param  label
     *         The text to display on the button
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any provided argument is null or empty.</li>
     *             <li>If the character limit for {@code id}, defined by {@link #ID_MAX_LENGTH} as {@value #ID_MAX_LENGTH},
     *             is exceeded.</li>
     *             <li>If the character limit for {@code label}, defined by {@link #LABEL_MAX_LENGTH} as {@value #LABEL_MAX_LENGTH},
     *             is exceeded.</li>
     *         </ul>
     *
     * @return The button instance
     */
    @Nonnull
    static Button primary(@Nonnull String id, @Nonnull String label)
    {
        Checks.notEmpty(id, "Id");
        Checks.notEmpty(label, "Label");
        Checks.notLonger(id, ID_MAX_LENGTH, "Id");
        Checks.notLonger(label, LABEL_MAX_LENGTH, "Label");
        return new ButtonImpl(id, label, ButtonStyle.PRIMARY, false, null);
    }

    /**
     * Creates a button with {@link ButtonStyle#PRIMARY PRIMARY} Style.
     * <br>The button is enabled and has no text label.
     * To use labels you can use {@code primary(id, label).withEmoji(emoji)}
     *
     * <p>To disable the button you can use {@link #asDisabled()}.
     *
     * @param  id
     *         The custom button ID
     * @param  emoji
     *         The emoji to use as the button label
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any provided argument is null or empty.</li>
     *             <li>If the character limit for {@code id}, defined by {@link #ID_MAX_LENGTH} as {@value #ID_MAX_LENGTH},
     *             is exceeded.</li>
     *         </ul>
     *
     * @return The button instance
     */
    @Nonnull
    static Button primary(@Nonnull String id, @Nonnull Emoji emoji)
    {
        Checks.notEmpty(id, "Id");
        Checks.notNull(emoji, "Emoji");
        Checks.notLonger(id, ID_MAX_LENGTH, "Id");
        return new ButtonImpl(id, "", ButtonStyle.PRIMARY, false, emoji);
    }

    /**
     * Creates a button with {@link ButtonStyle#SECONDARY SECONDARY} Style.
     * <br>The button is enabled and has no emoji attached by default.
     * You can use {@link #asDisabled()} and {@link #withEmoji(Emoji)} to further configure it.
     *
     * @param  id
     *         The custom button ID
     * @param  label
     *         The text to display on the button
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any provided argument is null or empty.</li>
     *             <li>If the character limit for {@code id}, defined by {@link #ID_MAX_LENGTH} as {@value #ID_MAX_LENGTH},
     *             is exceeded.</li>
     *             <li>If the character limit for {@code label}, defined by {@link #LABEL_MAX_LENGTH} as {@value #LABEL_MAX_LENGTH},
     *             is exceeded.</li>
     *         </ul>
     *
     * @return The button instance
     */
    @Nonnull
    static Button secondary(@Nonnull String id, @Nonnull String label)
    {
        Checks.notEmpty(id, "Id");
        Checks.notEmpty(label, "Label");
        Checks.notLonger(id, ID_MAX_LENGTH, "Id");
        Checks.notLonger(label, LABEL_MAX_LENGTH, "Label");
        return new ButtonImpl(id, label, ButtonStyle.SECONDARY, false, null);
    }

    /**
     * Creates a button with {@link ButtonStyle#SECONDARY SECONDARY} Style.
     * <br>The button is enabled and has no text label.
     * To use labels you can use {@code secondary(id, label).withEmoji(emoji)}
     *
     * <p>To disable the button you can use {@link #asDisabled()}.
     *
     * @param  id
     *         The custom button ID
     * @param  emoji
     *         The emoji to use as the button label
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any provided argument is null or empty.</li>
     *             <li>If the character limit for {@code id}, defined by {@link #ID_MAX_LENGTH} as {@value #ID_MAX_LENGTH},
     *             is exceeded.</li>
     *         </ul>
     *
     * @return The button instance
     */
    @Nonnull
    static Button secondary(@Nonnull String id, @Nonnull Emoji emoji)
    {
        Checks.notEmpty(id, "Id");
        Checks.notNull(emoji, "Emoji");
        Checks.notLonger(id, ID_MAX_LENGTH, "Id");
        return new ButtonImpl(id, "", ButtonStyle.SECONDARY, false, emoji);
    }

    /**
     * Creates a button with {@link ButtonStyle#SUCCESS SUCCESS} Style.
     * <br>The button is enabled and has no emoji attached by default.
     * You can use {@link #asDisabled()} and {@link #withEmoji(Emoji)} to further configure it.
     *
     * @param  id
     *         The custom button ID
     * @param  label
     *         The text to display on the button
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any provided argument is null or empty.</li>
     *             <li>If the character limit for {@code id}, defined by {@link #ID_MAX_LENGTH} as {@value #ID_MAX_LENGTH},
     *             is exceeded.</li>
     *             <li>If the character limit for {@code label}, defined by {@link #LABEL_MAX_LENGTH} as {@value #LABEL_MAX_LENGTH},
     *             is exceeded.</li>
     *         </ul>
     *
     * @return The button instance
     */
    @Nonnull
    static Button success(@Nonnull String id, @Nonnull String label)
    {
        Checks.notEmpty(id, "Id");
        Checks.notEmpty(label, "Label");
        Checks.notLonger(id, ID_MAX_LENGTH, "Id");
        Checks.notLonger(label, LABEL_MAX_LENGTH, "Label");
        return new ButtonImpl(id, label, ButtonStyle.SUCCESS, false, null);
    }

    /**
     * Creates a button with {@link ButtonStyle#SUCCESS SUCCESS} Style.
     * <br>The button is enabled and has no text label.
     * To use labels you can use {@code success(id, label).withEmoji(emoji)}
     *
     * <p>To disable the button you can use {@link #asDisabled()}.
     *
     * @param  id
     *         The custom button ID
     * @param  emoji
     *         The emoji to use as the button label
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any provided argument is null or empty.</li>
     *             <li>If the character limit for {@code id}, defined by {@link #ID_MAX_LENGTH} as {@value #ID_MAX_LENGTH},
     *             is exceeded.</li>
     *         </ul>
     *
     * @return The button instance
     */
    @Nonnull
    static Button success(@Nonnull String id, @Nonnull Emoji emoji)
    {
        Checks.notEmpty(id, "Id");
        Checks.notNull(emoji, "Emoji");
        Checks.notLonger(id, ID_MAX_LENGTH, "Id");
        return new ButtonImpl(id, "", ButtonStyle.SUCCESS, false, emoji);
    }

    /**
     * Creates a button with {@link ButtonStyle#DANGER DANGER} Style.
     * <br>The button is enabled and has no emoji attached by default.
     * You can use {@link #asDisabled()} and {@link #withEmoji(Emoji)} to further configure it.
     *
     * @param  id
     *         The custom button ID
     * @param  label
     *         The text to display on the button
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any provided argument is null or empty.</li>
     *             <li>If the character limit for {@code id}, defined by {@link #ID_MAX_LENGTH} as {@value #ID_MAX_LENGTH},
     *             is exceeded.</li>
     *             <li>If the character limit for {@code label}, defined by {@link #LABEL_MAX_LENGTH} as {@value #LABEL_MAX_LENGTH},
     *             is exceeded.</li>
     *         </ul>
     *
     * @return The button instance
     */
    @Nonnull
    static Button danger(@Nonnull String id, @Nonnull String label)
    {
        Checks.notEmpty(id, "Id");
        Checks.notEmpty(label, "Label");
        Checks.notLonger(id, ID_MAX_LENGTH, "Id");
        Checks.notLonger(label, LABEL_MAX_LENGTH, "Label");
        return new ButtonImpl(id, label, ButtonStyle.DANGER, false, null);
    }

    /**
     * Creates a button with {@link ButtonStyle#DANGER DANGER} Style.
     * <br>The button is enabled and has no text label.
     * To use labels you can use {@code danger(id, label).withEmoji(emoji)}
     *
     * <p>To disable the button you can use {@link #asDisabled()}.
     *
     * @param  id
     *         The custom button ID
     * @param  emoji
     *         The emoji to use as the button label
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any provided argument is null or empty.</li>
     *             <li>If the character limit for {@code id}, defined by {@link #ID_MAX_LENGTH} as {@value #ID_MAX_LENGTH},
     *             is exceeded.</li>
     *         </ul>
     *
     * @return The button instance
     */
    @Nonnull
    static Button danger(@Nonnull String id, @Nonnull Emoji emoji)
    {
        Checks.notEmpty(id, "Id");
        Checks.notNull(emoji, "Emoji");
        Checks.notLonger(id, ID_MAX_LENGTH, "Id");
        return new ButtonImpl(id, "", ButtonStyle.DANGER, false, emoji);
    }

    /**
     * Creates a button with {@link ButtonStyle#LINK LINK} Style.
     * <br>The button is enabled and has no emoji attached by default.
     * You can use {@link #asDisabled()} and {@link #withEmoji(Emoji)} to further configure it.
     *
     * <p>Note that link buttons never send a {@link ButtonInteractionEvent ButtonInteractionEvent}.
     * These buttons only open a link for the user.
     *
     * @param  url
     *         The target URL for this button
     * @param  label
     *         The text to display on the button
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any provided argument is null or empty.</li>
     *             <li>If the character limit for {@code url}, defined by {@link #URL_MAX_LENGTH} as {@value #URL_MAX_LENGTH},
     *             is exceeded.</li>
     *             <li>If the character limit for {@code label}, defined by {@link #LABEL_MAX_LENGTH} as {@value #LABEL_MAX_LENGTH},
     *             is exceeded.</li>
     *         </ul>
     *
     * @return The button instance
     */
    @Nonnull
    static Button link(@Nonnull String url, @Nonnull String label)
    {
        Checks.notEmpty(url, "URL");
        Checks.notEmpty(label, "Label");
        Checks.notLonger(url, URL_MAX_LENGTH, "URL");
        Checks.notLonger(label, LABEL_MAX_LENGTH, "Label");
        return new ButtonImpl(null, label, ButtonStyle.LINK, url, false, null);
    }

    /**
     * Creates a button with {@link ButtonStyle#LINK LINK} Style.
     * <br>The button is enabled and has no text label.
     * To use labels you can use {@code link(url, label).withEmoji(emoji)}
     *
     * <p>To disable the button you can use {@link #asDisabled()}.
     *
     * <p>Note that link buttons never send a {@link ButtonInteractionEvent ButtonInteractionEvent}.
     * These buttons only open a link for the user.
     *
     * @param  url
     *         The target URL for this button
     * @param  emoji
     *         The emoji to use as the button label
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any provided argument is null or empty.</li>
     *             <li>If the character limit for {@code url}, defined by {@link #URL_MAX_LENGTH} as {@value #URL_MAX_LENGTH},
     *             is exceeded.</li>
     *         </ul>
     *
     * @return The button instance
     */
    @Nonnull
    static Button link(@Nonnull String url, @Nonnull Emoji emoji)
    {
        Checks.notEmpty(url, "URL");
        Checks.notNull(emoji, "Emoji");
        Checks.notLonger(url, URL_MAX_LENGTH, "URL");
        return new ButtonImpl(null, "", ButtonStyle.LINK, url, false, emoji);
    }

    /**
     * Create a button with the provided {@link ButtonStyle style}, URL or ID, and label.
     * <br>The button is enabled and has no emoji attached by default.
     * You can use {@link #asDisabled()} and {@link #withEmoji(Emoji)} to further configure it.
     *
     * <p>See {@link #link(String, String)} or {@link #primary(String, String)} for more details.
     *
     * @param  style
     *         The button style
     * @param  idOrUrl
     *         Either the ID or URL for this button
     * @param  label
     *         The text to display on the button
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any provided argument is null or empty.</li>
     *             <li>If the id is longer than {@value #ID_MAX_LENGTH}, as defined by {@link #ID_MAX_LENGTH}.</li>
     *             <li>If the url is longer than {@value #URL_MAX_LENGTH}, as defined by {@link #URL_MAX_LENGTH}.</li>
     *             <li>If the character limit for {@code label}, defined by {@link #LABEL_MAX_LENGTH} as {@value #LABEL_MAX_LENGTH},
     *             is exceeded.</li>
     *         </ul>
     *
     * @return The button instance
     */
    @Nonnull
    static Button of(@Nonnull ButtonStyle style, @Nonnull String idOrUrl, @Nonnull String label)
    {
        Checks.check(style != ButtonStyle.UNKNOWN, "Cannot make button with unknown style!");
        Checks.notNull(style, "Style");
        Checks.notNull(label, "Label");
        Checks.notLonger(label, LABEL_MAX_LENGTH, "Label");
        if (style == ButtonStyle.LINK)
            return link(idOrUrl, label);
        Checks.notEmpty(idOrUrl, "Id");
        Checks.notLonger(idOrUrl, ID_MAX_LENGTH, "Id");
        return new ButtonImpl(idOrUrl, label, style, false, null);
    }

    /**
     * Create a button with the provided {@link ButtonStyle style}, URL or ID, and {@link Emoji emoji}.
     * <br>The button is enabled and has no text label.
     * To use labels you can use {@code of(style, idOrUrl, label).withEmoji(emoji)}
     *
     * <p>See {@link #link(String, Emoji)} or {@link #primary(String, Emoji)} for more details.
     *
     * @param  style
     *         The button style
     * @param  idOrUrl
     *         Either the ID or URL for this button
     * @param  emoji
     *         The emoji to use as the button label
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any provided argument is null or empty.</li>
     *             <li>If the id is longer than {@value #ID_MAX_LENGTH}, as defined by {@link #ID_MAX_LENGTH}.</li>
     *             <li>If the url is longer than {@value #URL_MAX_LENGTH}, as defined by {@link #URL_MAX_LENGTH}.</li>
     *         </ul>
     *
     * @return The button instance
     */
    @Nonnull
    static Button of(@Nonnull ButtonStyle style, @Nonnull String idOrUrl, @Nonnull Emoji emoji)
    {
        Checks.check(style != ButtonStyle.UNKNOWN, "Cannot make button with unknown style!");
        Checks.notNull(style, "Style");
        Checks.notNull(emoji, "Emoji");
        if (style == ButtonStyle.LINK)
            return link(idOrUrl, emoji);
        Checks.notEmpty(idOrUrl, "Id");
        Checks.notLonger(idOrUrl, ID_MAX_LENGTH, "Id");
        return new ButtonImpl(idOrUrl, "", style, false, emoji);
    }

    /**
     * Create an enabled button with the provided {@link ButtonStyle style}, URL or ID, label and {@link Emoji emoji}.
     *
     * <p>You can use {@link #asDisabled()} to disable it.
     *
     * <p>See {@link #link(String, String)} or {@link #primary(String, String)} for more details.
     *
     * @param  style
     *         The button style
     * @param  idOrUrl
     *         Either the ID or URL for this button
     * @param  label
     *         The text to display on the button
     * @param  emoji
     *         The emoji to use as the button label
     *
     * @throws IllegalArgumentException
     *         If any of the following scenarios occurs:
     *         <ul>
     *             <li>The style is null</li>
     *             <li>You provide a URL that is null, empty or longer than {@value #URL_MAX_LENGTH} characters, as defined by {@link #URL_MAX_LENGTH}
     *             or you provide an ID that is null, empty or longer than {@value #ID_MAX_LENGTH} characters, as defined by {@link #ID_MAX_LENGTH}.</li>
     *             <li>The {@code label} is non-null and longer than {@value #LABEL_MAX_LENGTH} characters, as defined by {@link #LABEL_MAX_LENGTH}.</li>
     *             <li>The {@code label} is null/empty, and the {@code emoji} is also null.</li>
     *         </ul>
     *
     * @return The button instance
     */
    @Nonnull
    static Button of(@Nonnull ButtonStyle style, @Nonnull String idOrUrl, @Nullable String label, @Nullable Emoji emoji)
    {
        if (label != null)
            return of(style, idOrUrl, label).withEmoji(emoji);
        else if (emoji != null)
            return of(style, idOrUrl, emoji);
        throw new IllegalArgumentException("Cannot build a button without a label and emoji. At least one has to be provided as non-null.");
    }
}
