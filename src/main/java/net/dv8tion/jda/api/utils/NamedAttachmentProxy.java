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

package net.dv8tion.jda.api.utils;

import javax.annotation.Nonnull;

/**
 * A utility class to retrieve attachments.
 * <br>This supports downloading the images from the normal URL, as well as downloading the image with a specific width and height.
 *
 * <p>This is a specialization of {@link AttachmentProxy}, which is aware of the file name of the attachment.
 */
public class NamedAttachmentProxy extends AttachmentProxy
{
    private final String fileName;

    /**
     * Constructs a new {@link AttachmentProxy} for the provided URL.
     *
     * @param  url
     *         The URL to download the attachment from
     * @param  fileName
     *         An optional file name for {@link #downloadAsFileUpload(int, int)}
     *
     * @throws IllegalArgumentException
     *         If the provided URL is null
     */
    public NamedAttachmentProxy(@Nonnull String url, @Nonnull String fileName)
    {
        super(url);
        this.fileName = fileName;
    }

    /**
     * The file name of the attachment.
     *
     * @return The file name
     */
    @Nonnull
    public String getFileName()
    {
        return fileName;
    }

    /**
     * Returns a {@link FileUpload} which supplies a data stream of this attachment,
     * with the original attachment's file name and at the specified size.
     * <br>The returned {@link FileUpload} can be reused safely, and does not need to be closed.
     *
     * <p>The attachment, if an image, may be resized at any size, however if the size does not fit the ratio of the image, then it will be cropped as to fit the target size.
     * <br>If the attachment is not an image then the size parameters are ignored and the file is downloaded.
     *
     * @param  width
     *         The width of this image, must be positive
     * @param  height
     *         The height of this image, must be positive
     *
     * @throws IllegalArgumentException
     *         If any of the follow checks are true
     *         <ul>
     *             <li>The requested width is negative or 0</li>
     *             <li>The requested height is negative or 0</li>
     *         </ul>
     * @throws IllegalStateException
     *         If the original attachment's name is not known,
     *         this can happen if this isn't from a {@link net.dv8tion.jda.api.entities.Message.Attachment Message.Attachment}
     *
     * @return {@link FileUpload} from this attachment.
     */
    @Nonnull
    public FileUpload downloadAsFileUpload(int width, int height)
    {
        if (fileName == null)
            throw new IllegalStateException("The file name is not available for this AttachmentProxy");

        return downloadAsFileUpload(fileName, width, height);
    }
}
