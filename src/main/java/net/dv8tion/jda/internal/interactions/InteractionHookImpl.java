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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.InteractionExpiredException;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.response.InteractionCallbackResponse;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageDeleteAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageRetrieveAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.AbstractWebhookClient;
import net.dv8tion.jda.internal.entities.ReceivedMessage;
import net.dv8tion.jda.internal.interactions.response.InteractionCallbackResponseImpl;
import net.dv8tion.jda.internal.requests.restaction.*;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.JDALogger;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

public class InteractionHookImpl extends AbstractWebhookClient<Message> implements InteractionHook
{
    public static final String TIMEOUT_MESSAGE = "Timed out waiting for interaction acknowledgement";
    private final DeferrableInteractionImpl interaction;
    private final List<TriggerRestAction<?>> readyCallbacks = new LinkedList<>();
    private final Future<?> timeoutHandle;
    private final ReentrantLock mutex = new ReentrantLock();
    private final String token;
    private Exception exception;
    private boolean isReady;
    private boolean ephemeral;
    private InteractionCallbackResponseImpl callbackResponse;

    public InteractionHookImpl(@Nonnull DeferrableInteractionImpl interaction, @Nonnull JDA api)
    {
        super(api.getSelfUser().getApplicationIdLong(), interaction.getToken(), api);
        this.interaction = interaction;
        this.token = interaction.getToken();
        // 10 second timeout for our failure
        this.timeoutHandle = api.getGatewayPool().schedule(() -> this.fail(new TimeoutException(TIMEOUT_MESSAGE)), 10, TimeUnit.SECONDS);
    }

    public InteractionHookImpl(@Nonnull JDA api, @Nonnull String token)
    {
        super(api.getSelfUser().getApplicationIdLong(), token, api);
        this.interaction = null;
        this.token = token;
        this.timeoutHandle = null;
        this.isReady = true;
    }

    public boolean isAck()
    {
        return interaction == null || interaction.isAcknowledged();
    }

    public void ready()
    {
        MiscUtil.locked(mutex, () ->
        {
            if (timeoutHandle != null)
                timeoutHandle.cancel(false);
            isReady = true;
            readyCallbacks.forEach(TriggerRestAction::run);
        });
    }

    public void fail(Exception exception)
    {
        MiscUtil.locked(mutex, () ->
        {
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
        return MiscUtil.locked(mutex, () ->
        {
            if (isReady)
                runnable.run();
            else if (exception != null)
                runnable.fail(exception);
            else
                readyCallbacks.add(runnable);
            return runnable;
        });
    }

    public InteractionHookImpl setCallbackResponse(InteractionCallbackResponseImpl callbackResponse)
    {
        this.callbackResponse = callbackResponse;
        return this;
    }

    @Nonnull
    @Override
    public InteractionImpl getInteraction()
    {
        if (interaction == null)
            throw new IllegalStateException("Cannot get interaction instance from this webhook.");
        return interaction;
    }

    @Nonnull
    @Override
    public InteractionCallbackResponse getCallbackResponse()
    {
        if (!hasCallbackResponse())
            throw new IllegalStateException("Cannot get callback response. Has this interaction been acknowledged yet?");
        return callbackResponse;
    }

    @Override
    public boolean hasCallbackResponse()
    {
        return callbackResponse != null;
    }

    @Override
    public long getExpirationTimestamp()
    {
        OffsetDateTime creationTime = interaction == null ? OffsetDateTime.now() : interaction.getTimeCreated();
        return creationTime.plus(15, ChronoUnit.MINUTES).toEpochSecond() * 1000;
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
    public WebhookMessageCreateActionImpl<Message> sendRequest()
    {
        Route.CompiledRoute route = Route.Interactions.CREATE_FOLLOWUP.compile(api.getSelfUser().getApplicationId(), token);
        route = route.withQueryParams("wait", "true");
        WebhookMessageCreateActionImpl<Message> action = new WebhookMessageCreateActionImpl<>(api, route, this::buildMessage).setEphemeral(ephemeral);
        action.setCheck(this::checkExpired);
        return onReady(action);
    }

    @Nonnull
    @Override
    public WebhookMessageEditActionImpl<Message> editRequest(String messageId)
    {
        if (!"@original".equals(messageId))
            Checks.isSnowflake(messageId);

        Route.CompiledRoute route = Route.Interactions.EDIT_FOLLOWUP.compile(api.getSelfUser().getApplicationId(), token, messageId);
        route = route.withQueryParams("wait", "true");
        WebhookMessageEditActionImpl<Message> action = new WebhookMessageEditActionImpl<>(api, route, this::buildMessage);
        action.setCheck(this::checkExpired);
        return onReady(action);
    }

    @Nonnull
    @Override
    public WebhookMessageDeleteAction deleteMessageById(@Nonnull String messageId)
    {
        if (!"@original".equals(messageId))
            Checks.isSnowflake(messageId);
        Route.CompiledRoute route = Route.Interactions.DELETE_FOLLOWUP.compile(api.getSelfUser().getApplicationId(), token, messageId);
        WebhookMessageDeleteActionImpl action = new WebhookMessageDeleteActionImpl(api, route);
        action.setCheck(this::checkExpired);
        return onReady(action);
    }

    @Nonnull
    @Override
    public WebhookMessageRetrieveAction retrieveMessageById(@Nonnull String messageId)
    {
        if (!"@original".equals(messageId))
            Checks.isSnowflake(messageId);
        Route.CompiledRoute route = Route.Interactions.GET_MESSAGE.compile(api.getSelfUser().getApplicationId(), token, messageId);
        WebhookMessageRetrieveActionImpl action = new WebhookMessageRetrieveActionImpl(api, route, (response, request) -> buildMessage(response.getObject()));
        action.setCheck(this::checkExpired);
        return onReady(action);
    }

    private boolean checkExpired()
    {
        if (isExpired())
            throw new InteractionExpiredException();
        return true;
    }

    // Creates a message with the resolved channel context from the interaction
    // Sometimes we can't resolve the channel and report an unknown type
    // Currently known cases where channels can't be resolved:
    //  - InteractionHook created using id/token factory, has no interaction object to use as context
    public Message buildMessage(DataObject json)
    {
        JDAImpl jda = (JDAImpl) api;
        MessageChannel channel = null;
        Guild guild = null;

        // Try getting context from interaction if available
        // This might not be present if the hook was created from id/token instead of an event
        if (interaction != null)
        {
            channel = (MessageChannel) interaction.getChannel();
            guild = interaction.getGuild();
        }

        // Try finding the channel in cache through the id in the message
        long channelId = json.getUnsignedLong("channel_id");
        if (channel == null)
            channel = api.getChannelById(MessageChannel.class, channelId);

        // Then build the message with the information we have
        ReceivedMessage message = jda.getEntityBuilder().createMessageBestEffort(json, channel, guild);
        return message.withHook(this);
    }
}
