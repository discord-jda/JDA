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

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.templates.Template;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.managers.GuildManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.*;
import net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction;
import net.dv8tion.jda.api.requests.restaction.order.ChannelOrderAction;
import net.dv8tion.jda.api.requests.restaction.order.RoleOrderAction;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import net.dv8tion.jda.api.utils.cache.*;
import net.dv8tion.jda.api.utils.concurrent.Task;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.managers.AudioManagerImpl;
import net.dv8tion.jda.internal.managers.GuildManagerImpl;
import net.dv8tion.jda.internal.requests.*;
import net.dv8tion.jda.internal.requests.restaction.*;
import net.dv8tion.jda.internal.requests.restaction.order.CategoryOrderActionImpl;
import net.dv8tion.jda.internal.requests.restaction.order.ChannelOrderActionImpl;
import net.dv8tion.jda.internal.requests.restaction.order.RoleOrderActionImpl;
import net.dv8tion.jda.internal.requests.restaction.pagination.AuditLogPaginationActionImpl;
import net.dv8tion.jda.internal.utils.*;
import net.dv8tion.jda.internal.utils.cache.AbstractCacheView;
import net.dv8tion.jda.internal.utils.cache.MemberCacheViewImpl;
import net.dv8tion.jda.internal.utils.cache.SnowflakeCacheViewImpl;
import net.dv8tion.jda.internal.utils.cache.SortedSnowflakeCacheViewImpl;
import net.dv8tion.jda.internal.utils.concurrent.task.GatewayTask;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GuildImpl implements Guild
{
    private final long id;
    private final JDAImpl api;

    private final SortedSnowflakeCacheViewImpl<Category> categoryCache = new SortedSnowflakeCacheViewImpl<>(Category.class, Channel::getName, Comparator.naturalOrder());
    private final SortedSnowflakeCacheViewImpl<VoiceChannel> voiceChannelCache = new SortedSnowflakeCacheViewImpl<>(VoiceChannel.class, Channel::getName, Comparator.naturalOrder());
    private final SortedSnowflakeCacheViewImpl<StoreChannel> storeChannelCache = new SortedSnowflakeCacheViewImpl<>(StoreChannel.class, Channel::getName, Comparator.naturalOrder());
    private final SortedSnowflakeCacheViewImpl<TextChannel> textChannelCache = new SortedSnowflakeCacheViewImpl<>(TextChannel.class, Channel::getName, Comparator.naturalOrder());
    private final SortedSnowflakeCacheViewImpl<NewsChannel> newsChannelCache = new SortedSnowflakeCacheViewImpl<>(NewsChannel.class, Channel::getName, Comparator.naturalOrder());
    private final SortedSnowflakeCacheViewImpl<StageChannel> stageChannelCache = new SortedSnowflakeCacheViewImpl<>(StageChannel.class, Channel::getName, Comparator.naturalOrder());
    private final SortedSnowflakeCacheViewImpl<ThreadChannel> threadChannelCache = new SortedSnowflakeCacheViewImpl<>(ThreadChannel.class, Channel::getName, Comparator.naturalOrder());
    private final SortedSnowflakeCacheViewImpl<GuildScheduledEvent> scheduledEventCache = new SortedSnowflakeCacheViewImpl<>(GuildScheduledEvent.class, GuildScheduledEvent::getName, Comparator.naturalOrder());
    private final SortedSnowflakeCacheViewImpl<Role> roleCache = new SortedSnowflakeCacheViewImpl<>(Role.class, Role::getName, Comparator.reverseOrder());
    private final SnowflakeCacheViewImpl<Emote> emoteCache = new SnowflakeCacheViewImpl<>(Emote.class, Emote::getName);
    private final MemberCacheViewImpl memberCache = new MemberCacheViewImpl();
    private final CacheView.SimpleCacheView<MemberPresenceImpl> memberPresences;

    private GuildManager manager;
    private CompletableFuture<Void> pendingRequestToSpeak;

    private Member owner;
    private String name;
    private String iconId, splashId;
    private String vanityCode;
    private String description, banner;
    private int maxPresences, maxMembers;
    private int boostCount;
    private long ownerId;
    private Set<String> features;
    private VoiceChannel afkChannel;
    private TextChannel systemChannel;
    private TextChannel rulesChannel;
    private TextChannel communityUpdatesChannel;
    private Role publicRole;
    private VerificationLevel verificationLevel = VerificationLevel.UNKNOWN;
    private NotificationLevel defaultNotificationLevel = NotificationLevel.UNKNOWN;
    private MFALevel mfaLevel = MFALevel.UNKNOWN;
    private ExplicitContentLevel explicitContentLevel = ExplicitContentLevel.UNKNOWN;
    private NSFWLevel nsfwLevel = NSFWLevel.UNKNOWN;
    private Timeout afkTimeout;
    private BoostTier boostTier = BoostTier.NONE;
    private Locale preferredLocale = Locale.ENGLISH;
    private int memberCount;
    private boolean boostProgressBarEnabled;

    public GuildImpl(JDAImpl api, long id)
    {
        this.id = id;
        this.api = api;
        if (api.getCacheFlags().stream().anyMatch(CacheFlag::isPresence))
            memberPresences = new CacheView.SimpleCacheView<>(MemberPresenceImpl.class, null);
        else
            memberPresences = null;
    }

    @Nonnull
    @Override
    public RestAction<List<Command>> retrieveCommands()
    {
        Route.CompiledRoute route = Route.Interactions.GET_GUILD_COMMANDS.compile(getJDA().getSelfUser().getApplicationId(), getId());
        return new RestActionImpl<>(getJDA(), route,
                (response, request) ->
                        response.getArray()
                                .stream(DataArray::getObject)
                                .map(json -> new Command(getJDA(), this, json))
                                .collect(Collectors.toList()));
    }

    @Nonnull
    @Override
    public RestAction<Command> retrieveCommandById(@Nonnull String id)
    {
        Checks.isSnowflake(id);
        Route.CompiledRoute route = Route.Interactions.GET_GUILD_COMMAND.compile(getJDA().getSelfUser().getApplicationId(), getId(), id);
        return new RestActionImpl<>(getJDA(), route, (response, request) -> new Command(getJDA(), this, response.getObject()));
    }

    @Nonnull
    @Override
    public CommandCreateAction upsertCommand(@Nonnull CommandData command)
    {
        Checks.notNull(command, "CommandData");
        return new CommandCreateActionImpl(this, command);
    }

    @Nonnull
    @Override
    public CommandListUpdateAction updateCommands()
    {
        Route.CompiledRoute route = Route.Interactions.UPDATE_GUILD_COMMANDS.compile(getJDA().getSelfUser().getApplicationId(), getId());
        return new CommandListUpdateActionImpl(getJDA(), this, route);
    }

    @Nonnull
    @Override
    public CommandEditAction editCommandById(@Nonnull String id)
    {
        Checks.isSnowflake(id);
        return new CommandEditActionImpl(this, id);
    }

    @Nonnull
    @Override
    public RestAction<Void> deleteCommandById(@Nonnull String commandId)
    {
        Checks.isSnowflake(commandId);
        Route.CompiledRoute route = Route.Interactions.DELETE_GUILD_COMMAND.compile(getJDA().getSelfUser().getApplicationId(), getId(), commandId);
        return new RestActionImpl<>(getJDA(), route);
    }

    @Nonnull
    @Override
    public RestAction<List<CommandPrivilege>> retrieveCommandPrivilegesById(@Nonnull String commandId)
    {
        Checks.isSnowflake(commandId, "ID");
        Route.CompiledRoute route = Route.Interactions.GET_COMMAND_PERMISSIONS.compile(getJDA().getSelfUser().getApplicationId(), getId(), commandId);
        return new RestActionImpl<>(getJDA(), route, (response, request) -> parsePrivilegesList(response.getObject()));
    }

    @Nonnull
    @Override
    public RestAction<Map<String, List<CommandPrivilege>>> retrieveCommandPrivileges()
    {
        Route.CompiledRoute route = Route.Interactions.GET_ALL_COMMAND_PERMISSIONS.compile(getJDA().getSelfUser().getApplicationId(), getId());
        return new RestActionImpl<>(getJDA(), route, (response, request) -> {
            Map<String, List<CommandPrivilege>> privileges = new HashMap<>();
            response.getArray().stream(DataArray::getObject).forEach(obj -> {
                String id = obj.getString("id");
                List<CommandPrivilege> list = parsePrivilegesList(obj);
                privileges.put(id, list);
            });
            return privileges;
        });
    }

    @Nonnull
    @Override
    public RestAction<List<CommandPrivilege>> updateCommandPrivilegesById(@Nonnull String id, @Nonnull Collection<? extends CommandPrivilege> privileges)
    {
        Checks.isSnowflake(id, "ID");
        Checks.noneNull(privileges, "Privileges");
        Checks.check(privileges.size() <= 10, "Cannot have more than 10 privileges for a command!");
        Route.CompiledRoute route = Route.Interactions.EDIT_COMMAND_PERMISSIONS.compile(getJDA().getSelfUser().getApplicationId(), getId(), id);
        DataArray array = DataArray.fromCollection(privileges);
        return new RestActionImpl<>(getJDA(), route, DataObject.empty().put("permissions", array),
            (response, request) -> parsePrivilegesList(response.getObject()));
    }

    @Nonnull
    @Override
    public RestAction<Map<String, List<CommandPrivilege>>> updateCommandPrivileges(@Nonnull Map<String, Collection<? extends CommandPrivilege>> privileges)
    {
        Checks.notNull(privileges, "Privileges");
        privileges.forEach((key, value) -> {
            Checks.isSnowflake(key, "Map Key");
            Checks.noneNull(value, "Privilege List for Command");
            Checks.check(value.size() <= 10, "Cannot have more than 10 privileges for a command!");
        });
        DataArray array = DataArray.empty();
        privileges.forEach((commandId, list) -> {
            DataObject entry = DataObject.empty();
            entry.put("id", commandId);
            entry.put("permissions", DataArray.fromCollection(list));
            array.add(entry);
        });

        Route.CompiledRoute route = Route.Interactions.EDIT_ALL_COMMAND_PERMISSIONS.compile(getJDA().getSelfUser().getApplicationId(), getId());
        return new RestActionImpl<>(getJDA(), route, RequestBody.create(Requester.MEDIA_TYPE_JSON, array.toJson()), (response, request) -> {
            Map<String, List<CommandPrivilege>> map = new HashMap<>();
            response.getArray().stream(DataArray::getObject).forEach(obj -> {
                String id = obj.getString("id");
                List<CommandPrivilege> list = parsePrivilegesList(obj);
                map.put(id, list);
            });
            return map;
        });
    }

    private List<CommandPrivilege> parsePrivilegesList(DataObject obj)
    {
        return obj.getArray("permissions")
                .stream(DataArray::getObject)
                .map(this::parsePrivilege)
                .collect(Collectors.toList());
    }

    private CommandPrivilege parsePrivilege(DataObject data)
    {
        CommandPrivilege.Type type = CommandPrivilege.Type.fromKey(data.getInt("type", 1));
        boolean enabled = data.getBoolean("permission");
        return new CommandPrivilege(type, enabled, data.getUnsignedLong("id"));
    }

    @Nonnull
    @Override
    public RestAction<EnumSet<Region>> retrieveRegions(boolean includeDeprecated)
    {
        Route.CompiledRoute route = Route.Guilds.GET_VOICE_REGIONS.compile(getId());
        return new RestActionImpl<>(getJDA(), route, (response, request) ->
        {
            EnumSet<Region> set = EnumSet.noneOf(Region.class);
            DataArray arr = response.getArray();
            for (int i = 0; i < arr.length(); i++)
            {
                DataObject obj = arr.getObject(i);
                if (!includeDeprecated && obj.getBoolean("deprecated"))
                    continue;
                String id = obj.getString("id", "");
                Region region = Region.fromKey(id);
                if (region != Region.UNKNOWN)
                    set.add(region);
            }
            return set;
        });
    }

    @Nonnull
    @Override
    public MemberAction addMember(@Nonnull String accessToken, @Nonnull String userId)
    {
        Checks.notBlank(accessToken, "Access-Token");
        Checks.isSnowflake(userId, "User ID");
        Checks.check(getMemberById(userId) == null, "User is already in this guild");
        if (!getSelfMember().hasPermission(Permission.CREATE_INSTANT_INVITE))
            throw new InsufficientPermissionException(this, Permission.CREATE_INSTANT_INVITE);
        return new MemberActionImpl(getJDA(), this, userId, accessToken);
    }

    @Override
    public boolean isLoaded()
    {
        // Only works with GUILD_MEMBERS intent
        return getJDA().isIntent(GatewayIntent.GUILD_MEMBERS)
                && (long) getMemberCount() <= getMemberCache().size();
    }

    @Override
    public void pruneMemberCache()
    {
        try (UnlockHook h = memberCache.writeLock())
        {
            EntityBuilder builder = getJDA().getEntityBuilder();
            Set<Member> members = memberCache.asSet();
            members.forEach(m -> builder.updateMemberCache((MemberImpl) m));
        }
    }

    @Override
    public boolean unloadMember(long userId)
    {
        if (userId == api.getSelfUser().getIdLong())
            return false;
        MemberImpl member = (MemberImpl) getMemberById(userId);
        if (member == null)
            return false;
        api.getEntityBuilder().updateMemberCache(member, true);
        return true;
    }

    @Override
    public int getMemberCount()
    {
        return memberCount;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getIconId()
    {
        return iconId;
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
        return splashId;
    }

    @Nullable
    @Override
    public String getVanityCode()
    {
        return vanityCode;
    }

    @Override
    @Nonnull
    public RestAction<VanityInvite> retrieveVanityInvite()
    {
        checkPermission(Permission.MANAGE_SERVER);
        JDAImpl api = getJDA();
        Route.CompiledRoute route = Route.Guilds.GET_VANITY_URL.compile(getId());
        return new RestActionImpl<>(api, route,
            (response, request) -> new VanityInvite(vanityCode, response.getObject().getInt("uses")));
    }

    @Nullable
    @Override
    public String getDescription()
    {
        return description;
    }

    @Nonnull
    @Override
    public Locale getLocale()
    {
        return preferredLocale;
    }

    @Nullable
    @Override
    public String getBannerId()
    {
        return banner;
    }

    @Nonnull
    @Override
    public BoostTier getBoostTier()
    {
        return boostTier;
    }

    @Override
    public int getBoostCount()
    {
        return boostCount;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions") // can't be null here
    public List<Member> getBoosters()
    {
        return memberCache.applyStream((members) ->
            members.filter(m -> m.getTimeBoosted() != null)
                   .sorted(Comparator.comparing(Member::getTimeBoosted))
                   .collect(Collectors.toList()));
    }

    @Override
    public int getMaxMembers()
    {
        return maxMembers;
    }

    @Override
    public int getMaxPresences()
    {
        return maxPresences;
    }

    @Nonnull
    @Override
    public RestAction<MetaData> retrieveMetaData()
    {
        Route.CompiledRoute route = Route.Guilds.GET_GUILD.compile(getId());
        route = route.withQueryParams("with_counts", "true");
        return new RestActionImpl<>(getJDA(), route, (response, request) -> {
            DataObject json = response.getObject();
            int memberLimit = json.getInt("max_members", 0);
            int presenceLimit = json.getInt("max_presences", 5000);
            this.maxMembers = memberLimit;
            this.maxPresences = presenceLimit;
            int approxMembers = json.getInt("approximate_member_count", this.memberCount);
            int approxPresence = json.getInt("approximate_presence_count", 0);
            return new MetaData(memberLimit, presenceLimit, approxPresence, approxMembers);
        });
    }

    @Override
    public VoiceChannel getAfkChannel()
    {
        return afkChannel;
    }

    @Override
    public TextChannel getSystemChannel()
    {
        return systemChannel;
    }

    @Override
    public TextChannel getRulesChannel()
    {
        return rulesChannel;
    }

    @Override
    public TextChannel getCommunityUpdatesChannel()
    {
        return communityUpdatesChannel;
    }

    @Nonnull
    @Override
    public RestAction<List<Webhook>> retrieveWebhooks()
    {
        if (!getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS))
            throw new InsufficientPermissionException(this, Permission.MANAGE_WEBHOOKS);

        Route.CompiledRoute route = Route.Guilds.GET_WEBHOOKS.compile(getId());

        return new RestActionImpl<>(getJDA(), route, (response, request) ->
        {
            DataArray array = response.getArray();
            List<Webhook> webhooks = new ArrayList<>(array.length());
            EntityBuilder builder = api.getEntityBuilder();

            for (int i = 0; i < array.length(); i++)
            {
                try
                {
                    webhooks.add(builder.createWebhook(array.getObject(i)));
                }
                catch (Exception e)
                {
                    JDAImpl.LOG.error("Error creating webhook from json", e);
                }
            }

            return Collections.unmodifiableList(webhooks);
        });
    }

    @Override
    public Member getOwner()
    {
        return owner;
    }

    @Override
    public long getOwnerIdLong()
    {
        return ownerId;
    }

    @Nonnull
    @Override
    public Timeout getAfkTimeout()
    {
        return afkTimeout;
    }

    @Override
    public boolean isMember(@Nonnull User user)
    {
        return memberCache.get(user.getIdLong()) != null;
    }

    @Nonnull
    @Override
    public Member getSelfMember()
    {
        Member member = getMember(getJDA().getSelfUser());
        if (member == null)
            throw new IllegalStateException("Guild does not have a self member");
        return member;
    }

    @Override
    public Member getMember(@Nonnull User user)
    {
        Checks.notNull(user, "User");
        return getMemberById(user.getIdLong());
    }

    @Nonnull
    @Override
    public MemberCacheView getMemberCache()
    {
        return memberCache;
    }

    @Nonnull
    @Override
    public SortedSnowflakeCacheView<Category> getCategoryCache()
    {
        return categoryCache;
    }

    @Nonnull
    @Override
    public SortedSnowflakeCacheView<StoreChannel> getStoreChannelCache()
    {
        return storeChannelCache;
    }

    @Nonnull
    @Override
    public SortedSnowflakeCacheView<TextChannel> getTextChannelCache()
    {
        return textChannelCache;
    }

    @Nonnull
    @Override
    public SortedSnowflakeCacheView<NewsChannel> getNewsChannelCache()
    {
        return newsChannelCache;
    }

    @Nonnull
    @Override
    public SortedSnowflakeCacheView<VoiceChannel> getVoiceChannelCache()
    {
        return voiceChannelCache;
    }

    @Nonnull
    @Override
    public SortedSnowflakeCacheView<StageChannel> getStageChannelCache()
    {
        return stageChannelCache;
    }

    @Nonnull
    @Override
    public SortedSnowflakeCacheView<GuildScheduledEvent> getScheduledEventCache()
    {
        return scheduledEventCache;
    }

    @Nonnull
    @Override
    public SortedSnowflakeCacheView<ThreadChannel> getThreadChannelCache()
    {
        return threadChannelCache;
    }

    @Nonnull
    @Override
    public SortedSnowflakeCacheView<Role> getRoleCache()
    {
        return roleCache;
    }

    @Nonnull
    @Override
    public SnowflakeCacheView<Emote> getEmoteCache()
    {
        return emoteCache;
    }

    @Nonnull
    @Override
    public List<GuildChannel> getChannels(boolean includeHidden)
    {
        Member self = getSelfMember();
        Predicate<GuildChannel> filterHidden = it -> {
            //TODO-v5: Do we need to if-protected cast here? If the channel _isnt_ a IPermissionContainer, then would we even be using this filter on it?
            if (it instanceof IPermissionContainer) {
                self.hasPermission((IPermissionContainer) it, Permission.VIEW_CHANNEL);
            }
            return false;
        };

        List<GuildChannel> channels;
        SnowflakeCacheViewImpl<Category> categoryView = getCategoriesView();
        SnowflakeCacheViewImpl<VoiceChannel> voiceView = getVoiceChannelsView();
        SnowflakeCacheViewImpl<StageChannel> stageView = getStageChannelsView();
        SnowflakeCacheViewImpl<TextChannel> textView = getTextChannelsView();
        SnowflakeCacheViewImpl<NewsChannel> newsView = getNewsChannelView();
        SnowflakeCacheViewImpl<StoreChannel> storeView = getStoreChannelView();
        List<TextChannel> textChannels;
        List<NewsChannel> newsChannels;
        List<StoreChannel> storeChannels;
        List<VoiceChannel> voiceChannels;
        List<StageChannel> stageChannels;
        List<Category> categories;
        try (UnlockHook categoryHook = categoryView.readLock();
             UnlockHook voiceHook = voiceView.readLock();
             UnlockHook textHook = textView.readLock();
             UnlockHook newsHook = newsView.readLock();
             UnlockHook storeHook = storeView.readLock();
             UnlockHook stageHook = stageView.readLock())
        {
            if (includeHidden)
            {
                storeChannels = storeView.asList();
                textChannels = textView.asList();
                newsChannels = newsView.asList();
                voiceChannels = voiceView.asList();
                stageChannels = stageView.asList();
            }
            else
            {
                storeChannels = storeView.stream().filter(filterHidden).collect(Collectors.toList());
                textChannels = textView.stream().filter(filterHidden).collect(Collectors.toList());
                newsChannels = newsView.stream().filter(filterHidden).collect(Collectors.toList());
                voiceChannels = voiceView.stream().filter(filterHidden).collect(Collectors.toList());
                stageChannels = stageView.stream().filter(filterHidden).collect(Collectors.toList());
            }
            categories = categoryView.asList(); // we filter categories out when they are empty (no visible channels inside)
            channels = new ArrayList<>((int) categoryView.size() + voiceChannels.size() + textChannels.size() + newsChannels.size() + storeChannels.size() + stageChannels.size());
        }

        storeChannels.stream().filter(it -> it.getParentCategory() == null).forEach(channels::add);
        textChannels.stream().filter(it -> it.getParentCategory() == null).forEach(channels::add);
        newsChannels.stream().filter(it -> it.getParentCategory() == null).forEach(channels::add);
        voiceChannels.stream().filter(it -> it.getParentCategory() == null).forEach(channels::add);
        stageChannels.stream().filter(it -> it.getParentCategory() == null).forEach(channels::add);
        Collections.sort(channels);

        for (Category category : categories)
        {
            List<GuildChannel> children;
            if (includeHidden)
            {
                children = category.getChannels();
            }
            else
            {
                children = category.getChannels().stream().filter(filterHidden).collect(Collectors.toList());
                if (children.isEmpty())
                    continue;
            }

            channels.add(category);
            channels.addAll(children);
        }

        return Collections.unmodifiableList(channels);
    }

    @Nonnull
    @Override
    public RestAction<List<ListedEmote>> retrieveEmotes()
    {
        Route.CompiledRoute route = Route.Emotes.GET_EMOTES.compile(getId());
        return new RestActionImpl<>(getJDA(), route, (response, request) ->
        {

            EntityBuilder builder = GuildImpl.this.getJDA().getEntityBuilder();
            DataArray emotes = response.getArray();
            List<ListedEmote> list = new ArrayList<>(emotes.length());
            for (int i = 0; i < emotes.length(); i++)
            {
                DataObject emote = emotes.getObject(i);
                list.add(builder.createEmote(GuildImpl.this, emote));
            }

            return Collections.unmodifiableList(list);
        });
    }

    @Nonnull
    @Override
    public RestAction<ListedEmote> retrieveEmoteById(@Nonnull String id)
    {
        Checks.isSnowflake(id, "Emote ID");

        JDAImpl jda = getJDA();
        return new DeferredRestAction<>(jda, ListedEmote.class,
        () -> {
            Emote emote = getEmoteById(id);
            if (emote != null)
            {
                ListedEmote listedEmote = (ListedEmote) emote;
                if (listedEmote.hasUser() || !getSelfMember().hasPermission(Permission.MANAGE_EMOTES_AND_STICKERS))
                    return listedEmote;
            }
            return null;
        }, () -> {
            Route.CompiledRoute route = Route.Emotes.GET_EMOTE.compile(getId(), id);
            return new AuditableRestActionImpl<>(jda, route, (response, request) ->
            {
                EntityBuilder builder = GuildImpl.this.getJDA().getEntityBuilder();
                return builder.createEmote(GuildImpl.this, response.getObject());
            });
        });
    }

    @Nonnull
    @Override
    public RestActionImpl<List<Ban>> retrieveBanList()
    {
        if (!getSelfMember().hasPermission(Permission.BAN_MEMBERS))
            throw new InsufficientPermissionException(this, Permission.BAN_MEMBERS);

        Route.CompiledRoute route = Route.Guilds.GET_BANS.compile(getId());
        return new RestActionImpl<>(getJDA(), route, (response, request) ->
        {
            EntityBuilder builder = api.getEntityBuilder();
            List<Ban> bans = new LinkedList<>();
            DataArray bannedArr = response.getArray();

            for (int i = 0; i < bannedArr.length(); i++)
            {
                final DataObject object = bannedArr.getObject(i);
                DataObject user = object.getObject("user");
                bans.add(new Ban(builder.createUser(user), object.getString("reason", null)));
            }
            return Collections.unmodifiableList(bans);
        });
    }

    @Nonnull
    @Override
    public RestAction<Ban> retrieveBanById(@Nonnull String userId)
    {
        if (!getSelfMember().hasPermission(Permission.BAN_MEMBERS))
            throw new InsufficientPermissionException(this, Permission.BAN_MEMBERS);

        Checks.isSnowflake(userId, "User ID");

        Route.CompiledRoute route = Route.Guilds.GET_BAN.compile(getId(), userId);
        return new RestActionImpl<>(getJDA(), route, (response, request) ->
        {

            EntityBuilder builder = api.getEntityBuilder();
            DataObject bannedObj = response.getObject();
            DataObject user = bannedObj.getObject("user");
            return new Ban(builder.createUser(user), bannedObj.getString("reason", null));
        });
    }

    @Nonnull
    @Override
    public RestAction<Integer> retrievePrunableMemberCount(int days)
    {
        if (!getSelfMember().hasPermission(Permission.KICK_MEMBERS))
            throw new InsufficientPermissionException(this, Permission.KICK_MEMBERS);

        Checks.check(days >= 1 && days <= 30, "Provided %d days must be between 1 and 30.", days);

        Route.CompiledRoute route = Route.Guilds.PRUNABLE_COUNT.compile(getId()).withQueryParams("days", Integer.toString(days));
        return new RestActionImpl<>(getJDA(), route, (response, request) -> response.getObject().getInt("pruned"));
    }

    @Nonnull
    @Override
    public Role getPublicRole()
    {
        return publicRole;
    }

    @Nullable
    @Override
    public TextChannel getDefaultChannel()
    {
        final Role role = getPublicRole();
        return getTextChannelsView().stream()
                                    .filter(c -> role.hasPermission(c, Permission.VIEW_CHANNEL))
                                    .min(Comparator.naturalOrder()).orElse(null);
    }

    @Nonnull
    @Override
    public GuildManager getManager()
    {
        if (manager == null)
            return manager = new GuildManagerImpl(this);
        return manager;
    }

    @Override
    public boolean isBoostProgressBarEnabled()
    {
        return boostProgressBarEnabled;
    }

    @Nonnull
    @Override
    public AuditLogPaginationAction retrieveAuditLogs()
    {
        return new AuditLogPaginationActionImpl(this);
    }

    @Nonnull
    @Override
    public RestAction<Void> leave()
    {
        if (getSelfMember().isOwner())
            throw new IllegalStateException("Cannot leave a guild that you are the owner of! Transfer guild ownership first!");

        Route.CompiledRoute route = Route.Self.LEAVE_GUILD.compile(getId());
        return new RestActionImpl<>(getJDA(), route);
    }

    @Nonnull
    @Override
    public RestAction<Void> delete()
    {
        if (!getJDA().getSelfUser().isBot() && getJDA().getSelfUser().isMfaEnabled())
            throw new IllegalStateException("Cannot delete a guild without providing MFA code. Use Guild#delete(String)");

        return delete(null);
    }

    @Nonnull
    @Override
    public RestAction<Void> delete(String mfaCode)
    {
        if (!getSelfMember().isOwner())
            throw new PermissionException("Cannot delete a guild that you do not own!");

        DataObject mfaBody = null;
        if (!getJDA().getSelfUser().isBot() && getJDA().getSelfUser().isMfaEnabled())
        {
            Checks.notEmpty(mfaCode, "Provided MultiFactor Auth code");
            mfaBody = DataObject.empty().put("code", mfaCode);
        }

        Route.CompiledRoute route = Route.Guilds.DELETE_GUILD.compile(getId());
        return new RestActionImpl<>(getJDA(), route, mfaBody);
    }

    @Nonnull
    @Override
    public AudioManager getAudioManager()
    {
        if (!getJDA().isIntent(GatewayIntent.GUILD_VOICE_STATES))
            throw new IllegalStateException("Cannot use audio features with disabled GUILD_VOICE_STATES intent!");
        final AbstractCacheView<AudioManager> managerMap = getJDA().getAudioManagersView();
        AudioManager mng = managerMap.get(id);
        if (mng == null)
        {
            // No previous manager found -> create one
            try (UnlockHook hook = managerMap.writeLock())
            {
                GuildImpl cachedGuild = (GuildImpl) getJDA().getGuildById(id);
                if (cachedGuild == null)
                    throw new IllegalStateException("Cannot get an AudioManager instance on an uncached Guild");
                mng = managerMap.get(id);
                if (mng == null)
                {
                    mng = new AudioManagerImpl(cachedGuild);
                    managerMap.getMap().put(id, mng);
                }
            }
        }
        return mng;
    }

    @Nonnull
    @Override
    public synchronized Task<Void> requestToSpeak()
    {
        if (!isRequestToSpeakPending())
            pendingRequestToSpeak = new CompletableFuture<>();

        Task<Void> task = new GatewayTask<>(pendingRequestToSpeak, this::cancelRequestToSpeak);
        updateRequestToSpeak();
        return task;
    }

    @Nonnull
    @Override
    public synchronized Task<Void> cancelRequestToSpeak()
    {
        if (isRequestToSpeakPending())
        {
            pendingRequestToSpeak.cancel(false);
            pendingRequestToSpeak = null;
        }

        AudioChannel channel = getSelfMember().getVoiceState().getChannel();
        StageInstance instance = channel instanceof StageChannel ? ((StageChannel) channel).getStageInstance() : null;
        if (instance == null)
            return new GatewayTask<>(CompletableFuture.completedFuture(null), () -> {});
        CompletableFuture<Void> future = instance.cancelRequestToSpeak().submit();
        return new GatewayTask<>(future, () -> future.cancel(false));
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
        return Collections.unmodifiableList(
                getMembersView().stream()
                    .map(Member::getVoiceState)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
    }

    @Nonnull
    @Override
    public VerificationLevel getVerificationLevel()
    {
        return verificationLevel;
    }

    @Nonnull
    @Override
    public NotificationLevel getDefaultNotificationLevel()
    {
        return defaultNotificationLevel;
    }

    @Nonnull
    @Override
    public MFALevel getRequiredMFALevel()
    {
        return mfaLevel;
    }

    @Nonnull
    @Override
    public ExplicitContentLevel getExplicitContentLevel()
    {
        return explicitContentLevel;
    }

    @Nonnull
    @Override
    public Task<Void> loadMembers(@Nonnull Consumer<Member> callback)
    {
        Checks.notNull(callback, "Callback");
        if (!getJDA().isIntent(GatewayIntent.GUILD_MEMBERS))
            throw new IllegalStateException("Cannot use loadMembers without GatewayIntent.GUILD_MEMBERS!");
        if (isLoaded())
        {
            memberCache.forEachUnordered(callback);
            return new GatewayTask<>(CompletableFuture.completedFuture(null), () -> {});
        }

        MemberChunkManager chunkManager = getJDA().getClient().getChunkManager();
        boolean includePresences = getJDA().isIntent(GatewayIntent.GUILD_PRESENCES);
        CompletableFuture<Void> handler = chunkManager.chunkGuild(this, includePresences, (last, list) -> list.forEach(callback));
        handler.exceptionally(ex -> {
            WebSocketClient.LOG.error("Encountered exception trying to handle member chunk response", ex);
            return null;
        });
        return new GatewayTask<>(handler, () -> handler.cancel(false));
    }

    // Helper function for deferred cache access
    private Member getMember(long id, boolean update, JDAImpl jda)
    {
        if (!update || jda.isIntent(GatewayIntent.GUILD_MEMBERS))
        {
            // return member from cache if member tracking is enabled through intents
            Member member = getMemberById(id);
            // if the join time is inaccurate we also have to load it through REST to update this information
            if (!update || (member != null && member.hasTimeJoined()))
                return member;
        }
        return null;
    }

    @Nonnull
    @Override
    public RestAction<Member> retrieveMemberById(long id, boolean update)
    {
        JDAImpl jda = getJDA();
        if (id == jda.getSelfUser().getIdLong())
            return new CompletedRestAction<>(jda, getSelfMember());

        return new DeferredRestAction<>(jda, Member.class,
                () -> getMember(id, update, jda),
                () -> { // otherwise we need to update the member with a REST request first to get the nickname/roles
                    Route.CompiledRoute route = Route.Guilds.GET_MEMBER.compile(getId(), Long.toUnsignedString(id));
                    return new RestActionImpl<>(jda, route, (resp, req) -> {
                        MemberImpl member = jda.getEntityBuilder().createMember(this, resp.getObject());
                        jda.getEntityBuilder().updateMemberCache(member);
                        return member;
                    });
                });
    }

    @Nonnull
    @Override
    public Task<List<Member>> retrieveMembersByIds(boolean includePresence, @Nonnull long... ids)
    {
        Checks.notNull(ids, "ID Array");
        Checks.check(!includePresence || api.isIntent(GatewayIntent.GUILD_PRESENCES),
                "Cannot retrieve presences of members without GUILD_PRESENCES intent!");

        if (ids.length == 0)
            return new GatewayTask<>(CompletableFuture.completedFuture(Collections.emptyList()), () -> {});
        Checks.check(ids.length <= 100, "You can only request 100 members at once");
        MemberChunkManager chunkManager = api.getClient().getChunkManager();
        List<Member> collect = new ArrayList<>(ids.length);
        CompletableFuture<List<Member>> result = new CompletableFuture<>();
        CompletableFuture<Void> handle = chunkManager.chunkGuild(this, includePresence, ids, (last, list) -> {
            collect.addAll(list);
            if (last)
                result.complete(collect);
        });

        handle.exceptionally(ex -> {
            WebSocketClient.LOG.error("Encountered exception trying to handle member chunk response", ex);
            result.completeExceptionally(ex);
            return null;
        });

        return new GatewayTask<>(result, () -> handle.cancel(false));
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public Task<List<Member>> retrieveMembersByPrefix(@Nonnull String prefix, int limit)
    {
        Checks.notEmpty(prefix, "Prefix");
        Checks.positive(limit, "Limit");
        Checks.check(limit <= 100, "Limit must not be greater than 100");
        MemberChunkManager chunkManager = api.getClient().getChunkManager();

        List<Member> collect = new ArrayList<>(limit);
        CompletableFuture<List<Member>> result = new CompletableFuture<>();
        CompletableFuture<Void> handle = chunkManager.chunkGuild(this, prefix, limit, (last, list) -> {
            collect.addAll(list);
            if (last)
                result.complete(collect);
        });

        handle.exceptionally(ex -> {
            WebSocketClient.LOG.error("Encountered exception trying to handle member chunk response", ex);
            result.completeExceptionally(ex);
            return null;
        });

        return new GatewayTask<>(result, () -> handle.cancel(false));
    }

    @Nonnull
    @Override
    public RestAction<List<ThreadChannel>> retrieveActiveThreads()
    {
        Route.CompiledRoute route = Route.Guilds.LIST_ACTIVE_THREADS.compile(getId());
        return new RestActionImpl<>(api, route, (response, request) ->
        {
            DataObject obj = response.getObject();
            DataArray selfThreadMembers = obj.getArray("members");
            DataArray threads = obj.getArray("threads");

            List<ThreadChannel> list = new ArrayList<>(threads.length());
            EntityBuilder builder = api.getEntityBuilder();

            TLongObjectMap<DataObject> selfThreadMemberMap = new TLongObjectHashMap<>();
            for (int i = 0; i < selfThreadMembers.length(); i++)
            {
                DataObject selfThreadMember = selfThreadMembers.getObject(i);

                //Store the thread member based on the "id" which is the _thread's_ id, not the member's id (which would be our id)
                selfThreadMemberMap.put(selfThreadMember.getLong("id"), selfThreadMember);
            }

            for (int i = 0; i < threads.length(); i++)
            {
                DataObject threadObj = threads.getObject(i);
                DataObject selfThreadMemberObj = selfThreadMemberMap.get(threadObj.getLong("id", 0));

                if (selfThreadMemberObj != null)
                {
                    //Combine the thread and self thread-member into a single object to model what we get from
                    // thread payloads (like from Gateway, etc)
                    threadObj.put("member", selfThreadMemberObj);
                }

                ThreadChannel thread = builder.createThreadChannel(threadObj, this.getIdLong());
                list.add(thread);
            }

            return Collections.unmodifiableList(list);
        });
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public RestAction<GuildScheduledEvent> retrieveScheduledEventById(long id)
    {
        // TODO: Implement
        return null;
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
        if (!this.getSelfMember().hasPermission(Permission.MANAGE_SERVER))
            throw new InsufficientPermissionException(this, Permission.MANAGE_SERVER);

        final Route.CompiledRoute route = Route.Invites.GET_GUILD_INVITES.compile(getId());
        return new RestActionImpl<>(getJDA(), route, (response, request) ->
        {
            EntityBuilder entityBuilder = api.getEntityBuilder();
            DataArray array = response.getArray();
            List<Invite> invites = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++)
                invites.add(entityBuilder.createInvite(array.getObject(i)));
            return Collections.unmodifiableList(invites);
        });
    }

    @Nonnull
    @Override
    public RestAction<List<Template>> retrieveTemplates()
    {
        if (!this.getSelfMember().hasPermission(Permission.MANAGE_SERVER))
            throw new InsufficientPermissionException(this, Permission.MANAGE_SERVER);

        final Route.CompiledRoute route = Route.Templates.GET_GUILD_TEMPLATES.compile(getId());
        return new RestActionImpl<>(getJDA(), route, (response, request) ->
        {
            EntityBuilder entityBuilder = api.getEntityBuilder();
            DataArray array = response.getArray();
            List<Template> templates = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++)
            {
                try
                {
                    templates.add(entityBuilder.createTemplate(array.getObject(i)));
                }
                catch (Exception e)
                {
                    JDAImpl.LOG.error("Error creating template from json", e);
                }
            }
            return Collections.unmodifiableList(templates);
        });
    }

    @Nonnull
    @Override
    public RestAction<Template> createTemplate(@Nonnull String name, @Nullable String description)
    {
        checkPermission(Permission.MANAGE_SERVER);
        Checks.notBlank(name, "Name");
        name = name.trim();

        Checks.notLonger(name, 100, "Name");
        if (description != null)
            Checks.notLonger(description, 120, "Description");

        final Route.CompiledRoute route = Route.Templates.CREATE_TEMPLATE.compile(getId());

        DataObject object = DataObject.empty();
        object.put("name", name);
        object.put("description", description);

        return new RestActionImpl<>(getJDA(), route, object, (response, request) ->
        {
            EntityBuilder entityBuilder = api.getEntityBuilder();
            return entityBuilder.createTemplate(response.getObject());
        });
    }

    @Nonnull
    @Override
    public RestAction<Void> moveVoiceMember(@Nonnull Member member, @Nullable AudioChannel audioChannel)
    {
        Checks.notNull(member, "Member");
        checkGuild(member.getGuild(), "Member");
        if (audioChannel != null)
            checkGuild(audioChannel.getGuild(), "AudioChannel");

        GuildVoiceState vState = member.getVoiceState();
        if (vState == null)
            throw new IllegalStateException("Cannot move a Member with disabled CacheFlag.VOICE_STATE");
        AudioChannel channel = vState.getChannel();
        if (channel == null)
            throw new IllegalStateException("You cannot move a Member who isn't in an AudioChannel!");

        if (!PermissionUtil.checkPermission((IPermissionContainer) channel, getSelfMember(), Permission.VOICE_MOVE_OTHERS))
            throw new InsufficientPermissionException(channel, Permission.VOICE_MOVE_OTHERS, "This account does not have Permission to MOVE_OTHERS out of the channel that the Member is currently in.");

        if (audioChannel != null
            && !getSelfMember().hasPermission(audioChannel, Permission.VOICE_CONNECT)
            && !member.hasPermission(audioChannel, Permission.VOICE_CONNECT))
            throw new InsufficientPermissionException(audioChannel, Permission.VOICE_CONNECT,
                                                      "Neither this account nor the Member that is attempting to be moved have the VOICE_CONNECT permission " +
                                                      "for the destination AudioChannel, so the move cannot be done.");

        DataObject body = DataObject.empty().put("channel_id", audioChannel == null ? null : audioChannel.getId());
        Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(getId(), member.getUser().getId());
        return new RestActionImpl<>(getJDA(), route, body);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> modifyNickname(@Nonnull Member member, String nickname)
    {
        Checks.notNull(member, "Member");
        checkGuild(member.getGuild(), "Member");

        if (member.equals(getSelfMember()))
        {
            if (!member.hasPermission(Permission.NICKNAME_CHANGE) && !member.hasPermission(Permission.NICKNAME_MANAGE))
                throw new InsufficientPermissionException(this, Permission.NICKNAME_CHANGE, "You neither have NICKNAME_CHANGE nor NICKNAME_MANAGE permission!");
        }
        else
        {
            checkPermission(Permission.NICKNAME_MANAGE);
            checkPosition(member);
        }

        JDAImpl jda = getJDA();
        return new DeferredRestAction<>(jda, () -> {
            DataObject body = DataObject.empty().put("nick", nickname == null ? "" : nickname);

            Route.CompiledRoute route;
            if (member.equals(getSelfMember()))
                route = Route.Guilds.MODIFY_SELF.compile(getId());
            else
                route = Route.Guilds.MODIFY_MEMBER.compile(getId(), member.getUser().getId());

            return new AuditableRestActionImpl<Void>(jda, route, body);
        }).setCacheCheck(() -> !Objects.equals(nickname, member.getNickname()));
    }

    @Nonnull
    @Override
    public AuditableRestAction<Integer> prune(int days, boolean wait, @Nonnull Role... roles)
    {
        checkPermission(Permission.KICK_MEMBERS);

        Checks.check(days >= 1 && days <= 30, "Provided %d days must be between 1 and 30.", days);
        Checks.notNull(roles, "Roles");

        Route.CompiledRoute route = Route.Guilds.PRUNE_MEMBERS.compile(getId());
        DataObject body = DataObject.empty();
        body.put("days", days);
        if (!wait)
            body.put("compute_prune_count", false);
        if (roles.length != 0)
        {
            for (Role role : roles)
            {
                Checks.notNull(role, "Role");
                Checks.check(role.getGuild().equals(this), "Role is not from the same guild!");
            }
            body.put("include_roles", Arrays.stream(roles).map(Role::getId).collect(Collectors.toList()));
        }
        return new AuditableRestActionImpl<>(getJDA(), route, body, (response, request) -> response.getObject().getInt("pruned", 0));
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> kick(@Nonnull Member member, String reason)
    {
        Checks.notNull(member, "member");
        checkGuild(member.getGuild(), "member");
        checkPermission(Permission.KICK_MEMBERS);
        checkPosition(member);
        return kick0(member.getUser().getId(), reason);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> kick(@Nonnull String userId, @Nullable String reason)
    {
        Member member = getMemberById(userId);
        if (member != null)
            return kick(member, reason);
        // Check permissions and whether the user is the owner, otherwise attempt a kick
        Checks.check(!userId.equals(getOwnerId()), "Cannot kick the owner of a guild!");
        checkPermission(Permission.KICK_MEMBERS);
        return kick0(userId, reason);
    }

    @Nonnull
    private AuditableRestAction<Void> kick0(@Nonnull String userId, @Nullable String reason)
    {
        Route.CompiledRoute route = Route.Guilds.KICK_MEMBER.compile(getId(), userId);
        if (!Helpers.isBlank(reason))
        {
            Checks.check(reason.length() <= 512, "Reason cannot be longer than 512 characters.");
            route = route.withQueryParams("reason", EncodingUtil.encodeUTF8(reason));
        }
        return new AuditableRestActionImpl<>(getJDA(), route);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> ban(@Nonnull User user, int delDays, String reason)
    {
        Checks.notNull(user, "User");
        checkPermission(Permission.BAN_MEMBERS);

        if (isMember(user)) // If user is in guild. Check if we are able to ban.
            checkPosition(getMember(user));

        return ban0(user.getId(), delDays, reason);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> ban(@Nonnull String userId, int delDays, String reason)
    {
        Checks.notNull(userId, "User");
        checkPermission(Permission.BAN_MEMBERS);

        User user = getJDA().getUserById(userId);
        if (user != null) // If we have the user cached then we should use the additional information available to use during the ban process.
            return ban(user, delDays, reason);

        return ban0(userId, delDays, reason);
    }

    @Nonnull
    private AuditableRestAction<Void> ban0(@Nonnull String userId, int delDays, String reason)
    {
        Checks.notNegative(delDays, "Deletion Days");
        Checks.check(delDays <= 7, "Deletion Days must not be bigger than 7.");

        Route.CompiledRoute route = Route.Guilds.BAN.compile(getId(), userId);
        DataObject params = DataObject.empty();
        if (!Helpers.isBlank(reason))
        {
            Checks.check(reason.length() <= 512, "Reason cannot be longer than 512 characters.");
            params.put("reason", reason);
        }
        if (delDays > 0)
            params.put("delete_message_days", delDays);

        return new AuditableRestActionImpl<>(getJDA(), route, params);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> unban(@Nonnull String userId)
    {
        Checks.isSnowflake(userId, "User ID");
        checkPermission(Permission.BAN_MEMBERS);

        Route.CompiledRoute route = Route.Guilds.UNBAN.compile(getId(), userId);
        return new AuditableRestActionImpl<>(getJDA(), route);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> deafen(@Nonnull Member member, boolean deafen)
    {
        Checks.notNull(member, "Member");
        checkGuild(member.getGuild(), "Member");
        checkPermission(Permission.VOICE_DEAF_OTHERS);

        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState != null)
        {
            if (voiceState.getChannel() == null)
                throw new IllegalStateException("Can only deafen members who are currently in a voice channel");
            if (voiceState.isGuildDeafened() == deafen)
                return new CompletedRestAction<>(getJDA(), null);
        }

        DataObject body = DataObject.empty().put("deaf", deafen);
        Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(getId(), member.getUser().getId());
        return new AuditableRestActionImpl<>(getJDA(), route, body);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> mute(@Nonnull Member member, boolean mute)
    {
        Checks.notNull(member, "Member");
        checkGuild(member.getGuild(), "Member");
        checkPermission(Permission.VOICE_MUTE_OTHERS);

        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState != null)
        {
            if (voiceState.getChannel() == null)
                throw new IllegalStateException("Can only mute members who are currently in a voice channel");
            if (voiceState.isGuildMuted() == mute && (mute || !voiceState.isSuppressed()))
                return new CompletedRestAction<>(getJDA(), null);
        }

        DataObject body = DataObject.empty().put("mute", mute);
        Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(getId(), member.getUser().getId());
        return new AuditableRestActionImpl<>(getJDA(), route, body);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> addRoleToMember(@Nonnull Member member, @Nonnull Role role)
    {
        Checks.notNull(member, "Member");
        Checks.notNull(role, "Role");
        checkGuild(member.getGuild(), "Member");
        checkGuild(role.getGuild(), "Role");
        checkPermission(Permission.MANAGE_ROLES);
        checkPosition(role);

        Route.CompiledRoute route = Route.Guilds.ADD_MEMBER_ROLE.compile(getId(), member.getUser().getId(), role.getId());
        return new AuditableRestActionImpl<>(getJDA(), route);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> removeRoleFromMember(@Nonnull Member member, @Nonnull Role role)
    {
        Checks.notNull(member, "Member");
        Checks.notNull(role, "Role");
        checkGuild(member.getGuild(), "Member");
        checkGuild(role.getGuild(), "Role");
        checkPermission(Permission.MANAGE_ROLES);
        checkPosition(role);

        Route.CompiledRoute route = Route.Guilds.REMOVE_MEMBER_ROLE.compile(getId(), member.getUser().getId(), role.getId());
        return new AuditableRestActionImpl<>(getJDA(), route);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> modifyMemberRoles(@Nonnull Member member, Collection<Role> rolesToAdd, Collection<Role> rolesToRemove)
    {
        Checks.notNull(member, "Member");
        checkGuild(member.getGuild(), "Member");
        checkPermission(Permission.MANAGE_ROLES);
        Set<Role> currentRoles = new HashSet<>(((MemberImpl) member).getRoleSet());
        if (rolesToAdd != null)
        {
            checkRoles(rolesToAdd, "add", "to");
            currentRoles.addAll(rolesToAdd);
        }

        if (rolesToRemove != null)
        {
            checkRoles(rolesToRemove, "remove", "from");
            currentRoles.removeAll(rolesToRemove);
        }

        return modifyMemberRoles(member, currentRoles);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> modifyMemberRoles(@Nonnull Member member, @Nonnull Collection<Role> roles)
    {
        Checks.notNull(member, "Member");
        Checks.notNull(roles, "Roles");
        checkGuild(member.getGuild(), "Member");
        roles.forEach(role ->
        {
            Checks.notNull(role, "Role in collection");
            checkGuild(role.getGuild(), "Role: " + role.toString());
        });

        Checks.check(!roles.contains(getPublicRole()),
             "Cannot add the PublicRole of a Guild to a Member. All members have this role by default!");

        // Return an empty rest action if there were no changes
        final List<Role> memberRoles = member.getRoles();
        if (Helpers.deepEqualsUnordered(roles, memberRoles))
            return new CompletedRestAction<>(getJDA(), null);

        // Check removed roles
        for (Role r : memberRoles)
        {
            if (!roles.contains(r))
            {
                checkPosition(r);
                Checks.check(!r.isManaged(), "Cannot remove managed role from member. Role: %s", r);
            }
        }

        // Check added roles
        for (Role r : roles)
        {
            if (!memberRoles.contains(r))
            {
                checkPosition(r);
                Checks.check(!r.isManaged(), "Cannot add managed role to member. Role: %s", r);
            }
        }

        DataObject body = DataObject.empty()
            .put("roles", roles.stream().map(Role::getId).collect(Collectors.toSet()));
        Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(getId(), member.getUser().getId());

        return new AuditableRestActionImpl<>(getJDA(), route, body);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> transferOwnership(@Nonnull Member newOwner)
    {
        Checks.notNull(newOwner, "Member");
        checkGuild(newOwner.getGuild(), "Member");
        if (!getSelfMember().isOwner())
            throw new PermissionException("The logged in account must be the owner of this Guild to be able to transfer ownership");

        Checks.check(!getSelfMember().equals(newOwner),
                     "The member provided as the newOwner is the currently logged in account. Provide a different member to give ownership to.");

        Checks.check(!newOwner.getUser().isBot(), "Cannot transfer ownership of a Guild to a Bot!");

        DataObject body = DataObject.empty().put("owner_id", newOwner.getUser().getId());
        Route.CompiledRoute route = Route.Guilds.MODIFY_GUILD.compile(getId());
        return new AuditableRestActionImpl<>(getJDA(), route, body);
    }

    @Nonnull
    @Override
    public ChannelAction<TextChannel> createTextChannel(@Nonnull String name, Category parent)
    {
        checkCanCreateChannel(parent);

        Checks.notBlank(name, "Name");
        name = name.trim();
        Checks.notLonger(name, 100, "Name");
        return new ChannelActionImpl<>(TextChannel.class, name, this, ChannelType.TEXT).setParent(parent);
    }

    @Nonnull
    @Override
    public ChannelAction<NewsChannel> createNewsChannel(@Nonnull String name, Category parent)
    {
        checkCanCreateChannel(parent);

        Checks.notBlank(name, "Name");
        name = name.trim();
        Checks.notLonger(name, 100, "Name");
        return new ChannelActionImpl<>(NewsChannel.class, name, this, ChannelType.NEWS).setParent(parent);
    }

    @Nonnull
    @Override
    public ChannelAction<VoiceChannel> createVoiceChannel(@Nonnull String name, Category parent)
    {
        checkCanCreateChannel(parent);

        Checks.notBlank(name, "Name");
        name = name.trim();
        Checks.notLonger(name, 100, "Name");
        return new ChannelActionImpl<>(VoiceChannel.class, name, this, ChannelType.VOICE).setParent(parent);
    }

    @Nonnull
    @Override
    public ChannelAction<StageChannel> createStageChannel(@Nonnull String name, Category parent)
    {
        checkCanCreateChannel(parent);

        Checks.notBlank(name, "Name");
        name = name.trim();
        Checks.notLonger(name, 100, "Name");
        return new ChannelActionImpl<>(StageChannel.class, name, this, ChannelType.STAGE).setParent(parent);
    }

    @Nonnull
    @Override
    public ChannelAction<Category> createCategory(@Nonnull String name)
    {
        checkPermission(Permission.MANAGE_CHANNEL);
        Checks.notBlank(name, "Name");
        name = name.trim();
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 100, "Name");
        return new ChannelActionImpl<>(Category.class, name, this, ChannelType.CATEGORY);
    }

    @Nonnull
    @Override
    public RoleAction createRole()
    {
        checkPermission(Permission.MANAGE_ROLES);
        return new RoleActionImpl(this);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Emote> createEmote(@Nonnull String name, @Nonnull Icon icon, @Nonnull Role... roles)
    {
        checkPermission(Permission.MANAGE_EMOTES_AND_STICKERS);
        Checks.inRange(name, 2, 32, "Emote name");
        Checks.notNull(icon, "Emote icon");
        Checks.notNull(roles, "Roles");

        DataObject body = DataObject.empty();
        body.put("name", name);
        body.put("image", icon.getEncoding());
        if (roles.length > 0) // making sure none of the provided roles are null before mapping them to the snowflake id
            body.put("roles", Stream.of(roles).filter(Objects::nonNull).map(ISnowflake::getId).collect(Collectors.toSet()));

        JDAImpl jda = getJDA();
        Route.CompiledRoute route = Route.Emotes.CREATE_EMOTE.compile(getId());
        return new AuditableRestActionImpl<>(jda, route, body, (response, request) ->
        {
            DataObject obj = response.getObject();
            return jda.getEntityBuilder().createEmote(this, obj);
        });
    }

    @Nonnull
    @Override
    @CheckReturnValue
    public GuildScheduledEventAction createScheduledEvent()
    {
        // TODO: Implement, possibly split into separate methods for each event type
        return null;
    }

    @Nonnull
    @Override
    public ChannelOrderAction modifyCategoryPositions()
    {
        return new ChannelOrderActionImpl(this, ChannelType.CATEGORY.getSortBucket());
    }

    @Nonnull
    @Override
    public ChannelOrderAction modifyTextChannelPositions()
    {
        return new ChannelOrderActionImpl(this, ChannelType.TEXT.getSortBucket());
    }

    @Nonnull
    @Override
    public ChannelOrderAction modifyVoiceChannelPositions()
    {
        return new ChannelOrderActionImpl(this, ChannelType.VOICE.getSortBucket());
    }

    @Nonnull
    @Override
    public CategoryOrderAction modifyTextChannelPositions(@Nonnull Category category)
    {
        Checks.notNull(category, "Category");
        checkGuild(category.getGuild(), "Category");
        return new CategoryOrderActionImpl(category, ChannelType.TEXT.getSortBucket());
    }

    @Nonnull
    @Override
    public CategoryOrderAction modifyVoiceChannelPositions(@Nonnull Category category)
    {
        Checks.notNull(category, "Category");
        checkGuild(category.getGuild(), "Category");
        return new CategoryOrderActionImpl(category, ChannelType.VOICE.getSortBucket());
    }

    @Nonnull
    @Override
    public RoleOrderAction modifyRolePositions(boolean useAscendingOrder)
    {
        return new RoleOrderActionImpl(this, useAscendingOrder);
    }

    protected void checkGuild(Guild providedGuild, String comment)
    {
        if (!equals(providedGuild))
            throw new IllegalArgumentException("Provided " + comment + " is not part of this Guild!");
    }

    protected void checkPermission(Permission perm)
    {
        if (!getSelfMember().hasPermission(perm))
            throw new InsufficientPermissionException(this, perm);
    }

    protected void checkPosition(Member member)
    {
        if(!getSelfMember().canInteract(member))
            throw new HierarchyException("Can't modify a member with higher or equal highest role than yourself!");
    }

    protected void checkPosition(Role role)
    {
        if(!getSelfMember().canInteract(role))
            throw new HierarchyException("Can't modify a role with higher or equal highest role than yourself! Role: " + role.toString());
    }

    private void checkRoles(Collection<Role> roles, String type, String preposition)
    {
        roles.forEach(role ->
        {
            Checks.notNull(role, "Role in roles to " + type);
            checkGuild(role.getGuild(), "Role: " + role.toString());
            checkPosition(role);
            Checks.check(!role.isManaged(), "Cannot %s a managed role %s a Member. Role: %s", type, preposition, role.toString());
        });
    }

    private synchronized boolean isRequestToSpeakPending()
    {
        return pendingRequestToSpeak != null && !pendingRequestToSpeak.isDone();
    }

    public synchronized void updateRequestToSpeak()
    {
        if (!isRequestToSpeakPending())
            return;
        AudioChannel connectedChannel = getSelfMember().getVoiceState().getChannel();
        if (!(connectedChannel instanceof StageChannel))
            return;
        StageChannel stage = (StageChannel) connectedChannel;
        StageInstance instance = stage.getStageInstance();
        if (instance == null)
            return;

        CompletableFuture<Void> future = pendingRequestToSpeak;
        pendingRequestToSpeak = null;

        instance.requestToSpeak().queue((v) -> future.complete(null), future::completeExceptionally);
    }

    // ---- Setters -----

    public GuildImpl setOwner(Member owner)
    {
        // Only cache owner if user cache is enabled
        if (owner != null && getMemberById(owner.getIdLong()) != null)
            this.owner = owner;
        return this;
    }

    public GuildImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public GuildImpl setIconId(String iconId)
    {
        this.iconId = iconId;
        return this;
    }

    public GuildImpl setFeatures(Set<String> features)
    {
        this.features = Collections.unmodifiableSet(features);
        return this;
    }

    public GuildImpl setSplashId(String splashId)
    {
        this.splashId = splashId;
        return this;
    }

    public GuildImpl setVanityCode(String code)
    {
        this.vanityCode = code;
        return this;
    }

    public GuildImpl setDescription(String description)
    {
        this.description = description;
        return this;
    }

    public GuildImpl setBannerId(String bannerId)
    {
        this.banner = bannerId;
        return this;
    }

    public GuildImpl setMaxPresences(int maxPresences)
    {
        this.maxPresences = maxPresences;
        return this;
    }

    public GuildImpl setMaxMembers(int maxMembers)
    {
        this.maxMembers = maxMembers;
        return this;
    }

    public GuildImpl setAfkChannel(VoiceChannel afkChannel)
    {
        this.afkChannel = afkChannel;
        return this;
    }

    public GuildImpl setSystemChannel(TextChannel systemChannel)
    {
        this.systemChannel = systemChannel;
        return this;
    }

    public GuildImpl setRulesChannel(TextChannel rulesChannel)
    {
        this.rulesChannel = rulesChannel;
        return this;
    }

    public GuildImpl setCommunityUpdatesChannel(TextChannel communityUpdatesChannel)
    {
        this.communityUpdatesChannel = communityUpdatesChannel;
        return this;
    }

    public GuildImpl setPublicRole(Role publicRole)
    {
        this.publicRole = publicRole;
        return this;
    }

    public GuildImpl setVerificationLevel(VerificationLevel level)
    {
        this.verificationLevel = level;
        return this;
    }

    public GuildImpl setDefaultNotificationLevel(NotificationLevel level)
    {
        this.defaultNotificationLevel = level;
        return this;
    }

    public GuildImpl setRequiredMFALevel(MFALevel level)
    {
        this.mfaLevel = level;
        return this;
    }

    public GuildImpl setExplicitContentLevel(ExplicitContentLevel level)
    {
        this.explicitContentLevel = level;
        return this;
    }

    public GuildImpl setAfkTimeout(Timeout afkTimeout)
    {
        this.afkTimeout = afkTimeout;
        return this;
    }

    public GuildImpl setLocale(String locale)
    {
        this.preferredLocale = Locale.forLanguageTag(locale);
        return this;
    }

    public GuildImpl setBoostTier(int tier)
    {
        this.boostTier = BoostTier.fromKey(tier);
        return this;
    }

    public GuildImpl setBoostCount(int count)
    {
        this.boostCount = count;
        return this;
    }

    public GuildImpl setOwnerId(long ownerId)
    {
        this.ownerId = ownerId;
        return this;
    }

    public GuildImpl setMemberCount(int count)
    {
        this.memberCount = count;
        return this;
    }

    public GuildImpl setNSFWLevel(NSFWLevel nsfwLevel)
    {
        this.nsfwLevel = nsfwLevel;
        return this;
    }

    public GuildImpl setBoostProgressBarEnabled(boolean enabled)
    {
        this.boostProgressBarEnabled = enabled;
        return this;
    }

    // -- Map getters --

    public SortedSnowflakeCacheViewImpl<Category> getCategoriesView()
    {
        return categoryCache;
    }

    public SortedSnowflakeCacheViewImpl<StoreChannel> getStoreChannelView()
    {
        return storeChannelCache;
    }

    public SortedSnowflakeCacheViewImpl<TextChannel> getTextChannelsView()
    {
        return textChannelCache;
    }

    public SortedSnowflakeCacheViewImpl<NewsChannel> getNewsChannelView()
    {
        return newsChannelCache;
    }

    public SortedSnowflakeCacheViewImpl<VoiceChannel> getVoiceChannelsView()
    {
        return voiceChannelCache;
    }

    public SortedSnowflakeCacheViewImpl<StageChannel> getStageChannelsView()
    {
        return stageChannelCache;
    }

    public SortedSnowflakeCacheViewImpl<ThreadChannel> getThreadChannelsView()
    {
        return threadChannelCache;
    }

    public SortedSnowflakeCacheViewImpl<GuildScheduledEvent> getScheduledEventsView()
    {
        return scheduledEventCache;
    }

    public SortedSnowflakeCacheViewImpl<Role> getRolesView()
    {
        return roleCache;
    }

    public SnowflakeCacheViewImpl<Emote> getEmotesView()
    {
        return emoteCache;
    }

    public MemberCacheViewImpl getMembersView()
    {
        return memberCache;
    }

    @Nonnull
    @Override
    public NSFWLevel getNSFWLevel()
    {
        return nsfwLevel;
    }

    @Nullable
    public CacheView.SimpleCacheView<MemberPresenceImpl> getPresenceView()
    {
        return memberPresences;
    }

    // -- Member Tracking --

    public void onMemberAdd()
    {
        memberCount++;
    }

    public void onMemberRemove()
    {
        memberCount--;
    }

    // -- Object overrides --

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
            return true;
        if (!(o instanceof GuildImpl))
            return false;
        GuildImpl oGuild = (GuildImpl) o;
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
        return "G:" + getName() + '(' + id + ')';
    }

    private void checkCanCreateChannel(Category parent)
    {
        if (parent != null)
        {
            Checks.check(parent.getGuild().equals(this), "Category is not from the same guild!");
            if (!getSelfMember().hasPermission(parent, Permission.MANAGE_CHANNEL))
                throw new InsufficientPermissionException(parent, Permission.MANAGE_CHANNEL);
        }
        else
        {
            checkPermission(Permission.MANAGE_CHANNEL);
        }
    }
}
