package net.dv8tion.jda.core.events;

import net.dv8tion.jda.core.JDA;

public class EntityCreationEvent extends Event {
    private final Object entity;

    public EntityCreationEvent(JDA api, Object entity) {
        super(api, -1);
        this.entity = entity;
    }

    public Object getEntity() {
        return entity;
    }
}
