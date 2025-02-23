package net.dv8tion.jda.internal.interactions.components.text_display;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.interactions.components.section.SectionContentComponentUnion;
import net.dv8tion.jda.api.interactions.components.text_display.TextDisplay;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.AbstractComponentImpl;

import javax.annotation.Nonnull;

public class TextDisplayImpl
        extends AbstractComponentImpl
        implements TextDisplay, MessageTopLevelComponentUnion, ContainerChildComponentUnion, SectionContentComponentUnion
{
    private final String content;

    public TextDisplayImpl(String content)
    {
        this.content = content;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return DataObject.empty()
                .put("type", getType().getKey())
                .put("content", content);
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
