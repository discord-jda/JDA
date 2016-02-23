/**
 * Copyright 2015-2016 Austin Keener & Michael Ritter
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.entities.impl;

import net.dv8tion.jda.EmbedType;
import net.dv8tion.jda.entities.MessageEmbed;

public class MessageEmbedImpl implements MessageEmbed {
    //TODO: Id?!
    private String url;
    private String title;
    private String description;
    private EmbedType type;
    private Thumbnail thumbnail;
    private Provider siteProvider;
    private Provider author;
    private VideoInfo videoInfo;

    @Override
    public String getUrl() {
        return url;
    }

    public MessageEmbedImpl setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public MessageEmbedImpl setTitle(String title) {
        this.title = title;
        return this;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public MessageEmbedImpl setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public EmbedType getType() {
        return type;
    }

    public MessageEmbedImpl setType(EmbedType type) {
        this.type = type;
        return this;
    }

    @Override
    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public MessageEmbedImpl setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }

    @Override
    public Provider getSiteProvider() {
        return siteProvider;
    }

    public MessageEmbedImpl setSiteProvider(Provider siteProvider) {
        this.siteProvider = siteProvider;
        return this;
    }

    @Override
    public Provider getAuthor() {
        return author;
    }

    public MessageEmbedImpl setAuthor(Provider author) {
        this.author = author;
        return this;
    }

    @Override
    public VideoInfo getVideoInfo() {
        return videoInfo;
    }

    public MessageEmbedImpl setVideoInfo(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MessageEmbed))
            return false;
        MessageEmbed oMsg = (MessageEmbed) o;
        return this == oMsg;
    }

    @Override
    public int hashCode() {
        return getUrl().hashCode();
    }

    @Override
    public String toString() {
        return "EmbedMessage";
    }
}
