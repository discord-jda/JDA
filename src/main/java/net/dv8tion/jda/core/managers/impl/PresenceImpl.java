/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

package net.dv8tion.jda.core.managers.impl;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.managers.Presence;
import org.json.JSONObject;

/**
 * The Presence associated with the provided JDA instance
 *
 * @since  3.0
 * @author Florian Spieß
 */
public class PresenceImpl implements Presence
{

    private final JDAImpl api;
    private boolean idle = false;
    private Game game = null;
    private OnlineStatus status = OnlineStatus.ONLINE;

    /**
     * Creates a new Presence representation for the provided JDAImpl instance
     *
     * @param jda
     *        The not-null JDAImpl instance to use
     */
    public PresenceImpl(JDAImpl jda)
    {
        this.api = jda;
    }


    /* -- Public Getters -- */


    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public OnlineStatus getStatus()
    {
        return status;
    }

    @Override
    public Game getGame()
    {
        return game;
    }

    @Override
    public boolean isIdle()
    {
        return idle;
    }


    /* -- Public Setters -- */


    @Override
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

    @Override
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

    @Override
    public void setIdle(boolean idle)
    {
        JSONObject object = getFullPresence();
        object.put("afk", idle);
        update(object);
        this.idle = idle;
    }


    /* -- Impl Setters -- */


    public PresenceImpl setCacheStatus(OnlineStatus status)
    {
        if (status == null)
            throw new NullPointerException("Null OnlineStatus is not allowed.");
        if (status == OnlineStatus.OFFLINE)
            status = OnlineStatus.INVISIBLE;
        this.status = status;
        return this;
    }

    public PresenceImpl setCacheGame(Game game)
    {
        this.game = game;
        return this;
    }

    public PresenceImpl setCacheIdle(boolean idle)
    {
        this.idle = idle;
        return this;
    }


    /* -- Internal Methods -- */


    public JSONObject getFullPresence()
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
