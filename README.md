# JDA (Java Discord API)
JDA strives to provide a clean and full wrapping of the Discord REST api and its Websocket-Events for Java.<br>
Besides audio, this library is full featured, allowing every operation that the Discord Client can provide.

## Creating the JDA Object
Creating the JDA Object is done via the JDABuilder class.
After setting email and password either via Constructor, or via setters,
the JDA Object is then created by calling the `.build()` (non-blocking login) or the `.buildBlocking()` method.
<p>
Examples:

```java
JDA jda = new JDABuilder("email", "password").buildBlocking();
```

```java
JDA jda = new JDABuilder().setEmail("email").setPassword("password").buildBlocking();
```

## Events
There a TON of events in JDA that you can listen to.<br>
There are 2 ways of writing your Event-Listener:
  1. Extend ListenerAdapter and use the provided methods that get fire dependent on the Event-Type. [Event Methods](https://github.com/DV8FromTheWorld/JDA/blob/master/src/main/java/net/dv8tion/jda/hooks/ListenerAdapter.java#L179-L254)
  2. Implement EventListener and listen to onEvent and figure out if it is the event you want (Not suggested)<br>

Listeners can be registered either in the JDABuilder (will catch all Events; recommended), or in the JDA instance (initial Events, especially the *READY*-Event could get lost)

#### Examples:
```java
public class ReadyListener implements EventListener
{
    public static void main(String[] args)
    {
        JDA jda = new JDABuilder(args[0], args[1]).addListener(new ReadyListener()).build();
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
        JDA jda = new JDABuilder(args[0], args[1]).build();
        jda.addListener(new MessageListener());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        System.out.printf("[%s][%s] %s: %s\n", event.getGuild().getName(),
            event.getChannel().getName(), event.getAuthor().getUsername(),
            event.getMessage().getContent());
    }
}
```

## More Examples
We provide a small set of Examples in the [Example Directory](https://github.com/DV8FromTheWorld/JDA/tree/master/src/examples/java).

## Download
Current Promoted Version:

![JDA promoted verison](https://www.dropbox.com/s/4jddygn33340uf4/version.png?dl=1)

You can get the latest promoted builds here:
[Promoted Downloads](https://github.com/DV8FromTheWorld/JDA/releases)


If you want the most up-to-date builds, you can get them here: [Beta Build Downloads](http://home.dv8tion.net:8080/job/JDA/)<br>
**Note:** It is quite possible that these are broken or bugged. Use with caution.

## Docs
Javadocs are available in both jar format and web format.<br>
The jar format is available on the [Promoted Downloads](https://github.com/DV8FromTheWorld/JDA/releases) page or on any of the
build pages of the [Beta Downloads](http://home.dv8tion.net:8080/job/JDA/).<br>
<br>
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

## TODO
* Voice
  * ~~Sending audio~~ (Completed)
  * Receiving audio
    * Ability to access a single combined stream and individual user audio streams.
  * ~~Usage examples~~
  * Extensive documentation
* ~~Private Messages~~
  * ~~Sending Private Messages~~
  * ~~Figure out a good Event system that handles both, private and guild messages~~
  * ~~Implement the Handler-code types other than MESSAGE_CREATE~~
* ~~Invites~~
* ~~Changing Account details (username, email, avatar, password)~~
* ~~Changing the own Presence~~
* ~~Modifying the server~~
* ~~Permissions~~
  * ~~Implement Exceptions~~
  * ~~Revisit the Permission calculation~~
* Read-States (which Message was last read in which channel)
  * ~~Message-ACK~~

## Dependencies:
This project requires **Java 8**.<br>
All dependencies are managed automatically by Gradle.
 * NV Websocket Client
   * Version: **1.16**
   * [Github](https://github.com/TakahikoKawasaki/nv-websocket-client)
   * [Central Repository](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.neovisionaries%22%20AND%20a%3A%22nv-websocket-client%22)
 * Apache Commons Lang3
   * Version: **3.4**
   * [Website](https://commons.apache.org/proper/commons-lang/)
   * [Central Repository](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.apache.commons%22%20AND%20a%3A%22commons-lang3%22)
 * json.org
   * Version: **20150729**
   * [Github](https://github.com/douglascrockford/JSON-java)
   * [Central Repository](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.json%22%20AND%20a%3A%22json%22)
