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

package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.AbstractWebhookClient;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.TriggerRestAction;
import net.dv8tion.jda.internal.requests.restaction.WebhookMessageActionImpl;
import net.dv8tion.jda.internal.requests.restaction.WebhookMessageUpdateActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.JDALogger;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class InteractionHookImpl extends AbstractWebhookClient<Message> implements InteractionHook
{
    public static final String TIMEOUT_MESSAGE = "Timed out waiting for interaction acknowledgement";
    private final DeferrableInteractionImpl interaction;
    private final List<TriggerRestAction<?>> readyCallbacks = new LinkedList<>();
    private final Future<?> timeoutHandle;
    private final ReentrantLock mutex = new ReentrantLock();
    private Exception exception;
    private boolean isReady;
    private boolean ephemeral;

    public InteractionHookImpl(@Nonnull DeferrableInteractionImpl interaction, @Nonnull JDA api)
    {
        super(api.getSelfUser().getApplicationIdLong(), interaction.getToken(), api);
        this.interaction = interaction;
        // 10 second timeout for our failure
        this.timeoutHandle = api.getGatewayPool().schedule(() -> this.fail(new TimeoutException(TIMEOUT_MESSAGE)), 10, TimeUnit.SECONDS);
    }

    public boolean ack()
    {
        return interaction.ack();
    }

    public boolean isAck()
    {
        return interaction.isAcknowledged();
    }

    public void ready()
    {
        MiscUtil.locked(mutex, () -> {
            timeoutHandle.cancel(false);
            isReady = true;
            readyCallbacks.forEach(TriggerRestAction::run);
        });
    }

    public void fail(Exception exception)
    {
        MiscUtil.locked(mutex, () -> {
            if (!isReady && this.exception == null)
            {
                this.exception = exception;
                if (!readyCallbacks.isEmpty()) // only log this if we even tried any responses
                {
                    if (exception instanceof TimeoutException)
                        JDALogger.getLog(InteractionHook.class).warn("Up to {} Interaction Followup Messages Timed out! Did you forget to acknowledge the interaction?", readyCallbacks.size());
                    readyCallbacks.forEach(callback -> callback.fail(exception));
                }
            }
        });
    }

    private <T extends TriggerRestAction<R>, R> T onReady(T runnable)
    {
        return MiscUtil.locked(mutex, () -> {
            if (isReady)
                runnable.run();
            else if (exception != null)
                runnable.fail(exception);
            else
                readyCallbacks.add(runnable);
            return runnable;
        });
    }

    @Nonnull
    @Override
    public InteractionImpl getInteraction()
    {
        return interaction;
    }

    @Nonnull
    @Override
    public InteractionHook setEphemeral(boolean ephemeral)
    {
        this.ephemeral = ephemeral;
        return this;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Nonnull
    @Override
    public RestAction<Message> retrieveOriginal()
    {
        JDAImpl jda = (JDAImpl) getJDA();
        Route.CompiledRoute route = Route.Interactions.GET_ORIGINAL.compile(jda.getSelfUser().getApplicationId(), interaction.getToken());
        return onReady(new TriggerRestAction<>(jda, route, (response, request) ->
                jda.getEntityBuilder().createMessage(response.getObject(), getInteraction().getMessageChannel(), false)));
    }

    @Nonnull
    @Override
    public WebhookMessageActionImpl<Message> sendRequest()
    {
        Route.CompiledRoute route = Route.Interactions.CREATE_FOLLOWUP.compile(getJDA().getSelfUser().getApplicationId(), interaction.getToken());
        route = route.withQueryParams("wait", "true");
        Function<DataObject, Message> transform = (json) -> ((JDAImpl) api).getEntityBuilder().createMessage(json, getInteraction().getMessageChannel(), false).withHook(this);
        return onReady(new WebhookMessageActionImpl<>(getJDA(), interaction.getMessageChannel(), route, transform)).setEphemeral(ephemeral);
    }

    @Nonnull
    @Override
    public WebhookMessageUpdateActionImpl<Message> editRequest(String messageId)
    {
        if (!"@original".equals(messageId))
            Checks.isSnowflake(messageId);
        Route.CompiledRoute route = Route.Interactions.EDIT_FOLLOWUP.compile(getJDA().getSelfUser().getApplicationId(), interaction.getToken(), messageId);
        route = route.withQueryParams("wait", "true");
        Function<DataObject, Message> transform = (json) -> ((JDAImpl) api).getEntityBuilder().createMessage(json, getInteraction().getMessageChannel(), false).withHook(this);
        return onReady(new WebhookMessageUpdateActionImpl<>(getJDA(), route, transform));
    }

    @Nonnull
    @Override
    public RestAction<Void> deleteMessageById(@Nonnull String messageId)
    {
        if (!"@original".equals(messageId))
            Checks.isSnowflake(messageId);
        Route.CompiledRoute route = Route.Interactions.DELETE_FOLLOWUP.compile(getJDA().getSelfUser().getApplicationId(), interaction.getToken(), messageId);
        return onReady(new TriggerRestAction<>(getJDA(), route));
    }
}
