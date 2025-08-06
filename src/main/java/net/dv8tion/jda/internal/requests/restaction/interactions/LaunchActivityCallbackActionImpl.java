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

import net.dv8tion.jda.api.requests.restaction.interactions.LaunchActivityCallbackAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.interactions.InteractionHookImpl;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;

public class LaunchActivityCallbackActionImpl extends DeferrableCallbackActionImpl implements LaunchActivityCallbackAction
{
    public LaunchActivityCallbackActionImpl(InteractionHookImpl hook)
    {
        super(hook);
    }

    @Override
    protected RequestBody finalizeData()
    {
        return getRequestBody(DataObject.empty()
                .put("type", ResponseType.LAUNCH_ACTIVITY.getRaw())
                .put("data", DataObject.empty()));
    }

    @Nonnull
    @Override
    public LaunchActivityCallbackAction setCheck(BooleanSupplier checks)
    {
        return (LaunchActivityCallbackAction) super.setCheck(checks);
    }

    @Nonnull
    @Override
    public LaunchActivityCallbackAction deadline(long timestamp)
    {
        return (LaunchActivityCallbackAction) super.deadline(timestamp);
    }
}
