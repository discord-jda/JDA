package net.dv8tion.jda.api.events.guild.scheduledevent.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ScheduledEvent;
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.GenericScheduledEventGatewayEvent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A generic gateway event class representing an update of a {@link ScheduledEvent ScheduledEvent} entity.
 * <br> All events in {@link net.dv8tion.jda.api.events.guild.scheduledevent.update} package extend this event and are fired
 * when a specified field in a {@link ScheduledEvent ScheduledEvent} is updated.
 *
 * <p> It should be noted that {@link ScheduledEvent ScheduledEvents} are not
 * actual gateway events found in the {@link net.dv8tion.jda.api.events} package, but are rather entities similar to
 * {@link net.dv8tion.jda.api.entities.User User} or {@link net.dv8tion.jda.api.entities.channel.concrete.TextChannel TextChannel} objects
 * representing a <a href="https://support.discord.com/hc/en-us/articles/4409494125719-Scheduled-Events">scheduled event</a>.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>These events require the {@link net.dv8tion.jda.api.requests.GatewayIntent#SCHEDULED_EVENTS SCHEDULED_EVENTS} intent and {@link CacheFlag#SCHEDULED_EVENTS} to be enabled.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createDefault(String) createDefault(String)} and
 * {@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Discord does not specifically tell us about the updates, but merely tells us the
 * {@link ScheduledEvent ScheduledEvent} was updated and gives us the updated {@link ScheduledEvent ScheduledEvent} object.
 * In order to fire a specific event like this we need to have the old {@link ScheduledEvent ScheduledEvent} cached to compare against.
 */
public abstract class GenericScheduledEventUpdateEvent<T> extends GenericScheduledEventGatewayEvent implements UpdateEvent<ScheduledEvent, T>
{
    protected final T previous;
    protected final T next;
    protected final String identifier;

    public GenericScheduledEventUpdateEvent(
        @Nonnull JDA api, long responseNumber, @Nonnull ScheduledEvent scheduledEvent,
        @Nullable T previous, @Nullable T next, @Nonnull String identifier)
    {
        super(api, responseNumber, scheduledEvent);
        this.previous = previous;
        this.next = next;
        this.identifier = identifier;
    }

    @Nonnull
    @Override
    public ScheduledEvent getEntity()
    {
        return getScheduledEvent();
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
        return "ScheduledEventUpdate[" + getPropertyIdentifier() + "](" + getOldValue() + "->" + getNewValue() + ')';
    }
}
