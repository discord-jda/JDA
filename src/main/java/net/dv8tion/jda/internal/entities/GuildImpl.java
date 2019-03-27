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
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.managers.GuildManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.MemberAction;
import net.dv8tion.jda.api.requests.restaction.RoleAction;
import net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction;
import net.dv8tion.jda.api.requests.restaction.order.ChannelOrderAction;
import net.dv8tion.jda.api.requests.restaction.order.RoleOrderAction;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.cache.MemberCacheView;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.managers.AudioManagerImpl;
import net.dv8tion.jda.internal.managers.GuildManagerImpl;
import net.dv8tion.jda.internal.requests.EmptyRestAction;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;
import net.dv8tion.jda.internal.requests.restaction.ChannelActionImpl;
import net.dv8tion.jda.internal.requests.restaction.MemberActionImpl;
import net.dv8tion.jda.internal.requests.restaction.RoleActionImpl;
import net.dv8tion.jda.internal.requests.restaction.order.CategoryOrderActionImpl;
import net.dv8tion.jda.internal.requests.restaction.order.ChannelOrderActionImpl;
import net.dv8tion.jda.internal.requests.restaction.order.RoleOrderActionImpl;
import net.dv8tion.jda.internal.requests.restaction.pagination.AuditLogPaginationActionImpl;
import net.dv8tion.jda.internal.utils.*;
import net.dv8tion.jda.internal.utils.cache.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private final TLongObjectMap<JSONObject> cachedPresences = MiscUtil.newLongMap();

    private final ReentrantLock mngLock = new ReentrantLock();
    private volatile GuildManager manager;

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
            JSONArray arr = response.getArray();
            for (int i = 0; arr != null && i < arr.length(); i++)
            {
                JSONObject obj = arr.getJSONObject(i);
                if (!includeDeprecated && Helpers.optBoolean(obj, "deprecated"))
                {
                    continue;
                }
                String id = obj.optString("id");
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
            JSONArray array = response.getArray();
            List<Webhook> webhooks = new ArrayList<>(array.length());
            EntityBuilder builder = api.get().getEntityBuilder();

            for (Object object : array)
            {
                try
                {
                    webhooks.add(builder.createWebhook((JSONObject) object));
                }
                catch (JSONException | NullPointerException e)
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
            JSONArray emotes = response.getArray();
            List<ListedEmote> list = new ArrayList<>(emotes.length());
            for (int i = 0; i < emotes.length(); i++)
            {
                JSONObject emote = emotes.getJSONObject(i);
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
            JSONArray bannedArr = response.getArray();

            for (int i = 0; i < bannedArr.length(); i++)
            {
                final JSONObject object = bannedArr.getJSONObject(i);
                JSONObject user = object.getJSONObject("user");
                bans.add(new Ban(builder.createFakeUser(user, false), object.optString("reason", null)));
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
            JSONObject bannedObj = response.getObject();
            JSONObject user = bannedObj.getJSONObject("user");
            return new Ban(builder.createFakeUser(user, false), bannedObj.optString("reason", null));
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

        JSONObject mfaBody = null;
        if (!getJDA().getSelfUser().isBot() && getJDA().getSelfUser().isMfaEnabled())
        {
            Checks.notEmpty(mfaCode, "Provided MultiFactor Auth code");
            mfaBody = new JSONObject().put("code", mfaCode);
        }

        Route.CompiledRoute route = Route.Guilds.DELETE_GUILD.compile(getId());
        return new RestActionImpl<Void>(getJDA(), route, mfaBody);
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

    /**
     * Used to move a {@link net.dv8tion.jda.api.entities.Member Member} from one {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}
     * to another {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}.
     * <br>As a note, you cannot move a Member that isn't already in a VoiceChannel. Also they must be in a VoiceChannel
     * in the same Guild as the one that you are moving them to.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be moved due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The specified channel was deleted before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.api.entities.Member Member} that you are moving.
     * @param  voiceChannel
     *         The destination {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel} to which the member is being
     *         moved to.
     *
     * @throws IllegalStateException
     *         If the Member isn't currently in a VoiceChannel in this Guild, or {@link net.dv8tion.jda.api.utils.cache.CacheFlag#VOICE_STATE} is disabled.
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the provided arguments is {@code null}</li>
     *             <li>If the provided Member isn't part of this {@link net.dv8tion.jda.api.entities.Guild Guild}</li>
     *             <li>If the provided VoiceChannel isn't part of this {@link net.dv8tion.jda.api.entities.Guild Guild}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         <ul>
     *             <li>If this account doesn't have {@link net.dv8tion.jda.api.Permission#VOICE_MOVE_OTHERS}
     *                 in the VoiceChannel that the Member is currently in.</li>
     *             <li>If this account <b>AND</b> the Member being moved don't have
     *                 {@link net.dv8tion.jda.api.Permission#VOICE_CONNECT} for the destination VoiceChannel.</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.RestAction RestAction}
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public RestAction<Void> moveVoiceMember(@Nonnull Member member, @Nonnull VoiceChannel voiceChannel)
    {
        Checks.notNull(member, "Member");
        Checks.notNull(voiceChannel, "VoiceChannel");
        checkGuild(member.getGuild(), "Member");
        checkGuild(voiceChannel.getGuild(), "VoiceChannel");

        GuildVoiceState vState = member.getVoiceState();
        if (vState == null)
            throw new IllegalStateException("Cannot move a Member with disabled CacheFlag.VOICE_STATE");
        if (!vState.inVoiceChannel())
            throw new IllegalStateException("You cannot move a Member who isn't in a VoiceChannel!");

        if (!PermissionUtil.checkPermission(vState.getChannel(), getSelfMember(), Permission.VOICE_MOVE_OTHERS))
            throw new InsufficientPermissionException(Permission.VOICE_MOVE_OTHERS, "This account does not have Permission to MOVE_OTHERS out of the channel that the Member is currently in.");

        if (!PermissionUtil.checkPermission(voiceChannel, getSelfMember(), Permission.VOICE_CONNECT)
            && !PermissionUtil.checkPermission(voiceChannel, member, Permission.VOICE_CONNECT))
            throw new InsufficientPermissionException(Permission.VOICE_CONNECT,
                                                      "Neither this account nor the Member that is attempting to be moved have the VOICE_CONNECT permission " +
                                                      "for the destination VoiceChannel, so the move cannot be done.");

        JSONObject body = new JSONObject().put("channel_id", voiceChannel.getId());
        Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(getId(), member.getUser().getId());

        return new RestActionImpl<>(getJDA(), route, body);
    }

    /**
     * Changes a Member's nickname in this guild.
     * The nickname is visible to all members of this guild.
     *
     * <p>To change the nickname for the currently logged in account
     * only the Permission {@link net.dv8tion.jda.api.Permission#NICKNAME_CHANGE NICKNAME_CHANGE} is required.
     * <br>To change the nickname of <b>any</b> {@link net.dv8tion.jda.api.entities.Member Member} for this {@link net.dv8tion.jda.api.entities.Guild Guild}
     * the Permission {@link net.dv8tion.jda.api.Permission#NICKNAME_MANAGE NICKNAME_MANAGE} is required.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The nickname of the target Member is not modifiable due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.api.entities.Member Member} for which the nickname should be changed.
     * @param  nickname
     *         The new nickname of the {@link net.dv8tion.jda.api.entities.Member Member}, provide {@code null} or an
     *         empty String to reset the nickname
     *
     * @throws IllegalArgumentException
     *         If the specified {@link net.dv8tion.jda.api.entities.Member Member}
     *         is not from the same {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *         Or if the provided member is {@code null}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         <ul>
     *             <li>If attempting to set nickname for self and the logged in account has neither {@link net.dv8tion.jda.api.Permission#NICKNAME_CHANGE}
     *                 or {@link net.dv8tion.jda.api.Permission#NICKNAME_MANAGE}</li>
     *             <li>If attempting to set nickname for another member and the logged in account does not have {@link net.dv8tion.jda.api.Permission#NICKNAME_MANAGE}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If attempting to set nickname for another member and the logged in account cannot manipulate the other user due to permission hierarchy position.
     *         <br>See {@link net.dv8tion.jda.internal.utils.PermissionUtil#canInteract(Member, Member) PermissionUtil.canInteract(Member, Member)}
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public AuditableRestAction<Void> modifyNickname(@Nonnull Member member, String nickname)
    {
        Checks.notNull(member, "Member");
        checkGuild(member.getGuild(), "Member");

        if(member.equals(getSelfMember()))
        {
            if(!member.hasPermission(Permission.NICKNAME_CHANGE)
               && !member.hasPermission(Permission.NICKNAME_MANAGE))
                throw new InsufficientPermissionException(Permission.NICKNAME_CHANGE, "You neither have NICKNAME_CHANGE nor NICKNAME_MANAGE permission!");
        }
        else
        {
            checkPermission(Permission.NICKNAME_MANAGE);
            checkPosition(member);
        }

        if (Objects.equals(nickname, member.getNickname()))
            return new EmptyRestAction<>(getJDA(), null);

        if (nickname == null)
            nickname = "";

        JSONObject body = new JSONObject().put("nick", nickname);

        Route.CompiledRoute route;
        if (member.equals(getSelfMember()))
            route = Route.Guilds.MODIFY_SELF_NICK.compile(getId());
        else
            route = Route.Guilds.MODIFY_MEMBER.compile(getId(), member.getUser().getId());

        return new AuditableRestActionImpl<>(getJDA(), route, body);
    }

    /**
     * This method will prune (kick) all members who were offline for at least <i>days</i> days.
     * <br>The RestAction returned from this method will return the amount of Members that were pruned.
     * <br>You can use {@link Guild#retrievePrunableMemberCount(int)} to determine how many Members would be pruned if you were to
     * call this method.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The prune cannot finished due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  days
     *         Minimum number of days since a member has been offline to get affected.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the account doesn't have {@link net.dv8tion.jda.api.Permission#KICK_MEMBERS KICK_MEMBER} Permission.
     * @throws IllegalArgumentException
     *         If the provided days are less than {@code 1}
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction} - Type: Integer
     *         <br>The amount of Members that were pruned from the Guild.
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public AuditableRestAction<Integer> prune(int days)
    {
        checkPermission(Permission.KICK_MEMBERS);

        Checks.check(days >= 1, "Days amount must be at minimum 1 day.");

        Route.CompiledRoute route = Route.Guilds.PRUNE_MEMBERS.compile(getId()).withQueryParams("days", Integer.toString(days));
        return new AuditableRestActionImpl<>(getJDA(), route, (response, request) -> response.getObject().getInt("pruned"));
    }

    /**
     * Kicks a {@link net.dv8tion.jda.api.entities.Member Member} from the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.api.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.api.entities.User User}
     * until Discord sends the {@link net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be kicked due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.api.entities.Member Member} to kick
     *         from the from the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     * @param  reason
     *         The reason for this action or {@code null} if there is no specified reason
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided member is not a Member of this Guild or is {@code null}
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#KICK_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot kick the other member due to permission hierarchy position.
     *         <br>See {@link net.dv8tion.jda.internal.utils.PermissionUtil#canInteract(Member, Member) PermissionUtil.canInteract(Member, Member)}
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     *         Kicks the provided Member from the current Guild
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public AuditableRestAction<Void> kick(@Nonnull Member member, String reason)
    {
        Checks.notNull(member, "member");
        checkGuild(member.getGuild(), "member");
        checkPermission(Permission.KICK_MEMBERS);
        checkPosition(member);

        final String userId = member.getUser().getId();
        final String guildId = getId();

        Route.CompiledRoute route = Route.Guilds.KICK_MEMBER.compile(guildId, userId);
        if (reason != null && !reason.isEmpty())
            route = route.withQueryParams("reason", EncodingUtil.encodeUTF8(reason));

        return new AuditableRestActionImpl<>(getJDA(), route);
    }

    /**
     * Bans a {@link net.dv8tion.jda.api.entities.User User} and deletes messages sent by the user
     * based on the amount of delDays.
     * <br>If you wish to ban a user without deleting any messages, provide delDays with a value of 0.
     * This change will be applied immediately.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.api.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.api.entities.User User's}
     * {@link net.dv8tion.jda.api.entities.Member Member} object (if the User was in the Guild)
     * until Discord sends the {@link net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be banned due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  user
     *         The {@link net.dv8tion.jda.api.entities.User User} to ban.
     * @param  delDays
     *         The history of messages, in days, that will be deleted.
     * @param  reason
     *         The reason for this action or {@code null} if there is no specified reason
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot ban the other user due to permission hierarchy position.
     *         <br>See {@link net.dv8tion.jda.internal.utils.PermissionUtil#canInteract(Member, Member) PermissionUtil.canInteract(Member, Member)}
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided amount of days (delDays) is less than 0.</li>
     *             <li>If the provided amount of days (delDays) is bigger than 7.</li>
     *             <li>If the provided user is null</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public AuditableRestAction<Void> ban(@Nonnull User user, int delDays, String reason)
    {
        Checks.notNull(user, "User");
        checkPermission(Permission.BAN_MEMBERS);

        if (isMember(user)) // If user is in guild. Check if we are able to ban.
            checkPosition(getMember(user));

        Checks.notNegative(delDays, "Deletion Days");

        Checks.check(delDays <= 7, "Deletion Days must not be bigger than 7.");

        final String userId = user.getId();

        Route.CompiledRoute route = Route.Guilds.BAN.compile(getId(), userId);
        if (reason != null && !reason.isEmpty())
            route = route.withQueryParams("reason", EncodingUtil.encodeUTF8(reason));
        if (delDays > 0)
            route = route.withQueryParams("delete-message-days", Integer.toString(delDays));

        return new AuditableRestActionImpl<>(getJDA(), route);
    }

    /**
     * Bans the a user specified by the userId and deletes messages sent by the user
     * based on the amount of delDays.
     * <br>If you wish to ban a user without deleting any messages, provide delDays with a value of 0.
     * This change will be applied immediately.
     *
     * <p><b>Note:</b> {@link net.dv8tion.jda.api.entities.Guild#getMembers()} will still contain the {@link net.dv8tion.jda.api.entities.User User's}
     * {@link net.dv8tion.jda.api.entities.Member Member} object (if the User was in the Guild)
     * until Discord sends the {@link net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be banned due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER UNKNOWN_USER}
     *     <br>The specified User does not exit</li>
     * </ul>
     *
     * @param  userId
     *         The id of the {@link net.dv8tion.jda.api.entities.User User} to ban.
     * @param  delDays
     *         The history of messages, in days, that will be deleted.
     * @param  reason
     *         The reason for this action or {@code null} if there is no specified reason
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#BAN_MEMBERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the logged in account cannot ban the other user due to permission hierarchy position.
     *         <br>See {@link net.dv8tion.jda.internal.utils.PermissionUtil#canInteract(Member, Member) PermissionUtil.canInteract(Member, Member)}
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the provided amount of days (delDays) is less than 0.</li>
     *             <li>If the provided amount of days (delDays) is bigger than 7.</li>
     *             <li>If the provided userId is null</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public AuditableRestAction<Void> ban(@Nonnull String userId, int delDays, String reason)
    {
        Checks.notNull(userId, "User");
        checkPermission(Permission.BAN_MEMBERS);

        User user = getJDA().getUserById(userId);
        if (user != null) // If we have the user cached then we should use the additional information available to use during the ban process.
            return ban(user, delDays, reason);

        Checks.notNegative(delDays, "Deletion Days");

        Checks.check(delDays <= 7, "Deletion Days must not be bigger than 7.");

        Route.CompiledRoute route = Route.Guilds.BAN.compile(getId(), userId);
        if (reason != null && !reason.isEmpty())
            route = route.withQueryParams("reason", EncodingUtil.encodeUTF8(reason));
        if (delDays > 0)
            route = route.withQueryParams("delete-message-days", Integer.toString(delDays));

        return new AuditableRestActionImpl<>(getJDA(), route);
    }

    /**
     * Unbans the a user specified by the userId from this Guild.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be unbanned due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_USER UNKNOWN_USER}
     *     <br>The specified User does not exist</li>
     * </ul>
     *
     * @param  userId
     *         The id of the {@link net.dv8tion.jda.api.entities.User User} to unban.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#BAN_MEMBERS} permission.
     * @throws IllegalArgumentException
     *         If the provided id is null or blank
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public AuditableRestAction<Void> unban(@Nonnull String userId)
    {
        Checks.isSnowflake(userId, "User ID");
        checkPermission(Permission.BAN_MEMBERS);

        Route.CompiledRoute route = Route.Guilds.UNBAN.compile(getId(), userId);
        return new AuditableRestActionImpl<>(getJDA(), route);
    }

    /**
     * Sets the Guild Deafened state state of the {@link net.dv8tion.jda.api.entities.Member Member} based on the provided
     * boolean.
     *
     * <p><b>Note:</b> The Member's {@link net.dv8tion.jda.api.entities.GuildVoiceState#isGuildDeafened() GuildVoiceState.isGuildDeafened()} value won't change
     * until JDA receives the {@link net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildDeafenEvent GuildVoiceGuildDeafenEvent} event related to this change.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be deafened due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.api.entities.Member Member} who's {@link GuildVoiceState VoiceState} is being changed.
     * @param  deafen
     *         Whether this {@link net.dv8tion.jda.api.entities.Member Member} should be deafened or undeafened.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#VOICE_DEAF_OTHERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the provided member is the Guild's owner. You cannot modify the owner of a Guild.
     * @throws IllegalArgumentException
     *         If the provided member is not from this Guild or null.
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public AuditableRestAction<Void> setDeafen(@Nonnull Member member, boolean deafen)
    {
        Checks.notNull(member, "Member");
        checkGuild(member.getGuild(), "Member");
        checkPermission(Permission.VOICE_DEAF_OTHERS);

        //We check the owner instead of Position because, apparently, Discord doesn't care about position for
        // muting and deafening, only whether the affected Member is the owner.
        if (getOwner().equals(member))
            throw new HierarchyException("Cannot modify Guild Deafen status the Owner of the Guild");

        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState != null && voiceState.isGuildDeafened() == deafen)
            return new EmptyRestAction<>(getJDA(), null);

        JSONObject body = new JSONObject().put("deaf", deafen);
        Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(getId(), member.getUser().getId());
        return new AuditableRestActionImpl<>(getJDA(), route, body);
    }

    /**
     * Sets the Guild Muted state state of the {@link net.dv8tion.jda.api.entities.Member Member} based on the provided
     * boolean.
     *
     * <p><b>Note:</b> The Member's {@link net.dv8tion.jda.api.entities.GuildVoiceState#isGuildMuted() GuildVoiceState.isGuildMuted()} value won't change
     * until JDA receives the {@link net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent GuildVoiceGuildMuteEvent} event related to this change.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The target Member cannot be muted due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The specified Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.api.entities.Member Member} who's {@link GuildVoiceState VoiceState} is being changed.
     * @param  mute
     *         Whether this {@link net.dv8tion.jda.api.entities.Member Member} should be muted or unmuted.
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#VOICE_DEAF_OTHERS} permission.
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the provided member is the Guild's owner. You cannot modify the owner of a Guild.
     * @throws java.lang.IllegalArgumentException
     *         If the provided member is not from this Guild or null.
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public AuditableRestAction<Void> setMute(@Nonnull Member member, boolean mute)
    {
        Checks.notNull(member, "Member");
        checkGuild(member.getGuild(), "Member");
        checkPermission(Permission.VOICE_MUTE_OTHERS);

        //We check the owner instead of Position because, apparently, Discord doesn't care about position for
        // muting and deafening, only whether the affected Member is the owner.
        if (getOwner().equals(member))
            throw new HierarchyException("Cannot modify Guild Mute status the Owner of the Guild");

        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState != null && voiceState.isGuildMuted() == mute)
            return new EmptyRestAction<>(getJDA(), null);

        JSONObject body = new JSONObject().put("mute", mute);
        Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(getId(), member.getUser().getId());
        return new AuditableRestActionImpl<>(getJDA(), route, body);
    }

    /**
     * Atomically assigns the provided {@link net.dv8tion.jda.api.entities.Role Role} to the specified {@link net.dv8tion.jda.api.entities.Member Member}.
     * <br><b>This can be used together with other role modification methods as it does not require an updated cache!</b>
     *
     * <p>If multiple roles should be added/removed (efficiently) in one request
     * you may use {@link #modifyMemberRoles(Member, Collection, Collection) modifyMemberRoles(Member, Collection, Collection)} or similar methods.
     *
     * <p>If the specified role is already present in the member's set of roles this does nothing.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_ROLE UNKNOWN_ROLE}
     *     <br>If the specified Role does not exist</li>
     * </ul>
     *
     * @param  member
     *         The target member who will receive the new role
     * @param  role
     *         The role which should be assigned atomically
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the specified member/role are not from the current Guild</li>
     *             <li>Either member or role are {@code null}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public AuditableRestAction<Void> addSingleRoleToMember(@Nonnull Member member, @Nonnull Role role)
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

    /**
     * Atomically removes the provided {@link net.dv8tion.jda.api.entities.Role Role} from the specified {@link net.dv8tion.jda.api.entities.Member Member}.
     * <br><b>This can be used together with other role modification methods as it does not require an updated cache!</b>
     *
     * <p>If multiple roles should be added/removed (efficiently) in one request
     * you may use {@link #modifyMemberRoles(Member, Collection, Collection) modifyMemberRoles(Member, Collection, Collection)} or similar methods.
     *
     * <p>If the specified role is not present in the member's set of roles this does nothing.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_ROLE UNKNOWN_ROLE}
     *     <br>If the specified Role does not exist</li>
     * </ul>
     *
     * @param  member
     *         The target member who will lose the specified role
     * @param  role
     *         The role which should be removed atomically
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the specified member/role are not from the current Guild</li>
     *             <li>Either member or role are {@code null}</li>
     *         </ul>
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public AuditableRestAction<Void> removeSingleRoleFromMember(@Nonnull Member member, @Nonnull Role role)
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

    /**
     * Modifies the {@link net.dv8tion.jda.api.entities.Role Roles} of the specified {@link net.dv8tion.jda.api.entities.Member Member}
     * by adding and removing a collection of roles.
     * <br>None of the provided roles may be the <u>Public Role</u> of the current Guild.
     * <br>If a role is both in {@code rolesToAdd} and {@code rolesToRemove} it will be removed.
     *
     * <p>None of the provided collections may be null
     * <br>To only add or remove roles use either {@link #removeRolesFromMember(Member, Collection)} or {@link #addRolesToMember(Member, Collection)}
     *
     * <h1>Warning</h1>
     * <b>This may <u>not</u> be used together with any other role add/remove/modify methods for the same Member
     * within one event listener cycle! The changes made by this require cache updates which are triggered by
     * lifecycle events which are received later. This may only be called again once the specific Member has been updated
     * by a {@link net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent GenericGuildMemberEvent} targeting the same Member.</b>
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         The {@link net.dv8tion.jda.api.entities.Member Member} that should be modified
     * @param  rolesToAdd
     *         A {@link java.util.Collection Collection} of {@link net.dv8tion.jda.api.entities.Role Roles}
     *         to add to the current Roles the specified {@link net.dv8tion.jda.api.entities.Member Member} already has
     * @param  rolesToRemove
     *         A {@link java.util.Collection Collection} of {@link net.dv8tion.jda.api.entities.Role Roles}
     *         to remove from the current Roles the specified {@link net.dv8tion.jda.api.entities.Member Member} already has
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the provided arguments is {@code null}</li>
     *             <li>If any of the specified Roles is managed or is the {@code Public Role} of the Guild</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public AuditableRestAction<Void> modifyMemberRoles(@Nonnull Member member, @Nonnull Collection<Role> rolesToAdd, @Nonnull Collection<Role> rolesToRemove)
    {
        Checks.notNull(member, "Member");
        Checks.notNull(rolesToAdd, "Collection containing roles to be added to the member");
        Checks.notNull(rolesToRemove, "Collection containing roles to be removed from the member");
        checkGuild(member.getGuild(), "Member");
        checkPermission(Permission.MANAGE_ROLES);
        rolesToAdd.forEach(role ->
                           {
                               Checks.notNull(role, "Role in rolesToAdd");
                               checkGuild(role.getGuild(), "Role: " + role.toString());
                               checkPosition(role);
                               Checks.check(!role.isManaged(), "Cannot add a Managed role to a Member. Role: %s", role.toString());
                           });
        rolesToRemove.forEach(role ->
                              {
                                  Checks.notNull(role, "Role in rolesToRemove");
                                  checkGuild(role.getGuild(), "Role: " + role.toString());
                                  checkPosition(role);
                                  Checks.check(!role.isManaged(), "Cannot remove a Managed role from a Member. Role: %s", role.toString());
                              });

        Set<Role> currentRoles = new HashSet<>(((MemberImpl) member).getRoleSet());
        Set<Role> newRolesToAdd = new HashSet<>(rolesToAdd);
        newRolesToAdd.removeAll(rolesToRemove);

        // If no changes have been made we return an EmptyRestAction instead
        if (currentRoles.addAll(newRolesToAdd))
            currentRoles.removeAll(rolesToRemove);
        else if (!currentRoles.removeAll(rolesToRemove))
            return new EmptyRestAction<>(getJDA());

        Checks.check(!currentRoles.contains(getPublicRole()),
                     "Cannot add the PublicRole of a Guild to a Member. All members have this role by default!");

        JSONObject body = new JSONObject()
            .put("roles", currentRoles.stream().map(Role::getId).collect(Collectors.toList()));
        Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(getId(), member.getUser().getId());

        return new AuditableRestActionImpl<>(getJDA(), route, body);
    }

    /**
     * Modifies the complete {@link net.dv8tion.jda.api.entities.Role Role} set of the specified {@link net.dv8tion.jda.api.entities.Member Member}
     * <br>The provided roles will replace all current Roles of the specified Member.
     *
     * <p><u>The new roles <b>must not</b> contain the Public Role of the Guild</u>
     *
     * <h1>Warning</h1>
     * <b>This may <u>not</u> be used together with any other role add/remove/modify methods for the same Member
     * within one event listener cycle! The changes made by this require cache updates which are triggered by
     * lifecycle events which are received later. This may only be called again once the specific Member has been updated
     * by a {@link net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent GenericGuildMemberEvent} targeting the same Member.</b>
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The Members Roles could not be modified due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  member
     *         A {@link net.dv8tion.jda.api.entities.Member Member} of which to override the Roles of
     * @param  roles
     *         New collection of {@link net.dv8tion.jda.api.entities.Role Roles} for the specified Member
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES Permission.MANAGE_ROLES}
     * @throws net.dv8tion.jda.api.exceptions.HierarchyException
     *         If the provided roles are higher in the Guild's hierarchy
     *         and thus cannot be modified by the currently logged in account
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any of the provided arguments is {@code null}</li>
     *             <li>If any of the provided arguments is not from this Guild</li>
     *             <li>If any of the specified {@link net.dv8tion.jda.api.entities.Role Roles} is managed</li>
     *             <li>If any of the specified {@link net.dv8tion.jda.api.entities.Role Roles} is the {@code Public Role} of this Guild</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     *
     * @see    #modifyMemberRoles(Member, Collection)
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public AuditableRestAction<Void> modifyMemberRoles(@Nonnull Member member, @Nonnull Collection<Role> roles)
    {
        Checks.notNull(member, "Member");
        Checks.notNull(roles, "Roles");
        checkGuild(member.getGuild(), "Member");
        roles.forEach(role ->
                      {
                          Checks.notNull(role, "Role in collection");
                          checkGuild(role.getGuild(), "Role: " + role.toString());
                          checkPosition(role);
                      });

        Checks.check(!roles.contains(getPublicRole()),
                     "Cannot add the PublicRole of a Guild to a Member. All members have this role by default!");

        // Return an empty rest action if there were no changes
        final List<Role> memberRoles = member.getRoles();
        if (memberRoles.size() == roles.size() && memberRoles.containsAll(roles))
            return new EmptyRestAction<>(getJDA());

        //Make sure that the current managed roles are preserved and no new ones are added.
        List<Role> currentManaged = memberRoles.stream().filter(Role::isManaged).collect(Collectors.toList());
        List<Role> newManaged = roles.stream().filter(Role::isManaged).collect(Collectors.toList());
        if (!currentManaged.isEmpty() || !newManaged.isEmpty())
        {
            if (!newManaged.containsAll(currentManaged))
            {
                currentManaged.removeAll(newManaged);
                throw new IllegalArgumentException("Cannot remove managed roles from a member! Roles: " + currentManaged.toString());
            }
            if (!currentManaged.containsAll(newManaged))
            {
                newManaged.removeAll(currentManaged);
                throw new IllegalArgumentException("Cannot add managed roles to a member! Roles: " + newManaged.toString());
            }
        }

        //This is identical to the rest action stuff in #modifyMemberRoles(Member, Collection<Role>, Collection<Role>)
        JSONObject body = new JSONObject()
            .put("roles", roles.stream().map(Role::getId).collect(Collectors.toList()));
        Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(getId(), member.getUser().getId());

        return new AuditableRestActionImpl<>(getJDA(), route, body);
    }

    /**
     * Transfers the Guild ownership to the specified {@link net.dv8tion.jda.api.entities.Member Member}
     * <br>Only available if the currently logged in account is the owner of this Guild
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The currently logged in account lost ownership before completing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_MEMBER UNKNOWN_MEMBER}
     *     <br>The target Member was removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  newOwner
     *         Not-null Member to transfer ownership to
     *
     * @throws net.dv8tion.jda.api.exceptions.PermissionException
     *         If the currently logged in account is not the owner of this Guild
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the specified Member is {@code null} or not from the same Guild</li>
     *             <li>If the specified Member already is the Guild owner</li>
     *             <li>If the specified Member is a bot account ({@link net.dv8tion.jda.api.AccountType#BOT AccountType.BOT})</li>
     *         </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public AuditableRestAction<Void> transferOwnership(@Nonnull Member newOwner)
    {
        Checks.notNull(newOwner, "Member");
        checkGuild(newOwner.getGuild(), "Member");
        if (!getOwner().equals(getSelfMember()))
            throw new PermissionException("The logged in account must be the owner of this Guild to be able to transfer ownership");

        Checks.check(!getSelfMember().equals(newOwner),
                     "The member provided as the newOwner is the currently logged in account. Provide a different member to give ownership to.");

        Checks.check(!newOwner.getUser().isBot(), "Cannot transfer ownership of a Guild to a Bot!");

        JSONObject body = new JSONObject().put("owner_id", newOwner.getUser().getId());
        Route.CompiledRoute route = Route.Guilds.MODIFY_GUILD.compile(getId());
        return new AuditableRestActionImpl<>(getJDA(), route, body);
    }

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.TextChannel TextChannel} in this Guild.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  name
     *         The name of the TextChannel to create
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL} permission
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or empty or greater than 100 characters in length
     *
     * @return A specific {@link net.dv8tion.jda.api.requests.restaction.ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new TextChannel before creating it
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelAction<TextChannel> createTextChannel(@Nonnull String name)
    {
        checkPermission(Permission.MANAGE_CHANNEL);
        Checks.notBlank(name, "Name");
        name = name.trim();

        Checks.check(name.length() > 0 && name.length() <= 100, "Provided name must be 1 - 100 characters in length");
        return new ChannelActionImpl<>(TextChannel.class, name, this, ChannelType.TEXT);
    }

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel} in this Guild.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  name
     *         The name of the VoiceChannel to create
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL} permission
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or empty or greater than 100 characters in length
     *
     * @return A specific {@link ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new VoiceChannel before creating it
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelAction<VoiceChannel> createVoiceChannel(@Nonnull String name)
    {
        checkPermission(Permission.MANAGE_CHANNEL);
        Checks.notBlank(name, "Name");
        name = name.trim();

        Checks.check(name.length() > 0 && name.length() <= 100, "Provided name must be 1 - 100 characters in length");
        return new ChannelActionImpl<>(VoiceChannel.class, name, this, ChannelType.VOICE);
    }

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.Category Category} in this Guild.
     * For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  name
     *         The name of the Category to create
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_CHANNEL} permission
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or empty or greater than 100 characters in length
     *
     * @return A specific {@link ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new Category before creating it
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelAction<Category> createCategory(@Nonnull String name)
    {
        checkPermission(Permission.MANAGE_CHANNEL);
        Checks.notBlank(name, "Name");
        name = name.trim();

        Checks.check(name.length() > 0 && name.length() <= 100, "Provided name must be 1 - 100 characters in length");
        return new ChannelActionImpl<>(Category.class, name, this, ChannelType.CATEGORY);
    }

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.Role Role} in this Guild.
     * <br>It will be placed at the bottom (just over the Public Role) to avoid permission hierarchy conflicts.
     * <br>For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES MANAGE_ROLES} Permission
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The role could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MAX_ROLES_PER_GUILD MAX_ROLES_PER_GUILD}
     *     <br>There are too many roles in this Guild</li>
     * </ul>
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_ROLES} Permission
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.RoleAction RoleAction}
     *         <br>Creates a new role with previously selected field values
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public RoleAction createRole()
    {
        checkPermission(Permission.MANAGE_ROLES);
        return new RoleActionImpl(this);
    }

    /**
     * Creates a new {@link net.dv8tion.jda.api.entities.Emote Emote} in this Guild.
     * <br>If one or more Roles are specified the new Emote will only be available to Members with any of the specified Roles (see {@link Member#canInteract(Emote)})
     * <br>For this to be successful, the logged in account has to have the {@link net.dv8tion.jda.api.Permission#MANAGE_EMOTES MANAGE_EMOTES} Permission.
     *
     * <p><b><u>Unicode emojis are not included as {@link net.dv8tion.jda.api.entities.Emote Emote}!</u></b>
     *
     * <p>Note that a guild is limited to 50 normal and 50 animated emotes by default.
     * Some guilds are able to add additional emotes beyond this limitation due to the
     * {@code MORE_EMOJI} feature (see {@link net.dv8tion.jda.api.entities.Guild#getFeatures() Guild.getFeatures()}).
     * <br>Due to simplicity we do not check for these limits.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link net.dv8tion.jda.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The emote could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>We were removed from the Guild before finishing the task</li>
     * </ul>
     *
     * @param  name
     *         The name for the new Emote
     * @param  icon
     *         The {@link net.dv8tion.jda.api.entities.Icon} for the new Emote
     * @param  roles
     *         The {@link net.dv8tion.jda.api.entities.Role Roles} the new Emote should be restricted to
     *         <br>If no roles are provided the Emote will be available to all Members of this Guild
     *
     * @throws net.dv8tion.jda.api.exceptions.InsufficientPermissionException
     *         If the logged in account does not have the {@link net.dv8tion.jda.api.Permission#MANAGE_EMOTES MANAGE_EMOTES} Permission
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.AuditableRestAction AuditableRestAction} - Type: {@link net.dv8tion.jda.api.entities.Emote Emote}
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public AuditableRestAction<Emote> createEmote(@Nonnull String name, @Nonnull Icon icon, @Nonnull Role... roles)
    {
        checkPermission(Permission.MANAGE_EMOTES);
        Checks.notBlank(name, "Emote name");
        Checks.notNull(icon, "Emote icon");
        Checks.notNull(roles, "Roles");

        JSONObject body = new JSONObject();
        body.put("name", name);
        body.put("image", icon.getEncoding());
        if (roles.length > 0) // making sure none of the provided roles are null before mapping them to the snowflake id
            body.put("roles", Stream.of(roles).filter(Objects::nonNull).map(ISnowflake::getId).collect(Collectors.toSet()));

        JDAImpl jda = getJDA();
        Route.CompiledRoute route = Route.Emotes.CREATE_EMOTE.compile(getId());
        return new AuditableRestActionImpl<>(jda, route, body, (response, request) ->
        {
            JSONObject obj = response.getObject();
            return jda.getEntityBuilder().createEmote(this, obj, true);
        });
    }

    /**
     * Modifies the positional order of {@link net.dv8tion.jda.api.entities.Guild#getCategories() Guild.getCategories()}
     * using a specific {@link net.dv8tion.jda.api.requests.RestAction RestAction} extension to allow moving Channels
     * {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveUp(int) up}/{@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveDown(int) down}
     * or {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveTo(int) to} a specific position.
     * <br>This uses <b>ascending</b> order with a 0 based index.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNNKOWN_CHANNEL}
     *     <br>One of the channels has been deleted before the completion of the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild</li>
     * </ul>
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.order.ChannelOrderAction ChannelOrderAction} - Type: {@link net.dv8tion.jda.api.entities.Category Category}
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelOrderAction<Category> modifyCategoryPositions()
    {
        return new ChannelOrderActionImpl<>(this, ChannelType.CATEGORY);
    }

    /**
     * Modifies the positional order of {@link net.dv8tion.jda.api.entities.Guild#getTextChannels() Guild.getTextChannels()}
     * using a specific {@link net.dv8tion.jda.api.requests.RestAction RestAction} extension to allow moving Channels
     * {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveUp(int) up}/{@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveDown(int) down}
     * or {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveTo(int) to} a specific position.
     * <br>This uses <b>ascending</b> order with a 0 based index.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNNKOWN_CHANNEL}
     *     <br>One of the channels has been deleted before the completion of the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild</li>
     * </ul>
     *
     * @return {@link ChannelOrderAction ChannelOrderAction} - Type: {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelOrderAction<TextChannel> modifyTextChannelPositions()
    {
        return new ChannelOrderActionImpl<>(this, ChannelType.TEXT);
    }

    /**
     * Modifies the positional order of {@link net.dv8tion.jda.api.entities.Guild#getVoiceChannels() Guild.getVoiceChannels()}
     * using a specific {@link net.dv8tion.jda.api.requests.RestAction RestAction} extension to allow moving Channels
     * {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveUp(int) up}/{@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveDown(int) down}
     * or {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveTo(int) to} a specific position.
     * <br>This uses <b>ascending</b> order with a 0 based index.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNNKOWN_CHANNEL}
     *     <br>One of the channels has been deleted before the completion of the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild</li>
     * </ul>
     *
     * @return {@link ChannelOrderAction ChannelOrderAction} - Type: {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannel}
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public ChannelOrderAction<VoiceChannel> modifyVoiceChannelPositions()
    {
        return new ChannelOrderActionImpl<>(this, ChannelType.VOICE);
    }

    /**
     * Modifies the positional order of {@link net.dv8tion.jda.api.entities.Category#getTextChannels() Category#getTextChannels()}
     * using an extension of {@link ChannelOrderAction ChannelOrderAction}
     * specialized for ordering the nested {@link net.dv8tion.jda.api.entities.TextChannel TextChannels} of this
     * {@link net.dv8tion.jda.api.entities.Category Category}.
     * <br>Like {@code ChannelOrderAction}, the returned {@link net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction CategoryOrderAction}
     * can be used to move TextChannels {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveUp(int) up},
     * {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveDown(int) down}, or
     * {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveTo(int) to} a specific position.
     * <br>This uses <b>ascending</b> order with a 0 based index.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNNKOWN_CHANNEL}
     *     <br>One of the channels has been deleted before the completion of the task.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild.</li>
     * </ul>
     *
     * @param  category
     *         The {@link net.dv8tion.jda.api.entities.Category Category} to order
     *         {@link net.dv8tion.jda.api.entities.TextChannel TextChannels} from.
     *
     * @return {@link net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction CategoryOrderAction} - Type: {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public CategoryOrderAction<TextChannel> modifyTextChannelPositions(@Nonnull Category category)
    {
        Checks.notNull(category, "Category");
        checkGuild(category.getGuild(), "Category");
        return new CategoryOrderActionImpl<>(category, ChannelType.TEXT);
    }

    /**
     * Modifies the positional order of {@link net.dv8tion.jda.api.entities.Category#getVoiceChannels() Category#getVoiceChannels()}
     * using an extension of {@link ChannelOrderAction ChannelOrderAction}
     * specialized for ordering the nested {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels} of this
     * {@link net.dv8tion.jda.api.entities.Category Category}.
     * <br>Like {@code ChannelOrderAction}, the returned {@link CategoryOrderAction CategoryOrderAction}
     * can be used to move VoiceChannels {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveUp(int) up},
     * {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveDown(int) down}, or
     * {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveTo(int) to} a specific position.
     * <br>This uses <b>ascending</b> order with a 0 based index.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNNKOWN_CHANNEL}
     *     <br>One of the channels has been deleted before the completion of the task.</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild.</li>
     * </ul>
     *
     * @param  category
     *         The {@link net.dv8tion.jda.api.entities.Category Category} to order
     *         {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels} from.
     *
     * @return {@link CategoryOrderAction CategoryOrderAction} - Type: {@link net.dv8tion.jda.api.entities.VoiceChannel VoiceChannels}
     */
    @Nonnull
    @Override
    @CheckReturnValue
    public CategoryOrderAction<VoiceChannel> modifyVoiceChannelPositions(@Nonnull Category category)
    {
        Checks.notNull(category, "Category");
        checkGuild(category.getGuild(), "Category");
        return new CategoryOrderActionImpl<>(category, ChannelType.VOICE);
    }

    /**
     * Modifies the positional order of {@link net.dv8tion.jda.api.entities.Guild#getRoles() Guild.getRoles()}
     * using a specific {@link net.dv8tion.jda.api.requests.RestAction RestAction} extension to allow moving Roles
     * {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveUp(int) up}/{@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveDown(int) down}
     * or {@link net.dv8tion.jda.api.requests.restaction.order.OrderAction#moveTo(int) to} a specific position.
     *
     * <p>Possible {@link net.dv8tion.jda.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#UNKNOWN_ROLE UNKNOWN_ROLE}
     *     <br>One of the roles was deleted before the completion of the task</li>
     *
     *     <li>{@link net.dv8tion.jda.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account was removed from the Guild</li>
     * </ul>
     *
     * @param  useAscendingOrder
     *         Defines the ordering of the OrderAction. If {@code false}, the OrderAction will be in the ordering
     *         defined by Discord for roles, which is Descending. This means that the highest role appears at index {@code 0}
     *         and the lowest role at index {@code n - 1}. Providing {@code true} will result in the ordering being
     *         in ascending order, with the lower role at index {@code 0} and the highest at index {@code n - 1}.
     *         <br>As a note: {@link net.dv8tion.jda.api.entities.Member#getRoles() Member.getRoles()}
     *         and {@link net.dv8tion.jda.api.entities.Guild#getRoles() Guild.getRoles()} are both in descending order.
     *
     * @return {@link RoleOrderAction RoleOrderAction}
     */
    @Nonnull
    @Override
    @CheckReturnValue
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
            throw new InsufficientPermissionException(perm);
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

    public TLongObjectMap<JSONObject> getCachedPresenceMap()
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
            JSONArray array = response.getArray();
            List<Invite> invites = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++)
                invites.add(entityBuilder.createInvite(array.getJSONObject(i)));
            return Collections.unmodifiableList(invites);
        });
    }
}
