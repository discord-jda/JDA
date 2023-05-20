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

import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;

public class UserSnowflakeImpl implements UserSnowflake
{
    protected final long id;

    public UserSnowflakeImpl(long id)
    {
        this.id = id;
    }

    @Override
    public long getIdLong()
    {
        return this.id;
    }

    @Nonnull
    @Override
    public String getAsMention()
    {
        return "<@" + getId() + ">";
    }

    @Nonnull
    @Override
    public String getDefaultAvatarId()
    {
        return String.valueOf((id >> 22) % 5);
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof UserSnowflakeImpl))
            return false;
        return ((UserSnowflakeImpl) obj).getIdLong() == this.id;
    }

    @Override
    public String toString()
    {
        return new EntityString(this).toString();
    }
}
