/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ExampleUtils
{
    //Simple config system to make life easier. THIS IS NOT REQUIRED FOR JDA.
    public static JSONObject getConfig()
    {
        File config = new File("config.json");
        if (!config.exists())
        {
            try
            {
                Files.write(Paths.get(config.getPath()),
                        new JSONObject()
                                .put("email", "")
                                .put("password", "")
                                .put("proxyHost", "")
                                .put("proxyPort", 8080)
                                .put("version", 2)
                                .toString(4).getBytes());
                System.out.println("config.json created. Populate with login information.");
                System.exit(0);
            }
            catch (JSONException | IOException e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            JSONObject object = new JSONObject(new String(Files.readAllBytes(Paths.get(config.getPath())), "UTF-8"));
            if (!object.has("version"))
            {
                object.put("version", 1);
            }
            switch(object.getInt("version"))
            {
                case 1:
                    object
                        .put("proxyHost", "")
                        .put("proxyPort", 8080);
                    //Setting new version and writing
                    object.put("version", 2);
                    Files.write(Paths.get(config.getPath()), object.toString(4).getBytes());
                    break;
                default:
            }
            return object;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
