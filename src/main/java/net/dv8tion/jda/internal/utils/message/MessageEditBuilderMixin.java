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

package net.dv8tion.jda.internal.utils.message;

import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.api.utils.messages.MessageEditRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

@SuppressWarnings("unchecked")
public interface MessageEditBuilderMixin<R extends MessageEditRequest<R>> extends AbstractMessageBuilderMixin<R, MessageEditBuilder>, MessageEditRequest<R>
{
    @Nonnull
    @Override
    default R setAttachments(@Nullable Collection<? extends AttachedFile> attachments)
    {
        getBuilder().setAttachments(attachments);
        return (R) this;
    }

    @Nonnull
    @Override
    default R setReplace(boolean isReplace)
    {
        getBuilder().setReplace(isReplace);
        return (R) this;
    }

    @Nonnull
    @Override
    default R setFiles(@Nullable Collection<? extends FileUpload> files)
    {
        getBuilder().setFiles(files);
        return (R) this;
    }

    @Nonnull
    @Override
    default R applyData(@Nonnull MessageEditData data)
    {
        getBuilder().applyData(data);
        return (R) this;
    }

    @Override
    default boolean isReplace()
    {
        return getBuilder().isReplace();
    }
}
