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
package net.dv8tion.jda.managers;

import net.dv8tion.jda.Region;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.entities.impl.UserImpl;
import net.dv8tion.jda.utils.AvatarUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Manager used to modify aspects of a {@link net.dv8tion.jda.entities.Guild Guild}.
 */
public class GuildManager
{
    /**
     * Represents the idle time allowed until a user is moved to the
     * AFK {@link net.dv8tion.jda.entities.VoiceChannel} if one is set.
     */
    enum Timeout
    {
        SECONDS_60(60),
        SECONDS_300(300),
        SECONDS_900(900),
        SECONDS_1800(1800),
        SECONDS_3600(3600);

        private final int seconds;
        Timeout(int seconds)
        {
            this.seconds = seconds;
        }

        /**
         * The amount of seconds represented by this {@link net.dv8tion.jda.managers.GuildManager.Timeout}.
         *
         * @return
         *      An positive non-zero int representing the timeout amount in seconds.
         */
        public int getSeconds()
        {
            return seconds;
        }

        /**
         * The timeout as a string.<br>
         * Examples:    "60"  "300"   etc
         *
         * @return
         *      Seconds as a string.
         */
        @Override
        public String toString()
        {
            return "" + seconds;
        }
    }

    private final Guild guild;

    /**
     * Creates a {@link net.dv8tion.jda.managers.GuildManager} that can be used to manage
     * different aspects of the provided {@link net.dv8tion.jda.entities.Guild}.
     *
     * @param guild
     *          The {@link net.dv8tion.jda.entities.Guild} which the manager deals with.
     */
    public GuildManager(Guild guild)
    {
        this.guild = guild;
    }

    /**
     * Returns the {@link net.dv8tion.jda.entities.Guild Guild} object of this Manager. Useful if this Manager was returned via a create function
     *
     * @return
     *      the {@link net.dv8tion.jda.entities.Guild Guild} of this Manager
     */
    public Guild getGuild()
    {
        return guild;
    }

