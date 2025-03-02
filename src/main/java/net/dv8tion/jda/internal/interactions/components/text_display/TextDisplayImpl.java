package net.dv8tion.jda.internal.interactions.components.text_display;

import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.interactions.components.section.SectionContentComponentUnion;
import net.dv8tion.jda.api.interactions.components.text_display.TextDisplay;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.AbstractComponentImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import java.util.Objects;

public class TextDisplayImpl
        extends AbstractComponentImpl
        implements TextDisplay, MessageTopLevelComponentUnion, ContainerChildComponentUnion, SectionContentComponentUnion
{
    private final int uniqueId;
    private final String content;

    public TextDisplayImpl(DataObject data)
    {
        this(
                data.getInt("id"),
                data.getString("content")
        );
    }

    public TextDisplayImpl(String content)
    {
        this(-1, content);
    }

    private TextDisplayImpl(int uniqueId, String content)
    {
        this.content = content;
        this.uniqueId = uniqueId;
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.TEXT_DISPLAY;
    }

    @Nonnull
    @Override
    public TextDisplay withUniqueId(int uniqueId)
    {
        Checks.notNegative(uniqueId, "Unique ID");
        return new TextDisplayImpl(uniqueId, content);
    }

    @Nonnull
    @Override
    public TextDisplay withContent(@Nonnull String content)
    {
        Checks.notNull(content, "Content");
        return new TextDisplayImpl(uniqueId, content);
    }

    @Override
    public int getUniqueId()
    {
        return uniqueId;
    }

    @Nonnull
    @Override
    public String getContent()
    {
        return content;
    }

    @Override
    public boolean isMessageCompatible()
    {
        return true;
    }

    @Override
    public boolean isModalCompatible()
    {
        return false;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        final DataObject json = DataObject.empty()
                .put("type", getType().getKey())
                .put("content", content);
        if (uniqueId >= 0)
            json.put("id", uniqueId);
        return json;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof TextDisplayImpl)) return false;
        TextDisplayImpl that = (TextDisplayImpl) o;
        return uniqueId == that.uniqueId && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(uniqueId, content);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("id", uniqueId)
                .addMetadata("content", content)
                .toString();
    }
}
