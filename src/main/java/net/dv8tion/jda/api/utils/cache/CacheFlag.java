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

package net.dv8tion.jda.api.utils.cache;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.annotations.RequiredCacheFlags;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

/**
 * Flags used to enable cache services for JDA.
 * <br>Check the flag descriptions to see which {@link net.dv8tion.jda.api.requests.GatewayIntent intents} are required to use them.
 */
public enum CacheFlag
{
    /**
     * Enables cache for {@link Member#getActivities()}
     *
     * <p>Requires {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} intent to be enabled.
     */
    ACTIVITY(GatewayIntent.GUILD_PRESENCES),
    /**
     * Enables cache for {@link Member#getVoiceState()}
     * <br>This will always be cached for self member.
     *
     * <p><b>Voice states are only cached when the member is connected to an audio channel.</b>
     *
     * <p>Requires {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_VOICE_STATES GUILD_VOICE_STATES} intent to be enabled.
     */
    VOICE_STATE(GatewayIntent.GUILD_VOICE_STATES),
    /**
     * Enables cache for {@link Guild#getEmojiCache()}
     *
     * <p>Requires {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_EXPRESSIONS GUILD_EXPRESSIONS} intent to be enabled.
     */
    EMOJI(GatewayIntent.GUILD_EXPRESSIONS),
    /**
     * Enables cache for {@link Guild#getStickerCache()}
     *
     * <p>Requires {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_EXPRESSIONS GUILD_EXPRESSIONS} intent to be enabled.
     */
    STICKER(GatewayIntent.GUILD_EXPRESSIONS),
    /**
     * Enables cache for {@link Member#getOnlineStatus(net.dv8tion.jda.api.entities.ClientType) Member.getOnlineStatus(ClientType)}
     *
     * <p>Requires {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} intent to be enabled.
     */
    CLIENT_STATUS(GatewayIntent.GUILD_PRESENCES),
    /**
     * Enables cache for {@link net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer#getMemberPermissionOverrides()}
     */
    MEMBER_OVERRIDES,
    /**
     * Enables cache for {@link Role#getTags()}
     */
    ROLE_TAGS,
    /**
     * Enables cache for {@link IPostContainer#getAvailableTagCache()} and {@link ThreadChannel#getAppliedTags()}
     */
    FORUM_TAGS,
    /**
     * Enables cache for {@link Member#getOnlineStatus()}
     * <br>This is enabled implicitly by {@link #ACTIVITY} and {@link #CLIENT_STATUS}.
     *
     * <p>Requires {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} intent to be enabled.
     *
     * @since 4.3.0
     */
    ONLINE_STATUS(GatewayIntent.GUILD_PRESENCES),
    /**
     * Enables cache for {@link Guild#getScheduledEventCache()}
     *
     * <p>Requires {@link net.dv8tion.jda.api.requests.GatewayIntent#SCHEDULED_EVENTS SCHEDULED_EVENTS} intent to be enabled.
     */
    SCHEDULED_EVENTS(GatewayIntent.SCHEDULED_EVENTS),
    ;

    private static final EnumSet<CacheFlag> privileged = EnumSet.of(ACTIVITY, CLIENT_STATUS, ONLINE_STATUS);
    private final GatewayIntent requiredIntent;

    CacheFlag()
    {
        this(null);
    }

    CacheFlag(GatewayIntent requiredIntent)
    {
        this.requiredIntent = requiredIntent;
    }

    /**
     * The required {@link GatewayIntent} for this cache flag.
     *
     * @return The required intent, or null if no intents are required.
     */
    @Nullable
    public GatewayIntent getRequiredIntent()
    {
        return requiredIntent;
    }

    /**
     * Whether this cache flag is for presence information of a member.
     *
     * @return True, if this is for presences
     */
    public boolean isPresence()
    {
        return requiredIntent == GatewayIntent.GUILD_PRESENCES;
    }

    /**
     * Collects all cache flags that require privileged intents
     *
     * @return {@link EnumSet} of the cache flags that require the privileged intents
     */
    @Nonnull
    public static EnumSet<CacheFlag> getPrivileged()
    {
        return EnumSet.copyOf(privileged);
    }

    /**
     * Parse the required cache flags from the provided {@link GenericEvent Event Types}.
     *
     * @param  events
     *         The event types
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return {@link EnumSet} for the required cache flags
     */
    @Nonnull
    @SafeVarargs
    public static EnumSet<CacheFlag> fromEvents(@Nonnull Class<? extends GenericEvent>... events)
    {
        Checks.noneNull(events, "Event");
        return fromEvents(Arrays.asList(events));
    }

    /**
     * Parse the required cache flags from the provided {@link GenericEvent Event Types}.
     *
     * @param  events
     *         The event types
     *
     * @throws IllegalArgumentException
     *         If provided with null
     *
     * @return {@link EnumSet} for the required cache flags
     */
    @Nonnull
    public static EnumSet<CacheFlag> fromEvents(@Nonnull Collection<Class<? extends GenericEvent>> events)
    {
        Checks.noneNull(events, "Events");
        EnumSet<CacheFlag> flags = EnumSet.noneOf(CacheFlag.class);
        for (Class<? extends GenericEvent> event : events)
        {
            final RequiredCacheFlags requiredCacheFlags = event.getDeclaredAnnotation(RequiredCacheFlags.class);
            if (requiredCacheFlags != null)
                Collections.addAll(flags, requiredCacheFlags.always());
        }
        return flags;
    }
}
