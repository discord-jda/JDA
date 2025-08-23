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

package net.dv8tion.jda.api.requests.restaction.interactions;

import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.FluentRestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateRequest;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * A {@link InteractionCallbackAction} which can be used to send a message reply for an interaction.
 * <br>You can use {@link #setEphemeral(boolean)} to hide this message from other users.
 */
public interface ReplyCallbackAction extends InteractionCallbackAction<InteractionHook>, MessageCreateRequest<ReplyCallbackAction>, FluentRestAction<InteractionHook, ReplyCallbackAction>
{
    @Nonnull
    @Override
    @CheckReturnValue
    ReplyCallbackAction closeResources();

    /**
     * Set whether this message should be visible to other users.
     * <br>When a message is ephemeral, it will only be visible to the user that used the interaction.
     *
     * <p>Ephemeral messages have some limitations and will be removed once the user restarts their client.
     * <br>Limitations:
     * <ul>
     *     <li>Cannot be reacted to</li>
     *     <li>Can only be retrieved using the {@link InteractionHook#retrieveMessageById(String) InteractionHook}</li>
     * </ul>
     *
     * <b>Note:</b> Your message can appear ephemeral in several cases:
     * <ul>
     *     <li>In guilds the bot is not a member of,
     *     if the member is unable to {@link net.dv8tion.jda.api.Permission#USE_EXTERNAL_APPLICATIONS use external application},
     *     this usually happens for user-installed commands</li>
     *     <li>If the interaction user is unable to {@link net.dv8tion.jda.api.Permission#MESSAGE_SEND send messages}</li>
     *     <li>If the content contains elements the user does not have the permission to send (like files or embeds)</li>
     *     <li>If the content triggered AutoMod</li>
     * </ul>
     *
     * @param  ephemeral
     *         True, if this message should be invisible for other users
     *
     * @return The same reply action, for chaining convenience
     */
    @Nonnull
    @CheckReturnValue
    ReplyCallbackAction setEphemeral(boolean ephemeral);
}
