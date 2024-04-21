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
package net.dv8tion.jda.api.entities

import net.dv8tion.jda.annotations.ForRemoval
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.utils.*
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.SerializableData
import net.dv8tion.jda.internal.utils.Helpers
import java.awt.Color
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.annotation.Nonnull
import kotlin.concurrent.Volatile

/**
 * Represents an embed displayed by Discord.
 * <br></br>A visual representation of an Embed can be found at:
 * [Embed Overview](https://raw.githubusercontent.com/discord-jda/JDA/assets/assets/docs/embeds/01-Overview.png)
 * <br></br>This class has many possibilities for null values, so be careful!
 *
 * @see EmbedBuilder
 *
 * @see Message.getEmbeds
 */
class MessageEmbed(
    url: String?, title: String?, description: String?, type: EmbedType, timestamp: OffsetDateTime?,
    color: Int, thumbnail: Thumbnail?, siteProvider: Provider?, author: AuthorInfo?,
    videoInfo: VideoInfo?, footer: Footer?, image: ImageInfo?, fields: List<Field?>?
) : SerializableData {
    protected val mutex = Any()

    /**
     * The url that was originally placed into chat that spawned this embed.
     * <br></br>**This will return the [title url][.getTitle] if the [type][.getType] of this embed is [RICH][EmbedType.RICH].**
     *
     * @return Possibly-null String containing the link that spawned this embed or the title url
     */
    @JvmField
    val url: String?

    /**
     * The title of the embed. Typically this will be the html title of the webpage that is being embedded.<br></br>
     * If no title could be found, like the case of [EmbedType] = [IMAGE][net.dv8tion.jda.api.entities.EmbedType.IMAGE],
     * this method will return null.
     *
     * @return Possibly-null String containing the title of the embedded resource.
     */
    @JvmField
    val title: String?

    /**
     * The description of the embedded resource.
     * <br></br>This is provided only if Discord could find a description for the embedded resource using the provided url.
     * <br></br>Commonly, this is null. Be careful when using it.
     *
     * @return Possibly-null String containing a description of the embedded resource.
     */
    @JvmField
    val description: String?

    /**
     * The [EmbedType][net.dv8tion.jda.api.entities.EmbedType] of this embed.
     *
     * @return The [EmbedType][net.dv8tion.jda.api.entities.EmbedType] of this embed.
     */
    @JvmField
    @get:Nonnull
    val type: EmbedType

    /**
     * The timestamp of the embed.
     *
     * @return Possibly-null OffsetDateTime object representing the timestamp.
     */
    @JvmField
    val timestamp: OffsetDateTime?

    /**
     * The raw RGB color value for this embed
     * <br></br>Defaults to [Role.DEFAULT_COLOR_RAW] if no color is set
     *
     * @return The raw RGB color value or default
     */
    val colorRaw: Int

    /**
     * The information about the [Thumbnail][net.dv8tion.jda.api.entities.MessageEmbed.Thumbnail] image to be displayed with the embed.
     * <br></br>If a [Thumbnail][net.dv8tion.jda.api.entities.MessageEmbed.Thumbnail] was not part of this embed, this returns null.
     *
     * @return Possibly-null [Thumbnail][net.dv8tion.jda.api.entities.MessageEmbed.Thumbnail] instance
     * containing general information on the displayable thumbnail.
     */
    @JvmField
    val thumbnail: Thumbnail?

    /**
     * The information on site from which the embed was generated from.
     * <br></br>If Discord did not generate any deliverable information about the site, this returns null.
     *
     * @return Possibly-null [Provider][net.dv8tion.jda.api.entities.MessageEmbed.Provider]
     * containing site information.
     */
    val siteProvider: Provider?

    /**
     * The information on the creator of the embedded content.
     * <br></br>This is typically used to represent the account on the providing site.
     *
     * @return Possibly-null [AuthorInfo][net.dv8tion.jda.api.entities.MessageEmbed.AuthorInfo]
     * containing author information.
     */
    @JvmField
    val author: AuthorInfo?

    /**
     * The information about the video which should be displayed as an embed.
     * <br></br>This is used when sites with HTML5 players are linked and embedded. Most commonly Youtube.
     * <br></br>If this [EmbedType][net.dv8tion.jda.api.entities.EmbedType] != [VIDEO][net.dv8tion.jda.api.entities.EmbedType.VIDEO]
     * this will always return null.
     *
     * @return Possibly-null [VideoInfo][net.dv8tion.jda.api.entities.MessageEmbed.VideoInfo]
     * containing the information about the video which should be embedded.
     */
    val videoInfo: VideoInfo?

    /**
     * The footer (bottom) of the embedded content.
     * <br></br>This is typically used for timestamps or site icons.
     *
     * @return Possibly-null [Footer][net.dv8tion.jda.api.entities.MessageEmbed.Footer]
     * containing the embed footer content.
     */
    @JvmField
    val footer: Footer?

    /**
     * The information about the image in the message embed
     *
     * @return Possibly-null [ImageInfo][net.dv8tion.jda.api.entities.MessageEmbed.ImageInfo]
     * containing image information.
     */
    @JvmField
    val image: ImageInfo?

    /**
     * The fields in a message embed.
     * <br></br>Message embeds can contain multiple fields, each with a name, value, and a boolean
     * to determine if it will fall in-line with other fields. If the embed contains no
     * fields, an empty list will be returned.
     *
     * @return Never-null (but possibly empty) immutable  List of [Field][net.dv8tion.jda.api.entities.MessageEmbed.Field] objects
     * containing field information.
     */
    @JvmField
    @get:Nonnull
    val fields: List<Field?>?

    @Volatile
    protected var length = -1

    @Volatile
    protected var json: DataObject? = null

    init {
        this.url = url
        this.title = title
        this.description = description
        this.type = type
        this.timestamp = timestamp
        colorRaw = color
        this.thumbnail = thumbnail
        this.siteProvider = siteProvider
        this.author = author
        this.videoInfo = videoInfo
        this.footer = footer
        this.image = image
        this.fields =
            if (fields != null && !fields.isEmpty()) Collections.unmodifiableList(fields) else emptyList<Field>()
    }

    /**
     * The color of the stripe on the side of the embed.
     * <br></br>If the color is 0 (no color), this will return null.
     *
     * @return Possibly-null Color.
     */
    fun getColor(): Color? {
        return if (colorRaw != Role.Companion.DEFAULT_COLOR_RAW) Color(colorRaw) else null
    }

    val isEmpty: Boolean
        /**
         * Whether this embed is empty.
         *
         * @return True, if this embed has no content
         */
        get() = (colorRaw == Role.Companion.DEFAULT_COLOR_RAW
                ) && (timestamp == null
                ) && (image == null
                ) && (thumbnail == null
                ) && (getLength() == 0)

    /**
     * The total amount of characters that is displayed when this embed is displayed by the Discord client.
     *
     *
     * The total character limit is defined by [.EMBED_MAX_LENGTH_BOT] as {@value #EMBED_MAX_LENGTH_BOT}.
     *
     * @return A never-negative sum of all displayed text characters.
     */
    fun getLength(): Int {
        if (length > -1) return length
        synchronized(mutex) {
            if (length > -1) return length
            length = 0
            if (title != null) length += Helpers.codePointLength(title)
            if (description != null) length += Helpers.codePointLength(description.trim { it <= ' ' })
            if (author != null) length += Helpers.codePointLength(author.getName())
            if (footer != null) length += Helpers.codePointLength(footer.getText())
            if (fields != null) {
                for (f: Field? in fields!!) length += Helpers.codePointLength(
                    f!!.name
                ) + Helpers.codePointLength(f.value)
            }
            return length
        }
    }

    val isSendable: Boolean
        /**
         * Whether this MessageEmbed can be used in a message.
         *
         *
         * The total character limit is defined by [.EMBED_MAX_LENGTH_BOT] as {@value #EMBED_MAX_LENGTH_BOT}.
         *
         * @return True, if this MessageEmbed can be used to send messages
         *
         * @see .getLength
         */
        get() {
            if (isEmpty) return false
            val length = getLength()
            return length <= EMBED_MAX_LENGTH_BOT
        }

    override fun equals(obj: Any?): Boolean {
        if (obj !is MessageEmbed) return false
        if (obj === this) return true
        val other = obj
        return (((url == other.url)
                && (title == other.title)
                && (description == other.description)
                && (type == other.type)
                && (thumbnail == other.thumbnail)
                && (siteProvider == other.siteProvider)
                && (author == other.author)
                && (videoInfo == other.videoInfo)
                && (footer == other.footer)
                && (image == other.image) && colorRaw and 0xFFFFFF == other.colorRaw and 0xFFFFFF) && (timestamp == other.timestamp)
                && Helpers.deepEquals(fields, other.fields))
    }

    /**
     * Creates a new [net.dv8tion.jda.api.utils.data.DataObject]
     * used for sending.
     *
     * @return JSONObject for this embed
     */
    @Nonnull
    override fun toData(): DataObject {
        if (json != null) return json
        synchronized(mutex) {
            if (json != null) return json
            val obj: DataObject = DataObject.empty()
            if (url != null) obj.put("url", url)
            if (title != null) obj.put("title", title)
            if (description != null) obj.put("description", description)
            if (timestamp != null) obj.put("timestamp", timestamp.format(DateTimeFormatter.ISO_INSTANT))
            if (colorRaw != Role.Companion.DEFAULT_COLOR_RAW) obj.put("color", colorRaw and 0xFFFFFF)
            if (thumbnail != null) obj.put("thumbnail", DataObject.empty().put("url", thumbnail.getUrl()))
            if (siteProvider != null) {
                val siteProviderObj: DataObject = DataObject.empty()
                if (siteProvider.getName() != null) siteProviderObj.put("name", siteProvider.getName())
                if (siteProvider.getUrl() != null) siteProviderObj.put("url", siteProvider.getUrl())
                obj.put("provider", siteProviderObj)
            }
            if (author != null) {
                val authorObj: DataObject = DataObject.empty()
                if (author.getName() != null) authorObj.put("name", author.getName())
                if (author.getUrl() != null) authorObj.put("url", author.getUrl())
                if (author.getIconUrl() != null) authorObj.put("icon_url", author.getIconUrl())
                obj.put("author", authorObj)
            }
            if (videoInfo != null) obj.put("video", DataObject.empty().put("url", videoInfo.getUrl()))
            if (footer != null) {
                val footerObj: DataObject = DataObject.empty()
                if (footer.getText() != null) footerObj.put("text", footer.getText())
                if (footer.getIconUrl() != null) footerObj.put("icon_url", footer.getIconUrl())
                obj.put("footer", footerObj)
            }
            if (image != null) obj.put("image", DataObject.empty().put("url", image.getUrl()))
            if (!fields!!.isEmpty()) {
                val fieldsArray: DataArray = DataArray.empty()
                for (field: Field? in fields!!) {
                    fieldsArray
                        .add(
                            DataObject.empty()
                                .put("name", field!!.name)
                                .put("value", field.value)
                                .put("inline", field.isInline)
                        )
                }
                obj.put("fields", fieldsArray)
            }
            return obj.also { json = it }
        }
    }

    /**
     * Represents the information Discord provided about a thumbnail image that should be
     * displayed with an embed message.
     */
    class Thumbnail(url: String, proxyUrl: String, width: Int, height: Int) {
        protected val url: String
        protected val proxyUrl: String

        /**
         * The width of the thumbnail image.
         *
         * @return Never-negative, Never-zero int containing the width of the image.
         */
        val width: Int

        /**
         * The height of the thumbnail image.
         *
         * @return Never-negative, Never-zero int containing the height of the image.
         */
        val height: Int

        init {
            this.url = url
            this.proxyUrl = proxyUrl
            this.width = width
            this.height = height
        }

        /**
         * The web url of this thumbnail image.
         *
         * @return Possibly-null String containing the url of the displayed image.
         */
        fun getUrl(): String? {
            return url
        }

        /**
         * The Discord proxied url of the thumbnail image.
         * <br></br>This url is used to access the image through Discord instead of directly to prevent ip scraping.
         *
         * @return Possibly-null String containing the proxied url of this image.
         */
        fun getProxyUrl(): String? {
            return proxyUrl
        }

        val proxy: AttachmentProxy?
            /**
             * Returns an [AttachmentProxy] for this embed thumbnail.
             *
             * @return Possibly-null [AttachmentProxy] of this embed thumbnail
             *
             * @see .getProxyUrl
             */
            get() {
                val proxyUrl = getProxyUrl()
                return proxyUrl?.let { AttachmentProxy(it) }
            }

        override fun equals(obj: Any?): Boolean {
            if (obj !is Thumbnail) return false
            val thumbnail = obj
            return thumbnail === this || ((thumbnail.url == url)
                    && (thumbnail.proxyUrl == proxyUrl) && thumbnail.width == width) && thumbnail.height == height
        }
    }

    /**
     * Multipurpose class that represents a provider of content,
     * whether directly through creation or indirectly through hosting.
     */
    class Provider(name: String, url: String) {
        protected val name: String
        protected val url: String

        init {
            this.name = name
            this.url = url
        }

        /**
         * The name of the provider.
         * <br></br>If this is an author, most likely the author's username.
         * <br></br>If this is a website, most likely the site's name.
         *
         * @return Possibly-null String containing the name of the provider.
         */
        fun getName(): String? {
            return name
        }

        /**
         * The url of the provider.
         *
         * @return Possibly-null String containing the url of the provider.
         */
        fun getUrl(): String? {
            return url
        }

        override fun equals(obj: Any?): Boolean {
            if (obj !is Provider) return false
            val provider = obj
            return provider === this || ((provider.name == name)
                    && (provider.url == url))
        }
    }

    /**
     * Represents the information provided to embed a video.
     * <br></br>The videos represented are expected to be played using an HTML5 player from the
     * site which the url belongs to.
     */
    class VideoInfo(url: String, proxyUrl: String, width: Int, height: Int) {
        protected val url: String
        protected val proxyUrl: String

        /**
         * The width of the video.
         * <br></br>This usually isn't the actual video width, but instead the starting embed window size.
         *
         *
         * Basically: Don't rely on this to represent the actual video's quality or size.
         *
         * @return Non-negative, Non-zero int containing the width of the embedded video.
         */
        val width: Int

        /**
         * The height of the video.
         * <br></br>This usually isn't the actual video height, but instead the starting embed window size.
         *
         *
         * Basically: Don't rely on this to represent the actual video's quality or size.
         *
         * @return
         * Non-negative, Non-zero int containing the height of the embedded video.
         */
        val height: Int

        init {
            this.url = url
            this.proxyUrl = proxyUrl
            this.width = width
            this.height = height
        }

        /**
         * The url of the video.
         *
         * @return Possibly-null String containing the video url.
         */
        fun getUrl(): String? {
            return url
        }

        /**
         * The url of the video, proxied by Discord
         * <br></br>This url is used to access the video through Discord instead of directly to prevent ip scraping.
         *
         * @return Possibly-null String containing the proxied video url.
         */
        fun getProxyUrl(): String? {
            return proxyUrl
        }

        val proxy: FileProxy?
            /**
             * Returns a [FileProxy] for this embed video.
             *
             * @return Possibly-null [FileProxy] of this embed video
             *
             * @see .getProxyUrl
             */
            get() {
                val proxyUrl = getProxyUrl()
                return if (proxyUrl == null) null else FileProxy(proxyUrl)
            }

        override fun equals(obj: Any?): Boolean {
            if (obj !is VideoInfo) return false
            val video = obj
            return video === this || (video.url == url) && video.width == width && video.height == height
        }
    }

    /**
     * Represents the information provided to embed an image.
     */
    class ImageInfo(url: String, proxyUrl: String, width: Int, height: Int) {
        protected val url: String
        protected val proxyUrl: String

        /**
         * The width of the image.
         *
         * @return Non-negative, Non-zero int containing the width of the embedded image.
         */
        val width: Int

        /**
         * The height of the image.
         *
         * @return Non-negative, Non-zero int containing the height of the embedded image.
         */
        val height: Int

        init {
            this.url = url
            this.proxyUrl = proxyUrl
            this.width = width
            this.height = height
        }

        /**
         * The url of the image.
         *
         * @return Possibly-null String containing the image url.
         */
        fun getUrl(): String? {
            return url
        }

        /**
         * The url of the image, proxied by Discord
         * <br></br>This url is used to access the image through Discord instead of directly to prevent ip scraping.
         *
         * @return Possibly-null String containing the proxied image url.
         */
        fun getProxyUrl(): String? {
            return proxyUrl
        }

        val proxy: AttachmentProxy?
            /**
             * Returns an [AttachmentProxy] for this embed image.
             *
             * @return Possibly-null [AttachmentProxy] of this embed image
             *
             * @see .getProxyUrl
             */
            get() {
                val proxyUrl = getProxyUrl()
                return proxyUrl?.let { AttachmentProxy(it) }
            }

        override fun equals(obj: Any?): Boolean {
            if (obj !is ImageInfo) return false
            val image = obj
            return image === this || ((image.url == url)
                    && (image.proxyUrl == proxyUrl) && image.width == width) && image.height == height
        }
    }

    /**
     * Class that represents the author of content, possibly including an icon
     * that Discord proxies.
     */
    class AuthorInfo(name: String, url: String, iconUrl: String, proxyIconUrl: String?) {
        protected val name: String
        protected val url: String
        protected val iconUrl: String

        /**
         * The url of the author's icon, proxied by Discord
         * <br></br>This url is used to access the image through Discord instead of directly to prevent ip scraping.
         *
         * @return Possibly-null String containing the proxied icon url.
         */
        val proxyIconUrl: String?

        init {
            this.name = name
            this.url = url
            this.iconUrl = iconUrl
            this.proxyIconUrl = proxyIconUrl
        }

        /**
         * The name of the Author.
         * <br></br>This is most likely the name of the account associated with the embed
         *
         * @return Possibly-null String containing the name of the author.
         */
        fun getName(): String? {
            return name
        }

        /**
         * The url of the author.
         *
         * @return Possibly-null String containing the url of the author.
         */
        fun getUrl(): String? {
            return url
        }

        /**
         * The url of the author's icon.
         *
         * @return Possibly-null String containing the author's icon url.
         */
        fun getIconUrl(): String? {
            return iconUrl
        }

        val proxyIcon: ImageProxy?
            /**
             * Returns an [ImageProxy] for this proxied author's icon.
             *
             * @return Possibly-null [ImageProxy] of this proxied author's icon
             *
             * @see .getProxyIconUrl
             */
            get() = if (proxyIconUrl == null) null else ImageProxy(proxyIconUrl)

        override fun equals(obj: Any?): Boolean {
            if (obj !is AuthorInfo) return false
            val author = obj
            return author === this || ((author.name == name)
                    && (author.url == url)
                    && (author.iconUrl == iconUrl)
                    && (author.proxyIconUrl == proxyIconUrl))
        }
    }

    /**
     * Class that represents a footer at the bottom of an embed
     */
    class Footer(text: String, iconUrl: String, proxyIconUrl: String?) {
        protected val text: String
        protected val iconUrl: String

        /**
         * The url of the footer's icon, proxied by Discord
         * <br></br>This url is used to access the image through Discord instead of directly to prevent ip scraping.
         *
         * @return Possibly-null String containing the proxied icon url.
         */
        val proxyIconUrl: String?

        init {
            this.text = text
            this.iconUrl = iconUrl
            this.proxyIconUrl = proxyIconUrl
        }

        /**
         * The text in the footer
         *
         * @return Possibly-null String containing the text in the footer.
         */
        fun getText(): String? {
            return text
        }

        /**
         * The url of the footer's icon.
         *
         * @return Possibly-null String containing the footer's icon url.
         */
        fun getIconUrl(): String? {
            return iconUrl
        }

        val proxyIcon: ImageProxy?
            /**
             * Returns an [ImageProxy] for this proxied footer's icon.
             *
             * @return Possibly-null [ImageProxy] of this proxied footer's icon
             *
             * @see .getProxyIconUrl
             */
            get() = if (proxyIconUrl == null) null else ImageProxy(proxyIconUrl)

        override fun equals(obj: Any?): Boolean {
            if (obj !is Footer) return false
            val footer = obj
            return footer === this || ((footer.text == text)
                    && (footer.iconUrl == iconUrl)
                    && (footer.proxyIconUrl == proxyIconUrl))
        }
    }

    /**
     * Represents a field in an embed. A single embed contains an array of
     * embed fields, each with a name and value, and a boolean determining if
     * the field can display on the same line as previous fields if there is
     * enough space horizontally.
     *
     * @since  3.0
     * @author John A. Grosh
     */
    class Field @JvmOverloads constructor(name: String?, value: String?, inline: Boolean, checked: Boolean = true) {
        /**
         * The name of the field
         *
         * @return Possibly-null String containing the name of the field.
         */
        @JvmField
        val name: String? = null

        /**
         * The value of the field
         *
         * @return Possibly-null String containing the value (contents) of the field.
         */
        @JvmField
        val value: String? = null

        /**
         * If the field is in line.
         *
         * @return true if the field can be in line with other fields, false otherwise.
         */
        val isInline: Boolean

        init {
            var name = name
            var value = value
            if (checked) {
                if (name == null || value == null) throw IllegalArgumentException("Both Name and Value must be set!") else if (name.length > TITLE_MAX_LENGTH) throw IllegalArgumentException(
                    "Name cannot be longer than " + TITLE_MAX_LENGTH + " characters."
                ) else require(!(value.length > VALUE_MAX_LENGTH)) { "Value cannot be longer than " + VALUE_MAX_LENGTH + " characters." }
                name = name.trim { it <= ' ' }
                value = value.trim { it <= ' ' }
                if (name.isEmpty()) this.name = EmbedBuilder.ZERO_WIDTH_SPACE else this.name = name
                if (value.isEmpty()) this.value = EmbedBuilder.ZERO_WIDTH_SPACE else this.value = value
            } else {
                this.name = name
                this.value = value
            }
            isInline = inline
        }

        override fun equals(obj: Any?): Boolean {
            if (obj !is Field) return false
            val field = obj
            return field === this || (field.isInline == isInline && (field.name == name)
                    && (field.value == value))
        }
    }

    companion object {
        /**
         * The maximum length an embed title can have
         *
         * @see net.dv8tion.jda.api.EmbedBuilder.setTitle
         * @see net.dv8tion.jda.api.EmbedBuilder.addField
         */
        const val TITLE_MAX_LENGTH = 256

        /**
         * The maximum length the author name of an embed can have
         *
         * @see net.dv8tion.jda.api.EmbedBuilder.setAuthor
         * @see net.dv8tion.jda.api.EmbedBuilder.setAuthor
         * @see net.dv8tion.jda.api.EmbedBuilder.setAuthor
         */
        const val AUTHOR_MAX_LENGTH = 256

        /**
         * The maximum length an embed field value can have
         *
         * @see net.dv8tion.jda.api.EmbedBuilder.addField
         */
        const val VALUE_MAX_LENGTH = 1024

        /**
         * The maximum length the description of an embed can have
         *
         * @see net.dv8tion.jda.api.EmbedBuilder.setDescription
         */
        const val DESCRIPTION_MAX_LENGTH = 4096

        /**
         * The maximum length the footer of an embed can have
         *
         * @see net.dv8tion.jda.api.EmbedBuilder.setFooter
         */
        const val TEXT_MAX_LENGTH = 2048

        /**
         * The maximum length any URL can have inside an embed
         *
         * @see net.dv8tion.jda.api.EmbedBuilder.setTitle
         * @see net.dv8tion.jda.api.EmbedBuilder.setAuthor
         * @see net.dv8tion.jda.api.EmbedBuilder.setFooter
         */
        const val URL_MAX_LENGTH = 2000

        /**
         * The maximum amount of total visible characters an embed can have
         *
         * @see net.dv8tion.jda.api.EmbedBuilder.setDescription
         * @see net.dv8tion.jda.api.EmbedBuilder.setTitle
         * @see net.dv8tion.jda.api.EmbedBuilder.setFooter
         * @see net.dv8tion.jda.api.EmbedBuilder.addField
         */
        const val EMBED_MAX_LENGTH_BOT = 6000

        /**
         * The maximum amount of total visible characters an embed can have
         *
         * @see net.dv8tion.jda.api.EmbedBuilder.setDescription
         * @see net.dv8tion.jda.api.EmbedBuilder.setTitle
         * @see net.dv8tion.jda.api.EmbedBuilder.setFooter
         * @see net.dv8tion.jda.api.EmbedBuilder.addField
         */
        @ForRemoval
        @Deprecated("This will be removed in the future.")
        val EMBED_MAX_LENGTH_CLIENT = 2000

        /**
         * The maximum amount of total embed fields the embed can hold
         *
         * @see net.dv8tion.jda.api.EmbedBuilder.addField
         */
        const val MAX_FIELD_AMOUNT = 25
    }
}
