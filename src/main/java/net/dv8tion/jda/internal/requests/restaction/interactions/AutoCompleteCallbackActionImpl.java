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
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.interactions.AutoCompleteCallbackAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.InteractionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AutoCompleteCallbackActionImpl extends InteractionCallbackImpl<Void> implements AutoCompleteCallbackAction
{
    private final OptionType type;
    private final List<Command.Choice> choices = new ArrayList<>(26);

    public AutoCompleteCallbackActionImpl(IAutoCompleteCallback interaction, OptionType type)
    {
        super((InteractionImpl) interaction);
        this.type = type;
    }

    @Nonnull
    @Override
    public OptionType getOptionType()
    {
        return type;
    }

    @Nonnull
    @Override
    public AutoCompleteCallbackAction addChoices(@Nonnull Collection<Command.Choice> choices)
    {
        Checks.noneNull(choices, "Choices");
        Checks.check(choices.size() + this.choices.size() <= OptionData.MAX_CHOICES,
                     "Can only reply with up to %d choices. Limit your suggestions!", OptionData.MAX_CHOICES);
        for (Command.Choice choice : choices)
        {
            Checks.inRange(choice.getName(), 1, OptionData.MAX_CHOICE_NAME_LENGTH, "Choice name");

            switch (type)
            {
            case INTEGER:
                Checks.check(choice.getType() == OptionType.INTEGER,
                             "Choice of type %s cannot be converted to INTEGER", choice.getType());
                long valueLong = choice.getAsLong();
                Checks.check(valueLong <= OptionData.MAX_POSITIVE_NUMBER,
                             "Choice value cannot be larger than %d Provided: %d",
                             OptionData.MAX_POSITIVE_NUMBER, valueLong);
                Checks.check(valueLong >= OptionData.MIN_NEGATIVE_NUMBER,
                             "Choice value cannot be smaller than %d. Provided: %d",
                             OptionData.MIN_NEGATIVE_NUMBER, valueLong);
                break;
            case NUMBER:
                Checks.check(choice.getType() == OptionType.NUMBER || choice.getType() == OptionType.INTEGER,
                             "Choice of type %s cannot be converted to NUMBER", choice.getType());
                double valueDouble = choice.getAsDouble();
                Checks.check(valueDouble <= OptionData.MAX_POSITIVE_NUMBER,
                             "Choice value cannot be larger than %d Provided: %d",
                             OptionData.MAX_POSITIVE_NUMBER, valueDouble);
                Checks.check(valueDouble >= OptionData.MIN_NEGATIVE_NUMBER,
                             "Choice value cannot be smaller than %d. Provided: %d",
                             OptionData.MIN_NEGATIVE_NUMBER, valueDouble);
                break;
            case STRING:
                // String can be any type, we just toString it
                String valueString = choice.getAsString();
                Checks.inRange(valueString, 1, OptionData.MAX_CHOICE_VALUE_LENGTH, "Choice value");
                break;
            }
        }
        this.choices.addAll(choices);
        return this;
    }

    @Override
    protected DataObject toData()
    {
        DataObject data = DataObject.empty();
        DataArray array = DataArray.empty();
        choices.forEach(choice -> {
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
        return DataObject.empty()
                .put("type", ResponseType.COMMAND_AUTOCOMPLETE_CHOICES.getRaw())
                .put("data", data);
    }
}
