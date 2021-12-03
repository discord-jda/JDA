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

package net.dv8tion.jda.internal.managers;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.GuildManager;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuildManagerImpl extends ManagerBase<GuildManager> implements GuildManager
{
    protected Guild guild;

    protected String name;
    protected Icon icon, splash, banner;
    protected String afkChannel, systemChannel, rulesChannel, communityUpdatesChannel;
    protected String description;
    protected int afkTimeout;
    protected int mfaLevel;
    protected int notificationLevel;
    protected int explicitContentLevel;
    protected int verificationLevel;
    protected boolean boostProgressBarEnabled;

    public GuildManagerImpl(Guild guild)
    {
        super(guild.getJDA(), Route.Guilds.MODIFY_GUILD.compile(guild.getId()));
        JDA api = guild.getJDA();
        this.guild = guild;
        if (isPermissionChecksEnabled())
            checkPermissions();
    }

    @Nonnull
    @Override
    public Guild getGuild()
    {
        Guild realGuild = api.getGuildById(guild.getIdLong());
        if (realGuild != null)
            guild = realGuild;
        return guild;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GuildManagerImpl reset(long fields)
    {
        super.reset(fields);
        if ((fields & NAME) == NAME)
            this.name = null;
        if ((fields & ICON) == ICON)
            this.icon = null;
        if ((fields & SPLASH) == SPLASH)
            this.splash = null;
        if ((fields & AFK_CHANNEL) == AFK_CHANNEL)
            this.afkChannel = null;
        if ((fields & SYSTEM_CHANNEL) == SYSTEM_CHANNEL)
            this.systemChannel = null;
        if ((fields & RULES_CHANNEL) == RULES_CHANNEL)
            this.rulesChannel = null;
        if ((fields & COMMUNITY_UPDATES_CHANNEL) == COMMUNITY_UPDATES_CHANNEL)
            this.communityUpdatesChannel = null;
        if ((fields & DESCRIPTION) == DESCRIPTION)
            this.description = null;
        if ((fields & BANNER) == BANNER)
            this.banner = null;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GuildManagerImpl reset(long... fields)
    {
        super.reset(fields);
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GuildManagerImpl reset()
    {
        super.reset();
        this.name = null;
        this.icon = null;
        this.splash = null;
        this.description = null;
        this.banner = null;
        this.afkChannel = null;
        this.systemChannel = null;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setName(@Nonnull String name)
    {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 100, "Name");
        this.name = name;
        set |= NAME;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setIcon(Icon icon)
    {
        this.icon = icon;
        set |= ICON;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setSplash(Icon splash)
    {
        checkFeature("INVITE_SPLASH");
        this.splash = splash;
        set |= SPLASH;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setAfkChannel(VoiceChannel afkChannel)
    {
        Checks.check(afkChannel == null || afkChannel.getGuild().equals(getGuild()), "Channel must be from the same guild");
        this.afkChannel = afkChannel == null ? null : afkChannel.getId();
        set |= AFK_CHANNEL;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setSystemChannel(TextChannel systemChannel)
    {
        Checks.check(systemChannel == null || systemChannel.getGuild().equals(getGuild()), "Channel must be from the same guild");
        this.systemChannel = systemChannel == null ? null : systemChannel.getId();
        set |= SYSTEM_CHANNEL;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setRulesChannel(TextChannel rulesChannel)
    {
        Checks.check(rulesChannel == null || rulesChannel.getGuild().equals(getGuild()), "Channel must be from the same guild");
        this.rulesChannel = rulesChannel == null ? null : rulesChannel.getId();
        set |= RULES_CHANNEL;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setCommunityUpdatesChannel(TextChannel communityUpdatesChannel)
    {
        Checks.check(communityUpdatesChannel == null || communityUpdatesChannel.getGuild().equals(getGuild()), "Channel must be from the same guild");
        this.communityUpdatesChannel = communityUpdatesChannel == null ? null : communityUpdatesChannel.getId();
        set |= COMMUNITY_UPDATES_CHANNEL;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setAfkTimeout(@Nonnull Guild.Timeout timeout)
    {
        Checks.notNull(timeout, "Timeout");
        this.afkTimeout = timeout.getSeconds();
        set |= AFK_TIMEOUT;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setVerificationLevel(@Nonnull Guild.VerificationLevel level)
    {
        Checks.notNull(level, "Level");
        Checks.check(level != Guild.VerificationLevel.UNKNOWN, "Level must not be UNKNOWN");
        this.verificationLevel = level.getKey();
        set |= VERIFICATION_LEVEL;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setDefaultNotificationLevel(@Nonnull Guild.NotificationLevel level)
    {
        Checks.notNull(level, "Level");
        Checks.check(level != Guild.NotificationLevel.UNKNOWN, "Level must not be UNKNOWN");
        this.notificationLevel = level.getKey();
        set |= NOTIFICATION_LEVEL;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setRequiredMFALevel(@Nonnull Guild.MFALevel level)
    {
        Checks.notNull(level, "Level");
        Checks.check(level != Guild.MFALevel.UNKNOWN, "Level must not be UNKNOWN");
        this.mfaLevel = level.getKey();
        set |= MFA_LEVEL;
        return this;
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GuildManagerImpl setExplicitContentLevel(@Nonnull Guild.ExplicitContentLevel level)
    {
        Checks.notNull(level, "Level");
        Checks.check(level != Guild.ExplicitContentLevel.UNKNOWN, "Level must not be UNKNOWN");
        this.explicitContentLevel = level.getKey();
        set |= EXPLICIT_CONTENT_LEVEL;
        return this;
    }

    @Nonnull
    @Override
    public GuildManager setBanner(@Nullable Icon banner)
    {
        checkFeature("BANNER");
        this.banner = banner;
        set |= BANNER;
        return this;
    }

    @Nonnull
    @Override
    public GuildManager setDescription(@Nullable String description)
    {
        checkFeature("VERIFIED");
        this.description = description;
        set |= DESCRIPTION;
        return this;
    }

    @Nonnull
    @Override
    public GuildManager setBoostProgressBarEnabled(boolean enabled)
    {
        this.boostProgressBarEnabled = enabled;
        set |= BOOST_PROGRESS_BAR_ENABLED;
        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject body = DataObject.empty().put("name", getGuild().getName());
        if (shouldUpdate(NAME))
            body.put("name", name);
        if (shouldUpdate(AFK_TIMEOUT))
            body.put("afk_timeout", afkTimeout);
        if (shouldUpdate(ICON))
            body.put("icon", icon == null ? null : icon.getEncoding());
        if (shouldUpdate(SPLASH))
            body.put("splash", splash == null ? null : splash.getEncoding());
        if (shouldUpdate(AFK_CHANNEL))
            body.put("afk_channel_id", afkChannel);
        if (shouldUpdate(SYSTEM_CHANNEL))
            body.put("system_channel_id", systemChannel);
        if (shouldUpdate(RULES_CHANNEL))
            body.put("rules_channel_id", rulesChannel);
        if (shouldUpdate(COMMUNITY_UPDATES_CHANNEL))
            body.put("public_updates_channel_id", communityUpdatesChannel);
        if (shouldUpdate(VERIFICATION_LEVEL))
            body.put("verification_level", verificationLevel);
        if (shouldUpdate(NOTIFICATION_LEVEL))
            body.put("default_message_notifications", notificationLevel);
        if (shouldUpdate(MFA_LEVEL))
            body.put("mfa_level", mfaLevel);
        if (shouldUpdate(EXPLICIT_CONTENT_LEVEL))
            body.put("explicit_content_filter", explicitContentLevel);
        if (shouldUpdate(BANNER))
            body.put("banner", banner == null ? null : banner.getEncoding());
        if (shouldUpdate(DESCRIPTION))
            body.put("description", description);
        if (shouldUpdate(BOOST_PROGRESS_BAR_ENABLED))
            body.put("premium_progress_bar_enabled", boostProgressBarEnabled);

        reset(); //now that we've built our JSON object, reset the manager back to the non-modified state
        return getRequestBody(body);
    }

    @Override
    protected boolean checkPermissions()
    {
        if (!getGuild().getSelfMember().hasPermission(Permission.MANAGE_SERVER))
            throw new InsufficientPermissionException(getGuild(), Permission.MANAGE_SERVER);
        return super.checkPermissions();
    }

    private void checkFeature(String feature)
    {
        if (!getGuild().getFeatures().contains(feature))
            throw new IllegalStateException("This guild doesn't have the " + feature + " feature enabled");
    }
}
