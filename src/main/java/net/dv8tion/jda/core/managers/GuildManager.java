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

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.requests.RestAction;

public class GuildManager
{
    protected final GuildManagerUpdatable updatable;

    public GuildManager(Guild guild)
    {
        this.updatable = new GuildManagerUpdatable(guild);
    }

    /**
     * Returns the {@link net.dv8tion.jda.core.entities.Guild Guild} object of this Manager. Useful if this Manager was returned via a create function
     *
     * @return
     *      the {@link net.dv8tion.jda.core.entities.Guild Guild} of this Manager
     */
    public Guild getGuild()
    {
        return updatable.getGuild();
    }

    public RestAction<Void> setName(String name)
    {
        return  updatable.setName(name).update();
    }

    public RestAction<Void> setRegion(Region region)
    {
        return updatable.setRegion(region).update();
    }

//    public RestAction<Void> setIcon(AvatarUtil.Avatar avatar)
//    {
//        return updatable.setIcon(avatar).update();
//    }

    public RestAction<Void> setAfkChannel(VoiceChannel afkChannel)
    {
        return updatable.setAfkChannel(afkChannel).update();
    }

    public RestAction<Void> setAfkTimeout(Timeout timeout)
    {
        return updatable.setAfkTimeout(timeout).update();
    }

    public RestAction<Void> setVerificationLevel(Guild.VerificationLevel level)
    {
        return updatable.setVerificationLevel(level).update();
    }

    public RestAction<Void> setDefaultNotificationLeveL(Guild.NotificationLevel level)
    {
        return updatable.setDefaultNotificationLevel(level).update();
    }

    public RestAction<Void> setRequiredMFALevel(Guild.MFALevel level)
    {
        return updatable.setRequiredMFALevel(level).update();
    }

    /**
     * Represents the idle time allowed until a user is moved to the
     * AFK {@link net.dv8tion.jda.core.entities.VoiceChannel} if one is set.
     */
    public enum Timeout
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
         * The amount of seconds represented by this {@link Timeout}.
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
}
