package net.dv8tion.jda.internal.interactions.components.container;

import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.interactions.components.container.Container;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponentUnion;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.component.AbstractComponentImpl;
import net.dv8tion.jda.internal.utils.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ContainerImpl
        extends AbstractComponentImpl
        implements Container, MessageTopLevelComponentUnion
{
    private final int uniqueId;
    private final List<ContainerChildComponentUnion> components;
    private final boolean spoiler;
    private final Integer accentColor;

    private ContainerImpl(Collection<ContainerChildComponentUnion> components)
    {
        this(-1, components, false, null);
    }

    private ContainerImpl(int uniqueId, Collection<ContainerChildComponentUnion> components, boolean spoiler, Integer accentColor)
    {
        this.uniqueId = uniqueId;
        this.components = Helpers.copyAsUnmodifiableList(components);
        this.spoiler = spoiler;
        this.accentColor = accentColor;
    }

    public static Container of(Collection<? extends ContainerChildComponent> _components)
    {
        Checks.notEmpty(_components, "Components");
        Checks.noneNull(_components, "Components");
        Checks.check(_components.size() <= MAX_COMPONENTS, "A container can only contain %d components, provided: %d", MAX_COMPONENTS, _components.size());

        final Collection<ContainerChildComponentUnion> components = UnionUtil.membersToUnion(_components);
        return new ContainerImpl(components);
    }

    public static ContainerImpl fromData(DataObject data)
    {
        Checks.notNull(data, "Data");
        if (data.getInt("type", 0) != Type.CONTAINER.getKey())
            throw new IllegalArgumentException("Data has incorrect type. Expected: " + Type.CONTAINER.getKey() + " Found: " + data.getInt("type"));

        final int uniqueId = data.getInt("id");
        final List<ContainerChildComponentUnion> components = ContainerChildComponentUnion.fromData(data.getArray("components"));
        final boolean spoiler = data.getBoolean("spoiler", false);
        final Integer accentColor = (Integer) data.opt("accent_color").orElse(null);

        return new ContainerImpl(uniqueId, components, spoiler, accentColor);
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.CONTAINER;
    }

    @Nonnull
    @Override
    public Container withUniqueId(int uniqueId)
    {
        Checks.notNegative(uniqueId, "Unique ID");
        return new ContainerImpl(uniqueId, components, spoiler, accentColor);
    }

    @Nonnull
    @Override
    public Container withSpoiler(boolean spoiler)
    {
        return new ContainerImpl(uniqueId, components, spoiler, accentColor);
    }

    @Nonnull
    @Override
    public Container withAccentColor(int accentColor)
    {
        return new ContainerImpl(uniqueId, components, spoiler, accentColor);
    }

    @Override
    public int getUniqueId()
    {
        return uniqueId;
    }

    @Nonnull
    @Override
    public Container replace(@Nonnull ComponentReplacer replacer)
    {
        Checks.notNull(replacer, "ComponentReplacer");

        return ComponentsUtil.doReplace(
                ContainerChildComponent.class,
                getComponents(),
                replacer,
                components -> new ContainerImpl(uniqueId, components, spoiler, accentColor)
        );
    }

    @Nonnull
    @Override
    public List<ContainerChildComponentUnion> getComponents()
    {
        return components;
    }

    @Nullable
    @Override
    public Integer getAccentColorRaw()
    {
        return accentColor;
    }

    @Override
    public boolean isSpoiler()
    {
        return spoiler;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        final DataObject json = DataObject.empty()
                .put("type", getType().getKey())
                .put("components", DataArray.fromCollection(getComponents()))
                .put("spoiler", spoiler);
        if (uniqueId >= 0)
            json.put("id", uniqueId);
        if (accentColor != null)
            json.put("accent_color", accentColor & 0xFFFFFF);
        return json;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof ContainerImpl)) return false;
        ContainerImpl that = (ContainerImpl) o;
        return uniqueId == that.uniqueId && spoiler == that.spoiler && Objects.equals(components, that.components) && Objects.equals(accentColor, that.accentColor);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(uniqueId, components, spoiler, accentColor);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("id", uniqueId)
                .addMetadata("accentColor", accentColor)
                .addMetadata("spoiler", spoiler)
                .addMetadata("components", components)
                .toString();
    }
}
