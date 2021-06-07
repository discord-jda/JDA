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
package net.dv8tion.jda.api.events.channel.voice.update;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * Indicates that a {@link VoiceChannel VoiceChannel}'s region changed.
 *
 * <p>Can be used to get affected VoiceChannel, affected Guild and previous region.
 *
 * <p>Identifier: {@code region}
 */
public class VoiceChannelUpdateRegionEvent extends GenericVoiceChannelUpdateEvent<String>
{
    public static final String IDENTIFIER = "region";

    public VoiceChannelUpdateRegionEvent(@NotNull JDA api, long responseNumber, @NotNull VoiceChannel channel, @Nullable String oldRegion)
    {
        super(api, responseNumber, channel, oldRegion, channel.getRegionRaw(), IDENTIFIER);
    }

    /**
     * The old {@link Region}
     *
     * @return The old region
     */
    @Nonnull
    public Region getOldRegion()
    {
        return getOldValue() == null ? Region.AUTOMATIC : Region.fromKey(getOldValue());
    }

    /**
     * The new {@link Region}
     *
     * @return The new region
     */
    @Nonnull
    public Region getNewRegion()
    {
        return getNewValue() == null ? Region.AUTOMATIC : Region.fromKey(getNewValue());
    }

    /**
     * The old raw region String
     *
     * @return The old raw region String
     * This will return null if the Region of this {@link VoiceChannel VoiceChannel} is set to Automatic.
     */
    @Nullable
    public String getOldRegionRaw()
    {
        return getOldValue();
    }

    /**
     * The new raw region String
     *
     * @return The new raw region String
     * This will return null if the Region of this {@link VoiceChannel VoiceChannel} is set to Automatic.
     */
    @Nullable
    public String getNewRegionRaw()
    {
        return getNewValue();
    }
}
