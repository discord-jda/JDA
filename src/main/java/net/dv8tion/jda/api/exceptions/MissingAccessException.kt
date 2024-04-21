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
package net.dv8tion.jda.api.exceptions

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import javax.annotation.Nonnull

/**
 * Indicates that the user is missing the [VIEW_CHANNEL][Permission.VIEW_CHANNEL],
 * in addition to [VOICE_CONNECT][Permission.VOICE_CONNECT] permission if [Channel.getType] is an [audio][ChannelType.isAudio] type.
 *
 * @see net.dv8tion.jda.api.entities.IPermissionHolder.hasAccess
 * @since 4.2.1
 */
class MissingAccessException : InsufficientPermissionException {
    constructor(@Nonnull channel: GuildChannel, @Nonnull permission: Permission) : super(channel, permission)
    constructor(@Nonnull channel: GuildChannel, @Nonnull permission: Permission, @Nonnull reason: String) : super(
        channel,
        permission,
        reason
    )
}
