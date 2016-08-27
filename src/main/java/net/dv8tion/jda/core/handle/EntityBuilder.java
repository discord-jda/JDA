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

package net.dv8tion.jda.core.handle;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.*;
import net.dv8tion.jda.core.requests.GuildLock;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.function.Consumer;

public class EntityBuilder
{
    protected final JDAImpl api;

    public EntityBuilder(JDA api)
    {
        this.api = (JDAImpl) api;
    }

    public User createUser(JSONObject user)
    {
        String id = user.getString("id");
        UserImpl userObj = ((UserImpl) api.getUserMap().get(id));
        if (userObj == null)
        {
            userObj = new UserImpl(id, api);
            api.getUserMap().put(id, userObj);
        }
        return userObj
                .setUserName(user.getString("username"))
                .setDiscriminator(user.get("discriminator").toString())
                .setAvatarId(user.isNull("avatar") ? null : user.getString("avatar"))
                .setIsBot(user.has("bot") && user.getBoolean("bot"));
    }

    public SelfInfo createSelfInfo(JSONObject self)
    {
        SelfInfoImpl selfInfo = ((SelfInfoImpl) api.getSelfInfo());
        if (selfInfo == null)
        {
            selfInfo = new SelfInfoImpl(self.getString("id"), api);
            api.setSelfInfo(selfInfo);
        }
        if (!api.getUserMap().containsKey(selfInfo.getId()))
        {
            api.getUserMap().put(selfInfo.getId(), selfInfo);
        }
        return (SelfInfo) selfInfo
                .setVerified(self.getBoolean("verified"))
                .setMfaEnabled(self.getBoolean("mfa_enabled"))
                .setUserName(self.getString("username"))
                .setDiscriminator(self.getString("discriminator"))
                .setAvatarId(self.isNull("avatar") ? null : self.getString("avatar"))
                .setIsBot(self.has("bot") && self.getBoolean("bot"));
    }

