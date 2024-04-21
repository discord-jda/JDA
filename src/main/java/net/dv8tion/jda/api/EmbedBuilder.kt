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
package net.dv8tion.jda.api

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.MessageEmbed.*
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.entities.EntityBuilder
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.Helpers
import java.awt.Color
import java.time.OffsetDateTime
import java.time.temporal.TemporalAccessor
import java.util.*
import java.util.function.BiFunction
import java.util.function.BinaryOperator
import java.util.function.Consumer
import java.util.function.Function
import java.util.regex.Pattern
import javax.annotation.Nonnull

/**
 * Builder system used to build [MessageEmbeds][net.dv8tion.jda.api.entities.MessageEmbed].
 *
 * <br></br>A visual breakdown of an Embed and how it relates to this class is available at
 * [Embed Overview](https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/01-Overview.png).
 *
 * @since  3.0
 * @author John A. Grosh
 */
class EmbedBuilder {
    private val fields: MutableList<MessageEmbed.Field?> = ArrayList()

    /**
     * The [StringBuilder][java.lang.StringBuilder] used to
     * build the description for the embed.
     * <br></br>Note: To reset the description use [setDescription(null)][.setDescription]
     *
     * @return StringBuilder with current description context
     */
    @get:Nonnull
    val descriptionBuilder = StringBuilder()
    private var color = Role.DEFAULT_COLOR_RAW
    private var url: String? = null
    private var title: String? = null
    private var timestamp: OffsetDateTime? = null
    private var thumbnail: Thumbnail? = null
    private var author: AuthorInfo? = null
    private var footer: Footer? = null
    private var image: MessageEmbed.ImageInfo? = null

    /**
     * Constructs a new EmbedBuilder instance, which can be used to create [MessageEmbeds][net.dv8tion.jda.api.entities.MessageEmbed].
     * These can then be sent to a channel using [net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.sendMessageEmbeds].
     * <br></br>Every part of an embed can be removed or cleared by providing `null` to the setter method.
     */
    constructor()

    /**
     * Creates an EmbedBuilder using fields from an existing builder
     *
     * @param  builder
     * the existing builder
     */
    constructor(builder: EmbedBuilder?) {
        copyFrom(builder)
    }

    /**
     * Creates an EmbedBuilder using fields in an existing embed.
     *
     * @param  embed
     * the existing embed
     */
    constructor(embed: MessageEmbed?) {
        copyFrom(embed)
    }

    /**
     * Returns a [MessageEmbed][net.dv8tion.jda.api.entities.MessageEmbed]
     * that has been checked as being valid for sending.
     *
     * @throws java.lang.IllegalStateException
     *
     *  * If the embed is empty. Can be checked with [.isEmpty].
     *  * If the character limit for `description`, defined by [net.dv8tion.jda.api.entities.MessageEmbed.DESCRIPTION_MAX_LENGTH] as {@value net.dv8tion.jda.api.entities.MessageEmbed#DESCRIPTION_MAX_LENGTH},
     * is exceeded.
     *  * If the embed's total length, defined by [net.dv8tion.jda.api.entities.MessageEmbed.EMBED_MAX_LENGTH_BOT] as {@value net.dv8tion.jda.api.entities.MessageEmbed#EMBED_MAX_LENGTH_BOT},
     * is exceeded.
     *  * If the embed's number of embed fields, defined by [net.dv8tion.jda.api.entities.MessageEmbed.MAX_FIELD_AMOUNT] as {@value net.dv8tion.jda.api.entities.MessageEmbed#MAX_FIELD_AMOUNT},
     * is exceeded.
     *
     *
     * @return the built, sendable [net.dv8tion.jda.api.entities.MessageEmbed]
     */
    @Nonnull
    fun build(): MessageEmbed {
        check(!isEmpty) { "Cannot build an empty embed!" }
        check(!(descriptionBuilder.length > MessageEmbed.DESCRIPTION_MAX_LENGTH)) {
            Helpers.format(
                "Description is longer than %d! Please limit your input!",
                MessageEmbed.DESCRIPTION_MAX_LENGTH
            )
        }
        if (length() > MessageEmbed.EMBED_MAX_LENGTH_BOT) throw IllegalStateException(
            Helpers.format(
                "Cannot build an embed with more than %d characters!",
                MessageEmbed.EMBED_MAX_LENGTH_BOT
            )
        )
        if (fields.size > MessageEmbed.MAX_FIELD_AMOUNT) throw IllegalStateException(
            Helpers.format(
                "Cannot build an embed with more than %d embed fields set!",
                MessageEmbed.MAX_FIELD_AMOUNT
            )
        )
        val description = if (descriptionBuilder.length < 1) null else descriptionBuilder.toString()
        return EntityBuilder.createMessageEmbed(
            url, title, description, EmbedType.RICH, timestamp,
            color, thumbnail, null, author, null, footer, image, LinkedList(fields)
        )
    }

