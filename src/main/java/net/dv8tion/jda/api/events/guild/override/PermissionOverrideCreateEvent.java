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

package net.dv8tion.jda.api.events.guild.override;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link PermissionOverride} in a {@link IPermissionContainer guild channel} has been created.
 *
 * <p>Can be used to retrieve the new override.
 *
 * <p><b>Note:</b> This event will also be fired when the {@link net.dv8tion.jda.api.entities.Guild#getPublicRole() @everyone} override
 * is modified, but previously had no allowed/denied permissions.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>These events require {@link CacheFlag#MEMBER_OVERRIDES} to be enabled for member overrides,
 * unless the member is the {@link net.dv8tion.jda.api.entities.Guild#getSelfMember() self member}.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disables this by default!
 */
public class PermissionOverrideCreateEvent extends GenericPermissionOverrideEvent
{
    public PermissionOverrideCreateEvent(@Nonnull JDA api, long responseNumber, @Nonnull IPermissionContainer channel, @Nonnull PermissionOverride override)
    {
        super(api, responseNumber, channel, override);
    }
}
