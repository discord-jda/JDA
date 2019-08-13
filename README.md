[version]: https://api.bintray.com/packages/dv8fromtheworld/maven/JDA/images/download.svg
[download]: https://bintray.com/dv8fromtheworld/maven/JDA/_latestVersion
[discord-invite]: https://discord.gg/0hMr4ce0tIl3SLv5
[migration]: https://github.com/DV8FromTheWorld/JDA/wiki/0\)-Migrating-to-V4
[jenkins]: https://ci.dv8tion.net/job/JDA
[license]: https://github.com/DV8FromTheWorld/JDA/tree/master/LICENSE
[faq]: https://github.com/DV8FromTheWorld/JDA/wiki/10\)-FAQ
[troubleshooting]: https://github.com/DV8FromTheWorld/JDA/wiki/19\)-Troubleshooting
[discord-shield]: https://discordapp.com/api/guilds/125227483518861312/widget.png
[faq-shield]: https://img.shields.io/badge/Wiki-FAQ-blue.svg
[troubleshooting-shield]: https://img.shields.io/badge/Wiki-Troubleshooting-red.svg
[jenkins-shield]: https://img.shields.io/badge/Download-Jenkins-brightgreen.svg
[license-shield]: https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg
[migration-shield]: https://img.shields.io/badge/Wiki-Migrating%20from%20V3-green.svg
[ ![version][] ][download]
[ ![jenkins-shield][] ][jenkins]
[ ![license-shield][] ][license]
[ ![discord-shield][] ][discord-invite]
[ ![faq-shield] ][faq]
[ ![troubleshooting-shield] ][troubleshooting]
[ ![migration-shield][] ][migration]

<img align="right" src="https://github.com/DV8FromTheWorld/JDA/blob/assets/assets/readme/logo.png?raw=true" height="200" width="200">

# JDA (Java Discord API)

JDA strives to provide a clean and full wrapping of the Discord REST api and its Websocket-Events for Java.
This library is a helpful tool that provides the functionality to create a discord bot in java.

## Summary

Due to official statements made by the Discord developers we will no longer support unofficial features. These features
are undocumented API endpoints or protocols that are not available to bot-accounts. We will however continue support
for the usage of JDA through the client account type through `JDABuilder(AccountType.CLIENT)`. This does not mean
it is encouraged or recommended to create applications such as selfbots or custom clients which are prohibited by
the Discord Terms of Service.

