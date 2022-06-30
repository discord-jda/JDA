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

package net.dv8tion.jda.api.entities.automod.build;

import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.automod.AutoModerationAction;
import net.dv8tion.jda.api.entities.automod.EventType;
import net.dv8tion.jda.api.entities.automod.TriggerMetadata;
import net.dv8tion.jda.api.entities.automod.TriggerType;
import net.dv8tion.jda.api.utils.data.SerializableData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

//TODO: java doc
public interface AutoModerationRuledData extends SerializableData
{
    @Nonnull
    AutoModerationRuledData setActions(@Nonnull List<AutoModerationAction> actions);

    @Nullable
    AutoModerationRuledData setExemptRoles(@Nonnull List<Role> exemptRoles);

    @Nullable
    AutoModerationRuledData setExemptChannels(@Nonnull List<GuildChannel> exemptChannels);

    @Nonnull
    String getName();

    @Nonnull
    AutoModerationRuledData setName(@Nonnull String name);

    @Nonnull
    EventType getEventType();

    @Nonnull
    AutoModerationRuledData setEventType(@Nonnull EventType eventType);

    @Nonnull
    TriggerType getTriggerType();

    @Nonnull
    AutoModerationRuledData setTriggerType(@Nonnull TriggerType triggerType);

    boolean isEnabled();

    @Nonnull
    AutoModerationRuledData setEnabled(boolean enabled);

    @Nullable
    TriggerMetadata getTriggerMetadata();

    @Nullable
    AutoModerationRuledData setTriggerMetadata(@Nonnull TriggerMetadata triggerMetaData);
}
