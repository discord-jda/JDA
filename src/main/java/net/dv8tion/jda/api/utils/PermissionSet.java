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

package net.dv8tion.jda.api.utils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;

import java.math.BigInteger;
import java.util.EnumSet;
import java.util.Objects;

import javax.annotation.Nonnull;

public final class PermissionSet {
    public static final int BIT_SIZE = 128;
    public static final int MAX_STRING_SIZE = BigInteger.ONE
            .shiftLeft(BIT_SIZE)
            .subtract(BigInteger.ONE)
            .toString()
            .length();
    public static final PermissionSet ZERO = new PermissionSet(0, 0);

    private final long high;
    private final long low;

    private PermissionSet(long high, long low) {
        this.high = high;
        this.low = low;
    }

    @Nonnull
    public static PermissionSet parse(@Nonnull String permissions) {
        Checks.notEmpty(permissions, "Permissions");
        Checks.notLonger(permissions, MAX_STRING_SIZE, "Permission string");

        BigInteger value = new BigInteger(permissions);
        if (value.signum() < 0) {
            throw new IllegalArgumentException("Permission value must be unsigned");
        }
        if (value.bitLength() > BIT_SIZE) {
            throw new IllegalArgumentException(
                    Helpers.format("Value exceeds %d bits. Please update your library version.", BIT_SIZE));
        }

        long low = value.longValue();
        long high = value.shiftRight(64).longValue();

        return new PermissionSet(high, low);
    }

    @Nonnull
    public PermissionSet and(@Nonnull PermissionSet other) {
        return new PermissionSet(this.high & other.high, this.low & other.low);
    }

    @Nonnull
    public PermissionSet or(@Nonnull PermissionSet other) {
        return new PermissionSet(this.high | other.high, this.low | other.low);
    }

    @Nonnull
    public PermissionSet xor(@Nonnull PermissionSet other) {
        return new PermissionSet(this.high ^ other.high, this.low ^ other.low);
    }

    @Nonnull
    public PermissionSet not() {
        return new PermissionSet(~this.high, ~this.low);
    }

    public boolean has(int bit) {
        ensureBit(bit);
        if (bit < 64) {
            return (low & (1L << bit)) != 0;
        } else {
            return (high & (1L << (bit - 64))) != 0;
        }
    }

    public boolean has(@Nonnull Permission permission) {
        return has(permission.getOffset());
    }

    @Nonnull
    public PermissionSet withBit(int bit) {
        ensureBit(bit);
        if (bit < 64) {
            return new PermissionSet(high, low | (1L << bit));
        } else {
            return new PermissionSet(high | (1L << (bit - 64)), low);
        }
    }

    @Nonnull
    public PermissionSet with(@Nonnull Permission permission) {
        return withBit(permission.getOffset());
    }

    @Nonnull
    public PermissionSet withoutBit(int bit) {
        ensureBit(bit);
        if (bit < 64) {
            return new PermissionSet(high, low & ~(1L << bit));
        } else {
            return new PermissionSet(high & ~(1L << (bit - 64)), low);
        }
    }

    @Nonnull
    public PermissionSet without(@Nonnull Permission permission) {
        return withoutBit(permission.getOffset());
    }

    @Nonnull
    public BigInteger toBigInteger() {
        BigInteger hi = BigInteger.valueOf(high).shiftLeft(64);
        BigInteger lo = BigInteger.valueOf(low);

        return hi.add(lo);
    }

    @Nonnull
    public EnumSet<Permission> toEnumSet() {
        EnumSet<Permission> set = EnumSet.noneOf(Permission.class);
        for (Permission value : Permission.values()) {
            if (value != Permission.UNKNOWN && has(value)) {
                set.add(value);
            }
        }
        return set;
    }

    private static void ensureBit(int bit) {
        if (bit < 0 || bit >= BIT_SIZE) {
            throw new IllegalArgumentException("bit index out of range [0," + (BIT_SIZE - 1) + ")");
        }
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("raw", toBigInteger().toString(16))
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PermissionSet)) {
            return false;
        }
        PermissionSet that = (PermissionSet) o;
        return high == that.high && low == that.low;
    }

    @Override
    public int hashCode() {
        return Objects.hash(high, low);
    }
}