    /**
     * Resets this builder to default state.
     * <br></br>All parts will be either empty or null after this method has returned.
     *
     * @return The current EmbedBuilder with default values
     */
    @Nonnull
    fun clear(): EmbedBuilder {
        descriptionBuilder.setLength(0)
        fields.clear()
        url = null
        title = null
        timestamp = null
        color = Role.DEFAULT_COLOR_RAW
        thumbnail = null
        author = null
        footer = null
        image = null
        return this
    }

    /**
     * Copies the data from the given builder into this builder.
     * <br></br>All the parts of the given builder will be applied to this one.
     *
     * @param  builder
     * the existing builder
     */
    fun copyFrom(builder: EmbedBuilder?) {
        if (builder != null) {
            setDescription(builder.descriptionBuilder.toString())
            clearFields()
            fields.addAll(builder.fields)
            url = builder.url
            title = builder.title
            timestamp = builder.timestamp
            color = builder.color
            thumbnail = builder.thumbnail
            author = builder.author
            footer = builder.footer
            image = builder.image
        }
    }

    /**
     * Copies the data from the given embed into this builder.
     * <br></br>All the parts of the given embed will be applied to this builder.
     *
     * @param  embed
     * the existing embed
     */
    fun copyFrom(embed: MessageEmbed?) {
        if (embed != null) {
            setDescription(embed.description)
            clearFields()
            fields.addAll(embed.fields!!)
            url = embed.url
            title = embed.title
            timestamp = embed.timestamp
            color = embed.colorRaw
            thumbnail = embed.thumbnail
            author = embed.author
            footer = embed.footer
            image = embed.image
        }
    }

    val isEmpty: Boolean
        /**
         * Checks if the given embed is empty. Empty embeds will throw an exception if built.
         *
         * @return true if the embed is empty and cannot be built
         */
        get() = ((title == null || title!!.trim { it <= ' ' }.isEmpty())
                && (timestamp == null
                ) && (thumbnail == null
                ) && (author == null
                ) && (footer == null
                ) && (image == null
                ) && (color == Role.DEFAULT_COLOR_RAW
                ) && (descriptionBuilder.length == 0
                ) && fields.isEmpty())

    /**
     * The overall length of the current EmbedBuilder in displayed characters.
     * <br></br>Represents the [MessageEmbed.getLength()][net.dv8tion.jda.api.entities.MessageEmbed.getLength] value.
     *
     * @return length of the current builder state
     */
    fun length(): Int {
        var length = descriptionBuilder.toString().trim { it <= ' ' }.length
        synchronized(fields) {
            length = fields.stream().map(Function { f: MessageEmbed.Field? -> f!!.name!!.length + f.value!!.length })
                .reduce(length, BinaryOperator { a: Int, b: Int -> Integer.sum(a, b) })
        }
        if (title != null) length += title!!.length
        if (author != null) length += author!!.getName()!!.length
        if (footer != null) length += footer!!.getText()!!.length
        return length
    }

