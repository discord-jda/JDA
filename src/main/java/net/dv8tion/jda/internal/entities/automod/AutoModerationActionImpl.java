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

package net.dv8tion.jda.internal.entities.automod;

import net.dv8tion.jda.api.entities.automod.ActionMetadata;
import net.dv8tion.jda.api.entities.automod.AutoModerationAction;
import net.dv8tion.jda.api.entities.automod.AutoModerationActionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class AutoModerationActionImpl implements AutoModerationAction
{

    private AutoModerationActionType type;
    private ActionMetadata metadata;

    @NotNull
    @Override
    public AutoModerationActionType getType()
    {
        return type;
    }

    @Nullable
    @Override
    public ActionMetadata getActionMetadata()
    {
        return metadata;
    }

    public AutoModerationActionImpl setType(AutoModerationActionType type)
    {
        this.type = type;
        return this;
    }


    public AutoModerationActionImpl setMetadata(ActionMetadata metadata)
    {
        this.metadata = metadata;
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AutoModerationActionImpl that = (AutoModerationActionImpl) o;
        return type == that.type && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type, metadata);
    }

    @Override
    public String toString()
    {
        return "AutoModerationActionImpl(" +
                "type=" + type +
                ", metadata=" + metadata +
                ')';
    }
}
