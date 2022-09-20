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
 * Channels which support slowmode.
 */
public interface ISlowmodeChannel extends GuildChannel
{
    /**
     * The maximum duration of slowmode in seconds
     */
    int MAX_SLOWMODE = 21600;

    /**
     * The slowmode set for this channel.
     * <br>If slowmode is set, this returns an {@code int} between 1 and {@value #MAX_SLOWMODE}.
     * <br>Otherwise, if no slowmode is set, this returns {@code 0}.
     *
     * <p>Note bots are unaffected by this.
     * <br>Having {@link net.dv8tion.jda.api.Permission#MESSAGE_MANAGE MESSAGE_MANAGE} or
     * {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} permission also
     * grants immunity to slowmode.
     *
     * <p><b>Special case</b><br>
     * {@link net.dv8tion.jda.api.entities.channel.concrete.ForumChannel ForumChannels} use this to limit how many posts a user can create.
     * The client refers to this as the post slowmode.
     *
     * @return The slowmode for this channel, between 1 and {@value #MAX_SLOWMODE}, or {@code 0} if no slowmode is set.
     */
    int getSlowmode();
}
