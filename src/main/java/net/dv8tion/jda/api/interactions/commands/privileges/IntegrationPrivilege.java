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
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Privilege used to restrict access to a command within a {@link net.dv8tion.jda.api.entities.Guild Guild}.
 *
 * <p>Moderators of a Guild can create these privileges inside the Integrations Menu
 *
 * @see Guild#retrieveCommandPrivileges()
 * @see net.dv8tion.jda.api.events.interaction.command.ApplicationCommandUpdatePrivilegesEvent
 * @see net.dv8tion.jda.api.events.interaction.command.ApplicationUpdatePrivilegesEvent
 */
public class IntegrationPrivilege implements ISnowflake
{
    private final Guild guild;
    private final Type type;
    private final boolean enabled;
    private final long id;

    public IntegrationPrivilege(@Nonnull Guild guild, @Nonnull Type type, boolean enabled, long id)
    {
        this.guild = guild;
        this.type = type;
        this.enabled = enabled;
        this.id = id;
    }

    /**
     * Whether this IntegrationPrivilege targets the {@literal @everyone} Role
     *
     * @return True, if this IntegrationPrivilege targets the {@literal @everyone} Role
     */
    public boolean targetsEveryone()
    {
        return type == Type.ROLE && id == guild.getIdLong();
    }

    /**
     * Whether this IntegrationPrivilege targets "All channels"
     *
     * @return True, if this IntegrationPrivilege targets all channels
     */
    public boolean targetsAllChannels()
    {
        return type == Type.CHANNEL && id == guild.getIdLong() - 1;
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
     * The {@link Guild} this IntegrationPrivilege was created in.
     *
     * @return the guild in which this IntegrationPrivilege was created in.
     */
    @Nonnull
    public Guild getGuild()
    {
        return guild;
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
        return Objects.hash(id, enabled);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof IntegrationPrivilege))
            return false;
        IntegrationPrivilege other = (IntegrationPrivilege) obj;
        return other.id == id && other.enabled == enabled;
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .setType(getType())
                .addMetadata("enabled", enabled)
                .toString();
    }

    /**
     * The target type this privilege applies to.
     */
    public enum Type
    {
        UNKNOWN(-1),
        ROLE(1),
        USER(2),
        CHANNEL(3);

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
