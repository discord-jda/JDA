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

package net.dv8tion.jda.bot.entities.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.dv8tion.jda.bot.entities.ApplicationInfo;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;

public class ApplicationInfoImpl implements ApplicationInfo
{
    private final JDA api;

    private final String description;
    private final String iconId;
    private final String id;
    private final String name;
    private final User owner;
    private final List<String> rpcOrigins;

    public ApplicationInfoImpl(final JDA api, final String description, final String iconId, final String id,
            final String name, final User owner, final List<String> rpcOrigins)
    {
        this.api = api;
        this.description = description;
        this.iconId = iconId;
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.rpcOrigins = rpcOrigins;
    }

    @Override
    public boolean equals(final Object obj)
    {
        return obj instanceof ApplicationInfoImpl && this.id.equals(((ApplicationInfoImpl) obj).id);
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
                : "https://cdn.discordapp.com/app-icons/" + this.id + '/' + this.iconId + ".jpg";
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public String getInviteUrl(final Collection<Permission> permissions)
    {
        return this.getInviteUrl(null, permissions);
    }

    @Override
    public String getInviteUrl(final Permission... permissions)
    {
        return this.getInviteUrl(null, permissions);
    }

    @Override
    public String getInviteUrl(final String guildId, final Collection<Permission> permissions)
    {
        return "https://discordapp.com/oauth2/authorize?client_id=" + this.getId() + "&scope=bot"
                + (permissions == null || permissions.isEmpty() ? "" : "&permissions=" + Permission.getRaw(permissions))
                + (guildId == null ? "" : "&guild_id=" + guildId);
    }

    @Override
    public String getInviteUrl(final String guildId, final Permission... permissions)
    {
        return "https://discordapp.com/oauth2/authorize?client_id=" + this.getId() + "&scope=bot"
                + (permissions == null || permissions.length == 0 ? ""
                        : "&permissions=" + Permission.getRaw(permissions))
                + (guildId == null ? "" : "&guild_id=" + guildId);
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
    public List<String> getRpcOrigins()
    {
        return Collections.unmodifiableList(this.rpcOrigins);
    }

    @Override
    public int hashCode()
    {
        return this.id.hashCode();
    }

    @Override
    public String toString()
    {
        return "ApplicationInfo(" + this.id + ")";
    }

}
