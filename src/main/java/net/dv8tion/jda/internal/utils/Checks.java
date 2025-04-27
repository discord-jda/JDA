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

package net.dv8tion.jda.internal.utils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.detached.IDetachableEntity;
import net.dv8tion.jda.api.exceptions.DetachedEntityException;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import org.intellij.lang.annotations.PrintFormat;
import org.jetbrains.annotations.Contract;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Checks
{
    public static final Pattern ALPHANUMERIC_WITH_DASH = Pattern.compile("[\\w-]+", Pattern.UNICODE_CHARACTER_CLASS);
    public static final Pattern ALPHANUMERIC = Pattern.compile("\\w+", Pattern.UNICODE_CHARACTER_CLASS);
    public static final Pattern LOWERCASE_ASCII_ALPHANUMERIC = Pattern.compile("[a-z0-9_]+");

    @Contract("null -> fail")
    public static void isSnowflake(final String snowflake)
    {
        isSnowflake(snowflake, snowflake);
    }

    @Contract("null, _ -> fail")
    public static void isSnowflake(final String snowflake, final String message)
    {
        notNull(snowflake, message);
        if (snowflake.length() > 20 || !Helpers.isNumeric(snowflake))
            throw new IllegalArgumentException(message + " is not a valid snowflake value! Provided: \"" + snowflake + "\"");
    }

    @Contract("false, _ -> fail")
    public static void check(final boolean expression, final String message)
    {
        if (!expression)
            throw new IllegalArgumentException(message);
    }

    @Contract("false, _, _ -> fail")
    public static void check(final boolean expression, @PrintFormat final String message, final Object... args)
    {
        if (!expression)
            throw new IllegalArgumentException(String.format(message, args));
    }

    @Contract("false, _, _ -> fail")
    public static void check(final boolean expression, @PrintFormat final String message, final Object arg)
    {
        if (!expression)
            throw new IllegalArgumentException(String.format(message, arg));
    }

    @Contract("null, _ -> fail")
    public static void notNull(final Object argument, final String name)
    {
        if (argument == null)
            throw new IllegalArgumentException(name + " may not be null");
    }

    @Contract("null, _ -> fail")
    public static void notEmpty(final CharSequence argument, final String name)
    {
        notNull(argument, name);
        if (Helpers.isEmpty(argument))
            throw new IllegalArgumentException(name + " may not be empty");
    }

    @Contract("null, _ -> fail")
    public static void notBlank(final CharSequence argument, final String name)
    {
        notNull(argument, name);
        if (Helpers.isBlank(argument))
            throw new IllegalArgumentException(name + " may not be blank");
    }

    @Contract("null, _ -> fail")
    public static void noWhitespace(final CharSequence argument, final String name)
    {
        notNull(argument, name);
        if (Helpers.containsWhitespace(argument))
            throw new IllegalArgumentException(name + " may not contain blanks. Provided: \"" + argument + "\"");
    }

    @Contract("null, _ -> fail")
    public static void notEmpty(final Collection<?> argument, final String name)
    {
        notNull(argument, name);
        if (argument.isEmpty())
            throw new IllegalArgumentException(name + " may not be empty");
    }

    @Contract("null, _ -> fail")
    public static void notEmpty(final Object[] argument, final String name)
    {
        notNull(argument, name);
        if (argument.length == 0)
            throw new IllegalArgumentException(name + " may not be empty");
    }

    @Contract("null, _ -> fail")
    public static void noneNull(final Collection<?> argument, final String name)
    {
        notNull(argument, name);
        argument.forEach(it -> notNull(it, name));
    }

    @Contract("null, _ -> fail")
    public static void noneNull(final Object[] argument, final String name)
    {
        notNull(argument, name);
        for (Object it : argument) {
            notNull(it, name);
        }
    }

    @Contract("null, _ -> fail")
    public static <T extends CharSequence> void noneEmpty(final Collection<T> argument, final String name)
    {
        notNull(argument, name);
        argument.forEach(it -> notEmpty(it, name));
    }

    @Contract("null, _ -> fail")
    public static <T extends CharSequence> void noneBlank(final Collection<T> argument, final String name)
    {
        notNull(argument, name);
        argument.forEach(it -> notBlank(it, name));
    }

    @Contract("null, _ -> fail")
    public static <T extends CharSequence> void noneContainBlanks(final Collection<T> argument, final String name)
    {
        notNull(argument, name);
        argument.forEach(it -> noWhitespace(it, name));
    }

    public static void inRange(final String input, final int min, final int max, final String name)
    {
        notNull(input, name);
        int length = Helpers.codePointLength(input);
        check(min <= length && length <= max,
                "%s must be between %d and %d characters long! Provided: \"%s\"",
                name, min, max, input);
    }

    public static void notLonger(final String input, final int length, final String name)
    {
        notNull(input, name);
        check(Helpers.codePointLength(input) <= length, "%s may not be longer than %d characters! Provided: \"%s\"", name, length, input);
    }

    public static void matches(final String input, final Pattern pattern, final String name)
    {
        notNull(input, name);
        check(pattern.matcher(input).matches(), "%s must match regex ^%s$. Provided: \"%s\"", name, pattern.pattern(), input);
    }

    public static void isLowercase(final String input, final String name)
    {
        notNull(input, name);
        check(input.toLowerCase(Locale.ROOT).equals(input), "%s must be lowercase only! Provided: \"%s\"", name, input);
    }

    public static void positive(final int n, final String name)
    {
        if (n <= 0)
            throw new IllegalArgumentException(name + " may not be negative or zero");
    }

    public static void positive(final long n, final String name)
    {
        if (n <= 0)
            throw new IllegalArgumentException(name + " may not be negative or zero");
    }

    public static void notNegative(final int n, final String name)
    {
        if (n < 0)
            throw new IllegalArgumentException(name + " may not be negative");
    }

    public static void notNegative(final long n, final String name)
    {
        if (n < 0)
            throw new IllegalArgumentException(name + " may not be negative");
    }

    public static void notLonger(final Duration duration, final Duration maxDuration, final TimeUnit resolutionUnit, final String name)
    {
        notNull(duration, name);
        check(
            duration.compareTo(maxDuration) <= 0,
           "%s may not be longer than %s. Provided: %s",
            name,
            JDALogger.getLazyString(() -> Helpers.durationToString(maxDuration, resolutionUnit)),
            JDALogger.getLazyString(() -> Helpers.durationToString(duration, resolutionUnit))
        );
    }

    // Unique streams checks

    public static <T> void checkUnique(Stream<T> stream, String format, BiFunction<Long, T, Object[]> getArgs)
    {
        Map<T, Long> counts = stream.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        for (Map.Entry<T, Long> entry : counts.entrySet())
        {
            if (entry.getValue() > 1)
            {
                Object[] args = getArgs.apply(entry.getValue(), entry.getKey());
                throw new IllegalArgumentException(Helpers.format(format, args));
            }
        }
    }

    public static void checkDuplicateIds(Stream<? extends LayoutComponent> layouts)
    {
        Stream<String> stream = layouts.flatMap(row -> row.getComponents().stream())
                .filter(ActionComponent.class::isInstance)
                .map(ActionComponent.class::cast)
                .map(ActionComponent::getId)
                .filter(Objects::nonNull);

        checkUnique(stream,
                "Cannot have components with duplicate custom IDs. Id: \"%s\" appeared %d times!",
                (count, value) -> new Object[]{ value, count }
        );
    }

    public static void checkComponents(String errorMessage, Collection<? extends Component> components, Predicate<Component> predicate)
    {
        StringBuilder sb = new StringBuilder();

        int idx = 0;
        for (Component component : components)
        {
            handleComponent(component, predicate, sb, "root.components[" + idx + "]");
            idx++;
        }

        if (sb.length() > 0)
            throw new IllegalArgumentException(errorMessage + "\n" + sb.toString().trim());
    }

    public static void checkComponents(String errorMessage, Component[] components, Predicate<Component> predicate)
    {
        checkComponents(errorMessage, Arrays.asList(components), predicate);
    }

    private static void handleComponent(Component component, Predicate<Component> predicate, StringBuilder sb, String path)
    {
        if (!predicate.test(component))
            sb.append(" - ").append(path).append(" - <").append(component.getType()).append(">\n");

        if (component instanceof LayoutComponent)
        {
            int idx = 0;
            for (Component child : (LayoutComponent) component)
            {
                handleComponent(child, predicate, sb, path + ".components[" + idx + "]");
                idx++;
            }
        }
    }

    // Permission checks

    public static void checkAccess(IPermissionHolder issuer, GuildChannel channel)
    {
        if (issuer.hasAccess(channel))
            return;

        EnumSet<Permission> perms = issuer.getPermissionsExplicit(channel);
        if (channel instanceof AudioChannel && !perms.contains(Permission.VOICE_CONNECT))
            throw new MissingAccessException(channel, Permission.VOICE_CONNECT);
        throw new MissingAccessException(channel, Permission.VIEW_CHANNEL);
    }

    // Attach checks

    public static void checkAttached(IDetachableEntity entity)
    {
        if (entity.isDetached())
            throw new DetachedEntityException();
    }

    // Type checks

    public static void checkSupportedChannelTypes(EnumSet<ChannelType> supported, ChannelType type, String what)
    {
        Checks.check(supported.contains(type), "Can only configure %s for channels of types %s", what,
                     JDALogger.getLazyString(() -> supported.stream().map(ChannelType::name).collect(Collectors.joining(", "))));
    }
}
