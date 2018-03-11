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

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;

/**
 * The Presence associated with the provided JDA instance
 *
 * @since  3.0
 */
public interface Presence
{

    /**
     * The JDA instance of this Presence
     *
     * @return The current JDA instance
     */
    JDA getJDA();

    /**
     * The current OnlineStatus for this session.
     * <br>This might not be what the Discord Client displays due to session clashing!
     *
     * @return The {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus}
     *         of the current session
     */
    OnlineStatus getStatus();

    /**
     * The current Game for this session.
     * <br>This might not be what the Discord Client displays due to session clashing!
     *
     * @return The {@link net.dv8tion.jda.core.entities.Game Game}
     *         of the current session or null if no game is set
     */
    Game getGame();

    /**
     * Whether the current session is marked as afk or not.
     *
     * <p>This is relevant to client accounts to monitor
     * whether new messages should trigger mobile push-notifications.
     *
     * @return True if this session is marked as afk
     */
    boolean isIdle();

    /**
     * Sets the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} for this session
     *
     * @throws IllegalArgumentException
     *         if the provided OnlineStatus is {@link net.dv8tion.jda.core.OnlineStatus#UNKNOWN UNKNOWN}
     *
     * @param  status
     *         the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus}
     *         to be used (OFFLINE/null {@literal ->} INVISIBLE)
     */
    void setStatus(OnlineStatus status);

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Game Game} for this session.
     * <br>A Game can be retrieved via {@link net.dv8tion.jda.core.entities.Game#playing(String)}.
     * For streams you provide a valid streaming url as second parameter
     *
     * <p>Examples:
     * <br>{@code presence.setGame(Game.playing("Thrones"));}
     * <br>{@code presence.setGame(Game.streaming("Thrones", "https://twitch.tv/EasterEggs"));}
     *
     * @param  game
     *         A {@link net.dv8tion.jda.core.entities.Game Game} instance or null to reset
     *
     * @see    net.dv8tion.jda.core.entities.Game#playing(String)
     * @see    net.dv8tion.jda.core.entities.Game#streaming(String, String)
     */
    void setGame(Game game);

    /**
     * Sets whether this session should be marked as afk or not
     *
     * <p>This is relevant to client accounts to monitor
     * whether new messages should trigger mobile push-notifications.
     *
     * @param idle
     *        boolean
     */
    void setIdle(boolean idle);

    /**
     * Sets all presence fields of this session.
     *
     * @param  status
     *         The {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} for this session
     *         (See {@link #setStatus(OnlineStatus)})
     * @param  game
     *         The {@link net.dv8tion.jda.core.entities.Game Game} for this session
     *         (See {@link #setGame(Game)} for more info)
     * @param  idle
     *         Whether to mark this session as idle (useful for client accounts {@link #setIdle(boolean)})
     *
     * @throws java.lang.IllegalArgumentException
     *         If the specified OnlineStatus is {@link net.dv8tion.jda.core.OnlineStatus#UNKNOWN UNKNOWN}
     */
    void setPresence(OnlineStatus status, Game game, boolean idle);

    /**
     * Sets two presence fields of this session.
     * <br>The third field stays untouched.
     *
     * @param  status
     *         The {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} for this session
     *         (See {@link #setStatus(OnlineStatus)})
     * @param  game
     *         The {@link net.dv8tion.jda.core.entities.Game Game} for this session
     *         (See {@link #setGame(Game)} for more info)
     *
     * @throws java.lang.IllegalArgumentException
     *         If the specified OnlineStatus is {@link net.dv8tion.jda.core.OnlineStatus#UNKNOWN UNKNOWN}
     */
    void setPresence(OnlineStatus status, Game game);

    /**
     * Sets two presence fields of this session.
     * <br>The third field stays untouched.
     *
     * @param  status
     *         The {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} for this session
     *         (See {@link #setStatus(OnlineStatus)})
     * @param  idle
     *         Whether to mark this session as idle (useful for client accounts {@link #setIdle(boolean)})
     *
     * @throws java.lang.IllegalArgumentException
     *         If the specified OnlineStatus is {@link net.dv8tion.jda.core.OnlineStatus#UNKNOWN UNKNOWN}
     */
    void setPresence(OnlineStatus status, boolean idle);

    /**
     * Sets two presence fields of this session.
     * <br>The third field stays untouched.
     *
     * @param  game
     *         The {@link net.dv8tion.jda.core.entities.Game Game} for this session
     *         (See {@link #setGame(Game)} for more info)
     * @param  idle
     *         Whether to mark this session as idle (useful for client accounts {@link #setIdle(boolean)})
     */
    void setPresence(Game game, boolean idle);
}
