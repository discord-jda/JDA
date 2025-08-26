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

package net.dv8tion.jda.test.data;

import net.dv8tion.jda.api.exceptions.DataObjectParsingException;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.test.AbstractSnapshotTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class DataObjectTest extends AbstractSnapshotTest
{
    @Test
    void testMissingKeyException()
    {
        DataObject data = DataObject.empty()
                .put("foo", 1)
                .put("nested_object", DataObject.empty().put("test", "test value"))
                .put("nested_array", DataArray.empty().add("test value"));

        assertThatExceptionOfType(DataObjectParsingException.class)
            .isThrownBy(() -> data.get("bar"))
            .satisfies(exception -> snapshotHandler.compareWithSnapshot(exception.toString(), null));
    }

    @Test
    void testUnexpectedNullException()
    {
        DataObject data = DataObject.empty()
                .put("foo", null)
                .put("nested_object", DataObject.empty().put("test", "test value"))
                .put("nested_array", DataArray.empty().add("test value"));

        assertThatExceptionOfType(DataObjectParsingException.class)
            .isThrownBy(() -> data.getInt("foo"))
            .satisfies(exception -> snapshotHandler.compareWithSnapshot(exception.toString(), null));
    }
}
