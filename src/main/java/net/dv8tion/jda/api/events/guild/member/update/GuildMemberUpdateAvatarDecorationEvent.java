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

package net.dv8tion.jda.api.events.guild.member.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.AvatarDecoration;
import net.dv8tion.jda.api.entities.Member;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that a {@link Member} updated their {@link net.dv8tion.jda.api.entities.Guild Guild} {@linkplain AvatarDecoration avatar decoration}.
 *
 * <p>Can be used to retrieve members who change their per guild avatar decoration, the triggering guild, the old avatar decoration and the new avatar decoration.
 *
 * <p>Identifier: {@code avatar_decoration}
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link net.dv8tion.jda.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent to be enabled.
 * <br>{@link net.dv8tion.jda.api.JDABuilder#createDefault(String) createDefault(String)} and
 * {@link net.dv8tion.jda.api.JDABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Additionally, this event requires the {@link net.dv8tion.jda.api.utils.MemberCachePolicy MemberCachePolicy}
 * to cache the updated members. Discord does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 */
public class GuildMemberUpdateAvatarDecorationEvent extends GenericGuildMemberUpdateEvent<AvatarDecoration> {

    public static final String IDENTIFIER = "avatar_decoration";

    public GuildMemberUpdateAvatarDecorationEvent(
            @Nonnull JDA api, long responseNumber, @Nonnull Member member, @Nullable AvatarDecoration next) {
        super(api, responseNumber, member, member.getAvatarDecoration(), next, IDENTIFIER);
    }

    /**
     * The old avatar decoration
     *
     * @return The old avatar decoration
     */
    @Nullable
    public AvatarDecoration getOldAvatarDecoration() {
        return getOldValue();
    }

    /**
     * The new avatar decoration
     *
     * @return The new avatar decoration
     */
    @Nullable
    public AvatarDecoration getNewAvatarDecoration() {
        return getNewValue();
    }
}
