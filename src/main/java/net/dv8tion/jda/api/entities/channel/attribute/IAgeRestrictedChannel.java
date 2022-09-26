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

package net.dv8tion.jda.api.entities.channel.attribute;

import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

/**
 * Channels which can be set to age-restricted.
 * <br>These channels only allow users with a verified mature age to participate.
 */
public interface IAgeRestrictedChannel extends GuildChannel
{
    /**
     * Whether this channel is considered as age-restricted, also known as NSFW (Not-Safe-For-Work)
     *
     * @return True, If this channel is age-restricted by the official Discord Client
     */
    boolean isNSFW();
}
