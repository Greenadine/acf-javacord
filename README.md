
# ACF Javacord
![](https://img.shields.io/github/v/release/Greenadine/acf-javacord)
![](https://img.shields.io/badge/javacord-v3.1.2-blue)
![](https://img.shields.io/github/license/Greenadine/acf-javacord)
<br>
ACF Javacord is a Javacord implementation of [aikar's](https://github.com/aikar) [Annotation Command Framework (ACF)](https://github.com/aikar/commands).

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
        <groupId>com.github.greenadine</groupId>
        <artifactId>acf-javacord</artifactId>
        <version>0.1</version>
        <scope>system</scope>
        <systemPath>${project.basedir}/libs/javacord-0.1.jar</systemPath>
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
    implementation files('libs/acf-javacord-0.1.jar')
}
````
