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
package net.dv8tion.jda.core.handle;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.guild.update.*;
import org.json.JSONArray;
import org.json.JSONObject;

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
    protected Long handleInternally(JSONObject content)
    {
        final long id = content.getLong("id");
        if (api.getGuildLock().isLocked(id))
            return id;

        GuildImpl guild = (GuildImpl) api.getGuildMap().get(id);
        Member owner = guild.getMembersMap().get(content.getLong("owner_id"));
        String name = content.getString("name");
        String iconId = content.optString("icon", null);
        String splashId = content.optString("splash", null);
        String region = content.getString("region");
        Guild.VerificationLevel verificationLevel = Guild.VerificationLevel.fromKey(content.getInt("verification_level"));
        Guild.NotificationLevel notificationLevel = Guild.NotificationLevel.fromKey(content.getInt("default_message_notifications"));
        Guild.MFALevel mfaLevel = Guild.MFALevel.fromKey(content.getInt("mfa_level"));
        Guild.ExplicitContentLevel explicitContentLevel = Guild.ExplicitContentLevel.fromKey(content.getInt("explicit_content_filter"));
        Guild.Timeout afkTimeout = Guild.Timeout.fromKey(content.getInt("afk_timeout"));
        VoiceChannel afkChannel = content.isNull("afk_channel_id")
                ? null : guild.getVoiceChannelsMap().get(content.getLong("afk_channel_id"));
        TextChannel systemChannel = content.isNull("system_channel_id")
                ? null : guild.getTextChannelsMap().get(content.getLong("system_channel_id"));
        Set<String> features;
        if(!content.isNull("features"))
        {
            JSONArray featureArr = content.getJSONArray("features");
            features = StreamSupport.stream(featureArr.spliterator(), false).map(String::valueOf).collect(Collectors.toSet());
        }
        else
        {
            features = Collections.emptySet();
        }

        if (!Objects.equals(owner, guild.getOwner()))
        {
            Member oldOwner = guild.getOwner();
            guild.setOwner(owner);
            api.getEventManager().handle(
                    new GuildUpdateOwnerEvent(
                        api, responseNumber,
                        guild, oldOwner));
        }
        if (!Objects.equals(name, guild.getName()))
        {
            String oldName = guild.getName();
            guild.setName(name);
            api.getEventManager().handle(
                    new GuildUpdateNameEvent(
                            api, responseNumber,
                            guild, oldName));
        }
        if (!Objects.equals(iconId, guild.getIconId()))
        {
            String oldIconId = guild.getIconId();
            guild.setIconId(iconId);
            api.getEventManager().handle(
                    new GuildUpdateIconEvent(
                            api, responseNumber,
                            guild, oldIconId));
        }
        if (!features.equals(guild.getFeatures()))
        {
            Set<String> oldFeatures = guild.getFeatures();
            guild.setFeatures(features);
            api.getEventManager().handle(
                    new GuildUpdateFeaturesEvent(
                            api, responseNumber,
                            guild, oldFeatures));
        }
        if (!Objects.equals(splashId, guild.getSplashId()))
        {
            String oldSplashId = guild.getSplashId();
            guild.setSplashId(splashId);
            api.getEventManager().handle(
                    new GuildUpdateSplashEvent(
                            api, responseNumber,
                            guild, oldSplashId));
        }
        if (!Objects.equals(region, guild.getRegionRaw()))
        {
            String oldRegion = guild.getRegionRaw();
            guild.setRegion(region);
            api.getEventManager().handle(
                    new GuildUpdateRegionEvent(
                            api, responseNumber,
                            guild, oldRegion));
        }
        if (!Objects.equals(verificationLevel, guild.getVerificationLevel()))
        {
            Guild.VerificationLevel oldVerificationLevel = guild.getVerificationLevel();
            guild.setVerificationLevel(verificationLevel);
            api.getEventManager().handle(
                    new GuildUpdateVerificationLevelEvent(
                            api, responseNumber,
                            guild, oldVerificationLevel));
        }
        if (!Objects.equals(notificationLevel, guild.getDefaultNotificationLevel()))
        {
            Guild.NotificationLevel oldNotificationLevel = guild.getDefaultNotificationLevel();
            guild.setDefaultNotificationLevel(notificationLevel);
            api.getEventManager().handle(
                    new GuildUpdateNotificationLevelEvent(
                            api, responseNumber,
                            guild, oldNotificationLevel));
        }
        if (!Objects.equals(mfaLevel, guild.getRequiredMFALevel()))
        {
            Guild.MFALevel oldMfaLevel = guild.getRequiredMFALevel();
            guild.setRequiredMFALevel(mfaLevel);
            api.getEventManager().handle(
                    new GuildUpdateMFALevelEvent(
                            api, responseNumber,
                            guild, oldMfaLevel));
        }
        if (!Objects.equals(explicitContentLevel, guild.getExplicitContentLevel()))
        {
            Guild.ExplicitContentLevel oldExplicitContentLevel = guild.getExplicitContentLevel();
            guild.setExplicitContentLevel(explicitContentLevel);
            api.getEventManager().handle(
                    new GuildUpdateExplicitContentLevelEvent(
                            api, responseNumber,
                            guild, oldExplicitContentLevel));
        }
        if (!Objects.equals(afkTimeout, guild.getAfkTimeout()))
        {
            Guild.Timeout oldAfkTimeout = guild.getAfkTimeout();
            guild.setAfkTimeout(afkTimeout);
            api.getEventManager().handle(
                    new GuildUpdateAfkTimeoutEvent(
                            api, responseNumber,
                            guild, oldAfkTimeout));
        }
        if (!Objects.equals(afkChannel, guild.getAfkChannel()))
        {
            VoiceChannel oldAfkChannel = guild.getAfkChannel();
            guild.setAfkChannel(afkChannel);
            api.getEventManager().handle(
                    new GuildUpdateAfkChannelEvent(
                            api, responseNumber,
                            guild, oldAfkChannel));
        }
        if (!Objects.equals(systemChannel, guild.getSystemChannel()))
        {
            TextChannel oldSystemChannel = guild.getSystemChannel();
            guild.setSystemChannel(systemChannel);
            api.getEventManager().handle(
                    new GuildUpdateSystemChannelEvent(
                            api, responseNumber,
                            guild, oldSystemChannel));
        }
        return null;
    }
}
