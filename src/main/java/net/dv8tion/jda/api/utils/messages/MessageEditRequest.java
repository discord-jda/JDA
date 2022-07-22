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

package net.dv8tion.jda.api.utils.messages;

import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public interface MessageEditRequest<R extends MessageEditRequest<R>> extends MessageRequest<R>
{
    @Nonnull
    R setAttachments(@Nullable Collection<? extends AttachedFile> attachments);

    @Nonnull
    @Override
    default R setFiles(@Nullable Collection<? extends FileUpload> files)
    {
        return setAttachments(files);
    }

    /**
     * Whether to replace the existing message completely.
     *
     * <p>By default, edit requests will only update the message fields which were explicitly set.
     * Changing this to {@code true}, will instead replace everything and remove all unset fields.
     *
     * <p><b>Example Default</b><br>
     * A request such as this will only edit the {@code content} of the message, and leave any existing embeds or attachments intact.
     * <pre>{@code
     * message.editMessage("hello").queue();
     * }</pre>
     *
     * <p><b>Example Replace</b><br>
     * A request such as this will replace the entire message, and remove any existing embeds, attachments, components, etc.
     * <pre>{@code
     * message.editMessage("hello").replace(true).queue();
     * }</pre>
     *
     * @param  isReplace
     *         True, if only things explicitly set on this request should be present after the message is edited.
     *
     * @return The same message edit request builder
     */
    @Nonnull
    R replace(boolean isReplace);
}
