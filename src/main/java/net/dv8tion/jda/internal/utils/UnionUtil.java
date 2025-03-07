package net.dv8tion.jda.internal.utils;

public class UnionUtil
{
    public static <T> T safeUnionCast(String classCategory, Object instance, Class<T> toObjectClass)
    {
        if (toObjectClass.isInstance(instance))
            return toObjectClass.cast(instance);

        String cleanedClassName = instance.getClass().getSimpleName().replace("Impl", "");
        throw new IllegalStateException(Helpers.format("Cannot convert " + classCategory + " of type %s to %s!", cleanedClassName, toObjectClass.getSimpleName()));
    }
}
