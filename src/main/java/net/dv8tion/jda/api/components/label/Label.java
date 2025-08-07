package net.dv8tion.jda.api.components.label;

import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponent;
import net.dv8tion.jda.internal.components.label.LabelImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Label extends ModalTopLevelComponent
{
    @Nonnull
    static Label of(@Nonnull String label, @Nullable String description, @Nonnull LabelChildComponent child)
    {
        return LabelImpl.of(label, description, child);
    }

    @Nonnull
    String getLabel();

    @Nullable
    String getDescription();

    @Nonnull
    LabelChildComponentUnion getChild();

    @Nonnull
    @Override
    @CheckReturnValue
    Label withUniqueId(int uniqueId);
}
