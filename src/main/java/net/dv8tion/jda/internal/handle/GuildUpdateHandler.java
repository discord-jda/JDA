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
package net.dv8tion.jda.internal.handle;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.update.*;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.requests.WebSocketClient;

import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GuildUpdateHandler extends SocketHandler
{

    public GuildUpdateHandler(JDAImpl api)
    {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content)
    {
        final long id = content.getLong("id");
        if (getJDA().getGuildSetupController().isLocked(id))
            return id;

        GuildImpl guild = (GuildImpl) getJDA().getGuildById(id);
        if (guild == null)
        {
            EventCache.LOG.debug("Caching GUILD_UPDATE for guild with id: {}", id);
            getJDA().getEventCache().cache(EventCache.Type.GUILD, id, responseNumber, allContent, this::handle);
            return null;
        }

        //When member limits aren't initialized we don't fire an update event for them
        int maxMembers = content.getInt("max_members", 0);
        int maxPresences = content.getInt("max_presences", 5000);
        if (guild.getMaxMembers() == 0)
        {
            // Initialize member limits to avoid unwanted update events
            guild.setMaxPresences(maxPresences);
            guild.setMaxMembers(maxMembers);
        }

        long ownerId = content.getLong("owner_id");
        int boostCount = content.getInt("premium_subscription_count", 0);
        int boostTier = content.getInt("premium_tier", 0);
        String description = content.getString("description", null);
        String vanityCode = content.getString("vanity_url_code", null);
        String bannerId = content.getString("banner", null);
        String name = content.getString("name");
        String iconId = content.getString("icon", null);
        String splashId = content.getString("splash", null);
        Guild.VerificationLevel verificationLevel = Guild.VerificationLevel.fromKey(content.getInt("verification_level"));
        Guild.NotificationLevel notificationLevel = Guild.NotificationLevel.fromKey(content.getInt("default_message_notifications"));
        Guild.MFALevel mfaLevel = Guild.MFALevel.fromKey(content.getInt("mfa_level"));
        Guild.NSFWLevel nsfwLevel = Guild.NSFWLevel.fromKey(content.getInt("nsfw_level", -1));
        Guild.ExplicitContentLevel explicitContentLevel = Guild.ExplicitContentLevel.fromKey(content.getInt("explicit_content_filter"));
        Guild.Timeout afkTimeout = Guild.Timeout.fromKey(content.getInt("afk_timeout"));
        Locale locale = Locale.forLanguageTag(content.getString("preferred_locale"));
        VoiceChannel afkChannel = content.isNull("afk_channel_id")
                ? null : guild.getVoiceChannelsView().get(content.getLong("afk_channel_id"));
        TextChannel systemChannel = content.isNull("system_channel_id")
                ? null : guild.getTextChannelsView().get(content.getLong("system_channel_id"));
        TextChannel rulesChannel = content.isNull("rules_channel_id")
                ? null : guild.getTextChannelsView().get(content.getLong("rules_channel_id"));
        TextChannel communityUpdatesChannel = content.isNull("public_updates_channel_id")
                ? null : guild.getTextChannelsView().get(content.getLong("public_updates_channel_id"));
        Set<String> features;
        if (!content.isNull("features"))
        {
            DataArray featureArr = content.getArray("features");
            features = StreamSupport.stream(featureArr.spliterator(), false).map(String::valueOf).collect(Collectors.toSet());
        }
        else
        {
            features = Collections.emptySet();
        }

        if (ownerId != guild.getOwnerIdLong())
        {
            long oldOwnerId = guild.getOwnerIdLong();
            Member oldOwner = guild.getOwner();
            Member newOwner = guild.getMembersView().get(ownerId);
            if (newOwner == null)
                WebSocketClient.LOG.debug("Received {} with owner not in cache. UserId: {} GuildId: {}", allContent.get("t"), ownerId, id);
            guild.setOwner(newOwner);
            guild.setOwnerId(ownerId);
            getJDA().handleEvent(
                new GuildUpdateOwnerEvent(
                    getJDA(), responseNumber,
                    guild, oldOwner,
                    oldOwnerId, ownerId));
        }
        if (!Objects.equals(description, guild.getDescription()))
        {
            String oldDescription = guild.getDescription();
            guild.setDescription(description);
            getJDA().handleEvent(
                new GuildUpdateDescriptionEvent(
                    getJDA(), responseNumber,
                    guild, oldDescription));
        }
        if (!Objects.equals(bannerId, guild.getBannerId()))
        {
            String oldBanner = guild.getBannerId();
            guild.setBannerId(bannerId);
            getJDA().handleEvent(
                new GuildUpdateBannerEvent(
                    getJDA(), responseNumber,
                    guild, oldBanner));
        }
        if (!Objects.equals(vanityCode, guild.getVanityCode()))
        {
            String oldCode = guild.getVanityCode();
            guild.setVanityCode(vanityCode);
            getJDA().handleEvent(
                new GuildUpdateVanityCodeEvent(
                    getJDA(), responseNumber,
                    guild, oldCode));
        }
        if (maxMembers != guild.getMaxMembers())
        {
            int oldMax = guild.getMaxMembers();
            guild.setMaxMembers(maxMembers);
            getJDA().handleEvent(
                new GuildUpdateMaxMembersEvent(
                    getJDA(), responseNumber,
                    guild, oldMax));
        }
        if (maxPresences != guild.getMaxPresences())
        {
            int oldMax = guild.getMaxPresences();
            guild.setMaxPresences(maxPresences);
            getJDA().handleEvent(
                new GuildUpdateMaxPresencesEvent(
                    getJDA(), responseNumber,
                    guild, oldMax));
        }
        if (boostCount != guild.getBoostCount())
        {
            int oldCount = guild.getBoostCount();
            guild.setBoostCount(boostCount);
            getJDA().handleEvent(
                new GuildUpdateBoostCountEvent(
                    getJDA(), responseNumber,
                    guild, oldCount));
        }
        if (Guild.BoostTier.fromKey(boostTier) != guild.getBoostTier())
        {
            Guild.BoostTier oldTier = guild.getBoostTier();
            guild.setBoostTier(boostTier);
            getJDA().handleEvent(
                new GuildUpdateBoostTierEvent(
                    getJDA(), responseNumber,
                    guild, oldTier));
        }
        if (!Objects.equals(name, guild.getName()))
        {
            String oldName = guild.getName();
            guild.setName(name);
            getJDA().handleEvent(
                    new GuildUpdateNameEvent(
                            getJDA(), responseNumber,
                            guild, oldName));
        }
        if (!Objects.equals(iconId, guild.getIconId()))
        {
            String oldIconId = guild.getIconId();
            guild.setIconId(iconId);
            getJDA().handleEvent(
                    new GuildUpdateIconEvent(
                            getJDA(), responseNumber,
                            guild, oldIconId));
        }
        if (!features.equals(guild.getFeatures()))
        {
            Set<String> oldFeatures = guild.getFeatures();
            guild.setFeatures(features);
            getJDA().handleEvent(
                    new GuildUpdateFeaturesEvent(
                            getJDA(), responseNumber,
                            guild, oldFeatures));
        }
        if (!Objects.equals(splashId, guild.getSplashId()))
        {
            String oldSplashId = guild.getSplashId();
            guild.setSplashId(splashId);
            getJDA().handleEvent(
                    new GuildUpdateSplashEvent(
                            getJDA(), responseNumber,
                            guild, oldSplashId));
        }
        if (!Objects.equals(verificationLevel, guild.getVerificationLevel()))
        {
            Guild.VerificationLevel oldVerificationLevel = guild.getVerificationLevel();
            guild.setVerificationLevel(verificationLevel);
            getJDA().handleEvent(
                    new GuildUpdateVerificationLevelEvent(
                            getJDA(), responseNumber,
                            guild, oldVerificationLevel));
        }
        if (!Objects.equals(notificationLevel, guild.getDefaultNotificationLevel()))
        {
            Guild.NotificationLevel oldNotificationLevel = guild.getDefaultNotificationLevel();
            guild.setDefaultNotificationLevel(notificationLevel);
            getJDA().handleEvent(
                    new GuildUpdateNotificationLevelEvent(
                            getJDA(), responseNumber,
                            guild, oldNotificationLevel));
        }
        if (!Objects.equals(mfaLevel, guild.getRequiredMFALevel()))
        {
            Guild.MFALevel oldMfaLevel = guild.getRequiredMFALevel();
            guild.setRequiredMFALevel(mfaLevel);
            getJDA().handleEvent(
                    new GuildUpdateMFALevelEvent(
                            getJDA(), responseNumber,
                            guild, oldMfaLevel));
        }
        if (!Objects.equals(explicitContentLevel, guild.getExplicitContentLevel()))
        {
            Guild.ExplicitContentLevel oldExplicitContentLevel = guild.getExplicitContentLevel();
            guild.setExplicitContentLevel(explicitContentLevel);
            getJDA().handleEvent(
                    new GuildUpdateExplicitContentLevelEvent(
                            getJDA(), responseNumber,
                            guild, oldExplicitContentLevel));
        }
        if (!Objects.equals(afkTimeout, guild.getAfkTimeout()))
        {
            Guild.Timeout oldAfkTimeout = guild.getAfkTimeout();
            guild.setAfkTimeout(afkTimeout);
            getJDA().handleEvent(
                    new GuildUpdateAfkTimeoutEvent(
                            getJDA(), responseNumber,
                            guild, oldAfkTimeout));
        }
        if (!Objects.equals(locale, guild.getLocale()))
        {
            Locale oldLocale = guild.getLocale();
            guild.setLocale(locale.toLanguageTag());
            getJDA().handleEvent(
                new GuildUpdateLocaleEvent(
                    getJDA(), responseNumber,
                    guild, oldLocale));
        }
        if (!Objects.equals(afkChannel, guild.getAfkChannel()))
        {
            VoiceChannel oldAfkChannel = guild.getAfkChannel();
            guild.setAfkChannel(afkChannel);
            getJDA().handleEvent(
                    new GuildUpdateAfkChannelEvent(
                            getJDA(), responseNumber,
                            guild, oldAfkChannel));
        }
        if (!Objects.equals(systemChannel, guild.getSystemChannel()))
        {
            TextChannel oldSystemChannel = guild.getSystemChannel();
            guild.setSystemChannel(systemChannel);
            getJDA().handleEvent(
                    new GuildUpdateSystemChannelEvent(
                            getJDA(), responseNumber,
                            guild, oldSystemChannel));
        }
        if (!Objects.equals(rulesChannel, guild.getRulesChannel()))
        {
            TextChannel oldRulesChannel = guild.getRulesChannel();
            guild.setRulesChannel(rulesChannel);
            getJDA().handleEvent(
                    new GuildUpdateRulesChannelEvent(
                            getJDA(), responseNumber,
                            guild, oldRulesChannel));
        }
        if (!Objects.equals(communityUpdatesChannel, guild.getCommunityUpdatesChannel()))
        {
            TextChannel oldCommunityUpdatesChannel = guild.getCommunityUpdatesChannel();
            guild.setCommunityUpdatesChannel(communityUpdatesChannel);
            getJDA().handleEvent(
                    new GuildUpdateCommunityUpdatesChannelEvent(
                            getJDA(), responseNumber,
                            guild, oldCommunityUpdatesChannel));
        }
        if (content.hasKey("nsfw_level") && nsfwLevel != guild.getNSFWLevel())
        {
            Guild.NSFWLevel oldNSFWLevel = guild.getNSFWLevel();
            guild.setNSFWLevel(nsfwLevel);
            getJDA().handleEvent(
                    new GuildUpdateNSFWLevelEvent(
                            getJDA(), responseNumber,
                            guild, oldNSFWLevel));
        }
        return null;
    }
}
