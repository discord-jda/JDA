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

package net.dv8tion.jda.api.interactions.callbacks;

import net.dv8tion.jda.api.interactions.components.text.Modal;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.interactions.InteractionCallbackAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Interactions which allow sending modals.
 */
public interface IModalCallback extends IDeferrableCallback
{
    /**
     * Replies to this interaction with a {@link Modal Modal}.
     *
     * <p>This will open a popup on the target user's Discord client.
     *
     * @param  modal 
     *         The Modal to send
     *
     * @throws IllegalArgumentException
     *         If modal is null
     *        
     * @return RestAction - Type: {@link Void}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> replyModal(Modal modal)
    {
        Checks.notNull(modal, "Modal");
        Route.CompiledRoute route = Route.Interactions.CALLBACK.compile(getId(), getToken());
        DataObject object = DataObject.empty()
                .put("type", InteractionCallbackAction.ResponseType.MODAL.getRaw())
                .put("data", modal.toData());

        return new RestActionImpl<>(getJDA(), route, object, ((response, voidRequest) -> null));
    }
}
