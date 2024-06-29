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

package net.dv8tion.jda.internal.entities.detached;

import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.automod.AutoModRule;
import net.dv8tion.jda.api.entities.automod.build.AutoModRuleData;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.entities.sticker.GuildSticker;
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake;
import net.dv8tion.jda.api.entities.templates.Template;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.PrivilegeConfig;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.IntegrationPrivilege;
import net.dv8tion.jda.api.managers.*;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.*;
import net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction;
import net.dv8tion.jda.api.requests.restaction.order.ChannelOrderAction;
import net.dv8tion.jda.api.requests.restaction.order.RoleOrderAction;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.cache.MemberCacheView;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView;
import net.dv8tion.jda.api.utils.concurrent.Task;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.detached.mixin.IDetachableEntityMixin;
import net.dv8tion.jda.internal.requests.restaction.pagination.BanPaginationActionImpl;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.cache.SortedChannelCacheViewImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DetachedGuildImpl implements Guild, IDetachableEntityMixin
{
    private final long id;
    private final JDAImpl api;

    private Set<String> features;
    private DiscordLocale preferredLocale = DiscordLocale.ENGLISH_US;

    public DetachedGuildImpl(JDAImpl api, long id)
    {
        this.id = id;
        this.api = api;
    }

    @Override
    public boolean isDetached()
    {
        return true;
    }

    @Nonnull
    @Override
    public RestAction<List<Command>> retrieveCommands(boolean withLocalizations)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<Command> retrieveCommandById(@Nonnull String id)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public CommandCreateAction upsertCommand(@Nonnull CommandData command)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public CommandListUpdateAction updateCommands()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public CommandEditAction editCommandById(@Nonnull String id)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<Void> deleteCommandById(@Nonnull String commandId)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<List<IntegrationPrivilege>> retrieveIntegrationPrivilegesById(@Nonnull String targetId)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<PrivilegeConfig> retrieveCommandPrivileges()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<EnumSet<Region>> retrieveRegions(boolean includeDeprecated)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<List<AutoModRule>> retrieveAutoModRules()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<AutoModRule> retrieveAutoModRuleById(@Nonnull String id)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditableRestAction<AutoModRule> createAutoModRule(@Nonnull AutoModRuleData rule)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AutoModRuleManager modifyAutoModRuleById(@Nonnull String id)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> deleteAutoModRuleById(@Nonnull String id)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public MemberAction addMember(@Nonnull String accessToken, @Nonnull UserSnowflake user)
    {
        throw detachedException();
    }

    @Override
    public boolean isLoaded()
    {
        throw detachedException();
    }

    @Override
    public void pruneMemberCache()
    {
        throw detachedException();
    }

    @Override
    public boolean unloadMember(long userId)
    {
        throw detachedException();
    }

    @Override
    public int getMemberCount()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public String getName()
    {
        throw detachedException();
    }

    @Override
    public String getIconId()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public Set<String> getFeatures()
    {
        return features;
    }

    @Override
    public String getSplashId()
    {
        throw detachedException();
    }

    @Nullable
    @Override
    public String getVanityCode()
    {
        throw detachedException();
    }

    @Override
    @Nonnull
    public RestAction<VanityInvite> retrieveVanityInvite()
    {
        throw detachedException();
    }

    @Nullable
    @Override
    public String getDescription()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public DiscordLocale getLocale()
    {
        return preferredLocale;
    }

    @Nullable
    @Override
    public String getBannerId()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public BoostTier getBoostTier()
    {
        throw detachedException();
    }

    @Override
    public int getBoostCount()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions") // can't be null here
    public List<Member> getBoosters()
    {
        throw detachedException();
    }

    @Override
    public int getMaxMembers()
    {
        throw detachedException();
    }

    @Override
    public int getMaxPresences()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<MetaData> retrieveMetaData()
    {
        throw detachedException();
    }

    @Override
    public VoiceChannel getAfkChannel()
    {
        throw detachedException();
    }

    @Override
    public TextChannel getSystemChannel()
    {
        throw detachedException();
    }

    @Override
    public TextChannel getRulesChannel()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public CacheRestAction<ScheduledEvent> retrieveScheduledEventById(@Nonnull String id)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public CacheRestAction<ScheduledEvent> retrieveScheduledEventById(long id)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ScheduledEventAction createScheduledEvent(@Nonnull String name, @Nonnull String location, @Nonnull OffsetDateTime startTime, @Nonnull OffsetDateTime endTime)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ScheduledEventAction createScheduledEvent(@Nonnull String name, @Nonnull GuildChannel channel, @Nonnull OffsetDateTime startTime)
    {
        throw detachedException();
    }


    @Override
    public TextChannel getCommunityUpdatesChannel()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<List<Webhook>> retrieveWebhooks()
    {
        throw detachedException();
    }

    @Override
    public Member getOwner()
    {
        throw detachedException();
    }

    @Override
    public long getOwnerIdLong()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public Timeout getAfkTimeout()
    {
        throw detachedException();
    }

    @Override
    public boolean isMember(@Nonnull UserSnowflake user)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public Member getSelfMember()
    {
        throw detachedException();
    }

    @Override
    public Member getMember(@Nonnull UserSnowflake user)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public Task<List<Member>> findMembers(@Nonnull Predicate<? super Member> filter)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public MemberCacheView getMemberCache()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public SortedSnowflakeCacheView<ScheduledEvent> getScheduledEventCache()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public SortedSnowflakeCacheView<Category> getCategoryCache()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public SortedSnowflakeCacheView<TextChannel> getTextChannelCache()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public SortedSnowflakeCacheView<NewsChannel> getNewsChannelCache()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public SortedSnowflakeCacheView<VoiceChannel> getVoiceChannelCache()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public SortedSnowflakeCacheView<ForumChannel> getForumChannelCache()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public SnowflakeCacheView<MediaChannel> getMediaChannelCache()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public SortedSnowflakeCacheView<StageChannel> getStageChannelCache()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public SortedSnowflakeCacheView<ThreadChannel> getThreadChannelCache()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public SortedChannelCacheViewImpl<GuildChannel> getChannelCache()
    {
        throw detachedException();
    }

    @Nullable
    @Override
    public GuildChannel getGuildChannelById(long id)
    {
        throw detachedException();
    }

    @Override
    public GuildChannel getGuildChannelById(@Nonnull ChannelType type, long id)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public SortedSnowflakeCacheView<Role> getRoleCache()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public SnowflakeCacheView<RichCustomEmoji> getEmojiCache()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public SnowflakeCacheView<GuildSticker> getStickerCache()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public List<GuildChannel> getChannels(boolean includeHidden)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<List<RichCustomEmoji>> retrieveEmojis()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<RichCustomEmoji> retrieveEmojiById(@Nonnull String id)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<RichCustomEmoji> retrieveEmoji(@Nonnull CustomEmoji emoji)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<List<GuildSticker>> retrieveStickers()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<GuildSticker> retrieveSticker(@Nonnull StickerSnowflake sticker)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public GuildStickerManager editSticker(@Nonnull StickerSnowflake sticker)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public BanPaginationActionImpl retrieveBanList()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<Ban> retrieveBan(@Nonnull UserSnowflake user)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<Integer> retrievePrunableMemberCount(int days)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public Role getPublicRole()
    {
        throw detachedException();
    }

    @Nullable
    @Override
    public DefaultGuildChannelUnion getDefaultChannel()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public GuildManager getManager()
    {
        throw detachedException();
    }

    @Override
    public boolean isBoostProgressBarEnabled()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditLogPaginationAction retrieveAuditLogs()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<Void> leave()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<Void> delete()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<Void> delete(String mfaCode)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AudioManager getAudioManager()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public synchronized Task<Void> requestToSpeak()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public synchronized Task<Void> cancelRequestToSpeak()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public JDAImpl getJDA()
    {
        return api;
    }

    @Nonnull
    @Override
    public List<GuildVoiceState> getVoiceStates()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public VerificationLevel getVerificationLevel()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public NSFWLevel getNSFWLevel()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public NotificationLevel getDefaultNotificationLevel()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public MFALevel getRequiredMFALevel()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ExplicitContentLevel getExplicitContentLevel()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public Task<Void> loadMembers(@Nonnull Consumer<Member> callback)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public CacheRestAction<Member> retrieveMemberById(long id)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public Task<List<Member>> retrieveMembersByIds(boolean includePresence, @Nonnull long... ids)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public Task<List<Member>> retrieveMembersByPrefix(@Nonnull String prefix, int limit)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<List<ThreadChannel>> retrieveActiveThreads()
    {
        throw detachedException();
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Nonnull
    @Override
    public RestAction<List<Invite>> retrieveInvites()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<List<Template>> retrieveTemplates()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<Template> createTemplate(@Nonnull String name, @Nullable String description)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<GuildWelcomeScreen> retrieveWelcomeScreen()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RestAction<Void> moveVoiceMember(@Nonnull Member member, @Nullable AudioChannel audioChannel)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> modifyNickname(@Nonnull Member member, String nickname)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditableRestAction<Integer> prune(int days, boolean wait, @Nonnull Role... roles)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> kick(@Nonnull UserSnowflake user)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> ban(@Nonnull UserSnowflake user, int duration, @Nonnull TimeUnit unit)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditableRestAction<BulkBanResponse> ban(@Nonnull Collection<UserSnowflake> users, @Nullable Duration deletionTime)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> unban(@Nonnull UserSnowflake user)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> timeoutUntil(@Nonnull UserSnowflake user, @Nonnull TemporalAccessor temporal)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> removeTimeout(@Nonnull UserSnowflake user)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> deafen(@Nonnull UserSnowflake user, boolean deafen)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> mute(@Nonnull UserSnowflake user, boolean mute)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> addRoleToMember(@Nonnull UserSnowflake user, @Nonnull Role role)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> removeRoleFromMember(@Nonnull UserSnowflake user, @Nonnull Role role)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> modifyMemberRoles(@Nonnull Member member, Collection<Role> rolesToAdd, Collection<Role> rolesToRemove)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> modifyMemberRoles(@Nonnull Member member, @Nonnull Collection<Role> roles)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> transferOwnership(@Nonnull Member newOwner)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ChannelAction<TextChannel> createTextChannel(@Nonnull String name, Category parent)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ChannelAction<NewsChannel> createNewsChannel(@Nonnull String name, Category parent)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ChannelAction<VoiceChannel> createVoiceChannel(@Nonnull String name, Category parent)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ChannelAction<StageChannel> createStageChannel(@Nonnull String name, Category parent)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ChannelAction<ForumChannel> createForumChannel(@Nonnull String name, Category parent)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ChannelAction<MediaChannel> createMediaChannel(@Nonnull String name, @Nullable Category parent)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ChannelAction<Category> createCategory(@Nonnull String name)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RoleAction createRole()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditableRestAction<RichCustomEmoji> createEmoji(@Nonnull String name, @Nonnull Icon icon, @Nonnull Role... roles)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditableRestAction<GuildSticker> createSticker(@Nonnull String name, @Nonnull String description, @Nonnull FileUpload file, @Nonnull Collection<String> tags)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> deleteSticker(@Nonnull StickerSnowflake id)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ChannelOrderAction modifyCategoryPositions()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ChannelOrderAction modifyTextChannelPositions()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public ChannelOrderAction modifyVoiceChannelPositions()
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public CategoryOrderAction modifyTextChannelPositions(@Nonnull Category category)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public CategoryOrderAction modifyVoiceChannelPositions(@Nonnull Category category)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public RoleOrderAction modifyRolePositions(boolean useAscendingOrder)
    {
        throw detachedException();
    }

    @Nonnull
    @Override
    public GuildWelcomeScreenManager modifyWelcomeScreen()
    {
        throw detachedException();
    }

    // ---- Setters -----

    public DetachedGuildImpl setFeatures(Set<String> features)
    {
        this.features = Collections.unmodifiableSet(features);
        return this;
    }

    public DetachedGuildImpl setLocale(DiscordLocale locale)
    {
        this.preferredLocale = locale;
        return this;
    }

    // -- Object overrides --

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
            return true;
        if (!(o instanceof DetachedGuildImpl))
            return false;
        DetachedGuildImpl oGuild = (DetachedGuildImpl) o;
        return this.id == oGuild.id;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public String toString()
    {
        return new EntityString(this).toString();
    }
}
