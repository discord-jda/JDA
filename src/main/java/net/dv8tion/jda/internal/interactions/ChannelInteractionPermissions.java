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

package net.dv8tion.jda.internal.interactions;

/**
 * Represents permissions the interaction's member has been granted
 * in the {@link net.dv8tion.jda.api.entities.channel.middleman.GuildChannel GuildChannel}.
 */
public class ChannelInteractionPermissions
{
    private final long memberId;
    private final long permissions;

    public ChannelInteractionPermissions(long memberId, long permissions) {
        this.memberId = memberId;
        this.permissions = permissions;
    }

    public long getMemberId()
    {
        return memberId;
    }

    public long getPermissions()
    {
        return permissions;
    }
}
