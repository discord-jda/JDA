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

import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Represents an image format support by Discord's CDNs.
 *
 * <p>Format support varies based on the CDN endpoint.
 *
 * @see <a href="https://discord.com/developers/docs/reference#image-formatting" target="_blank">Discord image formats and CDN endpoint compatibility</a>
 */
public final class ImageFormat {
    /**
     * Lossy static image format.
     * <br>Generally has a smaller size but can have visual artifacts and doesn't support transparency.
     *
     * <p>Requesting an image with this format should always work.
     */
    public static final ImageFormat JPG = new ImageFormat("jpg", Collections.emptyList());

    /**
     * Lossless static image format.
     * <br>Content is compressed but larger than other lossy image formats.
     *
     * <p>Requesting an image with this format should always work.
     */
    public static final ImageFormat PNG = new ImageFormat("png", Collections.emptyList());

    /**
     * Lossless animated image format.
     * <br>Content is very poorly compressed, and only supports up to 255 colors.
     * <br>Using {@link #ANIMATED_WEBP} is recommended instead.
     *
     * <p>Requesting an image with this format may fail with an <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status/415" target="_blank">HTTP 415 (Unsupported Media Type)</a>
     * if the image was originally uploaded as a WebP.
     *
     * @see #ANIMATED_WEBP
     */
    public static final ImageFormat GIF = new ImageFormat("gif", Collections.emptyList());

    /**
     * Lossy or lossless static image format.
     * <br>Sizes can be similar to {@link #JPG} with the addition of transparency support.
     *
     * <p>This is the format Discord recommends for static images.
     * Requesting an image with this format should always work.
     */
    public static final ImageFormat STATIC_WEBP = new ImageFormat("webp", Collections.emptyList());

    /**
     * Lossy or lossless animated image format.
     * <br>Sizes will be considerably smaller than {@link #GIF}, however, encoding can also be slower.
     *
     * <p>This is the format Discord recommends for animated images.
     * Requesting an image with this format should always work, including static images.
     */
    public static final ImageFormat ANIMATED_WEBP = new ImageFormat("webp", Arrays.asList("animated", "true"));

    private final String extension;
    private final List<String> queryParameters;

    private ImageFormat(String extension, List<String> queryParameters) {
        this.extension = extension;
        this.queryParameters = Helpers.copyAsUnmodifiableList(queryParameters);
    }

    /**
     * Creates an {@link ImageFormat} using the provided extension.
     *
     * @param  extension
     *         The extension of the image
     *
     * @throws IllegalArgumentException
     *         If the extension is {@code null}
     *
     * @return The new {@link ImageFormat}
     */
    @Nonnull
    public static ImageFormat of(@Nonnull String extension) {
        Checks.notBlank(extension, "Extension");
        return new ImageFormat(extension, Collections.emptyList());
    }

    /**
     * Creates an {@link ImageFormat} using the provided extension.
     *
     * @param  extension
     *         The extension of the image
     * @param  queryParameters
     *         Query parameters to add to URLs, must be a multiple of 2
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If any argument is {@code null}</li>
     *             <li>If the extension is blank</li>
     *             <li>If the query parameter list's length is not a multiple of 2</li>
     *         </ul>
     *
     * @return The new {@link ImageFormat}
     */
    @Nonnull
    public static ImageFormat of(@Nonnull String extension, @Nonnull List<String> queryParameters) {
        Checks.notBlank(extension, "Extension");
        Checks.notNull(queryParameters, "Query parameters");
        Checks.check((queryParameters.size() & 1) == 0, "Query parameters length must be a multiple of 2");
        return new ImageFormat(extension, queryParameters);
    }

    /**
     * Returns the extension of this image format.
     *
     * @return The extension
     */
    @Nonnull
    public String getExtension() {
        return extension;
    }

    /**
     * Returns the query parameters added to URLs by this image format.
     * <br>The list's size is a multiple of 2, the first item is the key and the second is the value.
     *
     * @return An immutable list of query parameters
     */
    @Nonnull
    @Unmodifiable
    public List<String> getQueryParameters() {
        return queryParameters;
    }
}
