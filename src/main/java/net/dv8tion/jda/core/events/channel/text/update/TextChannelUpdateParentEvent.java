/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.events.channel.text.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * Indicates that a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}'s parent changed.
 *
 * <p>Can be used to detect that the parent of a TextChannel changes.
 *
 * <p>Identifier: {@code parent}
 */
public class TextChannelUpdateParentEvent extends GenericTextChannelUpdateEvent<Category>
{
    public static final String IDENTIFIER = "parent";

    public TextChannelUpdateParentEvent(JDA api, long responseNumber, TextChannel channel, Category oldParent)
    {
        super(api, responseNumber, channel, oldParent, channel.getParent(), IDENTIFIER);
    }

    /**
     * The old parent {@link net.dv8tion.jda.core.entities.Category Category}
     *
     * @return The old parent category, or null
     */
    public Category getOldParent()
    {
        return getOldValue();
    }

    /**
     * The new parent {@link net.dv8tion.jda.core.entities.Category Category}
     *
     * @return The new parent category, or null
     */
    public Category getNewParent()
    {
        return getNewValue();
    }
}
