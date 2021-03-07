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

package net.dv8tion.jda.api.events.emote.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Role;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Indicates that the role whitelist for an {@link net.dv8tion.jda.api.entities.Emote Emote} changed.
 *
 * <p>Can be used to retrieve the old role whitelist
 *
 * <h2>Requirements</h2>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.utils.cache.CacheFlag#EMOTE EMOTE} CacheFlag to be enabled, which requires
 * the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_EMOJIS GUILD_EMOJIS} intent.
 *
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disables that CacheFlag by default!
 *
 * <p>Identifier: {@code roles}
 */
public class EmoteUpdateRolesEvent extends GenericEmoteUpdateEvent<List<Role>>
{
    public static final String IDENTIFIER = "roles";

    public EmoteUpdateRolesEvent(@Nonnull JDA api, long responseNumber, @Nonnull Emote emote, @Nonnull List<Role> oldRoles)
    {
        super(api, responseNumber, emote, oldRoles, emote.getRoles(), IDENTIFIER);
    }

    /**
     * The old role whitelist
     *
     * @return The old role whitelist
     */
    @Nonnull
    public List<Role> getOldRoles()
    {
        return getOldValue();
    }

    /**
     * The new role whitelist
     *
     * @return The new role whitelist
     */
    @Nonnull
    public List<Role> getNewRoles()
    {
        return getNewValue();
    }

    @Nonnull
    @Override
    public List<Role> getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public List<Role> getNewValue()
    {
        return super.getNewValue();
    }
}
