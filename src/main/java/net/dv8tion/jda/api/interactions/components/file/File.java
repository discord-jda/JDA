package net.dv8tion.jda.api.interactions.components.file;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.IdentifiableComponent;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponent;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface File extends Component, IdentifiableComponent, MessageTopLevelComponent, ContainerChildComponent
{
    @Nonnull
    @Override
    @CheckReturnValue
    File withUniqueId(int uniqueId);

    boolean isSpoiler();
}
