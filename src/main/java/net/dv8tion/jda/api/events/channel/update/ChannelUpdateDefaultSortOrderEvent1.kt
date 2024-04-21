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
import javax.annotation.Nonnull

/**
 * Indicates that the [default sort order][IPostContainer.getDefaultSortOrder] of a [IPostContainer] changed.
 *
 *
 * Can be used to retrieve the old default sort order and the new one.
 *
 * @see ChannelField.DEFAULT_SORT_ORDER
 */
class ChannelUpdateDefaultSortOrderEvent(
    @Nonnull api: JDA,
    responseNumber: Long,
    channel: IPostContainer,
    oldValue: IPostContainer.SortOrder?
) : GenericChannelUpdateEvent<IPostContainer.SortOrder?>(
    api,
    responseNumber,
    channel,
    ChannelField.DEFAULT_SORT_ORDER,
    oldValue,
    channel.defaultSortOrder
) {
    @Nonnull
    override fun getOldValue(): IPostContainer.SortOrder? {
        return super.getOldValue()
    }

    @Nonnull
    override fun getNewValue(): IPostContainer.SortOrder? {
        return super.getNewValue()
    }

    companion object {
        val FIELD = ChannelField.DEFAULT_SORT_ORDER
        val IDENTIFIER = FIELD.fieldName
    }
}
