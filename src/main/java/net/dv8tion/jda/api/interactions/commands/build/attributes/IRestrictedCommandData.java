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

package net.dv8tion.jda.api.interactions.commands.build.attributes;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

import javax.annotation.Nonnull;

/**
 * Builder for permission restricted Application Commands.
 *
 * @see net.dv8tion.jda.api.interactions.commands.build.CommandData
 * @see net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
 * @see net.dv8tion.jda.api.interactions.commands.build.EntryPointCommandData
 */
public interface IRestrictedCommandData extends INamedCommandData
{
    /**
     * Sets the {@link net.dv8tion.jda.api.Permission Permissions} that a user must have in a specific channel to be able to use this command.
     * <br>By default, everyone can use this command ({@link DefaultMemberPermissions#ENABLED}). Additionally, a command can be disabled for everyone but admins via {@link DefaultMemberPermissions#DISABLED}.
     * <p>These configurations can be overwritten by moderators in each guild. See {@link Command#retrievePrivileges(net.dv8tion.jda.api.entities.Guild)} to get moderator defined overrides.
     *
     * @param  permission
     *         {@link DefaultMemberPermissions} representing the default permissions of this command.
     *
     * @return The builder instance, for chaining
     *
     * @see DefaultMemberPermissions#ENABLED
     * @see DefaultMemberPermissions#DISABLED
     */
    @Nonnull
    IRestrictedCommandData setDefaultPermissions(@Nonnull DefaultMemberPermissions permission);

    /**
     * Gets the {@link DefaultMemberPermissions} of this command.
     * <br>If no permissions have been set, this returns {@link DefaultMemberPermissions#ENABLED}.
     *
     * @return DefaultMemberPermissions of this command.
     *
     * @see    DefaultMemberPermissions#ENABLED
     * @see    DefaultMemberPermissions#DISABLED
     */
    @Nonnull
    DefaultMemberPermissions getDefaultPermissions();
}
