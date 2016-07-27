/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.entities;

import net.dv8tion.jda.EmbedType;

/**
 * Represents an embed displayed by Discord.<br>
 * This class has many possibilities for null values, so be careful!
 */
public interface MessageEmbed
{
    /**
     * The that was originally placed into chat that spawned this embed.
     *
     * @return
     *      Never-null String containing the original message url.
     */
    String getUrl();

    /**
     * The title of the embed. Typically this will be the html title of the webpage that is being embedded.<br>
     * If no title could be found, like the case of {@link net.dv8tion.jda.EmbedType EmbedType} = {@link net.dv8tion.jda.EmbedType#IMAGE IMAGE},
     * this method will return null.
     *
     * @return
     *      Possibly-null String containing the title of the embedded resource.
     */
    String getTitle();

    /**
     * The description of the embedded resource.<br>
     * This is provided only if Discord could find a description for the embedded resource using the provided url.<br>
     * Commonly, this is null. Be careful when using it.
     *
     * @return
     *      Possibly-null String containing a description of the embedded resource.
     */
    String getDescription();

    /**
     * The {@link net.dv8tion.jda.EmbedType EmbedType} of this embed.
     *W
     * @return
     *      The {@link net.dv8tion.jda.EmbedType EmbedType} of this embed.
     */
    EmbedType getType();

    /**
     * The information about the {@link net.dv8tion.jda.entities.MessageEmbed.Thumbnail Thumbnail} image to be displayed with the embed.<br>
     * If a {@link net.dv8tion.jda.entities.MessageEmbed.Thumbnail Thumbnail} was not part of this embed, this returns null.
     *
     * @return
     *      Possibly-null {@link net.dv8tion.jda.entities.MessageEmbed.Thumbnail Thumbnail} instance containing general information on the displayable thumbnail.
     */
    Thumbnail getThumbnail();

    /**
     * The information on site from which the embed was generated from.<br>
     * If Discord did not generate any deliverable information about the site, this returns null.
     *
     * @return
     *      Possibly-null {@link net.dv8tion.jda.entities.MessageEmbed.Provider Provider} containing site information.
     */
    Provider getSiteProvider();

    /**
     * The information on the creator of the embedded content.<br>
     * This is typically used for Youtube stuff and will return the username of the video uploader.
     * If Discord did not generate any deliverable author information, this returns null.
     *
     * @return
     *      Possibly-null {@link net.dv8tion.jda.entities.MessageEmbed.Provider Provider} containing author information.
     */
    Provider getAuthor();

    /**
     * The information about the video which should be displayed as an embed.<br>
     * This is used when sites with HTML5 players are linked and embedded. Most commonly Youtube.<br>
     * If this {@link net.dv8tion.jda.EmbedType EmbedType} != {@link net.dv8tion.jda.EmbedType#VIDEO VIDEO} this will always return null.
     *
     * @return
     *      Possibly-null {@link net.dv8tion.jda.entities.MessageEmbed.VideoInfo VideoInfo} containing the information about the video which should be embedded.
     */
    VideoInfo getVideoInfo();

    /**
     * Represents the information Discord provided about a thumbnail image that should be
     *   displayed with an embed message.
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
         * @return
         *      Never-null String containing the url of the displayed image.
         */
        public String getUrl()
        {
            return url;
        }

        /**
         * The Discord proxied url of the thumbnail image.<br>
         * This url will always work, even if the original image was deleted from the hosting website.
         *
         * @return
         *      Never-null String containing the proxied url of this image.
         */
        public String getProxyUrl()
        {
            return proxyUrl;
        }

        /**
         * The width of the thumbnail image.
         *
         * @return
         *      Never-negative, Never-zero int containing the width of the image.
         */
        public int getWidth()
        {
            return width;
        }

        /**
         * The height of the thumbnail image.
         *
         * @return
         *      Never-negative, Never-zero int containing the height of the image.
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
         * The name of the provider.<br>
         * If this is an author, most likely the author's username.<br>
         * If this is a website, most likely the site's name.
         *
         * @return
         *      Never-null String containing the name of the provider.
         */
        public String getName()
        {
            return name;
        }

        /**
         * The url of the provider.<br>
         *
         * @return
         *      Possibly-null String containing the url of the provider.
         */
        public String getUrl()
        {
            return url;
        }
    }

    /**
     * Represents the information provided to embed a video.<br>
     * The videos represented are expected to be played using an HTML5 player from the
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
         * @return
         *      Never-null String containing the video url.
         */
        public String getUrl()
        {
            return url;
        }

        /**
         * The width of the video.<br>
         * This usually isn't the actual video width, but instead the starting embed window size.<br>
         * Basically: Don't rely on this to represent the actual video's quality or size.
         *
         * @return
         *      Non-negative, Non-zero int containing the width of the embedded video.
         */
        public int getWidth()
        {
            return width;
        }

        /**
         * The height of the video.<br>
         * This usually isn't the actual video height, but instead the starting embed window size.<br>
         * Basically: Don't rely on this to represent the actual video's quality or size.
         *
         * @return
         *      Non-negative, Non-zero int containing the height of the embedded video.
         */
        public int getHeight()
        {
            return height;
        }
    }
}
