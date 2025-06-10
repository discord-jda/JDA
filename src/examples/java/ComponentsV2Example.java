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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.components.ActionComponent;
import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponentUnion;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.filedisplay.FileDisplay;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.replacer.IReplaceable;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.selects.StringSelectMenu;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.components.tree.ComponentTree;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageRequest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.List;

/**
 * An example on how to use components version 2.
 *
 * <h3>Enabling Components V2</h3>
 * V2 components can only be used in messages which uses {@link MessageRequest#useComponentsV2()},
 * this however has some limits, which are documented on the method.
 *
 * <p>Note that every component documents whether it requires the V2 flag.
 *
 * <h3>Component typing</h3>
 * Components follow a type system similar to channels, using "union" types;
 * they help you discover which type of component a particular union can represent,
 * with methods such as {@link ActionRowChildComponentUnion#asButton()},
 * take a look at what each union supports!
 *
 * <p>Each component that can contain other components will contain a link to their compatible children type.
 *
 * <h3>Unique IDs</h3>
 * Every component has a {@linkplain Component#getUniqueId() unique numeric ID},
 * you can, for example, use them to replace specific components of a message.
 * <br>For this, you can use {@link IReplaceable#replace(ComponentReplacer)} on component containers and {@link ComponentTree}.
 *
 * <p>For example:
 * <pre>{@code
 *     public class MyButtonListener extends ListenerAdapter {
 *         @Override
 *         public void onButtonInteraction(ButtonInteractionEvent event)
 *         {
 *              final MessageComponentTree clickedAsDisabled = event.getMessage()
 *                     .getComponentTree()
 *                     .replace(ComponentReplacer.byId(event.getButton(), event.getButton().asDisabled()));
 *             event.editComponents(clickedAsDisabled).queue();
 *         }
 *     }
 * }</pre>
 *
 * You can also easily disable all components:
 *
 * <pre>{@code
 *     public class MyButtonListener extends ListenerAdapter {
 *         @Override
 *         public void onButtonInteraction(ButtonInteractionEvent event)
 *         {
 *              final MessageComponentTree everythingAsDisabled = event.getMessage()
 *                     .getComponentTree()
 *                     .asDisabled();
 *             event.editComponents(everythingAsDisabled).queue();
 *         }
 *     }
 * }</pre>
 *
 * <p>This is separate from {@linkplain ActionComponent#getCustomId() custom IDs}
 * which you can only find on components that trigger interactions.
 *
 * @see Container
 * @see Section
 * @see Thumbnail
 * @see TextDisplay
 * @see Separator
 * @see Button
 * @see ActionRow
 * @see StringSelectMenu
 * @see FileDisplay
 * @see MediaGallery
 * @see MediaGalleryItem
 */
public class ComponentsV2Example extends ListenerAdapter
{
    private static ApplicationEmoji backEmoji;

    public static void main(String[] args) throws IOException
    {
        JDA jda = JDABuilder.createLight("YOUR_BOT_TOKEN_HERE", EnumSet.noneOf(GatewayIntent.class)) // slash commands don't need any intents
                .addEventListeners(new ComponentsV2Example())
                .build();

        final List<ApplicationEmoji> applicationEmojis = jda.retrieveApplicationEmojis().complete();
        backEmoji = getOrCreateEmoji(jda, applicationEmojis, "/back.png");

        // Send the new set of commands to Discord; this will override any existing global commands with the new set provided here
        // You might need to reload your Discord client if you don't see the commands
        jda.updateCommands()
                .addCommands(Commands.slash("components_v2_sample", "Yippie!"))
                .addCommands(Commands.slash("components_v2_butterfly", "Butterflies!"))
                .queue();
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event)
    {
        switch (event.getName())
        {
        case "components_v2_sample":
            onComponentsV2Sample(event);
            break;
        case "components_v2_butterfly":
            onComponentsV2Butterfly(event);
            break;
        }
    }

    private static void onComponentsV2Sample(@Nonnull SlashCommandInteractionEvent event)
    {
        // A simple box; looks similar to an embed but...
        Container container = Container.of(
                // Displays content on the left and an "accessory" on the right.
                Section.of(
                        // A thumbnail, it should work with all image formats Discord supports.
                        // You can make it a spoiler and also give it a description (alternative text)
                        Thumbnail.fromFile(getResourceAsFileUpload("/cv2.png")),
                        // The section's children
                        TextDisplay.of("## A container"),
                        TextDisplay.of("Quite different from embeds"),
                        TextDisplay.of("-# You can even put small text")
                ),

                // A separator; can be made invisible or be larger.
                Separator.createDivider(Separator.Spacing.SMALL),

                // Another section, note that you can have at most 3 children (excluding the accessory).
                // You're always free to use newlines in your text displays,
                // but keep in mind a new TextDisplay will display as a different paragraph.
                Section.of(
                        // For the sake of the example, this button will do nothing.
                        Button.danger("feature_disable:moderation", "Disable moderation"),
                        TextDisplay.of("**Moderation:** Moderates the messages"),
                        TextDisplay.of("**Status:** Enabled")
                ),
                // A row of actionable components.
                ActionRow.of(
                        // For the sake of the example, this select menu will do nothing.
                        StringSelectMenu.create("feature")
                                .setPlaceholder("Select a module to configure")
                                .addOption("Moderation", "moderation", "Configure the moderation module")
                                .addOption("Fun", "fun", "Configure the fun module")
                                .setDefaultValues("moderation")
                                .build()
                ).withUniqueId(42), // Set an identifier, this may be useful to specifically remove this action row later

                // Separate things a bit.
                Separator.createDivider(Separator.Spacing.SMALL),

                // Another text display, you are not limited per-component,
                // there is only a character limit for the whole message (see [[Message#MAX_CONTENT_LENGTH_COMPONENT_V2]]).
                TextDisplay.of("Download the current configuration:"),
                // Displays a simple download component, has no preview.
                FileDisplay.fromFile(FileUpload.fromData("{}".getBytes(StandardCharsets.UTF_8), "config.json")),

                // A set of pictures to display, display in a mosaic
                // It can also take one item, in which case it will take the most horizontal space as possible,
                // depending on the aspect ratio.
                MediaGallery.of(
                        MediaGalleryItem.fromFile(getResourceAsFileUpload("/docs.gif"))
                )
        );

        // No need to upload files here, it's taken care of automatically
        event.replyComponents(container)
                // This is required any time you are using Components V2
                .useComponentsV2()
                .setEphemeral(true)
                .queue();
    }

