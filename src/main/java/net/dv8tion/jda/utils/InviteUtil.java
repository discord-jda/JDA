/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.utils;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Channel;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.Event;
import net.dv8tion.jda.events.guild.GuildJoinEvent;
import net.dv8tion.jda.exceptions.PermissionException;
import net.dv8tion.jda.hooks.EventListener;
import net.dv8tion.jda.hooks.SubscribeEvent;
import net.dv8tion.jda.utils.InviteUtil.AdvancedInvite;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class InviteUtil
{
    public static Invite resolve(String code)
    {
        if (code.startsWith("http"))
        {
            String[] split = code.split("/");
            code = split[split.length - 1];
        }
        JSONObject response = new JDAImpl(false).getRequester().get("https://discordapp.com/api/invite/" + code);
        if (response.has("code"))
        {
            JSONObject guild = response.getJSONObject("guild");
            JSONObject channel = response.getJSONObject("channel");
            return new Invite(response.getString("code"), guild.getString("name"), guild.getString("id"),
                    channel.getString("name"), channel.getString("id"), channel.getString("type").equals("text"));
        }
        return null;
    }

    public static Invite createInvite(Channel chan, JDA jda)
    {
        if (!chan.checkPermission(jda.getSelfInfo(), Permission.CREATE_INSTANT_INVITE))
            throw new PermissionException(Permission.CREATE_INSTANT_INVITE);

        JSONObject response = ((JDAImpl) jda).getRequester().post("https://discordapp.com/api/channels/" + chan.getId() + "/invites", new JSONObject());
        if (response.has("code"))
        {
            JSONObject guild = response.getJSONObject("guild");
            JSONObject channel = response.getJSONObject("channel");
            return new Invite(response.getString("code"), guild.getString("name"), guild.getString("id"),
                    channel.getString("name"), channel.getString("id"), channel.getString("type").equals("text"));
        }
        return null;
    }

    @Deprecated
    public static void join(Invite invite, JDA jda)
    {
        join(invite, jda, null);
    }

    public static void join(Invite invite, JDA jda, Consumer<Guild> callback)
    {
        ((JDAImpl) jda).getRequester().post("https://discordapp.com/api/invite/" + invite.getCode(), new JSONObject());
        if(callback != null)
            jda.addEventListener(new AsyncCallback(invite.getGuildId(), callback));
    }

    @Deprecated
    public static void join(String code, JDA jda)
    {
        join(code, jda, null);
    }

    public static void join(String code, JDA jda, Consumer<Guild> callback)
    {
        join(resolve(code), jda, callback);
    }

    public static void delete(Invite invite, JDA jda)
    {
        delete(invite.getCode(), jda);
    }

    public static void delete(String code, JDA jda)
    {
        ((JDAImpl) jda).getRequester().delete("https://discordapp.com/api/invite/" + code);
    }

    /**
     * Provides a list of all {@link net.dv8tion.jda.utils.InviteUtil.AdvancedInvite Invites} for the given {@link net.dv8tion.jda.entities.Guild Guild}.
     *
     * @return
     *      An Immutable List of {@link net.dv8tion.jda.utils.InviteUtil.AdvancedInvite Invites}.
     */
    public static List<AdvancedInvite> getInvites(Guild guildObj)
    {
        if (!PermissionUtil.checkPermission(guildObj.getJDA().getSelfInfo(), Permission.MANAGE_SERVER, guildObj))
            throw new PermissionException(Permission.MANAGE_SERVER);

        List<AdvancedInvite> invites = new ArrayList<>();

        JSONArray array = ((JDAImpl)guildObj.getJDA()).getRequester().getA("https://discordapp.com/api/guilds/" + guildObj.getId() + "/invites");

        for (int i = 0; i < array.length(); i++)
        {
            JSONObject invite = array.getJSONObject(i);

            if (invite.has("code"))
            {
                JSONObject guild = invite.getJSONObject("guild");
                JSONObject channel = invite.getJSONObject("channel");
                JSONObject inviter = invite.getJSONObject("inviter");

                invites.add(new AdvancedInvite(
                        invite.getString("code"),
                        guild.getString("name"),
                        guild.getString("id"),
                        channel.getString("name"),
                        channel.getString("id"),
                        channel.getString("type").equals("text"),
                        invite.getInt("max_age"),
                        guild.isNull("splash_hash") ? null : guild.getString("splash_hash"),
                        invite.getBoolean("temporary"),
                        invite.getInt("max_uses"),
                        OffsetDateTime.parse(invite.getString("created_at")),
                        invite.getInt("uses"),
                        guildObj.getJDA().getUserById(inviter.getString("id")),
                        invite.getBoolean("revoked")));
            }
        }

        return Collections.unmodifiableList(invites);
    }

    public static List<AdvancedInvite> getInvites(Channel channelObj)
    {
        if (!PermissionUtil.checkPermission(channelObj.getJDA().getSelfInfo(), Permission.MANAGE_CHANNEL, channelObj))
            throw new PermissionException(Permission.MANAGE_CHANNEL);

        List<AdvancedInvite> invites = new ArrayList<>();

        JSONArray array = ((JDAImpl)channelObj.getJDA()).getRequester().getA("https://discordapp.com/api/channels/" + channelObj.getId() + "/invites");

        for (int i = 0; i < array.length(); i++)
        {
            JSONObject invite = array.getJSONObject(i);

            if (invite.has("code"))
            {
                JSONObject guild = invite.getJSONObject("guild");
                JSONObject channel = invite.getJSONObject("channel");
                JSONObject inviter = invite.getJSONObject("inviter");

                invites.add(new AdvancedInvite(
                        invite.getString("code"),
                        guild.getString("name"),
                        guild.getString("id"),
                        channel.getString("name"),
                        channel.getString("id"),
                        channel.getString("type").equals("text"),
                        invite.getInt("max_age"),
                        guild.isNull("splash_hash") ? null : guild.getString("splash_hash"),
                        invite.getBoolean("temporary"),
                        invite.getInt("max_uses"),
                        OffsetDateTime.parse(invite.getString("created_at")),
                        invite.getInt("uses"),
                        channelObj.getJDA().getUserById(inviter.getString("id")),
                        invite.getBoolean("revoked")));
            }
        }

        return Collections.unmodifiableList(invites);
    }

    public static class Invite
    {
        private final String code;
        private final String guildName, guildId;
        private final String channelName, channelId;
        private final boolean isTextChannel;

        private Invite(String code, String guildName, String guildId, String channelName, String channelId, boolean isTextChannel)
        {
            this.code = code;
            this.guildName = guildName;
            this.guildId = guildId;
            this.channelName = channelName;
            this.channelId = channelId;
            this.isTextChannel = isTextChannel;
        }

        public String getCode()
        {
            return code;
        }

        public String getUrl()
        {
            return "https://discord.gg/" + code;
        }

        public String getGuildName()
        {
            return guildName;
        }

        public String getGuildId()
        {
            return guildId;
        }

        public String getChannelName()
        {
            return channelName;
        }

        public String getChannelId()
        {
            return channelId;
        }

        public boolean isTextChannel()
        {
            return isTextChannel;
        }
    }

    public static class AdvancedInvite extends Invite {

        private final int maxAge;
        private final String guildSplashHash;
        private final boolean temporary;
        private final int maxUses;
        private final OffsetDateTime createdAt;
        private final int uses;
        //TODO what happens if the inviter left the server (and therefore is unknown for the api)?
        private final User inviter;
        private final boolean revoked;

        private AdvancedInvite(String code, String guildName, String guildId, String channelName, String channelId, boolean isTextChannel, int maxAge, String guildSplashHash, boolean temporary,
                int maxUses, OffsetDateTime createdAt, int uses, User inviter, boolean revoked) {
            super(code, guildName, guildId, channelName, channelId, isTextChannel);
            this.maxAge = maxAge;
            this.guildSplashHash = guildSplashHash;
            this.temporary = temporary;
            this.maxUses = maxUses;
            this.createdAt = createdAt;
            this.uses = uses;
            this.inviter = inviter;
            this.revoked = revoked;
        }

        public final int getMaxAge()
        {
            return maxAge;
        }

        public final String getGuildSplashHash()
        {
            return guildSplashHash;
        }

        public final boolean isTemporary()
        {
            return temporary;
        }

        public final int getMaxUses()
        {
            return maxUses;
        }

        public final OffsetDateTime getCreatedAt()
        {
            return createdAt;
        }

        public final int getUses()
        {
            return uses;
        }

        public final User getInviter()
        {
            return inviter;
        }

        public final boolean isRevoked()
        {
            return revoked;
        }
    }

    private static class AsyncCallback implements EventListener
    {
        private final String id;
        private final Consumer<Guild> cb;

        public AsyncCallback(String id, Consumer<Guild> cb)
        {
            this.id = id;
            this.cb = cb;
        }

        @Override
        @SubscribeEvent
        public void onEvent(Event event)
        {
            if (event instanceof GuildJoinEvent && ((GuildJoinEvent) event).getGuild().getId().equals(id))
            {
                event.getJDA().removeEventListener(this);
                cb.accept(((GuildJoinEvent) event).getGuild());
            }
        }
    }
}
