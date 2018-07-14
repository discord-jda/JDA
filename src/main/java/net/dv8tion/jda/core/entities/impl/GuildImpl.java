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

package net.dv8tion.jda.core.entities.impl;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.client.requests.restaction.pagination.MentionPaginationAction;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.managers.GuildManager;
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.restaction.MemberAction;
import net.dv8tion.jda.core.requests.restaction.pagination.AuditLogPaginationAction;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.Helpers;
import net.dv8tion.jda.core.utils.MiscUtil;
import net.dv8tion.jda.core.utils.cache.MemberCacheView;
import net.dv8tion.jda.core.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.core.utils.cache.impl.MemberCacheViewImpl;
import net.dv8tion.jda.core.utils.cache.impl.SnowflakeCacheViewImpl;
import net.dv8tion.jda.core.utils.cache.impl.SortedSnowflakeCacheView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class GuildImpl implements Guild
{
    private final long id;
    private final JDAImpl api;

    private final SortedSnowflakeCacheView<Category> categoryCache = new SortedSnowflakeCacheView<>(Category.class, Channel::getName, Comparator.naturalOrder());
    private final SortedSnowflakeCacheView<VoiceChannel> voiceChannelCache = new SortedSnowflakeCacheView<>(VoiceChannel.class, Channel::getName, Comparator.naturalOrder());
    private final SortedSnowflakeCacheView<TextChannel> textChannelCache = new SortedSnowflakeCacheView<>(TextChannel.class, Channel::getName, Comparator.naturalOrder());
    private final SortedSnowflakeCacheView<Role> roleCache = new SortedSnowflakeCacheView<>(Role.class, Role::getName, Comparator.reverseOrder());
    private final SnowflakeCacheViewImpl<Emote> emoteCache = new SnowflakeCacheViewImpl<>(Emote.class, Emote::getName);
    private final MemberCacheViewImpl memberCache = new MemberCacheViewImpl();

    private final TLongObjectMap<JSONObject> cachedPresences = MiscUtil.newLongMap();

    private final ReentrantLock mngLock = new ReentrantLock();
    private volatile GuildManager manager;
    private volatile GuildController controller;

    private Member owner;
    private String name;
    private String iconId;
    private String splashId;
    private String region;
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
        this.api = api;
    }

    @Override
    public RestAction<EnumSet<Region>> retrieveRegions(boolean includeDeprecated)
    {
        Route.CompiledRoute route = Route.Guilds.GET_VOICE_REGIONS.compile(getId());
        return new RestAction<EnumSet<Region>>(api, route)
        {
            @Override
            protected void handleResponse(Response response, Request<EnumSet<Region>> request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }
                EnumSet<Region> set = EnumSet.noneOf(Region.class);
                JSONArray arr = response.getArray();
                for (int i = 0; arr != null && i < arr.length(); i++)
                {
                    JSONObject obj = arr.getJSONObject(i);
                    if (!includeDeprecated && Helpers.optBoolean(obj, "deprecated"))
                        continue;
                    String id = obj.optString("id");
                    Region region = Region.fromKey(id);
                    if (region != Region.UNKNOWN)
                        set.add(region);
                }
                request.onSuccess(set);
            }
        };
    }

    @Override
    public MemberAction addMember(String accessToken, String userId)
    {
        Checks.notBlank(accessToken, "Access-Token");
        Checks.isSnowflake(userId, "User ID");
        Checks.check(getMemberById(userId) == null, "User is already in this guild");
        if (!getSelfMember().hasPermission(Permission.CREATE_INSTANT_INVITE))
            throw new InsufficientPermissionException(Permission.CREATE_INSTANT_INVITE);
        return new MemberAction(api, this, userId, accessToken);
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
        return splashId == null ? null : "https://cdn.discordapp.com/splashes/" + id + "/" + splashId + ".jpg";
    }

    @Override
    public RestAction<String> getVanityUrl()
    {
        if (!isAvailable())
            throw new GuildUnavailableException();
        if (!getSelfMember().hasPermission(Permission.MANAGE_SERVER))
            throw new InsufficientPermissionException(Permission.MANAGE_SERVER);
        if (!getFeatures().contains("VANITY_URL"))
            throw new IllegalStateException("This guild doesn't have a vanity url");

        Route.CompiledRoute route = Route.Guilds.GET_VANITY_URL.compile(getId());

        return new RestAction<String>(api, route)
        {
            @Override
            protected void handleResponse(Response response, Request<String> request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }

                request.onSuccess(response.getObject().getString("code"));
            }
        };
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
    public RestAction<List<Webhook>> getWebhooks()
    {
        if (!getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS))
            throw new InsufficientPermissionException(Permission.MANAGE_WEBHOOKS);

        Route.CompiledRoute route = Route.Guilds.GET_WEBHOOKS.compile(getId());

        return new RestAction<List<Webhook>>(api, route)
        {
            @Override
            protected void handleResponse(Response response, Request<List<Webhook>> request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }

                JSONArray array = response.getArray();
                List<Webhook> webhooks = new ArrayList<>(array.length());
                EntityBuilder builder = api.getEntityBuilder();

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

                request.onSuccess(Collections.unmodifiableList(webhooks));
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
    public String getRegionRaw()
    {
        return region;
    }

    @Override
    public boolean isMember(User user)
    {
        return memberCache.getMap().containsKey(user.getIdLong());
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
    public MemberCacheView getMemberCache()
    {
        return memberCache;
    }

    @Override
    public SnowflakeCacheView<Category> getCategoryCache()
    {
        return categoryCache;
    }

    @Override
    public SnowflakeCacheView<TextChannel> getTextChannelCache()
    {
        return textChannelCache;
    }

    @Override
    public SnowflakeCacheView<VoiceChannel> getVoiceChannelCache()
    {
        return voiceChannelCache;
    }

    @Override
    public SnowflakeCacheView<Role> getRoleCache()
    {
        return roleCache;
    }

    @Override
    public SnowflakeCacheView<Emote> getEmoteCache()
    {
        return emoteCache;
    }

    @Nonnull
    @Override
    public RestAction<List<Ban>> getBanList()
    {
        if (!isAvailable())
            throw new GuildUnavailableException();
        if (!getSelfMember().hasPermission(Permission.BAN_MEMBERS))
            throw new InsufficientPermissionException(Permission.BAN_MEMBERS);

        Route.CompiledRoute route = Route.Guilds.GET_BANS.compile(getId());
        return new RestAction<List<Ban>>(getJDA(), route)
        {
            @Override
            protected void handleResponse(Response response, Request<List<Ban>> request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }

                EntityBuilder builder = api.getEntityBuilder();
                List<Ban> bans = new LinkedList<>();
                JSONArray bannedArr = response.getArray();

                for (int i = 0; i < bannedArr.length(); i++)
                {
                    final JSONObject object = bannedArr.getJSONObject(i);
                    JSONObject user = object.getJSONObject("user");
                    bans.add(new Ban(builder.createFakeUser(user, false), object.optString("reason", null)));
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
            throw new InsufficientPermissionException(Permission.KICK_MEMBERS);

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

    @Nullable
    @Override
    public TextChannel getDefaultChannel()
    {
        final Role role = getPublicRole();
        return getTextChannelsMap().valueCollection().stream()
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
                    manager = new GuildManager(this);
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
    public MentionPaginationAction getRecentMentions()
    {
        AccountTypeException.check(getJDA().getAccountType(), AccountType.CLIENT);
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
        return new RestAction<Void>(api, route)
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
        if (!api.getSelfUser().isBot() && api.getSelfUser().isMfaEnabled())
            throw new IllegalStateException("Cannot delete a guild without providing MFA code. Use Guild#delete(String)");

        return delete(null);
    }

    @Override
    public RestAction<Void> delete(String mfaCode)
    {
        if (!owner.equals(getSelfMember()))
            throw new PermissionException("Cannot delete a guild that you do not own!");

        JSONObject mfaBody = null;
        if (!api.getSelfUser().isBot() && api.getSelfUser().isMfaEnabled())
        {
            Checks.notEmpty(mfaCode, "Provided MultiFactor Auth code");
            mfaBody = new JSONObject().put("code", mfaCode);
        }

        Route.CompiledRoute route = Route.Guilds.DELETE_GUILD.compile(getId());
        return new RestAction<Void>(api, route, mfaBody)
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
        if (!api.isAudioEnabled())
            throw new IllegalStateException("Audio is disabled. Cannot retrieve an AudioManager while audio is disabled.");

        final TLongObjectMap<AudioManager> managerMap = api.getAudioManagerMap();
        AudioManager mng = managerMap.get(id);
        if (mng == null)
        {
            // No previous manager found -> create one
            synchronized (managerMap)
            {
                mng = managerMap.get(id);
                if (mng == null)
                {
                    mng = new AudioManagerImpl(this);
                    managerMap.put(id, mng);
                }
            }
        }
        return mng;
    }

    @Override
    public JDAImpl getJDA()
    {
        return api;
    }

    @Override
    public List<GuildVoiceState> getVoiceStates()
    {
        return Collections.unmodifiableList(
                getMembersMap().valueCollection().stream().map(Member::getVoiceState).collect(Collectors.toList()));
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
        if (api.getAccountType() == AccountType.BOT)
            return true;
        if(canSendVerification)
            return true;

        if (api.getSelfUser().getPhoneNumber() != null)
            return canSendVerification = true;

        switch (verificationLevel)
        {
            case VERY_HIGH:
                break; // we already checked for a verified phone number
            case HIGH:
                if (ChronoUnit.MINUTES.between(getSelfMember().getJoinDate(), OffsetDateTime.now()) < 10)
                    break;
            case MEDIUM:
                if (ChronoUnit.MINUTES.between(MiscUtil.getCreationTime(api.getSelfUser()), OffsetDateTime.now()) < 5)
                    break;
            case LOW:
                if (!api.getSelfUser().isVerified())
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

    // -- Map getters --

    public TLongObjectMap<Category> getCategoriesMap()
    {
        return categoryCache.getMap();
    }

    public TLongObjectMap<TextChannel> getTextChannelsMap()
    {
        return textChannelCache.getMap();
    }

    public TLongObjectMap<VoiceChannel> getVoiceChannelsMap()
    {
        return voiceChannelCache.getMap();
    }

    public TLongObjectMap<Member> getMembersMap()
    {
        return memberCache.getMap();
    }

    public TLongObjectMap<Role> getRolesMap()
    {
        return roleCache.getMap();
    }

    public TLongObjectMap<Emote> getEmoteMap()
    {
        return emoteCache.getMap();
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
        if (!this.getSelfMember().hasPermission(Permission.MANAGE_SERVER))
            throw new InsufficientPermissionException(Permission.MANAGE_SERVER);

        final Route.CompiledRoute route = Route.Invites.GET_GUILD_INVITES.compile(getId());

        return new RestAction<List<Invite>>(api, route)
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
                        invites.add(entityBuilder.createInvite(array.getJSONObject(i)));
                    request.onSuccess(Collections.unmodifiableList(invites));
                }
                else
                {
                    request.onFailure(response);
                }
            }
        };
    }

}
