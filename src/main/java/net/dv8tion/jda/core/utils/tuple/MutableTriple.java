/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
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

package net.dv8tion.jda.core.utils.tuple;

public class MutableTriple<LEFT, MIDDLE, RIGHT> extends MutablePair<LEFT, RIGHT>
{
    // public because it is also public in pair
    public MIDDLE middle;

    private MutableTriple(LEFT left, MIDDLE middle, RIGHT right)
    {
        super(left, right);
        this.middle = middle;
    }

    public static <LEFT, MIDDLE, RIGHT> MutableTriple<LEFT, MIDDLE, RIGHT> of(LEFT left, MIDDLE middle, RIGHT right)
    {
        return new MutableTriple<>(left, middle, right);
    }

    public MIDDLE getMiddle()
    {
        return middle;
    }

    public void setMiddle(MIDDLE middle)
    {
        this.middle = middle;
    }
}
