package net.dv8tion.jda.api.components.label;

import net.dv8tion.jda.api.components.Component;

import javax.annotation.Nonnull;

public interface LabelChildComponent extends Component
{
    @Nonnull
    @Override
    LabelChildComponent withUniqueId(int uniqueId);
}
