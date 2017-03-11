/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.core

import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.entities.impl.MessageEmbedImpl
import org.apache.http.util.Args
import java.awt.Color
import java.time.*
import java.time.temporal.TemporalAccessor
import java.util.*
import java.util.function.Consumer
import java.util.regex.Pattern

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
/* **
 * Builder system used to build [MessageEmbeds][net.dv8tion.jda.core.entities.MessageEmbed].
 * <br></br>A visual breakdown of an Embed and how it relates to this class is available at
 * [http://imgur.com/a/yOb5n](http://imgur.com/a/yOb5n).
 *
 * @since  JDA 3.0
 *
 * @author John A. Grosh
 */
abstract class AbstractEmbedBuilder<T : AbstractEmbedBuilder<T>> {
    private var url: String? = null
    private var title: String? = null
    /* **
     * The [StringBuilder][java.lang.StringBuilder] used to
     * build the description for the embed.
     * <br></br>Note: To reset the description use [setDescription(null)][.description]

     * @return StringBuilder with current description context
     */
    var descriptionBuilder: StringBuilder = StringBuilder()
        private set
    private var timestamp: OffsetDateTime? = null
    private var color: Color? = null
    private var thumbnail: MessageEmbed.Thumbnail? = null
    private var author: MessageEmbed.AuthorInfo? = null
    private var footer: MessageEmbed.Footer? = null
    private var image: MessageEmbed.ImageInfo? = null
    private val fields: MutableList<MessageEmbed.Field>

    init {
        fields = LinkedList<MessageEmbed.Field>()
    }

    /* **
     * Returns a [MessageEmbed][net.dv8tion.jda.core.entities.MessageEmbed]
     * that has been checked as being valid for sending.

     * @throws java.lang.IllegalStateException
     *         If the embed is empty. Can be checked with [.isEmpty].
     *
     *
     * @return the built, sendable [net.dv8tion.jda.core.entities.MessageEmbed]
     */
    fun build(): MessageEmbed {
        if (isEmpty)
            throw IllegalStateException("Cannot build an empty embed!")
        if (descriptionBuilder.length > TEXT_MAX_LENGTH)
            throw IllegalStateException(String.format("Description is longer than %d! Please limit your input!", TEXT_MAX_LENGTH))
        val description = if (this.descriptionBuilder.isEmpty()) null else this.descriptionBuilder.toString()

        return MessageEmbedImpl().setTitle(title)
                .setUrl(url)
                .setDescription(description)
                .setTimestamp(timestamp)
                .setColor(color)
                .setThumbnail(thumbnail)
                .setAuthor(author)
                .setFooter(footer)
                .setImage(image)
                .setFields(fields)
    }

    /* **
     * Checks if the given embed is empty. Empty embeds will throw an exception if built

     * @return true if the embed is empty and cannot be built
     */
    val isEmpty: Boolean
        get() = title == null
                //&& descriptionBuilder == null
                && timestamp == null
                && color == null
                && thumbnail == null
                && author == null
                && footer == null
                && image == null
                && fields.isEmpty()

    /* **
     * Sets the Title of the embed.

     *
     * **[Example](http://i.imgur.com/JgZtxIM.png)**

     * @param  title
     *         the title of the embed
     *
     * @param  url
     *         Makes the title into a hyperlink pointed at this url.
     *
     *
     * @throws java.lang.IllegalArgumentException
     *
     *              * If the provided `title` is an empty String.
     *              * If the length of `title` is greater than [net.dv8tion.jda.core.EmbedBuilder.TITLE_MAX_LENGTH].
     *              * If the length of `url` is longer than [net.dv8tion.jda.core.EmbedBuilder.URL_MAX_LENGTH].
     *              * If the provided `url` is not a properly formatted http or https url.
     *
     *
     *
     * @return the builder after the title has been set
     */
    @JvmOverloads
    fun setTitle(title: String?, url: String? = null): T {
        if (title == null) {
            this.title = null
            this.url = null
        } else {
            if (title.isEmpty())
                throw IllegalArgumentException("Title cannot be empty!")
            if (title.length > TITLE_MAX_LENGTH)
                throw IllegalArgumentException("Title cannot be longer than $TITLE_MAX_LENGTH characters.")
            urlCheck(url)

            this.title = title
            this.url = url
        }
        return this as T
    }