    /**
     * Changes the name of this Guild
     *
     * @param name
     *          the new name of the Guild
     * @return
     *      This {@link net.dv8tion.jda.managers.GuildManager GuildManager} instance. Useful for chaining.
     */
    public GuildManager setName(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Guild name must not be null!");
        }
        if (name.equals(guild.getName()))
        {
            return this;
        }
        update(new JSONObject().put("name", name));
        return this;
    }

    /**
     * Changes the {@link net.dv8tion.jda.Region Region} of this {@link net.dv8tion.jda.entities.Guild Guild}.
     *
     * @param region
     *          the new {@link net.dv8tion.jda.Region Region}
     * @return
     *
     *      This {@link net.dv8tion.jda.managers.GuildManager GuildManager} instance. Useful for chaining.
     */
    public GuildManager setRegion(Region region)
    {
        if (region == guild.getRegion() || region == Region.UNKNOWN)
        {
            return this;
        }
        update(getFrame().put("region", region.getKey()));
        return this;
    }

    /**
     * Changes the icon of this Guild.<br>
     * You can create the icon via the {@link net.dv8tion.jda.utils.AvatarUtil AvatarUtil} class.
     * Passing in null, will remove the current icon from the Guild
     *
     * @param avatar
     *          the new icon
     * @return
     *      This {@link net.dv8tion.jda.managers.GuildManager GuildManager} instance. Useful for chaining.
     */
    public GuildManager setIcon(AvatarUtil.Avatar avatar)
    {
        update(getFrame().put("icon", avatar == null ? JSONObject.NULL : avatar.getEncoded()));
        return this;
    }

    /**
     * Changes the AFK {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} of this Guild
     * If passed null, this will disable the AFK-Channel
     *
     * @param channel
     *          the new afk-channel
     * @return
     *      This {@link net.dv8tion.jda.managers.GuildManager GuildManager} instance. Useful for chaining.
     */
    public GuildManager setAfkChannel(VoiceChannel channel)
    {
        if (channel != null && channel.getGuild() != guild)
        {
            throw new IllegalArgumentException("Given VoiceChannel is not member of modifying Guild");
        }
        update(getFrame().put("afk_channel_id", channel == null ? JSONObject.NULL : channel.getId()));
        return this;
    }

    /**
     * Changes the AFK Timeout of this Guild
     * After given timeout (in seconds) Users being AFK in voice are being moved to the AFK-Channel
     * Valid timeouts are: 60, 300, 900, 1800, 3600
     *
     * @param timeout
     *      the new afk timeout
     * @return
     *      This {@link net.dv8tion.jda.managers.GuildManager GuildManager} instance. Useful for chaining.
     */
    public GuildManager setAfkTimeout(Timeout timeout)
    {
        update(getFrame().put("afk_timeout", timeout.getSeconds()));
        return this;
    }

    /**
     * Kicks a {@link net.dv8tion.jda.entities.User User} from the {@link net.dv8tion.jda.entities.Guild Guild}.<br>
     * <p>
     * <b>Note:</b> {@link net.dv8tion.jda.entities.Guild#getUsers()} will still contain the {@link net.dv8tion.jda.entities.User User}
     * until Discord sends the {@link net.dv8tion.jda.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * @param user
     *          The {@link net.dv8tion.jda.entities.User User} to kick from the from the {@link net.dv8tion.jda.entities.Guild Guild}.
     */
    public void kick(User user)
    {
        kick(user.getId());
    }

    /**
     * Kicks the {@link net.dv8tion.jda.entities.User User} specified by the userId from the from the {@link net.dv8tion.jda.entities.Guild Guild}.
     * <p>
     * <b>Note:</b> {@link net.dv8tion.jda.entities.Guild#getUsers()} will still contain the {@link net.dv8tion.jda.entities.User User}
     * until Discord sends the {@link net.dv8tion.jda.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * @param userId
     *          The id of the {@link net.dv8tion.jda.entities.User User} to kick from the from the {@link net.dv8tion.jda.entities.Guild Guild}.
     */
    public void kick(String userId)
    {
        ((JDAImpl) guild.getJDA()).getRequester().delete("https://discordapp.com/api/guilds/"
                + guild.getId() + "/members/" + userId);
    }

    /**
     * Bans a {@link net.dv8tion.jda.entities.User User} and deletes messages sent by the user
     * based on the amount of delDays.<br>
     * If you wish to ban a user without deleting any messages, provide delDays with a value of 0.
     * <p>
     * <b>Note:</b> {@link net.dv8tion.jda.entities.Guild#getUsers()} will still contain the {@link net.dv8tion.jda.entities.User User}
     * until Discord sends the {@link net.dv8tion.jda.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * @param user
     *          The {@link net.dv8tion.jda.entities.User User} to ban.
     * @param delDays
     *          The history of messages, in days, that will be deleted.
     */
    public void ban(User user, int delDays)
    {
        ban(user.getId(), delDays);
    }

    /**
     * Bans the {@link net.dv8tion.jda.entities.User User} specified by the userId nd deletes messages sent by the user
     * based on the amount of delDays.<br>
     * If you wish to ban a user without deleting any messages, provide delDays with a value of 0.
     * <p>
     * <b>Note:</b> {@link net.dv8tion.jda.entities.Guild#getUsers()} will still contain the {@link net.dv8tion.jda.entities.User User}
     * until Discord sends the {@link net.dv8tion.jda.events.guild.member.GuildMemberLeaveEvent GuildMemberLeaveEvent}.
     *
     * @param userId
     *          The id of the {@link net.dv8tion.jda.entities.User User} to ban.
     * @param delDays
     *          The history of messages, in days, that will be deleted.
     */
    public void ban(String userId, int delDays)
    {
        ((JDAImpl) guild.getJDA()).getRequester().put("https://discordapp.com/api/guilds/"
                + guild.getId() + "/bans/" + userId + (delDays > 0 ? "?delete-message-days=" + delDays : ""), new JSONObject());
    }

    /**
     * Gets an unmodifiable list of the currently banned {@link net.dv8tion.jda.entities.User Users}.<br>
     * If you wish to ban or unban a user, please use one of the ban or unban methods of this Manager
     *
     * @return
     *      unmodifiable list of currently banned Users
     */
    List<User> getBans()
    {
        List<User> bans = new LinkedList<>();
        JSONArray bannedArr = ((JDAImpl) guild.getJDA()).getRequester().getA("https://discordapp.com/api/guilds/" + guild.getId() + "/bans");
        for (int i = 0; i < bannedArr.length(); i++)
        {
            JSONObject userObj = bannedArr.getJSONObject(i).getJSONObject("user");
            User u = guild.getJDA().getUserById(userObj.getString("id"));
            if (u != null)
            {
                bans.add(u);
            }
            else
            {
                //Create user here, instead of using the EntityBuilder (don't want to add users to registry)
                bans.add(new UserImpl(userObj.getString("id"), ((JDAImpl) guild.getJDA()))
                        .setUserName(userObj.getString("username"))
                        .setDiscriminator(userObj.get("discriminator").toString())
                        .setAvatarId(userObj.isNull("avatar") ? null : userObj.getString("avatar")));
            }
        }
        return bans;
    }

    /**
     * Unbans the provided {@link net.dv8tion.jda.entities.User User} from the {@link net.dv8tion.jda.entities.Guild Guild}.
     *
     * @param user
     *          The {@link net.dv8tion.jda.entities.User User} to unban.
     */
    public void unBan(User user)
    {
        unBan(user.getId());
    }

    /**
     * Unbans the {@link net.dv8tion.jda.entities.User User} from the {@link net.dv8tion.jda.entities.Guild Guild} based on the provided userId.
     *
     * @param userId
     *          The id of the {@link net.dv8tion.jda.entities.User User} to unban.
     */
    public void unBan(String userId)
    {
        ((JDAImpl) guild.getJDA()).getRequester().delete("https://discordapp.com/api/guilds/"
                + guild.getId() + "/bans/" + userId);
    }

    /**
     * Leaves or Deletes this {@link net.dv8tion.jda.entities.Guild Guild}.
     * If the logged in {@link net.dv8tion.jda.entities.User User} is the owner of
     * this {@link net.dv8tion.jda.entities.Guild Guild}, the {@link net.dv8tion.jda.entities.Guild Guild} is deleted.
     * Otherwise, this guild will be left.
     */
    public void leaveOrDelete()
    {
        ((JDAImpl) guild.getJDA()).getRequester().delete("https://discordapp.com/api/guilds/" + guild.getId());
    }

    private JSONObject getFrame()
    {
        return new JSONObject().put("name", guild.getName());
    }

    private void update(JSONObject object)
    {
        ((JDAImpl) guild.getJDA()).getRequester().patch("https://discordapp.com/api/guilds/" + guild.getId(), object);
    }
}
