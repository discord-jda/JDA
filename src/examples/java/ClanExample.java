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
import net.dv8tion.jda.api.entities.Clan;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jetbrains.annotations.NotNull;

public class ClanExample extends ListenerAdapter
{
    public static void main(String[] args) throws InterruptedException
    {
        // Initialize bot
        JDA jda = JDABuilder.createDefault("BOT_TOKEN_HERE")
                .addEventListeners(new ClanExample())
                .build();

        jda.awaitReady();
    }


    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        Clan clan = event.getAuthor().getClan();
        if (clan == null) {
            System.out.println("User doesn't have clan");
            return;
        }

        System.out.println();
        System.out.println(event.getAuthor().getGlobalName() + " clan's information");
        System.out.println(" -  ID: " + clan.getId());
        System.out.println(" -  Badge: " + clan.getBadge());
        System.out.println(" -  Name: " + clan.getName());
        System.out.println(" -  Status: " + (clan.isEnabled() ? "Enabled" : "Disabled"));
        System.out.println();
    }
}
