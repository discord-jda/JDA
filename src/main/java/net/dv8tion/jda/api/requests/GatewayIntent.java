/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.requests;

import javax.annotation.Nonnull;
import java.util.EnumSet;

public enum GatewayIntent
{
    //GUILDS(0), we currently don't support this one
    GUILD_MEMBERS(1),
    GUILD_BANS(2),
    GUILD_EMOJIS(3),
    GUILD_INTEGRATIONS(4),
    GUILD_WEBHOOKS(5),
    GUILD_INVITES(6),
    GUILD_VOICE_STATES(7),
    GUILD_PRESENCES(8),
    GUILD_MESSAGES(9),
    GUILD_MESSAGE_REACTIONS(10),
    GUILD_MESSAGE_TYPING(11),
    DIRECT_MESSAGES(12),
    DIRECT_MESSAGE_REACTIONS(13),
    DIRECT_MESSAGE_TYPING(14);

    public static final int ALL_INTENTS = 1 | getRaw(EnumSet.allOf(GatewayIntent.class));

    private final int rawValue;
    private final int offset;

    GatewayIntent(int offset)
    {
        this.offset = offset;
        this.rawValue = 1 << offset;
    }

    public int getRawValue()
    {
        return rawValue;
    }

    public int getOffset()
    {
        return offset;
    }

    public static int getRaw(@Nonnull EnumSet<GatewayIntent> set)
    {
        int raw = 0;
        for (GatewayIntent intent : set)
            raw |= intent.rawValue;
        return raw;
    }
}
