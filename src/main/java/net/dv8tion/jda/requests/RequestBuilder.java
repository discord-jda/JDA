/**
 *    Copyright 2015 Austin Keener & Michael Ritter
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

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDAInfo;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

public class RequestBuilder
{
    protected String data = "";
    protected String url = "";
    protected RequestType type = null;
    protected HttpURLConnection con;
    protected boolean sendLoginHeaders = true;
    protected boolean getResponse = true;
    protected int code = 200;
    protected JDA api;

    public RequestBuilder(JDA api)
    {
        this.api = api;
    }

    public String makeRequest()
    {
        try
        {
            if (type == RequestType.PATCH)
            {
                try
                {
                    String[] urlparts = url.split("/", 4);
                    boolean ishttps = urlparts[0].startsWith("https");
                    // apaches api isn't working very well for me
                    // and java doesn't support patch... so why
                    // not completely recode it for what we need?
                    Socket clientSocket;

                    if (ishttps)
                    {
                        SocketFactory sslf = SSLSocketFactory.getDefault();
                        clientSocket = sslf.createSocket(urlparts[2], 443);
                    }
                    else
                    {
                        clientSocket = new Socket(urlparts[2], 80);
                    }

                    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                    out.writeBytes("PATCH /" + urlparts[3] + " HTTP/1.1\n" +
                            "Host: " + urlparts[2] + "\n" +
                            "Connection: close\n" +
                            "Content-Length: " + data.length() + "\n" +
                            "Origin: http://discordapp.com\n" +
                            "User-Agent: " + JDAInfo.GITHUB + " " + JDAInfo.VERSION + "\n" +
                            "Content-Type: application/json\n" +
                            "Accept: */*\n" +
                            "authorization: " + api.getAuthToken() + "\n\n" + data);
                    if (getResponse)
                    {
                        DataInputStream din = new DataInputStream(clientSocket.getInputStream());
                        StringBuilder builder = new StringBuilder();
                        byte[] buffer = new byte[100];
                        int read;
                        while ((read = din.read(buffer)) >= 0)
                        {
                            builder.append(new String(buffer, 0, read));
                        }
                        out.close();
                        din.close();
                        clientSocket.close();

                        String[] responseParts = builder.toString().split("\\r\\n\\r\\n");
                        if (responseParts.length > 1)
                        {
                            return responseParts[1];
                        }
                        return null;
                    }
                    out.close();
                    clientSocket.close();
                    return null;

                } catch (Exception e)
                {
                    e.printStackTrace();
                    return null;
                }
            }

            URL obj = new URL(url);
            con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod(type.toString().equals("PATCH") ? "POST" : type.toString());


            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            con.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
            con.setRequestProperty("User-Agent", JDAInfo.GITHUB + " " + JDAInfo.VERSION);
            con.setDoOutput(getResponse);
            if (sendLoginHeaders)
                con.addRequestProperty("authorization", api.getAuthToken());

            if (!(data.getBytes().length == 0))
            {
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.write(data.getBytes());
                wr.flush();
                wr.close();
            }

            code = con.getResponseCode();

            if (code == 200 || code == 201)
            {
                if (getResponse)
                {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null)
                    {
                        response.append(inputLine);
                    }
                    in.close();
                    return response.toString();
                }
                return null;
            } else if (code == 204)
            {
                return "";
            } else
            {
                System.err.println("Error Queuing " + type + " " + url + "\n\tPayload: " + data + "\n\tResponse: " + code);
                return null;
            }

        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public void setSendLoginHeaders(boolean sendLoginHeaders)
    {
        this.sendLoginHeaders = sendLoginHeaders;
    }

    public void setGetResponse(boolean getResponse)
    {
        this.getResponse = getResponse;
    }

    public void setData(String data)
    {
        this.data = data;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public void setType(RequestType type)
    {
        this.type = type;
    }
}
