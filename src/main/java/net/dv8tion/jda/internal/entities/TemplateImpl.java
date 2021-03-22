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
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel;
import net.dv8tion.jda.api.entities.Guild.NotificationLevel;
import net.dv8tion.jda.api.entities.Guild.Timeout;
import net.dv8tion.jda.api.entities.Guild.VerificationLevel;
import net.dv8tion.jda.api.entities.Template;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.TemplateManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.managers.TemplateManagerImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

public class TemplateImpl implements Template
{
    private final JDAImpl api;
    private final String code;
    private final String name;
    private final String description;
    private final int uses;
    private final User creator;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
    private final Template.Guild guild;
    private final boolean synced;

    protected TemplateManager manager;

    public TemplateImpl(final JDAImpl api, final String code, final String name, final String description,
                        final int uses, final User creator, final OffsetDateTime createdAt, final OffsetDateTime updatedAt,
                        final Template.Guild guild, final boolean synced)
    {
        this.api = api;
        this.code = code;
        this.name = name;
        this.description = description;
        this.uses = uses;
        this.creator = creator;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.guild = guild;
        this.synced = synced;
    }

    public static RestAction<Template> resolve(final JDA api, final String code)
    {
        Checks.notNull(code, "code");
        Checks.notNull(api, "api");

        Route.CompiledRoute route = Route.Templates.GET_TEMPLATE.compile(code);

        JDAImpl jda = (JDAImpl) api;
        return new RestActionImpl<>(api, route, (response, request) ->
                jda.getEntityBuilder().createTemplate(response.getObject()));
    }

    @Nonnull
    @Override
    public RestAction<Template> sync()
    {
        checkInteraction();
        final Route.CompiledRoute route = Route.Templates.SYNC_TEMPLATE.compile(guild.getId(), this.code);
        JDAImpl jda = (JDAImpl) api;
        return new RestActionImpl<>(api, route, (response, request) ->
                jda.getEntityBuilder().createTemplate(response.getObject()));
    }

    @Nonnull
    @Override
    public RestAction<Void> delete()
    {
        checkInteraction();
        final Route.CompiledRoute route = Route.Templates.DELETE_TEMPLATE.compile(guild.getId(), this.code);
        return new RestActionImpl<>(api, route);
    }

    @Nonnull
    @Override
    public String getCode()
    {
        return this.code;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return this.name;
    }

    @Nullable
    @Override
    public String getDescription()
    {
        return this.description;
    }

    @Override
    public int getUses()
    {
        return this.uses;
    }

    @Nonnull
    @Override
    public User getCreator()
    {
        return this.creator;
    }

    @Nonnull
    @Override
    public OffsetDateTime getTimeCreated()
    {
        return this.createdAt;
    }

    @Nonnull
    @Override
    public OffsetDateTime getTimeUpdated()
    {
        return this.updatedAt;
    }

    @Nonnull
    @Override
    public Template.Guild getGuild()
    {
        return this.guild;
    }

    @Override
    public boolean isSynced()
    {
        return this.synced;
    }

    @Nonnull
    @Override
    public TemplateManager getManager()
    {
        if (manager == null)
            return manager = new TemplateManagerImpl(this);
        return manager;
    }

