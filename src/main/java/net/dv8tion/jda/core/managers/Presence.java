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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import org.json.JSONObject;

/**
 * The Presence associated with the provided JDA instance
 */
public class Presence
{

    private final JDAImpl api;
    private boolean idle = false;
    private Game game = null;
    private OnlineStatus status = OnlineStatus.ONLINE;

    /**
     * Creates a new Presence representation for the provided JDAImpl instance
     *
     * @param jda
     *      The not-null JDAImpl instance to use
     */
    public Presence(JDAImpl jda)
    {
        this.api = jda;
        if (jda.getAccountType() == AccountType.CLIENT)
            status = jda.asClient().getSettings().getStatus();
    }


    /* -- Getters -- */


    /**
     * The current OnlineStatus for this session.<br>
     * This might not be what the Discord Client displays due to session clashing!
     *
     * @return
     *      The {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} of the current session
     */
    public OnlineStatus getStatus()
    {
        return status;
    }

    /**
     * The current Game for this session.<br>
     * This might not be what the Discord Client displays due to session clashing!
     *
     * @return
     *      The {@link net.dv8tion.jda.core.entities.Game Game} of the current session
     */
    public Game getGame()
    {
        return game;
    }

    /**
     * Whether the current session is marked as afk or not
     *
     * @return
     *      true if this session is marked as afk
     */
    public boolean isIdle()
    {
        return idle;
    }

    /**
     * The JDA instance of this Presence
     *
     * @return
     *      The current JDA instance
     */
    public JDA getJDA()
    {
        return api;
    }


    /* -- Setters -- */


    /**
     * Sets the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} for this session
     *
     * @param status
     *      the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} to be used (OFFLINE/null -> INVISIBLE)
     * @throws IllegalArgumentException
     *      if the provided OnlineStatus is {@link net.dv8tion.jda.core.OnlineStatus#UNKNOWN UNKNOWN}
     */
    public void setStatus(OnlineStatus status)
    {
        if (status == OnlineStatus.UNKNOWN)
            throw new IllegalArgumentException("Cannot set the presence status to an unknown OnlineStatus!");
        if (status == OnlineStatus.OFFLINE || status == null)
            status = OnlineStatus.INVISIBLE;
        JSONObject object = getFullPresence();
        object.put("status", status.getKey());
        update(object);
        this.status = status;
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Game Game} for this session
     *
     * @param game
     *      A {@link net.dv8tion.jda.core.entities.Game Game} instance or null to reset
     * @see net.dv8tion.jda.core.entities.Game#of(String)
     * @see net.dv8tion.jda.core.entities.Game#of(String, String)
     */
    public void setGame(Game game)
    {
        JSONObject gameObj = getGameJson(game);
        if (gameObj == null)
        {
            update(getFullPresence().put("game", JSONObject.NULL));
            this.game = null;
            return;
        }
        JSONObject object = getFullPresence();
        object.put("game", gameObj);
        update(object);
        this.game = game;
    }

    /**
     * Sets whether this session should be marked as afk or not
     *
     * @param idle
     *      boolean
     */
    public void setIdle(boolean idle)
    {
        JSONObject object = getFullPresence();
        object.put("afk", idle);
        update(object);
        this.idle = idle;
    }


    /* -- Private Methods -- */


    private JSONObject getFullPresence()
    {
        JSONObject game = getGameJson(this.game);
        return new JSONObject()
              .put("afk", idle)
              .put("since", System.currentTimeMillis())
              .put("game", game == null ? JSONObject.NULL : game)
              .put("status", getStatus().getKey());
    }

    private JSONObject getGameJson(Game game)
    {
        if (game == null || game.getName() == null || game.getType() == null)
            return null;
        JSONObject gameObj = new JSONObject();
        gameObj.put("name", game.getName());
        gameObj.put("type", game.getType().getKey());
        if (game.getType() == Game.GameType.TWITCH && game.getUrl() != null)
            gameObj.put("url", game.getUrl());

        return gameObj;
    }


    /* -- Terminal -- */


    protected void update(JSONObject data)
    {
        api.getClient().send(new JSONObject()
            .put("d", data)
            .put("op", 3).toString());
    }

}
