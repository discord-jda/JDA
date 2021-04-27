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

package net.dv8tion.jda.api.interactions;

import net.dv8tion.jda.api.utils.data.SerializableData;

import javax.annotation.Nullable;

public interface Component extends SerializableData
{
//    @Nonnull
//    Type getType();

    @Nullable
    String getId(); // this is not a snowflake!

    // https://gist.github.com/Zomatree/4aac9733bfa65c86e700ff5c8023787f
//    enum Type
//    {
//        UNKNOWN(-1),
//        ACTION_ROW(1),
//        BUTTON(2)
//        ;
//
//        private final int key;
//
//        Type(int key)
//        {
//            this.key = key;
//        }
//
//        @Nonnull
//        public static Type fromKey(int type)
//        {
//            for (Type t : values())
//            {
//                if (t.key == type)
//                    return t;
//            }
//            return UNKNOWN;
//        }
//    }
}
