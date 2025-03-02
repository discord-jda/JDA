package net.dv8tion.jda.internal.interactions.components;

import net.dv8tion.jda.api.interactions.components.ResolvedMedia;
import net.dv8tion.jda.api.utils.AttachmentProxy;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;

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
}
