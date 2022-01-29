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

import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.requests.restaction.interactions.AutoCompleteCallbackAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.interactions.InteractionImpl;
import net.dv8tion.jda.internal.requests.restaction.interactions.AutoCompleteCallbackActionImpl;

import javax.annotation.Nonnull;
import java.util.Collection;

public class CommandAutoCompleteInteractionImpl extends InteractionImpl implements CommandInteractionPayloadMixin, CommandAutoCompleteInteraction
{
    private final CommandInteractionPayload payload;
    private AutoCompleteQuery focused;

    public CommandAutoCompleteInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);
        this.payload = new CommandInteractionPayloadImpl(jda, data);

        DataArray options = data.getObject("data").getArray("options");
        findFocused(options);

        if (focused == null)
            throw new IllegalStateException("Failed to get focused option for auto complete interaction");
    }

    private void findFocused(DataArray options)
    {
        for (int i = 0; i < options.length(); i++)
        {
            DataObject option = options.getObject(i);
            switch (OptionType.fromKey(option.getInt("type")))
            {
            case SUB_COMMAND:
            case SUB_COMMAND_GROUP:
                findFocused(option.getArray("options"));
                break;
            default:
                if (option.getBoolean("focused"))
                {
                    OptionMapping opt = getOption(option.getString("name"));
                    focused = new AutoCompleteQuery(opt);
                    break;
                }
            }
        }
    }

    @Nonnull
    @Override
    public AutoCompleteQuery getFocusedOption()
    {
        return focused;
    }

    @Override
    public CommandInteractionPayload getCommandPayload()
    {
        return payload;
    }

    @Nonnull
    @Override
    public AutoCompleteCallbackAction replyChoices(@Nonnull Collection<Command.Choice> choices)
    {
        return new AutoCompleteCallbackActionImpl(this, focused.getType()).addChoices(choices);
    }
}
