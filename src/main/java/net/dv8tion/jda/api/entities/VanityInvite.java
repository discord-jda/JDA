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

import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Meta data for the vanity invite of a guild
 *
 * @since  4.2.1
 */
public class VanityInvite
{
    private final String code;
    private final int uses;

    public VanityInvite(@Nonnull String code, int uses)
    {
        this.code = code;
        this.uses = uses;
    }

    /**
     * The invite code used for the invite url.
     *
     * @return The code
     */
    @Nonnull
    public String getCode()
    {
        return code;
    }

    /**
     * How many times this invite has been used.
     * <br>This is reset after the invite is changed or removed.
     *
     * @return The invite uses
     */
    public int getUses()
    {
        return uses;
    }

    /**
     * The invite url.
     *
     * @return The invite url
     */
    @Nonnull
    public String getUrl()
    {
        return "https://discord.gg/" + getCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof VanityInvite))
            return false;
        VanityInvite other = (VanityInvite) obj;
        return uses == other.uses && code.equals(other.code);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(code, uses);
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("code", code)
                .toString();
    }
}