    public Guild createGuildFirstPass(JSONObject guild, Consumer<Guild> secondPassCallback)
    {
        String id = guild.getString("id");
        GuildImpl guildObj = ((GuildImpl) api.getGuildMap().get(id));
        if (guildObj == null)
        {
            guildObj = new GuildImpl(api, id);
            api.getGuildMap().put(id, guildObj);
        }
        if (guild.has("unavailable") && guild.getBoolean("unavailable"))
        {
            guildObj.setAvailable(false);
            if (secondPassCallback != null)
            {
                secondPassCallback.accept(guildObj);
            }
            GuildLock.get(api).lock(id);
            return guildObj;
        }
        guildObj
                .setAvailable(true)
                .setIconId(guild.isNull("icon") ? null : guild.getString("icon"))
                .setRegion(Region.fromKey(guild.getString("region")))
                .setName(guild.getString("name"))
                .setAfkTimeout(guild.getInt("afk_timeout"))
                .setVerificationLevel(Guild.VerificationLevel.fromKey(guild.getInt("verification_level")));


        JSONArray roles = guild.getJSONArray("roles");
        for (int i = 0; i < roles.length(); i++)
        {
            Role role = createRole(roles.getJSONObject(i), guildObj.getId());
            guildObj.getRolesMap().put(role.getId(), role);
            if (role.getId().equals(guildObj.getId()))
                guildObj.setPublicRole(role);
        }

        if (guild.has("members"))
        {
            JSONArray members = guild.getJSONArray("members");
            createGuildMemberPass(guildObj, members);
        }

        User owner = api.getUserById(guild.getString("owner_id"));
        if (owner != null)
            guildObj.setOwner(owner);

        if (guild.has("presences"))
        {
            JSONArray presences = guild.getJSONArray("presences");
            for (int i = 0; i < presences.length(); i++)
            {
                JSONObject presence = presences.getJSONObject(i);
                UserImpl user = ((UserImpl) api.getUserMap().get(presence.getJSONObject("user").getString("id")));
                if (user == null)
                {
                    //corresponding user to presence not found... ignoring
                    continue;
                }
                Game presenceGame = null;
                if( !presence.isNull("game") && !presence.getJSONObject("game").isNull("name") )
                {
                    presenceGame = new GameImpl(presence.getJSONObject("game").get("name").toString(),
                            presence.getJSONObject("game").isNull("url") ? null : presence.getJSONObject("game").get("url").toString(),
                            presence.getJSONObject("game").isNull("type") ? Game.GameType.DEFAULT : Game.GameType.fromKey((int) presence.getJSONObject("game").get("type")) );
                }
                user
                        .setCurrentGame(presenceGame)
                        .setOnlineStatus(OnlineStatus.fromKey(presence.getString("status")));
            }
        }

        if (guild.has("channels"))
        {
            JSONArray channels = guild.getJSONArray("channels");

            for (int i = 0; i < channels.length(); i++)
            {
                JSONObject channel = channels.getJSONObject(i);
                ChannelType type = ChannelType.fromId(channel.getInt("type"));
                if (type == ChannelType.TEXT)
                {
                    TextChannel newChannel = createTextChannel(channel, guildObj.getId());
                    if (newChannel.getId().equals(guildObj.getId()))
                        guildObj.setPublicChannel(newChannel);
                }
                else if (type == ChannelType.VOICE)
                {
                    VoiceChannel newChannel = createVoiceChannel(channel, guildObj.getId());
                    if (newChannel.getId().equals(guild.getString("afk_channel_id")))
                        guildObj.setAfkChannel(newChannel);
                }
                else
                    JDAImpl.LOG.fatal("Received a channel for a guild that isn't a text or voice channel. JSON: " + channel);
            }
        }

        if (guild.has("voice_states"))
        {
            JSONArray voiceStates = guild.getJSONArray("voice_states");
            for (int i = 0; i < voiceStates.length(); i++)
            {
                JSONObject voiceState = voiceStates.getJSONObject(i);
                User user = api.getUserById(voiceState.getString("user_id"));
                if (user == null)
                {
                    //voice-status of offline user -> ignore
                    continue;
                }

                try
                {
                    VoiceStatus voiceStatus = createVoiceStatus(voiceState, guildObj, user);
                    ((VoiceChannelImpl) voiceStatus.getChannel()).getUsersModifiable().add(user);
                }
                catch (IllegalArgumentException ignored)
                {
                    //Ignore this: weird behaviour of Discord itself gives us presences to vc that were deleted
                }
            }
        }


        //We allow Guild creation without second pass for when JDA itself creates a NEW Guild. We won't need
        // to worry about there being a lack of offline Users because there wont be -any users or, at the very
        // most, the only User will be the JDA user that just created the new Guild.
        //This fall through is used by JDAImpl.createGuild(String, Region).
        if (secondPassCallback != null && guild.has("large") && guild.getBoolean("large"))
        {
            HashMap<String, JSONObject> cachedGuildJsons = cachedJdaGuildJsons.get(api);
            HashMap<String, Consumer<Guild>> cachedGuildCallbacks = cachedJdaGuildCallbacks.get(api);
            cachedGuildJsons.put(id, guild);
            cachedGuildCallbacks.put(id, secondPassCallback);
            GuildMembersChunkHandler.setExpectedGuildMembers(api, id, guild.getInt("member_count"));
            if (api.getClient().isReady())
            {
                JSONObject obj = new JSONObject()
                        .put("op", 8)
                        .put("d", new JSONObject()
                                .put("guild_id", id)
                                .put("query","")
                                .put("limit", 0)
                        );
                api.getClient().send(obj.toString());
            }
            else
            {
                new ReadyHandler(api, 0).onGuildNeedsMembers(guildObj);
            }
            GuildLock.get(api).lock(id);
            return null;//Nothing should be using the return of this method besides JDAImpl.createGuild(String, Region)
        }

        JSONArray channels = guild.getJSONArray("channels");
        createGuildChannelPass(guildObj, channels);

        if (secondPassCallback != null)
        {
            secondPassCallback.accept(guildObj);
            GuildLock.get(api).unlock(guildObj.getId());
            return null;//Nothing should be using the return of this method besides JDAImpl.createGuild(String, Region)
        }

        GuildLock.get(api).unlock(guildObj.getId());
        return guildObj;
    }
}
