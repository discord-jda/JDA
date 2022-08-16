/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.ApplicationTeam;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class ApplicationInfoImpl implements ApplicationInfo
{
    private final JDA api;

    private final boolean doesBotRequireCodeGrant;
    private final boolean isBotPublic;
    private final long id;
    private final String iconId;
    private final String description;
    private final String termsOfServiceUrl;
    private final String privacyPolicyUrl;
    private final String name;
    private final User owner;
    private final ApplicationTeam team;
    private final List<String> tags;
    private final String customAuthUrl;
    private final long defaultAuthUrlPerms;
    private final List<String> defaultAuthUrlScopes;
    private String scopes = "bot";

    public ApplicationInfoImpl(JDA api, String description, boolean doesBotRequireCodeGrant, String iconId, long id,
            boolean isBotPublic, String name, String termsOfServiceUrl, String privacyPolicyUrl, User owner, ApplicationTeam team,
            List<String> tags, String customAuthUrl, long defaultAuthUrlPerms, List<String> defaultAuthUrlScopes)
    {
        this.api = api;
        this.description = description;
        this.doesBotRequireCodeGrant = doesBotRequireCodeGrant;
        this.iconId = iconId;
        this.id = id;
        this.isBotPublic = isBotPublic;
        this.name = name;
        this.termsOfServiceUrl = termsOfServiceUrl;
        this.privacyPolicyUrl = privacyPolicyUrl;
        this.owner = owner;
        this.team = team;
        this.tags = Collections.unmodifiableList(tags);
        this.customAuthUrl = customAuthUrl;
        this.defaultAuthUrlPerms = defaultAuthUrlPerms;
        this.defaultAuthUrlScopes = Collections.unmodifiableList(defaultAuthUrlScopes);
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

    @Nonnull
    @Override
    public String getDescription()
    {
        return this.description;
    }

    @Override
    public String getTermsOfServiceUrl()
    {
        return this.termsOfServiceUrl;
    }

    @Override
    public String getPrivacyPolicyUrl()
    {
        return this.privacyPolicyUrl;
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

    @Nonnull
    @Override
    public ApplicationTeam getTeam()
    {
        return team;
    }

    @Nonnull
    @Override
    public ApplicationInfo setRequiredScopes(@Nonnull Collection<String> scopes)
    {
        Checks.noneNull(scopes, "Scopes");
        this.scopes = String.join("+", scopes);
        if (!this.scopes.contains("bot"))
        {
            if (this.scopes.isEmpty())
                this.scopes = "bot";
            else
                this.scopes += "+bot";
        }
        return this;
    }

    @Override
    public long getIdLong()
    {
        return this.id;
    }

    @Nonnull
    @Override
    public String getInviteUrl(final String guildId, final Collection<Permission> permissions)
    {
        StringBuilder builder = new StringBuilder("https://discord.com/oauth2/authorize?client_id=");
        builder.append(this.getId());
        builder.append("&scope=").append(scopes);
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

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return this.api;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return this.name;
    }

    @Nonnull
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

    @Nonnull
    @Override
    public List<String> getTags()
    {
        return tags;
    }

    @Nullable
    @Override
    public String getCustomAuthorizationUrl()
    {
        return customAuthUrl;
    }

    @Nonnull
    @Override
    public EnumSet<Permission> getPermissions()
    {
        return Permission.getPermissions(defaultAuthUrlPerms);
    }

    @Override
    public long getPermissionsRaw()
    {
        return defaultAuthUrlPerms;
    }

    @Nonnull
    @Override
    public List<String> getScopes()
    {
        return defaultAuthUrlScopes;
    }

    @Override
    public String toString()
    {
        return "ApplicationInfo(" + this.id + ")";
    }

}
