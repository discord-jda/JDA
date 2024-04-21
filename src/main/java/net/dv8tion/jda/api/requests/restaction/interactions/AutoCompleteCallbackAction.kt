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
package net.dv8tion.jda.api.requests.restaction.interactions

import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.requests.FluentRestAction
import net.dv8tion.jda.internal.utils.Checks
import java.util.*
import java.util.stream.Collectors
import javax.annotation.CheckReturnValue
import javax.annotation.Nonnull

/**
 * An [InteractionCallbackAction] that can be used to suggest auto-complete choices.
 *
 * @see OptionData.setAutoComplete
 *
 * @see IAutoCompleteCallback
 *
 * @see CommandAutoCompleteInteraction
 */
interface AutoCompleteCallbackAction : InteractionCallbackAction<Void?>,
    FluentRestAction<Void?, AutoCompleteCallbackAction?> {
    @get:Nonnull
    val optionType: OptionType?

    /**
     * Add up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br></br>The user may continue writing inputs instead of using one of your choices.
     *
     * @param  choices
     * The choice suggestions to present to the user, 0-[OptionData.MAX_CHOICES] choices
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If more than {@value OptionData#MAX_CHOICES} choices are added
     *  * If any of the choice names are empty or longer than {@value OptionData#MAX_CHOICE_NAME_LENGTH}
     *  * If the option type is incompatible with the choice type
     *  * If the numeric value of any of the choices is not between {@value OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}
     *  * If the string value of any of the choices is empty or longer than {@value OptionData#MAX_CHOICE_VALUE_LENGTH}
     *
     *
     * @return The same callback action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun addChoices(@Nonnull choices: Collection<Command.Choice?>?): AutoCompleteCallbackAction?

    /**
     * Add up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br></br>The user may continue writing inputs instead of using one of your choices.
     *
     * @param  choices
     * The choice suggestions to present to the user, 0-[OptionData.MAX_CHOICES] choices
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If more than {@value OptionData#MAX_CHOICES} choices are added
     *  * If any of the choice names are empty or longer than {@value OptionData#MAX_CHOICE_NAME_LENGTH}
     *  * If the option type is incompatible with the choice type
     *  * If the numeric value of any of the choices is not between {@value OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}
     *  * If the string value of any of the choices is empty or longer than {@value OptionData#MAX_CHOICE_VALUE_LENGTH}
     *
     *
     * @return The same callback action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun addChoices(@Nonnull vararg choices: Command.Choice?): AutoCompleteCallbackAction? {
        Checks.noneNull(choices, "Choices")
        return addChoices(Arrays.asList(*choices))
    }

    /**
     * Add up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br></br>The user may continue writing inputs instead of using one of your choices.
     *
     * @param  name
     * The choice name to show to the user, 1-{@value OptionData#MAX_CHOICE_NAME_LENGTH} characters
     * @param  value
     * The choice value, 1-{@value OptionData#MAX_CHOICE_VALUE_LENGTH} characters
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If more than {@value OptionData#MAX_CHOICES} choices are added
     *  * If the choice name is empty or longer than {@value OptionData#MAX_CHOICE_NAME_LENGTH}
     *  * If the option type is not [OptionType.STRING]
     *  * If the value is empty or longer than {@value OptionData#MAX_CHOICE_VALUE_LENGTH}
     *
     *
     * @return The same callback action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun addChoice(@Nonnull name: String?, @Nonnull value: String?): AutoCompleteCallbackAction? {
        return addChoices(Command.Choice(name!!, value!!))
    }

    /**
     * Add up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br></br>The user may continue writing inputs instead of using one of your choices.
     *
     * @param  name
     * The choice name to show to the user, 1-{@value OptionData#MAX_CHOICE_NAME_LENGTH} characters
     * @param  value
     * The choice value, must be between {@value OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If more than {@value OptionData#MAX_CHOICES} choices are added
     *  * If the choice name is empty or longer than {@value OptionData#MAX_CHOICE_NAME_LENGTH}
     *  * If the option type is incompatible with the choice type
     *  * If the value is not between {@value OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}
     *
     *
     * @return The same callback action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun addChoice(@Nonnull name: String?, value: Long): AutoCompleteCallbackAction? {
        return addChoices(Command.Choice(name!!, value))
    }

    /**
     * Add up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br></br>The user may continue writing inputs instead of using one of your choices.
     *
     * @param  name
     * The choice name to show to the user, 1-{@value OptionData#MAX_CHOICE_NAME_LENGTH} characters
     * @param  value
     * The choice value, must be between {@value OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If more than {@value OptionData#MAX_CHOICES} choices are added
     *  * If the choice name is empty or longer than {@value OptionData#MAX_CHOICE_NAME_LENGTH}
     *  * If the option type is incompatible with the choice type
     *  * If the value is not between {@value OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}
     *
     *
     * @return The same callback action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun addChoice(@Nonnull name: String?, value: Double): AutoCompleteCallbackAction? {
        return addChoices(Command.Choice(name!!, value))
    }

    /**
     * Add up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br></br>The user may continue writing inputs instead of using one of your choices.
     *
     *
     * The provided strings will be used as value and name for the [Choices][net.dv8tion.jda.api.interactions.commands.Command.Choice].
     *
     * @param  choices
     * The choice suggestions to present to the user, each limited to {@value OptionData#MAX_CHOICE_NAME_LENGTH} characters
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If more than {@value OptionData#MAX_CHOICES} choices are added
     *  * If any of the choice names are empty or longer than {@value OptionData#MAX_CHOICE_NAME_LENGTH}
     *  * If the string value of any of the choices is empty or longer than {@value OptionData#MAX_CHOICE_VALUE_LENGTH}
     *
     *
     * @return The same callback action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun addChoiceStrings(@Nonnull vararg choices: String): AutoCompleteCallbackAction? {
        return addChoices(Arrays.stream(choices)
            .map { it: String? ->
                Command.Choice(
                    it!!, it
                )
            }
            .collect(Collectors.toList()))
    }

    /**
     * Add up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br></br>The user may continue writing inputs instead of using one of your choices.
     *
     *
     * The provided strings will be used as value and name for the [Choices][net.dv8tion.jda.api.interactions.commands.Command.Choice].
     *
     * @param  choices
     * The choice suggestions to present to the user, each limited to {@value OptionData#MAX_CHOICE_NAME_LENGTH} characters
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If more than {@value OptionData#MAX_CHOICES} choices are added
     *  * If any of the choice names are empty or longer than {@value OptionData#MAX_CHOICE_NAME_LENGTH}
     *  * If the string value of any of the choices is empty or longer than {@value OptionData#MAX_CHOICE_VALUE_LENGTH}
     *
     *
     * @return The same callback action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun addChoiceStrings(@Nonnull choices: Collection<String?>): AutoCompleteCallbackAction? {
        return addChoices(choices.stream()
            .map { it: String? ->
                Command.Choice(
                    it!!, it
                )
            }
            .collect(Collectors.toList()))
    }

    /**
     * Add up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br></br>The user may continue writing inputs instead of using one of your choices.
     *
     *
     * The string values of the provided longs will be used as value and name for the [Choices][net.dv8tion.jda.api.interactions.commands.Command.Choice].
     *
     * @param  choices
     * The choice suggestions to present to the user
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If more than {@value OptionData#MAX_CHOICES} choices are added
     *  * If the option type is incompatible with the choice type
     *  * If the numeric value of any of the choices is not between {@value OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}
     *
     *
     * @return The same callback action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun addChoiceLongs(@Nonnull vararg choices: Long): AutoCompleteCallbackAction? {
        return addChoices(Arrays.stream(choices)
            .mapToObj { it: Long -> Command.Choice(it.toString(), it) }
            .collect(Collectors.toList()))
    }

    /**
     * Add up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br></br>The user may continue writing inputs instead of using one of your choices.
     *
     *
     * The string values of the provided longs will be used as value and name for the [Choices][net.dv8tion.jda.api.interactions.commands.Command.Choice].
     *
     * @param  choices
     * The choice suggestions to present to the user
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If more than {@value OptionData#MAX_CHOICES} choices are added
     *  * If the option type is incompatible with the choice type
     *  * If the numeric value of any of the choices is not between {@value OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}
     *
     *
     * @return The same callback action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun addChoiceLongs(@Nonnull choices: Collection<Long>): AutoCompleteCallbackAction? {
        return addChoices(choices.stream()
            .map { it: Long -> Command.Choice(it.toString(), it) }
            .collect(Collectors.toList()))
    }

    /**
     * Add up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br></br>The user may continue writing inputs instead of using one of your choices.
     *
     *
     * The string values of the provided doubles will be used as value and name for the [Choices][net.dv8tion.jda.api.interactions.commands.Command.Choice].
     *
     * @param  choices
     * The choice suggestions to present to the user
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If more than {@value OptionData#MAX_CHOICES} choices are added
     *  * If the option type is incompatible with the choice type
     *  * If the numeric value of any of the choices is not between {@value OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}
     *
     *
     * @return The same callback action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun addChoiceDoubles(@Nonnull vararg choices: Double): AutoCompleteCallbackAction? {
        return addChoices(Arrays.stream(choices)
            .mapToObj { it: Double -> Command.Choice(it.toString(), it) }
            .collect(Collectors.toList()))
    }

    /**
     * Add up to {@value OptionData#MAX_CHOICES} choices which can be picked from by the user.
     * <br></br>The user may continue writing inputs instead of using one of your choices.
     *
     *
     * The string values of the provided doubles will be used as value and name for the [Choices][net.dv8tion.jda.api.interactions.commands.Command.Choice].
     *
     * @param  choices
     * The choice suggestions to present to the user
     *
     * @throws IllegalArgumentException
     *
     *  * If `null` is provided
     *  * If more than {@value OptionData#MAX_CHOICES} choices are added
     *  * If the option type is incompatible with the choice type
     *  * If the numeric value of any of the choices is not between {@value OptionData#MIN_NEGATIVE_NUMBER} and {@value OptionData#MAX_POSITIVE_NUMBER}
     *
     *
     * @return The same callback action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    fun addChoiceDoubles(@Nonnull choices: Collection<Double>): AutoCompleteCallbackAction? {
        return addChoices(choices.stream()
            .map { it: Double -> Command.Choice(it.toString(), it) }
            .collect(Collectors.toList()))
    }
}
