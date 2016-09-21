/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.entities;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.List;

public interface Member extends IMentionable
{
    User getUser();

    Guild getGuild();

    JDA getJDA();

    OffsetDateTime getJoinDate();

    VoiceState getVoiceState();

    /**
     * The game that the user is currently playing.
     * If the user is not currently playing a game, this will return null.
     *
     * @return
     *      Possibly-null {@link net.dv8tion.jda.core.entities.Game Game} containing the game that the {@link net.dv8tion.jda.core.entities.User User} is currently playing.
     */
    Game getGame();

    /**
     * Returns the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} of the User.<br>
     * If the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} is unrecognized, will return {@link net.dv8tion.jda.core.OnlineStatus#UNKNOWN UNKNOWN}.
     *
     * @return
     *      The current {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} of the {@link net.dv8tion.jda.core.entities.User User}.
     */
    OnlineStatus getOnlineStatus();

    /**
     * Returns the current nickname of this Member for the parent Guild.
     *
     * @return
     *      The nickname or null, if no nickname is set.
     */
    String getNickname();

    String getEffectiveName();

    List<Role> getRoles();

    Color getColor();

    List<Permission> getPermissions();
    List<Permission> getPermissions(Channel channel);

    boolean hasPermission(Permission... permissions);
    boolean hasPermission(Channel channel, Permission... permission);
}
