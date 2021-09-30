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

package net.dv8tion.jda.api.events.role.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that the Unicode Emoji of a {@link net.dv8tion.jda.api.entities.Role Role} changed.
 *
 * <p>Can be used to detect when a role emoji changes and retrieve the old one
 *
 * <p>Identifier: {@code emoji}
 */
public class RoleUpdateEmojiEvent extends GenericRoleUpdateEvent<String>
{
    public static final String IDENTIFIER = "emoji";

    public RoleUpdateEmojiEvent(@Nonnull JDA api, long responseNumber, @Nonnull Role role, @Nullable String oldEmoji)
    {
        super(api, responseNumber, role, oldEmoji, role.getEmoji(), IDENTIFIER);
    }

    /**
     * The old emoji
     *
     * @return The old emoji, or null
     */
    @Nullable
    public String getOldEmoji()
    {
        return getOldValue();
    }

    /**
     * The old emoji
     *
     * @return The new emoji, or null
     */
    @Nullable
    public String getNewEmoji()
    {
        return getNewValue();
    }
}
