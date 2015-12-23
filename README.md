# JDA
JDA strives to provide a clean and full wrapping of the Discord REST api for the Java language.
<p>
Not much here yet for explinations, but there are examples of listening to events: [Link](https://github.com/DV8FromTheWorld/JDA/tree/master/src/examples/java).

## Events
There a TON of events in JDA that you can listen to.<br>
You have 2 choices:
  1. Extend ListenerAdapter and use the provided methods that fire the specific events for you. [Event Methods](https://github.com/DV8FromTheWorld/JDA/blob/master/src/main/java/net/dv8tion/jda/hooks/ListenerAdapter.java#L179-L254)
  2. Implement EventListener and listen to onEvent and figure out if it is the event you want (Not suggested)<br>
  
Personally I would suggest using the ListenerAdapter.

### Dependencies:
All dependencies are managed automatically by Gradle.
 * Java-Websocket
   * Version: **1.3.0**
   * [Github](https://github.com/TooTallNate/Java-WebSocket)
   * [Central Repository](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.java-websocket%22%20AND%20a%3A%22Java-WebSocket%22)
 * Apache Commons Lang3
   * Version: **3.4**
   * [Website](https://commons.apache.org/proper/commons-lang/)
   * [Central Repository](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.apache.commons%22%20AND%20a%3A%22commons-lang3%22)
 * json.org
   * Version: **20150729**
   * [Github](https://github.com/douglascrockford/JSON-java)
   * [Central Repository](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.json%22%20AND%20a%3A%22json%22)
