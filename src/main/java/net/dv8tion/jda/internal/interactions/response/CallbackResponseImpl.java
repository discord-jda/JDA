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

package net.dv8tion.jda.internal.interactions.response;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.response.CallbackResponseUnion;
import net.dv8tion.jda.api.requests.restaction.interactions.InteractionCallbackAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.InteractionHookImpl;

import javax.annotation.Nonnull;

public class CallbackResponseImpl implements CallbackResponseUnion
{
    private final InteractionCallbackAction.ResponseType type;

    private final Message message;

    public CallbackResponseImpl(InteractionHookImpl hook, DataObject resource)
    {
        this.type = InteractionCallbackAction.ResponseType.fromId(resource.getInt("type", -1));

        if (type.doesCallbackResponseContainMessage())
            this.message = resource.optObject("message").map(hook::buildMessage).orElse(null);
        else
            this.message = null;
    }

    @Nonnull
    @Override
    public Message asMessage()
    {
        if (!hasMessage())
            throw new IllegalStateException("Callback response of type " + type + " does not contain a Message!");
        return message;
    }

    @Override
    public boolean hasMessage()
    {
        return message != null;
    }

    @Nonnull
    @Override
    public InteractionCallbackAction.ResponseType getResponseType()
    {
        return type;
    }
}
