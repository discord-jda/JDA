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

package net.dv8tion.jda.api.interactions.components.thumbnail;

import net.dv8tion.jda.api.interactions.components.IdentifiableComponent;
import net.dv8tion.jda.api.interactions.components.ResolvedMedia;
import net.dv8tion.jda.api.interactions.components.section.SectionAccessoryComponent;
import net.dv8tion.jda.internal.interactions.components.thumbnail.ThumbnailImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// TODO-components-v2 docs
public interface Thumbnail extends SectionAccessoryComponent, IdentifiableComponent
{
    // TODO-components-v2 docs
    @Nonnull
    static Thumbnail fromUrl(@Nonnull String url)
    {
        Checks.notNull(url, "URL");
        return new ThumbnailImpl(url);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    Thumbnail withUniqueId(int uniqueId);

    // TODO-components-v2 docs
    @Nonnull
    @CheckReturnValue
    Thumbnail withUrl(@Nonnull String url);

    // TODO-components-v2 docs
    @Nonnull
    @CheckReturnValue
    Thumbnail withDescription(@Nullable String description);

    // TODO-components-v2 docs
    @Nonnull
    @CheckReturnValue
    Thumbnail withSpoiler(boolean spoiler);

    // TODO-components-v2 docs
    @Nonnull
    String getUrl();

    // TODO-components-v2 docs
    @Nullable
    ResolvedMedia getResolvedMedia();

    // TODO-components-v2 docs
    @Nullable
    String getDescription();

    // TODO-components-v2 docs
    boolean isSpoiler();

}
