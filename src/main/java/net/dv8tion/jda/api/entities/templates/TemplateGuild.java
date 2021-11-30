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

package net.dv8tion.jda.api.entities.templates;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel;
import net.dv8tion.jda.api.entities.Guild.NotificationLevel;
import net.dv8tion.jda.api.entities.Guild.Timeout;
import net.dv8tion.jda.api.entities.Guild.VerificationLevel;
import net.dv8tion.jda.api.entities.ISnowflake;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * POJO for the guild information provided by a template.
 *
 * @see Template#getGuild()
 */
public class TemplateGuild implements ISnowflake
{
    private final long id;
    private final String name, description, iconId;
    private final VerificationLevel verificationLevel;
    private final NotificationLevel notificationLevel;
    private final ExplicitContentLevel explicitContentLevel;
    private final Locale locale;
    private final Timeout afkTimeout;
    private final TemplateChannel afkChannel;
    private final TemplateChannel systemChannel;
    private final List<TemplateRole> roles;
    private final List<TemplateChannel> channels;

    public TemplateGuild(final long id, final String name, final String description, final String iconId, final VerificationLevel verificationLevel,
                         final NotificationLevel notificationLevel, final ExplicitContentLevel explicitContentLevel, final Locale locale, final Timeout afkTimeout,
                         final TemplateChannel afkChannel, final TemplateChannel systemChannel, final List<TemplateRole> roles, final List<TemplateChannel> channels)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconId = iconId;
        this.verificationLevel = verificationLevel;
        this.notificationLevel = notificationLevel;
        this.explicitContentLevel = explicitContentLevel;
        this.locale = locale;
        this.afkTimeout = afkTimeout;
        this.afkChannel = afkChannel;
        this.systemChannel = systemChannel;
        this.roles = Collections.unmodifiableList(roles);
        this.channels = Collections.unmodifiableList(channels);
    }

    @Override
    public long getIdLong()
    {
        return this.id;
    }

    /**
     * The name of this guild.
     *
     * @return The guild's name
     */
    @Nonnull
    public String getName()
    {
        return this.name;
    }

    /**
     * The description for this guild.
     * <br>This is displayed in the server browser below the guild name for verified guilds.
     *
     * @return The description
     */
    @Nullable
    public String getDescription()
    {
        return this.description;
    }

    /**
     * The icon id of this guild.
     *
     * @return The guild's icon id
     *
     * @see    #getIconUrl()
     */
    @Nullable
    public String getIconId()
    {
        return this.iconId;
    }

    /**
     * The icon url of this guild.
     *
     * @return The guild's icon url
     *
     * @see    #getIconId()
     */
    @Nullable
    public String getIconUrl()
    {
        return this.iconId == null ? null
                : String.format(Guild.ICON_URL, this.id, this.iconId, iconId.startsWith("a_") ? "gif" : "png");
    }

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.Guild.VerificationLevel VerificationLevel} of this guild.
     *
     * @return the verification level of the guild
     */
    @Nonnull
    public VerificationLevel getVerificationLevel()
    {
        return this.verificationLevel;
    }

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.Guild.NotificationLevel NotificationLevel} of this guild.
     *
     * @return the notification level of the guild
     */
    @Nonnull
    public NotificationLevel getDefaultNotificationLevel()
    {
        return this.notificationLevel;
    }

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel ExplicitContentLevel} of this guild.
     *
     * @return the explicit content level of the guild
     */
    @Nonnull
    public ExplicitContentLevel getExplicitContentLevel()
    {
        return this.explicitContentLevel;
    }

    /**
     * The preferred locale for this guild.
     *
     * @return The preferred {@link Locale} for this guild
     */
    @Nonnull
    public Locale getLocale()
    {
        return this.locale;
    }

    /**
     * Returns the {@link net.dv8tion.jda.api.entities.Guild.Timeout AFK Timeout} for this guild.
     *
     * @return the afk timeout for this guild
     */
    @Nonnull
    public Timeout getAfkTimeout()
    {
        return this.afkTimeout;
    }

    /**
     * Provides the {@link net.dv8tion.jda.api.entities.templates.TemplateChannel TemplateChannel} that has been set as the channel
     * which {@link net.dv8tion.jda.api.entities.Member Members} will be moved to after they have been inactive in a
     * {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel} for longer than {@link #getAfkTimeout()}.
     * <br>If no channel has been set as the AFK channel, this returns {@code null}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.templates.TemplateChannel TemplateChannel} that is the AFK Channel.
     */
    @Nullable
    public TemplateChannel getAfkChannel()
    {
        return this.afkChannel;
    }

    /**
     * Provides the {@link net.dv8tion.jda.api.entities.templates.TemplateChannel TemplateChannel} that has been set as the channel
     * which newly joined {@link net.dv8tion.jda.api.entities.Member Members} will be announced in.
     * <br>If no channel has been set as the system channel, this returns {@code null}.
     *
     * @return Possibly-null {@link net.dv8tion.jda.api.entities.templates.TemplateChannel TemplateChannel} that is the system Channel.
     */
    @Nullable
    public TemplateChannel getSystemChannel()
    {
        return this.systemChannel;
    }

    /**
     * Gets all {@link net.dv8tion.jda.api.entities.templates.TemplateRole Roles} in this {@link net.dv8tion.jda.api.entities.templates.TemplateGuild Guild}.
     *
     * @return An immutable List of {@link net.dv8tion.jda.api.entities.templates.TemplateRole Roles}.
     */
    @Nonnull
    public List<TemplateRole> getRoles()
    {
        return this.roles;
    }

    /**
     * Gets all {@link net.dv8tion.jda.api.entities.templates.TemplateChannel Channels} in this {@link net.dv8tion.jda.api.entities.templates.TemplateGuild Guild}.
     *
     * @return An immutable List of {@link net.dv8tion.jda.api.entities.templates.TemplateChannel Channels}.
     */
    @Nonnull
    public List<TemplateChannel> getChannels()
    {
        return this.channels;
    }
}
