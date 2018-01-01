/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.entities;

/**
 * Marks a fakeable entity.
 * <br>A fake entity ({@link #isFake()} is true) is an entity which is not directly related to this instance of JDA or
 * this JDA shard.
 * <br>An example would be a fake {@link net.dv8tion.jda.core.entities.User User}.
 * <br>A fake user can occur when sharding. Discord only sends private messages to Shard 0. If a User which is connected
 * to Guilds on shard 1 sends a private message to the logged in account, it is received on Shard 0. However, if Shard 0
 * does not know about the User due to not having a Guild connection with them, it will use the information provided in
 * the MESSAGE_CREATE event to create a temporary fake user.
 * In this case, the associated {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} is also fake!
 *
 * <p>Another example would be a fake {@link net.dv8tion.jda.core.entities.Emote Emote}. If a user sends a message containing
 * an Emote from a {@link net.dv8tion.jda.core.entities.Guild Guild} that the currently logged in account is not a part of,
 * JDA will construct a fake {@link net.dv8tion.jda.core.entities.Emote Emote} object for it.
 *
 * @since 3.0
 */
public interface IFakeable
{
    /**
     * Describes whether an entity is fake or not.
     *
     * @return False, if this is an actual JDA entity.
     */
    boolean isFake();
}
