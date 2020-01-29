/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.api.exceptions;

import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ErrorHandler implements Consumer<Throwable>
{
    private static final Consumer<? super Throwable> empty = (e) -> {};
    private final Consumer<? super Throwable> base;
    private final Map<Predicate<? super Throwable>, Consumer<? super Throwable>> cases = new LinkedHashMap<>();

    public ErrorHandler()
    {
        this(RestAction.getDefaultFailure());
    }

    public ErrorHandler(Consumer<? super Throwable> base)
    {
        this.base = base;
    }

    @Nonnull
    public ErrorHandler ignore(@Nonnull ErrorResponse response, @Nonnull ErrorResponse... set)
    {
        Checks.notNull(response, "ErrorResponse");
        Checks.noneNull(set, "ErrorResponse");
        return ignore(EnumSet.of(response, set));
    }

    @Nonnull
    public ErrorHandler ignore(@Nonnull EnumSet<ErrorResponse> match)
    {
        return handle(empty, match);
    }

    @Nonnull
    public ErrorHandler ignore(@Nonnull Class<? extends Throwable>... classes)
    {
        Checks.noneNull(classes, "Classes");
        return ignore(it -> {
            for (Class<? extends Throwable> clazz : classes)
            {
                if (clazz.isInstance(it))
                    return true;
            }
            return false;
        });
    }

    @Nonnull
    public ErrorHandler ignore(@Nonnull Predicate<? super Throwable> condition)
    {
        return handle(condition, empty);
    }

    @Nonnull
    public ErrorHandler handle(@Nonnull Consumer<? super ErrorResponseException> handler, @Nonnull ErrorResponse response, @Nonnull ErrorResponse... set)
    {
        Checks.notNull(response, "ErrorResponse");
        Checks.noneNull(set, "ErrorResponse");
        return handle(handler, EnumSet.of(response, set));
    }

    @Nonnull
    public ErrorHandler handle(@Nonnull Consumer<? super ErrorResponseException> handler, @Nonnull EnumSet<ErrorResponse> match)
    {
        Checks.notNull(handler, "Handler");
        Checks.notNull(match, "EnumSet");
        return handle(ErrorResponseException.class, (it) -> match.contains(it.getErrorResponse()), handler);
    }

    @Nonnull
    public <T> ErrorHandler handle(@Nonnull Class<T> clazz, @Nonnull Consumer<? super T> handler)
    {
        Checks.notNull(clazz, "Class");
        Checks.notNull(handler, "Handler");
        return handle(clazz::isInstance, (ex) -> handler.accept(clazz.cast(ex)));
    }

    @Nonnull
    public <T> ErrorHandler handle(@Nonnull Class<T> clazz, @Nonnull Predicate<? super T> condition, @Nonnull Consumer<? super T> handler)
    {
        Checks.notNull(clazz, "Class");
        Checks.notNull(handler, "Handler");
        return handle(
            (it) -> clazz.isInstance(it) && condition.test(clazz.cast(it)),
            (ex) -> handler.accept(clazz.cast(ex)));
    }

    @Nonnull
    public ErrorHandler handle(@Nonnull Predicate<? super Throwable> condition, @Nonnull Consumer<? super Throwable> handler)
    {
        Checks.notNull(condition, "Condition");
        Checks.notNull(handler, "Handler");
        cases.put(condition, handler);
        return this;
    }

    @Override
    public void accept(Throwable t)
    {
        for (Map.Entry<Predicate<? super Throwable>, Consumer<? super Throwable>> entry : cases.entrySet())
        {
            Predicate<? super Throwable> condition = entry.getKey();
            Consumer<? super Throwable> callback = entry.getValue();
            if (condition.test(t))
            {
                callback.accept(t);
                return;
            }
        }

        base.accept(t);
    }
}
