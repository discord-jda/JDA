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
        // I'd like to use their recipe-library, but it pulls a beta version of the shadow plugin, forcing us to upgrade
        // which then breaks the buildscript for god knows what reason.
        // I ain't putting more effort into what could have been at worst, a 5 minutes job for the end user,
        // when it cost me my entire day.
        spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "JDA-5.3.0"));
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
//        rewriteRun(
//                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2"),
//                //language=java
//                java(
//                        "import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;\n\npublic class Test { void x(SelectMenu.Builder<?, ?> component) { component.getId(); } }",
//                        "import net.dv8tion.jda.api.components.selections.SelectMenu;\n\npublic class Test { void x(SelectMenu.Builder<?, ?> component) { component.getCustomId(); } }"
//                )
//        );
//
//        rewriteRun(
//                spec -> spec.recipeFromResources("net.dv8tion.MigrateComponentsV2"),
//                //language=java
//                java(
//                        "import net.dv8tion.jda.api.interactions.components.text.TextInput;\n\npublic class Test { void x(TextInput.Builder component) { component.getId(); } }",
//                        "import net.dv8tion.jda.api.components.textinput.TextInput;\n\npublic class Test { void x(TextInput.Builder component) { component.getCustomId(); } }"
//                )
//        );
    }
}
