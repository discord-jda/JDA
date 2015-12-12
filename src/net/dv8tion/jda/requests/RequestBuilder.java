package net.dv8tion.jda.requests;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class RequestBuilder
{
    private static String authToken;
    protected String data = "";
    protected String url = "";
    protected RequestType type = null;
    protected Map<String, String> headers = new HashMap<String, String>();
    protected HttpURLConnection con;
    protected boolean sendLoginHeaders = true;
    protected int code = 200;

    public static void setAuthToken(String authToken)
    {
        RequestBuilder.authToken = authToken;
    }

    protected void addAuth()
    {
        addHeader("authorization", authToken);
    }

    public void addHeader(String key, String val)
    {
        headers.put(key, val);
    }

    public String makeRequest()
    {
        try
        {
            URL obj = new URL(url);
            con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod(type.toString().equals("PATCH") ? "POST" : type.toString());

            if (type == RequestType.PATCH)
            {
                try
                {
                    // apaches api isn't working very well for me
                    // and java doesn't support patch... so why
                    // not completely recode it for what we need?
                    Socket clientSocket = new Socket("discordapp.com", 80);
                    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                    out.writeBytes("PATCH " + url + " HTTP/1.1\n" +
                            "Host: discordapp.com\n" +
                            "Connection: keep-alive\n" +
                            "Content-Length: " + data.length() + "\n" +
                            "Origin: http://discordapp.com\n" +
                            "User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.130 Safari/537.36 OPR/31.0.1889.151\n" +
                            "Content-Type: application/json\n" +
                            "Accept: */*\n" +
                            "authorization: " + authToken + "\n\n" + data);
                    out.close();
                    clientSocket.close();
                    return null;

                } catch (Exception e)
                {
                    e.printStackTrace();
                    return null;
                }
            }

            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            con.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
            con.setRequestProperty("User-Agent", "https://github.com/DV8FromTheWorld/JDA v0.0.1");
            con.setDoOutput(true);
            if (sendLoginHeaders)
                addAuth();
            for (String s : headers.keySet())
                con.addRequestProperty(s, headers.get(s));

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
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null)
                {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
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
