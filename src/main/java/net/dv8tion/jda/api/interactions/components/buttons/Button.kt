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
package net.dv8tion.jda.api.interactions.components.buttons

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.emoji.EmojiUnion
import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.internal.interactions.component.ButtonImpl
import net.dv8tion.jda.internal.utils.Checks
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * Represents a Message Button.
 * <br></br>These buttons are located below the message in [ActionRows][ActionRow].
 *
 *
 * Each button has either a `custom_id` or URL attached.
 * The id has to be provided by the user and can be used to identify the button in the [ButtonInteractionEvent].
 *
 *
 * **Example Usage**<br></br>
 * <pre>`public class HelloBot extends ListenerAdapter {
 * public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
 * if (event.getName().equals("hello")) {
 * event.reply("Click the button to say hello")
 * .addActionRow(
 * Button.primary("hello", "Click Me"), // Button with only a label
 * Button.success("emoji", Emoji.fromMarkdown("<:minn:245267426227388416>"))) // Button with only an emoji
 * .queue();
 * } else if (event.getName().equals("info")) {
 * event.reply("Click the buttons for more info")
 * .addActionRow( // link buttons don't send events, they just open a link in the browser when clicked
 * Button.link("https://github.com/discord-jda/JDA", "GitHub")
 * .withEmoji(Emoji.fromMarkdown("<:github:849286315580719104>")), // Link Button with label and emoji
 * Button.link("https://docs.jda.wiki/", "Javadocs")) // Link Button with only a label
 * .queue();
 * }
 * }
 *
 * public void onButtonInteraction(ButtonInteractionEvent event) {
 * if (event.getComponentId().equals("hello")) {
 * event.reply("Hello :)").queue();
 * }
 * }
 * }
`</pre> *
 *
 * To see what each button looks like here is an example cheatsheet:
 * <br></br>
 * <img alt="ButtonExample" src="https://raw.githubusercontent.com/discord-jda/JDA/52377f69d1f3bfba909c51a449ac6b258f606956/assets/wiki/interactions/ButtonExamples.png"></img>
 *
 * @see ReplyCallbackAction.addActionRow
 * @see ReplyCallbackAction.addComponents
 */
interface Button : ActionComponent {
    @get:Nonnull
    val label: String?

    @get:Nonnull
    val style: ButtonStyle

    /**
     * The target URL for this button, if it is a [LINK][ButtonStyle.LINK]-Style Button.
     *
     * @return The target URL or null
     */
    @JvmField
    val url: String?

    /**
     * The emoji attached to this button.
     * <br></br>This can be either [unicode][Emoji.Type.UNICODE] or [custom][Emoji.Type.CUSTOM].
     *
     *
     * You can use [.withEmoji] to create a button with an Emoji.
     *
     * @return [Emoji] for this button
     */
    val emoji: EmojiUnion?
    @Nonnull
    @CheckReturnValue
    override fun asDisabled(): Button {
        return withDisabled(true)
    }

    @Nonnull
    @CheckReturnValue
    override fun asEnabled(): Button {
        return withDisabled(false)
    }

    @Nonnull
    @CheckReturnValue
    override fun withDisabled(disabled: Boolean): Button {
        return ButtonImpl(id, label, style, url, disabled, emoji)
    }

    /**
     * Returns a copy of this button with the attached Emoji.
     *
     * @param  emoji
     * The emoji to use
     *
     * @return New button with emoji
     */
    @Nonnull
    @CheckReturnValue
    fun withEmoji(emoji: Emoji?): Button? {
        return ButtonImpl(id, label, style, url, isDisabled, emoji)
    }

