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
package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.ActivityInstanceResource;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ActivityInstanceResourceImpl implements ActivityInstanceResource
{
    private final String instanceId;

    public ActivityInstanceResourceImpl(String instanceId)
    {
        this.instanceId = instanceId;
    }

    @Nonnull
    @Override
    public String getInstanceId()
    {
        return instanceId;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof ActivityInstanceResourceImpl))
            return false;

        ActivityInstanceResourceImpl other = (ActivityInstanceResourceImpl) obj;
        return Objects.equals(instanceId, other.instanceId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(instanceId);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("instanceId", instanceId)
                .toString();
    }
}
