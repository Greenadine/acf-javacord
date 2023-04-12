
# ACF Javacord [![](https://img.shields.io/badge/version-v0.4.0-blue?style=flat-square)](https://javadocs.greenadine.dev/acf-javacord/0.4.0/) [![](https://img.shields.io/badge/acf-v0.5.1--SNAPSHOT-blue?style=flat-square)](https://github.com/aikar/commands) [![](https://img.shields.io/badge/javacord-v3.7.0-blue?style=flat-square)](https://github.com/Javacord/Javacord) ![](https://img.shields.io/github/license/Greenadine/acf-javacord?style=flat-square)
A Javacord implementation of Aikar's [Annotation Command Framework (ACF)](https://github.com/aikar/commands).

ACF-Javacord allows the usage of the powerful command framework ACF for [Javacord](https://github.com/Javacord/Javacord)-based Discord bots.

### DISCLAIMER
This implementation of ACF is not official, and the core of ACF has been marked as not stable enough for new implementations, however it has worked flawlessly for me thus far. Use this implementation at your own risk.

## Installation
For instructions on installing, see the [Beginner's Guide](https://github.com/Greenadine/acf-javacord/wiki/Beginner's-Guide).

## Documentation
* [Wiki](https://github.com/Greenadine/acf-javacord/wiki)
* [JavaDocs](https://javadocs.greenadine.dev/acf-javacord/0.4.0/)

For ACF core documentation please consult the [ACF core wiki](https://github.com/aikar/commands/wiki).

## Example command
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
