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

package net.dv8tion.jda.entities;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.util.PrettyRepresentation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageSerializationTest
{
    @Test
    void testEmbedSerialization()
    {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription("Description Text");
        builder.setTitle("Title Text", "https://example.com/title");
        builder.setAuthor("Author Text", "https://example.com/author", "https://example.com/author_icon");
        builder.setFooter("Footer Text", "https://example.com/footer_icon");
        builder.setImage("https://example.com/image");
        builder.setThumbnail("https://example.com/thumbnail");
        builder.addField("Field 1", "Field 1 Text", true);
        builder.addField("Field 2", "Field 2 Text", false);
        builder.addField("Field 3", "Field 3 Text", true);

        MessageEmbed embed = builder.build();

        MessageEmbed dataEmbed = EmbedBuilder.fromData(embed.toData()).build();

        assertThat(dataEmbed).isNotSameAs(embed);
        assertThat(dataEmbed).isEqualTo(embed);

        assertThat(dataEmbed.toData())
            .withRepresentation(new PrettyRepresentation())
            .isEqualTo(DataObject.empty()
                .put("title", "Title Text")
                .put("url", "https://example.com/title")
                .put("description", "Description Text")
                .put("image", DataObject.empty()
                    .put("url", "https://example.com/image"))
                .put("thumbnail", DataObject.empty()
                    .put("url", "https://example.com/thumbnail"))
                .put("footer", DataObject.empty()
                    .put("icon_url", "https://example.com/footer_icon")
                    .put("text", "Footer Text"))
                .put("author", DataObject.empty()
                    .put("icon_url", "https://example.com/author_icon")
                    .put("name", "Author Text")
                    .put("url", "https://example.com/author"))
                .put("fields", DataArray.empty()
                    .add(DataObject.empty()
                        .put("inline", true)
                        .put("name", "Field 1")
                        .put("value", "Field 1 Text"))
                    .add(DataObject.empty()
                        .put("inline", false)
                        .put("name", "Field 2")
                        .put("value", "Field 2 Text"))
                    .add(DataObject.empty()
                        .put("inline", true)
                        .put("name", "Field 3")
                        .put("value", "Field 3 Text"))));
    }
}
