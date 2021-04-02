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

package net.dv8tion.jda.api.exceptions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;

import javax.annotation.Nonnull;

/**
 * Indicates that the user is missing the {@link Permission#VIEW_CHANNEL VIEW_CHANNEL}
 * or {@link Permission#VOICE_CONNECT VOICE_CONNECT} permission.
 *
 * @see   net.dv8tion.jda.api.entities.IPermissionHolder#hasAccess(GuildChannel)
 *
 * @since 4.2.1
 */
public class MissingAccessException extends InsufficientPermissionException
{
    public MissingAccessException(@Nonnull GuildChannel channel, @Nonnull Permission permission)
    {
        super(channel, permission);
    }

    public MissingAccessException(@Nonnull GuildChannel channel, @Nonnull Permission permission, @Nonnull String reason)
    {
        super(channel, permission, reason);
    }
}
