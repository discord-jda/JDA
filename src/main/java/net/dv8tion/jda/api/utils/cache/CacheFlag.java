/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.annotation.Nullable;

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
     * <p>Requires {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_VOICE_STATES GUILD_VOICE_STATES} intent to be enabled.
     */
    VOICE_STATE(GatewayIntent.GUILD_VOICE_STATES),
    /**
     * Enables cache for {@link Guild#getEmoteCache()}
     *
     * <p>Requires {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_EMOJIS GUILD_EMOJIS} intent to be enabled.
     */
    EMOTE(GatewayIntent.GUILD_EMOJIS),
    /**
     * Enables cache for {@link Member#getOnlineStatus(net.dv8tion.jda.api.entities.ClientType) Member.getOnlineStatus(ClientType)}
     *
     * <p>Requires {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} intent to be enabled.
     */
    CLIENT_STATUS(GatewayIntent.GUILD_PRESENCES),
    /**
     * Enables cache for {@link GuildChannel#getMemberPermissionOverrides()}
     */
    MEMBER_OVERRIDES(null),
    ;
    private final GatewayIntent requiredIntent;

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
}
