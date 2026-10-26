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

package net.dv8tion.jda.test.entities.message;

import net.dv8tion.jda.api.entities.Message;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageAttachmentTest {
    private static final int NO_DIMENSIONS = -1;

    @Test
    void isImageUsesContentType() {
        assertThat(createAttachment("image.avif", "image/avif", 100).isImage()).isTrue();
        assertThat(createAttachment("image.png", "image/png", 100).isImage()).isTrue();
        assertThat(createAttachment("image.png", "IMAGE/PNG", 100).isImage()).isTrue();
        assertThat(createAttachment("video.mp4", "video/mp4", 100).isImage()).isFalse();
    }

    @Test
    void isVideoUsesContentType() {
        assertThat(createAttachment("video.mp4", "video/mp4", 100).isVideo()).isTrue();
        assertThat(createAttachment("video.mkv", "video/x-matroska", 100).isVideo())
                .isTrue();
        assertThat(createAttachment("image.avif", "image/avif", 100).isVideo()).isFalse();
    }

    @Test
    void contentTypeTakesPrecedenceOverMismatchedExtension() {
        Message.Attachment videoNamedAsImage = createAttachment("video.png", "video/mp4", 100);
        assertThat(videoNamedAsImage.isImage()).isFalse();
        assertThat(videoNamedAsImage.isVideo()).isTrue();
    }

    @Test
    void extensionIsUsedWithoutContentType() {
        assertThat(createAttachment("image.avif", null, 100).isImage()).isTrue();
        assertThat(createAttachment("image.png", null, 100).isImage()).isTrue();
        assertThat(createAttachment("video.mp4", null, 100).isVideo()).isTrue();
        assertThat(createAttachment("file.txt", null, 100).isImage()).isFalse();
    }

    @Test
    void extensionIsUsedWithGenericContentType() {
        assertThat(createAttachment("image.png", "application/octet-stream", 100)
                        .isImage())
                .isTrue();
        assertThat(createAttachment("video.mp4", "application/octet-stream", 100)
                        .isVideo())
                .isTrue();
    }

    @Test
    void attachmentWithoutDimensionsIsNeitherImageNorVideo() {
        assertThat(createAttachment("image.png", "image/png", NO_DIMENSIONS).isImage())
                .isFalse();
        assertThat(createAttachment("video.mp4", "video/mp4", NO_DIMENSIONS).isVideo())
                .isFalse();
    }

    private Message.Attachment createAttachment(String fileName, String contentType, int dimensions) {
        return new Message.Attachment(
                1L,
                "https://cdn.discordapp.com/attachments/1/1/" + fileName,
                "https://media.discordapp.net/attachments/1/1/" + fileName,
                fileName,
                contentType,
                null,
                1024,
                dimensions,
                dimensions,
                0,
                false,
                null,
                0,
                null,
                null);
    }
}
