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

package net.dv8tion.jda.api.requests.restaction.interactions;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public interface AutoCompleteCallbackAction extends InteractionCallbackAction<Void>
{
    @Nonnull
    OptionType getOptionType();

    @Nonnull
    @CheckReturnValue
    AutoCompleteCallbackAction addChoices(@Nonnull Collection<Command.Choice> choices);

    @Nonnull
    @CheckReturnValue
    default AutoCompleteCallbackAction addChoices(@Nonnull Command.Choice... choices)
    {
        Checks.noneNull(choices, "Choices");
        return addChoices(Arrays.asList(choices));
    }

    @Nonnull
    @CheckReturnValue
    default AutoCompleteCallbackAction addChoice(@Nonnull String name, @Nonnull String value)
    {
        return addChoices(new Command.Choice(name, value));
    }

    @Nonnull
    @CheckReturnValue
    default AutoCompleteCallbackAction addChoice(@Nonnull String name, long value)
    {
        return addChoices(new Command.Choice(name, value));
    }

    @Nonnull
    @CheckReturnValue
    default AutoCompleteCallbackAction addChoice(@Nonnull String name, double value)
    {
        return addChoices(new Command.Choice(name, value));
    }

    @Nonnull
    @CheckReturnValue
    default AutoCompleteCallbackAction addChoiceStrings(@Nonnull String... choices)
    {
        return addChoices(Arrays.stream(choices)
                .map(it -> new Command.Choice(it, it))
                .collect(Collectors.toList()));
    }

    @Nonnull
    @CheckReturnValue
    default AutoCompleteCallbackAction addChoiceStrings(@Nonnull Collection<String> choices)
    {
        return addChoices(choices.stream()
                .map(it -> new Command.Choice(it, it))
                .collect(Collectors.toList()));
    }

    @Nonnull
    @CheckReturnValue
    default AutoCompleteCallbackAction addChoiceLongs(@Nonnull long... choices)
    {
        return addChoices(Arrays.stream(choices)
                .mapToObj(it -> new Command.Choice(String.valueOf(it), it))
                .collect(Collectors.toList()));
    }

    @Nonnull
    @CheckReturnValue
    default AutoCompleteCallbackAction addChoiceLongs(@Nonnull Collection<Long> choices)
    {
        return addChoices(choices.stream()
                .map(it -> new Command.Choice(String.valueOf(it), it))
                .collect(Collectors.toList()));
    }

    @Nonnull
    @CheckReturnValue
    default AutoCompleteCallbackAction addChoiceDoubles(@Nonnull double... choices)
    {
        return addChoices(Arrays.stream(choices)
                .mapToObj(it -> new Command.Choice(String.valueOf(it), it))
                .collect(Collectors.toList()));
    }

    @Nonnull
    @CheckReturnValue
    default AutoCompleteCallbackAction addChoiceDoubles(@Nonnull Collection<Double> choices)
    {
        return addChoices(choices.stream()
                .map(it -> new Command.Choice(String.valueOf(it), it))
                .collect(Collectors.toList()));
    }
}
