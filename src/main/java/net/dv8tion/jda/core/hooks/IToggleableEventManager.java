package net.dv8tion.jda.core.hooks;

import net.dv8tion.jda.core.events.Event;

public interface IToggleableEventManager extends IEventManager
{
    void setEventEnabled(Class<? extends Event> eventClass, boolean enabled);

    boolean isEventEnabled(Class<? extends Event> eventClass);
}
