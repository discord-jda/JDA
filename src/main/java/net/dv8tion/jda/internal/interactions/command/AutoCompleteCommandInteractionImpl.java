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

import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.requests.restaction.interactions.ChoiceAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.interactions.InteractionImpl;
import net.dv8tion.jda.internal.requests.restaction.interactions.ChoiceActionImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collection;

public class AutoCompleteCommandInteractionImpl extends InteractionImpl implements CommandPayloadMixin, CommandAutoCompleteInteraction
{
    private final CommandPayload payload;
    private OptionMapping focused;

    public AutoCompleteCommandInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);
        this.payload = new CommandPayloadImpl(jda, data);

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
                    focused = getOption(option.getString("name"));
                    break;
                }
            }
        }
    }

    @NotNull
    @Override
    public OptionMapping getFocusedOption()
    {
        return focused;
    }

    @Override
    public CommandPayload getCommandPayload()
    {
        return payload;
    }

    @Nonnull
    @Override
    public ChoiceAction replyChoices(@Nonnull Collection<Command.Choice> choices)
    {
        return new ChoiceActionImpl(this, focused.getType()).addChoices(choices);
    }
}
