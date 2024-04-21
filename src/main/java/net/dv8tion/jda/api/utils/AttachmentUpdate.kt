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
package net.dv8tion.jda.api.utils

import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.api.utils.data.DataObject.Companion.empty
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.EntityString
import javax.annotation.Nonnull

/**
 * Represents existing message attachment.
 * <br></br>This is primarily used for message edit requests, to specify which attachments to retain in the message after the update.
 */
class AttachmentUpdate protected constructor(
    override val idLong: Long,
    /**
     * The existing attachment filename.
     *
     * @return The filename, or `null` if not provided
     */
    val name: String?
) : AttachedFile, ISnowflake {

    override fun addPart(@Nonnull builder: Builder, index: Int) {}
    @Nonnull
    override fun toAttachmentData(index: Int): DataObject {
        val `object` = empty().put("id", idLong)
        if (name != null) `object`.put("filename", name)
        return `object`
    }

    override fun close() {}
    override fun forceClose() {}
    override fun toString(): String {
        val entityString = EntityString("AttachedFile").setType("Attachment")
        if (name != null) entityString.setName(name)
        return entityString.toString()
    }

    companion object {
        /**
         * Creates an [AttachmentUpdate] with the given attachment id.
         * <br></br>This is primarily used for message edit requests, to specify which attachments to retain in the message after the update.
         *
         * @param  id
         * The id of the attachment to retain
         *
         * @return [AttachmentUpdate]
         */
        @Nonnull
        fun fromAttachment(id: Long): AttachmentUpdate {
            return AttachmentUpdate(id, null)
        }

        /**
         * Creates an [AttachmentUpdate] with the given attachment id.
         * <br></br>This is primarily used for message edit requests, to specify which attachments to retain in the message after the update.
         *
         * @param  id
         * The id of the attachment to retain
         *
         * @throws IllegalArgumentException
         * If the id is not a valid snowflake
         *
         * @return [AttachmentUpdate]
         */
        @Nonnull
        fun fromAttachment(@Nonnull id: String): AttachmentUpdate {
            return fromAttachment(MiscUtil.parseSnowflake(id))
        }

        /**
         * Creates an [AttachmentUpdate] with the given attachment.
         * <br></br>This is primarily used for message edit requests, to specify which attachments to retain in the message after the update.
         *
         * @param  attachment
         * The attachment to retain
         *
         * @return [AttachmentUpdate]
         */
        @Nonnull
        fun fromAttachment(@Nonnull attachment: Message.Attachment): AttachmentUpdate {
            Checks.notNull(attachment, "Attachment")
            return AttachmentUpdate(attachment.getIdLong(), attachment.fileName)
        }
    }
}