    private void checkInteraction()
    {
        final net.dv8tion.jda.api.entities.Guild guild = this.api.getGuildById(this.guild.getIdLong());

        if (guild == null)
            throw new IllegalStateException("Cannot interact with a template without shared guild");
        if (!guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER))
            throw new InsufficientPermissionException(guild, Permission.MANAGE_SERVER);
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return this.api;
    }

    @Override
    public int hashCode()
    {
        return code.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof TemplateImpl))
            return false;
        TemplateImpl impl = (TemplateImpl) obj;
        return impl.code.equals(this.code);
    }

    @Override
    public String toString()
    {
        return "Template(" + this.code + ")";
    }

    public static class GuildImpl implements Guild
    {
        private final long id;
        private final String name, description, region, iconId;
        private final VerificationLevel verificationLevel;
        private final NotificationLevel notificationLevel;
        private final ExplicitContentLevel explicitContentLevel;
        private final Locale locale;
        private final Timeout afkTimeout;
        private final List<Template.Role> roles;

        public GuildImpl(final long id, final String name, final String description, final String region, final String iconId, final VerificationLevel verificationLevel,
                         final NotificationLevel notificationLevel, final ExplicitContentLevel explicitContentLevel, final Locale locale, final Timeout afkTimeout,
                         final List<Template.Role> roles)
        {
            this.id = id;
            this.name = name;
            this.description = description;
            this.region = region;
            this.iconId = iconId;
            this.verificationLevel = verificationLevel;
            this.notificationLevel = notificationLevel;
            this.explicitContentLevel = explicitContentLevel;
            this.locale = locale;
            this.afkTimeout = afkTimeout;
            this.roles = roles;
        }

        @Override
        public long getIdLong()
        {
            return this.id;
        }

        @Nonnull
        @Override
        public String getName()
        {
            return this.name;
        }

        @Nullable
        @Override
        public String getDescription()
        {
            return this.description;
        }

        @Nonnull
        @Override
        public Region getRegion()
        {
            return Region.fromKey(region);
        }

        @Nonnull
        @Override
        public String getRegionRaw()
        {
            return region;
        }

        @Nullable
        @Override
        public String getIconId()
        {
            return this.iconId;
        }

        @Nullable
        @Override
        public String getIconUrl()
        {
            return this.iconId == null ? null
                    : String.format(net.dv8tion.jda.api.entities.Guild.ICON_URL, this.id, this.iconId, iconId.startsWith("a_") ? "gif" : "png");
        }

        @Nonnull
        @Override
        public VerificationLevel getVerificationLevel()
        {
            return this.verificationLevel;
        }

        @Nonnull
        @Override
        public NotificationLevel getDefaultNotificationLevel()
        {
            return this.notificationLevel;
        }

        @Nonnull
        @Override
        public ExplicitContentLevel getExplicitContentLevel()
        {
            return this.explicitContentLevel;
        }

        @Nonnull
        @Override
        public Locale getLocale()
        {
            return this.locale;
        }

        @Nonnull
        @Override
        public Timeout getAfkTimeout()
        {
            return this.afkTimeout;
        }

        @Nonnull
        @Override
        public List<Role> getRoles()
        {
            return Collections.unmodifiableList(this.roles);
        }
    }

    public static class RoleImpl implements Role
    {
        private final long id;
        private final String name;
        private final int color;
        private final boolean hoisted;
        private final boolean mentionable;
        private final long rawPermissions;

        public RoleImpl(final long id, final String name, final int color, final boolean hoisted, final boolean mentionable, final long rawPermissions)
        {
            this.id = id;
            this.name = name;
            this.color = color;
            this.hoisted = hoisted;
            this.mentionable = mentionable;
            this.rawPermissions = rawPermissions;
        }

        @Override
        public long getIdLong()
        {
            return this.id;
        }

        @Nonnull
        @Override
        public String getName()
        {
            return this.name;
        }

        @Nullable
        @Override
        public Color getColor()
        {
            return this.color == net.dv8tion.jda.api.entities.Role.DEFAULT_COLOR_RAW ? null : new Color(this.color);
        }

        @Override
        public int getColorRaw()
        {
            return this.color;
        }

        @Override
        public boolean isHoisted()
        {
            return this.hoisted;
        }

        @Override
        public boolean isMentionable()
        {
            return this.mentionable;
        }

        @Nonnull
        @Override
        public EnumSet<Permission> getPermissions()
        {
            return Permission.getPermissions(rawPermissions);
        }

        @Override
        public long getPermissionsRaw()
        {
            return rawPermissions;
        }
    }
}
