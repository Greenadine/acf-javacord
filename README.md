
# ACF Javacord ![](https://img.shields.io/badge/version-v0.2-blue?style=flat-square) [![](https://img.shields.io/badge/acf-v0.5.0-blue?style=flat-square)](https://github.com/aikar/commands) [![](https://img.shields.io/badge/javacord-v3.3.0-blue?style=flat-square)](https://github.com/Javacord/Javacord) ![](https://img.shields.io/github/license/Greenadine/acf-javacord?style=flat-square)
A Javacord implementation of Aikar's [Annotation Command Framework (ACF)](https://github.com/aikar/commands).

ACF-Javacord allows the usage of the powerful command framework ACF for [Javacord](https://github.com/Javacord/Javacord)-based Discord bots.

_NOTE: This implementation of ACF is not official, and the core of ACF has been marked as not stable enough for new implementations. Use this implementation at your own risk._

## Installation
Add the .jar file to a new `/libs` folder (or a different one) within the project.
### Maven
```xml
<repositories>
    <repository>
        <id>aikar</id>
        <url>https://repo.aikar.co/content/groups/aikar/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>co.aikar</groupId>
        <artifactId>acf-core</artifactId>
        <version>0.5.0</version>
    </dependency>
    <dependency>
        <groupId>nl.greenadine</groupId>
        <artifactId>acf-javacord</artifactId>
        <version>0.2</version>
        <scope>system</scope>
        <systemPath>${project.basedir}/libs/javacord-0.1.1.jar</systemPath>
    </dependency>
</dependencies>
````
### Gradle
````gradle
repositories {
    flatDir {
        dirs 'libs'
    }
    
    maven { url = "https://repo.aikar.co/content/groups/aikar/" }
}

dependencies {
    implementation 'co.aikar:acf-core:0.5.0-SNAPSHOT'
    implementation files('libs/acf-javacord-0.2.jar')
}
````

## Documentation
[ACF Javacord wiki](https://github.com/Greenadine/acf-javacord/wiki) - ACF Javacord-specific documentation.

For ACF documentation please consult the [ACF wiki](https://github.com/aikar/commands/wiki).

## Example
```java
@CommandAlias("ping")
@Description("Check API latency.")
public class PingCommand extends BaseCommand {

    @Default
    public void onPing(JavacordCommandEvent event) {
        event.reply("Testing latency...").thenAcceptAsync(message -> {
            double messageTimestamp = message.getCreationTimestamp().toEpochMilli();
            double currentTimestamp = System.currentTimeMillis();
            double ping = Math.abs(Math.round((currentTimestamp - messageTimestamp) / 100));

            message.edit(String.format("My API latency is %.0fms.", ping));
        });
    }
}
```
