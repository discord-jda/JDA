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

import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.requests.Response;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.InteractionHookImpl;
import net.dv8tion.jda.internal.interactions.response.CallbackResponseImpl;
import okhttp3.MediaType;
import okhttp3.ResponseBody;

public abstract class DeferrableCallbackActionImpl extends InteractionCallbackImpl<InteractionHook>
{
    protected final InteractionHookImpl hook;

    public DeferrableCallbackActionImpl(InteractionHookImpl hook)
    {
        super(hook.getInteraction());
        this.hook = hook;
    }

    //Here we intercept the responses and forward this information to our followup hook
    // Using this, we can make nice cascading exceptions which fail all followup messages simultaneously.

    @Override
    protected void handleSuccess(Response response, Request<InteractionHook> request)
    {
        //releaseHook would be called by the super class's handling of this success, however
        // we also need to provide the hook itself to the success callback of the RestAction,
        // so we override this functionality
        interaction.releaseHook(true);
        parseOptionalBody(response);
        request.onSuccess(hook);
    }

    private void parseOptionalBody(Response response)
    {
        okhttp3.Response rawResponse = response.getRawResponse();
        if (rawResponse == null)
            return;

        ResponseBody body = rawResponse.body();
        if (body == null)
            return;

        MediaType mediaType = body.contentType();
        if (mediaType != null && mediaType.toString().startsWith("application/json"))
        {
            response.optObject().ifPresent(json ->
            {
                DataObject resource = json.getObject("resource");
                hook.setCallbackResponse(new CallbackResponseImpl(hook, resource));
            });
        }
    }
}
