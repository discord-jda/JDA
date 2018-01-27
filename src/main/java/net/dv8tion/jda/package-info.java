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
 * Root package of the Java Discord API library containing basic information about JDA
 * and the builder system to connect to the Discord API
 *
 * <p>From here you can navigate to the library features.
 * <ul>
 *     <li>{@link net.dv8tion.jda.core Core Features}
 *     <br>Package which includes the core functionalities of JDA, available throughout all feature pacakges</li>
 *
 *     <li>{@link net.dv8tion.jda.bot Bot Features}
 *     <br>Package which includes functionalities that are only available for accounts of {@link net.dv8tion.jda.core.AccountType#BOT AccountType BOT}</li>
 *
 *     <li>{@link net.dv8tion.jda.client Client Features}
 *     <br>Package which includes functionalities that are only available for accounts of {@link net.dv8tion.jda.core.AccountType#CLIENT AccountType CLIENT}
 *     <br>Such as {@link net.dv8tion.jda.client.entities.Relationship Relationships}</li>
 *
 *     <li>{@link net.dv8tion.jda.webhook Webhook Features}
 *     <br>Functionality to send/execute webhooks without a bound JDA instance</li>
 * </ul>
 */
package net.dv8tion.jda;
