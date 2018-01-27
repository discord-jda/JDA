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

package net.dv8tion.jda.bot.entities.impl;

import net.dv8tion.jda.bot.entities.ApplicationInfo;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;

import java.util.Collection;

public class ApplicationInfoImpl implements ApplicationInfo
{
    private final JDA api;


    private final boolean doesBotRequireCodeGrant;
    private final boolean isBotPublic;
    private final long id;
    private final String iconId;
    private final String description;
    private final String name;
    private final User owner;

    public ApplicationInfoImpl(JDA api, String description, boolean doesBotRequireCodeGrant, String iconId, long id,
            boolean isBotPublic, String name, User owner)
    {
        this.api = api;
        this.description = description;
        this.doesBotRequireCodeGrant = doesBotRequireCodeGrant;
        this.iconId = iconId;
        this.id = id;
        this.isBotPublic = isBotPublic;
        this.name = name;
        this.owner = owner;
    }

    @Override
    public final boolean doesBotRequireCodeGrant()
    {
        return this.doesBotRequireCodeGrant;
    }

    @Override
    public boolean equals(final Object obj)
    {
        return obj instanceof ApplicationInfoImpl && this.id == ((ApplicationInfoImpl) obj).id;
    }

    @Override
    public String getDescription()
    {
        return this.description;
    }

    @Override
    public String getIconId()
    {
        return this.iconId;
    }

    @Override
    public String getIconUrl()
    {
        return this.iconId == null ? null
                : "https://cdn.discordapp.com/app-icons/" + this.id + '/' + this.iconId + ".png";
    }

    @Override
    public long getIdLong()
    {
        return this.id;
    }

    @Override
    public String getInviteUrl(final String guildId, final Collection<Permission> permissions)
    {
        StringBuilder builder = new StringBuilder("https://discordapp.com/oauth2/authorize?client_id=");
        builder.append(this.getId());
        builder.append("&scope=bot");
        if (permissions != null && !permissions.isEmpty())
        {
            builder.append("&permissions=");
            builder.append(Permission.getRaw(permissions));
        }
        if (guildId != null)
        {
            builder.append("&guild_id=");
            builder.append(guildId);
        }
        return builder.toString();
    }

    @Override
    public JDA getJDA()
    {
        return this.api;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public User getOwner()
    {
        return this.owner;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(this.id);
    }

    @Override
    public final boolean isBotPublic()
    {
        return this.isBotPublic;
    }

    @Override
    public String toString()
    {
        return "ApplicationInfo(" + this.id + ")";
    }

}
