/*
 * Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.requests;

import net.dv8tion.jda.annotations.DeprecatedSince;
import net.dv8tion.jda.annotations.ReplaceWith;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.utils.JDALogger;
import org.json.JSONObject;
import org.slf4j.Logger;

@Deprecated
@DeprecatedSince("3.8.0")
@ReplaceWith("GuildSetupController")
public class GuildLock
{
    public static final Logger LOG = JDALogger.getLog(GuildLock.class);

    private final JDAImpl jda;

    public GuildLock(JDAImpl jda)
    {
        LOG.error("Using deprecated GuildLock. This should not be used anymore and will be removed in the future!", new UnsupportedOperationException());
        this.jda = jda;
    }

    public boolean isLocked(long guildId)
    {
        return jda.getGuildSetupController().isLocked(guildId);
    }

    public void unlock(long guildId)
    {
        throw new UnsupportedOperationException();
    }

    public void queue(long guildId, JSONObject event)
    {
        jda.getGuildSetupController().cacheEvent(guildId, event);
    }

    public void clear() {}
}
