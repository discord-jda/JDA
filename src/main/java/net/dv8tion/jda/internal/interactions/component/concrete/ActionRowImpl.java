package net.dv8tion.jda.internal.interactions.component.concrete;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.components.action_row.ActionRow;
import net.dv8tion.jda.api.interactions.components.action_row.ActionRowChildComponent;
import net.dv8tion.jda.api.interactions.components.action_row.ActionRowChildComponentUnion;
import net.dv8tion.jda.api.interactions.components.attribute.IDisableable;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.interactions.modals.ModalTopLevelComponentUnion;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.AbstractComponentImpl;
import net.dv8tion.jda.internal.interactions.components.replacer.IReplacerAware;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.UnionUtil;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ActionRowImpl extends AbstractComponentImpl implements ActionRow, MessageTopLevelComponentUnion, ModalTopLevelComponentUnion, ContainerChildComponentUnion, IReplacerAware<ActionRow>
{
    private final int uniqueId;
    private final List<ActionRowChildComponentUnion> components;

    private ActionRowImpl() {
        this.uniqueId = -1;
        this.components = new ArrayList<>();
    }

    private ActionRowImpl(List<ActionRowChildComponentUnion> components, int uniqueId) {
        this.uniqueId = uniqueId;
        this.components = new ArrayList<>(components);
    }

    @Nonnull
    public static ActionRowImpl fromData(@Nonnull DataObject data)
    {
        Checks.notNull(data, "Data");
        if (data.getInt("type", 0) != Type.ACTION_ROW.getKey())
            throw new IllegalArgumentException("Data has incorrect type. Expected: " + Type.ACTION_ROW.getKey() + " Found: " + data.getInt("type"));
        List<ActionRowChildComponentUnion> components = ActionRowChildComponentUnion.fromData(data.getArray("components"));

        return new ActionRowImpl(
                components,
                // Absent in modals
                data.getInt("id", -1)
        );
    }

    @Nonnull
    public static ActionRow of(@Nonnull Collection<? extends ActionRowChildComponent> _components)
    {
        Collection<ActionRowChildComponentUnion> components = UnionUtil.membersToUnion(_components);
        Checks.noneNull(components, "Components");

        Checks.check(!components.isEmpty(), "Cannot have empty row!");

        ActionRowImpl row = new ActionRowImpl();
        row.components.addAll(components);
        if (!row.isValid())
        {
            Map<Component.Type, List<ActionRowChildComponentUnion>> grouped = components.stream().collect(Collectors.groupingBy(Component::getType));
            String provided = grouped.entrySet()
                    .stream()
                    .map(entry -> entry.getValue().size() + "/" + ActionRow.getMaxAllowed(entry.getKey()) + " of " + entry.getKey())
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Cannot create action row with invalid component combinations. Provided: " + provided);
        }
        return row;
    }

    @Nonnull
    public static List<ActionRow> partitionOf(@Nonnull Collection<? extends ActionRowChildComponent> _components)
    {
        Collection<ActionRowChildComponentUnion> components = UnionUtil.membersToUnion(_components);
        Checks.noneNull(components, "Components");

        List<ActionRow> rows = new ArrayList<>();
        // The current action row we are building
        List<ActionRowChildComponentUnion> currentRow = null;
        // The component types contained in that row (for now it can't have mixed types)
        Component.Type type = null;

        for (ActionRowChildComponentUnion current : components)
        {
            if (type != current.getType() || currentRow.size() == ActionRow.getMaxAllowed(type))
            {
                type = current.getType();
                ActionRowImpl row = (ActionRowImpl) ActionRow.of(current);
                currentRow = row.components;
                rows.add(row);
            }
            else
            {
                currentRow.add(current);
            }
        }

        return rows;
    }

    @Override
    public int getUniqueId()
    {
        return uniqueId;
    }

    @Nonnull
    @Override
    public List<ActionRowChildComponentUnion> getComponents()
    {
        return components;
    }

    @Override
    public ActionRow replace(ComponentReplacer replacer)
    {
        return IReplacerAware.doReplace(
                ActionRowChildComponent.class,
                getComponents(),
                replacer,
                newComponents -> new ActionRowImpl(newComponents, uniqueId)
        );
    }

    @Nonnull
    @Override
    public ActionRow withUniqueId(int uniqueId)
    {
        Checks.notNegative(uniqueId, "Unique ID");
        return new ActionRowImpl(getComponents(), uniqueId);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ActionRow withDisabled(boolean disabled)
    {
        return of(
            components.stream()
                .map(c -> {
                    if (c instanceof IDisableable)
                        return (ActionRowChildComponentUnion) ((IDisableable) c).withDisabled(disabled);
                    return c;
                })
                .collect(Collectors.toList()));
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ActionRow asDisabled()
    {
        return withDisabled(true);
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public ActionRow asEnabled()
    {
        return withDisabled(false);
    }

    @Nonnull
    @Override
    public ActionRow createCopy()
    {
        return ActionRow.of(components);
    }

    // TODO after removal, make this immutable starting from the constructor
    @Nullable
    @Override
    @Deprecated
    public ItemComponent updateComponent(@Nonnull String id, @Nullable ItemComponent newComponent)
    {
        Checks.notNull(id, "ID");

        throw new UnsupportedOperationException("To be implemented, must return old/removed component, use IFinderAware to find component and then delegate to updateComponent(ItemComponent, ItemComponent)");
    }

    @Nullable
    @Override
    @Deprecated
    public ItemComponent updateComponent(@Nonnull ItemComponent component, @Nullable ItemComponent newComponent)
    {
        Checks.notNull(component, "Component to be replaced");

        throw new UnsupportedOperationException("To be implemented, must return old/removed component, use IFinderAware to find component and IReplacerAware to replace/remove");
    }

    @Nonnull
    @Override
    public Component.Type getType()
    {
        return Component.Type.ACTION_ROW;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        final DataObject json = DataObject.empty()
                .put("type", 1)
                .put("components", DataArray.fromCollection(components));
        if (uniqueId >= 0)
            json.put("id", uniqueId);
        return json;
    }

    @Nonnull
    @Override
    public Iterator<ActionRowChildComponentUnion> iterator()
    {
        return components.iterator();
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("components", components)
                .toString();
    }

    @Override
    public int hashCode()
    {
        return components.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof ActionRowImpl))
            return false;

        return components.equals(((ActionRowImpl) obj).components);
    }
}
