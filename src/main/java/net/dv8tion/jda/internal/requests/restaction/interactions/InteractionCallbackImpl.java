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

import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.interactions.InteractionCallbackAction;
import net.dv8tion.jda.internal.interactions.InteractionImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class InteractionCallbackImpl<T> extends RestActionImpl<T> implements InteractionCallbackAction<T>
{
    protected final InteractionImpl interaction;

    public InteractionCallbackImpl(InteractionImpl interaction)
    {
        super(interaction.getJDA(),  Route.Interactions.CALLBACK.compile(interaction.getId(), interaction.getToken()));
        this.interaction = interaction;
    }

    @Nonnull
    @Override
    public InteractionCallbackAction<T> closeResources()
    {
        return this;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Here we intercept calls to queue/submit/complete to prevent double ack/reply scenarios with a better error message than discord provides //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // This is an exception factory method that only returns an exception if we would have to throw it or fail in another way.
    protected final IllegalStateException tryAck() // note that interaction.ack() is already synchronized so this is actually thread-safe!
    {
        // true => we already called this before => this will never succeed!
        return interaction.ack()
                ? new IllegalStateException("This interaction has already been acknowledged or replied to. You can only reply or acknowledge an interaction once!")
                : null; // null indicates we were successful, no exception means we can't fail :)
    }

    @Override
    public final void queue(Consumer<? super T> success, Consumer<? super Throwable> failure)
    {
        IllegalStateException exception = tryAck();
        if (exception != null)
        {
            if (failure != null)
                failure.accept(exception); // if the failure callback throws that will just bubble up, which is acceptable
            else
                RestAction.getDefaultFailure().accept(exception);
            return;
        }

        super.queue(success, failure);
    }

    @Nonnull
    @Override
    public final CompletableFuture<T> submit(boolean shouldQueue)
    {
        IllegalStateException exception = tryAck();
        if (exception != null)
        {
            CompletableFuture<T> future = new CompletableFuture<>();
            future.completeExceptionally(exception);
            return future;
        }

        return super.submit(shouldQueue);
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Here we handle the interaction hook, which awaits the signal that the interaction was acknowledged before sending any requests. //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void handleSuccess(Response response, Request<T> request)
    {
        interaction.releaseHook(true); // sends followup messages
        super.handleSuccess(response, request);
    }

    @Override
    public void handleResponse(Response response, Request<T> request)
    {
        if (!response.isOk())
            interaction.releaseHook(false); // cancels followup messages with an exception
        super.handleResponse(response, request);
    }
}
