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

package net.dv8tion.jda.internal.interactions.command;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface CommandInteractionPayloadMixin extends CommandInteractionPayload
{
    CommandInteractionPayload getCommandPayload();

    @Nonnull
    @Override
    default Command.Type getCommandType()
    {
        return getCommandPayload().getCommandType();
    }

    @Nonnull
    @Override
    default String getName()
    {
        return getCommandPayload().getName();
    }

    @Nullable
    @Override
    default String getSubcommandName()
    {
        return getCommandPayload().getSubcommandName();
    }

    @Nullable
    @Override
    default String getSubcommandGroup()
    {
        return getCommandPayload().getSubcommandGroup();
    }

    @Override
    default long getCommandIdLong()
    {
        return getCommandPayload().getCommandIdLong();
    }

    @Nonnull
    @Override
    default List<OptionMapping> getOptions()
    {
        return getCommandPayload().getOptions();
    }
}
