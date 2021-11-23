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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

public class UserById implements User
{
    protected final long id;

    public UserById(long id)
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
        if (!(obj instanceof User))
            return false;
        return ((User) obj).getIdLong() == this.id;
    }

    @Override
    public String toString()
    {
        return "U:(" + getId() + ')';
    }

    @Contract("->fail")
    private void unsupported()
    {
        throw new UnsupportedOperationException("This User instance only wraps an ID. Other operations are unsupported");
    }

    @Nonnull
    @Override
    public String getName()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public String getDiscriminator()
    {
        unsupported();
        return null;
    }

    @Nullable
    @Override
    public String getAvatarId()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public RestAction<Profile> retrieveProfile()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public String getDefaultAvatarId()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public String getAsTag()
    {
        unsupported();
        return null;
    }

    @Override
    public boolean hasPrivateChannel()
    {
        unsupported();
        return false;
    }

    @Nonnull
    @Override
    public RestAction<PrivateChannel> openPrivateChannel()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public List<Guild> getMutualGuilds()
    {
        unsupported();
        return null;
    }

    @Override
    public boolean isBot()
    {
        unsupported();
        return false;
    }

    @Override
    public boolean isSystem()
    {
        unsupported();
        return false;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        unsupported();
        return null;
    }

    @Nonnull
    @Override
    public EnumSet<UserFlag> getFlags()
    {
        unsupported();
        return null;
    }

    @Override
    public int getFlagsRaw()
    {
        unsupported();
        return 0;
    }
}
