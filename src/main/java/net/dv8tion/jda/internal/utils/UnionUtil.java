package net.dv8tion.jda.internal.utils;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ComponentUnion;

import java.util.Collection;
import java.util.List;

public class UnionUtil
{

    public static <T extends ComponentUnion> List<T> componentMembersToUnionWithUnknownValidation(Collection<? extends Component> components, Class<T> targetUnion)
    {
        return (List<T>) components;
    }

    public static <T> T safeUnionCast(String name, Object instance, Class<T> type)
    {
        if (type.isInstance(instance))
            return type.cast(instance);

        String cleanedClassName = instance.getClass().getSimpleName().replace("Impl", "");
        throw new IllegalStateException(Helpers.format("Cannot convert " + name + " of type %s to %s!", cleanedClassName, type.getSimpleName()));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Component, U extends ComponentUnion> Collection<U> membersToUnion(Collection<T> members) {
        return (Collection<U>) members;
    }
}