    val isValidLength: Boolean
        /**
         * Checks whether the constructed [MessageEmbed][net.dv8tion.jda.api.entities.MessageEmbed]
         * is within the limits for a bot account.
         *
         * @return True, if the [length][.length] is less or equal to {@value net.dv8tion.jda.api.entities.MessageEmbed#EMBED_MAX_LENGTH_BOT}
         *
         * @see MessageEmbed.EMBED_MAX_LENGTH_BOT
         */
        get() {
            val length = length()
            return length <= MessageEmbed.EMBED_MAX_LENGTH_BOT
        }

    /**
     * Sets the Title of the embed.
     * <br></br>Overload for [.setTitle] without URL parameter.
     *
     *
     * **[Example](https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/04-setTitle.png)**
     *
     * @param  title
     * the title of the embed
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * If the provided `title` is an empty String.
     *  * If the character limit for `title`, defined by [net.dv8tion.jda.api.entities.MessageEmbed.TITLE_MAX_LENGTH] as {@value net.dv8tion.jda.api.entities.MessageEmbed#TITLE_MAX_LENGTH},
     * is exceeded.
     *
     *
     * @return the builder after the title has been set
     */
    @Nonnull
    fun setTitle(title: String?): EmbedBuilder {
        return setTitle(title, null)
    }

    /**
     * Sets the Title of the embed.
     * <br></br>You can provide `null` as url if no url should be used.
     * <br></br>If you want to set a URL without a title, use [.setUrl] instead.
     *
     *
     * **[Example](https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/04-setTitle.png)**
     *
     * @param  title
     * the title of the embed
     * @param  url
     * Makes the title into a hyperlink pointed at this url.
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * If the provided `title` is an empty String.
     *  * If the character limit for `title`, defined by [net.dv8tion.jda.api.entities.MessageEmbed.TITLE_MAX_LENGTH] as {@value net.dv8tion.jda.api.entities.MessageEmbed#TITLE_MAX_LENGTH},
     * is exceeded.
     *  * If the character limit for `url`, defined by [net.dv8tion.jda.api.entities.MessageEmbed.URL_MAX_LENGTH] as {@value net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH},
     * is exceeded.
     *  * If the provided `url` is not a properly formatted http or https url.
     *
     *
     * @return the builder after the title has been set
     */
    @Nonnull
    fun setTitle(title: String?, url: String?): EmbedBuilder {
        var url = url
        if (title == null) {
            this.title = null
            this.url = null
        } else {
            Checks.notEmpty(title, "Title")
            Checks.check(
                title.length <= MessageEmbed.TITLE_MAX_LENGTH,
                "Title cannot be longer than %d characters.",
                MessageEmbed.TITLE_MAX_LENGTH
            )
            if (Helpers.isBlank(url)) url = null
            urlCheck(url)
            this.title = title
            this.url = url
        }
        return this
    }

    /**
     * Sets the URL of the embed.
     * <br></br>The Discord client mostly only uses this property in combination with the [title][.setTitle] for a clickable Hyperlink.
     *
     *
     * If multiple embeds in a message use the same URL, the Discord client will merge them into a single embed and aggregate images into a gallery view.
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * If the character limit for `url`, defined by [net.dv8tion.jda.api.entities.MessageEmbed.URL_MAX_LENGTH] as {@value net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH},
     * is exceeded.
     *  * If the provided `url` is not a properly formatted http or https url.
     *
     *
     * @return the builder after the URL has been set
     *
     * @see .setTitle
     */
    @Nonnull
    fun setUrl(url: String?): EmbedBuilder {
        var url = url
        if (Helpers.isBlank(url)) url = null
        urlCheck(url)
        this.url = url
        return this
    }

    /**
     * Sets the Description of the embed. This is where the main chunk of text for an embed is typically placed.
     *
     *
     * **[Example](https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/05-setDescription.png)**
     *
     * @param  description
     * the description of the embed, `null` to reset
     *
     * @throws java.lang.IllegalArgumentException
     * If `description` is longer than {@value net.dv8tion.jda.api.entities.MessageEmbed#DESCRIPTION_MAX_LENGTH} characters,
     * as defined by [net.dv8tion.jda.api.entities.MessageEmbed.DESCRIPTION_MAX_LENGTH]
     *
     * @return the builder after the description has been set
     */
    @Nonnull
    fun setDescription(description: CharSequence?): EmbedBuilder {
        descriptionBuilder.setLength(0)
        if (description != null && description.length >= 1) appendDescription(description)
        return this
    }

