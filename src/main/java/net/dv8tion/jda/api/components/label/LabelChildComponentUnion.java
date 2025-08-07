package net.dv8tion.jda.api.components.label;

import net.dv8tion.jda.api.components.IComponentUnion;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textinput.TextInput;

import javax.annotation.Nonnull;

public interface LabelChildComponentUnion extends LabelChildComponent, IComponentUnion
{
    @Nonnull
    TextInput asTextInput();

    @Nonnull
    StringSelectMenu asStringSelectMenu();

    @Nonnull
    @Override
    LabelChildComponentUnion withUniqueId(int uniqueId);
}
