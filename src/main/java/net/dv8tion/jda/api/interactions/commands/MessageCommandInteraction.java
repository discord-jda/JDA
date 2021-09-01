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

package net.dv8tion.jda.api.interactions.commands;

import net.dv8tion.jda.api.entities.AbstractChannel;
import net.dv8tion.jda.api.entities.Message;

import javax.annotation.Nonnull;

public interface MessageCommandInteraction extends CommandInteraction
{

    /**
     * The id of the message clicked on.
     *
     * @return The id
     */
    long getInteractedIdLong();

    /**
     * The id of the message clicked on.
     *
     * @return The id
     */
    @Nonnull
    default String getInteractedId() {
        return Long.toUnsignedString(getInteractedIdLong());
    }

    /**
     * The {@link Message} clicked on.
     *
     * @return The {@link Message} clicked on
     */
    @Nonnull
    Message getInteractedMessage();

    @Nonnull
    @Override
    AbstractChannel getChannel();
}
