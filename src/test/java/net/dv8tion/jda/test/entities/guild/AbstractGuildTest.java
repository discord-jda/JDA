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

package net.dv8tion.jda.test.entities.guild;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.SelfUserImpl;
import net.dv8tion.jda.internal.utils.UnlockHook;
import net.dv8tion.jda.internal.utils.cache.MemberCacheViewImpl;
import net.dv8tion.jda.test.Constants;
import net.dv8tion.jda.test.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;

import java.util.EnumSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public abstract class AbstractGuildTest extends IntegrationTest
{
    @Mock
    protected SelfUserImpl selfUser;
    @Mock
    protected MemberImpl selfMember;

    protected GuildImpl guild;

    @BeforeEach
    final void setupGuild()
    {
        when(selfUser.getIdLong()).thenReturn(Constants.MINN_USER_ID);
        when(jda.getSelfUser()).thenReturn(selfUser);
        when(jda.getCacheFlags()).thenReturn(EnumSet.allOf(CacheFlag.class));

        guild = new GuildImpl(jda, Constants.GUILD_ID);

        MemberCacheViewImpl members = guild.getMembersView();
        try (UnlockHook ignored = members.writeLock())
        {
            members.getMap().put(Constants.MINN_USER_ID, selfMember);
        }
    }

    protected void hasPermission(boolean has)
    {
        when(selfMember.hasPermission(any(Permission[].class))).thenReturn(has);
    }
}
