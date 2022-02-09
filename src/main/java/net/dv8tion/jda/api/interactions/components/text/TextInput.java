package net.dv8tion.jda.api.interactions.components.text;

import net.dv8tion.jda.api.interactions.components.ActionComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface TextInput extends ActionComponent
{
    @Nonnull
    TextInputStyle getStyle();

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

    @Override
    default boolean isDisabled()
    {
        return false;
    }

    @NotNull
    @Override
    default ActionComponent withDisabled(boolean disabled)
    {
        throw new UnsupportedOperationException("Text Inputs cannot be disabled!");
    }
}
