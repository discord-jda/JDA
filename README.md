# JDA (Java Discord API)
JDA strives to provide a clean and full wrapping of the Discord REST api and its Websocket-Events for Java.<br>

## JDA 2.x
After the release of the official Discord API, we decided to split JDA into 2 seperate libraries.

JDA will be continued with version 2.x and will only have Bot-features (for bot-accounts).
Please see the [Discord docs](https://discordapp.com/developers/docs/reference) for more information about bot accounts.


We are also creating a new library called JDA-Client, which extends the functionality of JDA 2.x
and add some client-only features like friends-list, acks, creating/owning Guilds,...<br/>
You can find JDA-Client [Here](https://github.com/DV8FromTheWorld/JDA-Client)

## Creating the JDA Object
Creating the JDA Object is done via the JDABuilder class.
After setting the bot-token via setter,
the JDA Object is then created by calling the `.buildBlocking()` or the `.buildAsync()` (non-blocking login) method.

Example:

```java
JDA jda = new JDABuilder().setBotToken("token").buildBlocking();
```

## Events
There a TON of events in JDA that you can listen to.<br>
There are 2 ways of writing your Event-Listener:
  1. Extend ListenerAdapter and use the provided methods that get fire dependent on the Event-Type. [Event Methods](https://github.com/DV8FromTheWorld/JDA/blob/master/src/main/java/net/dv8tion/jda/hooks/ListenerAdapter.java)
  2. Implement EventListener and listen to onEvent and figure out if it is the event you want (Not suggested)<br>

Listeners can be registered either in the JDABuilder (will catch all Events; recommended), or in the JDA instance (initial Events, especially the *READY*-Event could get lost)

#### Examples:
```java
public class ReadyListener implements EventListener
{
    public static void main(String[] args)
    {
        JDA jda = new JDABuilder().setBotToken("token").addListener(new ReadyListener()).buildBlocking();
    }

    @Override
    public void onEvent(Event event)
    {
        if(event instanceof ReadyEvent)
            System.out.println("API is ready!");
    }
}
```

```java
public class MessageListener extends ListenerAdapter
{
    public static void main(String[] args)
    {
        JDA jda = new JDABuilder().setBotToken("token").buildBlocking();
        jda.addEventListener(new MessageListener());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.isPrivate())
        {
            System.out.printf("[PM] %s: %s\n", event.getAuthor().getUsername(),
                                    event.getMessage().getContent());
        }
        else
        {
            System.out.printf("[%s][%s] %s: %s\n", event.getGuild().getName(),
                        event.getTextChannel().getName(), event.getAuthor().getUsername(),
                        event.getMessage().getContent());
        }
    }
}
```

## More Examples
We provide a small set of Examples in the [Example Directory](https://github.com/DV8FromTheWorld/JDA/tree/master/src/examples/java).

## Download
Current Promoted Version:

![JDA promoted verison](https://www.dropbox.com/s/4jddygn33340uf4/version.png?dl=1)

You can get the latest promoted builds here:
[Promoted Downloads](https://github.com/DV8FromTheWorld/JDA/releases)<br>
(Contains information about Maven and Gradle distribution)


If you want the most up-to-date builds, you can get them here: [Beta Build Downloads](http://home.dv8tion.net:8080/job/JDA/)<br>
**Note:** It is quite possible that these are broken or bugged. Use with caution.<br>
The dev builds are also available for maven/gradle on JCenter through Bintray [JDA JCenter Bintray](https://bintray.com/dv8fromtheworld/maven/JDA/)

## Docs
Javadocs are available in both jar format and web format.<br>
The jar format is available on the [Promoted Downloads](https://github.com/DV8FromTheWorld/JDA/releases) page or on any of the
build pages of the [Beta Downloads](http://home.dv8tion.net:8080/job/JDA/).

The web format allows for viewing of the [Latest Promoted Docs](http://home.dv8tion.net:8080/job/JDA/Promoted%20Build/javadoc/)
and also viewing of each individual build's javadoc. To view the javadoc for a specific build, you will need to go to that build's page
on [the build server](http://home.dv8tion.net:8080/job/JDA/) and click the javadoc button on the left side of the build page.<br>
A shortcut would be: http://home.dv8tion.net:8080/job/JDA/BUILD_NUMBER_GOES_HERE/javadoc/, you just need to replace the 
"BUILD_NUMBER_GOES_HERE" with the build you want.<br>
Example: Build 90's javadoc url would be http://home.dv8tion.net:8080/job/JDA/90/javadoc/

## Getting Help
If you need help, or just want to talk with the JDA or other Discord Devs, you can join the [Unofficial Discord API](https://discord.gg/0SBTUU1wZTUydsWv) Guild.

Once you joined, you can find JDA-specific help in the #java_jda channel

## Contributing to JDA
If you want to contribute to JDA, make sure to base your branch off of our development branch (or a feature-branch)
and create your PR into that same branch. **We will be rejecting any PRs to master or between branches!**

It is also highly recommended to get in touch with the Devs via the Discord API Guild (see section above).

## Dependencies:
This project requires **Java 8**.<br>
All dependencies are managed automatically by Gradle.
 * NV Websocket Client
   * Version: **1.16**
   * [Github](https://github.com/TakahikoKawasaki/nv-websocket-client)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/com.neovisionaries%3Anv-websocket-client/view)
 * Unirest
   * Version: **1.4.7**
   * [Github](https://github.com/Mashape/unirest-java)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/com.mashape.unirest%3Aunirest-java/view)
 * Apache Commons Lang3
   * Version: **3.4**
   * [Website](https://commons.apache.org/proper/commons-lang/)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/org.apache.commons%3Acommons-lang3/view)
 * json.org
   * Version: **20150729**
   * [Github](https://github.com/douglascrockford/JSON-java)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/org.json%3Ajson/view)
 * JNA
   * Version: **4.2.1**
   * [Github](https://github.com/java-native-access/jna)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/net.java.dev.jna%3Ajna/view)
 * Tritonus-Share
   * Version: **0.3.7-2**
   * [Sourceforge](https://sourceforge.net/projects/tritonus/)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/com.googlecode.soundlibs%3Atritonus-share/view)
 * Tritonus-DSP
   * Version: **0.3.6**
   * [Sourceforge](https://sourceforge.net/projects/tritonus/)
   * [JCenter Repository](https://bintray.com/dv8fromtheworld/maven/Tritonus-DSP/view)
 * MP3-SPI
   * Version: **1.9.5-1**
   * [Website](http://www.javazoom.net/mp3spi/mp3spi.html)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/com.googlecode.soundlibs%3Amp3spi/view)
 * jFLAC
   * Version: **1.3**
   * [Sourceforge](https://sourceforge.net/projects/jflac/)
   * [JCenter Repository](https://bintray.com/dv8fromtheworld/maven/jFLAC/view)
 * JAADec
   * Version: **0.8.5**
   * [Github](https://github.com/DV8FromTheWorld/JAADec)
   * [JCenter Repository](https://bintray.com/dv8fromtheworld/maven/JAADec/view)