    fun getTitle() = title
    fun getUrl() = url

    fun setUrl(url: String?): T {
        urlCheck(url)
        setTitle(getTitle(), url)
        return this as T
    }

    /* **
     * Sets the Description of the embed. This is where the main chunk of text for an embed is typically placed.
     *
     *
     * * **[Example](http://i.imgur.com/lbchtwk.png)* **
     *
     * @param  description
     *         the description of the embed, `null` to reset
     *
     *
     * @throws java.lang.IllegalArgumentException
     *         If the length of `description` is greater than [net.dv8tion.jda.core.EmbedBuilder.TEXT_MAX_LENGTH]
     *
     *
     * @return the builder after the description has been set
     */
    fun setDescription(description: CharSequence?): T {
        if (description == null || description.isEmpty()) {
            this.descriptionBuilder = StringBuilder()
        } else {
            Args.check(description.length <= TEXT_MAX_LENGTH,
                    "Description cannot be longer than %d characters.", TEXT_MAX_LENGTH)
            this.descriptionBuilder = StringBuilder(description)
        }
        return this as T
    }

    fun getDescription() = descriptionBuilder.toString()

    fun setDescription(any: Any?): T {
        return setDescription(any.toString())
    }

    fun description(block: Consumer<StringBuilder>): T {
        block.accept(descriptionBuilder)
        return this as T
    }

    inline fun description(block: StringBuilder.() -> Unit): T {
        descriptionBuilder.block()
        return this as T
    }

    /*
     * Appends to the description of the embed. This is where the main chunk of text for an embed is typically placed.
     * 
     *
     * **[Example](http://i.imgur.com/lbchtwk.png)* **
     *
     * @param  description
     *         the string to append to the description of the embed
     *
     *
     * @throws java.lang.IllegalArgumentException
     *
     *              * If the provided `description` String is null
     *              * If the length of `description` is greater than [net.dv8tion.jda.core.EmbedBuilder.TEXT_MAX_LENGTH].
     *
     *
     *
     * @return the builder after the description has been set
     */
    fun appendDescription(description: CharSequence): T {
        Args.notNull(description, "description")
        Args.check(this.descriptionBuilder.length + description.length <= TEXT_MAX_LENGTH,
                "Description cannot be longer than %d characters.", TEXT_MAX_LENGTH)
        this.descriptionBuilder.append(description)
        return this as T
    }

    /* **
     * Sets the Timestamp of the embed.

     *
     * **[Example](http://i.imgur.com/YP4NiER.png)* **

     *
     * **Hint:* ** You can get the current time using [Instant.now()][java.time.Instant.now] or convert time from a
     * millisecond representation by using [Instant.ofEpochMilli(long)][java.time.Instant.ofEpochMilli];

     * @param  temporal
     *         the temporal accessor of the timestamp
     *
     *
     * @return the builder after the timestamp has been set
     */
    fun setTimestamp(temporal: TemporalAccessor?): T {
        if (temporal == null) {
            this.timestamp = null
        } else if (temporal is OffsetDateTime) {
            this.timestamp = temporal as OffsetDateTime?
        } else {
            var offset: ZoneOffset
            try {
                offset = ZoneOffset.from(temporal)
            } catch (ignore: DateTimeException) {
                offset = ZoneOffset.UTC
            }

            try {
                val ldt = LocalDateTime.from(temporal)
                this.timestamp = OffsetDateTime.of(ldt, offset)
            } catch (ignore: DateTimeException) {
                try {
                    val instant = Instant.from(temporal)
                    this.timestamp = OffsetDateTime.ofInstant(instant, offset)
                } catch (ex: DateTimeException) {
                    throw DateTimeException("Unable to obtain OffsetDateTime from TemporalAccessor: " +
                            temporal + " of type " + temporal.javaClass.name, ex)
                }

            }

        }
        return this as T
    }

