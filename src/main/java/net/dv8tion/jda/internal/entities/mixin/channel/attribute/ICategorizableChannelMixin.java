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

package net.dv8tion.jda.internal.entities.mixin.channel.attribute;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.api.entities.ICategorizableChannel;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.internal.entities.mixin.channel.middleman.GuildChannelMixin;

public interface ICategorizableChannelMixin<T extends ICategorizableChannelMixin<T>> extends ICategorizableChannel, GuildChannelMixin<T>, IPermissionContainerMixin<T>
{
    // ---- Default implementations of interface ----
    @Override
    default boolean isSynced() {
        IPermissionContainerMixin<?> parent = (IPermissionContainerMixin<?>) getParentCategory();
        if (parent == null)
            return true; // Channels without a parent category are always considered synced. Also the case for categories.

        TLongObjectMap<PermissionOverride> parentOverrides = parent.getPermissionOverrideMap();
        TLongObjectMap<PermissionOverride> overrides = getPermissionOverrideMap();
        if (parentOverrides.size() != overrides.size())
            return false;

        // Check that each override matches with the parent override
        for (PermissionOverride override : parentOverrides.valueCollection())
        {
            PermissionOverride ourOverride = overrides.get(override.getIdLong());
            if (ourOverride == null) // this means we don't have the parent override => not synced
                return false;
            // Permissions are different => not synced
            if (ourOverride.getAllowedRaw() != override.getAllowedRaw() || ourOverride.getDeniedRaw() != override.getDeniedRaw())
                return false;
        }

        // All overrides exist and are the same as the parent => synced
        return true;
    }

    // ---- State Accessors ----
    T setParentCategory(long parentCategoryId);
}
