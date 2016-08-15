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
package net.dv8tion.jda.entities.impl;

import net.dv8tion.jda.entities.Emote;
import net.dv8tion.jda.entities.Guild;

import java.util.*;

public class EmoteImpl implements Emote
{
    private final static String EMOTE_URL_PREFIX = "https://discordcdn.com/emojis/";
    private final String id;
    private final String name;
    private final Guild guild;

    public EmoteImpl(String name, String id)
    {
        this(name, id, null);
    }

    public EmoteImpl(String name, String id, Guild guild)
    {
        this.id = id;
        this.name = name;
        this.guild = guild;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public boolean isAvailable()
    {
        return guild != null;
    }

    @Override
    public Guild getGuild()
    {
        return guild;
    }

    @Override
    @Deprecated
    public List<Guild> getGuilds()
    {
        return Collections.singletonList(guild);
    }

    @Override
    public String getImageUrl()
    {
        return EMOTE_URL_PREFIX + getId() + ".png";
    }

    @Override
    public String getAsEmote()
    {
        return "<:" + getName() + ":" + getId() + ">";
    }

    @Override
    public String toString()
    {
        return "E:" + (isAvailable() ? getAsEmote() : getName()) + "(" + getId() + ")";
    }

}
