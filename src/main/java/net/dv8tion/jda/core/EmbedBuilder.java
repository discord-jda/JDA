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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.dv8tion.jda.core;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.impl.MessageEmbedImpl;

public class EmbedBuilder 
{
    public final static int TITLE_MAX_LENGTH = 256;
    public final static int VALUE_MAX_LENGTH = 1024;
    public final static int TEXT_MAX_LENGTH = 2048;
    public final static int URL_MAX_LENGTH = 2000;
    public final static Pattern URL_PATTERN = Pattern.compile("\\s*https?:\\/\\/[a-z0-9]+([a-z0-9-]*[a-z0-9]+)?\\.[a-z]{2,}\\s*");
    public final static Pattern HTTPS_URL_PATTERN = Pattern.compile("\\s*https:\\/\\/[a-z0-9]+([a-z0-9-]*[a-z0-9]+)?\\.[a-z]{2,}\\s*");
    
    private String url;
    private String title;
    private String description;
    private OffsetDateTime timestamp;
    private Color color;
    private MessageEmbed.Thumbnail thumbnail;
    private MessageEmbed.Provider siteProvider;
    private MessageEmbed.AuthorInfo author;
    private MessageEmbed.VideoInfo videoInfo;
    private MessageEmbed.Footer footer;
    private MessageEmbed.ImageInfo image;
    private final List<MessageEmbed.Field> fields;
    /**
     * Creates an EmbedBuilder to be used to creates an embed to send
     */
    public EmbedBuilder()
    {
        fields = new LinkedList<>();
    }
    
    /**
     * Returns a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}
     * that has been checked as being valid for sending.
     * 
     * @return the built, sendable MessageEmbed
     */
    public MessageEmbed build()
    {
        if (title == null
                && url == null
                && description == null
                && timestamp == null
                && color == null
                && thumbnail == null
                && siteProvider == null
                && author == null
                && videoInfo == null
                && footer == null
                && image == null
                && fields.isEmpty())
        {
            throw new IllegalStateException("Cannot build an empty embed!");
        }
        
        return new MessageEmbedImpl().setTitle(title)
                .setUrl(url)
                .setDescription(description)
                .setTimestamp(timestamp)
                .setColor(color)
                .setThumbnail(thumbnail)
                .setSiteProvider(siteProvider)
                .setAuthor(author)
                .setVideoInfo(videoInfo)
                .setFooter(footer)
                .setImage(image)
                .setFields(fields);
    }
    
    /**
     * Sets the URL of the embed
     * @param url the url of the embed
     * @return the builder after the url has been set
     */
    public EmbedBuilder setUrl(String url)
    {
        urlCheck(url);
        this.url = url;
        return this;
    }
    
    /**
     * Sets the Title of the embed
     * @param title the title of the embed
     * @return the builder after the title has been set
     */
    public EmbedBuilder setTitle(String title)
    {
        if (title != null && title.length() > TITLE_MAX_LENGTH)
            throw new IllegalStateException("Title cannot be longer than " + TITLE_MAX_LENGTH + " characters.");
        this.title = title;
        return this;
    }
    
    /**
     * Sets the Description of the embed.
     * @param description the description of the embed
     * @return the builder after the description has been set
     */
    public EmbedBuilder setDescription(String description)
    {
        if (description != null && description.length() > TEXT_MAX_LENGTH)
            throw new IllegalStateException("Description cannot be longer than " + TEXT_MAX_LENGTH + " characters.");
        this.description = description;
        return this;
    }
    
    /**
     * Sets the Timestamp of the embed.
     * @param timestamp the timestamp of the embed
     * @return the builder after the timestamp has been set
     */
    public EmbedBuilder setTimestamp(OffsetDateTime timestamp)
    {
        this.timestamp = timestamp;
        return this;
    }
    
    /**
     * Sets the Color of the embed.
     * @param color the color of the embed
     * @return the builder after the color has been set
     */
    public EmbedBuilder setColor(Color color)
    {
        this.color = color;
        return this;
    }
    
    /**
     * Sets the Thumbnail of the embed.
     * @param url the url of the thumbnail of the embed
     * @return the builder after the thumbnail has been set
     */
    public EmbedBuilder setThumbnail(String url)
    {
        if (url == null)
        {
            this.thumbnail = null;
        }
        else
        {
            httpsCheck(url);
            this.thumbnail = new MessageEmbed.Thumbnail(url, null, 0, 0);
        }
        return this;
    }
    
