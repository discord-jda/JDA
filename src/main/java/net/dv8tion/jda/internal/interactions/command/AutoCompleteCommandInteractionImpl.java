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

import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import net.dv8tion.jda.api.interactions.commands.CommandPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.interactions.InteractionImpl;
import org.jetbrains.annotations.NotNull;

public class AutoCompleteCommandInteractionImpl extends InteractionImpl implements CommandPayloadMixin, CommandAutoCompleteInteraction
{
    private final CommandPayload payload;
    private OptionMapping focused;

    public AutoCompleteCommandInteractionImpl(JDAImpl jda, DataObject data)
    {
        super(jda, data);
        this.payload = new CommandPayloadImpl(jda, data);

        DataArray options = data.getObject("data").getArray("options");
        for (int i = 0; i < options.length(); i++)
        {
            DataObject option = options.getObject(i);
            if (option.getBoolean("focused"))
            {
                focused = getOption(option.getString("name"));
                break;
            }
        }

        if (focused == null)
            throw new IllegalStateException("Failed to get focused option for auto complete interaction");
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
}
