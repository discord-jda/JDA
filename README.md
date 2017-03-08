# JDA+K (JDA)
k-JDA is an superset of JDA, the Java API for Discord. It's not much different from JDA right now except for a few utility extensions added to the original library.

### Changes
* `JDA.kt`
    * General extensions to aid development in Kotlin.
* `ResponseAction.kt`
    * Utility methods that respond in the designated channel set during construction.
* `EmbedBuilder.kt`
    * Migrated `EmbedBuilder.java` to Kotlin.
    * Renamed methods.
    * Added methods to suit Kotlin development.
* `Message` / `MessageImpl`
    * `#respond()` extension that allows for access of utility methods that respond in the message's channel.
* `Member` /` MemberImpl` now implements `User`.
    * `User` methods are delegated to the `Member`'s user.
    * `#hasRoleNamed(String name)` extension to find if member has a role named `name`.