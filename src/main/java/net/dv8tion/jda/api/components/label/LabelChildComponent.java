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

package net.dv8tion.jda.api.components.label;

import net.dv8tion.jda.api.components.Component;

import javax.annotation.Nonnull;

/**
 * Represents a component that can be used inside {@link Label Labels}. This includes:
 * <ul>
 *     <li>{@link net.dv8tion.jda.api.components.textinput.TextInput TextInput}</li>
 *     <li>{@link net.dv8tion.jda.api.components.selections.StringSelectMenu StringSelectMenu}</li>
 * </ul>
 */
public interface LabelChildComponent extends Component
{
    @Nonnull
    @Override
    LabelChildComponent withUniqueId(int uniqueId);
}
