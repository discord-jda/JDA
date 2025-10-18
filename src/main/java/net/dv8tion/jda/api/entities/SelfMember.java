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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.managers.SelfMemberManager;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Represents a Guild-specific {@link SelfUser}.
 *
 * @see Member
 */
public interface SelfMember extends Member
{
    /** Max length of a member bio */
    int MAX_BIO_LENGTH = 190;

    @Nonnull
    @Override
    SelfUser getUser();

    /**
     * Returns the {@link SelfMemberManager} for this SelfMember,
     * used to modify some properties of the currently logged in guild member.
     *
     * <p>If you wish to modify multiple fields,
     * do it in one request by chaining setters before calling {@link RestAction#queue()}.
     *
     * @return The manager of this SelfMember
     */
    @Nonnull
    @CheckReturnValue
    SelfMemberManager getManager();
}
