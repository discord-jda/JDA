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

import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.context.ContextInteraction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

import javax.annotation.Nonnull;
import java.util.function.Function;

public abstract class ContextInteractionImpl<T> extends CommandInteractionImpl implements ContextInteraction<T>, CommandInteractionPayloadMixin
{
    private final T target;
    private final CommandInteractionPayloadImpl payload;

    public ContextInteractionImpl(JDAImpl jda, DataObject data, Function<DataObject, T> entityParser)
    {
        super(jda, data);
        this.payload = new CommandInteractionPayloadImpl(jda, data);
        this.target = entityParser.apply(data.getObject("data").getObject("resolved"));
    }

    @Override
    public CommandInteractionPayload getCommandPayload()
    {
        return payload;
    }

    @Nonnull
    @Override
    public T getTarget()
    {
        return target;
    }
}
