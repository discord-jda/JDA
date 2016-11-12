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

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.requests.RestAction;

public class GuildManager
{
    protected final GuildManagerUpdatable updatable;

    public GuildManager(Guild guild)
    {
        this.updatable = new GuildManagerUpdatable(guild);
    }

    public JDA getJDA()
    {
        return updatable.getJDA();
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
        return  updatable.getNameField().setValue(name).update();
    }

    public RestAction<Void> setRegion(Region region)
    {
        return updatable.getRegionField().setValue(region).update();
    }

    public RestAction<Void> setIcon(Icon icon)
    {
        return updatable.getIconField().setValue(icon).update();
    }

    public RestAction<Void> setSplash(Icon splash)
    {
        return updatable.getSplashField().setValue(splash).update();
    }

    public RestAction<Void> setAfkChannel(VoiceChannel afkChannel)
    {
        return updatable.getAfkChannelField().setValue(afkChannel).update();
    }

    public RestAction<Void> setAfkTimeout(Guild.Timeout timeout)
    {
        return updatable.getAfkTimeoutField().setValue(timeout).update();
    }

    public RestAction<Void> setVerificationLevel(Guild.VerificationLevel level)
    {
        return updatable.getVerificationLevelField().setValue(level).update();
    }

    public RestAction<Void> setDefaultNotificationLeveL(Guild.NotificationLevel level)
    {
        return updatable.getDefaultNotificationLevelField().setValue(level).update();
    }

    public RestAction<Void> setRequiredMFALevel(Guild.MFALevel level)
    {
        return updatable.getRequiredMFALevelField().setValue(level).update();
    }
}
