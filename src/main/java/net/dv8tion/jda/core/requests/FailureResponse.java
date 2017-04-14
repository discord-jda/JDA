/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter
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

package net.dv8tion.jda.core.requests;

public interface FailureResponse
{

    String getMeaning();

    int getCode();

    class UnknownFailure implements FailureResponse
    {

        private final String meaning;
        private final int code;

        public UnknownFailure(int code, String meaning)
        {
            this.meaning = meaning;
            this.code = code;
        }

        @Override
        public String getMeaning()
        {
            return meaning;
        }

        @Override
        public int getCode()
        {
            return code;
        }
    }

}
