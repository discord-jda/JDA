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
package net.dv8tion.jda.api.entities.automod.build

import net.dv8tion.jda.api.entities.automod.AutoModTriggerType
import net.dv8tion.jda.api.utils.data.DataObject
import javax.annotation.Nonnull

/**
 * Abstract base class for all trigger configurations.
 *
 * @param <B>
 * The builder type
</B> */
open class AbstractTriggerConfig<B : AbstractTriggerConfig<B>?> protected constructor(
    /**
     * The type of trigger this config applies to.
     *
     * @return [AutoModTriggerType]
     */
    @get:Nonnull override val type: AutoModTriggerType
) : TriggerConfig {

    @Nonnull
    override fun toData(): DataObject {
        return DataObject.empty()
    }
}