    /**
     * Appends to the description of the embed. This is where the main chunk of text for an embed is typically placed.
     *
     *
     * **[Example](https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/05-setDescription.png)**
     *
     * @param  description
     * the string to append to the description of the embed
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * If the provided `description` String is null.
     *  * If the character limit for `description`, defined by [net.dv8tion.jda.api.entities.MessageEmbed.DESCRIPTION_MAX_LENGTH] as {@value net.dv8tion.jda.api.entities.MessageEmbed#DESCRIPTION_MAX_LENGTH},
     * is exceeded.
     *
     *
     * @return the builder after the description has been set
     */
    @Nonnull
    fun appendDescription(@Nonnull description: CharSequence): EmbedBuilder {
        Checks.notNull(description, "description")
        Checks.check(
            descriptionBuilder.length + description.length <= MessageEmbed.DESCRIPTION_MAX_LENGTH,
            "Description cannot be longer than %d characters.", MessageEmbed.DESCRIPTION_MAX_LENGTH
        )
        descriptionBuilder.append(description)
        return this
    }

    /**
     * Sets the Timestamp of the embed.
     *
     *
     * **[Example](https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/13-setTimestamp.png)**
     *
     *
     * **Hint:** You can get the current time using [Instant.now()][java.time.Instant.now] or convert time from a
     * millisecond representation by using [Instant.ofEpochMilli(long)][java.time.Instant.ofEpochMilli];
     *
     * @param  temporal
     * the temporal accessor of the timestamp
     *
     * @return the builder after the timestamp has been set
     */
    @Nonnull
    fun setTimestamp(temporal: TemporalAccessor?): EmbedBuilder {
        timestamp = Helpers.toOffsetDateTime(temporal)
        return this
    }

    /**
     * Sets the Color of the embed.
     *
     *
     * **[Example](https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/02-setColor.png)**
     *
     * @param  color
     * The [Color][java.awt.Color] of the embed
     * or `null` to use no color
     *
     * @return the builder after the color has been set
     *
     * @see .setColor
     */
    @Nonnull
    fun setColor(color: Color?): EmbedBuilder {
        this.color = color?.rgb ?: Role.DEFAULT_COLOR_RAW
        return this
    }

    /**
     * Sets the raw RGB color value for the embed.
     *
     *
     * **[Example](https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/02-setColor.png)**
     *
     * @param  color
     * The raw rgb value, or [Role.DEFAULT_COLOR_RAW] to use no color
     *
     * @return the builder after the color has been set
     *
     * @see .setColor
     */
    @Nonnull
    fun setColor(color: Int): EmbedBuilder {
        this.color = color
        return this
    }

    /**
     * Sets the Thumbnail of the embed.
     *
     *
     * **[Example](https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/06-setThumbnail.png)**
     *
     *
     * **Uploading images with Embeds**
     * <br></br>When uploading an <u>image</u>
     * (using [MessageChannel.sendFiles(...)][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.sendFiles])
     * you can reference said image using the specified filename as URI `attachment://filename.ext`.
     *
     *
     * <u>Example</u>
     * <pre>`
     * MessageChannel channel; // = reference of a MessageChannel
     * EmbedBuilder embed = new EmbedBuilder();
     * InputStream file = new URL("https://http.cat/500").openStream();
     * embed.setThumbnail("attachment://cat.png") // we specify this in sendFile as "cat.png"
     * .setDescription("This is a cute cat :3");
     * channel.sendFiles(FileUpload.fromData(file, "cat.png")).setEmbeds(embed.build()).queue();
    `</pre> *
     *
     * @param  url
     * the url of the thumbnail of the embed
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * If the character limit for `url`, defined by [net.dv8tion.jda.api.entities.MessageEmbed.URL_MAX_LENGTH] as {@value net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH},
     * is exceeded.
     *  * If the provided `url` is not a properly formatted http or https url.
     *
     *
     * @return the builder after the thumbnail has been set
     */
    @Nonnull
    fun setThumbnail(url: String?): EmbedBuilder {
        if (url == null) {
            thumbnail = null
        } else {
            urlCheck(url)
            thumbnail = Thumbnail(url, null, 0, 0)
        }
        return this
    }

