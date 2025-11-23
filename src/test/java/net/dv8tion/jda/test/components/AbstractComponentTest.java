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

package net.dv8tion.jda.test.components;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.utils.ComponentSerializer;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.test.AbstractSnapshotTest;
import net.dv8tion.jda.test.LoadSampleTraits;

import java.util.List;
import java.util.stream.Collectors;

class AbstractComponentTest extends AbstractSnapshotTest implements LoadSampleTraits {
    void assertSerialization(ComponentSerializer serializer, List<? extends Component> components, String suffix) {
        List<DataObject> dataObjects = serializer.serializeAll(components);
        List<FileUpload> fileUploads = serializer.getFileUploads(components);

        List<String> fileNames = fileUploads.stream().map(FileUpload::getName).collect(Collectors.toList());

        String actualSuffix = suffix == null ? "" : (suffix + "-");
        assertWithSnapshot(DataArray.fromCollection(dataObjects), actualSuffix + "data");
        assertWithSnapshot(DataArray.fromCollection(fileNames), actualSuffix + "files");
    }
}
