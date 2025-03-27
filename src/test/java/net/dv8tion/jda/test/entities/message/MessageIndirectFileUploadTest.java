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

import net.dv8tion.jda.api.components.filedisplay.FileDisplay;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageIndirectFileUploadTest
{
    @Test
    void testIndirectFileUploadGetsUploaded()
    {
        try (MessageCreateData data = new MessageCreateBuilder()
                .setComponents(FileDisplay.fromFile(FileUpload.fromData(new byte[100], "bytes.bin")))
                .useComponentsV2()
                .build())
        {
            assertThat(data.getFiles()).isEmpty();
            assertThat(data.getAdditionalFiles()).hasSize(1)
                    .element(0).extracting(FileUpload::getName).isEqualTo("bytes.bin");
        }

        try (MessageEditData data = new MessageEditBuilder()
                .setComponents(FileDisplay.fromFile(FileUpload.fromData(new byte[100], "bytes.bin")))
                .useComponentsV2()
                .build())
        {
            assertThat(data.getFiles()).isEmpty();
            assertThat(data.getAdditionalFiles()).hasSize(1)
                    .element(0).extracting(FileUpload::getName).isEqualTo("bytes.bin");
        }
    }
}
