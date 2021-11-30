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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IPermissionContainer;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
import net.dv8tion.jda.internal.entities.mixin.channel.middleman.GuildChannelMixin;
import net.dv8tion.jda.internal.requests.restaction.PermissionOverrideActionImpl;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public interface IPermissionContainerMixin<T extends IPermissionContainerMixin<T>> extends IPermissionContainer, GuildChannelMixin<T>
{
    // ---- Default implementations of interface ----
    @Override
    default PermissionOverride getPermissionOverride(@Nonnull IPermissionHolder permissionHolder)
    {
        Checks.notNull(permissionHolder, "Permission Holder");
        Checks.check(permissionHolder.getGuild().equals(getGuild()), "Provided permission holder is not from the same guild as this channel!");

        TLongObjectMap<PermissionOverride> overrides = getPermissionOverrideMap();
        return overrides.get(permissionHolder.getIdLong());
    }

    @Nonnull
    @Override
    default List<PermissionOverride> getPermissionOverrides()
    {
        TLongObjectMap<PermissionOverride> overrides = getPermissionOverrideMap();
        return Arrays.asList(overrides.values(new PermissionOverride[overrides.size()]));
    }

    @Nonnull
    @Override
    default PermissionOverrideAction putPermissionOverride(@Nonnull IPermissionHolder permissionHolder)
    {
        checkPermission(Permission.MANAGE_PERMISSIONS);
        Checks.notNull(permissionHolder, "PermissionHolder");
        Checks.check(permissionHolder.getGuild().equals(getGuild()), "Provided permission holder is not from the same guild as this channel!");
        return new PermissionOverrideActionImpl(getJDA(), this, permissionHolder);
    }


    // --- Default implementation of parent mixins hooks ----
    @Override
    @Nonnull
    default IPermissionContainer getPermissionContainer()
    {
        return this;
    }


    // ---- State Accessors ----
    TLongObjectMap<PermissionOverride> getPermissionOverrideMap();
}
