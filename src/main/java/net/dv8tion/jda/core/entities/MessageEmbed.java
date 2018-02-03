/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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
package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.Helpers;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents an embed displayed by Discord.
 * <br>A visual representation of an Embed can be found at:
 * <a href="http://imgur.com/a/yOb5n" target="_blank">http://imgur.com/a/yOb5n</a>
 * <br>This class has many possibilities for null values, so be careful!
 */
public class MessageEmbed
{
    /**
     * The maximum length an embed title can have
     *
     * @see net.dv8tion.jda.core.EmbedBuilder#setTitle(String) EmbedBuilder.setTitle(title)
     * @see net.dv8tion.jda.core.EmbedBuilder#addField(String, String, boolean) EmbedBuilder.addField(title, value, inline)
     */
    public static final int TITLE_MAX_LENGTH = 256;

    /**
     * The maximum length an embed field value can have
     *
     * @see net.dv8tion.jda.core.EmbedBuilder#addField(String, String, boolean) EmbedBuilder.addField(title, value, inline)
     */
    public static final int VALUE_MAX_LENGTH = 1024;

    /**
     * The maximum length the description and footer of an embed can have
     *
     * @see net.dv8tion.jda.core.EmbedBuilder#setFooter(String, String) EmbedBuilder.setFooter(text, iconUrl)
     * @see net.dv8tion.jda.core.EmbedBuilder#setDescription(CharSequence) EmbedBuilder.setDescription(text)
     */
    public static final int TEXT_MAX_LENGTH = 2048;

    /**
     * The maximum length any URL can have inside an embed
     *
     * @see net.dv8tion.jda.core.EmbedBuilder#setTitle(String, String) EmbedBuilder.setTitle(text, url)
     * @see net.dv8tion.jda.core.EmbedBuilder#setAuthor(String, String, String) EmbedBuilder.setAuthor(text, url, iconUrl)
     * @see net.dv8tion.jda.core.EmbedBuilder#setFooter(String, String) EmbedBuilder.setFooter(text, url)
     */
    public static final int URL_MAX_LENGTH = 2000;

    /**
     * The maximum amount of total visible characters an embed can have
     * <br>This limit depends on the current {@link net.dv8tion.jda.core.AccountType AccountType} and applies to BOT
     *
     * @see net.dv8tion.jda.core.EmbedBuilder#setDescription(CharSequence)
     * @see net.dv8tion.jda.core.EmbedBuilder#setTitle(String)
     * @see net.dv8tion.jda.core.EmbedBuilder#setFooter(String, String)
     * @see net.dv8tion.jda.core.EmbedBuilder#addField(String, String, boolean)
     */
    public static final int EMBED_MAX_LENGTH_BOT = 6000;

    /**
     * The maximum amount of total visible characters an embed can have
     * <br>This limit depends on the current {@link net.dv8tion.jda.core.AccountType AccountType} and applies to CLIENT
     *
     * @see net.dv8tion.jda.core.EmbedBuilder#setDescription(CharSequence)
     * @see net.dv8tion.jda.core.EmbedBuilder#setTitle(String)
     * @see net.dv8tion.jda.core.EmbedBuilder#setFooter(String, String)
     * @see net.dv8tion.jda.core.EmbedBuilder#addField(String, String, boolean)
     */
    public static final int EMBED_MAX_LENGTH_CLIENT = 2000;

    protected final Object mutex = new Object();

    protected final String url;
    protected final String title;
    protected final String description;
    protected final EmbedType type;
    protected final OffsetDateTime timestamp;
    protected final int color;
    protected final Thumbnail thumbnail;
    protected final Provider siteProvider;
    protected final AuthorInfo author;
    protected final VideoInfo videoInfo;
    protected final Footer footer;
    protected final ImageInfo image;
    protected final List<Field> fields;

    protected volatile int length = -1;
    protected volatile JSONObject json = null;

