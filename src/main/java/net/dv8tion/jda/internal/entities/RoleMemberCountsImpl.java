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

package net.dv8tion.jda.internal.entities;

import gnu.trove.map.TLongIntMap;
import net.dv8tion.jda.api.entities.RoleMemberCounts;
import net.dv8tion.jda.api.utils.LongIntConsumer;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

public class RoleMemberCountsImpl implements RoleMemberCounts {
    private final TLongIntMap roleMemberCounts;

    public RoleMemberCountsImpl(TLongIntMap roleMemberCounts) {
        this.roleMemberCounts = roleMemberCounts;
    }

    @Override
    public int get(long roleId) {
        // Default value is 0
        return roleMemberCounts.get(roleId);
    }

    @Override
    public boolean contains(long roleId) {
        return roleMemberCounts.containsKey(roleId);
    }

    @Override
    public void forEach(@Nonnull LongIntConsumer action) {
        roleMemberCounts.forEachEntry((roleId, count) -> {
            action.accept(roleId, count);
            return true;
        });
    }

    @Nonnull
    @Override
    @Unmodifiable
    public Map<Long, Integer> asMap() {
        Map<Long, Integer> map = new HashMap<>(roleMemberCounts.size());
        roleMemberCounts.forEachEntry((roleId, count) -> {
            map.put(roleId, count);
            return true;
        });
        return map;
    }
}
