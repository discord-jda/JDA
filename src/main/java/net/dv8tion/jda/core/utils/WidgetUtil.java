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
package net.dv8tion.jda.core.utils;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.UserImpl;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.Requester;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The WidgetUtil is a class for interacting with various facets of Discord's
 * guild widgets
 *
 * @since  3.0
 * @author John A. Grosh
 */
public class WidgetUtil 
{
    public static final String WIDGET_PNG = Requester.DISCORD_API_PREFIX + "guilds/%s/widget.png?style=%s";
    public static final String WIDGET_URL = Requester.DISCORD_API_PREFIX + "guilds/%s/widget.json";
    public static final String WIDGET_HTML = "<iframe src=\"https://discordapp.com/widget?id=%s&theme=%s\" width=\"%d\" height=\"%d\" allowtransparency=\"true\" frameborder=\"0\"></iframe>";
    
    /**
     * Gets the banner image for the specified guild of the specified type.
     * <br>This banner will only be available if the guild in question has the
     * Widget enabled.
     * 
     * @param  guild
     *         The guild
     * @param  type
     *         The type (visual style) of the banner
     *
     * @return A String containing the URL of the banner image
     */
    public static String getWidgetBanner(Guild guild, BannerType type)
    {
        Checks.notNull(guild, "Guild");
        return getWidgetBanner(guild.getId(), type);
    }
    
    /**
     * Gets the banner image for the specified guild of the specified type.
     * <br>This banner will only be available if the guild in question has the
     * Widget enabled. Additionally, this method can be used independently of
     * being on the guild in question.
     * 
     * @param  guildId
     *         the guild ID
     * @param  type
     *         The type (visual style) of the banner
     *
     * @return A String containing the URL of the banner image
     */
    public static String getWidgetBanner(String guildId, BannerType type)
    {
        Checks.notNull(guildId, "GuildId");
        Checks.notNull(type, "BannerType");
        return String.format(WIDGET_PNG, guildId, type.name().toLowerCase());
    }
    
    /**
     * Gets the pre-made HTML Widget for the specified guild using the specified
     * settings. The widget will only display correctly if the guild in question
     * has the Widget enabled.
     * 
     * @param  guild
     *         the guild
     * @param  theme
     *         the theme, light or dark
     * @param  width
     *         the width of the widget
     * @param  height
     *         the height of the widget
     *
     * @return a String containing the pre-made widget with the supplied settings
     */
    public static String getPremadeWidgetHtml(Guild guild, WidgetTheme theme, int width, int height)
    {
        Checks.notNull(guild, "Guild");
        return getPremadeWidgetHtml(guild.getId(), theme, width, height);
    }
    
    /**
     * Gets the pre-made HTML Widget for the specified guild using the specified
     * settings. The widget will only display correctly if the guild in question
     * has the Widget enabled. Additionally, this method can be used independently
     * of being on the guild in question.
     * 
     * @param  guildId
     *         the guild ID
     * @param  theme
     *         the theme, light or dark
     * @param  width
     *         the width of the widget
     * @param  height
     *         the height of the widget
     *
     * @return a String containing the pre-made widget with the supplied settings
     */
    public static String getPremadeWidgetHtml(String guildId, WidgetTheme theme, int width, int height)
    {
        Checks.notNull(guildId, "GuildId");
        Checks.notNull(theme, "WidgetTheme");
        Checks.notNegative(width, "Width");
        Checks.notNegative(height, "Height");
        return String.format(WIDGET_HTML, guildId, theme.name().toLowerCase(), width, height);
    }
    
    /**
     * Makes a GET request to get the information for a Guild's widget. This
     * widget (if available) contains information about the guild, including the
     * Guild's name, an invite code (if set), a list of voice channels, and a
     * list of online members (plus the voice states of any members in voice
     * channels).
     *
     * <p>This Widget can be obtained from any valid guild ID that has
     * it enabled; no accounts need to be on the server to access this information.
     * 
     * @param  guildId
     *         The id of the Guild
     *
     * @throws net.dv8tion.jda.core.exceptions.RateLimitedException
     *         If the request was rate limited, <b>respect the timeout</b>!
     * @throws java.lang.NumberFormatException
     *         If the provided {@code guildId} cannot be parsed by {@link Long#parseLong(String)}
     *
     * @return {@code null} if the provided guild ID is not a valid Discord guild ID
     *         <br>a Widget object with null fields and isAvailable() returning
     *         false if the guild ID is valid but the guild in question does not
     *         have the widget enabled
     *         <br>a filled-in Widget object if the guild ID is valid and the guild
     *         in question has the widget enabled.
     */
    public static Widget getWidget(String guildId) throws RateLimitedException
    {
        return getWidget(MiscUtil.parseSnowflake(guildId));
    }

