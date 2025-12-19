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

package net.dv8tion.jda.test.util;

import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.generated.CreateRoleRequestDto;
import net.dv8tion.jda.internal.utils.SerializationUtil;
import net.dv8tion.jda.internal.utils.requestbody.JacksonRequestBody;
import net.dv8tion.jda.test.AbstractSnapshotTest;
import okio.Okio;
import okio.Sink;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class SerializationUtilTest extends AbstractSnapshotTest {
    @Test
    void testToJson() {
        CreateRoleRequestDto request =
                new CreateRoleRequestDto().setName("test").setMentionable(true).setIcon(null);

        byte[] json = SerializationUtil.toJson(request);

        assertThat(SerializationUtil.fromJson(CreateRoleRequestDto.class, json))
                .usingRecursiveComparison()
                .ignoringActualNullFields()
                .isEqualTo(request);

        assertWithSnapshot(DataObject.fromJson(json));
    }

    @Test
    void testJacksonRequestBody() throws Exception {
        CreateRoleRequestDto original = new CreateRoleRequestDto().setName("Test role");
        JacksonRequestBody body = new JacksonRequestBody(original);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Sink sink = Okio.sink(outputStream)) {
            body.writeTo(Okio.buffer(sink));
        }

        CreateRoleRequestDto output =
                SerializationUtil.fromJson(CreateRoleRequestDto.class, outputStream.toByteArray());

        assertThat(output).usingRecursiveComparison().ignoringActualNullFields().isEqualTo(original);
    }
}
