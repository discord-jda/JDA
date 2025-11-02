package net.dv8tion.jda.test.rewrite;

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
        // https://docs.openrewrite.org/authoring-recipes/multiple-versions#manually-copying-jars-and-using-the-classpathfromresources-function
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
                        "import net.dv8tion.jda.api.interactions.components.*;\n\npublic class Test1 { ActionComponent x() {} }",
                        "import net.dv8tion.jda.api.components.ActionComponent;\nimport net.dv8tion.jda.api.interactions.components.*;\n\npublic class Test1 { ActionComponent x() {} }"
                ),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.ActionComponent;\n\npublic class Test2 { ActionComponent x() {} }",
                        "import net.dv8tion.jda.api.components.ActionComponent;\n\npublic class Test2 { ActionComponent x() {} }"
                ),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.ActionRow;\n\npublic class Test3 { ActionRow x() {} }",
                        "import net.dv8tion.jda.api.components.actionrow.ActionRow;\n\npublic class Test3 { ActionRow x() {} }"
                ),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.Component;\n\npublic class Test4 { Component x() {} }",
                        "import net.dv8tion.jda.api.components.Component;\n\npublic class Test4 { Component x() {} }"
                ),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.Component.Type;\n\npublic class Test5 { Type x() {} }",
                        "import net.dv8tion.jda.api.components.Component.Type;\n\npublic class Test5 { Type x() {} }"
                ),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.buttons.Button;\n\npublic class Test6 { Button x() {} }",
                        "import net.dv8tion.jda.api.components.buttons.Button;\n\npublic class Test6 { Button x() {} }"
                ),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;\n\npublic class Test7 { ButtonStyle x() {} }",
                        "import net.dv8tion.jda.api.components.buttons.ButtonStyle;\n\npublic class Test7 { ButtonStyle x() {} }"
                ),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;\n\npublic class Test8 { EntitySelectMenu x() {} }",
                        "import net.dv8tion.jda.api.components.selections.EntitySelectMenu;\n\npublic class Test8 { EntitySelectMenu x() {} }"
                ),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.Builder;\n\npublic class Test9 { Builder x() {} }",
                        "import net.dv8tion.jda.api.components.selections.EntitySelectMenu.Builder;\n\npublic class Test9 { Builder x() {} }"
                ),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.DefaultValue;\n\npublic class Test10 { DefaultValue x() {} }",
                        "import net.dv8tion.jda.api.components.selections.EntitySelectMenu.DefaultValue;\n\npublic class Test10 { DefaultValue x() {} }"
                ),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget;\n\npublic class Test11 { SelectTarget x() {} }",
                        "import net.dv8tion.jda.api.components.selections.EntitySelectMenu.SelectTarget;\n\npublic class Test11 { SelectTarget x() {} }"
                ),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;\n\npublic class Test12 { SelectMenu x() {} }",
                        "import net.dv8tion.jda.api.components.selections.SelectMenu;\n\npublic class Test12 { SelectMenu x() {} }"
                ),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.selections.SelectMenu.Builder;\n\npublic class Test13 { Builder x() {} }",
                        "import net.dv8tion.jda.api.components.selections.SelectMenu.Builder;\n\npublic class Test13 { Builder x() {} }"
                ),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.selections.SelectOption;\n\npublic class Test14 { SelectOption x() {} }",
                        "import net.dv8tion.jda.api.components.selections.SelectOption;\n\npublic class Test14 { SelectOption x() {} }"
                ),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;\n\npublic class Test15 { StringSelectMenu x() {} }",
                        "import net.dv8tion.jda.api.components.selections.StringSelectMenu;\n\npublic class Test15 { StringSelectMenu x() {} }"
                ),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.Builder;\n\npublic class Test16 { Builder x() {} }",
                        "import net.dv8tion.jda.api.components.selections.StringSelectMenu.Builder;\n\npublic class Test16 { Builder x() {} }"
                ),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.text.TextInput;\n\npublic class Test17 { TextInput x() {} }",
                        "import net.dv8tion.jda.api.components.textinput.TextInput;\n\npublic class Test17 { TextInput x() {} }"
                ),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.text.TextInput.Builder;\n\npublic class Test18 { Builder x() {} }",
                        "import net.dv8tion.jda.api.components.textinput.TextInput.Builder;\n\npublic class Test18 { Builder x() {} }"
                ),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;\n\npublic class Test19 { TextInputStyle x() {} }",
                        "import net.dv8tion.jda.api.components.textinput.TextInputStyle;\n\npublic class Test19 { TextInputStyle x() {} }"
                )
        );
        rewriteRun(
                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2Packages"),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.modals.Modal;\n\npublic class Test20 { Modal x() {} }",
                        "import net.dv8tion.jda.api.modals.Modal;\n\npublic class Test20 { Modal x() {} }"
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
                        "import net.dv8tion.jda.api.interactions.components.buttons.Button;\n\npublic class Test1 { void x(Button component) { component.getId(); } }",
                        "import net.dv8tion.jda.api.components.buttons.Button;\n\npublic class Test1 { void x(Button component) { component.getCustomId(); } }"
                ),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.modals.ModalMapping;\n\npublic class Test2 { void x(ModalMapping component) { component.getId(); } }",
                        "import net.dv8tion.jda.api.interactions.modals.ModalMapping;\n\npublic class Test2 { void x(ModalMapping component) { component.getCustomId(); } }"
                ),
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
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;\n\npublic class Test3 { void x(SelectMenu.Builder<?, ?> component) { component.getId(); } }",
                        "import net.dv8tion.jda.api.components.selections.SelectMenu;\nimport net.dv8tion.jda.api.components.selections.SelectMenu.Builder;\n\npublic class Test3 { void x(Builder<?, ?> component) { component.getCustomId(); } }"
                ),
                //language=java
                java(
                        "import net.dv8tion.jda.api.interactions.components.text.TextInput;\n\npublic class Test4 { void x(TextInput.Builder component) { component.getId(); } }",
                        "import net.dv8tion.jda.api.components.textinput.TextInput;\nimport net.dv8tion.jda.api.components.textinput.TextInput.Builder;\n\npublic class Test4 { void x(Builder component) { component.getCustomId(); } }"
                )
        );
    }
}
