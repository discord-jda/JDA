package net.dv8tion.jda.handle;

import org.json.JSONObject;

/**
 * Created by Michael Ritter on 13.12.2015.
 */
public interface ISocketHandler
{
    void handle(JSONObject content);
}
