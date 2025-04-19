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

package net.dv8tion.jda.test.entities;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.test.PrettyRepresentation;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageSerializationTest
{
    private static final String DESCRIPTION_TEXT = "Description Text";
    private static final String TITLE_TEXT = "Title Text";
    private static final String TITLE_URL = "https://example.com/title";
    private static final String AUTHOR_TEXT = "Author Text";
    private static final String AUTHOR_URL = "https://example.com/author";
    private static final String AUTHOR_ICON = "https://example.com/author_icon";
    private static final String FOOTER_TEXT = "Footer Text";
    private static final String FOOTER_ICON = "https://example.com/footer_icon";
    private static final String IMAGE_URL = "https://example.com/image";
    private static final String THUMBNAIL_URL = "https://example.com/thumbnail";
    private static final String FIELD_1_NAME = "Field 1";
    private static final String FIELD_1_TEXT = "Field 1 Text";
    private static final String FIELD_2_NAME = "Field 2";
    private static final String FIELD_2_TEXT = "Field 2 Text";
    private static final String FIELD_3_NAME = "Field 3";
    private static final String FIELD_3_TEXT = "Field 3 Text";

    @Test
    void testEmbedSerialization()
    {
        MessageEmbed embed = getTestEmbed();

        MessageEmbed dataEmbed = EmbedBuilder.fromData(embed.toData()).build();

        assertThat(dataEmbed).isNotSameAs(embed);
        assertThat(dataEmbed).isEqualTo(embed);

        assertThat(dataEmbed.toData())
            .withRepresentation(new PrettyRepresentation())
            .isEqualTo(DataObject.empty()
                .put("title", TITLE_TEXT)
                .put("url", TITLE_URL)
                .put("description", DESCRIPTION_TEXT)
                .put("image", DataObject.empty()
                    .put("url", IMAGE_URL))
                .put("thumbnail", DataObject.empty()
                    .put("url", THUMBNAIL_URL))
                .put("footer", DataObject.empty()
                    .put("icon_url", FOOTER_ICON)
                    .put("text", FOOTER_TEXT))
                .put("author", DataObject.empty()
                    .put("icon_url", AUTHOR_ICON)
                    .put("name", AUTHOR_TEXT)
                    .put("url", AUTHOR_URL))
                .put("fields", DataArray.empty()
                    .add(DataObject.empty()
                        .put("inline", true)
                        .put("name", FIELD_1_NAME)
                        .put("value", FIELD_1_TEXT))
                    .add(DataObject.empty()
                        .put("inline", false)
                        .put("name", FIELD_2_NAME)
                        .put("value", FIELD_2_TEXT))
                    .add(DataObject.empty()
                        .put("inline", true)
                        .put("name", FIELD_3_NAME)
                        .put("value", FIELD_3_TEXT))));
    }

    @NotNull
    private static MessageEmbed getTestEmbed()
    {
        return new EmbedBuilder()
                .setDescription(DESCRIPTION_TEXT)
                .setTitle(TITLE_TEXT, TITLE_URL)
                .setAuthor(AUTHOR_TEXT, AUTHOR_URL, AUTHOR_ICON)
                .setFooter(FOOTER_TEXT, FOOTER_ICON)
                .setImage(IMAGE_URL)
                .setThumbnail(THUMBNAIL_URL)
                .addField(FIELD_1_NAME, FIELD_1_TEXT, true)
                .addField(FIELD_2_NAME, FIELD_2_TEXT, false)
                .addField(FIELD_3_NAME, FIELD_3_TEXT, true)
                .build();
    }
}