    protected MessageEmbed(
        String url, String title, String description, EmbedType type, OffsetDateTime timestamp,
        int color, Thumbnail thumbnail, Provider siteProvider, AuthorInfo author,
        VideoInfo videoInfo, Footer footer, ImageInfo image, List<Field> fields)
    {
        this.url = url;
        this.title = title;
        this.description = description;
        this.type = type;
        this.timestamp = timestamp;
        this.color = color;
        this.thumbnail = thumbnail;
        this.siteProvider = siteProvider;
        this.author = author;
        this.videoInfo = videoInfo;
        this.footer = footer;
        this.image = image;
        this.fields = fields != null && !fields.isEmpty()
            ? Collections.unmodifiableList(fields) : Collections.emptyList();
    }

    /**
     * The that was originally placed into chat that spawned this embed.
     *
     * @return Never-null String containing the original message url.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * The title of the embed. Typically this will be the html title of the webpage that is being embedded.<br>
     * If no title could be found, like the case of {@link EmbedType EmbedType} = {@link net.dv8tion.jda.core.entities.EmbedType#IMAGE IMAGE},
     * this method will return null.
     *
     * @return Possibly-null String containing the title of the embedded resource.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * The description of the embedded resource.
     * <br>This is provided only if Discord could find a description for the embedded resource using the provided url.
     * <br>Commonly, this is null. Be careful when using it.
     *
     * @return Possibly-null String containing a description of the embedded resource.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.EmbedType EmbedType} of this embed.
     *
     * @return The {@link net.dv8tion.jda.core.entities.EmbedType EmbedType} of this embed.
     */
    public EmbedType getType()
    {
        return type;
    }

    /**
     * The information about the {@link net.dv8tion.jda.core.entities.MessageEmbed.Thumbnail Thumbnail} image to be displayed with the embed.
     * <br>If a {@link net.dv8tion.jda.core.entities.MessageEmbed.Thumbnail Thumbnail} was not part of this embed, this returns null.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.MessageEmbed.Thumbnail Thumbnail} instance
     *         containing general information on the displayable thumbnail.
     */
    public Thumbnail getThumbnail()
    {
        return thumbnail;
    }

    /**
     * The information on site from which the embed was generated from.
     * <br>If Discord did not generate any deliverable information about the site, this returns null.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.MessageEmbed.Provider Provider}
     *         containing site information.
     */
    public Provider getSiteProvider()
    {
        return siteProvider;
    }

    /**
     * The information on the creator of the embedded content.
     * <br>This is typically used to represent the account on the providing site.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.MessageEmbed.AuthorInfo AuthorInfo}
     *         containing author information.
     */
    public AuthorInfo getAuthor()
    {
        return author;
    }

    /**
     * The information about the video which should be displayed as an embed.
     * <br>This is used when sites with HTML5 players are linked and embedded. Most commonly Youtube.
     * <br>If this {@link net.dv8tion.jda.core.entities.EmbedType EmbedType} != {@link net.dv8tion.jda.core.entities.EmbedType#VIDEO VIDEO}
     * this will always return null.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.MessageEmbed.VideoInfo VideoInfo}
     *         containing the information about the video which should be embedded.
     */
    public VideoInfo getVideoInfo()
    {
        return videoInfo;
    }
    
    /**
     * The footer (bottom) of the embedded content.
     * <br>This is typically used for timestamps or site icons.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.MessageEmbed.Footer Footer}
     *         containing the embed footer content.
     */
    public Footer getFooter()
    {
        return footer;
    }
    
    /**
     * The information about the image in the message embed
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.MessageEmbed.ImageInfo ImageInfo}
     *         containing image information.
     */
    public ImageInfo getImage()
    {
        return image;
    }
    
    /**
     * The fields in a message embed.
     * <br>Message embeds can contain multiple fields, each with a name, value, and a boolean
     * to determine if it will fall in-line with other fields. If the embed contains no
     * fields, an empty list will be returned.
     *
     * @return Never-null (but possibly empty) List of {@link net.dv8tion.jda.core.entities.MessageEmbed.Field Field} objects
     *         containing field information.
     */
    public List<Field> getFields()
    {
        return fields;
    }
    
    /**
     * The color of the stripe on the side of the embed.
     * <br>If the color is 0 (no color), this will return null.
     *
     * @return Possibly-null Color.
     */
    public Color getColor()
    {
        return color != Role.DEFAULT_COLOR_RAW ? new Color(color) : null;
    }