    /**
     * Returns a copy of this button with the provided label.
     *
     * @param  label
     * The label to use
     *
     * @throws IllegalArgumentException
     *
     *  * If the provided `label` is null or empty.
     *  * If the character limit for `label`, defined by [.LABEL_MAX_LENGTH] as {@value #LABEL_MAX_LENGTH},
     * is exceeded.
     *
     *
     * @return New button with the changed label
     */
    @Nonnull
    @CheckReturnValue
    fun withLabel(@Nonnull label: String?): Button? {
        Checks.notEmpty(label, "Label")
        Checks.notLonger(label, LABEL_MAX_LENGTH, "Label")
        return ButtonImpl(id, label, style, url, isDisabled, emoji)
    }

    /**
     * Returns a copy of this button with the provided id.
     *
     * @param  id
     * The id to use
     *
     * @throws IllegalArgumentException
     *
     *  * If the provided `id` is null or empty.
     *  * If the character limit for `id`, defined by [.ID_MAX_LENGTH] as {@value #ID_MAX_LENGTH},
     * is exceeded.
     *
     *
     * @return New button with the changed id
     */
    @Nonnull
    @CheckReturnValue
    fun withId(@Nonnull id: String?): Button? {
        Checks.notEmpty(id, "ID")
        Checks.notLonger(id, ID_MAX_LENGTH, "ID")
        return ButtonImpl(id, label, style, null, isDisabled, emoji)
    }

    /**
     * Returns a copy of this button with the provided url.
     *
     * @param  url
     * The url to use
     *
     * @throws IllegalArgumentException
     *
     *  * If the provided `url` is null or empty.
     *  * If the character limit for `url`, defined by [.URL_MAX_LENGTH] as {@value #URL_MAX_LENGTH},
     * is exceeded.
     *
     *
     * @return New button with the changed url
     */
    @Nonnull
    @CheckReturnValue
    fun withUrl(@Nonnull url: String?): Button? {
        Checks.notEmpty(url, "URL")
        Checks.notLonger(url, URL_MAX_LENGTH, "URL")
        return ButtonImpl(null, label, ButtonStyle.LINK, url, isDisabled, emoji)
    }

    /**
     * Returns a copy of this button with the provided style.
     *
     *
     * You cannot use this convert link buttons.
     *
     * @param  style
     * The style to use
     *
     * @throws IllegalArgumentException
     *
     *  * If the provided `style` is null.
     *  * If the provided `style` tries to change whether this button is a [LINK][ButtonStyle.LINK] button.
     *
     *
     * @return New button with the changed style
     */
    @Nonnull
    @CheckReturnValue
    fun withStyle(@Nonnull style: ButtonStyle): Button? {
        Checks.notNull(style, "Style")
        Checks.check(style != ButtonStyle.UNKNOWN, "Cannot make button with unknown style!")
        require(!(this.style == ButtonStyle.LINK && style != ButtonStyle.LINK)) { "You cannot change a link button to another style!" }
        require(!(this.style != ButtonStyle.LINK && style == ButtonStyle.LINK)) { "You cannot change a styled button to a link button!" }
        return ButtonImpl(id, label, style, url, isDisabled, emoji)
    }

