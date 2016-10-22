/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.managers.GuildManager;
import net.dv8tion.jda.core.managers.GuildManagerUpdatable;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.utils.MiscUtil;
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
    private int afkTimeout;
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
    public Member getOwner()
    {
        return owner;
    }

    @Override
    public int getAfkTimeout()
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
        return getMember(getJDA().getSelfInfo());
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
        return getMembersWithRoles(Arrays.asList(roles));
    }

    @Override
    public List<Member> getMembersWithRoles(Collection<Role> roles)
    {
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
        if (!owner.equals(getSelfMember()))
            throw new PermissionException("Cannot delete a guild that you do not own!");

        Route.CompiledRoute route = Route.Guilds.DELETE_GUILD.compile(id);
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
    public JDA getJDA()
    {
        return api;
    }

    @Override
    public List<VoiceState> getVoiceStates()
    {
        return Collections.unmodifiableList(
                members.values().stream().<VoiceState>map(Member::getVoiceState).collect(Collectors.toList()));
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
                if(ChronoUnit.MINUTES.between(MiscUtil.getCreationTime(api.getSelfInfo()), OffsetDateTime.now()) < 5)
                    break;
            case LOW:
                if(!api.getSelfInfo().isVerified())
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

    public GuildImpl setAfkTimeout(int afkTimeout)
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
}
