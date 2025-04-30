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
import net.dv8tion.jda.api.utils.data.DataArray;

import java.util.List;

public class MessageUtil
{
    public static DataArray getAttachmentsData(List<? extends AttachedFile> files, List<FileUpload> additionalFiles)
    {
        DataArray attachments = DataArray.empty();
        int fileUploadCount = 0;

        if (files != null)
        {
            for (AttachedFile file : files)
            {
                attachments.add(file.toAttachmentData(fileUploadCount));
                if (file instanceof FileUpload)
                    fileUploadCount++;
            }
        }

        if (!additionalFiles.isEmpty())
        {
            for (FileUpload file : additionalFiles)
            {
                attachments.add(file.toAttachmentData(fileUploadCount));
                fileUploadCount++;
            }
        }

        return attachments;
    }
}
