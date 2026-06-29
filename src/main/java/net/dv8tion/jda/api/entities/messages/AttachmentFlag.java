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

package net.dv8tion.jda.api.entities.messages;

import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.channel.concrete.MediaChannel;

import java.util.EnumSet;

import javax.annotation.Nonnull;

/**
 * The flags for a {@link Attachment Message Attachment}.
 */
public enum AttachmentFlag {
    /** This attachment is a clip from a stream */
    IS_CLIP(0),
    /** This attachment is the thumbnail of a thread in a {@link MediaChannel}, displayed in the grid but not on the message */
    IS_THUMBNAIL(1),
    /** This attachment is edited using the remix feature on mobile */
    IS_REMIX(2),
    /** This attachment is marked as a spoiler and is blurred until clicked */
    IS_SPOILER(3),
    /** This attachment is an animated image */
    IS_ANIMATED(5),
    ;

    private final int raw;

    AttachmentFlag(int offset) {
        this.raw = 1 << offset;
    }

    /**
     * The raw value used by Discord for this flag.
     *
     * @return The raw value
     */
    public int getRaw() {
        return raw;
    }

    /**
     * Converts a bitfield to an {@link EnumSet} of {@link AttachmentFlag} values.
     *
     * @param  bitset
     *         the bit field representing a set of attachment flags
     *
     * @return {@link EnumSet} containing the attachment flags corresponding to the specified bit field
     */
    @Nonnull
    public static EnumSet<AttachmentFlag> fromBitField(int bitset) {
        EnumSet<AttachmentFlag> flags = EnumSet.noneOf(AttachmentFlag.class);
        for (AttachmentFlag flag : values()) {
            if ((flag.getRaw() & bitset) == flag.getRaw()) {
                flags.add(flag);
            }
        }
        return flags;
    }
}