    /**
     * Sets the Image of the embed.
     *
     *
     * **[Example](https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/11-setImage.png)**
     *
     *
     * **Uploading images with Embeds**
     * <br></br>When uploading an <u>image</u>
     * (using [MessageChannel.sendFiles(...)][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.sendFiles])
     * you can reference said image using the specified filename as URI `attachment://filename.ext`.
     *
     *
     * <u>Example</u>
     * <pre>`
     * MessageChannel channel; // = reference of a MessageChannel
     * EmbedBuilder embed = new EmbedBuilder();
     * InputStream file = new URL("https://http.cat/500").openStream();
     * embed.setImage("attachment://cat.png") // we specify this in sendFile as "cat.png"
     * .setDescription("This is a cute cat :3");
     * channel.sendFiles(FileUpload.fromData(file, "cat.png")).setEmbeds(embed.build()).queue();
    `</pre> *
     *
     * @param  url
     * the url of the image of the embed
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * If the character limit for `url`, defined by [net.dv8tion.jda.api.entities.MessageEmbed.URL_MAX_LENGTH] as {@value net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH},
     * is exceeded.
     *  * If the provided `url` is not a properly formatted http or https url.
     *
     *
     * @return the builder after the image has been set
     *
     * @see net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.sendFiles
     */
    @Nonnull
    fun setImage(url: String?): EmbedBuilder {
        if (url == null) {
            image = null
        } else {
            urlCheck(url)
            image = MessageEmbed.ImageInfo(url, null, 0, 0)
        }
        return this
    }

    /**
     * Sets the Author of the embed. The author appears in the top left of the embed and can have a small
     * image beside it along with the author's name being made clickable by way of providing a url.
     * This convenience method just sets the name.
     *
     *
     * **[Example](https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/03-setAuthor.png)**
     *
     * @param  name
     * the name of the author of the embed. If this is not set, the author will not appear in the embed
     *
     * @throws java.lang.IllegalArgumentException
     * If `name` is longer than {@value net.dv8tion.jda.api.entities.MessageEmbed#AUTHOR_MAX_LENGTH} characters,
     * as defined by [net.dv8tion.jda.api.entities.MessageEmbed.AUTHOR_MAX_LENGTH]
     *
     * @return the builder after the author has been set
     */
    @Nonnull
    fun setAuthor(name: String?): EmbedBuilder {
        return setAuthor(name, null, null)
    }

    /**
     * Sets the Author of the embed. The author appears in the top left of the embed and can have a small
     * image beside it along with the author's name being made clickable by way of providing a url.
     * This convenience method just sets the name and the url.
     *
     *
     * **[Example](https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/03-setAuthor.png)**
     *
     * @param  name
     * the name of the author of the embed. If this is not set, the author will not appear in the embed
     * @param  url
     * the url of the author of the embed
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * If the character limit for `name`, defined by [net.dv8tion.jda.api.entities.MessageEmbed.AUTHOR_MAX_LENGTH] as {@value net.dv8tion.jda.api.entities.MessageEmbed#AUTHOR_MAX_LENGTH},
     * is exceeded.
     *  * If the character limit for `url`, defined by [net.dv8tion.jda.api.entities.MessageEmbed.URL_MAX_LENGTH] as {@value net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH},
     * is exceeded.
     *  * If the provided `url` is not a properly formatted http or https url.
     *
     *
     * @return the builder after the author has been set
     */
    @Nonnull
    fun setAuthor(name: String?, url: String?): EmbedBuilder {
        return setAuthor(name, url, null)
    }

