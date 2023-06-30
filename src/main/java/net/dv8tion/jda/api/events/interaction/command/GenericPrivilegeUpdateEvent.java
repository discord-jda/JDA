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
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.interactions.commands.privileges.IntegrationPrivilege;
import net.dv8tion.jda.api.interactions.commands.privileges.PrivilegeTargetType;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Indicates that the privileges of an integration or its commands changed.
 *
 * <p>Can be used to get affected {@link Guild} and the new {@link IntegrationPrivilege IntegrationPrivileges}
 */
public abstract class GenericPrivilegeUpdateEvent extends GenericGuildEvent
{
    private final long targetId;
    private final long applicationId;
    private final List<IntegrationPrivilege> privileges;

    public GenericPrivilegeUpdateEvent(@Nonnull JDA api, long responseNumber, @Nonnull Guild guild,
                                       long targetId, long applicationId, @Nonnull List<IntegrationPrivilege> privileges)
    {
        super(api, responseNumber, guild);
        this.targetId = targetId;
        this.applicationId = applicationId;
        this.privileges = Collections.unmodifiableList(privileges);
    }

    /**
     * The target {@link PrivilegeTargetType Type}.
     *
     * <p>This can either be:
     * <ul>
     *     <li>{@link PrivilegeTargetType#INTEGRATION INTEGRATION} - If the privileges have been changed on the integration-level.</li>
     *     <li>{@link PrivilegeTargetType#COMMAND COMMAND} - If the privileges have been changed on a command.</li>
     * </ul>
     *
     * @return The target type.
     */
    @Nonnull
    public abstract PrivilegeTargetType getTargetType();

    /**
     * The target-id.
     *
     * <p>This can either be the id of an integration, or of a command.
     *
     * @return The target-id.
     *
     * @see #getTargetType()
     */
    public long getTargetIdLong()
    {
        return targetId;
    }

    /**
     * The target-id.
     *
     * <p>This can either be the id of an integration, or of a command.
     *
     * @return The target-id.
     *
     * @see #getTargetType()
     */
    @Nonnull
    public String getTargetId()
    {
        return Long.toUnsignedString(targetId);
    }

    /**
     * The id of the application of which privileges have been changed.
     *
     * @return id of the application of which privileges have been changed.
     */
    public long getApplicationIdLong()
    {
        return applicationId;
    }

    /**
     * The id of the application of which privileges have been changed.
     *
     * @return id of the application of which privileges have been changed.
     */
    @Nonnull
    public String getApplicationId()
    {
        return Long.toUnsignedString(applicationId);
    }

    /**
     * The list of new {@link IntegrationPrivilege IntegrationPrivileges}.
     *
     * @return Unmodifiable list containing the new IntegrationPrivileges.
     */
    @Nonnull
    public List<IntegrationPrivilege> getPrivileges()
    {
        return privileges;
    }
}
