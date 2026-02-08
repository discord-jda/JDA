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

package net.dv8tion.jda.test.rewrite;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class MigrateComponentsV2Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        // https://docs.openrewrite.org/authoring-recipes/multiple-versions#manually-copying-jars-and-using-the-classpathfromresources-function
        spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "JDA-5.6.1"));
    }

    // https://docs.openrewrite.org/authoring-recipes/recipe-testing#declarative-recipe-testing
    @Test
    void replacesPackage() {
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2Packages"),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.*;

                                public class Test1 { ActionComponent x() {} }""", """
                                import net.dv8tion.jda.api.components.ActionComponent;
                                import net.dv8tion.jda.api.interactions.components.*;

                                public class Test1 { ActionComponent x() {} }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.ActionComponent;

                                public class Test2 { ActionComponent x() {} }""", """
                                import net.dv8tion.jda.api.components.ActionComponent;

                                public class Test2 { ActionComponent x() {} }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.ActionRow;

                                public class Test3 { ActionRow x() {} }""", """
                                import net.dv8tion.jda.api.components.actionrow.ActionRow;

                                public class Test3 { ActionRow x() {} }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.Component;

                                public class Test4 { Component x() {} }""", """
                                import net.dv8tion.jda.api.components.Component;

                                public class Test4 { Component x() {} }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.Component.Type;

                                public class Test5 { Type x() {} }""", """
                                import net.dv8tion.jda.api.components.Component.Type;

                                public class Test5 { Type x() {} }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.buttons.Button;

                                public class Test6 { Button x() {} }""", """
                                import net.dv8tion.jda.api.components.buttons.Button;

                                public class Test6 { Button x() {} }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

                                public class Test7 { ButtonStyle x() {} }""", """
                                import net.dv8tion.jda.api.components.buttons.ButtonStyle;

                                public class Test7 { ButtonStyle x() {} }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;

                                public class Test8 { EntitySelectMenu x() {} }""", """
                                import net.dv8tion.jda.api.components.selections.EntitySelectMenu;

                                public class Test8 { EntitySelectMenu x() {} }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.Builder;

                                public class Test9 { Builder x() {} }""", """
                                import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
                                import net.dv8tion.jda.api.components.selections.EntitySelectMenu.Builder;

                                public class Test9 { EntitySelectMenu.Builder x() {} }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.DefaultValue;

                                public class Test10 { DefaultValue x() {} }""", """
                                import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
                                import net.dv8tion.jda.api.components.selections.EntitySelectMenu.DefaultValue;

                                public class Test10 { EntitySelectMenu.DefaultValue x() {} }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget;

                                public class Test11 { SelectTarget x() {} }""", """
                                import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
                                import net.dv8tion.jda.api.components.selections.EntitySelectMenu.SelectTarget;

                                public class Test11 { EntitySelectMenu.SelectTarget x() {} }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

                                public class Test12 { SelectMenu x() {} }""", """
                                import net.dv8tion.jda.api.components.selections.SelectMenu;

                                public class Test12 { SelectMenu x() {} }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.selections.SelectMenu.Builder;

                                public class Test13 { Builder x() {} }""", """
                                import net.dv8tion.jda.api.components.selections.SelectMenu;
                                import net.dv8tion.jda.api.components.selections.SelectMenu.Builder;

                                public class Test13 { SelectMenu.Builder x() {} }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

                                public class Test14 { SelectOption x() {} }""", """
                                import net.dv8tion.jda.api.components.selections.SelectOption;

                                public class Test14 { SelectOption x() {} }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

                                public class Test15 { StringSelectMenu x() {} }""", """
                                import net.dv8tion.jda.api.components.selections.StringSelectMenu;

                                public class Test15 { StringSelectMenu x() {} }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.Builder;

                                public class Test16 { Builder x() {} }""", """
                                import net.dv8tion.jda.api.components.selections.StringSelectMenu;
                                import net.dv8tion.jda.api.components.selections.StringSelectMenu.Builder;

                                public class Test16 { StringSelectMenu.Builder x() {} }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.text.TextInput;

                                public class Test17 { TextInput x() {} }""", """
                                import net.dv8tion.jda.api.components.textinput.TextInput;

                                public class Test17 { TextInput x() {} }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.text.TextInput.Builder;

                                public class Test18 { Builder x() {} }""", """
                                import net.dv8tion.jda.api.components.textinput.TextInput;
                                import net.dv8tion.jda.api.components.textinput.TextInput.Builder;

                                public class Test18 { TextInput.Builder x() {} }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

                                public class Test19 { TextInputStyle x() {} }""", """
                                import net.dv8tion.jda.api.components.textinput.TextInputStyle;

                                public class Test19 { TextInputStyle x() {} }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.modals.Modal;

                                public class Test20 { Modal x() {} }""", """
                                import net.dv8tion.jda.api.modals.Modal;

                                public class Test20 { Modal x() {} }"""));
    }

    @Test
    void replacesGetId() {
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2"),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.buttons.Button;

                                public class Test1 { void x(Button component) { component.getId(); }\
                                 }""", """
                                import net.dv8tion.jda.api.components.buttons.Button;

                                public class Test1 { void x(Button component) {\
                                 component.getCustomId(); } }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.modals.ModalMapping;

                                public class Test2 { void x(ModalMapping component) {\
                                 component.getId(); } }""", """
                                import net.dv8tion.jda.api.interactions.modals.ModalMapping;

                                public class Test2 { void x(ModalMapping component) {\
                                 component.getCustomId(); } }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

                                public class Test3 { void x(SelectMenu.Builder<?, ?> component) {\
                                 component.getId(); } }""", """
                                import net.dv8tion.jda.api.components.selections.SelectMenu;

                                public class Test3 { void x(SelectMenu.Builder<?, ?> component) {\
                                 component.getCustomId(); } }"""),
                // language=java
                java("""
                                import net.dv8tion.jda.api.interactions.components.text.TextInput;

                                public class Test4 { void x(TextInput.Builder component) {\
                                 component.getId(); } }""", """
                                import net.dv8tion.jda.api.components.textinput.TextInput;

                                public class Test4 { void x(TextInput.Builder component) {\
                                 component.getCustomId(); } }"""));
    }
}
