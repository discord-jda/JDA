package net.dv8tion.jda.api.interactions.components.text;

import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.internal.interactions.component.TextInputImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TextInput extends ActionComponent
{
    @Nullable
    TextInputStyle getStyle();

    @NotNull
    String getId();

    @Nullable
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

    static TextInput.Builder create(@NotNull String id, @NotNull String label, @NotNull TextInputStyle style)
    {
        return new Builder(id, label, style);
    }

    class Builder
    {
        private final String id;
        private final TextInputStyle style;
        private final String label;

        private int minLength = -1;
        private int maxLength = -1;
        private String value;
        private String placeholder;
        private boolean required;

        protected Builder(String id, String label, TextInputStyle style)
        {
            this.id = id;
            this.label = label;
            this.style = style;
        }

        public Builder setRequired(boolean required)
        {
            this.required = required;
            return this;
        }

        public Builder setMinLength(int minLength)
        {
            this.minLength = minLength;
            return this;
        }

        public Builder setMaxLength(int maxLength)
        {
            this.maxLength = maxLength;
            return this;
        }

        public Builder setValue(String value)
        {
            this.value = value;
            return this;
        }

        public Builder setPlaceholder(String placeholder)
        {
            this.placeholder = placeholder;
            return this;
        }

        public int getMinLength()
        {
            return minLength;
        }

        public int getMaxLength()
        {
            return maxLength;
        }

        public String getId()
        {
            return id;
        }

        public String getLabel()
        {
            return label;
        }

        public TextInputStyle getStyle()
        {
            return style;
        }

        public String getPlaceholder()
        {
            return placeholder;
        }

        public String getValue()
        {
            return value;
        }

        public boolean isRequired()
        {
            return required;
        }

        public TextInput build()
        {
            return new TextInputImpl(id, style, label, minLength, maxLength, required, value, placeholder);
        }
    }
}
