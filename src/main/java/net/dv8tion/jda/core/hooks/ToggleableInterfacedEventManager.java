package net.dv8tion.jda.core.hooks;

import net.dv8tion.jda.core.events.Event;
import org.apache.http.util.Args;

import java.util.Map;
import java.util.stream.Collectors;

public class ToggleableInterfacedEventManager extends InterfacedEventManager implements IToggleableEventManager
{
    private final Map<Class<? extends Event>, Boolean> eventEnabledMap = IEventManager.eventList.stream().collect(Collectors
            .toMap(eventClass -> eventClass, eventClass -> true));

    @Override
    public void setEventEnabled(Class<? extends Event> eventClass, boolean enabled)
    {
        Args.notNull(eventClass, "eventClass");
        if (!eventEnabledMap.containsKey(eventClass)) throw new IllegalArgumentException("Event class must be class of a default event");
        eventEnabledMap.put(eventClass, enabled);
    }

    @Override
    public boolean isEventEnabled(Class<? extends Event> eventClass)
    {
        Args.notNull(eventClass, "eventClass");
        if (!eventEnabledMap.containsKey(eventClass)) throw new IllegalArgumentException("Event class must be class of a default event");
        return eventEnabledMap.get(eventClass);
    }

    @Override
    public void handle(Event event) {
        Boolean eventIsEnabled = eventEnabledMap.get(event.getClass());
        if (eventIsEnabled == null /* Let superclass handle unknown events */ || eventIsEnabled)
            super.handle(event);
    }
}
