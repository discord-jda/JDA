/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
import net.dv8tion.jda.api.entities.data.GuildData;
import net.dv8tion.jda.api.entities.data.MutableGuildData;
import net.dv8tion.jda.api.events.guild.update.*;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.requests.WebSocketClient;

import java.util.Collections;
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

        MutableGuildData data = guild.getMutableGuildData();
        GuildData oldData = data.copy();

        //////////////
        //  WARNING //
        //Do not rely on allContent past this point, this method is also called from GuildCreateHandler!
        //////////////
        long ownerId = content.getLong("owner_id");
        String vanityCode = content.getString("vanity_url_code", null);
        String name = content.getString("name");

        data.setMaxMembers(content.getInt("max_members", 0));
        data.setMaxPresences(content.getInt("max_presences", 5000));
        data.setBoostCount(content.getInt("premium_subscription_count", 0));
        data.setBoostTier(Guild.BoostTier.fromKey(content.getInt("premium_tier", 0)));
        data.setDescription(content.getString("description", null));
        data.setBannerId(content.getString("banner", null));
        data.setIconId(content.getString("icon", null));
        data.setSplashId(content.getString("splash", null));
        data.setRegion(content.getString("region"));
        data.setVerificationLevel(Guild.VerificationLevel.fromKey(content.getInt("verification_level")));
        data.setNotificationLevel(Guild.NotificationLevel.fromKey(content.getInt("default_message_notifications")));
        data.setMFALevel(Guild.MFALevel.fromKey(content.getInt("mfa_level")));
        data.setExplicitContentLevel(Guild.ExplicitContentLevel.fromKey(content.getInt("explicit_content_filter")));
        data.setAfkTimeout(Guild.Timeout.fromKey(content.getInt("afk_timeout")));
        data.setAfkChannelId(content.getUnsignedLong("afk_channel_id", 0));
        data.setSystemChannelId(content.getUnsignedLong("system_channel_id", 0));
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
        if (!Objects.equals(name, guild.getName()))
        {
            String oldName = guild.getName();
            guild.setName(name);
            getJDA().handleEvent(
                new GuildUpdateNameEvent(
                    getJDA(), responseNumber,
                    guild, oldName));
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
        if (!features.equals(guild.getFeatures()))
        {
            Set<String> oldFeatures = guild.getFeatures();
            guild.setFeatures(features);
            getJDA().handleEvent(
                new GuildUpdateFeaturesEvent(
                    getJDA(), responseNumber,
                    guild, oldFeatures));
        }

        handleDigestUpdate(guild, data, oldData);
        handleAtomicUpdates(guild, data, oldData);
        return null;
    }

    private void handleDigestUpdate(GuildImpl guild, MutableGuildData data, GuildData oldData)
    {
        if (data.equals(oldData))
            return;
        getJDA().handleEvent(
            new GuildUpdateDigestEvent(
                getJDA(), responseNumber,
                guild, oldData));
    }

    private void handleAtomicUpdates(GuildImpl guild, MutableGuildData data, GuildData oldData)
    {
        if (!Objects.equals(oldData.getDescription(), data.getDescription()))
        {
            getJDA().handleEvent(
                new GuildUpdateDescriptionEvent(
                    getJDA(), responseNumber,
                    guild, oldData.getDescription()));
        }
        if (!Objects.equals(oldData.getBannerId(), data.getBannerId()))
        {
            getJDA().handleEvent(
                new GuildUpdateBannerEvent(
                    getJDA(), responseNumber,
                    guild, oldData.getBannerId()));
        }
        if (oldData.getMaxMembers() != data.getMaxMembers())
        {
            getJDA().handleEvent(
                new GuildUpdateMaxMembersEvent(
                    getJDA(), responseNumber,
                    guild, oldData.getMaxMembers()));
        }
        if (oldData.getMaxPresences() != data.getMaxPresences())
        {
            getJDA().handleEvent(
                new GuildUpdateMaxPresencesEvent(
                    getJDA(), responseNumber,
                    guild, oldData.getMaxPresences()));
        }
        if (oldData.getBoostCount() != data.getBoostCount())
        {
            getJDA().handleEvent(
                new GuildUpdateBoostCountEvent(
                    getJDA(), responseNumber,
                    guild, oldData.getBoostCount()));
        }
        if (oldData.getBoostTier() != data.getBoostTier())
        {
            getJDA().handleEvent(
                new GuildUpdateBoostTierEvent(
                    getJDA(), responseNumber,
                    guild, oldData.getBoostTier()));
        }
        if (!Objects.equals(oldData.getIconId(), data.getIconId()))
        {
            getJDA().handleEvent(
                    new GuildUpdateIconEvent(
                            getJDA(), responseNumber,
                            guild, oldData.getIconId()));
        }
        if (!Objects.equals(oldData.getSplashId(), data.getSplashId()))
        {
            getJDA().handleEvent(
                    new GuildUpdateSplashEvent(
                            getJDA(), responseNumber,
                            guild, oldData.getSplashId()));
        }
        if (!Objects.equals(oldData.getRegion(), data.getRegion()))
        {
            getJDA().handleEvent(
                    new GuildUpdateRegionEvent(
                            getJDA(), responseNumber,
                            guild, oldData.getRegion()));
        }
        if (!Objects.equals(oldData.getVerificationLevel(), data.getVerificationLevel()))
        {
            getJDA().handleEvent(
                    new GuildUpdateVerificationLevelEvent(
                            getJDA(), responseNumber,
                            guild, oldData.getVerificationLevel()));
        }
        if (!Objects.equals(oldData.getNotificationLevel(), data.getNotificationLevel()))
        {
            getJDA().handleEvent(
                    new GuildUpdateNotificationLevelEvent(
                            getJDA(), responseNumber,
                            guild, oldData.getNotificationLevel()));
        }
        if (!Objects.equals(oldData.getMFALevel(), data.getMFALevel()))
        {
            getJDA().handleEvent(
                    new GuildUpdateMFALevelEvent(
                            getJDA(), responseNumber,
                            guild, oldData.getMFALevel()));
        }
        if (!Objects.equals(oldData.getExplicitContentLevel(), data.getExplicitContentLevel()))
        {
            getJDA().handleEvent(
                    new GuildUpdateExplicitContentLevelEvent(
                            getJDA(), responseNumber,
                            guild, oldData.getExplicitContentLevel()));
        }
        if (!Objects.equals(oldData.getAfkTimeout(), data.getAfkTimeout()))
        {
            getJDA().handleEvent(
                    new GuildUpdateAfkTimeoutEvent(
                            getJDA(), responseNumber,
                            guild, oldData.getAfkTimeout()));
        }
        if (!Objects.equals(oldData.getAfkChannelId(), data.getAfkChannelId()))
        {
            getJDA().handleEvent(
                    new GuildUpdateAfkChannelEvent(
                            getJDA(), responseNumber,
                            guild, guild.getVoiceChannelById(oldData.getAfkChannelId())));
        }
        if (!Objects.equals(oldData.getSystemChannelId(), data.getSystemChannelId()))
        {
            getJDA().handleEvent(
                    new GuildUpdateSystemChannelEvent(
                            getJDA(), responseNumber,
                            guild, guild.getTextChannelById(oldData.getSystemChannelId())));
        }
    }
}
