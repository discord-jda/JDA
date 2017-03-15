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
package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.core.entities.EmbedType;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class MessageEmbedImpl implements MessageEmbed
{
    private String url;
    private String title;
    private String description;
    private EmbedType type;
    private OffsetDateTime timestamp;
    private Color color;
    private Thumbnail thumbnail;
    private Provider siteProvider;
    private AuthorInfo author;
    private VideoInfo videoInfo;
    private Footer footer;
    private ImageInfo image;
    private List<Field> fields;

    @Override
    public String getUrl()
    {
        return url;
    }

    @Override
    public String getTitle()
    {
        return title;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public EmbedType getType()
    {
        return type;
    }

    @Override
    public Thumbnail getThumbnail()
    {
        return thumbnail;
    }

    @Override
    public Provider getSiteProvider()
    {
        return siteProvider;
    }

    @Override
    public AuthorInfo getAuthor()
    {
        return author;
    }

    @Override
    public VideoInfo getVideoInfo()
    {
        return videoInfo;
    }
    
    @Override
    public Footer getFooter() {
        return footer;
    }

    @Override
    public ImageInfo getImage() {
        return image;
    }

    @Override
    public List<Field> getFields() {
        return Collections.unmodifiableList(fields);
    }
    
    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public int getLength()
    {
        int len = 0;

        if (title != null)
            len += title.length();
        if (description != null)
            len += description.length();
        if (author != null)
            len += author.getName().length();
        if (footer != null)
            len += footer.getText().length();
        if (fields != null)
        {
            for (Field f : fields)
            {
                len += f.getName().length() + f.getValue().length();
            }
        }

        return len;
    }

    public MessageEmbedImpl setUrl(String url)
    {
        this.url = url;
        return this;
    }

    public MessageEmbedImpl setTitle(String title)
    {
        this.title = title;
        return this;
    }

    public MessageEmbedImpl setDescription(String description)
    {
        this.description = description;
        return this;
    }

    public MessageEmbedImpl setType(EmbedType type)
    {
        this.type = type;
        return this;
    }

    public MessageEmbedImpl setThumbnail(Thumbnail thumbnail)
    {
        this.thumbnail = thumbnail;
        return this;
    }

    public MessageEmbedImpl setSiteProvider(Provider siteProvider)
    {
        this.siteProvider = siteProvider;
        return this;
    }

    public MessageEmbedImpl setAuthor(AuthorInfo author)
    {
        this.author = author;
        return this;
    }

    public MessageEmbedImpl setVideoInfo(VideoInfo videoInfo)
    {
        this.videoInfo = videoInfo;
        return this;
    }

    public MessageEmbedImpl setFooter(Footer footer)
    {
        this.footer = footer;
        return this;
    }
    
    public MessageEmbedImpl setImage(ImageInfo image)
    {
        this.image = image;
        return this;
    }
    
    public MessageEmbedImpl setFields(List<Field> fields)
    {
        this.fields = fields;
        return this;
    }
    
    public MessageEmbedImpl setColor(Color color)
    {
        this.color = color;
        return this;
    }
    
    public MessageEmbedImpl setTimestamp(OffsetDateTime timestamp)
    {
        this.timestamp = timestamp;
        return this;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof MessageEmbed))
            return false;
        MessageEmbed oMsg = (MessageEmbed) o;
        return this == oMsg;
    }

    @Override
    public int hashCode()
    {
        return getUrl().hashCode();
    }

    @Override
    public String toString()
    {
        return "EmbedMessage";
    }
    
    public JSONObject toJSONObject()
    {
        JSONObject obj = new JSONObject();
        if (url != null)
            obj.put("url", url);
        if (title != null)
            obj.put("title", title);
        if (description != null)
            obj.put("description", description);
        if (timestamp != null)
            obj.put("timestamp", timestamp.format(DateTimeFormatter.ISO_INSTANT));
        if (color != null)
            obj.put("color", color.getRGB() & 0xFFFFFF);
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
            fields.stream().forEach(field -> 
                fieldsArray.put(new JSONObject()
                    .put("name", field.getName())
                    .put("value", field.getValue())
                    .put("inline", field.isInline())));
            obj.put("fields", fieldsArray);
        }
        return obj;
    }
}
