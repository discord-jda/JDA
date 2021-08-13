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
package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;

/**
 * TODO-v5: Revisit these docs as they're identical to GuildChannel's
 * Represents a {@link net.dv8tion.jda.api.entities.Guild Guild} channel.
 *
 * @see Guild#getGuildChannelById(long)
 * @see Guild#getGuildChannelById(ChannelType, long)
 *
 * @see JDA#getGuildChannelById(long)
 * @see JDA#getGuildChannelById(ChannelType, long)
 */
public interface StandardGuildChannel extends GuildChannel, IMemberContainer, IPermissionContainer, ICopyableChannel, IPositionableChannel, ICategorizableChannel, IInviteContainer, Comparable<StandardGuildChannel>
{
    //No methods should be included here. This is suppose to be a collective interface for simplifying aspects of the API
    // when multiple interfaces are expected.
}
