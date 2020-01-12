/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.data.MemberData;
import net.dv8tion.jda.internal.entities.MemberImpl;

import javax.annotation.Nonnull;

@SuppressWarnings("ConstantConditions")
public class GuildMemberUpdateDigestEvent extends GenericGuildMemberUpdateEvent<MemberData>
{
    public static final String IDENTIFIER = "member-data";

    public GuildMemberUpdateDigestEvent(@Nonnull JDA api, long responseNumber, @Nonnull Member member, @Nonnull MemberData previous)
    {
        super(api, responseNumber, member, previous, ((MemberImpl) member).getMutableMemberData(), IDENTIFIER);
    }

    @Nonnull
    @Override
    public MemberData getOldValue()
    {
        return super.getOldValue();
    }

    @Nonnull
    @Override
    public MemberData getNewValue()
    {
        return super.getNewValue();
    }
}
