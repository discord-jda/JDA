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

package net.dv8tion.jda.api.components;

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
    @Nullable
    String getContentType();
}
