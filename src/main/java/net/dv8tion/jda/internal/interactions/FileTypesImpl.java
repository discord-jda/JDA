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

package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.interactions.FileType;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import static net.dv8tion.jda.api.interactions.IFilterableFileTypes.MAX_FILE_TYPES;

public final class FileTypesImpl {
    private final List<FileType> fileTypes;

    private FileTypesImpl(List<FileType> fileTypes) {
        this.fileTypes = fileTypes;
    }

    @Nonnull
    public static FileTypesImpl empty() {
        return new FileTypesImpl(new ArrayList<>());
    }

    @Nonnull
    public static FileTypesImpl fromArray(@Nonnull DataArray array) {
        return new FileTypesImpl(
                array.stream(DataArray::getString).map(FileType::new).collect(Collectors.toList()));
    }

    @Nonnull
    public FileTypesImpl copy() {
        return new FileTypesImpl(new ArrayList<>(fileTypes));
    }

    @Nonnull
    @UnmodifiableView
    public List<FileType> asView() {
        return Collections.unmodifiableList(fileTypes);
    }

    public void addAll(@Nonnull Collection<FileType> fileTypes) {
        Checks.noneNull(fileTypes, "File types");
        Checks.check(
                this.fileTypes.size() + fileTypes.size() <= MAX_FILE_TYPES,
                "Cannot have more than %d file types (provided: %d + %d)",
                MAX_FILE_TYPES,
                this.fileTypes.size(),
                fileTypes.size());
        this.fileTypes.addAll(fileTypes);
    }

    public void setAll(@Nonnull Collection<FileType> fileTypes) {
        Checks.noneNull(fileTypes, "File types");
        Checks.check(
                fileTypes.size() <= MAX_FILE_TYPES,
                "Cannot have more than %d file types (provided: %d)",
                MAX_FILE_TYPES,
                fileTypes.size());
        this.fileTypes.clear();
        this.fileTypes.addAll(fileTypes);
    }

    @Nonnull
    public DataArray toData() {
        DataArray array = DataArray.empty();
        for (FileType fileType : fileTypes) {
            array.add(fileType.getValue());
        }
        return array;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FileTypesImpl)) {
            return false;
        }
        FileTypesImpl that = (FileTypesImpl) o;
        return fileTypes.equals(that.fileTypes);
    }

    @Override
    public int hashCode() {
        return fileTypes.hashCode();
    }

    @Override
    public String toString() {
        return fileTypes.toString();
    }
}
