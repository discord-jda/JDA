package net.dv8tion.jda.api.interactions.components.file;

import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.interactions.components.container.ContainerChildComponent;

public interface File extends Component, MessageTopLevelComponent, ContainerChildComponent
{
    boolean isSpoiler();
}