    /**
     * Sets the Author of the embed. The author appears in the top left of the embed and can have a small
     * image beside it along with the author's name being made clickable by way of providing a url.
     *
     *
     * **[Example](https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/03-setAuthor.png)**
     *
     *
     * **Uploading images with Embeds**
     * <br></br>When uploading an <u>image</u>
     * (using [MessageChannel.sendFiles(...)][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.sendFiles])
     * you can reference said image using the specified filename as URI `attachment://filename.ext`.
     *
     *
     * <u>Example</u>
     * <pre>`
     * MessageChannel channel; // = reference of a MessageChannel
     * EmbedBuilder embed = new EmbedBuilder();
     * InputStream file = new URL("https://http.cat/500").openStream();
     * embed.setAuthor("Minn", null, "attachment://cat.png") // we specify this in sendFile as "cat.png"
     * .setDescription("This is a cute cat :3");
     * channel.sendFiles(FileUpload.fromData(file, "cat.png")).setEmbeds(embed.build()).queue();
    `</pre> *
     *
     * @param  name
     * the name of the author of the embed. If this is not set, the author will not appear in the embed
     * @param  url
     * the url of the author of the embed
     * @param  iconUrl
     * the url of the icon for the author
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * If the character limit for `name`, defined by [net.dv8tion.jda.api.entities.MessageEmbed.AUTHOR_MAX_LENGTH] as {@value net.dv8tion.jda.api.entities.MessageEmbed#AUTHOR_MAX_LENGTH},
     * is exceeded.
     *  * If the character limit for `url`, defined by [net.dv8tion.jda.api.entities.MessageEmbed.URL_MAX_LENGTH] as {@value net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH},
     * is exceeded.
     *  * If the provided `url` is not a properly formatted http or https url.
     *  * If the character limit for `iconUrl`, defined by [net.dv8tion.jda.api.entities.MessageEmbed.URL_MAX_LENGTH] as {@value net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH},
     * is exceeded.
     *  * If the provided `iconUrl` is not a properly formatted http or https url.
     *
     *
     * @return the builder after the author has been set
     */
    @Nonnull
    fun setAuthor(name: String?, url: String?, iconUrl: String?): EmbedBuilder {
        // We only check if the name is null because its presence is what determines if the author will appear in the embed.
        if (name == null) {
            author = null
        } else {
            Checks.notLonger(name, MessageEmbed.AUTHOR_MAX_LENGTH, "Name")
            urlCheck(url)
            urlCheck(iconUrl)
            author = AuthorInfo(name, url!!, iconUrl!!, null)
        }
        return this
    }

    /**
     * Sets the Footer of the embed without icon.
     *
     *
     * **[Example](https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/12-setFooter.png)**
     *
     * @param  text
     * the text of the footer of the embed. If this is not set or set to null, the footer will not appear in the embed.
     *
     * @throws java.lang.IllegalArgumentException
     * If `text` is longer than {@value net.dv8tion.jda.api.entities.MessageEmbed#TEXT_MAX_LENGTH} characters,
     * as defined by [net.dv8tion.jda.api.entities.MessageEmbed.TEXT_MAX_LENGTH]
     *
     * @return the builder after the footer has been set
     */
    @Nonnull
    fun setFooter(text: String?): EmbedBuilder {
        return setFooter(text, null)
    }

