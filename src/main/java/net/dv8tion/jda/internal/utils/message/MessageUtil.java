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

import net.dv8tion.jda.api.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.components.utils.ComponentIterator;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.internal.entities.FileContainerMixin;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MessageUtil
{
    @Nonnull
    public static List<FileUpload> getIndirectFiles(@Nonnull Collection<MessageTopLevelComponentUnion> components)
    {
        return ComponentIterator.createStream(components)
                .filter(FileContainerMixin.class::isInstance)
                .map(FileContainerMixin.class::cast)
                .flatMap(FileContainerMixin::getFiles)
                .collect(Collectors.toList());
    }

    public static DataArray getAttachmentsData(@Nonnull Collection<? extends AttachedFile> files)
    {
        DataArray attachments = DataArray.empty();
        int fileUploadCount = 0;

        for (AttachedFile file : files)
        {
            attachments.add(file.toAttachmentData(fileUploadCount));
            if (file instanceof FileUpload)
                fileUploadCount++;
        }

        return attachments;
    }
}
