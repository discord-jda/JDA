package net.dv8tion.jda.api.interactions.components.text;

import net.dv8tion.jda.api.interactions.components.ItemComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface TextInput extends ItemComponent
{
    @Nonnull
    String getId();

    @Nonnull
    String getLabel();

    int getMinLength();

    int getMaxLength();

    boolean isRequired();

    @Nullable
    String getValue();

    @Nullable
    String getPlaceHolder();
}
