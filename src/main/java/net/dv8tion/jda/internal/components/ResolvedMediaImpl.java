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

package net.dv8tion.jda.internal.components;

import net.dv8tion.jda.api.components.ResolvedMedia;
import net.dv8tion.jda.api.utils.AttachmentProxy;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ResolvedMediaImpl implements ResolvedMedia
{
    private final String url;
    private final String proxyUrl;
    private final int width, height;
    private final String contentType;
    private final LoadingState loadingState;

    public ResolvedMediaImpl(DataObject data)
    {
        this(
                data.getString("url"),
                data.getString("proxy_url"),
                data.getInt("width"),
                data.getInt("height"),
                data.getString("content_type"),
                LoadingState.fromKey(data.getInt("loading_state"))
        );
    }

    public ResolvedMediaImpl(String url, String proxyUrl, int width, int height, String contentType, LoadingState loadingState)
    {
        this.url = url;
        this.proxyUrl = proxyUrl;
        this.width = width;
        this.height = height;
        this.contentType = contentType;
        this.loadingState = loadingState;
    }

    @Nonnull
    @Override
    public String getUrl()
    {
        return url;
    }

    @Nonnull
    @Override
    public String getProxyUrl()
    {
        return proxyUrl;
    }

    @Nonnull
    @Override
    public AttachmentProxy getProxy()
    {
        return new AttachmentProxy(proxyUrl);
    }

    @Override
    public int getWidth()
    {
        return width;
    }

    @Override
    public int getHeight()
    {
        return height;
    }

    @Nonnull
    @Override
    public String getContentType()
    {
        return contentType;
    }

    @Nonnull
    @Override
    public LoadingState getLoadingState()
    {
        return loadingState;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof ResolvedMediaImpl)) return false;
        ResolvedMediaImpl that = (ResolvedMediaImpl) o;
        return Objects.equals(url, that.url) && loadingState == that.loadingState;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(url, loadingState);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .setType(loadingState)
                // url is already shown by the classes containing resolved medias
                .addMetadata("proxy_url", url)
                .toString();
    }
}
