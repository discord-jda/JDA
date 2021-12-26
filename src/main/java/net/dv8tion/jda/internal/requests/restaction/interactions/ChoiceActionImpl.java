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

package net.dv8tion.jda.internal.requests.restaction.interactions;

import net.dv8tion.jda.api.interactions.callbacks.IAutoCompleteCallback;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.restaction.interactions.AutoCompleteCallbackAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ChoiceActionImpl extends InteractionCallbackImpl<Void> implements AutoCompleteCallbackAction
{
    private final OptionType type;
    private final Map<String, Command.Choice> choices = new LinkedHashMap<>();

    public ChoiceActionImpl(IAutoCompleteCallback interaction, OptionType type)
    {
        super(interaction);
        this.type = type;
    }

    @NotNull
    @Override
    public OptionType getOptionType()
    {
        return type;
    }

    @NotNull
    @Override
    public AutoCompleteCallbackAction addChoices(@NotNull Collection<Command.Choice> choices)
    {
        Checks.noneNull(choices, "Choices");
        for (Command.Choice choice : choices)
        {
            Checks.notNull(choice.getName(), "Choice Names");
            switch (type)
            {
            case INTEGER:
                Checks.check(choice.getType() == OptionType.INTEGER,
                             "Choice of type %s cannot be converted to INTEGER", choice.getType());
                break;
            case NUMBER:
                Checks.check(choice.getType() == OptionType.NUMBER || choice.getType() == OptionType.INTEGER,
                             "Choice of type %s cannot be converted to NUMBER", choice.getType());
                break;
            // String can be any type, we just toString it
            }
            this.choices.put(choice.getName(), choice);
        }
        return this;
    }

    @Override
    protected DataObject toData()
    {
        DataObject data = DataObject.empty();
        DataArray array = DataArray.empty();
        choices.values().forEach(choice -> {
            DataObject json = DataObject.empty().put("name", choice.getName());
            switch (type)
            {
            case INTEGER:
                json.put("value", choice.getAsLong());
                break;
            case NUMBER:
                json.put("value", choice.getAsDouble());
                break;
            case STRING:
                json.put("value", choice.getAsString());
                break;
            }
            array.add(json);
        });
        data.put("choices", array);
        return DataObject.empty().put("type", 8).put("data", data);
    }
}
