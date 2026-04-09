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

import javax.annotation.Nonnull;

/**
 * Represents a "ThumbHash", a very compact representation of an image,
 * stored inline with a media's data, enabling a smoother loading experience.
 *
 * @see <a href="https://evanw.github.io/thumbhash/" target="_blank">ThumbHash website</a>
 */
public interface Placeholder {
    /**
     * The version of the placeholder
     *
     * @return The placeholder version
     */
    int getVersion();

    /**
     * The ThumbHash as a base64 string, as transmitted by Discord.
     *
     * @return Base64 of the ThumbHash
     *
     * @see   #asBinary()
     */
    @Nonnull
    String asBase64();

    /**
     * The binary Thumbhash. This does not represent a valid image of any format,
     * it must be converted, see <a href="https://github.com/evanw/thumbhash" target="_blank">ThumbHash implementations</a>
     *
     * @return Base64 of the Thumbhash
     *
     * @see   #asBase64()
     */
    @Nonnull
    byte[] asBinary();
}