    /**
     * The raw RGB color value for this embed
     * <br>Defaults to {@link Role#DEFAULT_COLOR_RAW} if no color is set
     *
     * @return The raw RGB color value or default
     */
    public int getColorRaw()
    {
        return color;
    }
    
    /**
     * The timestamp of the embed.
     *
     * @return Possibly-null OffsetDateTime object representing the timestamp.
     */
    public OffsetDateTime getTimestamp()
    {
        return timestamp;
    }

    /**
     * Whether this embed is empty.
     *
     * @return True, if this embed has no content
     */
    public boolean isEmpty()
    {
        return getLength() == 0
            && getImage() == null
            && getThumbnail() == null;
    }

    /**
     * The total amount of characters that is displayed when this embed is displayed by the Discord client.
     *
     * <p>An Embed can only have, at max, {@value #EMBED_MAX_LENGTH_BOT} displayable text characters for {@link net.dv8tion.jda.core.AccountType#BOT AccountType.BOT}
     * accounts or {@value #EMBED_MAX_LENGTH_CLIENT} displayable text characters for {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType.CLIENT} accounts.
     *
     * <p>Both of these values are defined by {@link #EMBED_MAX_LENGTH_BOT EMBED_MAX_LENGTH_BOT} and
     * {@link #EMBED_MAX_LENGTH_CLIENT EMBED_MAX_LENGTH_CLIENT} respectively.
     *
     * @return A never-negative sum of all displayed text characters.
     */
    public int getLength()
    {
        if (length > -1)
            return length;
        synchronized (mutex)
        {
            if (length > -1)
                return length;
            length = 0;

            if (title != null)
                length += title.length();
            if (description != null)
                length += description.length();
            if (author != null)
                length += author.getName().length();
            if (footer != null)
                length += footer.getText().length();
            if (fields != null)
            {
                for (Field f : fields)
                    length += f.getName().length() + f.getValue().length();
            }

            return length;
        }
    }

