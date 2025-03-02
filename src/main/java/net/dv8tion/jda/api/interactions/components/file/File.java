package net.dv8tion.jda.api.interactions.components.file;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.IdentifiableComponent;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.interactions.components.ResolvedMedia;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponent;
import net.dv8tion.jda.internal.interactions.components.file.FileImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// TODO-components-v2 docs
public interface File extends Component, IdentifiableComponent, MessageTopLevelComponent, ContainerChildComponent
{
    // TODO-components-v2 docs
    @Nonnull
    static File fromUrl(@Nonnull String url)
    {
        Checks.notNull(url, "URL");
        return new FileImpl(url);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    File withUniqueId(int uniqueId);

    // TODO-components-v2 docs
    @Nonnull
    @CheckReturnValue
    File withUrl(@Nonnull String url);

    // TODO-components-v2 docs
    @Nonnull
    @CheckReturnValue
    File withSpoiler(boolean spoiler);

    // TODO-components-v2 docs
    @Nonnull
    String getUrl();

    // TODO-components-v2 docs
    @Nullable
    ResolvedMedia getResolvedMedia();

    // TODO-components-v2 docs
    boolean isSpoiler();
}
