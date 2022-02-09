package net.dv8tion.jda.internal.interactions.component;

import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.Modal;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ModalImpl implements Modal
{
    private final String id;
    private final String title;
    private final List<ActionRow> components;

    public ModalImpl(DataObject object)
    {
        this.id = object.getString("id");

        this.title = object.getString("title");

        this.components = object.getArray("components")
                        .stream(DataArray::getObject)
                        .map(ActionRow::fromData)
                        .collect(Collectors.toList());
    }

    public ModalImpl(String id, String title, List<ActionRow> components)
    {
        this.id = id;
        this.title = title;
        this.components = Collections.unmodifiableList(components);
    }

    @Override
    public boolean isDisabled()
    {
        return false;
    }

    @NotNull
    @Override
    public Type getType()
    {
        return Type.ACTION_ROW;
    }

    @NotNull
    @Override
    public String getId()
    {
        return id;
    }

    @NotNull
    @Override
    public String getTitle()
    {
        return title;
    }

    @NotNull
    @Override
    public List<ActionRow> getComponents()
    {
        return Collections.unmodifiableList(components);
    }

    @NotNull
    @Override
    public DataObject toData()
    {
        DataObject object = DataObject.empty()
                .put("custom_id", id)
                .put("title", title);

        DataArray componentsArray = DataArray.empty();

        components.stream()
                .map(ActionRow::toData)
                .forEach(componentsArray::add);

        object.put("components", componentsArray);

        return object;
    }
}
