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

import net.dv8tion.jda.api.entities.ForumTag;

import javax.annotation.Nonnull;

public class ForumTagImpl extends ForumTagSnowflakeImpl implements ForumTag
{
    private boolean moderated;
    private String name;
    private int position;

    public ForumTagImpl(long id)
    {
        super(id);
    }

    @Override
    public int getPosition()
    {
        return position;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean isModerated()
    {
        return moderated;
    }

    public ForumTagImpl setModerated(boolean moderated)
    {
        this.moderated = moderated;
        return this;
    }

    public ForumTagImpl setName(String name)
    {
        this.name = name;
        return this;
    }

    public ForumTagImpl setPosition(int position)
    {
        this.position = position;
        return this;
    }

    @Override
    public String toString()
    {
        return "ForumTag:" + name + "(" + id + ')';
    }
}
