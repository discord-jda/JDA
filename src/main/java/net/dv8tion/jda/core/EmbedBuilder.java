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

import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.impl.MessageEmbedImpl;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Builder system used to build {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbeds}.
 * <br>A visual breakdown of an Embed and how it relates to this class is available at
 * <a href="http://imgur.com/a/yOb5n" target="_blank">http://imgur.com/a/yOb5n</a>.
 *
 * @since  JDA 3.0
 * @author John A. Grosh
 */
public class EmbedBuilder 
{
    public final static int TITLE_MAX_LENGTH = 256;
    public final static int VALUE_MAX_LENGTH = 1024;
    public final static int TEXT_MAX_LENGTH = 2048;
    public final static int URL_MAX_LENGTH = 2000;
    public final static String ZERO_WIDTH_SPACE = "\u200E";
    public final static Pattern URL_PATTERN = Pattern.compile("\\s*https?:\\/\\/.+\\..{2,}\\s*", Pattern.CASE_INSENSITIVE);
    
    private String url;
    private String title;
    private String description;
    private OffsetDateTime timestamp;
    private Color color;
    private MessageEmbed.Thumbnail thumbnail;
    private MessageEmbed.AuthorInfo author;
    private MessageEmbed.Footer footer;
    private MessageEmbed.ImageInfo image;
    private final List<MessageEmbed.Field> fields;
    
    /**
     * Creates an EmbedBuilder to be used to creates an embed to send.
     * <br>Every part of an embed can be removed or cleared by providing {@code null} to the setter method.
     */
    public EmbedBuilder()
    {
        this(null);
    }
    
    /**
     * Creates an EmbedBuilder using fields in an existing embed.
     *
     * @param  embed
     *         the existing embed
     */
    public EmbedBuilder(MessageEmbed embed)
    {
        fields = new LinkedList<>();
        if(embed != null)
        {
            this.url = embed.getUrl();
            this.title = embed.getTitle();
            this.description = embed.getDescription();
            this.timestamp = embed.getTimestamp();
            this.color = embed.getColor();
            this.thumbnail = embed.getThumbnail();
            this.author = embed.getAuthor();
            this.footer = embed.getFooter();
            this.image = embed.getImage();
            if (embed.getFields() != null)
                fields.addAll(embed.getFields());
        }
    }
    
    /**
     * Returns a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}
     * that has been checked as being valid for sending.
     *
     * @throws java.lang.IllegalStateException
     *         If the embed is empty. Can be checked with {@link #isEmpty()}.
     *
     * @return the built, sendable {@link net.dv8tion.jda.core.entities.MessageEmbed}
     */
    public MessageEmbed build()
    {
        if (isEmpty())
        {
            throw new IllegalStateException("Cannot build an empty embed!");
        }
        
        return new MessageEmbedImpl().setTitle(title)
                .setUrl(url)
                .setDescription(description)
                .setTimestamp(timestamp)
                .setColor(color)
                .setThumbnail(thumbnail)
                .setAuthor(author)
                .setFooter(footer)
                .setImage(image)
                .setFields(fields);
    }
    
    /**
     * Checks if the given embed is empty. Empty embeds will throw an exception if built
     * 
     * @return true if the embed is empty and cannot be built
     */
    public boolean isEmpty()
    {
        return title == null
                && description == null
                && timestamp == null
                && color == null
                && thumbnail == null
                && author == null
                && footer == null
                && image == null
                && fields.isEmpty();
    }
    
    /**
     * Sets the Title of the embed.
     *
     * <p><b><a href="http://i.imgur.com/JgZtxIM.png">Example</a></b>
     *
     * @param  title
     *         the title of the embed
     * @param  url
     *         Makes the title into a hyperlink pointed at this url.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided {@code title} is an empty String.</li>
     *             <li>If the length of {@code title} is greater than {@link net.dv8tion.jda.core.EmbedBuilder#TITLE_MAX_LENGTH}.</li>
     *             <li>If the length of {@code url} is longer than {@link net.dv8tion.jda.core.EmbedBuilder#URL_MAX_LENGTH}.</li>
     *             <li>If the provided {@code url} is not a properly formatted http or https url.</li>
     *         </ul>
     *
     * @return the builder after the title has been set
     */
    public EmbedBuilder setTitle(String title, String url)
    {
        if (title == null)
        {
            this.title = null;
            this.url = null;
        }
        else
        {
            if (title.isEmpty())
                throw new IllegalArgumentException("Title cannot be empty!");
            if (title.length() > TITLE_MAX_LENGTH)
                throw new IllegalArgumentException("Title cannot be longer than " + TITLE_MAX_LENGTH + " characters.");
            urlCheck(url);

            this.title = title;
            this.url = url;
        }
        return this;
    }
    
