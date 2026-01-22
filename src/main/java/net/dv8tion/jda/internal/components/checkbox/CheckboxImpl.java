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

package net.dv8tion.jda.internal.components.checkbox;

import net.dv8tion.jda.api.components.checkbox.Checkbox;
import net.dv8tion.jda.api.components.label.LabelChildComponentUnion;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.components.AbstractComponentImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.EntityString;

import java.util.Objects;

import javax.annotation.Nonnull;

public class CheckboxImpl extends AbstractComponentImpl implements Checkbox, LabelChildComponentUnion {
    private final int uniqueId;
    private final String customId;
    private final boolean isDefault;

    public CheckboxImpl(int uniqueId, String customId, boolean isDefault) {
        this.uniqueId = uniqueId;
        this.customId = customId;
        this.isDefault = isDefault;
    }

    public CheckboxImpl(DataObject data) {
        this.uniqueId = data.getInt("id", -1);
        this.customId = data.getString("custom_id");
        this.isDefault = data.getBoolean("default", false);
    }

    @Nonnull
    @Override
    public Type getType() {
        return Type.CHECKBOX;
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
    public boolean isDefault() {
        return isDefault;
    }

    @Nonnull
    @Override
    public CheckboxImpl withUniqueId(int uniqueId) {
        Checks.positive(uniqueId, "Unique ID");
        return new CheckboxImpl(uniqueId, customId, isDefault);
    }

    @Nonnull
    @Override
    public CheckboxImpl withCustomId(@Nonnull String customId) {
        Checks.notBlank(customId, "Custom ID");
        Checks.notLonger(customId, CUSTOM_ID_MAX_LENGTH, "Custom ID");
        return new CheckboxImpl(uniqueId, customId, isDefault);
    }

    @Nonnull
    @Override
    public CheckboxImpl withDefault(boolean isDefault) {
        return new CheckboxImpl(uniqueId, customId, isDefault);
    }

    @Nonnull
    @Override
    public DataObject toData() {
        DataObject object = DataObject.empty().put("type", getType().getKey()).put("custom_id", customId);
        if (uniqueId >= 0) {
            object.put("id", uniqueId);
        }
        if (isDefault) {
            object.put("default", true);
        }

        return object;
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof CheckboxImpl)) {
            return false;
        }
        CheckboxImpl that = (CheckboxImpl) o;
        return uniqueId == that.uniqueId && customId.equals(that.customId) && isDefault == that.isDefault;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId, customId, isDefault);
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("id", customId)
                .addMetadata("selected", isDefault)
                .toString();
    }
}
