package net.dv8tion.jda.api.events.guild.scheduledevent.update

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.ScheduledEvent
import net.dv8tion.jda.api.events.UpdateEvent
import net.dv8tion.jda.api.events.guild.scheduledevent.GenericScheduledEventGatewayEvent
import javax.annotation.Nonnull

/**
 * A generic gateway event class representing an update of a [ScheduledEvent] entity.
 * <br></br> All events in [net.dv8tion.jda.api.events.guild.scheduledevent.update] package extend this event and are fired
 * when a specified field in a [ScheduledEvent] is updated.
 *
 *
 *  It should be noted that [ScheduledEvents][ScheduledEvent] are not
 * actual gateway events found in the [net.dv8tion.jda.api.events] package, but are rather entities similar to
 * [User][net.dv8tion.jda.api.entities.User] or [TextChannel][net.dv8tion.jda.api.entities.channel.concrete.TextChannel] objects
 * representing a [scheduled event](https://support.discord.com/hc/en-us/articles/4409494125719-Scheduled-Events).
 *
 *
 * **Requirements**<br></br>
 *
 *
 * These events require the [SCHEDULED_EVENTS][net.dv8tion.jda.api.requests.GatewayIntent.SCHEDULED_EVENTS] intent and [CacheFlag.SCHEDULED_EVENTS] to be enabled.
 * <br></br>[createDefault(String)][net.dv8tion.jda.api.JDABuilder.createDefault] and
 * [createLight(String)][net.dv8tion.jda.api.JDABuilder.createLight] disable this by default!
 *
 *
 * Discord does not specifically tell us about the updates, but merely tells us the
 * [ScheduledEvent] was updated and gives us the updated [ScheduledEvent] object.
 * In order to fire a specific event like this we need to have the old [ScheduledEvent] cached to compare against.
 */
abstract class GenericScheduledEventUpdateEvent<T>(
    @Nonnull api: JDA, responseNumber: Long, @Nonnull scheduledEvent: ScheduledEvent,
    previous: T?, next: T?, @Nonnull identifier: String
) : GenericScheduledEventGatewayEvent(api, responseNumber, scheduledEvent), UpdateEvent<ScheduledEvent?, T?> {
    protected val previous: T
    protected val next: T

    @get:Nonnull
    override val propertyIdentifier: String

    init {
        this.previous = previous
        this.next = next
        propertyIdentifier = identifier
    }

    @get:Nonnull
    override val entity: E
        get() = getScheduledEvent()
    override val oldValue: T?
        get() = previous
    override val newValue: T?
        get() = next

    override fun toString(): String {
        return "ScheduledEventUpdate[" + propertyIdentifier + "](" + oldValue + "->" + newValue + ')'
    }
}
