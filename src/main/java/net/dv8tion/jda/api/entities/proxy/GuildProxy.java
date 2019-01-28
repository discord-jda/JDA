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

package net.dv8tion.jda.api.entities.proxy;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ProxyResolutionException;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.managers.GuildController;
import net.dv8tion.jda.api.managers.GuildManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MemberAction;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import net.dv8tion.jda.api.utils.cache.MemberCacheView;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class GuildProxy implements Guild, ProxyEntity
{
    private final long id;
    private final JDA api;

    public GuildProxy(Guild guild)
    {
        this.id = guild.getIdLong();
        this.api = guild.getJDA();
    }

    @Override
    public Guild getSubject()
    {
        Guild guild = getJDA().getGuildById(id);
        if (guild == null)
            throw new ProxyResolutionException("Guild(" + getId() + ")");
        return guild;
    }

    @Override
    public GuildProxy getProxy()
    {
        return this;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public RestAction<EnumSet<Region>> retrieveRegions(boolean includeDeprecated)
    {
        return getSubject().retrieveRegions(includeDeprecated);
    }

    @Override
    public MemberAction addMember(String accessToken, String userId)
    {
        return getSubject().addMember(accessToken, userId);
    }

    @Override
    public String getName()
    {
        return getSubject().getName();
    }

    @Override
    public String getIconId()
    {
        return getSubject().getIconId();
    }

    @Override
    public String getIconUrl()
    {
        return getSubject().getIconUrl();
    }

    @Override
    public Set<String> getFeatures()
    {
        return getSubject().getFeatures();
    }

    @Override
    public String getSplashId()
    {
        return getSubject().getSplashId();
    }

    @Override
    public String getSplashUrl()
    {
        return getSubject().getSplashUrl();
    }

    @Override
    public RestAction<String> getVanityUrl()
    {
        return getSubject().getVanityUrl();
    }

    @Override
    public VoiceChannel getAfkChannel()
    {
        return getSubject().getAfkChannel();
    }

    @Override
    public TextChannel getSystemChannel()
    {
        return getSubject().getSystemChannel();
    }

    @Override
    public Member getOwner()
    {
        return getSubject().getOwner();
    }

    @Override
    public long getOwnerIdLong()
    {
        return getSubject().getOwnerIdLong();
    }

    @Override
    public Timeout getAfkTimeout()
    {
        return getSubject().getAfkTimeout();
    }

    @Override
    public String getRegionRaw()
    {
        return getSubject().getRegionRaw();
    }

    @Override
    public boolean isMember(User user)
    {
        return getSubject().isMember(user);
    }

    @Override
    public Member getSelfMember()
    {
        return getSubject().getSelfMember();
    }

    @Override
    public Member getMember(User user)
    {
        return getSubject().getMember(user);
    }

    @Override
    public MemberCacheView getMemberCache()
    {
        return getSubject().getMemberCache();
    }

    @Override
    public SortedSnowflakeCacheView<Category> getCategoryCache()
    {
        return getSubject().getCategoryCache();
    }

    @Override
    public SortedSnowflakeCacheView<TextChannel> getTextChannelCache()
    {
        return getSubject().getTextChannelCache();
    }

    @Override
    public SortedSnowflakeCacheView<VoiceChannel> getVoiceChannelCache()
    {
        return getSubject().getVoiceChannelCache();
    }

    @Override
    public List<GuildChannel> getChannels(boolean includeHidden)
    {
        return getSubject().getChannels(includeHidden);
    }

    @Override
    public SortedSnowflakeCacheView<Role> getRoleCache()
    {
        return getSubject().getRoleCache();
    }

    @Override
    public SnowflakeCacheView<Emote> getEmoteCache()
    {
        return getSubject().getEmoteCache();
    }

    @Nonnull
    @Override
    public RestAction<List<ListedEmote>> retrieveEmotes()
    {
        return getSubject().retrieveEmotes();
    }

    @Nonnull
    @Override
    public RestAction<ListedEmote> retrieveEmoteById(@Nonnull String id)
    {
        return getSubject().retrieveEmoteById(id);
    }

    @Nonnull
    @Override
    public RestAction<List<Ban>> getBanList()
    {
        return getSubject().getBanList();
    }

    @Nonnull
    @Override
    public RestAction<Ban> getBanById(@Nonnull String userId)
    {
        return getSubject().getBanById(userId);
    }

    @Override
    public RestAction<Integer> getPrunableMemberCount(int days)
    {
        return getSubject().getPrunableMemberCount(days);
    }

    @Override
    public Role getPublicRole()
    {
        return getSubject().getPublicRole();
    }

    @Nullable
    @Override
    public TextChannel getDefaultChannel()
    {
        return getSubject().getDefaultChannel();
    }

    @Override
    public GuildManager getManager()
    {
        return getSubject().getManager();
    }

    @Override
    public GuildController getController()
    {
        return getSubject().getController();
    }

    @Override
    public AuditLogPaginationAction getAuditLogs()
    {
        return getSubject().getAuditLogs();
    }

    @Override
    public RestAction<Void> leave()
    {
        return getSubject().leave();
    }

    @Override
    public RestAction<Void> delete()
    {
        return getSubject().delete();
    }

    @Override
    public RestAction<Void> delete(String mfaCode)
    {
        return getSubject().delete(mfaCode);
    }

    @Override
    public AudioManager getAudioManager()
    {
        return getSubject().getAudioManager();
    }

    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public RestAction<List<Invite>> getInvites()
    {
        return getSubject().getInvites();
    }

    @Override
    public RestAction<List<Webhook>> getWebhooks()
    {
        return getSubject().getWebhooks();
    }

    @Override
    public List<GuildVoiceState> getVoiceStates()
    {
        return getSubject().getVoiceStates();
    }

    @Override
    public VerificationLevel getVerificationLevel()
    {
        return getSubject().getVerificationLevel();
    }

    @Override
    public NotificationLevel getDefaultNotificationLevel()
    {
        return getSubject().getDefaultNotificationLevel();
    }

    @Override
    public MFALevel getRequiredMFALevel()
    {
        return getSubject().getRequiredMFALevel();
    }

    @Override
    public ExplicitContentLevel getExplicitContentLevel()
    {
        return getSubject().getExplicitContentLevel();
    }

    @Override
    public boolean checkVerification()
    {
        return getSubject().checkVerification();
    }

    @Override
    public boolean isAvailable()
    {
        return getSubject().isAvailable();
    }

    @Override
    public int hashCode()
    {
        return getSubject().hashCode();
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj)
    {
        return obj == this || getSubject().equals(obj);
    }

    @Override
    public String toString()
    {
        return getSubject().toString();
    }
}
