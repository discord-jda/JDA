package net.dv8tion.jda.internal.interactions.component;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.action_row.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.container.Container;
import net.dv8tion.jda.api.interactions.components.file.File;
import net.dv8tion.jda.api.interactions.components.media_gallery.MediaGallery;
import net.dv8tion.jda.api.interactions.components.section.Section;
import net.dv8tion.jda.api.interactions.components.selects.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selects.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.separator.Separator;
import net.dv8tion.jda.api.interactions.components.text_display.TextDisplay;
import net.dv8tion.jda.api.interactions.components.text_input.TextInput;
import net.dv8tion.jda.api.interactions.components.thumbnail.Thumbnail;
import net.dv8tion.jda.internal.utils.UnionUtil;

import javax.annotation.Nonnull;

public abstract class AbstractComponentImpl
{

    // -- Union hooks --

    @Nonnull
    public ActionRow asActionRow()
    {
        return toComponentType(ActionRow.class);
    }

    @Nonnull
    public Button asButton()
    {
        return toComponentType(Button.class);
    }

    @Nonnull
    public StringSelectMenu asStringSelect()
    {
        return toComponentType(StringSelectMenu.class);
    }

    @Nonnull
    public EntitySelectMenu asEntitySelect()
    {
        return toComponentType(EntitySelectMenu.class);
    }

    @Nonnull
    public TextInput asTextInput()
    {
        return toComponentType(TextInput.class);
    }

    @Nonnull
    public Section asSection()
    {
        return toComponentType(Section.class);
    }

    @Nonnull
    public TextDisplay asTextDisplay()
    {
        return toComponentType(TextDisplay.class);
    }

    @Nonnull
    public MediaGallery asMediaGallery()
    {
        return toComponentType(MediaGallery.class);
    }

    @Nonnull
    public Thumbnail asThumbnail() {
        return toComponentType(Thumbnail.class);
    }

    @Nonnull
    public Separator asSeparator()
    {
        return toComponentType(Separator.class);
    }

    @Nonnull
    public File asFile()
    {
        return toComponentType(File.class);
    }

    @Nonnull
    public Container asContainer()
    {
        return toComponentType(Container.class);
    }

    protected <T extends Component> T toComponentType(Class<T> type) {
        return UnionUtil.safeUnionCast("component", this, type);
    }
}
