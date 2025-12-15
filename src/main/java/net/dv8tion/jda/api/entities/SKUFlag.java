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

package net.dv8tion.jda.api.entities;

import java.util.EnumSet;

import javax.annotation.Nonnull;

public enum SKUFlag {
    /**
     * SKU is available for purchase
     */
    AVAILABLE(2),
    /**
     * Recurring SKU that can be purchased by a user and applied to a single server. Grants access to every user in that server.
     */
    GUILD_SUBSCRIPTION(7),
    /**
     * Recurring SKU purchased by a user for themselves. Grants access to the purchasing user in every server.
     */
    USER_SUBSCRIPTION(8);

    private final int offset;
    private final int raw;

    SKUFlag(int offset) {
        this.offset = offset;
        this.raw = 1 << offset;
    }

    public int getOffset() {
        return offset;
    }

    @Nonnull
    public static EnumSet<SKUFlag> getFlags(int flags) {
        if (flags == 0) {
            return EnumSet.noneOf(SKUFlag.class);
        }

        EnumSet<SKUFlag> flagSet = EnumSet.noneOf(SKUFlag.class);
        for (SKUFlag flag : SKUFlag.values()) {
            if ((flags & flag.raw) == flag.raw) {
                flagSet.add(flag);
            }
        }
        return flagSet;
    }
}
