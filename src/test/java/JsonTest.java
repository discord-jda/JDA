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

import net.dv8tion.jda.api.utils.data.DataObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonTest
{
    private static final String json = "{\"int\":10,\"long\":100,\"boolean\":true,\"string\":\"test\"}";

    @Test
    public void testParse()
    {
        DataObject object = DataObject.fromJson(json);
        Assertions.assertEquals(10, object.getInt("int", 0));
        Assertions.assertEquals(100, object.getLong("long", 0));
        Assertions.assertEquals(true, object.getBoolean("boolean", false));
        Assertions.assertEquals("test", object.getString("string", null));
    }

    @Test
    public void testJsonToString()
    {
        DataObject object = DataObject.fromJson(json);
        String result = object.toString();
        DataObject symmetric = DataObject.fromJson(result);
        Assertions.assertEquals(object.toMap(), symmetric.toMap()); // lucky that this works here :)
    }
}
