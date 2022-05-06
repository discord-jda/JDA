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
package net.dv8tion.jda.api.events.interaction.command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Indicates that the Permissions of an interaction-command changed
 *
 * <p>Can be used to get affected Guild and {@link List} of new {@link Permission Permissions}
 */
public class ApplicationCommandUpdatePermissionsEvent extends GenericGuildEvent
{
    private final long commandId;
    private final long applicationId;
    private final List<Permission> permissions;

    public ApplicationCommandUpdatePermissionsEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild, @Nonnull DataObject json)
    {
        super(api, responseNumber, guild);
        this.commandId = Long.parseLong(json.getString("id"));
        this.applicationId = Long.parseLong(json.getString("application_id"));
        this.permissions = json.getArray("permissions")
                .stream(DataArray::getObject)
                .map(obj -> new Permission(api, guild, obj.getBoolean("permission"), obj.getLong("id"), obj.getInt("type")))
                .collect(Collectors.toList());
    }

    /**
     * The new {@link Permission CommandPermissions} of this command.
     *
     * @return Unmodifiable list containing the new CommandPermissions of the affected command.
     */
    @Nonnull
    public List<Permission> getPermissions()
    {
        return Collections.unmodifiableList(permissions);
    }

    /**
     * The id of the command in question.
     *
     * @return id of the command in question.
     */
    public long getCommandId()
    {
        return commandId;
    }

    /**
     * The id of the application whose command has changed.
     *
     * @return id of the application in question.
     */
    public long getApplicationId()
    {
        return applicationId;
    }

    /**
     * Represents a Discord Interaction-Command Permission object.
     */
    public static class Permission
    {
        private final JDA jda;
        private final Guild guild;
        private final boolean grant;
        private final long id;
        private final Type type;

        public Permission(@Nonnull JDA jda, @Nonnull Guild guild, boolean grant, long id, int type)
        {
            Checks.notNull(jda, "JDA");
            Checks.notNull(guild, "Guild");
            this.jda = jda;
            this.guild = guild;
            this.grant = grant;
            this.id = id;
            this.type = Type.fromKey(type);
        }

        /**
         * The affected {@link GuildChannel}
         *
         * @throws IllegalStateException
         *         <ul>
         *             <li>If this Permission is not of Type {@link Type#CHANNEL CHANNEL}</li>
         *             <li>If this Permission targets all channels</li>
         *         </ul>
         *
         * @return affected GuildChannel
         */
        @Nullable
        public GuildChannel getChannel()
        {
            if (type != Type.CHANNEL)
                throw new IllegalStateException("Permission is of type " + type + "!");

            if (id == guild.getIdLong() - 1)
                throw new IllegalStateException("Permission targets all channels!");

            return guild.getGuildChannelById(id);
        }

        /**
         * The affected {@link Role}
         *
         * <p>If this Permission targets @everyone, this will return {@link Guild#getPublicRole()}
         *
         * @throws IllegalStateException
         *         If this Permission is not of type {@link Type#ROLE ROLE}
         *
         *  @return affected Role, or {@link Guild#getPublicRole()}
         */
        @Nullable
        public Role getRole()
        {
            if (type != Type.ROLE)
                throw new IllegalStateException("Permission is of type " + type + "!");

            if (id == guild.getIdLong())
                return guild.getPublicRole();

            return guild.getRoleById(id);
        }

        /**
         * The id of the user in question
         *
         * @throws IllegalStateException
         *         If this Permission is not of type {@link Type#USER USER}
         *
         * @return id of the user in question
         */
        public long getUserId()
        {
            if (type != Type.USER)
                throw new IllegalStateException("Permission is of type " + type + "!");

            return id;
        }

        /**
         * {@link RestAction} to get the target {@link User} in question
         *
         * @throws IllegalStateException
         *         If this Permission is not of type {@link Type#USER USER}
         *
         * @return {@link RestAction} - Type: {@link User}
         */
        @Nonnull
        public RestAction<User> retrieveUser()
        {
            if (type != Type.USER)
                throw new IllegalStateException("Permission is of type " + type + "!");

            return jda.retrieveUserById(id);
        }

        /**
         * Whether this Permission targets the @everyone Role
         *
         * @return Whether this Permission targets the @everyone Role
         *
         * @see    Guild#getPublicRole()
         */
        public boolean isPublicRole()
        {
            if (type != Type.ROLE)
                return false;

            return id == guild.getIdLong();
        }

        /**
         * Whether this Permission targets "All channels"
         *
         * @return Whether this Permission targets all channels
         */
        public boolean isAllChannels()
        {
            if (type != Type.CHANNEL)
                return false;

            return id == guild.getIdLong() - 1;
        }

        /**
         * Whether this Permission is granted
         *
         * @return Whether this Permission is granted
         */
        public boolean isGranted()
        {
            return grant;
        }

        /**
         * The raw id of this Permission
         *
         * <p>The result can either be:
         * <ul>
         *     <li>A {@link Role Role-ID}</li>
         *     <li>A {@link User User-ID}</li>
         *     <li>A {@link GuildChannel Channel-ID}</li>
         *     <li>Guild-ID (@everyone)</li>
         *     <li>Guild-ID - 1 (All Channels)</li>
         * </ul>
         *
         * @see    #getType()
         *
         * @return The raw id of this Permission
         */
        public long getRawId()
        {
            return id;
        }

        /**
         * The {@link Type Type} of this Permission
         *
         * <p>Possible values are:
         * <ul>
         *     <li>{@link Type#ROLE ROLE}</li>
         *     <li>{@link Type#USER USER}</li>
         *     <li>{@link Type#CHANNEL CHANNEL}</li>
         * </ul>
         *
         * @return The type of this Permission
         */
        @Nonnull
        public Type getType()
        {
            return type;
        }

        /**
         * The JDA instance
         *
         * @return JDA instance
         */
        @Nonnull
        public JDA getJDA()
        {
            return jda;
        }

        /**
         * The affected {@link Guild} in which the permissions have been updated
         *
         * @return affected guild
         */
        @Nonnull
        public Guild getGuild()
        {
            return guild;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (!(o instanceof Permission))
                return false;
            Permission that = (Permission) o;
            return grant == that.grant && id == that.id && Objects.equals(jda, that.jda) && Objects.equals(guild, that.guild) && type == that.type;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(grant, id, type);
        }

        @Override
        public String toString()
        {
            return "ApplicationCommandUpdatePermissionsEvent.Permission[" + getType() + "](grant=" + grant + ", id=" + id + ", everyone=" + isPublicRole() + ", all_channels=" + isAllChannels() + ")";
        }

        /**
         * The Type a {@link Permission Permission} can have
         */
        public enum Type {
            /** Placeholder for future types */
            UNKNOWN(-1),

            /** Updated permissions on a role */
            ROLE(1),

            /** Updated permissions on a user */
            USER(2),

            /** Updated permissions on a channel */
            CHANNEL(3),
            ;

            private final int key;

            Type(int key)
            {
                this.key = key;
            }

            /**
             * The raw value for this type or -1 for {@link #UNKNOWN}
             *
             * @return The raw value
             */
            public int getKey()
            {
                return key;
            }

            /**
             * Converts the provided raw type to the enum constant.
             *
             * @param  key
             *         The raw type
             *
             * @return The Type constant or {@link #UNKNOWN}
             */
            @Nonnull
            public static Type fromKey(int key)
            {
                for (Type type : values())
                    if (type.getKey() == key)
                        return type;

                return UNKNOWN;
            }
        }
    }
}
