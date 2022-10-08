package net.dv8tion.jda;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.internal.utils.EntityString;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EntityStringTest
{
    @Test
    @Order(1)
    public void testSimple()
    {
        assertEquals("AnEntity", new EntityString(new AnEntity()).toString());
        assertEquals("AnEntity:AName", new EntityString(new AnEntity()).setName("AName").toString());
    }

    @Test
    @Order(2)
    public void testType()
    {
        assertEquals("AnEntity[AType]", new EntityString(new AnEntity()).setType("AType").toString());
        assertEquals("AnEntity[AType]:AName", new EntityString(new AnEntity()).setType("AType").setName("AName").toString());
        assertEquals("AnEntity[NEWS]:AName", new EntityString(new AnEntity()).setType(ChannelType.NEWS).setName("AName").toString());
    }

    @Test
    @Order(3)
    public void testMetadata()
    {
        assertEquals("AnEntity(Metadata1)", new EntityString(new AnEntity()).addMetadata(null, "Metadata1").toString());
        assertEquals("AnEntity(MetaKey=Metadata1)", new EntityString(new AnEntity()).addMetadata("MetaKey", "Metadata1").toString());
        assertEquals("AnEntity(MetaKey=42)", new EntityString(new AnEntity()).addMetadata("MetaKey", 42).toString());
        assertEquals("AnEntity(MetaKey1=Metadata1, MetaKey2=Metadata2)", new EntityString(new AnEntity())
                .addMetadata("MetaKey1", "Metadata1")
                .addMetadata("MetaKey2", "Metadata2")
                .toString());
    }

    @Test
    @Order(4)
    public void testAll()
    {
        assertEquals("AnEntity:AName(Metadata1)", new EntityString(new AnEntity())
                .setName("AName")
                .addMetadata(null, "Metadata1")
                .toString());
        assertEquals("AnEntity:AName(MetaKey=Metadata1)", new EntityString(new AnEntity())
                .setName("AName")
                .addMetadata("MetaKey", "Metadata1")
                .toString());
        assertEquals("AnEntity:AName(MetaKey=42)", new EntityString(new AnEntity())
                .setName("AName")
                .addMetadata("MetaKey", 42)
                .toString());
        assertEquals("AnEntity:AName(MetaKey1=Metadata1, MetaKey2=Metadata2)", new EntityString(new AnEntity())
                .setName("AName")
                .addMetadata("MetaKey1", "Metadata1")
                .addMetadata("MetaKey2", "Metadata2")
                .toString());

        assertEquals("AnEntity[Type]:AName(Metadata1)", new EntityString(new AnEntity())
                .setName("AName")
                .setType("Type")
                .addMetadata(null, "Metadata1")
                .toString());
        assertEquals("AnEntity[Type]:AName(MetaKey=Metadata1)", new EntityString(new AnEntity())
                .setName("AName")
                .setType("Type")
                .addMetadata("MetaKey", "Metadata1")
                .toString());
        assertEquals("AnEntity[Type]:AName(MetaKey=42)", new EntityString(new AnEntity())
                .setName("AName")
                .setType("Type")
                .addMetadata("MetaKey", 42)
                .toString());
        assertEquals("AnEntity[Type]:AName(MetaKey1=Metadata1, MetaKey2=Metadata2)", new EntityString(new AnEntity())
                .setName("AName")
                .setType("Type")
                .addMetadata("MetaKey1", "Metadata1")
                .addMetadata("MetaKey2", "Metadata2")
                .toString());
    }

    @Test
    @Order(5)
    public void testSimpleSnowflake()
    {
        assertEquals("ASnowflake(id=42)", new EntityString(new ASnowflake()).toString());
        assertEquals("ASnowflake:AName(id=42)", new EntityString(new ASnowflake()).setName("AName").toString());
    }

    @Test
    @Order(6)
    public void testTypeSnowflake()
    {
        assertEquals("ASnowflake[AType](id=42)", new EntityString(new ASnowflake()).setType("AType").toString());
        assertEquals("ASnowflake[AType]:AName(id=42)", new EntityString(new ASnowflake()).setType("AType").setName("AName").toString());
        assertEquals("ASnowflake[NEWS]:AName(id=42)", new EntityString(new ASnowflake()).setType(ChannelType.NEWS).setName("AName").toString());
    }

    @Test
    @Order(7)
    public void testMetadataSnowflake()
    {
        assertEquals("ASnowflake(id=42, Metadata1)", new EntityString(new ASnowflake()).addMetadata(null, "Metadata1").toString());
        assertEquals("ASnowflake(id=42, MetaKey=Metadata1)", new EntityString(new ASnowflake()).addMetadata("MetaKey", "Metadata1").toString());
        assertEquals("ASnowflake(id=42, MetaKey=42)", new EntityString(new ASnowflake()).addMetadata("MetaKey", 42).toString());
        assertEquals("ASnowflake(id=42, MetaKey1=Metadata1, MetaKey2=Metadata2)", new EntityString(new ASnowflake())
                .addMetadata("MetaKey1", "Metadata1")
                .addMetadata("MetaKey2", "Metadata2")
                .toString());
    }

    @Test
    @Order(8)
    public void testAllSnowflake()
    {
        assertEquals("ASnowflake:AName(id=42, Metadata1)", new EntityString(new ASnowflake())
                .setName("AName")
                .addMetadata(null, "Metadata1")
                .toString());
        assertEquals("ASnowflake:AName(id=42, MetaKey=Metadata1)", new EntityString(new ASnowflake())
                .setName("AName")
                .addMetadata("MetaKey", "Metadata1")
                .toString());
        assertEquals("ASnowflake:AName(id=42, MetaKey=42)", new EntityString(new ASnowflake())
                .setName("AName")
                .addMetadata("MetaKey", 42)
                .toString());
        assertEquals("ASnowflake:AName(id=42, MetaKey1=Metadata1, MetaKey2=Metadata2)", new EntityString(new ASnowflake())
                .setName("AName")
                .addMetadata("MetaKey1", "Metadata1")
                .addMetadata("MetaKey2", "Metadata2")
                .toString());

        assertEquals("ASnowflake[Type]:AName(id=42, Metadata1)", new EntityString(new ASnowflake())
                .setName("AName")
                .setType("Type")
                .addMetadata(null, "Metadata1")
                .toString());
        assertEquals("ASnowflake[Type]:AName(id=42, MetaKey=Metadata1)", new EntityString(new ASnowflake())
                .setName("AName")
                .setType("Type")
                .addMetadata("MetaKey", "Metadata1")
                .toString());
        assertEquals("ASnowflake[Type]:AName(id=42, MetaKey=42)", new EntityString(new ASnowflake())
                .setName("AName")
                .setType("Type")
                .addMetadata("MetaKey", 42)
                .toString());
        assertEquals("ASnowflake[Type]:AName(id=42, MetaKey1=Metadata1, MetaKey2=Metadata2)", new EntityString(new ASnowflake())
                .setName("AName")
                .setType("Type")
                .addMetadata("MetaKey1", "Metadata1")
                .addMetadata("MetaKey2", "Metadata2")
                .toString());
    }
}
