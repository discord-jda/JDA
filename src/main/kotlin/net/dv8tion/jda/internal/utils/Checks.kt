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

package net.dv8tion.jda.internal.utils

import com.google.errorprone.annotations.FormatMethod
import com.google.errorprone.annotations.FormatString
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.components.Component
import net.dv8tion.jda.api.components.utils.ComponentPathIterator
import net.dv8tion.jda.api.entities.IPermissionHolder
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.detached.IDetachableEntity
import net.dv8tion.jda.api.exceptions.DetachedEntityException
import net.dv8tion.jda.api.exceptions.MissingAccessException
import org.intellij.lang.annotations.PrintFormat
import org.jetbrains.annotations.Contract
import java.time.Duration
import java.util.EnumSet
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.function.BiFunction
import java.util.function.Predicate
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Suppress("TooManyFunctions")
object Checks {
    @JvmField
    val ALPHANUMERIC_WITH_DASH: Pattern = Pattern.compile("[\\w-]+", Pattern.UNICODE_CHARACTER_CLASS)

    @JvmField
    val ALPHANUMERIC: Pattern = Pattern.compile("\\w+", Pattern.UNICODE_CHARACTER_CLASS)

    @JvmField
    val LOWERCASE_ASCII_ALPHANUMERIC: Pattern = Pattern.compile("[a-z0-9_]+")

    private const val MAX_SNOWFLAKE_LENGTH = 20

    @JvmStatic
    @Contract("null -> fail")
    fun isSnowflake(snowflake: String?) {
        isSnowflake(snowflake, snowflake)
    }

    // The message is nullable because the one-argument overload forwards the (possibly null) snowflake as its own name,
    // and Java callers reach notNull() with a null name: that must stay an IllegalArgumentException, not an NPE.
    @JvmStatic
    @Contract("null, _ -> fail")
    fun isSnowflake(
        snowflake: String?,
        message: String?,
    ) {
        notNull(snowflake, message)
        if (snowflake.length > MAX_SNOWFLAKE_LENGTH || !Helpers.isNumeric(snowflake)) {
            throw IllegalArgumentException("$message is not a valid snowflake value! Provided: \"$snowflake\"")
        }
    }

    @JvmStatic
    @Contract("false, _ -> fail")
    fun check(
        expression: Boolean,
        message: String?,
    ) {
        if (!expression) {
            throw IllegalArgumentException(message)
        }
    }

    @JvmStatic
    @FormatMethod
    @Contract("false, _, _ -> fail")
    fun check(
        expression: Boolean,
        @PrintFormat @FormatString message: String,
        vararg args: Any?,
    ) {
        if (!expression) {
            throw IllegalArgumentException(String.format(message, *args))
        }
    }

    @JvmStatic
    @FormatMethod
    @Contract("false, _, _ -> fail")
    fun check(
        expression: Boolean,
        @PrintFormat @FormatString message: String,
        arg: Any?,
    ) {
        if (!expression) {
            throw IllegalArgumentException(String.format(message, arg))
        }
    }

    // The Kotlin contract mirrors @Contract("null, _ -> fail") so converted callers get smart casts instead of `!!`.
    @OptIn(ExperimentalContracts::class)
    @JvmStatic
    @Contract("null, _ -> fail")
    fun notNull(
        argument: Any?,
        name: String?,
    ) {
        contract { returns() implies (argument != null) }
        if (argument == null) {
            throw IllegalArgumentException("$name may not be null")
        }
    }

    @JvmStatic
    @Contract("null, _ -> fail")
    fun notEmpty(
        argument: CharSequence?,
        name: String,
    ) {
        notNull(argument, name)
        if (Helpers.isEmpty(argument)) {
            throw IllegalArgumentException("$name may not be empty")
        }
    }

    @JvmStatic
    @Contract("null, _ -> fail")
    fun notBlank(
        argument: CharSequence?,
        name: String,
    ) {
        notNull(argument, name)
        if (Helpers.isBlank(argument)) {
            throw IllegalArgumentException("$name may not be blank")
        }
    }

    @JvmStatic
    @Contract("null, _ -> fail")
    fun noWhitespace(
        argument: CharSequence?,
        name: String,
    ) {
        notNull(argument, name)
        if (Helpers.containsWhitespace(argument)) {
            throw IllegalArgumentException("$name may not contain blanks. Provided: \"$argument\"")
        }
    }

    @JvmStatic
    @Contract("null, _ -> fail")
    fun notEmpty(
        argument: Collection<*>?,
        name: String,
    ) {
        notNull(argument, name)
        if (argument.isEmpty()) {
            throw IllegalArgumentException("$name may not be empty")
        }
    }

    @JvmStatic
    @Contract("null, _ -> fail")
    fun notEmpty(
        argument: Array<*>?,
        name: String,
    ) {
        notNull(argument, name)
        if (argument.isEmpty()) {
            throw IllegalArgumentException("$name may not be empty")
        }
    }

    @JvmStatic
    @Contract("null, _ -> fail")
    fun noneNull(
        argument: Collection<*>?,
        name: String,
    ) {
        notNull(argument, name)
        argument.forEach { notNull(it, name) }
    }

    @JvmStatic
    @Contract("null, _ -> fail")
    fun noneNull(
        argument: Array<*>?,
        name: String,
    ) {
        notNull(argument, name)
        for (it in argument) {
            notNull(it, name)
        }
    }

    @JvmStatic
    @Contract("null, _ -> fail")
    fun <T : CharSequence> noneEmpty(
        argument: Collection<T>?,
        name: String,
    ) {
        notNull(argument, name)
        argument.forEach { notEmpty(it, name) }
    }

