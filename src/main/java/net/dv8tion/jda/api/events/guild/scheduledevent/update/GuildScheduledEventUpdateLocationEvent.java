/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.api.events.guild.scheduledevent.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildScheduledEvent;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.GuildScheduledEventUpdateLocationEvent.Location;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Indicates that the location of a {@link GuildScheduledEvent} has changed.
 * <p> Because a {@link GuildScheduledEvent} may take place in either a {@link net.dv8tion.jda.api.entities.StageChannel StageChannel},
 * {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}, or at an external location not necessarily in a guild, this event uses
 * the {@link Location GuildScheduledEventUpdateLocationEvent.Location} class to store the {@link GuildScheduledEvent.Type Type}
 * of location as well as the location itself.
 *
 * <p>Can be used to detect when the {@link GuildScheduledEvent} status has changed and retrieve the old one.
 *
 * <p>Identifier: {@code guild_scheduled_event_status}
 *
 * <h2> Example </h2>
 * <pre>{@code
 * Location location = guildScheduledEventUpdateLocationEvent.getNewLocation();
 * GuildScheduledEvent.Type locationType = location.getType();
 * String logMessage = "";
 * switch (locationType)
 * {
 *     case EXTERNAL:
 *         String externalLocation = location.getExternalLocation();
 *         logMessage = "This event will now take place at " + externalLocation;
 *         break;
 *     case STAGE_INSTANCE:
 *         StageChannel stageChannel = location.getStageChannel();
 *         logMessage = "This event will now take place at the following Stage Channel: " + stageChannel.getAsMention();
 *         break;
 *     case VOICE:
 *         VoiceChannel voiceChannel = location.getVoiceChannel();
 *         logMessage = "This event will now take place at the following Voice Channel: " + voiceChannel.getAsMention();
 *         break;
 *     case UNKNOWN:
 *         logMessage = "Sorry, this bot cannot determine the location of the event";
 * }
 * }</pre>
 */
public class GuildScheduledEventUpdateLocationEvent extends GenericGuildScheduledEventUpdateEvent<Location>
{
    public static final String IDENTIFIER = "guild_scheduled_event_location";

    public GuildScheduledEventUpdateLocationEvent(@Nonnull JDA api, long responseNumber, @Nonnull GuildScheduledEvent guildScheduledEvent, @Nonnull Location previous)
    {
        super(api, responseNumber, guildScheduledEvent, previous, new Location(guildScheduledEvent), IDENTIFIER);
    }

    /**
     * The old {@link Location} of the {@link GuildScheduledEvent}
     *
     * @return The old location
     */
    @Nonnull
    public Location getOldLocation()
    {
        return getOldValue();
    }

    /**
     * The new {@link Location} of the {@link GuildScheduledEvent}
     *
     * @return The new location
     */
    @Nonnull
    public Location getNewLocation()
    {
        return getNewValue();
    }

    @Nonnull
    @Override
    public Location getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public Location getNewValue()
    {
        return super.getNewValue();
    }

    /**
     * A wrapper for the location of a {@link GuildScheduledEvent} entity used during {@link GuildScheduledEventUpdateLocationEvent} gateway event handling.
     * <p> A {@link GuildScheduledEvent} may take place in either a {@link net.dv8tion.jda.api.entities.StageChannel StageChannel},
     * {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}, or at an external location not necessarily in
     * a guild. This wrapper contains methods for identifying the {@link GuildScheduledEvent.Type Type} of location as well retrieving the location itself.
     * Note: In some cases the {@link GuildScheduledEvent.Type Type} of the location may be {@link GuildScheduledEvent.Type#UNKNOWN}, which indicates that
     * Discord has added a new guild scheduled event type, and the current JDA version being used does not yet support it.
     *
     * <h2> Example </h2>
     * <pre>{@code
     * Location location = guildScheduledEventUpdateLocationEvent.getNewLocation();
     * GuildScheduledEvent.Type locationType = location.getType();
     * String logMessage = "";
     * switch (locationType)
     * {
     *     case EXTERNAL:
     *         String externalLocation = location.getExternalLocation();
     *         logMessage = "This event will now take place at " + externalLocation;
     *         break;
     *     case STAGE_INSTANCE:
     *         StageChannel stageChannel = location.getStageChannel();
     *         logMessage = "This event will now take place at the following Stage Channel: " + stageChannel.getAsMention();
     *         break;
     *     case VOICE:
     *         VoiceChannel voiceChannel = location.getVoiceChannel();
     *         logMessage = "This event will now take place at the following Voice Channel: " + voiceChannel.getAsMention();
     *         break;
     *     case UNKNOWN:
     *         logMessage = "Sorry, this bot cannot determine the location of the event";
     * }
     * }</pre>
     */
    public static class Location
    {
        // Only one of these will not be null at any given time. If all are null, then the event's location is unknown to JDA.
        private String externalLocation;
        private StageChannel stageChannel;
        private VoiceChannel voiceChannel;