    fun getTimestamp() = timestamp

    /* **
     * Sets the Color of the embed.

     *
     * **[Example](http://i.imgur.com/2YnxnRM.png)* **

     *
     * **Hint:* ** You can use a predefined color like [java.awt.Color.BLUE] or you can define
     * your own color using one of Color's constructors.
     * <br></br>Example: [new Color(0, 0, 255)][java.awt.Color.Color]. This is the same as [java.awt.Color.BLUE]

     * @param  color
     *         the color of the embed
     *
     *
     * @return the builder after the color has been set
     */
    fun setColor(color: Color?): T {
        this.color = color
        return this as T
    }

    fun getColor() = color

    /* **
     * Sets the Thumbnail of the embed.

     *
     * **[Example](http://i.imgur.com/Zc3qwqB.png)* **

     * @param  url
     *         the url of the thumbnail of the embed
     *
     *
     * @throws java.lang.IllegalArgumentException
     *
     *              * If the length of `url` is longer than [net.dv8tion.jda.core.EmbedBuilder.URL_MAX_LENGTH].
     *              * If the provided `url` is not a properly formatted http or https url.
     *
     *
     *
     * @return the builder after the thumbnail has been set
     */
    fun setThumbnail(url: String?): T {
        if (url == null) {
            this.thumbnail = null
        } else {
            urlCheck(url)
            this.thumbnail = MessageEmbed.Thumbnail(url, null, 0, 0)
        }
        return this as T
    }

    fun getThumbnail() = thumbnail?.url


    /* **
     * Sets the Image of the embed.

     *
     * **[Example](http://i.imgur.com/2hzuHFJ.png)* **

     * @param  url
     *         the url of the image of the embed
     *
     *
     * @throws java.lang.IllegalArgumentException
     *
     *              * If the length of `url` is longer than [net.dv8tion.jda.core.EmbedBuilder.URL_MAX_LENGTH].
     *              * If the provided `url` is not a properly formatted http or https url.
     *
     *
     *
     * @return the builder after the image has been set
     */
    fun setImage(url: String?): T {
        if (url == null) {
            this.image = null
        } else {
            urlCheck(url)
            this.image = MessageEmbed.ImageInfo(url, null, 0, 0)
        }
        return this as T
    }

    fun getImage() = image?.url

    /* **
     * Sets the Author of the embed. The author appears in the top left of the embed and can have a small
     * image beside it along with the author's name being made clickable by way of providing a url.

     *
     * **[Example](http://i.imgur.com/JgZtxIM.png)* **

     * @param  name
     *         the name of the author of the embed. If this is not set, the author will not appear in the embed
     *
     * @param  url
     *         the url of the author of the embed
     *
     * @param  iconUrl
     *         the url of the icon for the author
     *
     *
     * @throws java.lang.IllegalArgumentException
     *
     *              * If the length of `url` is longer than [net.dv8tion.jda.core.EmbedBuilder.URL_MAX_LENGTH].
     *              * If the provided `url` is not a properly formatted http or https url.
     *              * If the length of `iconUrl` is longer than [net.dv8tion.jda.core.EmbedBuilder.URL_MAX_LENGTH].
     *              * If the provided `iconUrl` is not a properly formatted http or https url.
     *
     *
     *
     * @return the builder after the author has been set
     */
    fun setAuthor(name: String?, url: String?, iconUrl: String?): T {
        //We only check if the name is null because its presence is what determines if the
        // the author will appear in the embed.
        if (name == null) {
            this.author = null
        } else {
            urlCheck(url)
            urlCheck(iconUrl)
            this.author = MessageEmbed.AuthorInfo(name, url, iconUrl, null)
        }
        return this as T
    }

    fun setAuthor(user: User): T {
        return setAuthor(user.name, null, user.avatarUrl)
    }

    fun getAuthor() = author

