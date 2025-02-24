package net.dv8tion.jda.internal.interactions.components;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ComponentTree;
import net.dv8tion.jda.api.interactions.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.interactions.components.replacer.IReplaceable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ComponentTreeImpl implements ComponentTree
{
    private final List<Component> components;

    public ComponentTreeImpl(List<Component> components)
    {
        this.components = new ArrayList<>(components);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nonnull
    @Override
    public <T extends Component> ComponentTree replace(ComponentReplacer<T> replacer)
    {
        List<Component> newComponents = new ArrayList<>();
        for (Component component : components)
        {
            if (replacer.getComponentType().isInstance(component))
                newComponents.add(replacer.apply((T) component));
            else if (component instanceof IReplaceable)
                newComponents.add(((IReplaceable) component).replace(replacer));
            else
                newComponents.add(component);
        }

        return new ComponentTreeImpl(newComponents);
    }
}
