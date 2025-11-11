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

package net.dv8tion.jda.internal.entities.channel.mixin.concrete;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.channel.mixin.middleman.MessageChannelMixin;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import net.dv8tion.jda.internal.requests.RestActionImpl;

import javax.annotation.Nonnull;

public interface PrivateChannelMixin<T extends PrivateChannelMixin<T>>
        extends PrivateChannel, MessageChannelMixin<T> {

    @Nonnull
    @Override
    default String getName() {
        User user = getUser();
        if (user == null) {
            // don't break or override the contract of @NonNull
            return "";
        }
        return user.getName();
    }

    @Nonnull
    @Override
    default RestAction<User> retrieveUser() {
        User user = getUser();
        if (user != null) {
            return new CompletedRestAction<>(getJDA(), user);
        }
        // even if the user blocks the bot, this does not fail.
        return retrievePrivateChannel().map(PrivateChannel::getUser);
    }

    @Nonnull
    default RestAction<PrivateChannel> retrievePrivateChannel() {
        Route.CompiledRoute route = Route.Channels.GET_CHANNEL.compile(getId());
        return new RestActionImpl<>(getJDA(), route, (response, request) -> ((JDAImpl) getJDA())
                .getEntityBuilder()
                .createPrivateChannel(response.getObject()));
    }
}
