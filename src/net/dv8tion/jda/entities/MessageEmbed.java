/**
 *    Copyright 2015 Austin Keener & Michael Ritter
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

public interface MessageEmbed
{
    String getUrl();

    String getTitle();

    String getDescription();

    EmbedType getType();

    Thumbnail getThumbnail();

    Provider getSiteProvider();

    Provider getAuthor();

    VideoInfo getVideoInfo();

    public class Thumbnail
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

        public String getUrl()
        {
            return url;
        }

        public String getProxyUrl()
        {
            return proxyUrl;
        }

        public int getWidth()
        {
            return width;
        }

        public int getHeight()
        {
            return height;
        }
    }

    public class Provider
    {
        protected final String name;
        protected final String url;

        public Provider(String name, String url)
        {
            this.name = name;
            this.url = url;
        }

        public String getName()
        {
            return name;
        }

        public String getUrl()
        {
            return url;
        }
    }

    public class VideoInfo
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

        public String getUrl()
        {
            return url;
        }

        public int getWidth()
        {
            return width;
        }

        public int getHeight()
        {
            return height;
        }
    }
}
