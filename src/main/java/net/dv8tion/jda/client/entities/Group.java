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

package net.dv8tion.jda.client.entities;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.cache.SnowflakeCacheView;

import javax.annotation.CheckReturnValue;
import java.util.List;

public interface Group extends MessageChannel, CallableChannel
{
    /**
     * Returns the name set for this group.<br>
     * If no name has been set for this group, then null is returned.
     *
     * @return
     *      Possibly-null name of the group.
     */
    String getName();
    String getIconId();
    String getIconUrl();

    User getOwner();
    SnowflakeCacheView<User> getUserCache();
    List<User> getUsers();
    List<User> getNonFriendUsers();
    List<Friend> getFriends();

    //getGroupManager()

    @CheckReturnValue
    RestAction leaveGroup();
}