    private static void onComponentsV2Butterfly(@Nonnull SlashCommandInteractionEvent event)
    {
        Container container = Container.of(
                TextDisplay.of("Summary of Daylight Prairie"),
                TextDisplay.of("### [Butterfly Fields](https://sky-children-of-the-light.fandom.com/wiki/Daylight_Prairie#Butterfly_Fields)"),

                Separator.createDivider(Separator.Spacing.LARGE),

                Section.of(
                        Thumbnail.fromFile(getResourceAsFileUpload("/Prairie_ButterflyFields.jpg"))
                                // Set an "alternative text", useful for accessibility
                                .withDescription("Butterfly Fields"),
                        // In Java 15+, you can use text blocks instead: https://www.baeldung.com/java-text-blocks
                        TextDisplay.of("The Butterfly Fields is a prairie field covered in bountiful fauna. In the fields, players once again find Butterflies that can help reach otherwise difficult to access places. The field contains gateways into three of Prairie's main locations: Prairie Village, Bird Nest - with a Spirit Gate requiring 4 Prairie Regular Spirits relived - and the Prairie Caves - with a Spirit Gate requiring 2 Isle Regular Spirits and 3 Prairie Regular Spirits relived. A Passage Mask can be found to the left side, near the cave with Prairie Child of Light #1, to light and do Passage Quest #4. For a new player, Village is the only available path.\n" +
                                "\n" +
                                "*Source: [Daylight_Prairie#Butterfly_Fields](https://sky-children-of-the-light.fandom.com/wiki/Daylight_Prairie#Butterfly_Fields)*\n")
                ),
                TextDisplay.of("-# Page 2/9"),

                Separator.createDivider(Separator.Spacing.SMALL),

                ActionRow.of(
                        Button.secondary("previous", "⬅ Social Space"),
                        Button.success("back", "Back")
                                .withEmoji(backEmoji),
                        Button.secondary("next", "Prairie Village ➡")
                ),

                Separator.createDivider(Separator.Spacing.SMALL),

                ActionRow.of(
                        StringSelectMenu.create("area")
                                .addOption("Social Space", "social_space")
                                .addOption("Butterfly Fields", "butterfly_fields")
                                .addOption("Prairie Village", "prairie_village")
                                .setDefaultValues("butterfly_fields")
                                .build()
                )
        );

        // No need to upload files here, it's taken care of automatically
        event.replyComponents(container)
                // This is required any time you are using Components V2
                .useComponentsV2()
                .setEphemeral(true)
                .queue();
    }

    /**
     * Creates a {@link FileUpload} from the given path.
     * <br>The file name will be set to the path's last component.
     *
     * @param  path
     *         The path to the resource
     *
     * @throws IllegalArgumentException
     *         If the resource cannot be found
     *
     * @return The {@link FileUpload} made from the resource
     */
    @Nonnull
    private static FileUpload getResourceAsFileUpload(@Nonnull String path)
    {
        final int lastSeparatorIndex = path.lastIndexOf('/');
        final String fileName = path.substring(lastSeparatorIndex + 1);

        final InputStream stream = ComponentsV2Example.class.getResourceAsStream(path);
        if (stream == null)
            throw new IllegalArgumentException("Could not find resource at: " + path);

        return FileUpload.fromData(stream, fileName);
    }

    /**
     * Gets or creates an {@link ApplicationEmoji} from the given path.
     * <br>The emoji name will be set to the path's last component, excluding the extension.
     *
     * @param  jda
     *         The JDA instance
     * @param  applicationEmojis
     *         The existing application emojis
     * @param  path
     *         The path to the resource
     *
     * @return The {@link ApplicationEmoji}
     */
    @Nonnull
    private static ApplicationEmoji getOrCreateEmoji(@Nonnull JDA jda, @Nonnull List<ApplicationEmoji> applicationEmojis, @Nonnull String path) throws IOException
    {
        final int lastSeparatorIndex = path.lastIndexOf('/');
        final String fileName = path.substring(lastSeparatorIndex + 1);

        final int extensionIndex = fileName.lastIndexOf('.');
        final String fileNameWithoutExtension = fileName.substring(0, extensionIndex);

        for (ApplicationEmoji emoji : applicationEmojis)
        {
            if (emoji.getName().equals(fileNameWithoutExtension))
                return emoji;
        }
        return jda.createApplicationEmoji(fileNameWithoutExtension, getResourceAsIcon(path)).complete();
    }

    /**
     * Creates an {@link Icon} from the given path.
     *
     * @param  path
     *         The path to the resource
     *
     * @throws IllegalArgumentException
     *         If the resource cannot be found
     *
     * @return The {@link Icon} made from the resource
     */
    @Nonnull
    private static Icon getResourceAsIcon(@Nonnull String path) throws IOException
    {
        final InputStream stream = ComponentsV2Example.class.getResourceAsStream(path);
        if (stream == null)
            throw new IllegalArgumentException("Could not find resource at: " + path);

        return Icon.from(stream);
    }
}
