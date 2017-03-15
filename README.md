# JDA (Java Discord API)
JDA strives to provide a clean and full wrapping of the Discord REST api and its Websocket-Events for Java.

## JDA 3.x
JDA will be continued with version 3.x and will support Bot-features (for bot-accounts) and Client-features (for user-accounts).
_Please see the [Discord docs](https://discordapp.com/developers/docs/reference) for more information about bot accounts._


This officially makes [JDA-Client](https://github.com/DV8FromTheWorld/JDA-Client) deprecated.
Please do not continue using it, and instead switch to the promoted 3.x version listed further below.

## Creating the JDA Object
Creating the JDA Object is done via the JDABuilder class by providing an AccountType (Bot/Client).
After setting the token via setter,
the JDA Object is then created by calling the `.buildBlocking()` or the `.buildAsync()` (non-blocking login) method.

Example:

```java
JDA jda = new JDABuilder(AccountType.BOT).setToken("token").buildBlocking();
```

**Note**: It is important to set the correct AccountType because Bot-accounts require a token prefix to login.

## Events
There are a ***TON*** of events in JDA that you can listen to.<br>
Currently, there are 2 ways of writing your Event-Listener:
  1. Extend ListenerAdapter and use the provided methods that get fired depending on the Event-Type. [Event Methods](https://github.com/DV8FromTheWorld/JDA/blob/master/src/main/java/net/dv8tion/jda/core/hooks/ListenerAdapter.java)
  2. Implement EventListener and listen to onEvent and figure out if it is the event you want (Not suggested)<br>

Listeners can be registered either in the JDABuilder (will catch all Events; recommended), or in the JDA instance (initial Events, especially the *READY*-Event could get lost)

#### Examples:
```java
public class ReadyListener implements EventListener
{
    public static void main(String[] args)
            throws LoginException, RateLimitedException, InterruptedException
    {
        // Note: It is important to register your ReadyListener before building
        JDA jda = new JDABuilder(AccountType.BOT)
            .setToken("token")
            .addListener(new ReadyListener())
            .buildBlocking();
    }

    @Override
    public void onEvent(Event event)
    {
        if (event instanceof ReadyEvent)
            System.out.println("API is ready!");
    }
}
```

```java
public class MessageListener extends ListenerAdapter
{
    public static void main(String[] args)
            throws LoginException, RateLimitedException, InterruptedException
    {
        JDA jda = new JDABuilder(AccountType.BOT).setToken("token").buildBlocking();
        jda.addEventListener(new MessageListener());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.isFromType(ChannelType.PRIVATE))
        {
            System.out.printf("[PM] %s: %s\n", event.getAuthor().getName(),
                                    event.getMessage().getContent());
        }
        else
        {
            System.out.printf("[%s][%s] %s: %s\n", event.getGuild().getName(),
                        event.getTextChannel().getName(), event.getMember().getEffectiveName(),
                        event.getMessage().getContent());
        }
    }
}
```

> **Note**: In these examples we override methods from the inheriting class `ListenerAdapter`.<br>
> The usage of the `@Override` annotation is recommended to validate methods.

## More Examples
We provide a small set of Examples in the [Example Directory](https://github.com/DV8FromTheWorld/JDA/tree/master/src/examples/java).

## Download
Latest Version:
[ ![Download](https://api.bintray.com/packages/dv8fromtheworld/maven/JDA/images/download.svg) ](https://bintray.com/dv8fromtheworld/maven/JDA/_latestVersion)

Be sure to replace the **VERSION** key below with the latest version shown above!

Maven
```
<dependency>
    <groupId>net.dv8tion</groupId>
    <artifactId>JDA</artifactId>
    <version>VERSION</version>
</dependency>

<repository>
    <id>jcenter</id>
    <name>jcenter-bintray</name>
    <url>http://jcenter.bintray.com</url>
</repository>
```

Gradle
```
dependencies {
    compile 'net.dv8tion:JDA:VERSION'
}

repositories {
    jcenter()
}
```

The builds are distributed using JCenter through Bintray [JDA JCenter Bintray](https://bintray.com/dv8fromtheworld/maven/JDA/)

## Docs
Docs can be found on the [Jenkins](http://home.dv8tion.net:8080/) or directly [here](http://home.dv8tion.net:8080/job/JDA/javadoc/)
<br>A simple Wiki can also be found in this repo's [Wiki section](https://github.com/DV8FromTheWorld/JDA/wiki)

## Getting Help
If you need help, or just want to talk with the JDA or other Discord Devs, you can join the [Unofficial Discord API](https://discord.gg/0SBTUU1wZTUydsWv) Guild.

Once you joined, you can find JDA-specific help in the #java_jda channel<br>
We have our own Discord Server [here](https://discord.gg/0hMr4ce0tIl3SLv5)

For guides and setup help you can also take a look at the [wiki](https://github.com/DV8FromTheWorld/JDA/wiki)

## Contributing to JDA
If you want to contribute to JDA, make sure to base your branch off of our master branch (or a feature-branch)
and create your PR into that **same** branch. **We will be rejecting any PRs between branches!**

It is also highly recommended to get in touch with the Devs before opening Pull Requests (either through an issue or the Discord servers mentioned above).<br>
It is very possible that your change might already be in development or you missed something.

More information can be found at the wiki page [5) Contributing](https://github.com/DV8FromTheWorld/JDA/wiki/5\)-Contributing)

## Dependencies:
This project requires **Java 8**.<br>
All dependencies are managed automatically by Gradle.
 * NV Websocket Client
   * Version: **1.30**
   * [Github](https://github.com/TakahikoKawasaki/nv-websocket-client)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/com.neovisionaries%3Anv-websocket-client/view)
 * Unirest
   * Version: **1.4.9**
   * [Github](https://github.com/Mashape/unirest-java)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/com.mashape.unirest%3Aunirest-java/view)
 * Apache Commons Lang3
   * Version: **3.5**
   * [Website](https://commons.apache.org/proper/commons-lang/)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/org.apache.commons%3Acommons-lang3/view)
 * Apache Commons Collections4
   * Version: **4.1**
   * [Website](https://commons.apache.org/proper/commons-collections/)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/org.apache.commons%3Acommons-collections4/view)
 * org.json
   * Version: **20160810**
   * [Github](https://github.com/douglascrockford/JSON-java)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/org.json%3Ajson/view)
 * JNA
   * Version: **4.2.2**
   * [Github](https://github.com/java-native-access/jna)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/net.java.dev.jna%3Ajna/view)
