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

package net.dv8tion.jda.api.interactions.callbacks;

import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.interactions.AutoCompleteCallbackAction;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Interactions which allow auto-complete suggestion replies.
 *
 * <p>Auto-complete must be enabled on {@link OptionData options} via {@link OptionData#setAutoComplete(boolean)}.
 * When a user inputs auto-complete options, you will receive these interactions and can provide up to {@value OptionData#MAX_CHOICES} suggestions for the user to pick from.
 * You can also use {@link #replyChoices(Command.Choice...)} without parameters to acknowledge the interaction with 0 choices.
 */
public interface IAutoCompleteCallback extends Interaction
{
    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * @param  choices
     *         The choice suggestions to present to the user, 0-{@link OptionData#MAX_CHOICES} choices
     *
     * @throws IllegalArgumentException
     *         If any of the following is true:
     *         <ul>
     *             <li>{@code null} is provided</li>
     *             <li>More than {@value OptionData#MAX_CHOICES} are added</li>
     *             <li>Any of the choice names is empty or longer than {@value OptionData#MAX_CHOICE_NAME_LENGTH}</li>
     *             <li>The option type is incompatible with the choice type</li>
     *             <li>The numeric value of any of the choices is not between {@value  OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}</li>
     *             <li>The string value of any of the choices is empty or longer than {@value OptionData#MAX_CHOICE_VALUE_LENGTH}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    AutoCompleteCallbackAction replyChoices(@Nonnull Collection<Command.Choice> choices);

    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * @param  choices
     *         The choice suggestions to present to the user, 0-{@link OptionData#MAX_CHOICES} choices
     *
     * @throws IllegalArgumentException
     *         If any of the following is true:
     *         <ul>
     *             <li>{@code null} is provided</li>
     *             <li>More than {@value OptionData#MAX_CHOICES} are added</li>
     *             <li>Any of the choice names is empty or longer than {@value OptionData#MAX_CHOICE_NAME_LENGTH}</li>
     *             <li>The option type is incompatible with the choice type</li>
     *             <li>The numeric value of any of the choices is not between {@value  OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}</li>
     *             <li>The string value of any of the choices is empty or longer than {@value OptionData#MAX_CHOICE_VALUE_LENGTH}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default AutoCompleteCallbackAction replyChoices(@Nonnull Command.Choice... choices)
    {
        Checks.noneNull(choices, "Choice");
        return replyChoices(Arrays.asList(choices));
    }

    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * @param  name
     *         The choice name to show to the user, 1-{@value OptionData#MAX_CHOICE_NAME_LENGTH} characters
     * @param  value
     *         The choice value, 1-{@value OptionData#MAX_CHOICE_VALUE_LENGTH} characters
     *
     * @throws IllegalArgumentException
     *         If any of the following is true:
     *         <ul>
     *             <li>{@code null} is provided</li>
     *             <li>More than {@value OptionData#MAX_CHOICES} are added</li>
     *             <li>The choice name is empty or longer than {@value OptionData#MAX_CHOICE_NAME_LENGTH}</li>
     *             <li>The option type is not {@link OptionType#STRING}</li>
     *             <li>The string value of any of the choices is empty or longer than {@value OptionData#MAX_CHOICE_VALUE_LENGTH}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default AutoCompleteCallbackAction replyChoice(@Nonnull String name, @Nonnull String value)
    {
        return replyChoices(new Command.Choice(name, value));
    }

    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * @param  name
     *         The choice name to show to the user, 1-{@value OptionData#MAX_CHOICE_NAME_LENGTH} characters
     * @param  value
     *         The choice value, must be between {@value  OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}
     *
     * @throws IllegalArgumentException
     *         If any of the following is true:
     *         <ul>
     *             <li>{@code null} is provided</li>
     *             <li>More than {@value OptionData#MAX_CHOICES} are added</li>
     *             <li>The choice name is empty or longer than {@value OptionData#MAX_CHOICE_NAME_LENGTH}</li>
     *             <li>The option type is incompatible with the choice type</li>
     *             <li>The value of is not between {@value  OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default AutoCompleteCallbackAction replyChoice(@Nonnull String name, long value)
    {
        return replyChoices(new Command.Choice(name, value));
    }

    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * @param  name
     *         The choice name to show to the user, 1-{@value OptionData#MAX_CHOICE_NAME_LENGTH} characters
     * @param  value
     *         The choice value, must be between {@value  OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}
     *
     * @throws IllegalArgumentException
     *         If any of the following is true:
     *         <ul>
     *             <li>{@code null} is provided</li>
     *             <li>More than {@value OptionData#MAX_CHOICES} are added</li>
     *             <li>The choice name is empty or longer than {@value OptionData#MAX_CHOICE_NAME_LENGTH}</li>
     *             <li>The option type is incompatible with the choice type</li>
     *             <li>The value of is not between {@value  OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default AutoCompleteCallbackAction replyChoice(@Nonnull String name, double value)
    {
        return replyChoices(new Command.Choice(name, value));
    }

    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * <p>The provided strings will be used as value and name for the {@link net.dv8tion.jda.api.interactions.commands.Command.Choice Choices}.
     *
     * @param  choices
     *         The choice suggestions to present to the user, each limited to {@value OptionData#MAX_CHOICE_NAME_LENGTH} characters
     *
     * @throws IllegalArgumentException
     *         If any of the following is true:
     *         <ul>
     *             <li>{@code null} is provided</li>
     *             <li>More than {@value OptionData#MAX_CHOICES} are added</li>
     *             <li>Any of the choice names is empty or longer than {@value OptionData#MAX_CHOICE_NAME_LENGTH}</li>
     *             <li>The string value of any of the choices is empty or longer than {@value OptionData#MAX_CHOICE_VALUE_LENGTH}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default AutoCompleteCallbackAction replyChoiceStrings(@Nonnull String... choices)
    {
        return replyChoices(Arrays.stream(choices)
                .map(it -> new Command.Choice(it, it))
                .collect(Collectors.toList()));
    }

    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * <p>The provided strings will be used as value and name for the {@link net.dv8tion.jda.api.interactions.commands.Command.Choice Choices}.
     *
     * @param  choices
     *         The choice suggestions to present to the user, each limited to {@value OptionData#MAX_CHOICE_NAME_LENGTH} characters
     *
     * @throws IllegalArgumentException
     *         If any of the following is true:
     *         <ul>
     *             <li>{@code null} is provided</li>
     *             <li>More than {@value OptionData#MAX_CHOICES} are added</li>
     *             <li>Any of the choice names is empty or longer than {@value OptionData#MAX_CHOICE_NAME_LENGTH}</li>
     *             <li>The string value of any of the choices is empty or longer than {@value OptionData#MAX_CHOICE_VALUE_LENGTH}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default AutoCompleteCallbackAction replyChoiceStrings(@Nonnull Collection<String> choices)
    {
        return replyChoices(choices.stream()
                .map(it -> new Command.Choice(it, it))
                .collect(Collectors.toList()));
    }

    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * <p>The string values of the provided longs will be used as value and name for the {@link net.dv8tion.jda.api.interactions.commands.Command.Choice Choices}.
     *
     * @param  choices
     *         The choice suggestions to present to the user
     *
     * @throws IllegalArgumentException
     *         If any of the following is true:
     *         <ul>
     *             <li>{@code null} is provided</li>
     *             <li>More than {@value OptionData#MAX_CHOICES} are added</li>
     *             <li>The option type is incompatible with the choice type</li>
     *             <li>The numeric value of any of the choices is not between {@value  OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default AutoCompleteCallbackAction replyChoiceLongs(@Nonnull long... choices)
    {
        return replyChoices(Arrays.stream(choices)
                .mapToObj(it -> new Command.Choice(String.valueOf(it), it))
                .collect(Collectors.toList()));
    }

    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * <p>The string values of the provided longs will be used as value and name for the {@link net.dv8tion.jda.api.interactions.commands.Command.Choice Choices}.
     *
     * @param  choices
     *         The choice suggestions to present to the user
     *
     * @throws IllegalArgumentException
     *         If any of the following is true:
     *         <ul>
     *             <li>{@code null} is provided</li>
     *             <li>More than {@value OptionData#MAX_CHOICES} are added</li>
     *             <li>The option type is incompatible with the choice type</li>
     *             <li>The numeric value of any of the choices is not between {@value  OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default AutoCompleteCallbackAction replyChoiceLongs(@Nonnull Collection<Long> choices)
    {
        return replyChoices(choices.stream()
                .map(it -> new Command.Choice(String.valueOf(it), it))
                .collect(Collectors.toList()));
    }

    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * <p>The string values of the provided doubles will be used as value and name for the {@link net.dv8tion.jda.api.interactions.commands.Command.Choice Choices}.
     *
     * @param  choices
     *         The choice suggestions to present to the user
     *
     * @throws IllegalArgumentException
     *         If any of the following is true:
     *         <ul>
     *             <li>{@code null} is provided</li>
     *             <li>More than {@value OptionData#MAX_CHOICES} are added</li>
     *             <li>The option type is incompatible with the choice type</li>
     *             <li>The numeric value of any of the choices is not between {@value  OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default AutoCompleteCallbackAction replyChoiceDoubles(@Nonnull double... choices)
    {
        return replyChoices(Arrays.stream(choices)
                .mapToObj(it -> new Command.Choice(String.valueOf(it), it))
                .collect(Collectors.toList()));
    }

    /**
     * Reply with up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br>The user may continue writing inputs instead of using one of your choices.
     *
     * <p>The string values of the provided doubles will be used as value and name for the {@link net.dv8tion.jda.api.interactions.commands.Command.Choice Choices}.
     *
     * @param  choices
     *         The choice suggestions to present to the user
     *
     * @throws IllegalArgumentException
     *         If any of the following is true:
     *         <ul>
     *             <li>{@code null} is provided</li>
     *             <li>More than {@value OptionData#MAX_CHOICES} are added</li>
     *             <li>The option type is incompatible with the choice type</li>
     *             <li>The numeric value of any of the choices is not between {@value  OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}</li>
     *         </ul>
     *
     * @return {@link AutoCompleteCallbackAction}
     */
    @Nonnull
    @CheckReturnValue
    default AutoCompleteCallbackAction replyChoiceDoubles(@Nonnull Collection<Double> choices)
    {
        return replyChoices(choices.stream()
                .map(it -> new Command.Choice(String.valueOf(it), it))
                .collect(Collectors.toList()));
    }
}