    /**
     * Sets the Footer of the embed.
     *
     *
     * **[Example](https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/12-setFooter.png)**
     *
     *
     * **Uploading images with Embeds**
     * <br></br>When uploading an <u>image</u>
     * (using [MessageChannel.sendFiles(...)][net.dv8tion.jda.api.entities.channel.middleman.MessageChannel.sendFiles])
     * you can reference said image using the specified filename as URI `attachment://filename.ext`.
     *
     *
     * <u>Example</u>
     * <pre>`
     * MessageChannel channel; // = reference of a MessageChannel
     * EmbedBuilder embed = new EmbedBuilder();
     * InputStream file = new URL("https://http.cat/500").openStream();
     * embed.setFooter("Cool footer!", "attachment://cat.png") // we specify this in sendFile as "cat.png"
     * .setDescription("This is a cute cat :3");
     * channel.sendFiles(FileUpload.fromData(file, "cat.png")).setEmbeds(embed.build()).queue();
    `</pre> *
     *
     * @param  text
     * the text of the footer of the embed. If this is not set, the footer will not appear in the embed.
     * @param  iconUrl
     * the url of the icon for the footer
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * If the character limit for `text`, defined by [net.dv8tion.jda.api.entities.MessageEmbed.TEXT_MAX_LENGTH] as {@value net.dv8tion.jda.api.entities.MessageEmbed#TEXT_MAX_LENGTH},
     * is exceeded.
     *  * If the character limit for `iconUrl`, defined by [net.dv8tion.jda.api.entities.MessageEmbed.URL_MAX_LENGTH] as {@value net.dv8tion.jda.api.entities.MessageEmbed#URL_MAX_LENGTH},
     * is exceeded.
     *  * If the provided `iconUrl` is not a properly formatted http or https url.
     *
     *
     * @return the builder after the footer has been set
     */
    @Nonnull
    fun setFooter(text: String?, iconUrl: String?): EmbedBuilder {
        //We only check if the text is null because its presence is what determines if the
        // footer will appear in the embed.
        if (text == null) {
            footer = null
        } else {
            Checks.notLonger(text, MessageEmbed.TEXT_MAX_LENGTH, "Text")
            urlCheck(iconUrl)
            footer = Footer(text, iconUrl!!, null)
        }
        return this
    }

    /**
     * Copies the provided Field into a new Field for this builder.
     * <br></br>For additional documentation, see [.addField]
     *
     * @param  field
     * the field object to add
     *
     * @return the builder after the field has been added
     */
    @Nonnull
    fun addField(field: MessageEmbed.Field?): EmbedBuilder {
        return if (field == null) this else addField(field.name, field.value, field.isInline)
    }

    /**
     * Adds a Field to the embed.
     *
     *
     * Note: If a blank string is provided to either `name` or `value`, the blank string is replaced
     * with [net.dv8tion.jda.api.EmbedBuilder.ZERO_WIDTH_SPACE].
     *
     *
     * **[Example of Inline](https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/07-addField.png)**
     *
     * **[Example of Non-inline](https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/08-addField.png)**
     *
     * @param  name
     * the name of the Field, displayed in bold above the `value`.
     * @param  value
     * the contents of the field.
     * @param  inline
     * whether or not this field should display inline.
     *
     * @throws java.lang.IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If the character limit for `name`, defined by [net.dv8tion.jda.api.entities.MessageEmbed.TITLE_MAX_LENGTH] as {@value net.dv8tion.jda.api.entities.MessageEmbed#TITLE_MAX_LENGTH},
     * is exceeded.
     *  * If the character limit for `value`, defined by [net.dv8tion.jda.api.entities.MessageEmbed.VALUE_MAX_LENGTH] as {@value net.dv8tion.jda.api.entities.MessageEmbed#VALUE_MAX_LENGTH},
     * is exceeded.
     *
     *
     * @return the builder after the field has been added
     */
    @Nonnull
    fun addField(@Nonnull name: String?, @Nonnull value: String?, inline: Boolean): EmbedBuilder {
        Checks.notNull(name, "Name")
        Checks.notNull(value, "Value")
        fields.add(MessageEmbed.Field(name, value, inline))
        return this
    }

    /**
     * Adds a blank (empty) Field to the embed.
     *
     *
     * **[Example of Inline](https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/07-addField.png)**
     *
     * **[Example of Non-inline](https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/08-addField.png)**
     *
     * @param  inline
     * whether or not this field should display inline
     *
     * @return the builder after the field has been added
     */
    @Nonnull
    fun addBlankField(inline: Boolean): EmbedBuilder {
        fields.add(MessageEmbed.Field(ZERO_WIDTH_SPACE, ZERO_WIDTH_SPACE, inline))
        return this
    }