    @JvmStatic
    @Contract("null, _ -> fail")
    fun <T : CharSequence> noneBlank(
        argument: Collection<T>?,
        name: String,
    ) {
        notNull(argument, name)
        argument.forEach { notBlank(it, name) }
    }

    @JvmStatic
    @Contract("null, _ -> fail")
    fun <T : CharSequence> noneContainBlanks(
        argument: Collection<T>?,
        name: String,
    ) {
        notNull(argument, name)
        argument.forEach { noWhitespace(it, name) }
    }

    @JvmStatic
    fun inRange(
        input: String?,
        min: Int,
        max: Int,
        name: String,
    ) {
        notNull(input, name)
        val length = Helpers.codePointLength(input)
        check(
            length in min..max,
            "%s must be between %d and %d characters long! Provided: \"%s\"",
            name,
            min,
            max,
            input,
        )
    }

    @JvmStatic
    fun notLonger(
        input: String?,
        length: Int,
        name: String,
    ) {
        notNull(input, name)
        check(
            Helpers.codePointLength(input) <= length,
            "%s may not be longer than %d characters! Provided: \"%s\"",
            name,
            length,
            input,
        )
    }

    @JvmStatic
    fun matches(
        input: String?,
        pattern: Pattern,
        name: String,
    ) {
        notNull(input, name)
        check(
            pattern.matcher(input).matches(),
            "%s must match regex ^%s$. Provided: \"%s\"",
            name,
            pattern.pattern(),
            input,
        )
    }

    @JvmStatic
    fun isLowercase(
        input: String?,
        name: String,
    ) {
        notNull(input, name)
        check(input.lowercase(Locale.ROOT) == input, "%s must be lowercase only! Provided: \"%s\"", name, input)
    }

    @JvmStatic
    fun positive(
        n: Int,
        name: String,
    ) {
        if (n <= 0) {
            throw IllegalArgumentException("$name may not be negative or zero")
        }
    }

    @JvmStatic
    fun positive(
        n: Long,
        name: String,
    ) {
        if (n <= 0) {
            throw IllegalArgumentException("$name may not be negative or zero")
        }
    }

    @JvmStatic
    fun notNegative(
        n: Int,
        name: String,
    ) {
        if (n < 0) {
            throw IllegalArgumentException("$name may not be negative")
        }
    }

    @JvmStatic
    fun notNegative(
        n: Long,
        name: String,
    ) {
        if (n < 0) {
            throw IllegalArgumentException("$name may not be negative")
        }
    }

    @JvmStatic
    fun notLonger(
        duration: Duration?,
        maxDuration: Duration,
        resolutionUnit: TimeUnit,
        name: String,
    ) {
        notNull(duration, name)
        check(
            duration <= maxDuration,
            "%s may not be longer than %s. Provided: %s",
            name,
            JDALogger.getLazyString { Helpers.durationToString(maxDuration, resolutionUnit) },
            JDALogger.getLazyString { Helpers.durationToString(duration, resolutionUnit) },
        )
    }

    // Unique streams checks

    @JvmStatic
    @Suppress("FormatStringAnnotation")
    fun <T> checkUnique(
        stream: Stream<T>,
        format: String,
        getArgs: BiFunction<Long, T, Array<Any?>>,
    ) {
        val counts: Map<T, Long> = stream.collect(Collectors.groupingBy({ it }, Collectors.counting()))
        for ((key, count) in counts) {
            if (count > 1) {
                val args = getArgs.apply(count, key)
                throw IllegalArgumentException(Helpers.format(format, *args))
            }
        }
    }

    @JvmStatic
    fun checkComponents(
        errorMessage: String,
        components: Collection<Component>,
        predicate: Predicate<Component>,
    ) {
        val sb = StringBuilder()

        ComponentPathIterator
            .createStream("root", components)
            .filter { !predicate.test(it.component) }
            .forEach { sb.append(" - ").append(it.path).append("\n") }

        if (sb.isNotEmpty()) {
            throw IllegalArgumentException(errorMessage + "\n" + sb.toString().trim())
        }
    }

    @JvmStatic
    fun checkComponents(
        errorMessage: String,
        components: Array<Component>,
        predicate: Predicate<Component>,
    ) {
        checkComponents(errorMessage, components.asList(), predicate)
    }

    @JvmStatic
    fun checkComponentType(
        expectedChildrenType: Class<out Component>,
        originalComponent: Component,
        newComponent: Component,
    ) {
        check(
            expectedChildrenType.isInstance(newComponent),
            "%s was replaced by an incompatible component (%s), this layout only supports components of type %s",
            originalComponent,
            newComponent,
            expectedChildrenType.simpleName,
        )
    }

    // Permission checks

    @JvmStatic
    fun checkAccess(
        issuer: IPermissionHolder,
        channel: GuildChannel,
    ) {
        if (issuer.hasAccess(channel)) {
            return
        }

        val perms = issuer.getPermissionsExplicit(channel)
        if (channel is AudioChannel && !perms.contains(Permission.VOICE_CONNECT)) {
            throw MissingAccessException(channel, Permission.VOICE_CONNECT)
        }
        throw MissingAccessException(channel, Permission.VIEW_CHANNEL)
    }

    // Attach checks

    @JvmStatic
    fun checkAttached(entity: IDetachableEntity) {
        if (entity.isDetached) {
            throw DetachedEntityException()
        }
    }

    // Type checks

    @JvmStatic
    fun checkSupportedChannelTypes(
        supported: EnumSet<ChannelType>,
        type: ChannelType,
        what: String,
    ) {
        check(
            supported.contains(type),
            "Can only configure %s for channels of types %s",
            what,
            JDALogger.getLazyString { supported.stream().map { it.name }.collect(Collectors.joining(", ")) },
        )
    }
}
