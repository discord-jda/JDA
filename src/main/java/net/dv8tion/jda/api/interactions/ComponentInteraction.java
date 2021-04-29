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

package net.dv8tion.jda.api.interactions;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.InteractionHook;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ComponentInteraction extends Interaction
{
    @Nonnull
    String getComponentId();

    @Nullable
    Component getComponent();

    @Nullable
    Message getMessage();

    long getMessageIdLong();

    @Nonnull
    default String getMessageId()
    {
        return Long.toUnsignedString(getMessageIdLong());
    }

    @Nonnull
    Component.Type getComponentType();

    @Nonnull
    @Override
    MessageChannel getChannel();

    @Nonnull
    @CheckReturnValue // TODO: Support response type 7 (UPDATE_MESSAGE)
    RestAction<InteractionHook> acknowledge();
}
