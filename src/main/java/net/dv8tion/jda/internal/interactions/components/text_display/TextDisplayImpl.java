package net.dv8tion.jda.internal.interactions.components.text_display;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.interactions.components.section.SectionContentComponentUnion;
import net.dv8tion.jda.api.interactions.components.text_display.TextDisplay;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.AbstractComponentImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;

public class TextDisplayImpl
        extends AbstractComponentImpl
        implements TextDisplay, MessageTopLevelComponentUnion, ContainerChildComponentUnion, SectionContentComponentUnion
{
    private final int uniqueId;
    private final String content;

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
    public DataObject toData()
    {
        final DataObject json = DataObject.empty()
                .put("type", getType().getKey())
                .put("content", content);
        if (uniqueId >= 0)
            json.put("id", uniqueId);
        return json;
    }

    @Nonnull
    @Override
    public TextDisplay withUniqueId(int uniqueId)
    {
        Checks.notNegative(uniqueId, "Unique ID");
        return new TextDisplayImpl(uniqueId, content);
    }

    @Override
    public int getUniqueId()
    {
        return uniqueId;
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.TEXT_DISPLAY;
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

    @Override
    public String getContentRaw()
    {
        return content;
    }

    @Override
    public String getContentDisplay(Message message)
    {
        return content;
    }

    @Override
    public String getContentStripped()
    {
        return content;
    }
}
