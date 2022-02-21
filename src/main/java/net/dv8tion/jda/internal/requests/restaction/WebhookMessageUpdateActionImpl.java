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
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.Requester;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.IOUtil;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WebhookMessageUpdateActionImpl<T>
    extends TriggerRestAction<T>
    implements WebhookMessageUpdateAction<T>
{
    private static final int CONTENT = 1 << 0;
    private static final int EMBEDS = 1 << 1;
    private static final int FILES = 1 << 2;
    private static final int COMPONENTS = 1 << 3;
    private static final int RETAINED_FILES = 1 << 4;

    private int set = 0;
    private final List<ActionRow> components = new ArrayList<>();
    private final List<MessageEmbed> embeds = new ArrayList<>();
    private final List<String> retainedFiles = new ArrayList<>();
    private final Map<String, InputStream> files = new HashMap<>();
    private final Function<DataObject, T> transformer;
    private String content;

    public WebhookMessageUpdateActionImpl(JDA api, Route.CompiledRoute route, Function<DataObject, T> transformer)
    {
        super(api, route);
        this.transformer = transformer;
    }

    @Nonnull
    @Override
    public WebhookMessageUpdateAction<T> setContent(@Nullable String content)
    {
        if (content != null)
            Checks.notLonger(content, Message.MAX_CONTENT_LENGTH, "Content");
        this.content = content;
        set |= CONTENT;
        return this;
    }

    @Nonnull
    @Override
    public WebhookMessageUpdateAction<T> setEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds)
    {
        Checks.noneNull(embeds, "MessageEmbeds");
        embeds.forEach(embed ->
            Checks.check(embed.isSendable(),
                "Provided Message contains an empty embed or an embed with a length greater than %d characters, which is the max for bot accounts!",
                MessageEmbed.EMBED_MAX_LENGTH_BOT)
        );
        Checks.check(embeds.size() <= 10, "Cannot have more than 10 embeds in a message!");
        Checks.check(embeds.stream().mapToInt(MessageEmbed::getLength).sum() <= MessageEmbed.EMBED_MAX_LENGTH_BOT, "The sum of all MessageEmbeds may not exceed %d!", MessageEmbed.EMBED_MAX_LENGTH_BOT);
        this.embeds.clear();
        this.embeds.addAll(embeds);
        set |= EMBEDS;
        return this;
    }

    @Nonnull
    @Override
    public WebhookMessageUpdateAction<T> addFile(@Nonnull InputStream data, @Nonnull String name, @Nonnull AttachmentOption... options)
    {
        Checks.notNull(name, "File name");
        Checks.notNull(data, "File data");
        Checks.noneNull(options, "AttachmentOptions");
        if (options.length > 0)
            name = "SPOILER_" + name;
        this.files.put(name, data);
        set |= FILES;
        return this;
    }

    @Nonnull
    @Override
    public WebhookMessageUpdateAction<T> retainFilesById(@Nonnull Collection<String> ids)
    {
        Checks.noneNull(ids, "IDs");
        ids.forEach(Checks::isSnowflake);
        this.retainedFiles.clear();
        this.retainedFiles.addAll(ids);
        set |= RETAINED_FILES;
        return this;
    }

    @Nonnull
    @Override
    public WebhookMessageUpdateAction<T> setActionRows(@Nonnull ActionRow... rows)
    {
        Checks.noneNull(rows, "ActionRows");
        Checks.checkDuplicateIds(Arrays.stream(rows));
        this.components.clear();
        Collections.addAll(this.components, rows);
        set |= COMPONENTS;
        return this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public WebhookMessageUpdateAction<T> applyMessage(@Nonnull Message message)
    {
        Checks.notNull(message, "Message");
        setContent(message.getContentRaw());
        setActionRows(message.getActionRows());
        setEmbeds(message.getEmbeds());
        return this;
    }

    private boolean isUpdate(int flag)
    {
        return (set & flag) == flag;
    }

    @Override
    protected RequestBody finalizeData()
    {
        DataObject json = DataObject.empty();
        if (isUpdate(CONTENT))
            json.put("content", content);
        if (isUpdate(EMBEDS))
            json.put("embeds", DataArray.fromCollection(embeds));
        if (isUpdate(COMPONENTS))
            json.put("components", DataArray.fromCollection(components));
        if (isUpdate(RETAINED_FILES))
        {
            json.put("attachments", DataArray.fromCollection(
                retainedFiles.stream()
                    .map(id -> DataObject.empty().put("id", id))
                    .collect(Collectors.toList()))
            );
        }

        if (!isUpdate(FILES))
            return getRequestBody(json);
        MultipartBody.Builder body = new MultipartBody.Builder().setType(MultipartBody.FORM);
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
    protected void handleSuccess(Response response, Request<T> request)
    {
        T message = transformer.apply(response.getObject());
        request.onSuccess(message);
    }
}
