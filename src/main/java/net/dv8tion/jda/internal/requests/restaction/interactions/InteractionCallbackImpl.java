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

import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.*;
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
        super(interaction.getJDA(),
            Route.Interactions.CALLBACK.compile(interaction.getId(), interaction.getToken())
                .withQueryParams("with_response", "true"));
        this.interaction = interaction;
        setErrorMapper(this::handleUnknownInteraction);
    }

    private Throwable handleUnknownInteraction(Response response, Request<?> request, ErrorResponseException exception)
    {
        // While this error response has existed since at least 2022 (https://github.com/discord/discord-api-docs/pull/4484),
        // Discord does not always report this error correctly and instead sends a 'UNKNOWN_INTERACTION'.
        // That's why we also have a similar exception at the end of this method.
        if (exception.getErrorResponse() == ErrorResponse.INTERACTION_ALREADY_ACKNOWLEDGED)
            return ErrorResponseException.create(
                    "This interaction was acknowledged by another process running for the same bot.\n" +
                            "To resolve this, try stopping all current processes for the bot that could be responsible, or resetting your bot token.\n" +
                            "You can reset your token at https://discord.com/developers/applications/" + getJDA().getSelfUser().getApplicationId() + "/bot",
                    exception
            );

        // Time synchronization issues prevent us from checking the exact nature of the issue,
        // and storing a local Instant would be invalid in case the WS thread is blocked,
        // as it will be created when the thread is released.
        // Send a message for both issues instead.
        if (exception.getErrorResponse() == ErrorResponse.UNKNOWN_INTERACTION)
            return ErrorResponseException.create(
                    "Failed to acknowledge this interaction, this can be due to 2 reasons:\n" +
                            "1. This interaction took longer than 3 seconds to be acknowledged, see https://jda.wiki/using-jda/troubleshooting/#the-interaction-took-longer-than-3-seconds-to-be-acknowledged\n" +
                            "2. This interaction could have been acknowledged by another process running for the same bot\n" +
                            "You can confirm this by checking if your bot replied, or the three dots in a button disappeared without saying 'This interaction failed', or you see '[Bot] is thinking...' for more than 3 seconds.\n" +
                            "To resolve this, try stopping all current processes for the bot that could be responsible, or resetting your bot token.\n" +
                            "You can reset your token at https://discord.com/developers/applications/" + getJDA().getSelfUser().getApplicationId() + "/bot",
                    exception
            );

        return null;
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

    @Override
    public final void queue(Consumer<? super T> success, Consumer<? super Throwable> failure)
    {
        IllegalStateException exception = interaction.tryAck();
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
        IllegalStateException exception = interaction.tryAck();
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
