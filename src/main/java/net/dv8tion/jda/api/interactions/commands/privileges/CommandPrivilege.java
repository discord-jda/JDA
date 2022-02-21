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

package net.dv8tion.jda.api.interactions.commands.privileges;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

/**
 * Privilege used to restrict access to a command within a {@link net.dv8tion.jda.api.entities.Guild Guild}.
 *
 * <p>If the command is {@link Command#isDefaultEnabled() enabled by default}, these can be used to blacklist users or roles from using the command.
 * On the other hand if it is disabled by default, this can be used to whitelist users or roles instead.
 *
 * @see Guild#retrieveCommandPrivileges()
 * @see Guild#updateCommandPrivilegesById(String, Collection)
 * @see Guild#updateCommandPrivileges(Map)
 *
 * @see CommandData#setDefaultEnabled(boolean)
 * @see CommandCreateAction#setDefaultEnabled(boolean)
 * @see Command#isDefaultEnabled()
 */
public class CommandPrivilege implements ISnowflake, SerializableData
{
    private final Type type;
    private final boolean enabled;
    private final long id;

    public CommandPrivilege(@Nonnull Type type, boolean enabled, long id)
    {
        Checks.notNull(type, "Type");
        this.type = type;
        this.enabled = enabled;
        this.id = id;
    }

    /**
     * Creates a privilege that grants access to the command for the provided role.
     *
     * @param  role
     *         The role to grant access to
     *
     * @return CommandPrivilege instance
     */
    @Nonnull
    public static CommandPrivilege enable(@Nonnull Role role)
    {
        Checks.notNull(role, "Role");
        return new CommandPrivilege(Type.ROLE, true, role.getIdLong());
    }

    /**
     * Creates a privilege that grants access to the command for the provided user.
     *
     * @param  user
     *         The user to grant access to
     *
     * @return CommandPrivilege instance
     */
    @Nonnull
    public static CommandPrivilege enable(@Nonnull User user)
    {
        Checks.notNull(user, "User");
        return new CommandPrivilege(Type.USER, true, user.getIdLong());
    }

    /**
     * Creates a privilege that grants access to the command for the provided user.
     *
     * @param  userId
     *         The user to grant access to
     *
     * @return CommandPrivilege instance
     */
    @Nonnull
    public static CommandPrivilege enableUser(@Nonnull String userId)
    {
        return enableUser(MiscUtil.parseSnowflake(userId));
    }

    /**
     * Creates a privilege that grants access to the command for the provided user.
     *
     * @param  userId
     *         The user to grant access to
     *
     * @return CommandPrivilege instance
     */
    @Nonnull
    public static CommandPrivilege enableUser(long userId)
    {
        return new CommandPrivilege(Type.USER, true, userId);
    }

    /**
     * Creates a privilege that grants access to the command for the provided role.
     *
     * @param  roleId
     *         The role to grant access to
     *
     * @return CommandPrivilege instance
     */
    @Nonnull
    public static CommandPrivilege enableRole(@Nonnull String roleId)
    {
        return enableRole(MiscUtil.parseSnowflake(roleId));
    }

    /**
     * Creates a privilege that grants access to the command for the provided role.
     *
     * @param  roleId
     *         The role to grant access to
     *
     * @return CommandPrivilege instance
     */
    @Nonnull
    public static CommandPrivilege enableRole(long roleId)
    {
        return new CommandPrivilege(Type.ROLE, true, roleId);
    }

    /**
     * Creates a privilege that denies access to the command for the provided role.
     *
     * @param  role
     *         The role to deny access for
     *
     * @return CommandPrivilege instance
     */
    @Nonnull
    public static CommandPrivilege disable(@Nonnull Role role)
    {
        Checks.notNull(role, "Role");
        return new CommandPrivilege(Type.ROLE, false, role.getIdLong());
    }

    /**
     * Creates a privilege that denies access to the command for the provided user.
     *
     * @param  user
     *         The user to grant denies for
     *
     * @return CommandPrivilege instance
     */
    @Nonnull
    public static CommandPrivilege disable(@Nonnull User user)
    {
        Checks.notNull(user, "User");
        return new CommandPrivilege(Type.USER, false, user.getIdLong());
    }

    /**
     * Creates a privilege that denies access to the command for the provided user.
     *
     * @param  userId
     *         The user to grant access for
     *
     * @return CommandPrivilege instance
     */
    @Nonnull
    public static CommandPrivilege disableUser(@Nonnull String userId)
    {
        return disableUser(MiscUtil.parseSnowflake(userId));
    }

    /**
     * Creates a privilege that denies access to the command for the provided user.
     *
     * @param  userId
     *         The user to grant access for
     *
     * @return CommandPrivilege instance
     */
    @Nonnull
    public static CommandPrivilege disableUser(long userId)
    {
        return new CommandPrivilege(Type.USER, false, userId);
    }

    /**
     * Creates a privilege that denies access to the command for the provided role.
     *
     * @param  roleId
     *         The role to deny access for
     *
     * @return CommandPrivilege instance
     */
    @Nonnull
    public static CommandPrivilege disableRole(@Nonnull String roleId)
    {
        return disableRole(MiscUtil.parseSnowflake(roleId));
    }

    /**
     * Creates a privilege that denies access to the command for the provided role.
     *
     * @param  roleId
     *         The role to deny access for
     *
     * @return CommandPrivilege instance
     */
    @Nonnull
    public static CommandPrivilege disableRole(long roleId)
    {
        return new CommandPrivilege(Type.ROLE, false, roleId);
    }


    @Override
    public long getIdLong()
    {
        return id;
    }

    /**
     * The {@link Type} of entity this privilege is applied to.
     *
     * @return The target {@link Type}
     */
    @Nonnull
    public Type getType()
    {
        return type;
    }

    /**
     * True if this privilege is granting access to the command
     *
     * @return Whether this privilege grants access
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * True if this privilege is denying access to the command
     *
     * @return Whether this privilege denies access
     */
    public boolean isDisabled()
    {
        return !enabled;
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
        if (!(obj instanceof CommandPrivilege))
            return false;
        return ((CommandPrivilege) obj).id == id;
    }

    @Nonnull
    @Override
    public DataObject toData()
    {
        return DataObject.empty()
                .put("id", id)
                .put("type", type.key)
                .put("permission", enabled);
    }

    /**
     * The target type this privilege applies to.
     */
    public enum Type
    {
        UNKNOWN(-1),
        ROLE(1),
        USER(2);

        private final int key;

        Type(int key)
        {
            this.key = key;
        }

        /**
         * Returns the appropriate enum constant for the given key.
         *
         * @param  key
         *         The API key for the type
         *
         * @return The Type constant, or {@link #UNKNOWN} if there is no known representation
         */
        @Nonnull
        public static Type fromKey(int key)
        {
            for (Type type : values())
            {
                if (type.key == key)
                    return type;
            }
            return UNKNOWN;
        }
    }
}
