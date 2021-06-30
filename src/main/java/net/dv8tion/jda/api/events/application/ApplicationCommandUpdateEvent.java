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

package net.dv8tion.jda.api.events.application;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a {@link Command} was updated.
 *
 * <p>You cannot get the old command information from this event.
 *
 * <p>This is fired for commands from any application.
 */
public class ApplicationCommandUpdateEvent extends GenericApplicationCommandEvent {
    public ApplicationCommandUpdateEvent(@Nonnull JDA api, long responseNumber, @Nonnull Command command, @Nullable Guild guild) {
        super(api, responseNumber, command, guild);
    }
}
