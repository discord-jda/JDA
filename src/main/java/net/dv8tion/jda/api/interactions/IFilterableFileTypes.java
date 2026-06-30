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

package net.dv8tion.jda.api.interactions;

import net.dv8tion.jda.internal.utils.Checks;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Enables configuring a file type filter.
 */
public interface IFilterableFileTypes<T extends IFilterableFileTypes<T>> {
    /** The number of file type filters that can be applied at once. */
    int MAX_FILE_TYPES = 10;

    /**
     * Adds up to {@value #MAX_FILE_TYPES} file types to filter for.
     *
     * @param  fileTypes
     *         The file types, up to {@value #MAX_FILE_TYPES}
     *
     * @throws IllegalArgumentException
     *         If there are more than {@value #MAX_FILE_TYPES} file types
     *
     * @return This instance for chaining
     */
    @Nonnull
    T addFileTypes(@Nonnull Collection<FileType> fileTypes);

    /**
     * Adds up to {@value #MAX_FILE_TYPES} file types to filter for.
     *
     * @param  fileTypes
     *         The file types, up to {@value #MAX_FILE_TYPES}
     *
     * @throws IllegalArgumentException
     *         If there are more than {@value #MAX_FILE_TYPES} file types
     *
     * @return This instance for chaining
     */
    @Nonnull
    default T addFileTypes(@Nonnull FileType... fileTypes) {
        Checks.noneNull(fileTypes, "File types");
        return addFileTypes(Arrays.asList(fileTypes));
    }

    /**
     * Adds up to {@value #MAX_FILE_TYPES} file extensions to filter for.
     *
     * <p>This is the same as {@link #addFileTypes(Collection)} with {@link FileType#ofExtension(String)}.
     *
     * @param  extensions
     *         The extensions, up to {@value #MAX_FILE_TYPES}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If there are more than {@value #MAX_FILE_TYPES} extensions</li>
     *             <li>If an extension is {@code null} or empty</li>
     *             <li>If the extension does not match {@code [\w\-.]+} (latin letters, digits, dashes or dots)</li>
     *         </ul>
     *
     * @return This instance for chaining
     */
    @Nonnull
    default T addFileTypeExtensions(@Nonnull Collection<String> extensions) {
        Checks.noneNull(extensions, "Extensions");
        return addFileTypes(extensions.stream().map(FileType::ofExtension).collect(Collectors.toList()));
    }

    /**
     * Adds up to {@value #MAX_FILE_TYPES} file extensions to filter for.
     *
     * <p>This is the same as {@link #addFileTypes(Collection)} with {@link FileType#ofExtension(String)}.
     *
     * @param  extensions
     *         The extensions, up to {@value #MAX_FILE_TYPES}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If there are more than {@value #MAX_FILE_TYPES} extensions</li>
     *             <li>If an extension is {@code null} or empty</li>
     *             <li>If the extension does not match {@code [\w\-.]+} (latin letters, digits, dashes or dots)</li>
     *         </ul>
     *
     * @return This instance for chaining
     */
    @Nonnull
    default T addFileTypeExtensions(@Nonnull String... extensions) {
        Checks.noneNull(extensions, "Extensions");
        return addFileTypeExtensions(Arrays.asList(extensions));
    }

    /**
     * Sets up to {@value #MAX_FILE_TYPES} file types to filter for.
     * Pass an empty collection to remove file type filtering.
     *
     * @param  fileTypes
     *         The file types, up to {@value #MAX_FILE_TYPES}
     *
     * @throws IllegalArgumentException
     *         If there are more than {@value #MAX_FILE_TYPES} file types
     *
     * @return This instance for chaining
     */
    @Nonnull
    T setFileTypes(@Nonnull Collection<FileType> fileTypes);

    /**
     * Sets up to {@value #MAX_FILE_TYPES} file types to filter for.
     * Leave the arguments empty to remove file type filtering.
     *
     * @param  fileTypes
     *         The file types, up to {@value #MAX_FILE_TYPES}
     *
     * @throws IllegalArgumentException
     *         If there are more than {@value #MAX_FILE_TYPES} file types
     *
     * @return This instance for chaining
     */
    @Nonnull
    default T setFileTypes(@Nonnull FileType... fileTypes) {
        Checks.noneNull(fileTypes, "File types");
        return setFileTypes(Arrays.asList(fileTypes));
    }

    /**
     * Sets up to {@value #MAX_FILE_TYPES} file extensions to filter for.
     * Pass an empty collection to remove file type filtering.
     *
     * <p>This is the same as {@link #addFileTypes(Collection)} with {@link FileType#ofExtension(String)}.
     *
     * @param  extensions
     *         The extensions, up to {@value #MAX_FILE_TYPES}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If there are more than {@value #MAX_FILE_TYPES} extensions</li>
     *             <li>If an extension is {@code null} or empty</li>
     *             <li>If the extension does not match {@code [\w\-.]+} (latin letters, digits, dashes or dots)</li>
     *         </ul>
     *
     * @return This instance for chaining
     */
    @Nonnull
    default T setFileTypeExtensions(@Nonnull Collection<String> extensions) {
        Checks.noneNull(extensions, "Extensions");
        return setFileTypes(extensions.stream().map(FileType::ofExtension).collect(Collectors.toList()));
    }

    /**
     * Sets up to {@value #MAX_FILE_TYPES} file extensions to filter for.
     * Leave the arguments empty to remove file type filtering.
     *
     * <p>This is the same as {@link #addFileTypes(Collection)} with {@link FileType#ofExtension(String)}.
     *
     * @param  extensions
     *         The extensions, up to {@value #MAX_FILE_TYPES}
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If there are more than {@value #MAX_FILE_TYPES} extensions</li>
     *             <li>If an extension is {@code null} or empty</li>
     *             <li>If the extension does not match {@code [\w\-.]+} (latin letters, digits, dashes or dots)</li>
     *         </ul>
     *
     * @return This instance for chaining
     */
    @Nonnull
    default T setFileTypeExtensions(@Nonnull String... extensions) {
        Checks.noneNull(extensions, "Extensions");
        return setFileTypeExtensions(Arrays.asList(extensions));
    }
}
