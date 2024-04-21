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

package net.dv8tion.jda.test.entitystring;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.internal.utils.EntityString;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EntityStringTest
{
    @Test
    @Order(1)
    void testSimple()
    {
        assertThat(new EntityString(new AnEntity()))
            .hasToString("AnEntity");
        assertThat(new EntityString(new AnEntity()).setName("AName"))
            .hasToString("AnEntity:AName");
    }

    @Test
    @Order(2)
    void testClassNameAsString()
    {
        assertThat(new EntityString("NotAnEntity"))
            .hasToString("NotAnEntity");
        assertThat(new EntityString("NotAnEntity").setName("AName"))
            .hasToString("NotAnEntity:AName");
    }

    @Test
    @Order(3)
    void testType()
    {
        assertThat(new EntityString(new AnEntity()).setType("AType"))
            .hasToString("AnEntity[AType]");
        assertThat(new EntityString(new AnEntity()).setType("AType").setName("AName"))
            .hasToString("AnEntity[AType]:AName");
        assertThat(new EntityString(new AnEntity()).setType(ChannelType.NEWS).setName("AName"))
            .hasToString("AnEntity[NEWS]:AName");
    }

    @Test
    @Order(4)
    void testMetadata()
    {
        assertThat(new EntityString(new AnEntity()).addMetadata(null, "Metadata1"))
            .hasToString("AnEntity(Metadata1)");
        assertThat(new EntityString(new AnEntity()).addMetadata("MetaKey", "Metadata1"))
            .hasToString("AnEntity(MetaKey=Metadata1)");
        assertThat(new EntityString(new AnEntity()).addMetadata("MetaKey", 42))
            .hasToString("AnEntity(MetaKey=42)");
        assertThat(new EntityString(new AnEntity())
                .addMetadata("MetaKey1", "Metadata1")
                .addMetadata("MetaKey2", "Metadata2"))
            .hasToString("AnEntity(MetaKey1=Metadata1, MetaKey2=Metadata2)");
    }

    @Test
    @Order(5)
    void testAll()
    {
        assertThat(new EntityString(new AnEntity())
                .setName("AName")
                .addMetadata(null, "Metadata1"))
            .hasToString("AnEntity:AName(Metadata1)");
        assertThat(new EntityString(new AnEntity())
                .setName("AName")
                .addMetadata("MetaKey", "Metadata1"))
            .hasToString("AnEntity:AName(MetaKey=Metadata1)");
        assertThat(new EntityString(new AnEntity())
                .setName("AName")
                .addMetadata("MetaKey", 42))
            .hasToString("AnEntity:AName(MetaKey=42)");
        assertThat(new EntityString(new AnEntity())
                .setName("AName")
                .addMetadata("MetaKey1", "Metadata1")
                .addMetadata("MetaKey2", "Metadata2"))
            .hasToString("AnEntity:AName(MetaKey1=Metadata1, MetaKey2=Metadata2)");

        assertThat(new EntityString(new AnEntity())
                .setName("AName")
                .setType("Type")
                .addMetadata(null, "Metadata1"))
            .hasToString("AnEntity[Type]:AName(Metadata1)");
        assertThat(new EntityString(new AnEntity())
                .setName("AName")
                .setType("Type")
                .addMetadata("MetaKey", "Metadata1"))
            .hasToString("AnEntity[Type]:AName(MetaKey=Metadata1)");
        assertThat(new EntityString(new AnEntity())
                .setName("AName")
                .setType("Type")
                .addMetadata("MetaKey", 42))
            .hasToString("AnEntity[Type]:AName(MetaKey=42)");
        assertThat(new EntityString(new AnEntity())
                .setName("AName")
                .setType("Type")
                .addMetadata("MetaKey1", "Metadata1")
                .addMetadata("MetaKey2", "Metadata2"))
            .hasToString("AnEntity[Type]:AName(MetaKey1=Metadata1, MetaKey2=Metadata2)");
    }

    @Test
    @Order(6)
    void testSimpleSnowflake()
    {
        assertThat(new EntityString(new ASnowflake()))
            .hasToString("ASnowflake(id=42)");
        assertThat(new EntityString(new ASnowflake()).setName("AName"))
            .hasToString("ASnowflake:AName(id=42)");
    }

    @Test
    @Order(7)
    void testTypeSnowflake()
    {
        assertThat(new EntityString(new ASnowflake()).setType("AType"))
            .hasToString("ASnowflake[AType](id=42)");
        assertThat(new EntityString(new ASnowflake()).setType("AType").setName("AName"))
            .hasToString("ASnowflake[AType]:AName(id=42)");
        assertThat(new EntityString(new ASnowflake()).setType(ChannelType.NEWS).setName("AName"))
            .hasToString("ASnowflake[NEWS]:AName(id=42)");
    }

    @Test
    @Order(8)
    void testMetadataSnowflake()
    {
        assertThat(new EntityString(new ASnowflake()).addMetadata(null, "Metadata1"))
            .hasToString("ASnowflake(id=42, Metadata1)");
        assertThat(new EntityString(new ASnowflake()).addMetadata("MetaKey", "Metadata1"))
            .hasToString("ASnowflake(id=42, MetaKey=Metadata1)");
        assertThat(new EntityString(new ASnowflake()).addMetadata("MetaKey", 42))
            .hasToString("ASnowflake(id=42, MetaKey=42)");
        assertThat(new EntityString(new ASnowflake())
                .addMetadata("MetaKey1", "Metadata1")
                .addMetadata("MetaKey2", "Metadata2"))
            .hasToString("ASnowflake(id=42, MetaKey1=Metadata1, MetaKey2=Metadata2)");
    }

    @Test
    @Order(9)
    void testAllSnowflake()
    {
        assertThat(new EntityString(new ASnowflake())
                .setName("AName")
                .addMetadata(null, "Metadata1"))
            .hasToString("ASnowflake:AName(id=42, Metadata1)");
        assertThat(new EntityString(new ASnowflake())
                .setName("AName")
                .addMetadata("MetaKey", "Metadata1"))
            .hasToString("ASnowflake:AName(id=42, MetaKey=Metadata1)");
        assertThat(new EntityString(new ASnowflake())
                .setName("AName")
                .addMetadata("MetaKey", 42))
            .hasToString("ASnowflake:AName(id=42, MetaKey=42)");
        assertThat(new EntityString(new ASnowflake())
                .setName("AName")
                .addMetadata("MetaKey1", "Metadata1")
                .addMetadata("MetaKey2", "Metadata2"))
            .hasToString("ASnowflake:AName(id=42, MetaKey1=Metadata1, MetaKey2=Metadata2)");

        assertThat(new EntityString(new ASnowflake())
                .setName("AName")
                .setType("Type")
                .addMetadata(null, "Metadata1"))
            .hasToString("ASnowflake[Type]:AName(id=42, Metadata1)");
        assertThat(new EntityString(new ASnowflake())
                .setName("AName")
                .setType("Type")
                .addMetadata("MetaKey", "Metadata1"))
            .hasToString("ASnowflake[Type]:AName(id=42, MetaKey=Metadata1)");
        assertThat(new EntityString(new ASnowflake())
                .setName("AName")
                .setType("Type")
                .addMetadata("MetaKey", 42))
            .hasToString("ASnowflake[Type]:AName(id=42, MetaKey=42)");
        assertThat(new EntityString(new ASnowflake())
                .setName("AName")
                .setType("Type")
                .addMetadata("MetaKey1", "Metadata1")
                .addMetadata("MetaKey2", "Metadata2"))
            .hasToString("ASnowflake[Type]:AName(id=42, MetaKey1=Metadata1, MetaKey2=Metadata2)");
    }
}