    /**
     * Sets the Description of the embed. This is where the main chunk of text for an embed is typically placed.
     *
     * <p><b><a href="http://i.imgur.com/lbchtwk.png">Example</a></b>
     *
     * @param  description
     *         the description of the embed
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided {@code description} String is empty.</li>
     *             <li>If the length of {@code description} is greater than {@link net.dv8tion.jda.core.EmbedBuilder#TEXT_MAX_LENGTH}.</li>
     *         </ul>
     *
     * @return the builder after the description has been set
     */
    public EmbedBuilder setDescription(String description)
    {
        if (description == null)
        {
            this.description = null;
        }
        else
        {
            if (description.isEmpty())
                throw new IllegalArgumentException("Description must not be empty!");
            if (description.length() > TEXT_MAX_LENGTH)
                throw new IllegalArgumentException("Description cannot be longer than " + TEXT_MAX_LENGTH + " characters.");
            this.description = description;
        }
        return this;
    }
    
    /**
     * Sets the Timestamp of the embed.
     *
     * <p><b><a href="http://i.imgur.com/YP4NiER.png">Example</a></b>
     *
     * <p><b>Hint:</b> You can get the current time using {@link java.time.Instant#now() Instant.now()} or convert time from a
     * millisecond representation by using {@link java.time.Instant#ofEpochMilli(long) Instant.ofEpochMilli(long)};
     *
     * @param  temporal
     *         the temporal accessor of the timestamp
     *
     * @return the builder after the timestamp has been set
     */
    public EmbedBuilder setTimestamp(TemporalAccessor temporal)
    {
        if (temporal == null)
            this.timestamp = null;
        else
            this.timestamp = OffsetDateTime.from(temporal);
        return this; 
    }
    
    /**
     * Sets the Color of the embed.
     *
     * <p><b><a href="http://i.imgur.com/2YnxnRM.png">Example</a></b>
     *
     * <p><b>Hint:</b> You can use a predefined color like {@link java.awt.Color#BLUE} or you can define
     * your own color using one of Color's constructors.
     * <br>Example: {@link java.awt.Color#Color(int, int, int) new Color(0, 0, 255)}. This is the same as {@link java.awt.Color#BLUE}
     *
     * @param  color
     *         the color of the embed
     *
     * @return the builder after the color has been set
     */
    public EmbedBuilder setColor(Color color)
    {
        this.color = color;
        return this;
    }
    
    /**
     * Sets the Thumbnail of the embed.
     *
     * <p><b><a href="http://i.imgur.com/Zc3qwqB.png">Example</a></b>
     *
     * @param  url
     *         the url of the thumbnail of the embed
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the length of {@code url} is longer than {@link net.dv8tion.jda.core.EmbedBuilder#URL_MAX_LENGTH}.</li>
     *             <li>If the provided {@code url} is not a properly formatted http or https url.</li>
     *         </ul>
     *
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
            urlCheck(url);
            this.thumbnail = new MessageEmbed.Thumbnail(url, null, 0, 0);
        }
        return this;
    }

    /**
     * Sets the Image of the embed.
     *
     * <p><b><a href="http://i.imgur.com/2hzuHFJ.png">Example</a></b>
     *
     * @param  url
     *         the url of the image of the embed
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the length of {@code url} is longer than {@link net.dv8tion.jda.core.EmbedBuilder#URL_MAX_LENGTH}.</li>
     *             <li>If the provided {@code url} is not a properly formatted http or https url.</li>
     *         </ul>
     *
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
            urlCheck(url);
            this.image = new MessageEmbed.ImageInfo(url, null, 0, 0);
        }
        return this;
    }
    
    /**
     * Sets the Author of the embed. The author appears in the top left of the embed and can have a small
     * image beside it along with the author's name being made clickable by way of providing a url.
     *
     * <p><b><a href="http://i.imgur.com/JgZtxIM.png">Example</a></b>
     *
     * @param  name
     *         the name of the author of the embed. If this is not set, the author will not appear in the embed
     * @param  url
     *         the url of the author of the embed
     * @param  iconUrl
     *         the url of the icon for the author
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the length of {@code url} is longer than {@link net.dv8tion.jda.core.EmbedBuilder#URL_MAX_LENGTH}.</li>
     *             <li>If the provided {@code url} is not a properly formatted http or https url.</li>
     *             <li>If the length of {@code iconUrl} is longer than {@link net.dv8tion.jda.core.EmbedBuilder#URL_MAX_LENGTH}.</li>
     *             <li>If the provided {@code iconUrl} is not a properly formatted http or https url.</li>
     *         </ul>
     *
     * @return the builder after the author has been set
     */
    public EmbedBuilder setAuthor(String name, String url, String iconUrl)
    {
        //We only check if the name is null because its presence is what determines if the
        // the author will appear in the embed.
        if (name == null)
        {
            this.author = null;
        }
        else
        {
            urlCheck(url);
            urlCheck(iconUrl);
            this.author = new MessageEmbed.AuthorInfo(name, url, iconUrl, null);
        }
        return this;
    }
    
