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
 * {@link net.dv8tion.jda.core.requests.RestAction RestAction} extensions
 * specifically designed to change the order of discord entities.
 * <br>Such as:
 * <ul>
 *     <li>{@link net.dv8tion.jda.core.requests.restaction.order.CategoryOrderAction Categories}</li>
 *     <li>{@link net.dv8tion.jda.core.requests.restaction.order.ChannelOrderAction Channels}</li>
 *     <li>{@link net.dv8tion.jda.core.requests.restaction.order.RoleOrderAction Roles}</li>
 * </ul>
 *
 * <p>Abstract base implementation can be found at {@link net.dv8tion.jda.core.requests.restaction.order.OrderAction OrderAction}
 *
 * @since 3.0
 */
package net.dv8tion.jda.core.requests.restaction.order;
