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

import net.dv8tion.jda.api.interactions.callbacks.IPremiumRequiredReplyCallback;
import net.dv8tion.jda.api.requests.restaction.interactions.InteractionCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.PremiumRequiredCallbackAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.InteractionImpl;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;

public class PremiumRequiredCallbackActionImpl extends InteractionCallbackImpl<Void> implements PremiumRequiredCallbackAction
{

    public PremiumRequiredCallbackActionImpl(IPremiumRequiredReplyCallback interaction)
    {
        super((InteractionImpl) interaction);
    }

    @Override
    protected RequestBody finalizeData()
    {
        return getRequestBody(DataObject.empty()
                .put("type", InteractionCallbackAction.ResponseType.PREMIUM_REQUIRED.getRaw())
                .put("data", DataObject.empty()));
    }

    @Nonnull
    @Override
    public PremiumRequiredCallbackAction setCheck(BooleanSupplier checks)
    {
        return (PremiumRequiredCallbackAction) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public PremiumRequiredCallbackAction deadline(long timestamp)
    {
        return (PremiumRequiredCallbackAction) super.deadline(timestamp);
    }
}
