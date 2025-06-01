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
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Widget;
import net.dv8tion.jda.api.utils.ImageProxy;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WidgetImpl implements Widget
{
    private final boolean isAvailable;
    private final long id;
    private final String name;
    private final String invite;
    private final TLongObjectMap<VoiceChannelImpl> channels;
    private final TLongObjectMap<Member> members;

    /**
     * Constructs an unavailable Widget
     */
    public WidgetImpl(long guildId)
    {
        isAvailable = false;
        id = guildId;
        name = null;
        invite = null;
        channels = new TLongObjectHashMap<>();
        members = new TLongObjectHashMap<>();
    }

    /**
     * Constructs an available Widget
     *
     * @param json
     *        The {@link net.dv8tion.jda.api.utils.data.DataObject DataObject} to construct the Widget from
     */
    public WidgetImpl(@Nonnull DataObject json)
    {
        String inviteCode = json.getString("instant_invite", null);
        if (inviteCode != null)
            inviteCode = inviteCode.substring(inviteCode.lastIndexOf("/") + 1);

        isAvailable = true;
        id = json.getLong("id");
        name = json.getString("name");
        invite = inviteCode;
        channels = MiscUtil.newLongMap();
        members = MiscUtil.newLongMap();

        DataArray channelsJson = json.getArray("channels");
        for (int i = 0; i < channelsJson.length(); i++)
        {
            DataObject channel = channelsJson.getObject(i);
            channels.put(channel.getLong("id"), new VoiceChannelImpl(channel, this));
        }

        DataArray membersJson = json.getArray("members");
        for (int i = 0; i<membersJson.length(); i++)
        {
            DataObject memberJson = membersJson.getObject(i);
            MemberImpl member = new MemberImpl(memberJson, this);
            if (!memberJson.isNull("channel_id")) // voice state
            {
                VoiceChannelImpl channel = channels.get(memberJson.getLong("channel_id"));
                member.setVoiceState(new VoiceStateImpl(channel, 
                        memberJson.getBoolean("mute"), 
                        memberJson.getBoolean("deaf"), 
                        memberJson.getBoolean("suppress"), 
                        memberJson.getBoolean("self_mute"), 
                        memberJson.getBoolean("self_deaf"),
                        member,
                        this));
                channel.addMember(member);
            }
            members.put(member.getIdLong(), member);
        }
    }

    @Override
    public boolean isAvailable()
    {
        return isAvailable;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    @Nonnull
    public String getName()
    {
        checkAvailable();

        return name;
    }

    @Override
    @Nullable
    public String getInviteCode()
    {
        checkAvailable();

        return invite;
    }

    @Override
    @Nonnull
    public List<VoiceChannel> getVoiceChannels()
    {
        checkAvailable();

        return Collections.unmodifiableList(new ArrayList<>(channels.valueCollection()));
    }

    @Override
    @Nullable
    public VoiceChannel getVoiceChannelById(@Nonnull String id)
    {
        checkAvailable();

        return channels.get(MiscUtil.parseSnowflake(id));
    }

    @Override
    @Nullable
    public VoiceChannel getVoiceChannelById(long id)
    {
        checkAvailable();

        return channels.get(id);
    }

    @Override
    @Nonnull
    public List<Member> getMembers()
    {
        checkAvailable();

        return Collections.unmodifiableList(new ArrayList<>(members.valueCollection()));
    }

    @Override
    @Nullable
    public Member getMemberById(@Nonnull String id)
    {
        checkAvailable();

        return members.get(MiscUtil.parseSnowflake(id));
    }

    @Override
    @Nullable
    public Member getMemberById(long id)
    {
        checkAvailable();

        return members.get(id);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WidgetImpl))
            return false;
        WidgetImpl oWidget = (WidgetImpl) obj;
        return this == oWidget || this.id == oWidget.getIdLong();
    }

    @Override
    public String toString()
    {
        final EntityString entityString = new EntityString(this);
        if (isAvailable())
            entityString.setName(getName());
        return entityString.toString();
    }

    private void checkAvailable()
    {
        if (!isAvailable)
            throw new IllegalStateException("The widget for this Guild is unavailable!");
    }

    public class MemberImpl implements Member
    {
        private final boolean bot;
        private final long id;
        private final String username;
        private final String discriminator;
        private final String avatar;
        private final String nickname;
        private final OnlineStatus status;
        private final Activity game;
        private final WidgetImpl widget;
        private VoiceState state;

        private MemberImpl(@Nonnull DataObject json, @Nonnull WidgetImpl widget)
        {
            this.widget = widget;
            this.bot = json.getBoolean("bot");
            this.id = json.getLong("id");
            this.username = json.getString("username");
            this.discriminator = json.getString("discriminator");
            this.avatar = json.getString("avatar", null);
            this.nickname = json.getString("nick", null);
            this.status = OnlineStatus.fromKey(json.getString("status"));
            this.game = json.isNull("game") ? null : EntityBuilder.createActivity(json.getObject("game"));
        }

        private void setVoiceState(VoiceState voiceState)
        {
            state = voiceState;
        }

        @Override
        public boolean isBot()
        {
            return bot;
        }

        @Override
        @Nonnull
        public String getName()
        {
            return username;
        }

        @Override
        public long getIdLong()
        {
            return id;
        }

        @Nonnull
        @Override
        public String getAsMention()
        {
            return "<@" + getId() + ">";
        }

        @Override
        @Nonnull
        public String getDiscriminator()
        {
            return discriminator;
        }

        @Override
        @Nullable
        public String getAvatarId()
        {
            return avatar;
        }

        @Override
        @Nullable
        public String getAvatarUrl()
        {
            String avatarId = getAvatarId();
            return avatarId == null ? null : String.format(User.AVATAR_URL, getId(), avatarId, avatarId.startsWith("a_") ? ".gif" : ".png");
        }

        @Override
        @Nullable
        public ImageProxy getAvatar()
        {
            final String avatarUrl = getAvatarUrl();
            return avatarUrl == null ? null : new ImageProxy(avatarUrl);
        }

        @Override
        @Nonnull
        public String getDefaultAvatarId()
        {
            return String.valueOf(Integer.parseInt(getDiscriminator()) % 5);
        }

        @Override
        @Nonnull
        public String getDefaultAvatarUrl()
        {
            return String.format(User.DEFAULT_AVATAR_URL, getDefaultAvatarId());
        }

        @Override
        @Nonnull
        public ImageProxy getDefaultAvatar()
        {
            return new ImageProxy(getDefaultAvatarUrl());
        }

        @Override
        @Nonnull
        public String getEffectiveAvatarUrl()
        {
            String avatarUrl = getAvatarUrl();
            return avatarUrl == null ? getDefaultAvatarUrl() : avatarUrl;
        }

        @Override
        @Nonnull
        public ImageProxy getEffectiveAvatar()
        {
            return new ImageProxy(getEffectiveAvatarUrl());
        }

        @Override
        @Nullable
        public String getNickname()
        {
            return nickname;
        }

        @Override
        @Nonnull
        public String getEffectiveName()
        {
            return nickname == null ? username : nickname;
        }

        @Override
        @Nonnull
        public OnlineStatus getOnlineStatus()
        {
            return status;
        }
        
        @Override
        @Nullable
        public Activity getActivity()
        {
            return game;
        }

        @Override
        @Nonnull
        public VoiceState getVoiceState()
        {
            return state == null ? new VoiceStateImpl(this, widget) : state;
        }

        @Override
        @Nonnull
        public WidgetImpl getWidget()
        {
            return widget;
        }

        @Override
        public int hashCode() {
            return (widget.getId() + ' ' + id).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Member))
                return false;
            Member oMember = (Member) obj;
            return this == oMember || (this.id == oMember.getIdLong() && this.widget.getIdLong() == oMember.getWidget().getIdLong());
        }

        @Override
        public String toString()
        {
            return new EntityString(this)
                    .setName(getName())
                    .toString();
        }
    }

    public class VoiceChannelImpl implements VoiceChannel
    {
        private final int position;
        private final long id;
        private final String name;
        private final List<Member> members;
        private final Widget widget;

        private VoiceChannelImpl(@Nonnull DataObject json, @Nonnull Widget widget)
        {
            this.widget = widget;
            this.position = json.getInt("position");
            this.id = json.getLong("id");
            this.name = json.getString("name");
            this.members = new ArrayList<>();
        }

        private void addMember(@Nonnull Member member)
        {
            members.add(member);
        }

        @Override
        public int getPosition()
        {
            return position;
        }

        @Override
        public long getIdLong()
        {
            return id;
        }

        @Override
        @Nonnull
        public String getName()
        {
            return name;
        }

        @Override
        @Nonnull
        public List<Member> getMembers()
        {
            return members;
        }

        @Override
        @Nonnull
        public Widget getWidget()
        {
            return widget;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof VoiceChannel))
                return false;
            VoiceChannel oVChannel = (VoiceChannel) obj;
            return this == oVChannel || this.id == oVChannel.getIdLong();
        }

        @Override
        public String toString()
        {
            return new EntityString(this)
                    .setName(getName())
                    .toString();
        }
    }

    public class VoiceStateImpl implements VoiceState
    {
        private final VoiceChannel channel;
        private final boolean muted;
        private final boolean deafened;
        private final boolean suppress;
        private final boolean selfMute;
        private final boolean selfDeaf;
        private final Member member;
        private final Widget widget;

        private VoiceStateImpl(@Nonnull Member member, @Nonnull Widget widget)
        {
            this(null, false, false, false, false, false, member, widget);
        }

        private VoiceStateImpl(@Nullable VoiceChannel channel, boolean muted, boolean deafened, boolean suppress, boolean selfMute, boolean selfDeaf, @Nonnull Member member, @Nonnull Widget widget)
        {
            this.channel = channel;
            this.muted = muted;
            this.deafened = deafened;
            this.suppress = suppress;
            this.selfMute = selfMute;
            this.selfDeaf = selfDeaf;
            this.member = member;
            this.widget = widget;
        }
        
        @Override
        @Nullable
        public VoiceChannel getChannel()
        {
            return channel;
        }

        @Override
        public boolean inVoiceChannel()
        {
            return channel != null;
        }

        @Override
        public boolean isGuildMuted()
        {
            return muted;
        }

        @Override
        public boolean isGuildDeafened()
        {
            return deafened;
        }

        @Override
        public boolean isSuppressed()
        {
            return suppress;
        }

        @Override
        public boolean isSelfMuted()
        {
            return selfMute;
        }

        @Override
        public boolean isSelfDeafened()
        {
            return selfDeaf;
        }

        @Override
        public boolean isMuted()
        {
            return selfMute || muted;
        }

        @Override
        public boolean isDeafened()
        {
            return selfDeaf || deafened;
        }

        @Override
        @Nonnull
        public Member getMember()
        {
            return member;
        }

        @Override
        @Nonnull
        public Widget getWidget()
        {
            return widget;
        }

        @Override
        public int hashCode() {
            return member.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof VoiceState))
                return false;
            VoiceState oState = (VoiceState) obj;
            return this == oState || (this.member.equals(oState.getMember()) && this.widget.equals(oState.getWidget()));
        }

        @Override
        public String toString() {
            return new EntityString(this)
                    .setName(widget.getName())
                    .addMetadata("memberName", member.getEffectiveName())
                    .toString();
        }
    }
}
