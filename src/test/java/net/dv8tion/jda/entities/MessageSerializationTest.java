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
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

        Assertions.assertEquals(embed.getType(), dataEmbed.getType());
        Assertions.assertEquals(EmbedType.RICH, embed.getType());

        Assertions.assertEquals(embed.getDescription(), dataEmbed.getDescription());
        Assertions.assertEquals(embed.getTitle(), dataEmbed.getTitle());
        Assertions.assertEquals(embed.getUrl(), dataEmbed.getUrl());
        Assertions.assertEquals(embed.getAuthor(), dataEmbed.getAuthor());
        Assertions.assertEquals(embed.getFooter(), dataEmbed.getFooter());
        Assertions.assertEquals(embed.getImage(), dataEmbed.getImage());
        Assertions.assertEquals(embed.getThumbnail(), dataEmbed.getThumbnail());
        Assertions.assertEquals(embed.getFields(), dataEmbed.getFields());

        Assertions.assertEquals(embed, dataEmbed);

        Assertions.assertEquals("Description Text", dataEmbed.getDescription());
        Assertions.assertEquals("Title Text", dataEmbed.getTitle());
        Assertions.assertEquals("https://example.com/title", dataEmbed.getUrl());
        Assertions.assertEquals("Author Text", dataEmbed.getAuthor().getName());
        Assertions.assertEquals("https://example.com/author", dataEmbed.getAuthor().getUrl());
        Assertions.assertEquals("https://example.com/author_icon", dataEmbed.getAuthor().getIconUrl());
        Assertions.assertEquals("Footer Text", dataEmbed.getFooter().getText());
        Assertions.assertEquals("https://example.com/footer_icon", dataEmbed.getFooter().getIconUrl());
        Assertions.assertEquals("https://example.com/image", dataEmbed.getImage().getUrl());
        Assertions.assertEquals("https://example.com/thumbnail", dataEmbed.getThumbnail().getUrl());
        Assertions.assertEquals(3, dataEmbed.getFields().size());
        Assertions.assertEquals("Field 1", dataEmbed.getFields().get(0).getName());
        Assertions.assertEquals("Field 1 Text", dataEmbed.getFields().get(0).getValue());
        Assertions.assertTrue(dataEmbed.getFields().get(0).isInline());
        Assertions.assertEquals("Field 2", dataEmbed.getFields().get(1).getName());
        Assertions.assertEquals("Field 2 Text", dataEmbed.getFields().get(1).getValue());
        Assertions.assertFalse(dataEmbed.getFields().get(1).isInline());
        Assertions.assertEquals("Field 3", dataEmbed.getFields().get(2).getName());
        Assertions.assertEquals("Field 3 Text", dataEmbed.getFields().get(2).getValue());
        Assertions.assertTrue(dataEmbed.getFields().get(2).isInline());
    }
}
