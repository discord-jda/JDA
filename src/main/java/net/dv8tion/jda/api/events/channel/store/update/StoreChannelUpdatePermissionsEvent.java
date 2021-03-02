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

package net.dv8tion.jda.api.events.channel.store.update;

import net.dv8tion.jda.annotations.DeprecatedSince;
import net.dv8tion.jda.annotations.ForRemoval;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.StoreChannel;
import net.dv8tion.jda.api.events.channel.store.GenericStoreChannelEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Indicates that a {@link net.dv8tion.jda.api.entities.StoreChannel StoreChannel}'s permission overrides changed.
 *
 * <p>Can be use to detect when a StoreChannel's permission overrides change and get affected {@link net.dv8tion.jda.api.entities.Role Roles}/{@link net.dv8tion.jda.api.entities.Member Members}.
 *
 * @deprecated This event is no longer feasible due to members not being cached by default. We replaced this event
 *             with {@link net.dv8tion.jda.api.events.guild.override.GenericPermissionOverrideEvent GenericPermissionOverrideEvent} and derivatives.
 */
@Deprecated
@ForRemoval
@DeprecatedSince("4.2.0")
public class StoreChannelUpdatePermissionsEvent extends GenericStoreChannelEvent
{
    private final List<IPermissionHolder> changed;

    public StoreChannelUpdatePermissionsEvent(@Nonnull JDA api, long responseNumber, @Nonnull StoreChannel channel, List<IPermissionHolder> permHolders)
    {
        super(api, responseNumber, channel);
        this.changed = permHolders;
    }

    /**
     * The affected {@link net.dv8tion.jda.api.entities.IPermissionHolder IPermissionHolders}
     *
     * @return The affected permission holders
     *
     * @see    #getChangedRoles()
     * @see    #getChangedMembers()
     */
    @Nonnull
    public List<IPermissionHolder> getChangedPermissionHolders()
    {
        return changed;
    }

    /**
     * List of affected {@link net.dv8tion.jda.api.entities.Role Roles}
     *
     * @return List of affected roles
     */
    @Nonnull
    public List<Role> getChangedRoles()
    {
        return changed.stream()
            .filter(it -> it instanceof Role)
            .map(Role.class::cast)
            .collect(Collectors.toList());
    }

    /**
     * List of affected {@link net.dv8tion.jda.api.entities.Member Members}
     *
     * @return List of affected members
     */
    @Nonnull
    public List<Member> getChangedMembers()
    {
        return changed.stream()
            .filter(it -> it instanceof Member)
            .map(Member.class::cast)
            .collect(Collectors.toList());
    }
}
