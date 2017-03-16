/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

import net.dv8tion.jda.client.requests.restaction.pagination.MentionPaginationAction;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
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
import net.dv8tion.jda.core.utils.MiscUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Args;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class GuildImpl implements Guild
{
    private final String id;
    private final JDAImpl api;
    private final HashMap<String, TextChannel> textChannels = new HashMap<>();
    private final HashMap<String, VoiceChannel> voiceChannels = new HashMap<>();
    private final HashMap<String, Member> members = new HashMap<>();
    private final HashMap<String, Role> roles = new HashMap<>();
    private final HashMap<String, Emote> emotes = new HashMap<>();

    private final HashMap<String, JSONObject> cachedPresences = new HashMap<>();

    private volatile GuildManager manager;
    private volatile GuildManagerUpdatable managerUpdatable;
    private volatile GuildController controller;
    private Object mngLock = new Object();

    private Member owner;
    private String name;
    private String iconId;
    private String splashId;
    private Region region;
    private TextChannel publicChannel;
    private VoiceChannel afkChannel;
    private Role publicRole;
    private VerificationLevel verificationLevel;
    private NotificationLevel defaultNotificationLevel;
    private MFALevel mfaLevel;
    private Timeout afkTimeout;
    private boolean available;
    private boolean canSendVerification = false;

    public GuildImpl(JDAImpl api, String id)
    {
        this.id = id;
        this.api = api;
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

        Route.CompiledRoute route = Route.Guilds.GET_WEBHOOKS.compile(id);

        return new RestAction<List<Webhook>>(api, route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
            {
                if (!response.isOk())
                {
                    request.onFailure(response);
                    return;
                }

                List<Webhook> webhooks = new LinkedList<>();
                JSONArray array = response.getArray();
                EntityBuilder builder = EntityBuilder.get(getJDA());

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
        return region;
    }

    @Override
    public boolean isMember(User user)
    {
        return members.containsKey(user.getId());
    }

    @Override
    public Member getSelfMember()
    {
        return getMember(getJDA().getSelfUser());
    }

    @Override
    public Member getMember(User user)
    {
        return getMemberById(user.getId());
    }

    @Override
    public Member getMemberById(String userId)
    {
        return members.get(userId);
    }

    @Override
    public List<Member> getMembers()
    {
        return Collections.unmodifiableList(new ArrayList<>(members.values()));
    }

    @Override
    public List<Member> getMembersByName(String name, boolean ignoreCase)
    {
        Args.notNull(name, "name");
        return Collections.unmodifiableList(members.values().stream()
                .filter(m ->
                    ignoreCase
                    ? name.equalsIgnoreCase(m.getUser().getName())
                    : name.equals(m.getUser().getName()))
                .collect(Collectors.toList()));
    }

    @Override
    public List<Member> getMembersByNickname(String nickname, boolean ignoreCase)
    {
        Args.notNull(nickname, "nickname");
        return Collections.unmodifiableList(members.values().stream()
                .filter(m ->
                    ignoreCase
                    ? nickname.equalsIgnoreCase(m.getNickname())
                    : nickname.equals(m.getNickname()))
                .collect(Collectors.toList()));
    }

    @Override
    public List<Member> getMembersByEffectiveName(String name, boolean ignoreCase)
    {
        Args.notNull(name, "name");
        return Collections.unmodifiableList(members.values().stream()
                .filter(m ->
                    ignoreCase
                    ? name.equalsIgnoreCase(m.getEffectiveName())
                    : name.equals(m.getEffectiveName()))
                .collect(Collectors.toList()));
    }

    @Override
    public List<Member> getMembersWithRoles(Role... roles)
    {
        Args.notNull(roles, "roles");
        return getMembersWithRoles(Arrays.asList(roles));
    }

    @Override
    public List<Member> getMembersWithRoles(Collection<Role> roles)
    {
        Args.notNull(roles, "roles");
        for (Role r : roles)
        {
            Args.notNull(r, "Role provided in collection");
            if (!r.getGuild().equals(this))
                throw new IllegalArgumentException("Role provided was from a different Guild! Role: " + r);
        }

        return Collections.unmodifiableList(members.values().stream()
                        .filter(m -> m.getRoles().containsAll(roles))
                        .collect(Collectors.toList()));
    }

    @Override
    public TextChannel getTextChannelById(String id)
    {
        return textChannels.get(id);
    }

    @Override
    public List<TextChannel> getTextChannelsByName(String name, boolean ignoreCase)
    {
        Args.notNull(name, "name");
        return Collections.unmodifiableList(textChannels.values().stream()
                .filter(tc ->
                    ignoreCase
                    ? name.equalsIgnoreCase(tc.getName())
                    : name.equals(tc.getName()))
                .collect(Collectors.toList()));
    }

    @Override
    public List<TextChannel> getTextChannels()
    {
        ArrayList<TextChannel> channels = new ArrayList<>(textChannels.values());
        Collections.sort(channels, (c1, c2) -> c2.compareTo(c1));
        return Collections.unmodifiableList(channels);
    }

    @Override
    public VoiceChannel getVoiceChannelById(String id)
    {
        return voiceChannels.get(id);
    }

    @Override
    public List<VoiceChannel> getVoiceChannelsByName(String name, boolean ignoreCase)
    {
        Args.notNull(name, "name");
        return Collections.unmodifiableList(voiceChannels.values().stream()
            .filter(vc ->
                    ignoreCase
                    ? name.equalsIgnoreCase(vc.getName())
                    : name.equals(vc.getName()))
            .collect(Collectors.toList()));
    }

    @Override
    public List<VoiceChannel> getVoiceChannels()
    {
        List<VoiceChannel> channels = new ArrayList<>(voiceChannels.values());
        Collections.sort(channels, (v1, v2) -> v2.compareTo(v1));
        return Collections.unmodifiableList(channels);
    }

    @Override
    public Role getRoleById(String id)
    {
        return roles.get(id);
    }

    @Override
    public List<Role> getRoles()
    {
        List<Role> list = new ArrayList<>(roles.values());
        Collections.sort(list, (r1, r2) -> r2.compareTo(r1));
        return Collections.unmodifiableList(list);
    }

    @Override
    public List<Role> getRolesByName(String name, boolean ignoreCase)
    {
        Args.notNull(name, "name");
        return Collections.unmodifiableList(roles.values().stream()
                .filter(r ->
                        ignoreCase
                        ? name.equalsIgnoreCase(r.getName())
                        : name.equals(r.getName()))
                .collect(Collectors.toList()));
    }

    @Override
    public Emote getEmoteById(String id)
    {
        return emotes.get(id);
    }

    @Override
    public List<Emote> getEmotes()
    {
        return Collections.unmodifiableList(new LinkedList<>(emotes.values()));
    }

    @Override
    public List<Emote> getEmotesByName(String name, boolean ignoreCase)
    {
        Args.notNull(name, "name");
        return Collections.unmodifiableList(emotes.values().parallelStream()
                .filter(e ->
                        ignoreCase
                        ? StringUtils.equalsIgnoreCase(e.getName(), name)
                        : StringUtils.equals(e.getName(), name))
                .collect(Collectors.toList()));
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
    public RestAction<Void> leave()
    {
        if (owner.equals(getSelfMember()))
            throw new IllegalStateException("Cannot leave a guild that you are the owner of! Transfer guild ownership first!");

        Route.CompiledRoute route = Route.Self.LEAVE_GUILD.compile(id);
        return new RestAction<Void>(api, route, null)
        {
            @Override
            protected void handleResponse(Response response, Request request)
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
        if (api.getSelfUser().isMfaEnabled())
            throw new IllegalStateException("Cannot delete a guild without providing MFA code. Use Guild#delete(String)");

        return delete(null);
    }

    @Override
    public RestAction<Void> delete(String mfaCode)
    {
        if (!owner.equals(getSelfMember()))
            throw new PermissionException("Cannot delete a guild that you do not own!");

        JSONObject mfaBody = null;
        if (api.getSelfUser().isMfaEnabled())
        {
            Args.notEmpty(mfaCode, "Provided MultiFactor Auth code");
            mfaBody = new JSONObject().put("code", mfaCode);
        }

        Route.CompiledRoute route = Route.Guilds.DELETE_GUILD.compile(id);
        return new RestAction<Void>(api, route, mfaBody)
        {
            @Override
            protected void handleResponse(Response response, Request request)
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

        HashMap<String, AudioManager> audioManagers = ((JDAImpl) api).getAudioManagerMap();
        AudioManager mng = audioManagers.get(id);
        if (mng == null)
        {
            synchronized (audioManagers)
            {
                mng = audioManagers.get(id);
                if (mng == null)
                {
                    mng = new AudioManagerImpl(this);
                    audioManagers.put(id, mng);
                }
            }
        }
        return mng;
    }

    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public List<GuildVoiceState> getVoiceStates()
    {
        return Collections.unmodifiableList(
                members.values().stream().map(Member::getVoiceState).collect(Collectors.toList()));
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
    public boolean checkVerification()
    {
        if (api.getAccountType() == AccountType.BOT)
            return true;
        if(canSendVerification)
            return true;
        switch (verificationLevel)
        {
            case HIGH:
                if(ChronoUnit.MINUTES.between(getSelfMember().getJoinDate(), OffsetDateTime.now()) < 10)
                    break;
            case MEDIUM:
                if(ChronoUnit.MINUTES.between(MiscUtil.getCreationTime(api.getSelfUser()), OffsetDateTime.now()) < 5)
                    break;
            case LOW:
                if(!api.getSelfUser().isVerified())
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
    public String getId()
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

    public GuildImpl setRegion(Region region)
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

    public GuildImpl setAfkTimeout(Timeout afkTimeout)
    {
        this.afkTimeout = afkTimeout;
        return this;
    }

    // -- Map getters --

    public HashMap<String, TextChannel> getTextChannelsMap()
    {
        return textChannels;
    }

    public HashMap<String, VoiceChannel> getVoiceChannelMap()
    {
        return voiceChannels;
    }

    public HashMap<String, Member> getMembersMap()
    {
        return members;
    }

    public HashMap<String, Role> getRolesMap()
    {
        return roles;
    }

    public HashMap<String, JSONObject> getCachedPresenceMap()
    {
        return cachedPresences;
    }

    public HashMap<String, Emote> getEmoteMap()
    {
        return emotes;
    }


    // -- Object overrides --

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Guild))
            return false;
        Guild oGuild = (Guild) o;
        return this == oGuild || this.getId().equals(oGuild.getId());
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }

    @Override
    public String toString()
    {
        return "G:" + getName() + '(' + getId() + ')';
    }

    @Override
    public RestAction<List<Invite>> getInvites()
    {
        if (!this.getSelfMember().hasPermission(Permission.MANAGE_SERVER))
            throw new PermissionException(Permission.MANAGE_SERVER);

        final Route.CompiledRoute route = Route.Invites.GET_GUILD_INVITES.compile(getId());

        return new RestAction<List<Invite>>(api, route, null)
        {
            @Override
            protected void handleResponse(final Response response, final Request request)
            {
                if (response.isOk())
                {
                    EntityBuilder entityBuilder = EntityBuilder.get(this.api);
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

}
