package net.dv8tion.jda.internal.interactions.component;

import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TextInputImpl implements TextInput
{

    private final String id;
    private final int style;
    private final String label;
    private final int minLength;
    private final int maxLength;
    private final boolean required;
    private final String value;
    private final String placeholder;

    public TextInputImpl(DataObject object)
    {
        id = object.getString("custom_id");
        style = object.getInt("style");
        label = object.getString("label");
        minLength = object.getInt("min_Length", -1);
        maxLength = object.getInt("max_length", -1);
        required = object.getBoolean("required", false);
        value = object.getString("value", null);
        placeholder = object.getString("placeholder", null);
    }

    @NotNull
    @Override
    public Type getType()
    {
        return Type.TEXT_INPUT;
    }

    @NotNull
    @Override
    public String getId()
    {
        return id;
    }

    @NotNull
    @Override
    public String getLabel()
    {
        return label;
    }

    @Override
    public int getMinLength()
    {
        return minLength;
    }

    @Override
    public int getMaxLength()
    {
        return maxLength;
    }

    @Override
    public boolean isRequired()
    {
        return required;
    }

    @Nullable
    @Override
    public String getValue()
    {
        return value;
    }

    @Nullable
    @Override
    public String getPlaceHolder()
    {
        return placeholder;
    }

    @NotNull
    @Override
    public DataObject toData()
    {
        DataObject obj = DataObject.empty()
                    .put("type", 4)
                    .put("custom_id", id)
                    .put("style", style)
                    .put("label", label);
        if (minLength != -1)
            obj.put("min_length", minLength);
        if (maxLength != -1)
            obj.put("max_length", maxLength);
        if (required)
            obj.put("required", true);
        if (value != null)
            obj.put("value", value);
        if (placeholder != null)
            obj.put("placeholder", placeholder);
        return obj;
    }
}
