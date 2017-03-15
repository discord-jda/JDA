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

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.requests.RestAction;

/**
 * Facade for a {@link net.dv8tion.jda.core.managers.GuildManagerUpdatable GuildManagerUpdatable} instance.
 * <br>Simplifies managing flow for convenience.
 *
 * <p>This decoration allows to modify a single field by automatically building an update {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 */
public class GuildManager
{
    protected final GuildManagerUpdatable updatable;

    public GuildManager(Guild guild)
    {
        this.updatable = new GuildManagerUpdatable(guild);
    }

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Manager
     *
     * @return the corresponding JDA instance
     */
    public JDA getJDA()
    {
        return updatable.getJDA();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} object of this Manager.
     * Useful if this Manager was returned via a create function
     *
     * @return The {@link net.dv8tion.jda.core.entities.Guild Guild} of this Manager
     */
    public Guild getGuild()
    {
        return updatable.getGuild();
    }

    /**
     * Sets the name of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * More information can be found {@link GuildManagerUpdatable#getNameField() here}!
     *
     * <p>For information on possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}
     * by the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} see {@link GuildManagerUpdatable#update() #update()}
     *
     * @param  name
     *         The new name for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     *         <br>Update RestAction from {@link GuildManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#getNameField()
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#update()
     */
    public RestAction<Void> setName(String name)
    {
        return  updatable.getNameField().setValue(name).update();
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.Region Region} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * More information can be found {@link GuildManagerUpdatable#getRegionField() here}!
     *
     * <p>For information on possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}
     * by the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} see {@link GuildManagerUpdatable#update() #update()}
     *
     * @param  region
     *         The new region for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     *         <br>Update RestAction from {@link GuildManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#getRegionField()
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#update()
     */
    public RestAction<Void> setRegion(Region region)
    {
        return updatable.getRegionField().setValue(region).update();
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Icon Icon} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * More information can be found {@link GuildManagerUpdatable#getIconField() here}!
     *
     * <p>For information on possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}
     * by the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} see {@link GuildManagerUpdatable#update() #update()}
     *
     * @param  icon
     *         The new icon for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     *         <br>Update RestAction from {@link GuildManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#getIconField()
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#update()
     */
    public RestAction<Void> setIcon(Icon icon)
    {
        return updatable.getIconField().setValue(icon).update();
    }

    /**
     * Sets the Splash {@link net.dv8tion.jda.core.entities.Icon Icon} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * More information can be found {@link GuildManagerUpdatable#getSplashField() here}!
     *
     * <p>For information on possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}
     * by the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} see {@link GuildManagerUpdatable#update() #update()}
     *
     * @param  splash
     *         The new splash for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     *         <br>Update RestAction from {@link GuildManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#getSplashField()
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#update()
     */
    public RestAction<Void> setSplash(Icon splash)
    {
        return updatable.getSplashField().setValue(splash).update();
    }

    /**
     * Sets the AFK {@link net.dv8tion.jda.core.entities.VoiceChannel VoiceChannel} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * More information can be found {@link GuildManagerUpdatable#getAfkChannelField() here}!
     *
     * <p>For information on possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}
     * by the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} see {@link GuildManagerUpdatable#update() #update()}
     *
     * @param  afkChannel
     *         The new afk channel for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     *         <br>Update RestAction from {@link GuildManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#getAfkChannelField()
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#update()
     */
    public RestAction<Void> setAfkChannel(VoiceChannel afkChannel)
    {
        return updatable.getAfkChannelField().setValue(afkChannel).update();
    }

    /**
     * Sets the afk {@link net.dv8tion.jda.core.entities.Guild.Timeout Timeout} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * More information can be found {@link GuildManagerUpdatable#getAfkTimeoutField() here}!
     *
     * <p>For information on possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}
     * by the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} see {@link GuildManagerUpdatable#update() #update()}
     *
     * @param  timeout
     *         The new afk timeout for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     *         <br>Update RestAction from {@link GuildManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#getAfkTimeoutField()
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#update()
     */
    public RestAction<Void> setAfkTimeout(Guild.Timeout timeout)
    {
        return updatable.getAfkTimeoutField().setValue(timeout).update();
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Guild.VerificationLevel Verification Level} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * More information can be found {@link GuildManagerUpdatable#getVerificationLevelField() here}!
     *
     * <p>For information on possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}
     * by the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} see {@link GuildManagerUpdatable#update() #update()}
     *
     * @param  level
     *         The new Verification Level for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     *         <br>Update RestAction from {@link GuildManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#getVerificationLevelField()
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#update()
     */
    public RestAction<Void> setVerificationLevel(Guild.VerificationLevel level)
    {
        return updatable.getVerificationLevelField().setValue(level).update();
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Guild.NotificationLevel Notification Level} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * More information can be found {@link GuildManagerUpdatable#getDefaultNotificationLevelField() here}!
     *
     * <p>For information on possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}
     * by the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} see {@link GuildManagerUpdatable#update() #update()}
     *
     * @param  level
     *         The new Notification Level for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     *         <br>Update RestAction from {@link GuildManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#getDefaultNotificationLevelField()
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#update()
     */
    public RestAction<Void> setDefaultNotificationLevel(Guild.NotificationLevel level)
    {
        return updatable.getDefaultNotificationLevelField().setValue(level).update();
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Guild.MFALevel MFA Level} of this {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * More information can be found {@link GuildManagerUpdatable#getRequiredMFALevelField() here}!
     *
     * <p>For information on possible {@link net.dv8tion.jda.core.requests.ErrorResponse ErrorResponses}
     * by the returned {@link net.dv8tion.jda.core.requests.RestAction RestAction} see {@link GuildManagerUpdatable#update() #update()}
     *
     * @param  level
     *         The new MFA Level for this {@link net.dv8tion.jda.core.entities.Guild Guild}
     *
     * @return {@link net.dv8tion.jda.core.requests.RestAction RestAction}
     *         <br>Update RestAction from {@link GuildManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#getRequiredMFALevelField()
     * @see    net.dv8tion.jda.core.managers.GuildManagerUpdatable#update()
     */
    public RestAction<Void> setRequiredMFALevel(Guild.MFALevel level)
    {
        return updatable.getRequiredMFALevelField().setValue(level).update();
    }
}
