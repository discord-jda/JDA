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
import net.dv8tion.jda.api.entities.Message.MessageFlag;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.requests.restaction.ThreadCreateMetadata;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.message.MessageCreateBuilderMixin;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class WebhookMessageCreateActionImpl<T>
    extends AbstractWebhookMessageActionImpl<T, WebhookMessageCreateActionImpl<T>>
    implements WebhookMessageCreateAction<T>, MessageCreateBuilderMixin<WebhookMessageCreateAction<T>>
{
    private final MessageCreateBuilder builder = new MessageCreateBuilder();
    private final Function<DataObject, T> transformer;

    private boolean isInteraction = true;

    // Interactions only
    private boolean ephemeral;

    // Incoming webhooks only

    private String username;
    private String avatar;
    private ThreadCreateMetadata threadMetadata;

    public WebhookMessageCreateActionImpl(JDA api, Route.CompiledRoute route, Function<DataObject, T> transformer)
    {
        super(api, route);
        this.transformer = transformer;
    }

    public WebhookMessageCreateActionImpl<T> setInteraction(boolean isInteraction)
    {
        this.isInteraction = isInteraction;
        return this;
    }

    @Override
    public MessageCreateBuilder getBuilder()
    {
        return builder;
    }

    @Nonnull
    @Override
    public WebhookMessageCreateActionImpl<T> setEphemeral(boolean ephemeral)
    {
        if (!isInteraction && ephemeral)
            throw new IllegalStateException("Cannot create ephemeral messages with webhooks. Use InteractionHook instead!");

        this.ephemeral = ephemeral;
        return this;
    }

    @Nonnull
    @Override
    public WebhookMessageCreateAction<T> setUsername(@Nullable String name)
    {
        if (isInteraction && username != null)
            throw new IllegalStateException("Cannot set username on interaction messages.");

        if (name != null)
        {
            name = name.trim();
            Checks.inRange(name, 1, 80, "Name"); // See https://discord.com/developers/docs/resources/webhook#create-webhook
        }

        this.username = name;
        return this;
    }

    @Nonnull
    @Override
    public WebhookMessageCreateAction<T> setAvatarUrl(@Nullable String iconUrl)
    {
        if (isInteraction && iconUrl != null)
            throw new IllegalStateException("Cannot set avatar on interaction messages.");

        if (iconUrl != null)
        {
            Checks.noWhitespace(iconUrl, "Avatar URL");
            Checks.check(
                iconUrl.startsWith("https://") || iconUrl.startsWith("http://"),
                "Invalid URL format. Must start with 'https://' or 'http://'. Provided %s", iconUrl
            );
        }

        this.avatar = iconUrl;
        return this;
    }

    @Nonnull
    @Override
    public WebhookMessageCreateAction<T> createThread(@Nonnull ThreadCreateMetadata threadMetadata)
    {
        if (isInteraction)
            throw new IllegalStateException("Cannot create a thread through an interaction hook.");

        Checks.notNull(threadMetadata, "Thread Metadata");
        this.threadMetadata = threadMetadata;

        return this;
    }

    @Override
    protected RequestBody finalizeData()
    {
        try (MessageCreateData data = builder.build())
        {
            DataObject json = data.toData();
            if (ephemeral)
                json.put("flags", json.getInt("flags", 0) | MessageFlag.EPHEMERAL.getValue());

            if (username != null)
                json.put("username", username);
            if (avatar != null)
                json.put("avatar_url", avatar);

            if (threadId == null && threadMetadata != null)
            {
                json.put("thread_name", threadMetadata.getName());
                List<ForumTagSnowflake> tags = threadMetadata.getAppliedTags();
                if (!tags.isEmpty())
                    json.put("applied_tags", tags.stream().map(ForumTagSnowflake::getId).collect(Helpers.toDataArray()));
            }

            return getMultipartBody(data.getFiles(), data.getAdditionalFiles(), json);
        }
    }

    @Override
    protected Route.CompiledRoute finalizeRoute()
    {
        Route.CompiledRoute route = super.finalizeRoute();
        if (threadId != null)
           route = route.withQueryParams("thread_id", threadId);

        return route;
    }

    @Override
    protected void handleSuccess(Response response, Request<T> request)
    {
        T message = transformer.apply(response.getObject());
        request.onSuccess(message);
    }
}