    /**
     * Sets the Video of the embed.
     * @param url the url of the video of the embed
     * @return the builder after the video has been set
     */
    public EmbedBuilder setVideo(String url)
    {
        if (url == null)
        {
            this.videoInfo = null;
        }
        else
        {
            urlCheck(url);
            this.videoInfo = new MessageEmbed.VideoInfo(url, 0, 0);
        }
        return this;
    }
    
    /**
     * Sets the Image of the embed.
     * @param url the url of the image of the embed
     * @return the builder after the image has been set
     */
    public EmbedBuilder setImage(String url)
    {
        if (url == null)
        {
            this.image = null;
        }
        else
        {
            httpsCheck(url);
            this.image = new MessageEmbed.ImageInfo(url, null, 0, 0);
        }
        return this;
    }
    
    /**
     * Sets the Provider of the embed.
     * @param name the name of the provider of the embed
     * @param url the url of the provider of the embed
     * @return the builder after the provider has been set
     */
    public EmbedBuilder setProvider(String name, String url)
    {
        if (name == null && url == null)
        {
            this.siteProvider = null;
        }
        else
        {
            urlCheck(url);
            this.siteProvider = new MessageEmbed.Provider(name, url);
        }
        return this;
    }
    
    /**
     * Sets the Author of the embed.
     * @param name the name of the author of the embed. If this is not set, the 
     * author will not appear in the embed
     * @param url the url of the author of the embed
     * @param iconUrl the url of the icon for the author
     * @return the builder after the author has been set
     */
    public EmbedBuilder setAuthor(String name, String url, String iconUrl)
    {
        if (name == null && url == null && iconUrl == null)
        {
            this.author = null;
        }
        else
        {
            urlCheck(url);
            this.author = new MessageEmbed.AuthorInfo(name, url, iconUrl, null);
        }
        return this;
    }
    
    /**
     * Sets the Footer of the embed.
     * @param text the text of the footer of the embed. If this is not set, the 
     * footer will not appear in the embed
     * @param iconUrl the url of the icon for the footer
     * @return the builder after the footer has been set
     */
    public EmbedBuilder setFooter(String text, String iconUrl)
    {
        if (text == null && iconUrl == null)
        {
            this.footer = null;
        }
        else
        {
            if (text != null && text.length() > TEXT_MAX_LENGTH)
                throw new IllegalStateException("Text cannot be longer than " + TEXT_MAX_LENGTH + " characters.");
            httpsCheck(iconUrl);
            this.footer = new MessageEmbed.Footer(text, iconUrl, null);
        }
        return this;
    }
    
    /**
     * Adds a Field to the embed.
     * @param name the name of the footer of the embed.
     * @param value the contents of the field
     * @param inline whether or not this field should display inline
     * @return the builder after the field has been added
     */
    public EmbedBuilder addField(String name, String value, boolean inline)
    {
        if (name == null && value == null)
            return this;
        else if (name == null || value == null)
            throw new IllegalStateException("Both Name and Value must be set!");
        else if (name.length() > TITLE_MAX_LENGTH)
            throw new IllegalStateException("Name cannot be longer than " + TITLE_MAX_LENGTH + " characters.");
        else if (value.length() > VALUE_MAX_LENGTH)
            throw new IllegalStateException("Value cannot be longer than " + VALUE_MAX_LENGTH + " characters.");
        this.fields.add(new MessageEmbed.Field(name, value, inline));
        return this;
    }
    
    private void urlCheck(String url)
    {
        if (url == null)
            return;
        else if (url.length() > URL_MAX_LENGTH)
            throw new IllegalStateException("URL cannot be longer than " + URL_MAX_LENGTH + " characters.");
        else if (!URL_PATTERN.matcher(url).matches())
            throw new IllegalStateException("URL must be a valid http or https url.");
    }
    
    private void httpsCheck(String url)
    {
        if (url == null)
            return;
        else if (url.length() > URL_MAX_LENGTH)
            throw new IllegalStateException("URL cannot be longer than " + URL_MAX_LENGTH + " characters.");
        else if (!HTTPS_URL_PATTERN.matcher(url).matches())
            throw new IllegalStateException("URL must be a valid https url.");
    }
}
