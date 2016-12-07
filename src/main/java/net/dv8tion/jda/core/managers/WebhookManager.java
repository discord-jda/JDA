/*
 *     Copyright 2015-2016 Austin Keener & Michael Ritter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.Webhook;
import net.dv8tion.jda.core.requests.RestAction;

public class WebhookManager
{

    protected WebhookManagerUpdatable manager;

    public WebhookManager(Webhook webhook)
    {
        this.manager = new WebhookManagerUpdatable(webhook);
    }

    public JDA getJDA()
    {
        return manager.getJDA();
    }

    public Webhook getWebhook()
    {
        return manager.getWebhook();
    }

    public RestAction<Void> setName(String name)
    {
        return manager.getNameField().setValue(name).update();
    }

    public RestAction<Void> setAvatar(Icon icon)
    {
        return manager.getAvatarField().setValue(icon).update();
    }

    public RestAction<Void> setChannel(TextChannel channel)
    {
        return manager.getChannelField().setValue(channel).update();
    }

}
