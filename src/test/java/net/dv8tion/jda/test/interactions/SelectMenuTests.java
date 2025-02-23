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

package net.dv8tion.jda.test.interactions;

import net.dv8tion.jda.api.interactions.components.selects.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selects.EntitySelectMenu.Builder;
import net.dv8tion.jda.api.interactions.components.selects.EntitySelectMenu.DefaultValue;
import net.dv8tion.jda.api.interactions.components.selects.EntitySelectMenu.SelectTarget;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class SelectMenuTests
{
    @Test
    void testEntitySelectDefaultValueValid()
    {
        Builder builder = EntitySelectMenu.create("customid", SelectTarget.ROLE);
        builder.setDefaultValues(DefaultValue.role("1234"));

        EntitySelectMenu menu = builder.build();
        DataObject value = menu.toData().getArray("default_values").getObject(0);

        assertThat(menu.getDefaultValues()).containsExactly(DefaultValue.role("1234"));
        assertThat(value.getString("type")).isEqualTo("role");
        assertThat(value.getString("id")).isEqualTo("1234");

        builder = EntitySelectMenu.create("customid", SelectTarget.USER);
        builder.setDefaultValues(DefaultValue.user("1234"));

        menu = builder.build();
        value = menu.toData().getArray("default_values").getObject(0);

        assertThat(menu.getDefaultValues()).containsExactly(DefaultValue.user("1234"));
        assertThat(value.getString("type")).isEqualTo("user");
        assertThat(value.getString("id")).isEqualTo("1234");

        builder = EntitySelectMenu.create("customid", SelectTarget.CHANNEL);
        builder.setDefaultValues(DefaultValue.channel("1234"));

        menu = builder.build();
        value = menu.toData().getArray("default_values").getObject(0);

        assertThat(menu.getDefaultValues()).containsExactly(DefaultValue.channel("1234"));
        assertThat(value.getString("type")).isEqualTo("channel");
        assertThat(value.getString("id")).isEqualTo("1234");
    }

    @Test
    void testEntitySelectDefaultValueInvalid()
    {
        assertThatIllegalArgumentException().isThrownBy(() -> {
            Builder builder = EntitySelectMenu.create("customid", SelectTarget.ROLE);
            builder.setDefaultValues(DefaultValue.user("1234"));
        }).withMessage("The select menu supports types SelectTarget.ROLE, but provided default value has type SelectTarget.USER!");
        assertThatIllegalArgumentException().isThrownBy(() -> {
            Builder builder = EntitySelectMenu.create("customid", SelectTarget.ROLE);
            builder.setDefaultValues(DefaultValue.channel("1234"));
        }).withMessage("The select menu supports types SelectTarget.ROLE, but provided default value has type SelectTarget.CHANNEL!");
        assertThatIllegalArgumentException().isThrownBy(() -> {
            Builder builder = EntitySelectMenu.create("customid", SelectTarget.ROLE, SelectTarget.USER);
            builder.setDefaultValues(DefaultValue.channel("1234"));
        }).withMessage("The select menu supports types SelectTarget.ROLE and SelectTarget.USER, but provided default value has type SelectTarget.CHANNEL!");
        assertThatIllegalArgumentException().isThrownBy(() -> {
            Builder builder = EntitySelectMenu.create("customid", SelectTarget.USER);
            builder.setDefaultValues(DefaultValue.channel("1234"));
        }).withMessage("The select menu supports types SelectTarget.USER, but provided default value has type SelectTarget.CHANNEL!");
        assertThatIllegalArgumentException().isThrownBy(() -> {
            Builder builder = EntitySelectMenu.create("customid", SelectTarget.USER);
            builder.setDefaultValues(DefaultValue.role("1234"));
        }).withMessage("The select menu supports types SelectTarget.USER, but provided default value has type SelectTarget.ROLE!");
        assertThatIllegalArgumentException().isThrownBy(() -> {
            Builder builder = EntitySelectMenu.create("customid", SelectTarget.CHANNEL);
            builder.setDefaultValues(DefaultValue.user("1234"));
        }).withMessage("The select menu supports types SelectTarget.CHANNEL, but provided default value has type SelectTarget.USER!");
        assertThatIllegalArgumentException().isThrownBy(() -> {
            Builder builder = EntitySelectMenu.create("customid", SelectTarget.CHANNEL);
            builder.setDefaultValues(DefaultValue.role("1234"));
        }).withMessage("The select menu supports types SelectTarget.CHANNEL, but provided default value has type SelectTarget.ROLE!");
    }
}
