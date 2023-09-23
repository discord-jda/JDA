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

package net.dv8tion.jda.interactions;

import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.Builder;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.DefaultValue;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class SelectMenuTests
{
    @Test
    public void testEntitySelectDefaultValueValid()
    {
        Builder builder = EntitySelectMenu.create("customid", SelectTarget.ROLE);
        builder.setDefaultValues(DefaultValue.role("1234"));

        EntitySelectMenu menu = builder.build();
        DataObject value = menu.toData().getArray("default_values").getObject(0);

        Assertions.assertEquals(Arrays.asList(DefaultValue.role("1234")), menu.getDefaultValues());
        Assertions.assertEquals("role", value.getString("type"));
        Assertions.assertEquals("1234", value.getString("id"));

        builder = EntitySelectMenu.create("customid", SelectTarget.USER);
        builder.setDefaultValues(DefaultValue.user("1234"));

        menu = builder.build();
        value = menu.toData().getArray("default_values").getObject(0);

        Assertions.assertEquals(Arrays.asList(DefaultValue.user("1234")), menu.getDefaultValues());
        Assertions.assertEquals("user", value.getString("type"));
        Assertions.assertEquals("1234", value.getString("id"));

        builder = EntitySelectMenu.create("customid", SelectTarget.CHANNEL);
        builder.setDefaultValues(DefaultValue.channel("1234"));

        menu = builder.build();
        value = menu.toData().getArray("default_values").getObject(0);

        Assertions.assertEquals(Arrays.asList(DefaultValue.channel("1234")), menu.getDefaultValues());
        Assertions.assertEquals("channel", value.getString("type"));
        Assertions.assertEquals("1234", value.getString("id"));
    }

    @Test
    public void testEntitySelectDefaultValueInvalid()
    {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Builder builder = EntitySelectMenu.create("customid", SelectTarget.ROLE);
            builder.setDefaultValues(DefaultValue.user("1234"));
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Builder builder = EntitySelectMenu.create("customid", SelectTarget.ROLE);
            builder.setDefaultValues(DefaultValue.channel("1234"));
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Builder builder = EntitySelectMenu.create("customid", SelectTarget.ROLE, SelectTarget.USER);
            builder.setDefaultValues(DefaultValue.channel("1234"));
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Builder builder = EntitySelectMenu.create("customid", SelectTarget.USER);
            builder.setDefaultValues(DefaultValue.channel("1234"));
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Builder builder = EntitySelectMenu.create("customid", SelectTarget.USER);
            builder.setDefaultValues(DefaultValue.role("1234"));
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Builder builder = EntitySelectMenu.create("customid", SelectTarget.CHANNEL);
            builder.setDefaultValues(DefaultValue.user("1234"));
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Builder builder = EntitySelectMenu.create("customid", SelectTarget.CHANNEL);
            builder.setDefaultValues(DefaultValue.role("1234"));
        });
    }
}
