
# ACF Javacord ![](https://img.shields.io/badge/version-v0.2-blue?style=flat-square) [![](https://img.shields.io/badge/acf-v0.5.0-blue?style=flat-square)](https://github.com/aikar/commands) [![](https://img.shields.io/badge/javacord-v3.3.0-blue?style=flat-square)](https://github.com/Javacord/Javacord) ![](https://img.shields.io/github/license/Greenadine/acf-javacord?style=flat-square)
A Javacord implementation of Aikar's [Annotation Command Framework (ACF)](https://github.com/aikar/commands).

ACF-Javacord allows the usage of the powerful command framework ACF for [Javacord](https://github.com/Javacord/Javacord)-based Discord bots.

### DISCLAIMER
This implementation of ACF is not official, and the core of ACF has been marked as not stable enough for new implementations. Use this implementation at your own risk.

## Installation
### Option 1
The first, and also recommended option is [installing it to your local repository](https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html). After doing this, add the following depending on which build tool you use:

#### Maven (pom.xml)
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
        <version>[VERSION]</version> <!-- Replace '[VERSION]' with the version you want to use -->
    </dependency>
</dependencies>
```

#### Gradle (build.gradle)
```gradle
repositories {
    mavenLocal()
    maven { url = 'https://repo.aikar.co/content/groups/aikar/' }
}

dependencies {
    implementation 'co.aikar:acf-core:0.5.0-SNAPSHOT' // ACF core
    implementation 'nl.greenadine:acf-javacord:[VERSION]' // Replace '[VERSION]' with version you want to use
}
```

### Option 2
The second option is more of a "quick and dirty" solution, but it gets the job done. Follow the instructions listed below based on which build tool you use:

#### Maven (pom.xml)
_NOTE: Adding local files to your pom.xml is currently deprecated but still functional within Maven, therefore it is recommended you instead opt for option 1._

Add the following to your `pom.xml`:
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
        <version>[VERSION]</version> <!-- Replace '[VERSION]' with the version you added to the local folder -->
        <scope>system</scope>
        <systemPath>file://[PATH]y</systemPath> <!-- Replace '[PATH]' with the path to the  -->
    </dependency>
</dependencies>
```

#### Gradle
Add the .jar to a local folder on your computer wherever you prefer, and designate said folder as a local repository. Then, add the following to your `build.gradle`:
```gradle
repositories {
    flatDir {
        dirs 'libs'
    }
    
    maven { url = "https://repo.aikar.co/content/groups/aikar/" }
}

dependencies {
    implementation 'co.aikar:acf-core:0.5.0-SNAPSHOT' // ACF core
    implementation files('libs/acf-javacord-[VERSION].jar') // Replace '[VERSION]' with the version you want to use
}
```

## Documentation
[ACF Javacord wiki](https://github.com/Greenadine/acf-javacord/wiki) - ACF Javacord-specific documentation.

For ACF documentation please consult the [ACF wiki](https://github.com/aikar/commands/wiki).

## Example
````java
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
````
