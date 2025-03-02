package net.dv8tion.jda.api.interactions.components;

import net.dv8tion.jda.api.utils.AttachmentProxy;

import javax.annotation.Nonnull;

// TODO-components-v2 docs
public interface ResolvedMedia
{
    // TODO-components-v2 docs
    @Nonnull
    String getUrl();

    // TODO-components-v2 docs
    @Nonnull
    String getProxyUrl();

    @Nonnull
    AttachmentProxy getProxy();

    // TODO-components-v2 docs
    int getWidth();

    // TODO-components-v2 docs
    int getHeight();

    // TODO-components-v2 docs
    @Nonnull
    String getContentType();

    // TODO-components-v2 docs
    @Nonnull
    LoadingState getLoadingState();

    // TODO-components-v2 docs
    enum LoadingState
    {
        UNKNOWN(0),
        LOADING(1),
        LOADING_SUCCESS(2),
        LOADING_NOT_FOUND(3),
        ;

        private final int key;

        LoadingState(int key)
        {
            this.key = key;
        }

        public static LoadingState fromKey(int value)
        {
            for (LoadingState state : values())
            {
                if (state.getKey() == value)
                    return state;
            }

            return null;
        }

        public int getKey()
        {
            return key;
        }
    }
}
