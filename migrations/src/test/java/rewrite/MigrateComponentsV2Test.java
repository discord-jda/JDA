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

package rewrite;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class MigrateComponentsV2Test implements RewriteTest
{
    @Override
    public void defaults(RecipeSpec spec)
    {
        spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "JDA-5.6.1"));
    }

    // https://docs.openrewrite.org/authoring-recipes/recipe-testing#declarative-recipe-testing
    @Test
    void replacesPackage()
    {
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2Packages"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.ActionComponent;\n\npublic class Test { ActionComponent x() {} }",
                        "import net.dv8tion.jda.api.components.ActionComponent;\n\npublic class Test { ActionComponent x() {} }"
                )
        );
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2Packages"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.ActionRow;\n\npublic class Test { ActionRow x() {} }",
                        "import net.dv8tion.jda.api.components.actionrow.ActionRow;\n\npublic class Test { ActionRow x() {} }"
                )
        );
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2Packages"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.Component;\n\npublic class Test { Component x() {} }",
                        "import net.dv8tion.jda.api.components.Component;\n\npublic class Test { Component x() {} }"
                )
        );
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2Packages"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.Component.Type;\n\npublic class Test { Type x() {} }",
                        "import net.dv8tion.jda.api.components.Component.Type;\n\npublic class Test { Type x() {} }"
                )
        );
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2Packages"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.buttons.Button;\n\npublic class Test { Button x() {} }",
                        "import net.dv8tion.jda.api.components.buttons.Button;\n\npublic class Test { Button x() {} }"
                )
        );
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2Packages"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;\n\npublic class Test { ButtonStyle x() {} }",
                        "import net.dv8tion.jda.api.components.buttons.ButtonStyle;\n\npublic class Test { ButtonStyle x() {} }"
                )
        );
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2Packages"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;\n\npublic class Test { EntitySelectMenu x() {} }",
                        "import net.dv8tion.jda.api.components.selections.EntitySelectMenu;\n\npublic class Test { EntitySelectMenu x() {} }"
                )
        );
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2Packages"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.Builder;\n\npublic class Test { Builder x() {} }",
                        "import net.dv8tion.jda.api.components.selections.EntitySelectMenu.Builder;\n\npublic class Test { Builder x() {} }"
                )
        );
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2Packages"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.DefaultValue;\n\npublic class Test { DefaultValue x() {} }",
                        "import net.dv8tion.jda.api.components.selections.EntitySelectMenu.DefaultValue;\n\npublic class Test { DefaultValue x() {} }"
                )
        );
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2Packages"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget;\n\npublic class Test { SelectTarget x() {} }",
                        "import net.dv8tion.jda.api.components.selections.EntitySelectMenu.SelectTarget;\n\npublic class Test { SelectTarget x() {} }"
                )
        );
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2Packages"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;\n\npublic class Test { SelectMenu x() {} }",
                        "import net.dv8tion.jda.api.components.selections.SelectMenu;\n\npublic class Test { SelectMenu x() {} }"
                )
        );
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2Packages"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.selections.SelectMenu.Builder;\n\npublic class Test { Builder x() {} }",
                        "import net.dv8tion.jda.api.components.selections.SelectMenu.Builder;\n\npublic class Test { Builder x() {} }"
                )
        );
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2Packages"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.selections.SelectOption;\n\npublic class Test { SelectOption x() {} }",
                        "import net.dv8tion.jda.api.components.selections.SelectOption;\n\npublic class Test { SelectOption x() {} }"
                )
        );
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2Packages"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;\n\npublic class Test { StringSelectMenu x() {} }",
                        "import net.dv8tion.jda.api.components.selections.StringSelectMenu;\n\npublic class Test { StringSelectMenu x() {} }"
                )
        );
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2Packages"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.Builder;\n\npublic class Test { Builder x() {} }",
                        "import net.dv8tion.jda.api.components.selections.StringSelectMenu.Builder;\n\npublic class Test { Builder x() {} }"
                )
        );
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2Packages"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.text.TextInput;\n\npublic class Test { TextInput x() {} }",
                        "import net.dv8tion.jda.api.components.textinput.TextInput;\n\npublic class Test { TextInput x() {} }"
                )
        );
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2Packages"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.text.TextInput.Builder;\n\npublic class Test { Builder x() {} }",
                        "import net.dv8tion.jda.api.components.textinput.TextInput.Builder;\n\npublic class Test { Builder x() {} }"
                )
        );
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2Packages"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;\n\npublic class Test { TextInputStyle x() {} }",
                        "import net.dv8tion.jda.api.components.textinput.TextInputStyle;\n\npublic class Test { TextInputStyle x() {} }"
                )
        );
    }

    @Test
    void replacesGetId()
    {
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.buttons.Button;\n\npublic class Test { void x(Button component) { component.getId(); } }",
                        "import net.dv8tion.jda.api.components.buttons.Button;\n\npublic class Test { void x(Button component) { component.getCustomId(); } }"
                )
        );

        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.modals.ModalMapping;\n\npublic class Test { void x(ModalMapping component) { component.getId(); } }",
                        "import net.dv8tion.jda.api.interactions.modals.ModalMapping;\n\npublic class Test { void x(ModalMapping component) { component.getCustomId(); } }"
                )
        );

        // Nested classes are bugged.
        //
        // Upgrading versions (3.3.0 -> 3.6.1) had *some* improvement,
        // but it will still *add* an import for the nested class,
        // even though it is already qualified starting from the already-imported top-level class.
        //
        // Though this will happen only if the import wasn't already present,
        // it will still produce valid code and the user can mass optimize imports when it's finished.
        //
        // These tests will take this bug into account
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;\n\npublic class Test { void x(SelectMenu.Builder<?, ?> component) { component.getId(); } }",
                        "import net.dv8tion.jda.api.components.selections.SelectMenu;\nimport net.dv8tion.jda.api.components.selections.SelectMenu.Builder;\n\npublic class Test { void x(SelectMenu.Builder<?, ?> component) { component.getCustomId(); } }"
                )
        );

        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.text.TextInput;\n\npublic class Test { void x(TextInput.Builder component) { component.getId(); } }",
                        "import net.dv8tion.jda.api.components.textinput.TextInput;\nimport net.dv8tion.jda.api.components.textinput.TextInput.Builder;\n\npublic class Test { void x(TextInput.Builder component) { component.getCustomId(); } }"
                )
        );
    }
}