    /**
     * Clears all fields from the embed, such as those created with the
     * [EmbedBuilder(MessageEmbed)][net.dv8tion.jda.api.EmbedBuilder.EmbedBuilder]
     * constructor or via the
     * [addField][net.dv8tion.jda.api.EmbedBuilder.addField] methods.
     *
     * @return the builder after the field has been added
     */
    @Nonnull
    fun clearFields(): EmbedBuilder {
        fields.clear()
        return this
    }

    /**
     * **Modifiable** list of [MessageEmbed][net.dv8tion.jda.api.entities.MessageEmbed] Fields that the builder will
     * use for [.build].
     * <br></br>You can add/remove Fields and restructure this [List][java.util.List] and it will then be applied in the
     * built MessageEmbed. These fields will be available again through [MessageEmbed.getFields()][net.dv8tion.jda.api.entities.MessageEmbed.getFields].
     *
     * @return Mutable List of [Fields][net.dv8tion.jda.api.entities.MessageEmbed.Field]
     */
    @Nonnull
    fun getFields(): List<MessageEmbed.Field?> {
        return fields
    }

    private fun urlCheck(url: String?) {
        if (url != null) {
            Checks.notLonger(url, MessageEmbed.URL_MAX_LENGTH, "URL")
            Checks.check(URL_PATTERN.matcher(url).matches(), "URL must be a valid http(s) or attachment url.")
        }
    }

    companion object {
        const val ZERO_WIDTH_SPACE = "\u200E"
        val URL_PATTERN = Pattern.compile("\\s*(https?|attachment)://\\S+\\s*", Pattern.CASE_INSENSITIVE)

        /**
         * Creates an instance of this builder from the provided [DataObject].
         *
         *
         * This is the inverse of [MessageEmbed.toData].
         *
         * @param  data
         * The serialized embed object
         *
         * @throws IllegalArgumentException
         * If the provided data is `null` or invalid
         * @throws net.dv8tion.jda.api.exceptions.ParsingException
         * If the provided data is malformed
         *
         * @return The new builder instance
         */
        @JvmStatic
        @Nonnull
        fun fromData(@Nonnull data: DataObject): EmbedBuilder {
            Checks.notNull(data, "DataObject")
            val builder = EmbedBuilder()
            builder.setTitle(data.getString("title", null))
            builder.setUrl(data.getString("url", null))
            builder.setDescription(data.getString("description", ""))
            builder.setTimestamp(if (data.isNull("timestamp")) null else OffsetDateTime.parse(data.getString("timestamp")))
            builder.setColor(data.getInt("color", Role.DEFAULT_COLOR_RAW))
            data.optObject("thumbnail").ifPresent(
                { thumbnail: DataObject -> builder.setThumbnail(thumbnail.getString("url")) }
            )
            data.optObject("author").ifPresent(
                { author: DataObject ->
                    builder.setAuthor(
                        author.getString("name", ""),
                        author.getString("url", null),
                        author.getString("icon_url", null)
                    )
                }
            )
            data.optObject("footer").ifPresent(
                { footer: DataObject ->
                    builder.setFooter(
                        footer.getString("text", ""),
                        footer.getString("icon_url", null)
                    )
                }
            )
            data.optObject("image").ifPresent(
                { image: DataObject -> builder.setImage(image.getString("url")) }
            )
            data.optArray("fields").ifPresent(
                { arr: DataArray ->
                    arr.stream(BiFunction { obj: DataArray?, index: Int? ->
                        obj!!.getObject(
                            (index)!!
                        )
                    }).forEach(
                        Consumer { field: DataObject ->
                            builder.addField(
                                field.getString("name", ZERO_WIDTH_SPACE),
                                field.getString("value", ZERO_WIDTH_SPACE),
                                field.getBoolean("inline", false)
                            )
                        }
                    )
                }
            )
            return builder
        }
    }
}
