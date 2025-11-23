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

package net.dv8tion.jda.internal.components.attachmentupload;

import net.dv8tion.jda.api.components.attachmentupload.AttachmentUpload;
import net.dv8tion.jda.api.components.label.LabelChildComponentUnion;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.AbstractComponentImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;

import java.util.Objects;

import javax.annotation.Nonnull;

public class AttachmentUploadImpl extends AbstractComponentImpl implements AttachmentUpload, LabelChildComponentUnion {
    protected final int uniqueId;
    protected final String customId;
    protected final int minValues;
    protected final int maxValues;
    protected final boolean required;

    public AttachmentUploadImpl(DataObject data) {
        this(
                data.getInt("id", -1),
                data.getString("custom_id"),
                data.getInt("min_values", 1),
                data.getInt("max_values", 1),
                data.getBoolean("required", true));
    }

    public AttachmentUploadImpl(int uniqueId, String customId, int minValues, int maxValues, boolean required) {
        this.uniqueId = uniqueId;
        this.customId = customId;
        this.minValues = minValues;
        this.maxValues = maxValues;
        this.required = required;
    }

    @Nonnull
    @Override
    public Type getType() {
        return Type.FILE_UPLOAD;
    }

    @Nonnull
    @Override
    public AttachmentUploadImpl withUniqueId(int uniqueId) {
        Checks.positive(uniqueId, "Unique ID");
        return new AttachmentUploadImpl(uniqueId, customId, minValues, maxValues, required);
    }

    @Override
    public int getUniqueId() {
        return uniqueId;
    }

    @Nonnull
    @Override
    public String getCustomId() {
        return customId;
    }

    @Override
    public int getMinValues() {
        return minValues;
    }

    @Override
    public int getMaxValues() {
        return maxValues;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Nonnull
    @Override
    public DataObject toData() {
        DataObject json = DataObject.empty()
                .put("type", getType().getKey())
                .put("custom_id", customId)
                .put("required", required)
                .put("min_values", minValues)
                .put("max_values", maxValues);

        if (uniqueId >= 0) {
            json.put("id", uniqueId);
        }

        return json;
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("custom_id", customId)
                .addMetadata("required", required)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AttachmentUploadImpl)) {
            return false;
        }
        AttachmentUploadImpl that = (AttachmentUploadImpl) o;
        return uniqueId == that.uniqueId
                && minValues == that.minValues
                && maxValues == that.maxValues
                && required == that.required
                && Objects.equals(customId, that.customId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId, customId, minValues, maxValues, required);
    }
}
