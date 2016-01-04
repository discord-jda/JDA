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
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.utils.AvatarUtil;
import org.json.JSONObject;

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
     * @return
     *      the Guild of this Manager
     */
    public Guild getGuild()
    {
        return guild;
    }

    /**
     * Changes the name of this Guild
     *
     * @param name
     *      the new name of the Guild
     * @return
     *      this
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
     * Changes the {@link net.dv8tion.jda.Region Region} of this Guild
     *
     * @param region
     *      the new Region
     * @return
     *      this
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
     * Changes the icon of this Guild
     * You can create the icon via the {@link net.dv8tion.jda.utils.AvatarUtil AvatarUtil} class.
     * Passing in null, will remove the current icon from the Guild
     *
     * @param avatar
     *      the new icon
     * @return
     *      this
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
     *      the new afk-channel
     * @return
     *      this
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
     *      this
     */
    public GuildManager setAfkTimeout(Timeout timeout)
    {
        update(getFrame().put("afk_timeout", timeout.getSeconds()));
        return this;
    }

    /**
     * Leaves or Deletes this Guild.
     * If the logged in User is the owner of this Guild, the Guild is deleted.
     * Otherwise, this guild will be left
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
