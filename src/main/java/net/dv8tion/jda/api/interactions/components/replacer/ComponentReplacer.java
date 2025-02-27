package net.dv8tion.jda.api.interactions.components.replacer;

import net.dv8tion.jda.api.interactions.components.Component;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface ComponentReplacer
{
    @Nonnull
    Component apply(@Nonnull Component oldComponent);
}
