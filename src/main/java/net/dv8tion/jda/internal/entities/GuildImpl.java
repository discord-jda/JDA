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

package net.dv8tion.jda.internal.entities;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.managers.GuildController;
import net.dv8tion.jda.api.managers.GuildManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MemberAction;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.cache.MemberCacheView;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView;
import net.dv8tion.jda.api.utils.json.DataArray;
import net.dv8tion.jda.api.utils.json.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.managers.AudioManagerImpl;
import net.dv8tion.jda.internal.managers.GuildManagerImpl;
import net.dv8tion.jda.internal.requests.EmptyRestAction;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.MemberActionImpl;
import net.dv8tion.jda.internal.requests.restaction.pagination.AuditLogPaginationActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.UnlockHook;
import net.dv8tion.jda.internal.utils.cache.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GuildImpl implements Guild
{
    private final long id;
    private final UpstreamReference<JDAImpl> api;

    private final SortedSnowflakeCacheViewImpl<Category> categoryCache = new SortedSnowflakeCacheViewImpl<>(Category.class, GuildChannel::getName, Comparator.naturalOrder());
    private final SortedSnowflakeCacheViewImpl<VoiceChannel> voiceChannelCache = new SortedSnowflakeCacheViewImpl<>(VoiceChannel.class, GuildChannel::getName, Comparator.naturalOrder());
    private final SortedSnowflakeCacheViewImpl<TextChannel> textChannelCache = new SortedSnowflakeCacheViewImpl<>(TextChannel.class, GuildChannel::getName, Comparator.naturalOrder());
    private final SortedSnowflakeCacheViewImpl<Role> roleCache = new SortedSnowflakeCacheViewImpl<>(Role.class, Role::getName, Comparator.reverseOrder());
    private final SnowflakeCacheViewImpl<Emote> emoteCache = new SnowflakeCacheViewImpl<>(Emote.class, Emote::getName);
    private final MemberCacheViewImpl memberCache = new MemberCacheViewImpl();

    private final TLongObjectMap<DataObject> cachedPresences = MiscUtil.newLongMap();

    private final ReentrantLock mngLock = new ReentrantLock();
    private volatile GuildManager manager;
    private volatile GuildController controller;

    private Member owner;
    private String name;
    private String iconId;
    private String splashId;
    private String region;
    private long ownerId;
    private Set<String> features;
    private VoiceChannel afkChannel;
    private TextChannel systemChannel;
    private Role publicRole;
    private VerificationLevel verificationLevel;
    private NotificationLevel defaultNotificationLevel;
    private MFALevel mfaLevel;
    private ExplicitContentLevel explicitContentLevel;
    private Timeout afkTimeout;
    private boolean available;
    private boolean canSendVerification = false;

    public GuildImpl(JDAImpl api, long id)
    {
        this.id = id;
        this.api = new UpstreamReference<>(api);
    }

    @Override
    public RestAction<EnumSet<Region>> retrieveRegions(boolean includeDeprecated)
    {
        Route.CompiledRoute route = Route.Guilds.GET_VOICE_REGIONS.compile(getId());
        return new RestActionImpl<>(getJDA(), route, (response, request) ->
        {
            EnumSet<Region> set = EnumSet.noneOf(Region.class);
            DataArray arr = response.getArray();
            for (int i = 0; arr != null && i < arr.length(); i++)
            {
                DataObject obj = arr.getObject(i);
                if (!includeDeprecated && obj.getBoolean("deprecated"))
                {
                    continue;
                }
                String id = obj.getString("id", "");
                Region region = Region.fromKey(id);
                if (region != Region.UNKNOWN)
                {
                    set.add(region);
                }
            }
            return set;
        });
    }

    @Override
    public MemberAction addMember(String accessToken, String userId)
    {
        Checks.notBlank(accessToken, "Access-Token");
        Checks.isSnowflake(userId, "User ID");
        Checks.check(getMemberById(userId) == null, "User is already in this guild");
        if (!getSelfMember().hasPermission(Permission.CREATE_INSTANT_INVITE))
            throw new InsufficientPermissionException(Permission.CREATE_INSTANT_INVITE);
        return new MemberActionImpl(getJDA(), this, userId, accessToken);
    }

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

    @Override
    public String getIconUrl()
    {
        return iconId == null ? null : "https://cdn.discordapp.com/icons/" + id + "/" + iconId + ".png";
    }

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

    @Override
    public String getSplashUrl()
    {
        return splashId == null ? null : "https://cdn.discordapp.com/splashes/" + id + "/" + splashId + ".png";
    }

    @Override
    public RestAction<String> retrieveVanityUrl()
    {
        if (!getSelfMember().hasPermission(Permission.MANAGE_SERVER))
            throw new InsufficientPermissionException(Permission.MANAGE_SERVER);
        if (!getFeatures().contains("VANITY_URL"))
            throw new IllegalStateException("This guild doesn't have a vanity url");

        Route.CompiledRoute route = Route.Guilds.GET_VANITY_URL.compile(getId());

        return new RestActionImpl<>(getJDA(), route,
            (response, request) -> response.getObject().getString("code"));
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
    public RestAction<List<Webhook>> retrieveWebhooks()
    {
        if (!getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS))
            throw new InsufficientPermissionException(Permission.MANAGE_WEBHOOKS);

        Route.CompiledRoute route = Route.Guilds.GET_WEBHOOKS.compile(getId());

        return new RestActionImpl<>(getJDA(), route, (response, request) ->
        {
            DataArray array = response.getArray();
            List<Webhook> webhooks = new ArrayList<>(array.length());
            EntityBuilder builder = api.get().getEntityBuilder();

            for (int i = 0; i < array.length(); i++)
            {
                try
                {
                    webhooks.add(builder.createWebhook(array.getObject(i)));
                }
                catch (UncheckedIOException | NullPointerException e)
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

    @Override
    public Timeout getAfkTimeout()
    {
        return afkTimeout;
    }

    @Override
    public String getRegionRaw()
    {
        return region;
    }

    @Override
    public boolean isMember(User user)
    {
        return memberCache.get(user.getIdLong()) != null;
    }

    @Override
    public Member getSelfMember()
    {
        return getMember(getJDA().getSelfUser());
    }

    @Override
    public Member getMember(User user)
    {
        Checks.notNull(user, "User");
        return getMemberById(user.getIdLong());
    }

    @Override
    public MemberCacheView getMemberCache()
    {
        return memberCache;
    }

    @Override
    public SortedSnowflakeCacheView<Category> getCategoryCache()
    {
        return categoryCache;
    }

    @Override
    public SortedSnowflakeCacheView<TextChannel> getTextChannelCache()
    {
        return textChannelCache;
    }

    @Override
    public SortedSnowflakeCacheView<VoiceChannel> getVoiceChannelCache()
    {
        return voiceChannelCache;
    }

    @Override
    public SortedSnowflakeCacheView<Role> getRoleCache()
    {
        return roleCache;
    }

    @Override
    public SnowflakeCacheView<Emote> getEmoteCache()
    {
        return emoteCache;
    }

    @Override
    public List<GuildChannel> getChannels(boolean includeHidden)
    {
        Member self = getSelfMember();
        Predicate<GuildChannel> filterHidden = it -> self.hasPermission(it, Permission.VIEW_CHANNEL);

        List<GuildChannel> channels;
        SnowflakeCacheViewImpl<Category> categoryView = getCategoriesView();
        SnowflakeCacheViewImpl<VoiceChannel> voiceView = getVoiceChannelsView();
        SnowflakeCacheViewImpl<TextChannel> textView = getTextChannelsView();
        List<TextChannel> textChannels;
        List<VoiceChannel> voiceChannels;
        List<Category> categories;
        try (UnlockHook categoryHook = categoryView.readLock();
             UnlockHook voiceHook = voiceView.readLock();
             UnlockHook textHook = textView.readLock())
        {
            if (includeHidden)
            {
                textChannels = textView.asList();
                voiceChannels = voiceView.asList();
            }
            else
            {
                textChannels = textView.stream().filter(filterHidden).collect(Collectors.toList());
                voiceChannels = voiceView.stream().filter(filterHidden).collect(Collectors.toList());
            }
            categories = categoryView.asList(); // we filter categories out when they are empty (no visible channels inside)
            channels = new ArrayList<>((int) categoryView.size() + voiceChannels.size() + textChannels.size());
        }

        textChannels.stream().filter(it -> it.getParent() == null).forEach(channels::add);
        voiceChannels.stream().filter(it -> it.getParent() == null).forEach(channels::add);

        for (Category category : categories)
        {
            List<TextChannel> childTextChannels;
            List<VoiceChannel> childVoiceChannels;
            if (includeHidden)
            {
                childTextChannels = category.getTextChannels();
                childVoiceChannels = category.getVoiceChannels();
            }
            else
            {
                childTextChannels = category.getTextChannels().stream().filter(filterHidden).collect(Collectors.toList());
                childVoiceChannels = category.getVoiceChannels().stream().filter(filterHidden).collect(Collectors.toList());
                if (childTextChannels.isEmpty() && childVoiceChannels.isEmpty())
                    continue;
            }

            channels.add(category);
            channels.addAll(childTextChannels);
            channels.addAll(childVoiceChannels);
        }

        return Collections.unmodifiableList(channels);
    }

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
                list.add(builder.createEmote(GuildImpl.this, emote, true));
            }

            return Collections.unmodifiableList(list);
        });
    }

    @Override
    public RestAction<ListedEmote> retrieveEmoteById(String id)
    {
        Checks.isSnowflake(id, "Emote ID");
        Emote emote = getEmoteById(id);
        if (emote != null)
        {
            ListedEmote listedEmote = (ListedEmote) emote;
            if (listedEmote.hasUser() || !getSelfMember().hasPermission(Permission.MANAGE_EMOTES))
                return new EmptyRestAction<>(getJDA(), listedEmote);
        }
        Route.CompiledRoute route = Route.Emotes.GET_EMOTE.compile(getId(), id);
        return new RestActionImpl<>(getJDA(), route, (response, request) ->
        {
            EntityBuilder builder = GuildImpl.this.getJDA().getEntityBuilder();
            return builder.createEmote(GuildImpl.this, response.getObject(), true);
        });
    }

    @Nonnull
    @Override
    public RestActionImpl<List<Ban>> retrieveBanList()
    {
        if (!getSelfMember().hasPermission(Permission.BAN_MEMBERS))
            throw new InsufficientPermissionException(Permission.BAN_MEMBERS);

        Route.CompiledRoute route = Route.Guilds.GET_BANS.compile(getId());
        return new RestActionImpl<>(getJDA(), route, (response, request) ->
        {
            EntityBuilder builder = api.get().getEntityBuilder();
            List<Ban> bans = new LinkedList<>();
            DataArray bannedArr = response.getArray();

            for (int i = 0; i < bannedArr.length(); i++)
            {
                final DataObject object = bannedArr.getObject(i);
                DataObject user = object.getObject("user");
                bans.add(new Ban(builder.createFakeUser(user, false), object.getString("reason", null)));
            }
            return Collections.unmodifiableList(bans);
        });
    }

    @Nonnull
    @Override
    public RestAction<Ban> retrieveBanById(@Nonnull String userId)
    {
        if (!getSelfMember().hasPermission(Permission.BAN_MEMBERS))
            throw new InsufficientPermissionException(Permission.BAN_MEMBERS);

        Checks.isSnowflake(userId, "User ID");

        Route.CompiledRoute route = Route.Guilds.GET_BAN.compile(getId(), userId);
        return new RestActionImpl<>(getJDA(), route, (response, request) ->
        {

            EntityBuilder builder = api.get().getEntityBuilder();
            DataObject bannedObj = response.getObject();
            DataObject user = bannedObj.getObject("user");
            return new Ban(builder.createFakeUser(user, false), bannedObj.getString("reason", null));
        });
    }

    @Override
    public RestAction<Integer> retrievePrunableMemberCount(int days)
    {
        if (!getSelfMember().hasPermission(Permission.KICK_MEMBERS))
            throw new InsufficientPermissionException(Permission.KICK_MEMBERS);

        if (days < 1)
            throw new IllegalArgumentException("Days amount must be at minimum 1 day.");

        Route.CompiledRoute route = Route.Guilds.PRUNABLE_COUNT.compile(getId()).withQueryParams("days", Integer.toString(days));
        return new RestActionImpl<>(getJDA(), route, (response, request) -> response.getObject().getInt("pruned"));
    }

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
                                    .filter(c -> role.hasPermission(c, Permission.MESSAGE_READ))
                                    .sorted(Comparator.naturalOrder())
                                    .findFirst().orElse(null);
    }

    @Override
    public GuildManager getManager()
    {
        GuildManager mng = manager;
        if (mng == null)
        {
            mng = MiscUtil.locked(mngLock, () ->
            {
                if (manager == null)
                    manager = new GuildManagerImpl(this);
                return manager;
            });
        }
        return mng;
    }

    @Override
    public GuildController getController()
    {
        GuildController ctrl = controller;
        if (ctrl == null)
        {
            ctrl = MiscUtil.locked(mngLock, () ->
            {
                if (controller == null)
                    controller = new GuildController(this);
                return controller;
            });
        }
        return ctrl;
    }

    @Override
    public AuditLogPaginationAction retrieveAuditLogs()
    {
        return new AuditLogPaginationActionImpl(this);
    }

    @Override
    public RestAction<Void> leave()
    {
        if (owner.equals(getSelfMember()))
            throw new IllegalStateException("Cannot leave a guild that you are the owner of! Transfer guild ownership first!");

        Route.CompiledRoute route = Route.Self.LEAVE_GUILD.compile(getId());
        return new RestActionImpl<>(getJDA(), route);
    }

    @Override
    public RestAction<Void> delete()
    {
        if (!getJDA().getSelfUser().isBot() && getJDA().getSelfUser().isMfaEnabled())
            throw new IllegalStateException("Cannot delete a guild without providing MFA code. Use Guild#delete(String)");

        return delete(null);
    }

    @Override
    public RestAction<Void> delete(String mfaCode)
    {
        if (!owner.equals(getSelfMember()))
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

    @Override
    public AudioManager getAudioManager()
    {
        if (!getJDA().isAudioEnabled())
            throw new IllegalStateException("Audio is disabled. Cannot retrieve an AudioManager while audio is disabled.");

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

    @Override
    public JDAImpl getJDA()
    {
        return api.get();
    }

    @Override
    public List<GuildVoiceState> getVoiceStates()
    {
        return Collections.unmodifiableList(
                getMembersView().stream().map(Member::getVoiceState).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    @Override
    public VerificationLevel getVerificationLevel()
    {
        return verificationLevel;
    }

    @Override
    public NotificationLevel getDefaultNotificationLevel()
    {
        return defaultNotificationLevel;
    }

    @Override
    public MFALevel getRequiredMFALevel()
    {
        return mfaLevel;
    }

    @Override
    public ExplicitContentLevel getExplicitContentLevel()
    {
        return explicitContentLevel;
    }

    @Override
    public boolean checkVerification()
    {
        if (getJDA().getAccountType() == AccountType.BOT)
            return true;
        if(canSendVerification)
            return true;

        if (getJDA().getSelfUser().getPhoneNumber() != null)
            return canSendVerification = true;

        switch (verificationLevel)
        {
            case VERY_HIGH:
                break; // we already checked for a verified phone number
            case HIGH:
                if (ChronoUnit.MINUTES.between(getSelfMember().getTimeJoined(), OffsetDateTime.now()) < 10)
                    break;
            case MEDIUM:
                if (ChronoUnit.MINUTES.between(getJDA().getSelfUser().getTimeCreated(), OffsetDateTime.now()) < 5)
                    break;
            case LOW:
                if (!getJDA().getSelfUser().isVerified())
                    break;
            case NONE:
                canSendVerification = true;
                return true;
            case UNKNOWN:
                return true; // try and let discord decide
        }
        return false;
    }

    @Override
    public boolean isAvailable()
    {
        return available;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    // ---- Setters -----

    public GuildImpl setAvailable(boolean available)
    {
        this.available = available;
        return this;
    }

    public GuildImpl setOwner(Member owner)
    {
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

    public GuildImpl setRegion(String region)
    {
        this.region = region;
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

    public GuildImpl setPublicRole(Role publicRole)
    {
        this.publicRole = publicRole;
        return this;
    }

    public GuildImpl setVerificationLevel(VerificationLevel level)
    {
        this.verificationLevel = level;
        this.canSendVerification = false;   //recalc on next send
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

    public GuildImpl setOwnerId(long ownerId)
    {
        this.ownerId = ownerId;
        return this;
    }

    // -- Map getters --

    public SnowflakeCacheViewImpl<Category> getCategoriesView()
    {
        return categoryCache;
    }

    public SnowflakeCacheViewImpl<TextChannel> getTextChannelsView()
    {
        return textChannelCache;
    }

    public SnowflakeCacheViewImpl<VoiceChannel> getVoiceChannelsView()
    {
        return voiceChannelCache;
    }

    public SnowflakeCacheViewImpl<Role> getRolesView()
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

    public TLongObjectMap<DataObject> getCachedPresenceMap()
    {
        return cachedPresences;
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

    @Override
    public RestAction<List<Invite>> retrieveInvites()
    {
        if (!this.getSelfMember().hasPermission(Permission.MANAGE_SERVER))
            throw new InsufficientPermissionException(Permission.MANAGE_SERVER);

        final Route.CompiledRoute route = Route.Invites.GET_GUILD_INVITES.compile(getId());

        return new RestActionImpl<>(getJDA(), route, (response, request) ->
        {
            EntityBuilder entityBuilder = api.get().getEntityBuilder();
            DataArray array = response.getArray();
            List<Invite> invites = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++)
                invites.add(entityBuilder.createInvite(array.getObject(i)));
            return Collections.unmodifiableList(invites);
        });
    }
}
