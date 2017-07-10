/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.entities.impl;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.client.requests.restaction.pagination.MentionPaginationAction;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.managers.GuildManager;
import net.dv8tion.jda.core.managers.GuildManagerUpdatable;
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.pagination.AuditLogPaginationAction;
import net.dv8tion.jda.core.utils.MiscUtil;
import org.apache.commons.lang3.StringUtils;
import net.dv8tion.jda.core.utils.Checks;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class GuildImpl implements Guild, Disposable
{
    private final long id;
    private final WeakReference<JDAImpl> apiRef; // using WeakReference to properly dispose
    private final TLongObjectMap<TextChannelImpl> textChannels = MiscUtil.newLongMap();
    private final TLongObjectMap<VoiceChannelImpl> voiceChannels = MiscUtil.newLongMap();
    private final TLongObjectMap<MemberImpl> members = MiscUtil.newLongMap();
    private final TLongObjectMap<RoleImpl> roles = MiscUtil.newLongMap();
    private final TLongObjectMap<EmoteImpl> emotes = MiscUtil.newLongMap();

    private final TLongObjectMap<JSONObject> cachedPresences = MiscUtil.newLongMap();

    private final Object mngLock = new Object();
    private volatile GuildManager manager;
    private volatile GuildManagerUpdatable managerUpdatable;
    private volatile GuildController controller;

    private Member owner;
    private String name;
    private String iconId;
    private String splashId;
    private String region;
    private TextChannel publicChannel;
    private VoiceChannel afkChannel;
    private RoleImpl publicRole;
    private VerificationLevel verificationLevel;
    private NotificationLevel defaultNotificationLevel;
    private MFALevel mfaLevel;
    private ExplicitContentLevel explicitContentLevel;
    private Timeout afkTimeout;
    private boolean available;
    private boolean canSendVerification = false;
    private boolean disposed = false;

    public GuildImpl(JDAImpl api, long id)
    {
        this.id = id;
        this.apiRef = new WeakReference<>(api);
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
        return iconId == null ? null : "https://cdn.discordapp.com/icons/" + id + "/" + iconId + ".jpg";
    }

    @Override
    public String getSplashId()
    {
        return splashId;
    }

    @Override
    public String getSplashUrl()
    {
        return splashId == null ? null : "https://cdn.discordapp.com/splashes/" + id + "/" + splashId + ".jpg";
    }

    @Override
    public VoiceChannel getAfkChannel()
    {
        return afkChannel;
    }

    @Override
    public RestAction<List<Webhook>> getWebhooks()
    {
        if (!getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS))
            throw new PermissionException(Permission.MANAGE_WEBHOOKS);

        Route.CompiledRoute route = Route.Guilds.GET_WEBHOOKS.compile(getId());

        return new RestAction<List<Webhook>>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<List<Webhook>> request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }

                List<Webhook> webhooks = new LinkedList<>();
                JSONArray array = response.getArray();
                EntityBuilder builder = api.getEntityBuilder();

                for (Object object : array)
                {
                    try
                    {
                        webhooks.add(builder.createWebhook((JSONObject) object));
                    }
                    catch (JSONException | NullPointerException e)
                    {
                        JDAImpl.LOG.log(e);
                    }
                }

                request.onSuccess(webhooks);
            }
        };
    }

    @Override
    public Member getOwner()
    {
        return owner;
    }

    @Override
    public Timeout getAfkTimeout()
    {
        return afkTimeout;
    }

    @Override
    public Region getRegion()
    {
        return Region.fromKey(region);
    }

    @Override
    public String getRegionRaw()
    {
        return region;
    }

    @Override
    public boolean isMember(User user)
    {
        checkDisposed();
        return members.containsKey(user.getIdLong());
    }

    @Override
    public Member getSelfMember()
    {
        return getMember(getJDA().getSelfUser());
    }

    @Override
    public Member getMember(User user)
    {
        return getMemberById(user.getIdLong());
    }

    @Override
    public Member getMemberById(String userId)
    {
        return getMemberById(MiscUtil.parseSnowflake(userId));
    }

    @Override
    public Member getMemberById(long userId)
    {
        checkDisposed();
        return members.get(userId);
    }

    @Override
    public List<Member> getMembers()
    {
        checkDisposed();
        return Collections.unmodifiableList(new ArrayList<>(members.valueCollection()));
    }

    @Override
    public List<Member> getMembersByName(String name, boolean ignoreCase)
    {
        checkDisposed();
        Checks.notNull(name, "name");
        return Collections.unmodifiableList(members.valueCollection().stream()
                .filter(m ->
                    ignoreCase
                    ? name.equalsIgnoreCase(m.getUser().getName())
                    : name.equals(m.getUser().getName()))
                .collect(Collectors.toList()));
    }

    @Override
    public List<Member> getMembersByNickname(String nickname, boolean ignoreCase)
    {
        checkDisposed();
        Checks.notNull(nickname, "nickname");
        return Collections.unmodifiableList(members.valueCollection().stream()
                .filter(m ->
                    ignoreCase
                    ? nickname.equalsIgnoreCase(m.getNickname())
                    : nickname.equals(m.getNickname()))
                .collect(Collectors.toList()));
    }

    @Override
    public List<Member> getMembersByEffectiveName(String name, boolean ignoreCase)
    {
        checkDisposed();
        Checks.notNull(name, "name");
        return Collections.unmodifiableList(members.valueCollection().stream()
                .filter(m ->
                    ignoreCase
                    ? name.equalsIgnoreCase(m.getEffectiveName())
                    : name.equals(m.getEffectiveName()))
                .collect(Collectors.toList()));
    }

    @Override
    public List<Member> getMembersWithRoles(Role... roles)
    {
        Checks.notNull(roles, "roles");
        return getMembersWithRoles(Arrays.asList(roles));
    }

    @Override
    public List<Member> getMembersWithRoles(Collection<Role> roles)
    {
        checkDisposed();
        Checks.notNull(roles, "roles");
        for (Role r : roles)
        {
            Checks.notNull(r, "Role provided in collection");
            if (!r.getGuild().equals(this))
                throw new IllegalArgumentException("Role provided was from a different Guild! Role: " + r);
        }

        return Collections.unmodifiableList(members.valueCollection().stream()
                        .filter(m -> m.getRoles().containsAll(roles))
                        .collect(Collectors.toList()));
    }

    @Override
    public TextChannel getTextChannelById(String id)
    {
        return getTextChannelById(MiscUtil.parseSnowflake(id));
    }

    @Override
    public TextChannel getTextChannelById(long id)
    {
        checkDisposed();
        return textChannels.get(id);
    }

    @Override
    public List<TextChannel> getTextChannelsByName(String name, boolean ignoreCase)
    {
        checkDisposed();
        Checks.notNull(name, "name");
        return Collections.unmodifiableList(textChannels.valueCollection().stream()
                .filter(tc ->
                    ignoreCase
                    ? name.equalsIgnoreCase(tc.getName())
                    : name.equals(tc.getName()))
                .collect(Collectors.toList()));
    }

    @Override
    public List<TextChannel> getTextChannels()
    {
        checkDisposed();
        ArrayList<TextChannel> channels = new ArrayList<>(textChannels.valueCollection());
        channels.sort(Comparator.reverseOrder());
        return Collections.unmodifiableList(channels);
    }

    @Override
    public VoiceChannel getVoiceChannelById(String id)
    {
        return getVoiceChannelById(MiscUtil.parseSnowflake(id));
    }

    @Override
    public VoiceChannel getVoiceChannelById(long id)
    {
        checkDisposed();
        return voiceChannels.get(id);
    }

    @Override
    public List<VoiceChannel> getVoiceChannelsByName(String name, boolean ignoreCase)
    {
        checkDisposed();
        Checks.notNull(name, "name");
        return Collections.unmodifiableList(voiceChannels.valueCollection().stream()
            .filter(vc ->
                    ignoreCase
                    ? name.equalsIgnoreCase(vc.getName())
                    : name.equals(vc.getName()))
            .collect(Collectors.toList()));
    }

    @Override
    public List<VoiceChannel> getVoiceChannels()
    {
        checkDisposed();
        List<VoiceChannel> channels = new ArrayList<>(voiceChannels.valueCollection());
        channels.sort(Comparator.reverseOrder());
        return Collections.unmodifiableList(channels);
    }

    @Override
    public Role getRoleById(String id)
    {
        return getRoleById(MiscUtil.parseSnowflake(id));
    }

    @Override
    public Role getRoleById(long id)
    {
        checkDisposed();
        return roles.get(id);
    }

    @Override
    public List<Role> getRoles()
    {
        checkDisposed();
        List<Role> list = new ArrayList<>(roles.valueCollection());
        list.sort(Comparator.reverseOrder());
        return Collections.unmodifiableList(list);
    }

    @Override
    public List<Role> getRolesByName(String name, boolean ignoreCase)
    {
        checkDisposed();
        Checks.notNull(name, "name");
        return Collections.unmodifiableList(roles.valueCollection().stream()
                .filter(r ->
                        ignoreCase
                        ? name.equalsIgnoreCase(r.getName())
                        : name.equals(r.getName()))
                .collect(Collectors.toList()));
    }

    @Override
    public Emote getEmoteById(String id)
    {
        return getEmoteById(MiscUtil.parseSnowflake(id));
    }

    @Override
    public Emote getEmoteById(long id)
    {
        checkDisposed();
        return emotes.get(id);
    }

    @Override
    public List<Emote> getEmotes()
    {
        checkDisposed();
        return Collections.unmodifiableList(new LinkedList<>(emotes.valueCollection()));
    }

    @Override
    public List<Emote> getEmotesByName(String name, boolean ignoreCase)
    {
        checkDisposed();
        Checks.notNull(name, "name");
        return Collections.unmodifiableList(emotes.valueCollection().parallelStream()
                .filter(e ->
                        ignoreCase
                        ? StringUtils.equalsIgnoreCase(e.getName(), name)
                        : StringUtils.equals(e.getName(), name))
                .collect(Collectors.toList()));
    }

    @Override
    public RestAction<List<User>> getBans()
    {
        if (!isAvailable())
            throw new GuildUnavailableException();
        if (!getSelfMember().hasPermission(Permission.BAN_MEMBERS))
            throw new PermissionException(Permission.BAN_MEMBERS);

        Route.CompiledRoute route = Route.Guilds.GET_BANS.compile(getId());
        return new RestAction<List<User>>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<List<User>> request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }

                EntityBuilder builder = api.getEntityBuilder();
                List<User> bans = new LinkedList<>();
                JSONArray bannedArr = response.getArray();

                for (int i = 0; i < bannedArr.length(); i++)
                {
                    JSONObject user = bannedArr.getJSONObject(i).getJSONObject("user");
                    bans.add(builder.createFakeUser(user, false));
                }
                request.onSuccess(Collections.unmodifiableList(bans));
            }
        };
    }

    @Override
    public RestAction<Integer> getPrunableMemberCount(int days)
    {
        if (!isAvailable())
            throw new GuildUnavailableException();
        if (!getSelfMember().hasPermission(Permission.KICK_MEMBERS))
            throw new PermissionException(Permission.KICK_MEMBERS);

        if (days < 1)
            throw new IllegalArgumentException("Days amount must be at minimum 1 day.");

        Route.CompiledRoute route = Route.Guilds.PRUNABLE_COUNT.compile(getId()).withQueryParams("days", Integer.toString(days));
        return new RestAction<Integer>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Integer> request)
            {
                if (response.isOk())
                    request.onSuccess(response.getObject().getInt("pruned"));
                else
                    request .onFailure(response);
            }
        };
    }

    @Override
    public Role getPublicRole()
    {
        return publicRole;
    }

    @Override
    public TextChannel getPublicChannel()
    {
        return publicChannel;
    }

    @Override
    public GuildManager getManager()
    {
        GuildManager mng = manager;
        if (mng == null)
        {
            synchronized (mngLock)
            {
                checkDisposed();
                mng = manager;
                if (mng == null)
                    mng = manager = new GuildManager(this);
            }
        }
        return mng;
    }

    @Override
    public GuildManagerUpdatable getManagerUpdatable()
    {
        GuildManagerUpdatable mng = managerUpdatable;
        if (mng == null)
        {
            synchronized (mngLock)
            {
                checkDisposed();
                mng = managerUpdatable;
                if (mng == null)
                    mng = managerUpdatable = new GuildManagerUpdatable(this);
            }
        }
        return mng;
    }

    @Override
    public GuildController getController()
    {
        GuildController ctrl = controller;
        if (ctrl == null)
        {
            synchronized (mngLock)
            {
                checkDisposed();
                ctrl = controller;
                if (ctrl == null)
                    ctrl = controller = new GuildController(this);
            }
        }
        return ctrl;
    }

    @Override
    public MentionPaginationAction getRecentMentions()
    {
        if (getJDA().getAccountType() != AccountType.CLIENT)
            throw new AccountTypeException(AccountType.CLIENT);
        return getJDA().asClient().getRecentMentions(this);
    }

    @Override
    public AuditLogPaginationAction getAuditLogs()
    {
        return new AuditLogPaginationAction(this);
    }

    @Override
    public RestAction<Void> leave()
    {
        if (owner.equals(getSelfMember()))
            throw new IllegalStateException("Cannot leave a guild that you are the owner of! Transfer guild ownership first!");

        Route.CompiledRoute route = Route.Self.LEAVE_GUILD.compile(getId());
        return new RestAction<Void>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public RestAction<Void> delete()
    {
        if (getJDA().getSelfUser().isMfaEnabled())
            throw new IllegalStateException("Cannot delete a guild without providing MFA code. Use Guild#delete(String)");

        return delete(null);
    }

    @Override
    public RestAction<Void> delete(String mfaCode)
    {
        if (!owner.equals(getSelfMember()))
            throw new PermissionException("Cannot delete a guild that you do not own!");

        JSONObject mfaBody = null;
        if (getJDA().getSelfUser().isMfaEnabled())
        {
            Checks.notEmpty(mfaCode, "Provided MultiFactor Auth code");
            mfaBody = new JSONObject().put("code", mfaCode);
        }

        Route.CompiledRoute route = Route.Guilds.DELETE_GUILD.compile(getId());
        return new RestAction<Void>(getJDA(), route, mfaBody)
        {
            @Override
            protected void handleResponse(Response response, Request<Void> request)
            {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        };
    }

    @Override
    public AudioManager getAudioManager()
    {
        if (!getJDA().isAudioEnabled())
            throw new IllegalStateException("Audio is disabled. Cannot retrieve an AudioManager while audio is disabled.");

        final TLongObjectMap<AudioManagerImpl> managerMap = getJDA().getAudioManagerMap();
        AudioManagerImpl mng = managerMap.get(id);
        if (mng == null)
        {
            // No previous manager found -> create one
            synchronized (managerMap)
            {
                checkDisposed();
                mng = managerMap.get(id);
                if (mng == null)
                {
                    mng = new AudioManagerImpl(this);
                    managerMap.put(id, mng);
                }
            }
        }
        // set guild again to make sure the manager references this instance! Avoiding invalid member cache
        mng.setGuild(this);
        return mng;
    }

    @Override
    public JDAImpl getJDA()
    {
        checkDisposed();
        return apiRef.get();
    }

    @Override
    public List<GuildVoiceState> getVoiceStates()
    {
        checkDisposed();
        return Collections.unmodifiableList(
                members.valueCollection().stream().map(Member::getVoiceState).collect(Collectors.toList()));
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
        switch (verificationLevel)
        {
            case HIGH:
                if(ChronoUnit.MINUTES.between(getSelfMember().getJoinDate(), OffsetDateTime.now()) < 10)
                    break;
            case MEDIUM:
                if(ChronoUnit.MINUTES.between(MiscUtil.getCreationTime(getJDA().getSelfUser()), OffsetDateTime.now()) < 5)
                    break;
            case LOW:
                if(!getJDA().getSelfUser().isVerified())
                    break;
            case NONE:
                canSendVerification = true;
                return true;
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

    public GuildImpl setPublicChannel(TextChannel publicChannel)
    {
        this.publicChannel = publicChannel;
        return this;
    }

    public GuildImpl setAfkChannel(VoiceChannel afkChannel)
    {
        this.afkChannel = afkChannel;
        return this;
    }

    public GuildImpl setPublicRole(RoleImpl publicRole)
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

    // -- Map getters --

    public TLongObjectMap<TextChannelImpl> getTextChannelMap()
    {
        return textChannels;
    }

    public TLongObjectMap<VoiceChannelImpl> getVoiceChannelMap()
    {
        return voiceChannels;
    }

    public TLongObjectMap<MemberImpl> getMembersMap()
    {
        return members;
    }

    public TLongObjectMap<RoleImpl> getRolesMap()
    {
        return roles;
    }

    public TLongObjectMap<EmoteImpl> getEmoteMap()
    {
        return emotes;
    }

    public TLongObjectMap<JSONObject> getCachedPresenceMap()
    {
        return cachedPresences;
    }


    // -- Object overrides --

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof GuildImpl))
            return false;
        GuildImpl oGuild = (GuildImpl) o;
        return this == oGuild || this.id == oGuild.id;
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
    public RestAction<List<Invite>> getInvites()
    {
        if (!getSelfMember().hasPermission(Permission.MANAGE_SERVER))
            throw new PermissionException(Permission.MANAGE_SERVER);

        final Route.CompiledRoute route = Route.Invites.GET_GUILD_INVITES.compile(getId());

        return new RestAction<List<Invite>>(getJDA(), route)
        {
            @Override
            protected void handleResponse(final Response response, final Request<List<Invite>> request)
            {
                if (response.isOk())
                {
                    EntityBuilder entityBuilder = this.api.getEntityBuilder();
                    JSONArray array = response.getArray();
                    List<Invite> invites = new ArrayList<>(array.length());
                    for (int i = 0; i < array.length(); i++)
                    {
                        invites.add(entityBuilder.createInvite(array.getJSONObject(i)));
                    }
                    request.onSuccess(invites);
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

    @Override
    public boolean dispose()
    {
        textChannels.forEachValue(Disposable::dispose);
        voiceChannels.forEachValue(Disposable::dispose);
        members.forEachValue(Disposable::dispose);
        roles.forEachValue(Disposable::dispose);
        emotes.forEachValue(Disposable::dispose);
        if (publicRole != null) // just in case
            publicRole.dispose();
        synchronized (getJDA().getAudioManagerMap())
        {
            AudioManagerImpl audioManager = getJDA().getAudioManagerMap().remove(id);
            if (audioManager != null)
                audioManager.dispose();
        }

        textChannels.clear();
        voiceChannels.clear();
        members.clear();
        roles.clear();
        emotes.clear();
        synchronized (mngLock)
        {
            manager = null;
            managerUpdatable = null;
            controller = null;
            return disposed = true;
        }
    }

    @Override
    public boolean isDisposed()
    {
        return disposed;
    }
}
