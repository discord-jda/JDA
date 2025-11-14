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

package net.dv8tion.jda.test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public interface LoadSampleTraits {
    default InputStream loadSample(String name) {
        Class<? extends LoadSampleTraits> self = getClass();
        String path = "/samples/" + self.getName().replace('.', '/') + "/" + name;
        InputStream resource = self.getResourceAsStream(path);

        assertThat(resource)
                .as("Resource " + name + " not found at path " + path)
                .isNotNull();

        return resource;
    }
}