    /**
     * Whether this MessageEmbed can be used in a message.
     * <br>This applies to {@link net.dv8tion.jda.core.AccountType#BOT Bot}- and {@link net.dv8tion.jda.core.AccountType#CLIENT Client Accounts}
     *
     * <p>Total Character Limits
     * <ul>
     *     <li>Bot: {@value #EMBED_MAX_LENGTH_BOT}</li>
     *     <li>Client: {@value #EMBED_MAX_LENGTH_CLIENT}</li>
     * </ul>
     *
     * @param  type
     *         The {@link net.dv8tion.jda.core.AccountType AccountType} to inspect
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided AccountType is {@code null} or not supported by this operation
     *
     * @return True, if this MessageEmbed can be used to send messages for this specified AccountType
     *
     * @see    #getLength()
     */
    public boolean isSendable(AccountType type)
    {
        Checks.notNull(type, "AccountType");
        final int length = getLength();
        if (isEmpty())
            return false;

        switch (type)
        {
            case BOT: return length <= EMBED_MAX_LENGTH_BOT;
            case CLIENT: return length <= EMBED_MAX_LENGTH_CLIENT;
            default: throw new IllegalArgumentException(String.format("Cannot check against AccountType '%s'!", type));
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof MessageEmbed))
            return false;
        if (obj == this)
            return true;
        MessageEmbed other = (MessageEmbed) obj;
        return Objects.equals(url, other.url)
            && Objects.equals(title, other.title)
            && Objects.equals(description, other.description)
            && Objects.equals(type, other.type)
            && Objects.equals(thumbnail, other.thumbnail)
            && Objects.equals(siteProvider, other.siteProvider)
            && Objects.equals(author, other.author)
            && Objects.equals(videoInfo, other.videoInfo)
            && Objects.equals(footer, other.footer)
            && Objects.equals(image, other.image)
            && Objects.equals(color, other.color)
            && Objects.equals(timestamp, other.timestamp)
            && Helpers.deepEquals(fields, other.fields);
    }

    /**
     * Creates a new {@link org.json.JSONObject JSONObject}
     * used for sending.
     *
     * @return JSONObject for this embed
     */
    public JSONObject toJSONObject()
    {
        if (json != null)
            return json;
        synchronized (mutex)
        {
            if (json != null)
                return json;
            JSONObject obj = new JSONObject();
            if (url != null)
                obj.put("url", url);
            if (title != null)
                obj.put("title", title);
            if (description != null)
                obj.put("description", description);
            if (timestamp != null)
                obj.put("timestamp", timestamp.format(DateTimeFormatter.ISO_INSTANT));
            if (color != Role.DEFAULT_COLOR_RAW)
                obj.put("color", color & 0xFFFFFF);
            if (thumbnail != null)
                obj.put("thumbnail", new JSONObject().put("url", thumbnail.getUrl()));
            if (siteProvider != null)
            {
                JSONObject siteProviderObj = new JSONObject();
                if (siteProvider.getName() != null)
                    siteProviderObj.put("name", siteProvider.getName());
                if (siteProvider.getUrl() != null)
                    siteProviderObj.put("url", siteProvider.getUrl());
                obj.put("provider", siteProviderObj);
            }
            if (author != null)
            {
                JSONObject authorObj = new JSONObject();
                if (author.getName() != null)
                    authorObj.put("name", author.getName());
                if (author.getUrl() != null)
                    authorObj.put("url", author.getUrl());
                if (author.getIconUrl() != null)
                    authorObj.put("icon_url", author.getIconUrl());
                obj.put("author", authorObj);
            }
            if (videoInfo != null)
                obj.put("video", new JSONObject().put("url", videoInfo.getUrl()));
            if (footer != null)
            {
                JSONObject footerObj = new JSONObject();
                if (footer.getText() != null)
                    footerObj.put("text", footer.getText());
                if (footer.getIconUrl() != null)
                    footerObj.put("icon_url", footer.getIconUrl());
                obj.put("footer", footerObj);
            }
            if (image != null)
                obj.put("image", new JSONObject().put("url", image.getUrl()));
            if (!fields.isEmpty())
            {
                JSONArray fieldsArray = new JSONArray();
                for (Field field : fields)
                {
                    fieldsArray
                        .put(new JSONObject()
                            .put("name", field.getName())
                            .put("value", field.getValue())
                            .put("inline", field.isInline()));
                }
                obj.put("fields", fieldsArray);
            }
            return json = obj;
        }
    }

    /**
     * Represents the information Discord provided about a thumbnail image that should be
     * displayed with an embed message.
     */
    public static class Thumbnail
    {
        protected final String url;
        protected final String proxyUrl;
        protected final int width;
        protected final int height;

        public Thumbnail(String url, String proxyUrl, int width, int height)
        {
            this.url = url;
            this.proxyUrl = proxyUrl;
            this.width = width;
            this.height = height;
        }

        /**
         * The web url of this thumbnail image.
         *
         * @return Never-null String containing the url of the displayed image.
         */
        public String getUrl()
        {
            return url;
        }

        /**
         * The Discord proxied url of the thumbnail image.
         * <br>This url is used to access the image through Discord instead of directly to prevent ip scraping.
         *
         * @return Never-null String containing the proxied url of this image.
         */
        public String getProxyUrl()
        {
            return proxyUrl;
        }

        /**
         * The width of the thumbnail image.
         *
         * @return Never-negative, Never-zero int containing the width of the image.
         */
        public int getWidth()
        {
            return width;
        }

        /**
         * The height of the thumbnail image.
         *
         * @return Never-negative, Never-zero int containing the height of the image.
         */
        public int getHeight()
        {
            return height;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof Thumbnail))
                return false;
            Thumbnail thumbnail = (Thumbnail) obj;
            return thumbnail == this || (Objects.equals(thumbnail.url, url)
                && Objects.equals(thumbnail.proxyUrl, proxyUrl)
                && thumbnail.width == width
                && thumbnail.height == height);
        }
    }

    /**
     * Multipurpose class that represents a provider of content,
     * whether directly through creation or indirectly through hosting.
     */
    public static class Provider
    {
        protected final String name;
        protected final String url;

        public Provider(String name, String url)
        {
            this.name = name;
            this.url = url;
        }

        /**
         * The name of the provider.
         * <br>If this is an author, most likely the author's username.
         * <br>If this is a website, most likely the site's name.
         *
         * @return Never-null String containing the name of the provider.
         */
        public String getName()
        {
            return name;
        }

        /**
         * The url of the provider.
         *
         * @return Possibly-null String containing the url of the provider.
         */
        public String getUrl()
        {
            return url;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof Provider))
                return false;
            Provider provider = (Provider) obj;
            return provider == this || (Objects.equals(provider.name, name)
                && Objects.equals(provider.url, url));
        }
    }

    /**
     * Represents the information provided to embed a video.
     * <br>The videos represented are expected to be played using an HTML5 player from the
     * site which the url belongs to.
     */
    public static class VideoInfo
    {
        protected final String url;
        protected final int width;
        protected final int height;

        public VideoInfo(String url, int width, int height)
        {
            this.url = url;
            this.width = width;
            this.height = height;
        }

        /**
         * The url of the video.
         *
         * @return Never-null String containing the video url.
         */
        public String getUrl()
        {
            return url;
        }

        /**
         * The width of the video.
         * <br>This usually isn't the actual video width, but instead the starting embed window size.
         *
         * <p>Basically: Don't rely on this to represent the actual video's quality or size.
         *
         * @return Non-negative, Non-zero int containing the width of the embedded video.
         */
        public int getWidth()
        {
            return width;
        }

        /**
         * The height of the video.
         * <br>This usually isn't the actual video height, but instead the starting embed window size.
         *
         * <p>Basically: Don't rely on this to represent the actual video's quality or size.
         *
         * @return
         *      Non-negative, Non-zero int containing the height of the embedded video.
         */
        public int getHeight()
        {
            return height;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof VideoInfo))
                return false;
            VideoInfo video = (VideoInfo) obj;
            return video == this || (Objects.equals(video.url, url)
                && video.width == width
                && video.height == height);
        }
    }
    
    /**
     * Represents the information provided to embed an image.
     */
    public static class ImageInfo
    {
        protected final String url;
        protected final String proxyUrl;
        protected final int width;
        protected final int height;

        public ImageInfo(String url, String proxyUrl, int width, int height)
        {
            this.url = url;
            this.proxyUrl = proxyUrl;
            this.width = width;
            this.height = height;
        }

        /**
         * The url of the image.
         *
         * @return Never-null String containing the image url.
         */
        public String getUrl()
        {
            return url;
        }
        
        /**
         * The url of the image, proxied by Discord
         * <br>This url is used to access the image through Discord instead of directly to prevent ip scraping.
         *
         * @return Never-null String containing the proxied image url.
         */
        public String getProxyUrl()
        {
            return proxyUrl;
        }

        /**
         * The width of the image.
         *
         * @return Non-negative, Non-zero int containing the width of the embedded image.
         */
        public int getWidth()
        {
            return width;
        }

        /**
         * The height of the image.
         *
         * @return Non-negative, Non-zero int containing the height of the embedded image.
         */
        public int getHeight()
        {
            return height;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof ImageInfo))
                return false;
            ImageInfo image = (ImageInfo) obj;
            return image == this || (Objects.equals(image.url, url)
                && Objects.equals(image.proxyUrl, proxyUrl)
                && image.width == width
                && image.height == height);
        }
    }
    
    /**
     * Class that represents the author of content, possibly including an icon
     * that Discord proxies.
     */
    public static class AuthorInfo
    {
        protected final String name;
        protected final String url;
        protected final String iconUrl;
        protected final String proxyIconUrl;

        public AuthorInfo(String name, String url, String iconUrl, String proxyIconUrl)
        {
            this.name = name;
            this.url = url;
            this.iconUrl = iconUrl;
            this.proxyIconUrl = proxyIconUrl;
        }

        /**
         * The name of the Author.
         * <br>This is most likely the name of the account associated with the embed
         *
         * @return Possibly-null String containing the name of the author.
         */
        public String getName()
        {
            return name;
        }

        /**
         * The url of the author.
         *
         * @return Possibly-null String containing the url of the author.
         */
        public String getUrl()
        {
            return url;
        }
        
        /**
         * The url of the author's icon.
         *
         * @return Possibly-null String containing the author's icon url.
         */
        public String getIconUrl()
        {
            return iconUrl;
        }
        
        /**
         * The url of the author's icon, proxied by Discord
         * <br>This url is used to access the image through Discord instead of directly to prevent ip scraping.
         *
         * @return Possibly-null String containing the proxied icon url.
         */
        public String getProxyIconUrl()
        {
            return proxyIconUrl;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof AuthorInfo))
                return false;
            AuthorInfo author = (AuthorInfo) obj;
            return author == this || (Objects.equals(author.name, name)
                && Objects.equals(author.url, url)
                && Objects.equals(author.iconUrl, iconUrl)
                && Objects.equals(author.proxyIconUrl, proxyIconUrl));
        }
    }
    
    /**
     * Class that represents a footer at the bottom of an embed
     */
    public static class Footer
    {
        protected final String text;
        protected final String iconUrl;
        protected final String proxyIconUrl;

        public Footer(String text, String iconUrl, String proxyIconUrl)
        {
            this.text = text;
            this.iconUrl = iconUrl;
            this.proxyIconUrl = proxyIconUrl;
        }

        /**
         * The text in the footer
         *
         * @return Possibly-null String containing the text in the footer.
         */
        public String getText()
        {
            return text;
        }
        
        /**
         * The url of the footer's icon.
         *
         * @return Possibly-null String containing the footer's icon url.
         */
        public String getIconUrl()
        {
            return iconUrl;
        }
        
        /**
         * The url of the footer's icon, proxied by Discord
         * <br>This url is used to access the image through Discord instead of directly to prevent ip scraping.
         *
         * @return Possibly-null String containing the proxied icon url.
         */
        public String getProxyIconUrl()
        {
            return proxyIconUrl;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof Footer))
                return false;
            Footer footer = (Footer) obj;
            return footer == this || (Objects.equals(footer.text, text)
                && Objects.equals(footer.iconUrl, iconUrl)
                && Objects.equals(footer.proxyIconUrl, proxyIconUrl));
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
    public static class Field
    {
        protected final String name;
        protected final String value;
        protected final boolean inline;

        protected Field(String name, String value, boolean inline, boolean checked)
        {
            if (checked)
            {
                if (name == null || value == null)
                    throw new IllegalArgumentException("Both Name and Value must be set!");
                else if (name.length() > TITLE_MAX_LENGTH)
                    throw new IllegalArgumentException("Name cannot be longer than " + TITLE_MAX_LENGTH + " characters.");
                else if (value.length() > VALUE_MAX_LENGTH)
                    throw new IllegalArgumentException("Value cannot be longer than " + VALUE_MAX_LENGTH + " characters.");
                if (name.isEmpty())
                    this.name = EmbedBuilder.ZERO_WIDTH_SPACE;
                else
                    this.name = name;
                if (value.isEmpty())
                    this.value = EmbedBuilder.ZERO_WIDTH_SPACE;
                else
                    this.value = value;
                this.inline = inline;
            }
            else
            {
                this.name = name;
                this.value = value;
                this.inline = inline;
            }
        }
        
        public Field(String name, String value, boolean inline)
        {
            this(name, value, inline, true);
        }

        /**
         * The name of the field
         *
         * @return Possibly-null String containing the name of the field.
         */
        public String getName()
        {
            return name;
        }

        /**
         * The value of the field
         *
         * @return Possibly-null String containing the value (contents) of the field.
         */
        public String getValue()
        {
            return value;
        }
        
        /**
         * If the field is in line.
         *
         * @return true if the field can be in line with other fields, false otherwise.
         */
        public boolean isInline()
        {
            return inline;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof Field))
                return false;
            final Field field = (Field) obj;
            return field == this || (field.inline == inline
                && Objects.equals(field.name, name)
                && Objects.equals(field.value, value));
        }
    }
}
