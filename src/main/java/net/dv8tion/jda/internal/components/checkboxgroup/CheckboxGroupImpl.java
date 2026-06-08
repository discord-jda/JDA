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

package net.dv8tion.jda.internal.components.checkboxgroup;

import net.dv8tion.jda.api.components.checkboxgroup.CheckboxGroup;
import net.dv8tion.jda.api.components.checkboxgroup.CheckboxGroupOption;
import net.dv8tion.jda.api.components.label.LabelChildComponentUnion;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.AbstractComponentImpl;
import net.dv8tion.jda.internal.utils.EntityString;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

public class CheckboxGroupImpl extends AbstractComponentImpl implements CheckboxGroup, LabelChildComponentUnion {
    private final int uniqueId;
    private final String customId;
    private final List<CheckboxGroupOption> options;
    private final int minValues, maxValues;
    private final boolean required;

    public CheckboxGroupImpl(
            int uniqueId,
            String customId,
            List<CheckboxGroupOption> options,
            int minValues,
            int maxValues,
            boolean required) {
        this.uniqueId = uniqueId;
        this.customId = customId;
        this.options = Helpers.copyAsUnmodifiableList(options);
        this.minValues = minValues;
        this.maxValues = maxValues;
        this.required = required;
    }

    public CheckboxGroupImpl(DataObject data) {
        this.uniqueId = data.getInt("id", -1);
        this.customId = data.getString("custom_id");
        this.options = data.getArray("options").stream(DataArray::getObject)
                .map(CheckboxGroupOption::fromData)
                .collect(Helpers.toUnmodifiableList());
        this.minValues = data.getInt("min_values", -1);
        this.maxValues = data.getInt("max_values", -1);
        this.required = data.getBoolean("required", true);
    }

    @Nonnull
    @Override
    public Type getType() {
        return Type.CHECKBOX_GROUP;
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

    @Nonnull
    @Override
    @Unmodifiable
    public List<CheckboxGroupOption> getOptions() {
        return options;
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
    public CheckboxGroupImpl withUniqueId(int uniqueId) {
        return new CheckboxGroupImpl(uniqueId, customId, options, minValues, maxValues, required);
    }

    @Nonnull
    @Override
    public DataObject toData() {
        DataObject object = DataObject.empty()
                .put("type", getType().getKey())
                .put("custom_id", customId)
                .put("options", DataArray.fromCollection(options));
        if (uniqueId != -1) {
            object.put("id", uniqueId);
        }
        if (minValues != -1) {
            object.put("min_values", minValues);
        }
        if (maxValues != -1) {
            object.put("max_values", maxValues);
        }
        if (!required) {
            object.put("required", false);
        }

        return object;
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof CheckboxGroupImpl)) {
            return false;
        }
        CheckboxGroupImpl that = (CheckboxGroupImpl) o;
        return uniqueId == that.uniqueId
                && customId.equals(that.customId)
                && options.equals(that.options)
                && minValues == that.minValues
                && maxValues == that.maxValues
                && required == that.required;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId, customId, options, minValues, maxValues, required);
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("custom_id", customId)
                .addMetadata("required", required)
                .toString();
    }
}