    /**
     * Makes a GET request to get the information for a Guild's widget. This
     * widget (if available) contains information about the guild, including the
     * Guild's name, an invite code (if set), a list of voice channels, and a
     * list of online members (plus the voice states of any members in voice
     * channels).
     *
     * <p>This Widget can be obtained from any valid guild ID that has
     * it enabled; no accounts need to be on the server to access this information.
     *
     * @param  guildId
     *         The id of the Guild
     *
     * @throws net.dv8tion.jda.core.exceptions.RateLimitedException
     *         If the request was rate limited, <b>respect the timeout</b>!
     *
     * @return {@code null} if the provided guild ID is not a valid Discord guild ID
     *         <br>a Widget object with null fields and isAvailable() returning
     *         false if the guild ID is valid but the guild in question does not
     *         have the widget enabled
     *         <br>a filled-in Widget object if the guild ID is valid and the guild
     *         in question has the widget enabled.
     */
    public static Widget getWidget(long guildId) throws RateLimitedException
    {
        Checks.notNull(guildId, "GuildId");

        HttpURLConnection connection;
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                    .url(String.format(WIDGET_URL, guildId))
                    .method("GET", null)
                    .header("user-agent", Requester.USER_AGENT)
                    .header("accept-encoding", "gzip")
                    .build();

        try (Response response = client.newCall(request).execute())
        {
            final int code = response.code();
            InputStream data = Requester.getBody(response);

            switch (code)
            {
                case 200: // ok
                {
                    try (InputStream stream = data)
                    {
                        return new Widget(new JSONObject(new JSONTokener(stream)));
                    }
                    catch (IOException e)
                    {
                        throw new IllegalStateException(e);
                    }
                }
                case 400: // not valid snowflake
                case 404: // guild not found
                    return null;
                case 403: // widget disabled
                    return new Widget(guildId);
                case 429: // ratelimited
                {
                    long retryAfter;
                    try (InputStream stream = data)
                    {
                        retryAfter = new JSONObject(new JSONTokener(stream)).getLong("retry_after");
                    }
                    catch (Exception e)
                    {
                        retryAfter = 0;
                    }
                    throw new RateLimitedException(WIDGET_URL, retryAfter);
                }
                default:
                    throw new IllegalStateException("An unknown status was returned: " + code + " " + response.message());
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * Represents the available banner types
     * <br>Each of these has a different appearance:
     *
     * <p>
     * <br><b>Shield</b> - tiny, only contains Discord logo and online count
     * <br><b>Banner1</b> - medium, contains server name, icon, and online count, and a "Powered by Discord" bar on the bottom
     * <br><b>Banner2</b> - small, contains server name, icon, and online count, and a Discord logo on the side
     * <br><b>Banner3</b> - medium, contains server name, icon, and online count, and a Discord logo with a "Chat Now" bar on the bottom
     * <br><b>Banner4</b> - large, contains a very big Discord logo, server name, icon, and online count, and a big "Join My Server" button
     */
    public enum BannerType
    {
        SHIELD, BANNER1, BANNER2, BANNER3, BANNER4
    }
    
    /**
     * Represents the color scheme of the widget
     * <br>These color themes match Discord's dark and light themes
     */
    public enum WidgetTheme
    {
        LIGHT, DARK
    }
    
    public static class Widget implements ISnowflake
    {
        private final boolean isAvailable;
        private final long id;
        private final String name;
        private final String invite;
        private final TLongObjectMap<VoiceChannel> channels;
        private final TLongObjectMap<Member> members;
        
        /**
         * Constructs an unavailable Widget
         */
        private Widget(long guildId)
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
         *        The {@link org.json.JSONObject JSONObject} to construct the Widget from
         */
        private Widget(JSONObject json)
        {
            String inviteCode = json.optString("instant_invite", null);
            if (inviteCode != null)
                inviteCode = inviteCode.substring(inviteCode.lastIndexOf("/") + 1);
            
            isAvailable = true;
            id = json.getLong("id");
            name = json.getString("name");
            invite = inviteCode;
            channels = MiscUtil.newLongMap();
            members = MiscUtil.newLongMap();
            
            JSONArray channelsJson = json.getJSONArray("channels");
            for (int i = 0; i < channelsJson.length(); i++)
            {
                JSONObject channel = channelsJson.getJSONObject(i);
                channels.put(channel.getLong("id"), new VoiceChannel(channel, this));
            }
            
            JSONArray membersJson = json.getJSONArray("members");
            for (int i = 0; i<membersJson.length(); i++)
            {
                JSONObject memberJson = membersJson.getJSONObject(i);
                Member member = new Member(memberJson, this);
                if (!memberJson.isNull("channel_id")) // voice state
                {
                    VoiceChannel channel = channels.get(memberJson.getLong("channel_id"));
                    member.setVoiceState(new VoiceState(channel, 
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
        
        /**
         * Shows whether or not the widget for a guild is available. If this
         * method returns false, all other values will be null
         * 
         * @return True, if the widget is available, false otherwise
         */
        public boolean isAvailable()
        {
            return isAvailable;
        }

        @Override
        public long getIdLong()
        {
            return id;
        }
        
        /**
         * Gets the name of the guild
         *
         * @throws IllegalStateException
         *         If the widget is not {@link #isAvailable() available}
         *
         * @return the name of the guild
         */
        public String getName()
        {
            checkAvailable();

            return name;
        }
        
        /**
         * Gets an invite code for the guild, or null if no invite channel is
         * enabled in the widget
         *
         * @throws IllegalStateException
         *         If the widget is not {@link #isAvailable() available}
         *
         * @return an invite code for the guild, if widget invites are enabled
         */
        public String getInviteCode()
        {
            checkAvailable();

            return invite;
        }
        
        /**
         * Gets the list of voice channels in the guild
         *
         * @throws IllegalStateException
         *         If the widget is not {@link #isAvailable() available}
         *
         * @return the list of voice channels in the guild
         */
        public List<VoiceChannel> getVoiceChannels()
        {
            checkAvailable();

            return Collections.unmodifiableList(new ArrayList<>(channels.valueCollection()));
        }
        
        /**
         * Gets a voice channel with the given ID, or null if the voice channel is not found
         * 
         * @param  id
         *         the ID of the voice channel
         *
         * @throws IllegalStateException
         *         If the widget is not {@link #isAvailable() available}
         * @throws NumberFormatException
         *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
         *
         * @return possibly-null VoiceChannel with the given ID. 
         */
        public VoiceChannel getVoiceChannelById(String id)
        {
            checkAvailable();

            return channels.get(MiscUtil.parseSnowflake(id));
        }

        /**
         * Gets a voice channel with the given ID, or {@code null} if the voice channel is not found
         *
         * @param  id
         *         the ID of the voice channel
         *
         * @throws IllegalStateException
         *         If the widget is not {@link #isAvailable() available}
         *
         * @return possibly-null VoiceChannel with the given ID.
         */
        public VoiceChannel getVoiceChannelById(long id)
        {
            checkAvailable();

            return channels.get(id);
        }
        
        /**
         * Gets a list of online members in the guild
         *
         * @throws IllegalStateException
         *         If the widget is not {@link #isAvailable() available}
         *
         * @return the list of members
         */
        public List<Member> getMembers()
        {
            checkAvailable();

            return Collections.unmodifiableList(new ArrayList<>(members.valueCollection()));
        }
        
        /**
         * Gets a member with the given ID, or null if the member is not found
         * 
         * @param  id
         *         the ID of the member
         *
         * @throws NumberFormatException
         *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
         * @throws IllegalStateException
         *         If the widget is not {@link #isAvailable() available}
         *
         * @return possibly-null Member with the given ID. 
         */
        public Member getMemberById(String id)
        {
            checkAvailable();

            return members.get(MiscUtil.parseSnowflake(id));
        }

        /**
         * Gets a member with the given ID, or {@code null} if the member is not found
         *
         * @param  id
         *         the ID of the member
         *
         * @throws IllegalStateException
         *         If the widget is not {@link #isAvailable() available}
         *
         * @return possibly-null Member with the given ID.
         */
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
            if (!(obj instanceof Widget))
                return false;
            Widget oWidget = (Widget) obj;
            return this == oWidget || this.id == oWidget.getIdLong();
        }
        
        @Override
        public String toString()
        {
            return "W:" + (isAvailable() ? getName() : "") + '(' + id + ')';
        }

        private void checkAvailable()
        {
            if (!isAvailable)
                throw new IllegalStateException("The widget for this Guild is unavailable!");
        }
        
        
        
        public static class Member implements ISnowflake, IMentionable
        {
            private final boolean bot;
            private final long id;
            private final String username;
            private final String discriminator;
            private final String avatar;
            private final String nickname;
            private final OnlineStatus status;
            private final Game game;
            private final Widget widget;
            private VoiceState state;
            
            private Member(JSONObject json, Widget widget)
            {
                this.widget = widget;
                this.bot = Helpers.optBoolean(json, "bot");
                this.id = json.getLong("id");
                this.username = json.getString("username");
                this.discriminator = json.getString("discriminator");
                this.avatar = json.optString("avatar", null);
                this.nickname = json.optString("nick", null);
                this.status = OnlineStatus.fromKey(json.getString("status"));
                this.game = json.isNull("game") ? null : EntityBuilder.createGame(json.getJSONObject("game"));
            }
            
            private void setVoiceState(VoiceState voiceState)
            {
                state = voiceState;
            }
            
            /**
             * Returns whether or not the given member is a bot account
             * 
             * @return true if the member is a bot, false otherwise
             */
            public boolean isBot()
            {
                return bot;
            }
            
            /**
             * Returns the username of the member
             * 
             * @return the username of the member
             */
            public String getName()
            {
                return username;
            }

            @Override
            public long getIdLong()
            {
                return id;
            }

            @Override
            public String getAsMention()
            {
                return "<@" + getId() + ">";
            }
            
            /**
             * Gets the discriminator of the member
             * 
             * @return the never-null discriminator of the member
             */
            public String getDiscriminator()
            {
                return discriminator;
            }
            
            /**
             * Gets the avatar hash of the member, or null if they do not have
             * an avatar set.
             * 
             * @return possibly-null String containing the avatar hash of the
             *         member
             */
            public String getAvatarId()
            {
                return avatar;
            }
            
            /**
             * Gets the avatar url of the member, or null if they do not have
             * an avatar set.
             * 
             * @return possibly-null String containing the avatar url of the
             *         member
             */
            public String getAvatarUrl()
            {
                return getAvatarId() == null ? null : "https://cdn.discordapp.com/avatars/" + getId() + "/" + getAvatarId()
                        + (getAvatarId().startsWith("a_") ? ".gif" : ".png");
            }

            /**
             * Gets the asset id of the member's default avatar
             * 
             * @return never-null String containing the asset id of the member's
             *         default avatar
             */
            public String getDefaultAvatarId()
            {
                return UserImpl.DefaultAvatar.values()[Integer.parseInt(getDiscriminator()) % UserImpl.DefaultAvatar.values().length].toString();
            }

            /**
             * Gets the url of the member's default avatar
             * 
             * @return never-null String containing the url of the member's
             *         default avatar
             */
            public String getDefaultAvatarUrl()
            {
                return "https://discordapp.com/assets/" + getDefaultAvatarId() + ".png";
            }

            /**
            * The URL for the user's avatar image
            * <br>If they do not have an avatar set, this will return the URL of their
            * default avatar
            * 
            * @return Never-null String containing the member's effective avatar url.
            */
            public String getEffectiveAvatarUrl()
            {
                return getAvatarUrl() == null ? getDefaultAvatarUrl() : getAvatarUrl();
            }
            
            /**
             * Gets the nickname of the member. If they do not have a nickname on
             * the guild, this will return null;
             * 
             * @return possibly-null String containing the nickname of the member
             */
            public String getNickname()
            {
                return nickname;
            }
            
            /**
             * Gets the visible name of the member. If they have a nickname set,
             * this will be their nickname. Otherwise, it will be their username.
             * 
             * @return never-null String containing the member's effective (visible) name
             */
            public String getEffectiveName()
            {
                return nickname == null ? username : nickname;
            }
            
            /**
             * Gets the online status of the member. The widget does not show
             * offline members, so this status should never be offline
             * 
             * @return the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} of the member
             */
            public OnlineStatus getOnlineStatus()
            {
                return status;
            }
            
            /**
            * The game that the member is currently playing.
            * <br>This game cannot be a stream.
            * If the user is not currently playing a game, this will return null.
            *
            * @return Possibly-null {@link net.dv8tion.jda.core.entities.Game Game} containing the game
            *         that the member is currently playing.
            */
            public Game getGame()
            {
                return game;
            }
            
            /**
             * The current voice state of the member.
             * <br>If the user is not in voice, this will return a VoiceState with a null channel.
             * 
             * @return never-null VoiceState of the member
             */
            public VoiceState getVoiceState()
            {
                return state == null ? new VoiceState(this, widget) : state;
            }

            /**
             * Gets the widget that to which this member belongs
             * 
             * @return the Widget that holds this member
             */
            public Widget getWidget()
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
                return "W.M:" + getName() + '(' + id + ')';
            }
            
        }
        
        
        public static class VoiceChannel implements ISnowflake
        {
            private final int position;
            private final long id;
            private final String name;
            private final List<Member> members;
            private final Widget widget;
            
            private VoiceChannel(JSONObject json, Widget widget)
            {
                this.widget = widget;
                this.position = json.getInt("position");
                this.id = json.getLong("id");
                this.name = json.getString("name");
                this.members = new ArrayList<>();
            }
            
            private void addMember(Member member)
            {
                members.add(member);
            }
            
            /**
             * Gets the integer position of the channel
             * 
             * @return integer position of the channel
             */
            public int getPosition()
            {
                return position;
            }

            @Override
            public long getIdLong()
            {
                return id;
            }
            
            /**
             * Gets the name of the channel
             * 
             * @return name of the channel
             */
            public String getName()
            {
                return name;
            }
            
            /**
             * Gets a list of all members in the channel
             * 
             * @return never-null, possibly-empty list of members in the channel
             */
            public List<Member> getMembers()
            {
                return members;
            }

            /**
             * Gets the Widget to which this voice channel belongs
             * 
             * @return the Widget object that holds this voice channel
             */
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
                return "W.VC:" + getName() + '(' + id + ')';
            }
        }
        
        public static class VoiceState
        {
            private final VoiceChannel channel;
            private final boolean muted;
            private final boolean deafened;
            private final boolean suppress;
            private final boolean selfMute;
            private final boolean selfDeaf;
            private final Member member;
            private final Widget widget;
            
            private VoiceState(Member member, Widget widget)
            {
                this(null, false, false, false, false, false, member, widget);
            }
            
            private VoiceState(VoiceChannel channel, boolean muted, boolean deafened, boolean suppress, boolean selfMute, boolean selfDeaf, Member member, Widget widget)
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
            
            /**
             * Gets the channel the member is in
             * 
             * @return never-null VoiceChannel
             */
            public VoiceChannel getChannel()
            {
                return channel;
            }
            
            /**
             * Used to determine if the member is currently in a voice channel.
             * <br>If this is false, getChannel() will return null
             * 
             * @return True, if the member is in a voice channel
             */
            public boolean inVoiceChannel()
            {
                return channel != null;
            }
            
            /**
             * Whether the member is muted by an admin
             * 
             * @return True, if the member is muted
             */
            public boolean isGuildMuted()
            {
                return muted;
            }
            
            /**
             * Whether the member is deafened by an admin
             * 
             * @return True, if the member is deafened
             */
            public boolean isGuildDeafened()
            {
                return deafened;
            }
            
            /**
             * Whether the member is suppressed
             * 
             * @return True, if the member is suppressed
             */
            public boolean isSuppressed()
            {
                return suppress;
            }
            
            /**
             * Whether the member is self-muted
             * 
             * @return True, if the member is self-muted
             */
            public boolean isSelfMuted()
            {
                return selfMute;
            }
            
            /**
             * Whether the member is self-deafened
             * 
             * @return True, if the member is self-deafened
             */
            public boolean isSelfDeafened()
            {
                return selfDeaf;
            }
            
            /**
             * Whether the member is muted, either by an admin or self-muted
             * 
             * @return True, if the member is self-muted or guild-muted
             */
            public boolean isMuted()
            {
                return selfMute || muted;
            }
            
            /**
             * Whether the member is deafened, either by an admin or self-deafened
             * 
             * @return True, if the member is self-deafened or guild-deafened
             */
            public boolean isDeafened()
            {
                return selfDeaf || deafened;
            }
            
            public Member getMember()
            {
                return member;
            }
            
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
                return "VS:" + widget.getName() + ':' + member.getEffectiveName();
            }
        }
    }
}
