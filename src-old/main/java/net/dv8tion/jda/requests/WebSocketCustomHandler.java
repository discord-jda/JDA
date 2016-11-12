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
package net.dv8tion.jda.requests;

import org.json.JSONObject;

public interface WebSocketCustomHandler
{
    /**
     * Handles discord events passed to it. If this handler does not handle the event provided
     * this should return false.
     *
     * @param obj
     *          The Discord event which needs to be handled
     * @return
     *      boolean representing whether or not the event still needs to be handled.
     *      True  - finished handling.
     *      False - continue handling.
     */
    boolean handle(JSONObject obj);
}