    /**
     * Sets the Footer of the embed.
     *
     * <p><b><a href="http://i.imgur.com/jdf4sbi.png">Example</a></b>
     *
     * @param  text
     *         the text of the footer of the embed. If this is not set, the footer will not appear in the embed.
     * @param  iconUrl
     *         the url of the icon for the footer
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the length of {@code text} is longer than {@link net.dv8tion.jda.core.EmbedBuilder#TEXT_MAX_LENGTH}.</li>
     *             <li>If the length of {@code iconUrl} is longer than {@link net.dv8tion.jda.core.EmbedBuilder#URL_MAX_LENGTH}.</li>
     *             <li>If the provided {@code iconUrl} is not a properly formatted http or https url.</li>
     *         </ul>
     *
     * @return the builder after the footer has been set
     */
    public EmbedBuilder setFooter(String text, String iconUrl)
    {
        //We only check if the text is null because its presence is what determines if the
        // footer will appear in the embed.
        if (text == null)
        {
            this.footer = null;
        }
        else
        {
            if (text != null && text.length() > TEXT_MAX_LENGTH)
                throw new IllegalArgumentException("Text cannot be longer than " + TEXT_MAX_LENGTH + " characters.");
            urlCheck(iconUrl);
            this.footer = new MessageEmbed.Footer(text, iconUrl, null);
        }
        return this;
    }
    
    /**
     * Copies the provided Field into a new Field for this builder.
     * <br>For additional documentation, see {@link #addField(String, String, boolean)}
     * 
     * @param  field
     *         the field object to add
     *
     * @return the builder after the field has been added
     */
    public EmbedBuilder addField(MessageEmbed.Field field)
    {
        return addField(field.getName(), field.getValue(), field.isInline());
    }
    
    /**
     * Adds a Field to the embed.
     *
     * <p>Note: If a blank string is provided to either {@code name} or {@code value}, the blank string is replaced
     * with {@link net.dv8tion.jda.core.EmbedBuilder#ZERO_WIDTH_SPACE}.
     *
     * <p><b><a href="http://i.imgur.com/gnjzCoo.png">Example of Inline</a></b>
     * <p><b><a href="http://i.imgur.com/Ky0KlsT.png">Example if Non-inline</a></b>
     * 
     * @param  name
     *         the name of the Field, displayed in bold above the {@code value}.
     * @param  value
     *         the contents of the field.
     * @param  inline
     *         whether or not this field should display inline.
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If only {@code name} or {@code value} is set. Both must be set.</li>
     *             <li>If the length of {@code name} is greater than {@link net.dv8tion.jda.core.EmbedBuilder#TITLE_MAX_LENGTH}.</li>
     *             <li>If the length of {@code value} is greater than {@link net.dv8tion.jda.core.EmbedBuilder#TEXT_MAX_LENGTH}.</li>
     *         </ul>
     *
     * @return the builder after the field has been added
     */
    public EmbedBuilder addField(String name, String value, boolean inline)
    {
        if (name == null && value == null)
            return this;
        else if (name == null || value == null)
            throw new IllegalArgumentException("Both Name and Value must be set!");
        else if (name.length() > TITLE_MAX_LENGTH)
            throw new IllegalArgumentException("Name cannot be longer than " + TITLE_MAX_LENGTH + " characters.");
        else if (value.length() > VALUE_MAX_LENGTH)
            throw new IllegalArgumentException("Value cannot be longer than " + VALUE_MAX_LENGTH + " characters.");
        if (name.isEmpty())
            name = ZERO_WIDTH_SPACE;
        if (value.isEmpty())
            value = ZERO_WIDTH_SPACE;
        this.fields.add(new MessageEmbed.Field(name, value, inline));
        return this;
    }
    
    /**
     * Adds a blank (empty) Field to the embed.
     *
     * <p><b><a href="http://i.imgur.com/tB6tYWy.png">Example of Inline</a></b>
     * <p><b><a href="http://i.imgur.com/lQqgH3H.png">Example of Non-inline</a></b>
     *
     * @param  inline
     *         whether or not this field should display inline
     *
     * @return the builder after the field has been added
     */
    public EmbedBuilder addBlankField(boolean inline)
    {
        this.fields.add(new MessageEmbed.Field(ZERO_WIDTH_SPACE, ZERO_WIDTH_SPACE, inline));
        return this;
    }
    
    private void urlCheck(String url)
    {
        if (url == null)
            return;
        else if (url.length() > URL_MAX_LENGTH)
            throw new IllegalArgumentException("URL cannot be longer than " + URL_MAX_LENGTH + " characters.");
        else if (!URL_PATTERN.matcher(url).matches())
            throw new IllegalArgumentException("URL must be a valid http or https url.");
    }
}
