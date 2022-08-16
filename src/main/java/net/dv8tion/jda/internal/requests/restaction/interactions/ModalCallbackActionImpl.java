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

import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.requests.restaction.interactions.InteractionCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.InteractionImpl;

public class ModalCallbackActionImpl extends InteractionCallbackImpl<Void> implements ModalCallbackAction
{
    private final Modal modal;

    public ModalCallbackActionImpl(IModalCallback interaction, Modal modal)
    {
        super((InteractionImpl) interaction);
        this.modal = modal;
    }

    @Override
    protected DataObject toData()
    {
        return DataObject.empty()
                .put("type", InteractionCallbackAction.ResponseType.MODAL.getRaw())
                .put("data", modal);
    }
}
