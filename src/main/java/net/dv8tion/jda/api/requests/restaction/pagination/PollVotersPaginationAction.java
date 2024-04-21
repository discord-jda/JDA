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

import net.dv8tion.jda.api.entities.User;

/**
 * {@link PaginationAction PaginationAction} that paginates the votes for a poll answer.
 *
 * <p><b>Limits</b><br>
 * Minimum - 1<br>
 * Maximum - 1000
 * <br>Default - 1000
 */
public interface PollVotersPaginationAction extends PaginationAction<User, PollVotersPaginationAction>
{
}
