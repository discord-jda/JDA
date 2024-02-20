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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MessageSerializationTest
{

    private static MessageEmbed getTestEmbed()
    {
        return new EmbedBuilder()
        .setDescription("Description Text")
        .setTitle("Title Text", "https://example.com/title")
        .setAuthor("Author Text", "https://example.com/author", "https://example.com/author_icon")
        .setFooter("Footer Text", "https://example.com/footer_icon")
        .setImage("https://example.com/image")
        .setThumbnail("https://example.com/thumbnail")
        .addField("Field 1", "Field 1 Text", true)
        .addField("Field 2", "Field 2 Text", false)
        .addField("Field 3", "Field 3 Text", true).build();
    }

    @Test
    void testEmbedSerialization()
    {
        MessageEmbed embed = getTestEmbed();

        MessageEmbed dataEmbed = EmbedBuilder.fromData(embed.toData()).build();

        assertEquals(embed.getType(), dataEmbed.getType());
        assertEquals(EmbedType.RICH, embed.getType());

        assertEquals(embed.getDescription(), dataEmbed.getDescription());
        assertEquals(embed.getTitle(), dataEmbed.getTitle());
        assertEquals(embed.getUrl(), dataEmbed.getUrl());
        assertEquals(embed.getAuthor(), dataEmbed.getAuthor());
        assertEquals(embed.getFooter(), dataEmbed.getFooter());
        assertEquals(embed.getImage(), dataEmbed.getImage());
        assertEquals(embed.getThumbnail(), dataEmbed.getThumbnail());
        assertEquals(embed.getFields(), dataEmbed.getFields());

        assertEquals(embed, dataEmbed);

        assertEquals("Description Text", dataEmbed.getDescription());
        assertEquals("Title Text", dataEmbed.getTitle());
        assertEquals("https://example.com/title", dataEmbed.getUrl());
        assertEquals("Author Text", dataEmbed.getAuthor().getName());
        assertEquals("https://example.com/author", dataEmbed.getAuthor().getUrl());
        assertEquals("https://example.com/author_icon", dataEmbed.getAuthor().getIconUrl());
        assertEquals("Footer Text", dataEmbed.getFooter().getText());
        assertEquals("https://example.com/footer_icon", dataEmbed.getFooter().getIconUrl());
        assertEquals("https://example.com/image", dataEmbed.getImage().getUrl());
        assertEquals("https://example.com/thumbnail", dataEmbed.getThumbnail().getUrl());
        assertEquals(3, dataEmbed.getFields().size());
        assertEquals("Field 1", dataEmbed.getFields().get(0).getName());
        assertEquals("Field 1 Text", dataEmbed.getFields().get(0).getValue());
        assertTrue(dataEmbed.getFields().get(0).isInline());
        assertEquals("Field 2", dataEmbed.getFields().get(1).getName());
        assertEquals("Field 2 Text", dataEmbed.getFields().get(1).getValue());
        assertFalse(dataEmbed.getFields().get(1).isInline());
        assertEquals("Field 3", dataEmbed.getFields().get(2).getName());
        assertEquals("Field 3 Text", dataEmbed.getFields().get(2).getValue());
        assertTrue(dataEmbed.getFields().get(2).isInline());
    }
}
