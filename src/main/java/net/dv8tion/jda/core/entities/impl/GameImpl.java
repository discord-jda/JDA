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
package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.core.entities.Game;

public class GameImpl implements Game
{
    private String name;
    private String url;
    private Game.GameType type;

    public GameImpl(String name, String url, GameType type)
    {
        this.name = name;
        this.url = url;
        this.type = type;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getUrl()
    {
        return url;
    }

    @Override
    public GameType getType()
    {
        return type;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof GameImpl))
            return false;

        Game oGame = (Game) o;
        if (oGame.getType() != type)
            return false;
        return type == oGame.getType()
                && ((name == null && oGame.getName() == null) || (name != null && name.equals(oGame.getName())))
                && ((url == null && oGame.getUrl() == null) || (url != null && url.equals(oGame.getUrl())));
    }

    @Override
    public int hashCode()
    {
        return (name + type + url).hashCode();
    }

    @Override
    public String toString()
    {
        if (url != null)
            return String.format("Game(%s | %s)", name, url);
        else
            return String.format("Game(%s)", name);
    }
}
