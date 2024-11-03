/*
 * Copyright 2024 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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

package net.dv8tion.jda.internal.entities.emoji;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.managers.ApplicationEmojiManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.managers.ApplicationEmojiManagerImpl;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ApplicationEmojiImpl implements ApplicationEmoji, EmojiUnion
{
    private final long id;
    private final JDAImpl api;
    private final User owner;

    boolean animated = false;
    private String name;

    public ApplicationEmojiImpl(long id, JDAImpl api, User owner)
    {
        this.id = id;
        this.api = api;
        this.owner = owner;
    }

    @Nonnull
    @Override
    public Type getType()
    {
        return Type.CUSTOM;
    }

    @Nonnull
    @Override
    public String getAsReactionCode()
    {
        return name + ":" + id;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return DataObject.empty()
                .put("name", name)
                .put("animated", animated)
                .put("id", id);
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }

    @Nullable
    @Override
    public User getOwner()
    {
        return owner;
    }

    @Nonnull
    @Override
    public ApplicationEmojiManager getManager()
    {
        return new ApplicationEmojiManagerImpl(this);
    }

    @Override
    public boolean isAnimated()
    {
        return animated;
    }

    @Nonnull
    @Override
    public RestAction<Void> delete()
    {
        Route.CompiledRoute route = Route.Applications.DELETE_APPLICATION_EMOJI.compile(getJDA().getSelfUser().getApplicationId(), getId());
        return new RestActionImpl<>(getJDA(), route);
    }

    // -- Setters --

    public ApplicationEmojiImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public ApplicationEmojiImpl setAnimated(boolean animated)
    {
        this.animated = animated;
        return this;
    }

    // -- Object overrides --

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof ApplicationEmojiImpl))
            return false;

        ApplicationEmojiImpl other = (ApplicationEmojiImpl) obj;
        return this.id == other.getIdLong();
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .setName(name)
                .toString();
    }

    @Nonnull
    @Override
    public UnicodeEmoji asUnicode()
    {
        throw new IllegalStateException("Cannot convert ApplicationEmoji to UnicodeEmoji!");
    }

    @Nonnull
    @Override
    public CustomEmoji asCustom()
    {
        return this;
    }

    @Nonnull
    @Override
    public RichCustomEmoji asRich()
    {
        throw new IllegalStateException("Cannot convert ApplicationEmoji to RichCustomEmoji!");
    }

    @Nonnull
    @Override
    public ApplicationEmoji asApplication()
    {
        return this;
    }
}
