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

package net.dv8tion.jda.internal.commands;

import net.dv8tion.jda.api.commands.CommandHook;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.InteractionWebhookAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.TriggerRestAction;
import net.dv8tion.jda.internal.requests.restaction.WebhookMessageActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.JDALogger;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

public class CommandHookImpl implements CommandHook
{
    public static final String TIMEOUT_MESSAGE = "Timed out waiting for interaction acknowledgement";
    private final SlashCommandEvent event;
    private final List<TriggerRestAction<?>> readyCallbacks = new LinkedList<>();
    private final Future<?> timeoutHandle;
    private final ReentrantLock mutex = new ReentrantLock();
    private Exception exception;
    private boolean isReady;
    private boolean ephemeral;

    public CommandHookImpl(@Nonnull SlashCommandEvent event)
    {
        this.event = event;
        // 10 second timeout for our failure
        this.timeoutHandle = event.getJDA().getGatewayPool().schedule(() -> this.fail(new TimeoutException(TIMEOUT_MESSAGE)), 10, TimeUnit.SECONDS);
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
                        JDALogger.getLog(CommandHook.class).warn("Up to {} Interaction Followup Messages Timed out for command with name \"{}\"! Did you forget to acknowledge the interaction?", readyCallbacks.size(), event.getName());
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
    public SlashCommandEvent getEvent()
    {
        return event;
    }

    @Nonnull
    @Override
    public CommandHook setEphemeral(boolean ephemeral)
    {
        this.ephemeral = ephemeral;
        return this;
    }

    @Nonnull
    private WebhookMessageActionImpl sendRequest()
    {
        Route.CompiledRoute route = Route.Interactions.CREATE_FOLLOWUP.compile(getJDA().getSelfUser().getApplicationId(), event.getInteractionToken());
        route = route.withQueryParams("wait", "true");
        return onReady(new WebhookMessageActionImpl(getJDA(), route));
    }

    @Nonnull
    private WebhookMessageActionImpl editRequest(String messageId)
    {
        if (!"@original".equals(messageId))
            Checks.isSnowflake(messageId);
        Route.CompiledRoute route = Route.Interactions.EDIT_FOLLOWUP.compile(getJDA().getSelfUser().getApplicationId(), event.getInteractionToken(), messageId);
        route = route.withQueryParams("wait", "true");
        return onReady(new WebhookMessageActionImpl(getJDA(), route));
    }

    @Nonnull
    @Override
    public InteractionWebhookAction sendMessage(@Nonnull String content)
    {
        return sendRequest().setContent(content).setEphemeral(ephemeral);
    }

    @Nonnull
    @Override
    public InteractionWebhookAction sendMessage(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... embeds)
    {
        return sendRequest().addEmbeds(embed, embeds).setEphemeral(ephemeral);
    }

    @Nonnull
    @Override
    public InteractionWebhookAction sendMessage(@Nonnull Message message)
    {
        return sendRequest().applyMessage(message);
    }

    @Nonnull
    @Override
    public InteractionWebhookAction sendFile(@Nonnull InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        return sendRequest().addFile(name, data, options);
    }

    @Nonnull
    @Override
    public InteractionWebhookAction editMessageById(@Nonnull String messageId, @Nonnull String content)
    {
        return editRequest(messageId).setContent(content);
    }

    @Nonnull
    @Override
    public InteractionWebhookAction editMessageById(@Nonnull String messageId, @Nonnull MessageEmbed embed, @Nonnull MessageEmbed... embeds)
    {
        return editRequest(messageId).addEmbeds(embed, embeds);
    }

    @Nonnull
    @Override
    public InteractionWebhookAction editMessageById(@Nonnull String messageId, @Nonnull Message message)
    {
        return editRequest(messageId).applyMessage(message);
    }

    @Nonnull
    @Override
    public RestAction<Void> deleteMessageById(@Nonnull String messageId)
    {
        if (!"@original".equals(messageId))
            Checks.isSnowflake(messageId);
        Route.CompiledRoute route = Route.Interactions.DELETE_FOLLOWUP.compile(getJDA().getSelfUser().getApplicationId(), event.getInteractionToken(), messageId);
        return onReady(new TriggerRestAction<>(getJDA(), route));
    }
}
