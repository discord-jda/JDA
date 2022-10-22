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

package net.dv8tion.jda.api.requests.restaction.pagination;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import javax.annotation.Nonnull;

/**
 * {@link PaginationAction PaginationAction} that paginates the scheduled event users endpoint.
 * <br>Note that this implementation is not considered thread-safe as modifications to the cache are not done
 * with a lock. Calling methods on this class from multiple threads is not recommended.
 *
 * <p><b>Limits:</b><br>
 * Minimum - 1
 * <br>Maximum - 100
 *
 * <p><b>Example</b><br>
 * <pre>{@code
 * // Get every member interested in this event and add the members names to a list
 * public static void getInterestedMembers(ScheduledEvent event) {
 *      // get paginator
 *      ScheduledEventMembersPaginationAction members = event.retrieveInterestedMembers();
 *      // add the name of every interested member to a list
 *      ArrayList<String> memberNames = new ArrayList();
 *      members.forEachAsync(member -> memberNames.add(member.getEffectiveName()));
 * }
 * }</pre>
 */
public interface ScheduledEventMembersPaginationAction extends PaginationAction<Member, ScheduledEventMembersPaginationAction>
{
    /**
     * The current target {@link Guild Guild} for
     * this ScheduledEventMembersPaginationAction.
     *
     * @return The never-null target Guild
     */
    @Nonnull
    Guild getGuild();
}
