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

import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.requests.restaction.interactions.InteractionCallbackAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.Requester;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.IOUtil;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public abstract class InteractionCallbackImpl<T> extends RestActionImpl<T> implements InteractionCallbackAction<T>
{
    protected final Map<String, InputStream> files = new HashMap<>();

    public InteractionCallbackImpl(Interaction interaction)
    {
        super(interaction.getJDA(),  Route.Interactions.CALLBACK.compile(interaction.getId(), interaction.getToken()));
    }

    protected abstract DataObject toData();

    @Override
    protected RequestBody finalizeData()
    {
        DataObject json = toData();
        if (files.isEmpty())
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
}
