[version]: https://api.bintray.com/packages/dv8fromtheworld/maven/JDA/images/download.svg
[download]: https://bintray.com/dv8fromtheworld/maven/JDA/_latestVersion
[discord-invite]: https://discord.gg/0hMr4ce0tIl3SLv5
[license]: https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg
[jenkins]: https://img.shields.io/badge/Download-Jenkins-brightgreen.svg
[ ![version][] ][download]
[ ![jenkins][] ](http://home.dv8tion.net:8080/job/JDA/lastSuccessfulBuild/)
[ ![license][] ](https://github.com/DV8FromTheWorld/JDA/tree/master/LICENSE)
[ ![Discord](https://discordapp.com/api/guilds/125227483518861312/widget.png) ][discord-invite]

<img align="right" src="https://i.imgur.com/OG7Tne8.png" height="200" width="200">

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

**Example**:

```java
JDA jda = new JDABuilder(AccountType.BOT).setToken("token").buildBlocking();
```

**Note**: It is important to set the correct AccountType because Bot-accounts require a token prefix to login.

#### Examples:

**Using EventListener**:
```java
public class ReadyListener implements EventListener
{
    public static void main(String[] args)
            throws LoginException, RateLimitedException, InterruptedException
    {
        // Note: It is important to register your ReadyListener before building
        JDA jda = new JDABuilder(AccountType.BOT)
            .setToken("token")
            .addEventListener(new ReadyListener())
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
**Using ListenerAdapter**:
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

In addition you can look at the many Discord Bots that were implemented using JDA:
- [Yui](https://github.com/DV8FromTheWorld/Yui)
- [Vortex](https://github.com/jagrosh/Vortex)
- [FredBoat](https://github.com/Frederikam/FredBoat)

[And many more!](https://github.com/search?q=JDA+discord+bot&type=Repositories&utf8=%E2%9C%93)

## Download
Latest Version:
[ ![version][] ][download]

Be sure to replace the **VERSION** key below with the latest version shown above!

Maven
```xml
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
```gradle
dependencies {
    compile 'net.dv8tion:JDA:VERSION'
}

repositories {
    jcenter()
}
```

The builds are distributed using JCenter through Bintray [JDA JCenter Bintray](https://bintray.com/dv8fromtheworld/maven/JDA/)

## Documentation
Docs can be found on the [Jenkins](http://home.dv8tion.net:8080/) or directly [here](http://home.dv8tion.net:8080/job/JDA/javadoc/)
<br>A simple Wiki can also be found in this repository's [Wiki section](https://github.com/DV8FromTheWorld/JDA/wiki)

## Getting Help
If you need help, or just want to talk with the JDA or other Discord Devs, you can join the [Unofficial Discord API](https://discord.gg/0SBTUU1wZTUydsWv) Guild.

Once you joined, you can find JDA-specific help in the #java_jda channel<br>
We have our own Discord Server [here][discord-invite]

For guides and setup help you can also take a look at the [wiki](https://github.com/DV8FromTheWorld/JDA/wiki)
<br>Especially interesting are the [Getting Started](https://github.com/DV8FromTheWorld/JDA/wiki/3\)-Getting-Started)
and [Setup](https://github.com/DV8FromTheWorld/JDA/wiki/2\)-Setup) Pages.

## Third Party Recommendations

### [LavaPlayer](https://github.com/sedmelluq/lavaplayer)

Created and maintained by [sedmelluq](https://github.com/sedmelluq)
<br>LavaPlayer is the most popular library used by Music Bots created in Java.
It is highly compatible with JDA and Discord4J and allows to play audio from
Youtube, Soundcloud, Twitch, Bandcamp and [more providers](https://github.com/sedmelluq/lavaplayer#supported-formats). 
<br>The library can easily be expanded to more services by implementing your own AudioSourceManager and registering it.

It is recommended to read the [Usage](https://github.com/sedmelluq/lavaplayer#usage) section of LavaPlayer
to understand a proper implementation.
<br>Sedmelluq provided a demo in his repository which presents an example implementation for JDA:
https://github.com/sedmelluq/lavaplayer/tree/master/demo-jda


### [JDA-Utilities](https://github.com/JDA-Applications/JDA-Utilities)

Created and maintained by [jagrosh](https://github.com/jagrosh).
<br>JDA-Utilities provides a Command-Extension and several utilities to make using JDA very simple.

Features include:
- Paginated Message using Reactions
- EventWaiter allowing to wait for a response and other events

### [Kotlin-JDA](https://github.com/JDA-Applications/Kotlin-JDA)

Created and maintained by [MinnDevelopment](https://github.com/MinnDevelopment)
<br>Kotlin-JDA provides several extensions allowing to easily use kotlin idioms with JDA.

Features include:
- Groovy-style Builders
- Coroutine RestActions

### [JDAction](https://github.com/sedmelluq/jdaction)

Created and maintained by [sedmelluq](https://github.com/sedmelluq)
<br>JDAction is a [Gradle](https://gradle.org/) plugin which makes sure that the return values of all methods which return a RestAction are used.
Since it is a common mistake to forget to `.queue()`/`.complete()`/`.submit()` RestActions,
and it is often only discovered after noticing that something doesn't work,
this plugin will help catch those cases quickly as it will cause a build failure in such case.

More info about RestAction: [Wiki](https://github.com/DV8FromTheWorld/JDA/wiki/7\)-Using-RestAction)

------

More can be found in our github organization: [JDA-Applications](https://github.com/JDA-Applications)

## Contributing to JDA
If you want to contribute to JDA, make sure to base your branch off of our master branch (or a feature-branch)
and create your PR into that **same** branch. **We will be rejecting any PRs between branches!**

It is also highly recommended to get in touch with the Devs before opening Pull Requests (either through an issue or the Discord servers mentioned above).<br>
It is very possible that your change might already be in development or you missed something.

More information can be found at the wiki page [Contributing](https://github.com/DV8FromTheWorld/JDA/wiki/5\)-Contributing)

## Dependencies:
This project requires **Java 8**.<br>
All dependencies are managed automatically by Gradle.
 * NV Websocket Client
   * Version: **2.2**
   * [Github](https://github.com/TakahikoKawasaki/nv-websocket-client)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/com.neovisionaries%3Anv-websocket-client/view)
 * OkHttp
   * Version: **3.8.1**
   * [Github](https://github.com/square/okhttp)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/com.squareup.okhttp:okhttp)
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
   * Version: **4.4.0**
   * [Github](https://github.com/java-native-access/jna)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/net.java.dev.jna%3Ajna/view)
 * Trove4j
   * Version: **3.0.3**
   * [BitBucket](https://bitbucket.org/trove4j/trove)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/net.sf.trove4j%3Atrove4j/view)
   
## Related Projects

- [Discord4J](https://github.com/austinv11/Discord4J)
- [Discord.NET](https://github.com/RogueException/Discord.Net)
- [discord.py](https://github.com/Rapptz/discord.py)
- [serenity](https://github.com/zeyla/serenity)

**See also:** https://discordapp.com/developers/docs/topics/libraries
