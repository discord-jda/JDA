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

package net.dv8tion.jda.api.interactions.callbacks;

import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Entitlement;
import net.dv8tion.jda.api.entities.SkuSnowflake;
import net.dv8tion.jda.api.requests.restaction.interactions.PremiumRequiredCallbackAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Replies with an in-built client message stating that an {@link Entitlement Entitlement} is required.
 *
 * <p>Replying with {@link #replyWithPremiumRequired()} will automatically acknowledge this interaction.
 *
 * <p><b>Note:</b>This interaction requires <a href="https://discord.com/developers/docs/monetization/overview" target="_blank">monetization</a> to be enabled.
 *
 * @deprecated Replaced with {@link Button#premium(SkuSnowflake)},
 * see the <a href="https://discord.com/developers/docs/change-log#premium-apps-new-premium-button-style-deep-linking-url-schemes" target="_blank">Discord change logs</a> for more details.
 */
@Deprecated
public interface IPremiumRequiredReplyCallback extends IDeferrableCallback
{
    /**
     * @deprecated Replaced with {@link Button#premium(SkuSnowflake)},
     * see the <a href="https://discord.com/developers/docs/change-log#premium-apps-new-premium-button-style-deep-linking-url-schemes" target="_blank">Discord change logs</a> for more details.
     */
    @Nonnull
    @Deprecated
    @CheckReturnValue
    PremiumRequiredCallbackAction replyWithPremiumRequired();
}
