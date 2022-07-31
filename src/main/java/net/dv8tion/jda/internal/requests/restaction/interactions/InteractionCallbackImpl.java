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
import net.dv8tion.jda.api.requests.restaction.interactions.InteractionCallbackAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.InteractionImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.IOUtil;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class InteractionCallbackImpl<T> extends RestActionImpl<T> implements InteractionCallbackAction<T>
{
    protected final List<AttachedFile> files = new ArrayList<>();
    protected final InteractionImpl interaction;
    protected boolean isFileUpdate = false;

    public InteractionCallbackImpl(InteractionImpl interaction)
    {
        super(interaction.getJDA(),  Route.Interactions.CALLBACK.compile(interaction.getId(), interaction.getToken()));
        this.interaction = interaction;
    }

    protected abstract DataObject toData();

    @Override
    protected RequestBody finalizeData()
    {
        DataObject json = toData();

        if (isFileUpdate || !files.isEmpty())
        {
            // Add the attachments array to the payload, as required since v10
            DataObject data;
            if (json.isNull("data"))
                json.put("data", data = DataObject.empty());
            else
                data = json.getObject("data");

            DataArray attachments;
            if (data.isNull("attachments"))
                data.put("attachments", attachments = DataArray.empty());
            else
                attachments = data.getArray("attachments");

            for (int i = 0; i < files.size(); i++)
                attachments.add(files.get(i).toAttachmentData(i));
        }

        RequestBody body;
        // Upload files using multipart request if applicable
        if (files.stream().anyMatch(FileUpload.class::isInstance))
        {
            MultipartBody.Builder form = AttachedFile.createMultipartBody(files, null);
            form.addFormDataPart("payload_json", json.toString());
            body = form.build();
        }
        else
        {
            body = getRequestBody(json);
        }

        isFileUpdate = false;
        files.clear();
        return body;
    }

    @Nonnull
    @Override
    public InteractionCallbackAction<T> closeResources()
    {
        files.forEach(IOUtil::silentClose);
        files.clear();
        return this;
    }

    @Override
    @SuppressWarnings({"deprecation", "ResultOfMethodCallIgnored"})
    protected void finalize()
    {
        if (files.stream().noneMatch(FileUpload.class::isInstance))
            return;
        LOG.warn("Found open resources in interaction callback. Did you forget to close them?");
        closeResources();
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