_Please see the [Discord docs](https://discordapp.com/developers/docs/reference) for more information about bot accounts._

1. [Introduction](#creating-the-jda-object)
2. [Sharding](#sharding-a-bot)
3. [Entity Lifetimes](#entity-lifetimes)
4. [Download](#download)
5. [Documentation](#documentation)
6. [Support](#getting-help)
7. [Extensions And Plugins](#third-party-recommendations)
8. [Contributing](#contributing-to-jda)
9. [Dependencies](#dependencies)
10. [Other Libraries](#related-projects)

## UserBots and SelfBots

Discord is currently prohibiting creation and usage of automated client accounts (AccountType.CLIENT).
We however still have support to login with these accounts due to legacy support. That does not mean it is allowed or
welcome to use.
Note that JDA is not a good tool to build a custom discord client as it loads all servers/guilds on startup unlike
a client which does this via lazy loading instead.
If you need a bot, use a bot account from the [Application Dashboard](https://discordapp.com/developers/applications).

[Read More](https://support.discordapp.com/hc/en-us/articles/115002192352-Automated-user-accounts-self-bots-)

## Creating the JDA Object

Creating the JDA Object is done via the JDABuilder class. After setting the token and other options via setters,
the JDA Object is then created by calling the `build()` method. When `build()` returns,
JDA might not have finished starting up. However, you can use `awaitReady()`
on the JDA object to ensure that the entire cache is loaded before proceeding.
Note that this method is blocking and will cause the thread to sleep until startup has completed.

**Example**:

```java
JDA jda = new JDABuilder("token").build();
```

**Note**: By default this will use the `AccountType.BOT` as that is the recommended type of account.
You can change this to use `AccountType.CLIENT`, however you will be risking account termination.
Use `new JDABuilder(AccountType)` to change to a different account type.

### Configuration

Both the `JDABuilder` and the `DefaultShardManagerBuilder` allow a set of configurations to improve the experience.

**Example**:

```java
public static void main(String[] args) {
    JDABuilder builder = new JDABuilder(args[0]);
    
    // Disable parts of the cache
    builder.setDisabledCacheFlags(EnumSet.of(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE));
    // Enable the bulk delete event
    builder.setBulkDeleteSplittingEnabled(false);
    // Disable compression (not recommended)
    builder.setCompression(Compression.NONE);
    // Set activity (like "playing Something")
    builder.setActivity(Activity.watching("TV"));
    
    builder.build();
}
```

> See [JDABuilder](https://ci.dv8tion.net/job/JDA/javadoc/net/dv8tion/jda/api/JDABuilder.html)
  and [DefaultShardManagerBuilder](https://ci.dv8tion.net/job/JDA/javadoc/net/dv8tion/jda/api/sharding/DefaultShardManagerBuilder.html)

### Listening to Events

The event system in JDA is configured through a hierarchy of classes/interfaces.
We offer two implementations for the `IEventManager`:

- **InterfacedEventManager** which uses an `EventListener` interface and the `ListenerAdapter` abstract class
- **AnnotatedEventManager** which uses the `@SubscribeEvent` annotation that can be applied to methods

By default the **InterfacedEventManager** is used.
Since you can create your own implementation of `IEventManager` this is a very versatile and configurable system.
If the aforementioned implementations don't suit your use-case you can simply create a custom implementation and
configure it on the `JDABuilder` with `setEventManager(...)`.

#### Examples:

**Using EventListener**:

```java
public class ReadyListener implements EventListener
{
    public static void main(String[] args)
            throws LoginException, InterruptedException
    {
        // Note: It is important to register your ReadyListener before building
        JDA jda = new JDABuilder("token")
            .addEventListener(new ReadyListener())
            .build();

        // optionally block until JDA is ready
        jda.awaitReady();
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
            throws LoginException
    {
        JDA jda = new JDABuilder("token").build();
        //You can also add event listeners to the already built JDA instance
        // Note that some events may not be received if the listener is added after calling build()
        // This includes events such as the ReadyEvent
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

**Ping-Pong Bot**:

```java
public class Bot extends ListenerAdapter
{
    public static void main(String[] args) throws LoginException
    {
        new JDABuilder(args[0])
            .addEventListener(new Bot())
            .setActivity(Activity.playing("Type !ping"))
            .build();
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        Message msg = event.getMessage();
        if (msg.getContentRaw().equals("!ping"))
        {
            MessageChannel channel = event.getChannel();
            long time = System.currentTimeMillis();
            channel.sendMessage("Pong!") /* => RestAction<Message> */
                   .queue(response /* => Message */ -> {
                       response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue();
                   });
        }
    }
}
```

### RestAction

Through [RestAction](https://ci.dv8tion.net/job/JDA/javadoc/net/dv8tion/jda/api/requests/RestAction.html) we provide request handling with
 
 - [callbacks](https://ci.dv8tion.net/job/JDA/javadoc/net/dv8tion/jda/api/requests/RestAction.html#queue%28java.util.function.Consumer%29)
 - [promises](https://ci.dv8tion.net/job/JDA/javadoc/net/dv8tion/jda/api/requests/RestAction.html#submit%28%29)
 - and [sync](https://ci.dv8tion.net/job/JDA/javadoc/net/dv8tion/jda/api/requests/RestAction.html#complete%28%29)

and it is up to the user to decide which pattern to utilize.
It can be combined with reactive libraries such as [reactor-core](https://github.com/reactor/reactor-core) due to being lazy.

### More Examples

We provide a small set of Examples in the [Example Directory](https://github.com/DV8FromTheWorld/JDA/tree/master/src/examples/java).

<!--
TODO: Find good examples
- [JDA Butler](https://github.com/Almighty-Alpaca/JDA-Butler)

[And many more!](https://github.com/search?q=JDA+discord+bot&type=Repositories&utf8=%E2%9C%93)
-->

## Sharding a Bot

Discord allows Bot-accounts to share load across sessions by limiting them to a fraction of the total connected Guilds/Servers of the bot.
<br>This can be done using **sharding** which will limit JDA to only a certain amount of Guilds/Servers including events and entities.
Sharding will limit the amount of Guilds/Channels/Users visible to the JDA session so it is recommended to have some kind of elevated management to
access information of other shards.

To use sharding in JDA you will need to use `JDABuilder.useSharding(int shardId, int shardTotal)`. The **shardId** is 0-based which means the first shard
has the ID 0. The **shardTotal** is the total amount of shards (not 0-based) which can be seen similar to the length of an array, the last shard has the ID of
`shardTotal - 1`.

The [`SessionController`](https://ci.dv8tion.net/job/JDA/javadoc/net/dv8tion/jda/api/utils/SessionController.html) is a tool of the JDABuilder
that allows to control state and behaviour between shards (sessions). When using multiple builders to build shards you have to create one instance
of this controller and add the same instance to each builder: `builder.setSessionController(controller)`

Since version **3.4.0** JDA provides a `ShardManager` which automates this building process.

### Example Sharding - Using JDABuilder

```java
public static void main(String[] args) throws Exception
{
    JDABuilder shardBuilder = new JDABuilder(args[0]);
    //register your listeners here using shardBuilder.addEventListener(...)
    shardBuilder.addEventListener(new MessageListener());
    for (int i = 0; i < 10; i++)
    {
        shardBuilder.useSharding(i, 10)
                    .build();
    }
}
```

> When the `useSharding` method is invoked for the first time, the builder automatically sets a SessionController internally (if none is present)

### Example Sharding - Using DefaultShardManager
```java
public static void main(String[] args) throws Exception
{
    DefaultShardManagerBuilder builder = new DefaultShardManagerBuilder();
    builder.setToken(args[0]);
    builder.addEventListener(new MessageListener());
    builder.build();
}
```

## Entity Lifetimes

An **Entity** is the term used to describe types such as **GuildChannel**/**Message**/**User** and other entities
that Discord provides.
Instances of these entities are created and deleted by JDA when Discord instructs it. This means
the lifetime depends on signals provided by the Discord API which are used to create/update/delete entities.
This is done through Gateway Events known as "dispatches" that are handled by the JDA WebSocket handlers.
When Discord instructs JDA to delete entities they are simply removed from the JDA cache and lose their references.
Once that happens nothing in JDA interacts or updates the instances of those entities and they become useless. Discord
may instruct to delete these entities randomly for cache synchronization with the API.

**It is not recommended to store _any_ of these entities for a longer period of time!**
Instead of keeping (e.g.) a `User` instance in some field an ID should be used. With the ID of a user
you can use `getUserById(id)` to get and keep the user reference in a local variable. (see below)

### Fake Entities

Some entities in JDA are marked through an interface called `IFakeable`. These entities can exist outside
of the JDA cache and are inaccessible through the common `get...ById(id)` methods.
Fake entities are essentially instances that are not directly referenced by the JDA cache and are only
temporarily created for a specific usage. It may be used for the author of a message that has left the guild
when requesting the history of a `MessageChannel` or for `Emote` instances used in a `Message` that are not part
of any of the guilds available to the bot.

### Entity Updates

When an entity is updated through its manager they will send a request to the Discord API which will update the state
of the entity. The success of this request **does not** imply the entity has been updated yet. All entities are updated
by the aforementioned **Gateway Events** which means you cannot rely on the cache being updated yet once the
execution of a RestAction has completed. Some requests rely on the cache being updated to correctly update the entity.
An example of this is updating roles of a member which overrides all roles of the member by sending a list of the
new set of roles. This is done by first checking the current cache, the roles the member has right now, and appending
or removing the requested roles. If the cache has not yet been updated by an event this will result in unexpected behavior.

### Entity Deletion

Discord may request that a client (the JDA session) invalidates its entire cache. When this happens JDA will
remove all of its current entities and reconnect the session. This is signaled through the `ReconnectEvent`.
When entities are removed from the JDA cache they lose access to the encapsulating entities. For instance
a channel loses access to its guild. Once that happens they are unable to make any API requests through RestAction
and instead throw an `IllegalStateException`. It is **highly recommended** to only keep references to entities
by storing their **id** and using the respective `get...ById(id)` method when needed.

#### Example

```java
public class UserLogger extends ListenerAdapter 
{
    private final long userId;
    
    public UserLogger(User user)
    {
        this.userId = user.getIdLong();
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        User author = event.getAuthor();
        Message message = event.getMessage();
        if (author.getIdLong() == userId)
        {
            // Print the message of the user
            System.out.println(author.getAsTag() + ": " + message.getContentDisplay());
        }
    }
    
    @Override
    public void onGuildJoin(GuildJoinEvent event)
    {
        JDA api = event.getJDA();
        User user = api.getUserById(userId); // Acquire a reference to the User instance through the id
        user.openPrivateChannel().queue((channel) ->
        {
            // Send a private message to the user
            channel.sendMessageFormat("I have joined a new guild: **%s**", event.getGuild().getName()).queue();
        });
    }
}
```

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
    <url>https://jcenter.bintray.com</url>
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
            <groupId>club.minnced</groupId>
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

Docs can be found on the [Jenkins][jenkins] or directly [here](https://ci.dv8tion.net/job/JDA/javadoc/)
<br>A simple Wiki can also be found in this repository's [Wiki section](https://github.com/DV8FromTheWorld/JDA/wiki)

### Annotations

We use a number of annotations to indicate future plans for implemented functionality such as new features of
the Discord API.

- [Incubating](https://github.com/DV8FromTheWorld/JDA/blob/development/src/main/java/net/dv8tion/jda/annotations/Incubating.java)
    <br>This annotation is used to indicate that functionality may change in the future. Often used when a new feature is added.
- [ReplaceWith](https://github.com/DV8FromTheWorld/JDA/blob/development/src/main/java/net/dv8tion/jda/annotations/ReplaceWith.java)
    <br>Paired with `@Deprecated` this is used to inform you how the new code-fragment is supposed to look once the hereby annotated functionality is removed.
- [ForRemoval](https://github.com/DV8FromTheWorld/JDA/blob/development/src/main/java/net/dv8tion/jda/annotations/ForRemoval.java)
    <br>Paired with `@Deprecated` this indicates that we plan to entirely remove the hereby annotated functionality in the future.
- [DeprecatedSince](https://github.com/DV8FromTheWorld/JDA/blob/development/src/main/java/net/dv8tion/jda/annotations/DeprecatedSince.java)
    <br>Paired with `@Deprecated` this specifies when a feature was marked as deprecated.

[Sources](https://github.com/DV8FromTheWorld/JDA/tree/development/src/main/java/net/dv8tion/jda/annotations)

## Getting Help

For general troubleshooting you can visit our wiki [Troubleshooting](https://github.com/DV8FromTheWorld/JDA/wiki/19\)-Troubleshooting) and [FAQ](https://github.com/DV8FromTheWorld/JDA/wiki/10\)-FAQ).
<br>If you need help, or just want to talk with the JDA or other Devs, you can join the [Official JDA Discord Guild][discord-invite].

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

### [Lavalink](https://github.com/frederikam/Lavalink)

Lavalink is a popular standalone audio sending node based on Lavaplayer. Lavalink was built with scalability in mind,
and allows streaming music via many servers. It supports most of Lavaplayer's features.

Lavalink is used by many large bots, as well as bot developers who can not use a Java library like Lavaplayer.
If you plan on serving music on a smaller scale with JDA it is often preferable to just use Lavaplayer directly
as it is easier.

[Lavalink-Client](https://github.com/FredBoat/Lavalink-Client) is the official Lavalink client for JDA.
Both Lavalink-Client and Lavalink are created and maintained by [Frederik Mikkelsen](https://github.com/Frederikam),
the creator of FredBoat.

### [JDA-Utilities](https://github.com/JDA-Applications/JDA-Utilities)

Created and maintained by [jagrosh](https://github.com/jagrosh).
<br>JDA-Utilities provides a Command-Extension and several utilities to make using JDA very simple.

Features include:
- Paginated Message using Reactions
- EventWaiter allowing to wait for a response and other events


<!--
TODO: Ensure this is compatible with version 4
### [JDAction](https://github.com/sedmelluq/jdaction)

Created and maintained by [sedmelluq](https://github.com/sedmelluq)
<br>JDAction is a [Gradle](https://gradle.org/) plugin which makes sure that the return values of all methods which return a RestAction are used.
Since it is a common mistake to forget to `.queue()`/`.complete()`/`.submit()` RestActions,
and it is often only discovered after noticing that something doesn't work,
this plugin will help catch those cases quickly as it will cause a build failure in such case.

More info about RestAction: [Wiki](https://github.com/DV8FromTheWorld/JDA/wiki/7\)-Using-RestAction)
-->

### [jda-nas](https://github.com/sedmelluq/jda-nas)

Created and maintained by [sedmelluq](https://github.com/sedmelluq)
<br>Provides a native implementation for the JDA Audio Send-System to avoid GC pauses.

Note that this send system creates an extra UDP-Client which causes audio receive to no longer function properly
since discord identifies the sending UDP-Client as the receiver.

```java
JDABuilder builder = new JDABuilder(BOT_TOKEN)
    .setAudioSendFactory(new NativeAudioSendFactory());
```

### [jda-reactor](https://github.com/MinnDevelopment/jda-reactor)

Created and maintained by [MinnDevelopment](https://github.com/MinnDevelopment).
<br>Provides [Kotlin](https://kotlinlang.org/) extensions for **RestAction** and events that provide a [reactive](http://reactivex.io/intro.html) alternative to common JDA interfaces.

```kotlin
fun main() {
    val manager = ReactiveEventManager()
    manager.on<ReadyEvent>()
           .subscribe { println("Ready to serve!") }
    manager.on<MessageReceivedEvent>()
           .filter { it.message.contentRaw == "!ping" }
           .subscribe { it.channel.sendMessage("Pong!").queue() }

    val jda = JDABuilder(BOT_TOKEN)
               .setEventManager(manager)
               .build()
}
```

An example bot for this can be found at [Reactive JDA Bot](https://github.com/MinnDevelopment/reactive-jda-bot).

------

More can be found in our github organization: [JDA-Applications](https://github.com/JDA-Applications)

## Contributing to JDA
If you want to contribute to JDA, make sure to base your branch off of our **v4** branch (or a feature-branch)
and create your PR into that **same** branch. **We will be rejecting any PRs between branches or into release branches!**

It is also highly recommended to get in touch with the Developers before opening Pull Requests (either through an issue or the Discord servers mentioned above).<br>
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

This project requires **Java 8+**.<br>
All dependencies are managed automatically by Gradle.
 * NV Websocket Client
   * Version: **2.5**
   * [Github](https://github.com/TakahikoKawasaki/nv-websocket-client)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/com.neovisionaries%3Anv-websocket-client/view)
 * OkHttp
   * Version: **3.13.0**
   * [Github](https://github.com/square/okhttp)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/com.squareup.okhttp3:okhttp)
 * Apache Commons Collections4
   * Version: **4.1**
   * [Website](https://commons.apache.org/proper/commons-collections)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/org.apache.commons%3Acommons-collections4/view)
 * jackson
   * Version: **2.9.8**
   * [Github](https://github.com/FasterXML/jackson)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/com.fasterxml.jackson.core%3Ajackson-databind/view)
 * Trove4j
   * Version: **3.0.3**
   * [BitBucket](https://bitbucket.org/trove4j/trove)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/net.sf.trove4j%3Atrove4j/view)
 * slf4j-api
   * Version: **1.7.25**
   * [Website](https://www.slf4j.org/)
   * [JCenter Repository](https://bintray.com/bintray/jcenter/org.slf4j%3Aslf4j-api/view)
 * opus-java (optional)
   * Version: **1.0.4**
   * [GitHub](https://github.com/discord-java/opus-java)
   * [JCenter Repository](https://bintray.com/minndevelopment/maven/opus-java)

## Related Projects

- [Discord4J](https://github.com/Discord4J/Discord4J)
- [Discord.NET](https://github.com/discord-net/Discord.Net)
- [discord.py](https://github.com/Rapptz/discord.py)
- [serenity](https://github.com/serenity-rs/serenity)

**See also:** https://discordapp.com/developers/docs/topics/community-resources#libraries
