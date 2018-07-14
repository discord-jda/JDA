[version]: https://api.bintray.com/packages/dv8fromtheworld/maven/JDA/images/download.svg
[download]: https://bintray.com/dv8fromtheworld/maven/JDA/_latestVersion
[discord-invite]: https://discord.gg/0hMr4ce0tIl3SLv5
[license]: https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg
[jenkins]: https://img.shields.io/badge/Download-Jenkins-brightgreen.svg
[FAQ]: https://img.shields.io/badge/Wiki-FAQ-blue.svg
[ ![version][] ][download]
[ ![jenkins][] ](http://home.dv8tion.net:8080/job/JDA/lastSuccessfulBuild/)
[ ![license][] ](https://github.com/DV8FromTheWorld/JDA/tree/master/LICENSE)
[ ![Discord](https://discordapp.com/api/guilds/125227483518861312/widget.png) ][discord-invite]
[ ![FAQ] ](https://github.com/DV8FromTheWorld/JDA/wiki/10\)-FAQ)

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
                                    event.getMessage().getContentDisplay());
        }
        else
        {
            System.out.printf("[%s][%s] %s: %s\n", event.getGuild().getName(),
                        event.getTextChannel().getName(), event.getMember().getEffectiveName(),
                        event.getMessage().getContentDisplay());
        }
    }
}
```

> **Note**: In these examples we override methods from the inheriting class `ListenerAdapter`.<br>
> The usage of the `@Override` annotation is recommended to validate methods.

### Sharding a Bot

Discord allows Bot-accounts to share load across sessions by limiting them to a fraction of the total connected Guilds/Servers of the bot.
<br>This can be done using **sharding** which will limit JDA to only a certain amount of Guilds/Servers including events and entities.
Sharding will limit the amount of Guilds/Channels/Users visible to the JDA session so it is recommended to have some kind of elevated management to
access information of other shards.

To use sharding in JDA you will need to use `JDABuilder.useSharding(int shardId, int shardTotal)`. The **shardId** is 0-based which means the first shard
has the ID 0. The **shardTotal** is the total amount of shards (not 0-based) which can be seen similar to the length of an array, the last shard has the ID of
`shardTotal - 1`.

The [`SessionController`](https://home.dv8tion.net:8080/job/JDA/javadoc/net/dv8tion/jda/core/utils/SessionController.html) is a tool of the JDABuilder
that allows to control state and behaviour between shards (sessions). When using multiple builders to build shards you have to create one instance
of this controller and add the same instance to each builder: `builder.setSessionController(controller)`

Since version **3.4.0** JDA provides a `ShardManager` which automates this building process.

#### Example Sharding - Using JDABuilder

```java
public static void main(String[] args) throws Exception
{
    JDABuilder shardBuilder = new JDABuilder(AccountType.BOT).setToken(args[0]);
    //register your listeners here using shardBuilder.addEventListener(...)
    shardBuilder.addEventListener(new MessageListener());
    for (int i = 0; i < 10; i++)
    {
        shardBuilder.useSharding(i, 10)
                    .buildAsync();
    }
}
```

> When the `useSharding` method is invoked for the first time, the builder automatically sets a SessionController internally (if none is present)

#### Example Sharding - Using DefaultShardManager
```java
public static void main(String[] args) throws Exception
{
    DefaultShardManagerBuilder builder = new DefaultShardManagerBuilder();
    builder.setToken(args[0]);
    builder.addEventListener(new MessageListener());
    builder.build();
}
```

## More Examples
We provide a small set of Examples in the [Example Directory](https://github.com/DV8FromTheWorld/JDA/tree/master/src/examples/java).

In addition you can look at the many Discord Bots that were implemented using JDA:
- [Yui](https://github.com/DV8FromTheWorld/Yui)
- [Vortex](https://github.com/jagrosh/Vortex)
- [FredBoat](https://github.com/Frederikam/FredBoat)

[And many more!](https://github.com/search?q=JDA+discord+bot&type=Repositories&utf8=%E2%9C%93)

## Download
Latest Stable Version: [GitHub Release](https://github.com/DV8FromTheWorld/JDA/releases/latest)
Latest Version:
[ ![version][] ][download]

Be sure to replace the **VERSION** key below with the one of the versions shown above!

**Maven**
```xml
<dependency>
    <groupId>net.dv8tion</groupId>
    <artifactId>JDA</artifactId>
    <version>VERSION</version>
</dependency>
```
```xml
<repository>
    <id>jcenter</id>
    <name>jcenter-bintray</name>
    <url>http://jcenter.bintray.com</url>
</repository>

```

**Maven without Audio**
```xml
<dependency>
    <groupId>net.dv8tion</groupId>
    <artifactId>JDA</artifactId>
    <version>VERSION</version>
    <exclusions>
        <exclusion>
            <artifactId>opus-java</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

**Gradle**
```gradle
dependencies {
    compile 'net.dv8tion:JDA:VERSION'
}

repositories {
    jcenter()
}
```

**Gradle without Audio**
```gradle
dependencies {
    compile ('net.dv8tion:JDA:VERSION') {
        exclude module: 'opus-java'
    }
}
```

The builds are distributed using JCenter through Bintray [JDA JCenter Bintray](https://bintray.com/dv8fromtheworld/maven/JDA/)

If you do not need any opus de-/encoding done by JDA (voice receive/send with PCM) you can exclude `opus-java` entirely.
This can be done if you only send audio with an `AudioSendHandler` which only sends opus (`isOpus() = true`). (See [lavaplayer](https://github.com/sedmelluq/lavaplayer))

If you want to use a custom opus library you can provide the absolute path to `OpusLibrary.loadFrom(String)` before using
the audio api of JDA. This works without `opus-java-natives` as it only requires `opus-java-api`.
<br>_For this setup you should only exclude `opus-java-natives` as `opus-java-api` is a requirement for en-/decoding._

See [opus-java](https://github.com/discord-java/opus-java)

### Logging Framework - SLF4J
JDA is using [SLF4J](https://www.slf4j.org/) to log its messages.

That means you should add some SLF4J implementation to your build path in addition to JDA.
If no implementation is found, following message will be printed to the console on startup:
```
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
```

JDA currently provides a fallback Logger in case that no SLF4J implementation is present.
We strongly recommend to use one though, as that can improve speed and allows you to customize the Logger as well as log to files

The most popular implementations are [Log4j 2](https://logging.apache.org/log4j/2.x/) and [Logback](https://logback.qos.ch/)

## Documentation
Docs can be found on the [Jenkins](http://home.dv8tion.net:8080/) or directly [here](http://home.dv8tion.net:8080/job/JDA/javadoc/)
<br>A simple Wiki can also be found in this repository's [Wiki section](https://github.com/DV8FromTheWorld/JDA/wiki)

## Getting Help
If you need help, or just want to talk with the JDA or other Devs, you can join the [Official JDA Discord Guild][discord-invite].

Alternatively you can also join the [Unofficial Discord API Guild](https://discord.gg/discord-api).
Once you joined, you can find JDA-specific help in the `#java_jda` channel.

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
If you want to contribute to JDA, make sure to base your branch off of our **development** branch (or a feature-branch)
and create your PR into that **same** branch. **We will be rejecting any PRs between branches or into release branches!**

It is also highly recommended to get in touch with the Devs before opening Pull Requests (either through an issue or the Discord servers mentioned above).<br>
It is very possible that your change might already be in development or you missed something.

More information can be found at the wiki page [Contributing](https://github.com/DV8FromTheWorld/JDA/wiki/5\)-Contributing)

### Deprecation Policy

When a feature is introduced to replace or enhance existing functionality we might deprecate old functionality.

A deprecated method/class usually has a replacement mentioned in its documentation which should be switched to. Deprecated
functionality might or might not exist in the next minor release. (Hint: The minor version is the `MM` of `XX.MM.RR_BB` in our version format)

It is possible that some features are deprecated without replacement, in this case the functionality is no longer supported by either the JDA structure
due to fundamental changes (for example automation of a feature) or due to discord API changes that cause it to be removed.

We highly recommend to discontinue usage of deprecated functionality and update by going through each minor release instead of jumping.
For instance, when updating from version 3.3.0 to version 3.5.1 you should do the following:

- Update to `3.4.RR_BB` and check for deprecation, replace
- Update to `3.5.1_BB` and check for deprecation, replace

The `BB` indicates the build number specified in the release details.

The `RR` in version `3.4.RR` should be replaced by the latest version that was published for `3.4`, you can find out which the latest
version was by looking at the [release page](https://github.com/DV8FromTheWorld/JDA/releases)

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
 * Apache Commons Collections4
   * Version: **4.1**
   * [Website](https://commons.apache.org/proper/commons-collections/)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/org.apache.commons%3Acommons-collections4/view)
 * org.json
   * Version: **20160810**
   * [Github](https://github.com/douglascrockford/JSON-java)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/org.json%3Ajson/view)
 * Trove4j
   * Version: **3.0.3**
   * [BitBucket](https://bitbucket.org/trove4j/trove)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/net.sf.trove4j%3Atrove4j/view)
 * slf4j-api
   * Version: **1.7.25**
   * [Website](https://www.slf4j.org/)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/org.slf4j%3Aslf4j-api/view)
 * opus-java
   * Version: **1.0.2**
   * [GitHub](https://github.com/discord-java/opus-java)
   * [JCenter Repository](https://bintray.com/minndevelopment/maven/opus-java)

## Related Projects

- [Discord4J](https://github.com/austinv11/Discord4J)
- [Discord.NET](https://github.com/RogueException/Discord.Net)
- [discord.py](https://github.com/Rapptz/discord.py)
- [serenity](https://github.com/zeyla/serenity)

**See also:** https://discordapp.com/developers/docs/topics/community-resources#libraries
