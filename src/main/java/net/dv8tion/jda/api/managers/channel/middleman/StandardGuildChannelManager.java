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

package net.dv8tion.jda.api.managers.channel.middleman;

import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.managers.channel.attribute.ICategorizableChannelManager;
import net.dv8tion.jda.api.managers.channel.attribute.IPermissionContainerManager;
import net.dv8tion.jda.api.managers.channel.attribute.IPositionableChannelManager;

/**
 * Manager providing functionality common for all {@link net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel StandardGuildChannels}.
 *
 * <p><b>Example</b>
 * <pre>{@code
 * manager.setName("help")
 *        .setParent(categoryChannel)
 *        .queue();
 * manager.reset(ChannelManager.PARENT | ChannelManager.NAME)
 *        .putPermissionOverride(member, 0, Permission.ALL_PERMISSIONS)
 *        .queue();
 * }</pre>
 *
 * @see StandardGuildChannel#getManager()
 */
public interface StandardGuildChannelManager<T extends StandardGuildChannel, M extends StandardGuildChannelManager<T, M>>
        extends IPermissionContainerManager<T, M>,
        IPositionableChannelManager<T, M>,
        ICategorizableChannelManager<T, M>
{
}
