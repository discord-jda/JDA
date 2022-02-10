package net.dv8tion.jda.api.interactions.modals;

import net.dv8tion.jda.api.utils.data.DataObject;

public class TextInputMapping
{
    private final String id;
    private final String value;

    public TextInputMapping(DataObject object)
    {
        this.id = object.getString("custom_id");
        this.value = object.getString("value");
    }

    public String getId()
    {
        return id;
    }

    public String getValue()
    {
        return value;
    }
}
