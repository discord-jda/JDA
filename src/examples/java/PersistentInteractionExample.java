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
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/*
 * This example showcases a method of keeping state between multiple interactions, in the context of a component-based menu.
 *
 * We make use of a PaginationMenu, which uses the interaction ID of the slash command to keep track of pagination state.
 * The message objects created from command replies, always carry a context Message.Interaction instance that provides the original interaction ID.
 */
public class PersistentInteractionExample extends ListenerAdapter {
    public static void main(String[] args) {
        JDA jda = JDABuilder.createLight("BOT_TOKEN_HERE", EnumSet.noneOf(GatewayIntent.class)) // slash commands don't need any intents
                .addEventListeners(new PersistentInteractionExample())
                .build();

        jda.updateCommands()
            .addCommands(Commands.slash("menu", "Command that replies with a paginated menu!"))
            .queue();
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("menu")) return;

        PaginatedMenu menu = new PaginatedMenu(event.getIdLong())
                .addPage("First page content here")
                .addPage("Second page content here")
                .addPage("Third page content here");

        // This menu implements the event listener methods, so it can reply to the button interactions later
        // You should also implement some form of timeout system to remove this event listener again,
        // otherwise you risk leaking memory from continuously creating more and more listeners.
        event.getJDA().addEventListener(menu);
        // Then we simply reply with the first page of the menu
        event.reply(MessageCreateData.fromEditData(menu.getCurrentPage()))
             .setEphemeral(true) // This menu is ephemeral, which means we don't have to keep track of the user
             .queue();
    }

    private static class PaginatedMenu extends ListenerAdapter {
        private final long referenceId;

        private final List<MessageEditData> pages = new ArrayList<>();
        private int currentPage;

        public PaginatedMenu(long referenceId) {
            this.referenceId = referenceId;
        }

        public PaginatedMenu addPage(String content) {
            this.pages.add(MessageEditData.fromContent(content));
            return this;
        }

        public MessageEditData getCurrentPage() {
            return MessageEditBuilder.from(pages.get(currentPage))
                    .setComponents(getControlInputs()) // Also update the buttons according to the page context
                    .build();
        }

        public boolean shouldRespond(ComponentInteraction interaction) {
            // The message object provides us with the interaction id of the original slash command interaction
            // Using that ID we can uniquely identify our component message here
            Message.Interaction interactionContext = interaction.getMessage().getInteraction();
            return interactionContext != null && interactionContext.getIdLong() == referenceId;
        }

        @Override
        public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
            if (!shouldRespond(event)) return;

            switch (event.getComponentId()) {
            case "pagination:prev":
                currentPage--; // Change the page pointer here
                break;
            case "pagination:next":
                currentPage++;
                break;
            case "pagination:stop":
                // Remove this event listener, to improve performance and free memory
                // In practice, this should also happen automatically after a certain timeout.
                event.getJDA().removeEventListener(this);
                // Using deferEdit marks the component messages the "original"
                event.deferEdit().queue();
                // Then delete it using the interaction hook
                event.getHook().deleteOriginal().queue();
            }

            event.editMessage(getCurrentPage()).queue();
        }

        private ActionRow getControlInputs() {
            // The usage of this getter allows you to automatically adjust which buttons should be enabled for which page
            // For example, the first page should not allow to go to a "previous" page
            return ActionRow.of(
                Button.secondary("pagination:prev", "Previous")
                    .withDisabled(currentPage == 0),
                Button.secondary("pagination:next", "Next")
                    .withDisabled(currentPage + 1 == pages.size()),
                Button.danger("pagination:stop", "Stop")
            );
        }
    }
}
