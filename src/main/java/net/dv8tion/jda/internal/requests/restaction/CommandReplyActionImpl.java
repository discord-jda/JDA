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

package net.dv8tion.jda.internal.requests.restaction;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.exceptions.InteractionFailureException;
import net.dv8tion.jda.api.interactions.commands.CommandHook;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandReplyAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.CommandHookImpl;
import net.dv8tion.jda.internal.requests.Requester;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.AllowedMentionsUtil;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.IOUtil;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class CommandReplyActionImpl extends RestActionImpl<CommandHook> implements CommandReplyAction
{
    private final CommandHookImpl hook;
    private final List<MessageEmbed> embeds = new ArrayList<>();
    private final Map<String, InputStream> files = new HashMap<>();
    private final AllowedMentionsUtil allowedMentions = new AllowedMentionsUtil();
    private int flags;

    private String content = "";
    private boolean tts;

    public CommandReplyActionImpl(JDA api, Route.CompiledRoute route, CommandHookImpl hook)
    {
        super(api, route);
        this.hook = hook;
    }

    public CommandReplyActionImpl applyMessage(Message message)
    {
        this.content = message.getContentRaw();
        this.tts = message.isTTS();
        this.embeds.addAll(message.getEmbeds());
        this.allowedMentions.applyMessage(message);
        return this;
    }

    private DataObject getJSON()
    {
        DataObject json = DataObject.empty();
        if (isEmpty())
        {
            json.put("type", ResponseType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE.getRaw());
            if (flags != 0)
                json.put("data", DataObject.empty().put("flags", flags));
        }
        else
        {
            DataObject payload = DataObject.empty();
            payload.put("allowed_mentions", allowedMentions);
            payload.put("content", content);
            payload.put("tts", tts);
            payload.put("flags", flags);
            if (!embeds.isEmpty())
                payload.put("embeds", DataArray.fromCollection(embeds));
            json.put("data", payload);

            json.put("type", ResponseType.CHANNEL_MESSAGE_WITH_SOURCE.getRaw()); // This type seemingly makes no difference right now, idk why it exists
        }
        return json;
    }

    private boolean isEmpty()
    {
        return Helpers.isEmpty(content) && embeds.isEmpty() && files.isEmpty();
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject json = getJSON();
        if (files.isEmpty())
            return getRequestBody(json);

        MultipartBody.Builder body = new MultipartBody.Builder();
        int i = 0;
        for (Map.Entry<String, InputStream> file : files.entrySet())
        {
            RequestBody stream = IOUtil.createRequestBody(Requester.MEDIA_TYPE_OCTET, file.getValue());
            body.addFormDataPart("file" + i++, file.getKey(), stream);
        }

        body.addFormDataPart("payload_json", json.toString());
        files.clear();
        return body.build();
    }

    @Override
    protected void handleSuccess(Response response, Request<CommandHook> request)
    {
        hook.ready();
        request.onSuccess(hook);
    }

    @Override
    public void handleResponse(Response response, Request<CommandHook> request)
    {
        if (!response.isOk())
            hook.fail(new InteractionFailureException());
        super.handleResponse(response, request);
    }

    @Nonnull
    @Override
    public CommandReplyActionImpl setEphemeral(boolean ephemeral)
    {
        if (ephemeral)
            this.flags |= Flag.EPHEMERAL.getRaw();
        else
            this.flags &= ~Flag.EPHEMERAL.getRaw();
        return this;
    }

//    @Nonnull
//    @Override
//    public CommandReplyAction addFile(@Nonnull InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options)
//    {
//        Checks.notNull(data, "Data");
//        Checks.notEmpty(name, "Name");
//        Checks.noneNull(options, "Options");
//        if (options.length > 0)
//            name = "SPOILER_" + name;
//
//        files.put(name, data);
//        return this;
//    }

    @Nonnull
    @Override
    public CommandReplyAction addEmbeds(@Nonnull Collection<MessageEmbed> embeds)
    {
        Checks.noneNull(embeds, "MessageEmbed");
        for (MessageEmbed embed : embeds)
        {
            Checks.check(embed.isSendable(),
                "Provided Message contains an empty embed or an embed with a length greater than %d characters, which is the max for bot accounts!",
                MessageEmbed.EMBED_MAX_LENGTH_BOT);
        }

        if (embeds.size() + this.embeds.size() > 10)
            throw new IllegalStateException("Cannot have more than 10 embeds per message!");
        this.embeds.addAll(embeds);
        return this;
    }

    @Nonnull
    @Override
    public CommandReplyAction setCheck(BooleanSupplier checks)
    {
        return (CommandReplyAction) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public CommandReplyAction timeout(long timeout, @Nonnull TimeUnit unit)
    {
        return (CommandReplyAction) super.timeout(timeout, unit);
    }

    @Nonnull
    @Override
    public CommandReplyAction deadline(long timestamp)
    {
        return (CommandReplyAction) super.deadline(timestamp);
    }

    @Nonnull
    @Override
    public CommandReplyActionImpl setTTS(boolean isTTS)
    {
        this.tts = isTTS;
        return this;
    }

    @Nonnull
    @Override
    public CommandReplyActionImpl setContent(String content)
    {
        this.content = content == null ? "" : content;
        return this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public CommandReplyAction mentionRepliedUser(boolean mention)
    {
        allowedMentions.mentionRepliedUser(mention);
        return this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public CommandReplyAction allowedMentions(@Nullable Collection<Message.MentionType> allowedMentions)
    {
        this.allowedMentions.allowedMentions(allowedMentions);
        return this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public CommandReplyAction mention(@Nonnull IMentionable... mentions)
    {
        allowedMentions.mention(mentions);
        return this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public CommandReplyAction mentionUsers(@Nonnull String... userIds)
    {
        allowedMentions.mentionUsers(userIds);
        return this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public CommandReplyAction mentionRoles(@Nonnull String... roleIds)
    {
        allowedMentions.mentionRoles(roleIds);
        return this;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Here we intercept calls to queue/submit/complete to prevent double ack/reply scenarios with a better error message than discord provides //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // This is an exception factory method that only returns an exception if we would have to throw it or fail in another way.
    private IllegalStateException tryAck() // note that hook.ack() is already synchronized so this is actually thread-safe!
    {
        // true => we already called this before => this will never succeed!
        return hook.ack() ? new IllegalStateException("This interaction has already been acknowledged or replied to. You can only reply or acknowledge an interaction (or slash command) once!")
                          : null; // null indicates we were successful, no exception means we can't fail :)
    }

    @Override
    public void queue(Consumer<? super CommandHook> success, Consumer<? super Throwable> failure)
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
    public CompletableFuture<CommandHook> submit(boolean shouldQueue)
    {
        IllegalStateException exception = tryAck();
        if (exception != null)
        {
            CompletableFuture<CommandHook> future = new CompletableFuture<>();
            future.completeExceptionally(exception);
            return future;
        }

        return super.submit(shouldQueue);
    }
}