    companion object {
        /**
         * Creates a button with [PRIMARY][ButtonStyle.PRIMARY] Style.
         * <br></br>The button is enabled and has no emoji attached by default.
         * You can use [.asDisabled] and [.withEmoji] to further configure it.
         *
         * @param  id
         * The custom button ID
         * @param  label
         * The text to display on the button
         *
         * @throws IllegalArgumentException
         *
         *  * If any provided argument is null or empty.
         *  * If the character limit for `id`, defined by [.ID_MAX_LENGTH] as {@value #ID_MAX_LENGTH},
         * is exceeded.
         *  * If the character limit for `label`, defined by [.LABEL_MAX_LENGTH] as {@value #LABEL_MAX_LENGTH},
         * is exceeded.
         *
         *
         * @return The button instance
         */
        @Nonnull
        fun primary(@Nonnull id: String?, @Nonnull label: String?): Button? {
            Checks.notEmpty(id, "Id")
            Checks.notEmpty(label, "Label")
            Checks.notLonger(id, ID_MAX_LENGTH, "Id")
            Checks.notLonger(label, LABEL_MAX_LENGTH, "Label")
            return ButtonImpl(id, label, ButtonStyle.PRIMARY, false, null)
        }

        /**
         * Creates a button with [PRIMARY][ButtonStyle.PRIMARY] Style.
         * <br></br>The button is enabled and has no text label.
         * To use labels you can use `primary(id, label).withEmoji(emoji)`
         *
         *
         * To disable the button you can use [.asDisabled].
         *
         * @param  id
         * The custom button ID
         * @param  emoji
         * The emoji to use as the button label
         *
         * @throws IllegalArgumentException
         *
         *  * If any provided argument is null or empty.
         *  * If the character limit for `id`, defined by [.ID_MAX_LENGTH] as {@value #ID_MAX_LENGTH},
         * is exceeded.
         *
         *
         * @return The button instance
         */
        @Nonnull
        fun primary(@Nonnull id: String?, @Nonnull emoji: Emoji?): Button? {
            Checks.notEmpty(id, "Id")
            Checks.notNull(emoji, "Emoji")
            Checks.notLonger(id, ID_MAX_LENGTH, "Id")
            return ButtonImpl(id, "", ButtonStyle.PRIMARY, false, emoji)
        }

        /**
         * Creates a button with [SECONDARY][ButtonStyle.SECONDARY] Style.
         * <br></br>The button is enabled and has no emoji attached by default.
         * You can use [.asDisabled] and [.withEmoji] to further configure it.
         *
         * @param  id
         * The custom button ID
         * @param  label
         * The text to display on the button
         *
         * @throws IllegalArgumentException
         *
         *  * If any provided argument is null or empty.
         *  * If the character limit for `id`, defined by [.ID_MAX_LENGTH] as {@value #ID_MAX_LENGTH},
         * is exceeded.
         *  * If the character limit for `label`, defined by [.LABEL_MAX_LENGTH] as {@value #LABEL_MAX_LENGTH},
         * is exceeded.
         *
         *
         * @return The button instance
         */
        @JvmStatic
        @Nonnull
        fun secondary(@Nonnull id: String?, @Nonnull label: String?): Button? {
            Checks.notEmpty(id, "Id")
            Checks.notEmpty(label, "Label")
            Checks.notLonger(id, ID_MAX_LENGTH, "Id")
            Checks.notLonger(label, LABEL_MAX_LENGTH, "Label")
            return ButtonImpl(id, label, ButtonStyle.SECONDARY, false, null)
        }

        /**
         * Creates a button with [SECONDARY][ButtonStyle.SECONDARY] Style.
         * <br></br>The button is enabled and has no text label.
         * To use labels you can use `secondary(id, label).withEmoji(emoji)`
         *
         *
         * To disable the button you can use [.asDisabled].
         *
         * @param  id
         * The custom button ID
         * @param  emoji
         * The emoji to use as the button label
         *
         * @throws IllegalArgumentException
         *
         *  * If any provided argument is null or empty.
         *  * If the character limit for `id`, defined by [.ID_MAX_LENGTH] as {@value #ID_MAX_LENGTH},
         * is exceeded.
         *
         *
         * @return The button instance
         */
        @Nonnull
        fun secondary(@Nonnull id: String?, @Nonnull emoji: Emoji?): Button? {
            Checks.notEmpty(id, "Id")
            Checks.notNull(emoji, "Emoji")
            Checks.notLonger(id, ID_MAX_LENGTH, "Id")
            return ButtonImpl(id, "", ButtonStyle.SECONDARY, false, emoji)
        }

        /**
         * Creates a button with [SUCCESS][ButtonStyle.SUCCESS] Style.
         * <br></br>The button is enabled and has no emoji attached by default.
         * You can use [.asDisabled] and [.withEmoji] to further configure it.
         *
         * @param  id
         * The custom button ID
         * @param  label
         * The text to display on the button
         *
         * @throws IllegalArgumentException
         *
         *  * If any provided argument is null or empty.
         *  * If the character limit for `id`, defined by [.ID_MAX_LENGTH] as {@value #ID_MAX_LENGTH},
         * is exceeded.
         *  * If the character limit for `label`, defined by [.LABEL_MAX_LENGTH] as {@value #LABEL_MAX_LENGTH},
         * is exceeded.
         *
         *
         * @return The button instance
         */
        @Nonnull
        fun success(@Nonnull id: String?, @Nonnull label: String?): Button? {
            Checks.notEmpty(id, "Id")
            Checks.notEmpty(label, "Label")
            Checks.notLonger(id, ID_MAX_LENGTH, "Id")
            Checks.notLonger(label, LABEL_MAX_LENGTH, "Label")
            return ButtonImpl(id, label, ButtonStyle.SUCCESS, false, null)
        }

        /**
         * Creates a button with [SUCCESS][ButtonStyle.SUCCESS] Style.
         * <br></br>The button is enabled and has no text label.
         * To use labels you can use `success(id, label).withEmoji(emoji)`
         *
         *
         * To disable the button you can use [.asDisabled].
         *
         * @param  id
         * The custom button ID
         * @param  emoji
         * The emoji to use as the button label
         *
         * @throws IllegalArgumentException
         *
         *  * If any provided argument is null or empty.
         *  * If the character limit for `id`, defined by [.ID_MAX_LENGTH] as {@value #ID_MAX_LENGTH},
         * is exceeded.
         *
         *
         * @return The button instance
         */
        @Nonnull
        fun success(@Nonnull id: String?, @Nonnull emoji: Emoji?): Button? {
            Checks.notEmpty(id, "Id")
            Checks.notNull(emoji, "Emoji")
            Checks.notLonger(id, ID_MAX_LENGTH, "Id")
            return ButtonImpl(id, "", ButtonStyle.SUCCESS, false, emoji)
        }

        /**
         * Creates a button with [DANGER][ButtonStyle.DANGER] Style.
         * <br></br>The button is enabled and has no emoji attached by default.
         * You can use [.asDisabled] and [.withEmoji] to further configure it.
         *
         * @param  id
         * The custom button ID
         * @param  label
         * The text to display on the button
         *
         * @throws IllegalArgumentException
         *
         *  * If any provided argument is null or empty.
         *  * If the character limit for `id`, defined by [.ID_MAX_LENGTH] as {@value #ID_MAX_LENGTH},
         * is exceeded.
         *  * If the character limit for `label`, defined by [.LABEL_MAX_LENGTH] as {@value #LABEL_MAX_LENGTH},
         * is exceeded.
         *
         *
         * @return The button instance
         */
        @JvmStatic
        @Nonnull
        fun danger(@Nonnull id: String?, @Nonnull label: String?): Button? {
            Checks.notEmpty(id, "Id")
            Checks.notEmpty(label, "Label")
            Checks.notLonger(id, ID_MAX_LENGTH, "Id")
            Checks.notLonger(label, LABEL_MAX_LENGTH, "Label")
            return ButtonImpl(id, label, ButtonStyle.DANGER, false, null)
        }

        /**
         * Creates a button with [DANGER][ButtonStyle.DANGER] Style.
         * <br></br>The button is enabled and has no text label.
         * To use labels you can use `danger(id, label).withEmoji(emoji)`
         *
         *
         * To disable the button you can use [.asDisabled].
         *
         * @param  id
         * The custom button ID
         * @param  emoji
         * The emoji to use as the button label
         *
         * @throws IllegalArgumentException
         *
         *  * If any provided argument is null or empty.
         *  * If the character limit for `id`, defined by [.ID_MAX_LENGTH] as {@value #ID_MAX_LENGTH},
         * is exceeded.
         *
         *
         * @return The button instance
         */
        @Nonnull
        fun danger(@Nonnull id: String?, @Nonnull emoji: Emoji?): Button? {
            Checks.notEmpty(id, "Id")
            Checks.notNull(emoji, "Emoji")
            Checks.notLonger(id, ID_MAX_LENGTH, "Id")
            return ButtonImpl(id, "", ButtonStyle.DANGER, false, emoji)
        }

        /**
         * Creates a button with [LINK][ButtonStyle.LINK] Style.
         * <br></br>The button is enabled and has no emoji attached by default.
         * You can use [.asDisabled] and [.withEmoji] to further configure it.
         *
         *
         * Note that link buttons never send a [ButtonInteractionEvent].
         * These buttons only open a link for the user.
         *
         * @param  url
         * The target URL for this button
         * @param  label
         * The text to display on the button
         *
         * @throws IllegalArgumentException
         *
         *  * If any provided argument is null or empty.
         *  * If the character limit for `url`, defined by [.URL_MAX_LENGTH] as {@value #URL_MAX_LENGTH},
         * is exceeded.
         *  * If the character limit for `label`, defined by [.LABEL_MAX_LENGTH] as {@value #LABEL_MAX_LENGTH},
         * is exceeded.
         *
         *
         * @return The button instance
         */
        @Nonnull
        fun link(@Nonnull url: String?, @Nonnull label: String?): Button {
            Checks.notEmpty(url, "URL")
            Checks.notEmpty(label, "Label")
            Checks.notLonger(url, URL_MAX_LENGTH, "URL")
            Checks.notLonger(label, LABEL_MAX_LENGTH, "Label")
            return ButtonImpl(null, label, ButtonStyle.LINK, url, false, null)
        }

        /**
         * Creates a button with [LINK][ButtonStyle.LINK] Style.
         * <br></br>The button is enabled and has no text label.
         * To use labels you can use `link(url, label).withEmoji(emoji)`
         *
         *
         * To disable the button you can use [.asDisabled].
         *
         *
         * Note that link buttons never send a [ButtonInteractionEvent].
         * These buttons only open a link for the user.
         *
         * @param  url
         * The target URL for this button
         * @param  emoji
         * The emoji to use as the button label
         *
         * @throws IllegalArgumentException
         *
         *  * If any provided argument is null or empty.
         *  * If the character limit for `url`, defined by [.URL_MAX_LENGTH] as {@value #URL_MAX_LENGTH},
         * is exceeded.
         *
         *
         * @return The button instance
         */
        @Nonnull
        fun link(@Nonnull url: String?, @Nonnull emoji: Emoji?): Button? {
            Checks.notEmpty(url, "URL")
            Checks.notNull(emoji, "Emoji")
            Checks.notLonger(url, URL_MAX_LENGTH, "URL")
            return ButtonImpl(null, "", ButtonStyle.LINK, url, false, emoji)
        }

        /**
         * Create a button with the provided [style][ButtonStyle], URL or ID, and label.
         * <br></br>The button is enabled and has no emoji attached by default.
         * You can use [.asDisabled] and [.withEmoji] to further configure it.
         *
         *
         * See [.link] or [.primary] for more details.
         *
         * @param  style
         * The button style
         * @param  idOrUrl
         * Either the ID or URL for this button
         * @param  label
         * The text to display on the button
         *
         * @throws IllegalArgumentException
         *
         *  * If any provided argument is null or empty.
         *  * If the id is longer than {@value #ID_MAX_LENGTH}, as defined by [.ID_MAX_LENGTH].
         *  * If the url is longer than {@value #URL_MAX_LENGTH}, as defined by [.URL_MAX_LENGTH].
         *  * If the character limit for `label`, defined by [.LABEL_MAX_LENGTH] as {@value #LABEL_MAX_LENGTH},
         * is exceeded.
         *
         *
         * @return The button instance
         */
        @Nonnull
        fun of(@Nonnull style: ButtonStyle, @Nonnull idOrUrl: String?, @Nonnull label: String?): Button {
            Checks.check(style != ButtonStyle.UNKNOWN, "Cannot make button with unknown style!")
            Checks.notNull(style, "Style")
            Checks.notNull(label, "Label")
            Checks.notLonger(label, LABEL_MAX_LENGTH, "Label")
            if (style == ButtonStyle.LINK) return link(idOrUrl, label)
            Checks.notEmpty(idOrUrl, "Id")
            Checks.notLonger(idOrUrl, ID_MAX_LENGTH, "Id")
            return ButtonImpl(idOrUrl, label, style, false, null)
        }

        /**
         * Create a button with the provided [style][ButtonStyle], URL or ID, and [Emoji].
         * <br></br>The button is enabled and has no text label.
         * To use labels you can use `of(style, idOrUrl, label).withEmoji(emoji)`
         *
         *
         * See [.link] or [.primary] for more details.
         *
         * @param  style
         * The button style
         * @param  idOrUrl
         * Either the ID or URL for this button
         * @param  emoji
         * The emoji to use as the button label
         *
         * @throws IllegalArgumentException
         *
         *  * If any provided argument is null or empty.
         *  * If the id is longer than {@value #ID_MAX_LENGTH}, as defined by [.ID_MAX_LENGTH].
         *  * If the url is longer than {@value #URL_MAX_LENGTH}, as defined by [.URL_MAX_LENGTH].
         *
         *
         * @return The button instance
         */
        @Nonnull
        fun of(@Nonnull style: ButtonStyle, @Nonnull idOrUrl: String?, @Nonnull emoji: Emoji?): Button? {
            Checks.check(style != ButtonStyle.UNKNOWN, "Cannot make button with unknown style!")
            Checks.notNull(style, "Style")
            Checks.notNull(emoji, "Emoji")
            if (style == ButtonStyle.LINK) return link(idOrUrl, emoji)
            Checks.notEmpty(idOrUrl, "Id")
            Checks.notLonger(idOrUrl, ID_MAX_LENGTH, "Id")
            return ButtonImpl(idOrUrl, "", style, false, emoji)
        }

        /**
         * Create an enabled button with the provided [style][ButtonStyle], URL or ID, label and [Emoji].
         *
         *
         * You can use [.asDisabled] to disable it.
         *
         *
         * See [.link] or [.primary] for more details.
         *
         * @param  style
         * The button style
         * @param  idOrUrl
         * Either the ID or URL for this button
         * @param  label
         * The text to display on the button
         * @param  emoji
         * The emoji to use as the button label
         *
         * @throws IllegalArgumentException
         * If any of the following scenarios occurs:
         *
         *  * The style is null
         *  * You provide a URL that is null, empty or longer than {@value #URL_MAX_LENGTH} characters, as defined by [.URL_MAX_LENGTH]
         * or you provide an ID that is null, empty or longer than {@value #ID_MAX_LENGTH} characters, as defined by [.ID_MAX_LENGTH].
         *  * The `label` is non-null and longer than {@value #LABEL_MAX_LENGTH} characters, as defined by [.LABEL_MAX_LENGTH].
         *  * The `label` is null/empty, and the `emoji` is also null.
         *
         *
         * @return The button instance
         */
        @Nonnull
        fun of(@Nonnull style: ButtonStyle, @Nonnull idOrUrl: String?, label: String?, emoji: Emoji?): Button? {
            if (label != null) return of(
                style,
                idOrUrl,
                label
            ).withEmoji(emoji) else if (emoji != null) return of(style, idOrUrl, emoji)
            throw IllegalArgumentException("Cannot build a button without a label and emoji. At least one has to be provided as non-null.")
        }

        /**
         * The maximum length a button label can have
         */
        const val LABEL_MAX_LENGTH = 80

        /**
         * The maximum length a button id can have
         */
        const val ID_MAX_LENGTH = 100

        /**
         * The maximum length a button url can have
         */
        const val URL_MAX_LENGTH = 512
    }
}
