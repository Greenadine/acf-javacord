
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
<dependency>
    <groupId>com.github.greenadine</groupId>
    <artifactId>acf-javacord</artifactId>
    <version>0.1</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/javacord-0.1.jar</systemPath>
</dependency>
````
### Gradle
````gradle
repositories {
    flatDir {
        dirs 'libs'
    }
}

dependencies {
    implementation files('libs/acf-javacord-0.1.jar')
}
````