        /**
         * Constructs a new {@link Location} object from the provided object.
         *
         * @param object The location of the event.
         *               <br> The object must be either an instance of {@link StageChannel},
         *               {@link VoiceChannel}, or {@link String} in the case of an external locations. A {@code null}
         *               object indicates that the event's location {@link GuildScheduledEvent.Type Type} is
         *               {@link GuildScheduledEvent.Type#UNKNOWN Type#UNKNOWN}.
         */
        public Location(@Nullable Object object)
        {
            if (object instanceof String)
                externalLocation = (String) object;
            else if (object instanceof StageChannel)
                stageChannel = (StageChannel) object;
            else if (object instanceof VoiceChannel)
                voiceChannel = (VoiceChannel) object;
        }

        /**
         * Constructs a new {@link Location} object from the provided
         * {@link GuildScheduledEvent}.
         *
         * @param  guildScheduledEvent The guild scheduled event
         *
         * @throws IllegalArgumentException If the provided guild scheduled event object is {@code null}
         */
        public Location(@Nonnull GuildScheduledEvent guildScheduledEvent)
        {
            Checks.notNull(guildScheduledEvent, "Guild Scheduled Event");
            this.externalLocation = guildScheduledEvent.getExternalLocation();
            this.stageChannel = guildScheduledEvent.getStageChannel();
            this.voiceChannel = guildScheduledEvent.getVoiceChannel();
        }

        /**
         * Gets the {@link GuildScheduledEvent.Type Type} an event's location is. Possible types include
         * {@link GuildScheduledEvent.Type#STAGE_INSTANCE Type.STAGE_INSTANCE}, {@link GuildScheduledEvent.Type#VOICE Type.VOICE} and {@link GuildScheduledEvent.Type#EXTERNAL Type.EXTERNAL}
         * (which indicates that the event's location was manually set to a custom location).
         *
         * @return The type, or {@link GuildScheduledEvent.Type#UNKNOWN Type.UNKOWN} if the event's type is unknown to JDA.
         */
        @Nonnull
        GuildScheduledEvent.Type getType()
        {
            if (externalLocation != null)
                return GuildScheduledEvent.Type.EXTERNAL;
            else if (stageChannel != null)
                return GuildScheduledEvent.Type.STAGE_INSTANCE;
            else if (voiceChannel != null)
                return GuildScheduledEvent.Type.VOICE;
            return GuildScheduledEvent.Type.UNKNOWN;
        }

        /**
         * The stage channel that the event is set to take place at.
         * <br>Note that this method is only applicable to events which are of {@link GuildScheduledEvent.Type#STAGE_INSTANCE Type.STAGE_INSTANCE}.
         *
         * @return The stage channel, or {@code null} if the stage channel was deleted
         *         or if the event is not of {@link GuildScheduledEvent.Type#STAGE_INSTANCE Type.STAGE_INSTANCE}
         *
         * @see    #getType()
         * @see    #getVoiceChannel()
         * @see    #getExternalLocation()
         */
        @Nullable
        StageChannel getStageChannel()
        {
            return stageChannel;
        }

        /**
         * The voice channel that the event is set to take place at.
         * <br>Note that this method is only applicable to events which are of {@link GuildScheduledEvent.Type#VOICE Type.VOICE}.
         *
         * @return The voice channel, or {@code null} if the voice channel was deleted
         *         or if the event is not of {@link GuildScheduledEvent.Type#STAGE_INSTANCE Type.VOICE}
         *
         * @see    #getType()
         * @see    #getStageChannel()
         * @see    #getExternalLocation()
         */
        @Nullable
        VoiceChannel getVoiceChannel()
        {
            return voiceChannel;
        }

        /**
         * The external location that the event is set to take place at.
         * <br>Note that this method is only applicable to events which are of {@link GuildScheduledEvent.Type#EXTERNAL Type.EXTERNAL}.
         *
         * @return The location, or {@code null} if the event is not of {@link GuildScheduledEvent.Type#EXTERNAL Type.EXTERNAL}
         *
         * @see    #getType()
         * @see    #getStageChannel()
         * @see    #getVoiceChannel()
         */
        @Nullable
        String getExternalLocation()
        {
            return externalLocation;
        }

        // Object Overrides

        @Override
        public int hashCode()
        {
            return Objects.hash(externalLocation, stageChannel, voiceChannel);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (!(o instanceof Location))
                return false;
            Location otherLocation = (Location) o;
            return this.voiceChannel.equals(otherLocation.voiceChannel) &&
                   this.stageChannel.equals(otherLocation.stageChannel) &&
                   this.externalLocation.equals(otherLocation.externalLocation);
        }
    }
}