    /* **
     * Sets the Footer of the embed.

     *
     * **[Example](http://i.imgur.com/jdf4sbi.png)* **

     * @param  text
     *         the text of the footer of the embed. If this is not set, the footer will not appear in the embed.
     *
     * @param  iconUrl
     *         the url of the icon for the footer
     *
     *
     * @throws java.lang.IllegalArgumentException
     *
     *              * If the length of `text` is longer than [net.dv8tion.jda.core.EmbedBuilder.TEXT_MAX_LENGTH].
     *              * If the length of `iconUrl` is longer than [net.dv8tion.jda.core.EmbedBuilder.URL_MAX_LENGTH].
     *              * If the provided `iconUrl` is not a properly formatted http or https url.
     *
     *
     *
     * @return the builder after the footer has been set
     */
    fun setFooter(text: String?, iconUrl: String?): T {
        //We only check if the text is null because its presence is what determines if the
        // footer will appear in the embed.
        if (text == null) {
            this.footer = null
        } else {
            if (text.length > TEXT_MAX_LENGTH)
                throw IllegalArgumentException("Text cannot be longer than $TEXT_MAX_LENGTH characters.")
            urlCheck(iconUrl)
            this.footer = MessageEmbed.Footer(text, iconUrl, null)
        }
        return this as T
    }

    fun getFooter() = footer

    /* **
     * Copies the provided Field into a new Field for this builder.
     * <br></br>For additional documentation, see [.field]

     * @param  field
     *         the field object to add
     *
     *
     * @return the builder after the field has been added
     */
    fun field(field: MessageEmbed.Field?): T {
        return if (field == null) this as T else field(field.name, field.isInline, field.value)
    }

    /* **
     * Adds a Field to the embed.

     *
     * Note: If a blank string is provided to either `name` or `value`, the blank string is replaced
     * with [net.dv8tion.jda.core.EmbedBuilder.ZERO_WIDTH_SPACE].

     *
     * **[Example of Inline](http://i.imgur.com/gnjzCoo.png)* **
     *
     * **[Example if Non-inline](http://i.imgur.com/Ky0KlsT.png)* **

     * @param  name
     *         the name of the Field, displayed in bold above the `value`.
     *
     * @param  value
     *         the contents of the field.
     *
     * @param  inline
     *         whether or not this field should display inline.
     *
     *
     * @throws java.lang.IllegalArgumentException
     *
     *              * If only `name` or `value` is set. Both must be set.
     *              * If the length of `name` is greater than [net.dv8tion.jda.core.EmbedBuilder.TITLE_MAX_LENGTH].
     *              * If the length of `value` is greater than [net.dv8tion.jda.core.EmbedBuilder.TEXT_MAX_LENGTH].
     *
     *
     *
     * @return the builder after the field has been added
     */
    fun field(name: String, inline: Boolean, value: String?): T {
        var n = name
        var v = value
        if (v == null)
            return this as T
        else if (n.length > TITLE_MAX_LENGTH)
            throw IllegalArgumentException("Name cannot be longer than $TITLE_MAX_LENGTH characters.")
        else if (v.length > VALUE_MAX_LENGTH)
            throw IllegalArgumentException("Value cannot be longer than $VALUE_MAX_LENGTH characters.")
        if (n.isEmpty())
            n = ZERO_WIDTH_SPACE
        if (v.isEmpty())
            v = ZERO_WIDTH_SPACE
        this.fields.add(MessageEmbed.Field(n, inline, v))
        return this as T
    }

    inline fun field(name: String, inline: Boolean = false, value: Any?): T {
        return field(name, inline, value.toString())
    }

    inline fun field(name: String, inline: Boolean = false, block: StringBuilder.() -> Unit): T {
        return field(name, inline, StringBuilder().apply { block() })
    }

    fun field(name: String, inline: Boolean, block: Consumer<StringBuilder>): T {
        return field(name, inline, StringBuilder().apply { block.accept(this) })
    }

    /* **
     * Adds a blank (empty) Field to the embed.

     *
     * **[Example of Inline](http://i.imgur.com/tB6tYWy.png)* **
     *
     * **[Example of Non-inline](http://i.imgur.com/lQqgH3H.png)* **

     * @param  inline
     *         whether or not this field should display inline
     *
     *
     * @return the builder after the field has been added
     */
    fun blankField(inline: Boolean): T {
        this.fields.add(MessageEmbed.Field(ZERO_WIDTH_SPACE, inline, ZERO_WIDTH_SPACE))
        return this as T
    }

