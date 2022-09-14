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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a Discord Forum Tag.
 * <br>These tags can be applied to forum posts to help categorize them.
 */
public interface ForumTag extends ISnowflake, Comparable<ForumTag>, SerializableData
{
    /**
     * The maximum length of a forum tag name ({@value #MAX_NAME_LENGTH})
     */
    int MAX_NAME_LENGTH = 20;

    /**
     * The tag position, used for sorting.
     *
     * @return The tag position.
     */
    int getPosition();

    /**
     * The name of the tag.
     *
     * @return The name
     */
    @Nonnull
    String getName();

    /**
     * Whether this tag can only be applied by moderators with the {@link net.dv8tion.jda.api.Permission#MANAGE_THREADS MANAGE_THREADS} permission (aka Manage Posts).
     *
     * @return True, if this tag can only be applied by moderators with the required permission
     */
    boolean isModerated();

    /**
     * The emoji used as the tag icon.
     * <br><b>For custom emoji, this will have an empty name and {@link CustomEmoji#isAnimated()} is always {@code false}, due to discord chicanery.</b>
     *
     * @return {@link EmojiUnion} representing the tag emoji, or null if no emoji is applied.
     */
    @Nullable
    EmojiUnion getEmoji();

    @Override
    default int compareTo(@Nonnull ForumTag o)
    {
        Checks.notNull(o, "ForumTag");
        return Integer.compare(getPosition(), o.getPosition());
    }

    @Nonnull
    @Override
    default DataObject toData()
    {
        DataObject json = DataObject.empty()
                .put("id", getId())
                .put("name", getName())
                .put("moderated", isModerated());
        EmojiUnion emoji = getEmoji();
        if (emoji instanceof UnicodeEmoji)
            json.put("emoji_name", emoji.getName());
        else if (emoji instanceof CustomEmoji)
            json.put("emoji_id", ((CustomEmoji) emoji).getId());
        return json;
    }
}
