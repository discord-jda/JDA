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

package net.dv8tion.jda.api.events.annotations;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.lang.annotation.*;

/**
 * Annotates the required intents for this event.
 * <br>This is used by {@link GatewayIntent#fromEvents(Class[])}
 * to determine which intents are required and/or optional for a given event type.
 *
 * @see GatewayIntent#fromEvents(Class[])
 * @see JDABuilder#enableIntents(GatewayIntent, GatewayIntent...)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiredIntents
{
    /**
     * Gateway intents required for the annotated event to fire.
     */
    GatewayIntent[] always() default {};

    /**
     * Gateway intents which may be required for certain methods of the annotated event,
     * or which may help fire the event under certain conditions.
     *
     * <p>The details should be documented on the annotated event.
     */
    GatewayIntent[] sometimes() default {};
}
