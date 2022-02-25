package net.dv8tion.jda.api.events.guild.scheduledevent.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildScheduledEvent;
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.GenericGuildScheduledEventGatewayEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A generic gateway event class representing an update of a {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent} entity.
 * <br> All events in {@link net.dv8tion.jda.api.events.guild.scheduledevent.update} package extend this event and are fired
 * when a specified field in a {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvent} is updated.
 *
 * <p> It should be noted that {@link net.dv8tion.jda.api.entities.GuildScheduledEvent GuildScheduledEvents} are not
 * actual gateway events found in the {@link net.dv8tion.jda.api.events} package, but are rather entities similar to
 * {@link net.dv8tion.jda.api.entities.User User} or {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} objects
 * representing a <a href="https://support.discord.com/hc/en-us/articles/4409494125719-Scheduled-Events">guild's scheduled events</a>.
 *
 * @param <T>
 *        The type of the field being updated
 */
public abstract class GenericGuildScheduledEventUpdateEvent<T> extends GenericGuildScheduledEventGatewayEvent implements UpdateEvent<GuildScheduledEvent, T>
{
    protected final T previous;
    protected final T next;
    protected final String identifier;

    public GenericGuildScheduledEventUpdateEvent(
        @Nonnull JDA api, long responseNumber, @Nonnull GuildScheduledEvent guildScheduledEvent,
        @Nullable T previous, @Nullable T next, @Nonnull String identifier)
    {
        super(api, responseNumber, guildScheduledEvent);
        this.previous = previous;
        this.next = next;
        this.identifier = identifier;
    }

    @Nonnull
    @Override
    public GuildScheduledEvent getEntity()
    {
        return getGuildScheduledEvent();
    }

    @Nonnull
    @Override
    public String getPropertyIdentifier()
    {
        return identifier;
    }

    @Nullable
    @Override
    public T getOldValue()
    {
        return previous;
    }

    @Nullable
    @Override
    public T getNewValue()
    {
        return next;
    }

    @Override
    public String toString()
    {
        return "GuildSchedEventUpdate[" + getPropertyIdentifier() + "](" + getOldValue() + "->" + getNewValue() + ')';
    }
}
