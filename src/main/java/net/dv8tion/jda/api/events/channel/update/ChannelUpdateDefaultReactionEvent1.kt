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
package net.dv8tion.jda.api.events.channel.update

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.ChannelField
import net.dv8tion.jda.api.entities.channel.attribute.IPostContainer
import net.dv8tion.jda.api.entities.emoji.EmojiUnion
import javax.annotation.Nonnull

/**
 * Indicates that the [default reaction emoji][ForumChannel.getDefaultReaction] of a [IPostContainer] changed.
 *
 *
 * Can be used to retrieve the old default reaction and the new one.
 *
 * @see ChannelField.DEFAULT_REACTION_EMOJI
 */
class ChannelUpdateDefaultReactionEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    @Nonnull channel: IPostContainer,
    oldValue: EmojiUnion?,
    newValue: EmojiUnion?
) : GenericChannelUpdateEvent<EmojiUnion?>(api, responseNumber, channel, FIELD, oldValue, newValue) {
    companion object {
        val FIELD = ChannelField.DEFAULT_REACTION_EMOJI
        val IDENTIFIER = FIELD.fieldName
    }
}
