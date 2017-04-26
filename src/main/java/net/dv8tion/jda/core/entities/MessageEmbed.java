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
package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.AccountType;
import org.apache.http.util.Args;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Represents an embed displayed by Discord.
 * <br>A visual representation of an Embed can be found at:
 * <a href="http://imgur.com/a/yOb5n" target="_blank">http://imgur.com/a/yOb5n</a>
 * <br>This class has many possibilities for null values, so be careful!
 */
public interface MessageEmbed
{
    int TITLE_MAX_LENGTH = 256;
    int VALUE_MAX_LENGTH = 1024;
    int TEXT_MAX_LENGTH = 2048;
    int URL_MAX_LENGTH = 2000;
    int EMBED_MAX_LENGTH_BOT = 4000;
    int EMBED_MAX_LENGTH_CLIENT = 2000;

    /**
     * The that was originally placed into chat that spawned this embed.
     *
     * @return Never-null String containing the original message url.
     */
    String getUrl();

    /**
     * The title of the embed. Typically this will be the html title of the webpage that is being embedded.<br>
     * If no title could be found, like the case of {@link EmbedType EmbedType} = {@link net.dv8tion.jda.core.entities.EmbedType#IMAGE IMAGE},
     * this method will return null.
     *
     * @return Possibly-null String containing the title of the embedded resource.
     */
    String getTitle();

    /**
     * The description of the embedded resource.
     * <br>This is provided only if Discord could find a description for the embedded resource using the provided url.
     * <br>Commonly, this is null. Be careful when using it.
     *
     * @return Possibly-null String containing a description of the embedded resource.
     */
    String getDescription();

    /**
     * The {@link net.dv8tion.jda.core.entities.EmbedType EmbedType} of this embed.
     *
     * @return The {@link net.dv8tion.jda.core.entities.EmbedType EmbedType} of this embed.
     */
    EmbedType getType();

    /**
     * The information about the {@link net.dv8tion.jda.core.entities.MessageEmbed.Thumbnail Thumbnail} image to be displayed with the embed.
     * <br>If a {@link net.dv8tion.jda.core.entities.MessageEmbed.Thumbnail Thumbnail} was not part of this embed, this returns null.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.MessageEmbed.Thumbnail Thumbnail} instance
     *         containing general information on the displayable thumbnail.
     */
    Thumbnail getThumbnail();

    /**
     * The information on site from which the embed was generated from.
     * <br>If Discord did not generate any deliverable information about the site, this returns null.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.MessageEmbed.Provider Provider}
     *         containing site information.
     */
    Provider getSiteProvider();

    /**
     * The information on the creator of the embedded content.
     * <br>This is typically used to represent the account on the providing site.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.MessageEmbed.AuthorInfo AuthorInfo}
     *         containing author information.
     */
    AuthorInfo getAuthor();

    /**
     * The information about the video which should be displayed as an embed.
     * <br>This is used when sites with HTML5 players are linked and embedded. Most commonly Youtube.
     * <br>If this {@link net.dv8tion.jda.core.entities.EmbedType EmbedType} != {@link net.dv8tion.jda.core.entities.EmbedType#VIDEO VIDEO}
     * this will always return null.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.MessageEmbed.VideoInfo VideoInfo}
     *         containing the information about the video which should be embedded.
     */
    VideoInfo getVideoInfo();
    
    /**
     * The footer (bottom) of the embedded content.
     * <br>This is typically used for timestamps or site icons.
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.MessageEmbed.Footer Footer}
     *         containing the embed footer content.
     */
    Footer getFooter();
    
    /**
     * The information about the image in the message embed
     *
     * @return Possibly-null {@link net.dv8tion.jda.core.entities.MessageEmbed.ImageInfo ImageInfo}
     *         containing image information.
     */
    ImageInfo getImage();
    
    /**
     * The fields in a message embed.
     * <br>Message embeds can contain multiple fields, each with a name, value, and a boolean
     * to determine if it will fall in-line with other fields. If the embed contains no
     * fields, an empty list will be returned.
     *
     * @return Never-null (but possibly empty) List of {@link net.dv8tion.jda.core.entities.MessageEmbed.Field Field} objects
     *         containing field information.
     */
    List<Field> getFields();
    
    /**
     * The color of the stripe on the side of the embed.
     * <br>If the color is 0 (no color), this will return null.
     *
     * @return Possibly-null Color.
     */
    Color getColor();
    
    /**
     * The timestamp of the embed.
     *
     * @return Possibly-null OffsetDateTime object representing the timestamp.
     */
    OffsetDateTime getTimestamp();

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
    int getLength();

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
    default boolean isSendable(AccountType type)
    {
        Args.notNull(type, "AccountType");
        final int length = getLength();

        switch (type)
        {
            case BOT: return length <= EMBED_MAX_LENGTH_BOT;
            case CLIENT: return length <= EMBED_MAX_LENGTH_CLIENT;
            default: throw new IllegalArgumentException(String.format("Cannot check against AccountType '%s'!", type));
        }
    }

    /**
     * Represents the information Discord provided about a thumbnail image that should be
     * displayed with an embed message.
     */
    class Thumbnail
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
    }

    /**
     * Multipurpose class that represents a provider of content,
     * whether directly through creation or indirectly through hosting.
     */
    class Provider
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
    }

    /**
     * Represents the information provided to embed a video.
     * <br>The videos represented are expected to be played using an HTML5 player from the
     * site which the url belongs to.
     */
    class VideoInfo
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
    }
    
    /**
     * Represents the information provided to embed an image.
     */
    class ImageInfo
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
    }
    
    /**
     * Class that represents the author of content, possibly including an icon
     * that Discord proxies.
     */
    class AuthorInfo
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
    }
    
    /**
     * Class that represents a footer at the bottom of an embed
     */
    class Footer
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
    class Field
    {
        protected final String name;
        protected final String value;
        protected final boolean inline;

        public Field(String name, String value, boolean inline)
        {
            this.name = name;
            this.value = value;
            this.inline = inline;
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
    }
}