    /* **
     * Clears all fields from the embed, such as those created with the
     * [EmbedBuilder(MessageEmbed)][net.dv8tion.jda.core.EmbedBuilder.EmbedBuilder]
     * constructor or via the
     * [addField][net.dv8tion.jda.core.EmbedBuilder.field] methods.

     * @return the builder after the field has been added
     */
    fun clearFields(): T {
        this.fields.clear()
        return this as T
    }

    fun getFields() = fields

    companion object {
        @JvmStatic fun urlCheck(url: String?) {
            if (url == null)
                return
            else if (url.length > URL_MAX_LENGTH)
                throw IllegalArgumentException("URL cannot be longer than $URL_MAX_LENGTH characters.")
            else if (!URL_PATTERN.matcher(url).matches())
                throw IllegalArgumentException("URL must be a valid http or https url.")
        }

        @JvmField val TITLE_MAX_LENGTH = 256
        @JvmField val VALUE_MAX_LENGTH = 1024
        @JvmField val TEXT_MAX_LENGTH = 2048
        @JvmField val URL_MAX_LENGTH = 2000
        @JvmField val ZERO_WIDTH_SPACE = "\u200E"
        @JvmField val URL_PATTERN = Pattern.compile("\\s*(https?|attachment):\\/\\/.+\\..{2,}\\s*", Pattern.CASE_INSENSITIVE)!!
    }


    var AbstractEmbedBuilder<*>.title
        inline get() = this.getTitle()
        inline set(value) {
            this.setTitle(value)
        }

    var AbstractEmbedBuilder<*>.url
        inline get() = this.getUrl()
        inline set(value) {
            this.setTitle(getTitle(), value)
        }

    var AbstractEmbedBuilder<*>.author
        inline get() = this.getAuthor()
        inline set(value) {
            this.setAuthor(value?.name, value?.url, value?.iconUrl)
        }

    var AbstractEmbedBuilder<*>.color
        inline get() = this.getColor()
        inline set(value) {
            this.setColor(value)
        }

    var AbstractEmbedBuilder<*>.description
        inline get() = this.getDescription()
        inline set(value) {
            this.setDescription(value)
        }

    var AbstractEmbedBuilder<*>.footer
        inline get() = this.getFooter()
        inline set(value) {
            this.setFooter(value?.text, value?.iconUrl)
        }

    var AbstractEmbedBuilder<*>.image
        inline get() = this.getImage()
        inline set(value) {
            this.setImage(value)
        }

    var AbstractEmbedBuilder<*>.thumbnail
        inline get() = this.getThumbnail()
        inline set(value) {
            this.setThumbnail(value)
        }

    var AbstractEmbedBuilder<*>.timestamp
        inline get() = this.getTimestamp()
        inline set(value) {
            this.setTimestamp(value)
        }

    inline fun AbstractEmbedBuilder<*>.highlight(string: String) = b("[$string]()")
    //inline fun AbstractEmbedBuilder<*>.highlight(any: Any) = highlight(any.toString())

    inline fun AbstractEmbedBuilder<*>.b(string: String) = "**$string**"
    //inline fun AbstractEmbedBuilder<*>.b(any: Any?) = b(any.toString())
    inline fun AbstractEmbedBuilder<*>.i(string: String) = "*$string*"
    //inline fun AbstractEmbedBuilder<*>.i(any: Any?) = i(any.toString())
    inline fun AbstractEmbedBuilder<*>.u(string: String) = "__${string}__"
    //inline fun AbstractEmbedBuilder<*>.u(any: Any?) = u(any.toString())

    inline fun AbstractEmbedBuilder<*>.link(string: String, url: String? = null) = "[$string]${if (url != null) "($url)" else "()"}"
    inline fun AbstractEmbedBuilder<*>.link(any: Any, url: String? = null) = "[$any]${if (url != null) "($url)" else "()"}"

}
