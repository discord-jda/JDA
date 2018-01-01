/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Extensions of {@link net.dv8tion.jda.core.requests.RestAction RestAction} that allow
 * to access paginated discord endpoints like {@link net.dv8tion.jda.core.requests.Route.Messages#GET_REACTION_USERS Route.Messages.GET_REACTION_USERS}
 * <br>The {@link net.dv8tion.jda.core.requests.restaction.pagination.PaginationAction PaginationAction} is designed to work
 * as an {@link java.lang.Iterable Iterable} of the specified endpoint. Each implementation specifies the endpoints it will
 * use in the class-level javadoc
 *
 * @since 3.1
 */
package net.dv8tion.jda.core.requests.restaction.pagination;
