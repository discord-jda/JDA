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

package net.dv8tion.jda.core.managers.impl;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.WebSocketCode;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.managers.Presence;
import net.dv8tion.jda.core.utils.Checks;
import org.json.JSONObject;

/**
 * The Presence associated with the provided JDA instance
 * <br><b>Note that this does not automatically handle the 5/60 second rate limit!</b>
 *
 * @since  3.0
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
        setPresence(status, game, idle);
    }

    @Override
    public void setGame(Game game)
    {
        setPresence(status, game);
    }

    @Override
    public void setIdle(boolean idle)
    {
        setPresence(status, idle);
    }

    @Override
    public void setPresence(OnlineStatus status, Game game, boolean idle)
    {
        JSONObject gameObj = getGameJson(game);

        Checks.check(status != OnlineStatus.UNKNOWN,
                "Cannot set the presence status to an unknown OnlineStatus!");
        if (status == OnlineStatus.OFFLINE || status == null)
            status = OnlineStatus.INVISIBLE;

        JSONObject object = new JSONObject();

        if (gameObj == null)
            object.put("game", JSONObject.NULL);
        else
            object.put("game", gameObj);
        object.put("afk", idle);
        object.put("status", status.getKey());
        object.put("since", System.currentTimeMillis());
        update(object);
        this.idle = idle;
        this.status = status;
        this.game = gameObj == null ? null : game;
    }

    @Override
    public void setPresence(OnlineStatus status, Game game)
    {
        setPresence(status, game, idle);
    }

    @Override
    public void setPresence(OnlineStatus status, boolean idle)
    {
        setPresence(status, game, idle);
    }

    @Override
    public void setPresence(Game game, boolean idle)
    {
        setPresence(status, game, idle);
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
        if (game.getUrl() != null)
            gameObj.put("url", game.getUrl());

        return gameObj;
    }


    /* -- Terminal -- */


    protected void update(JSONObject data)
    {
        api.getClient().send(new JSONObject()
            .put("d", data)
            .put("op", WebSocketCode.PRESENCE).toString());
    }

}
