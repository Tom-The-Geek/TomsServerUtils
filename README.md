# TomsServerUtils
[![downloads (github)](https://img.shields.io/github/downloads/Geek202/TomsServerUtils/total?label=Downloads&logo=github)](https://github.com/Geek202/TomsServerUtils/releases) [![downloads (modrinth)](https://waffle.coffee/modrinth/toms-server-utils/downloads)](https://modrinth.com/mod/toms-server-utils) ![version](https://img.shields.io/github/v/release/Geek202/TomsServerUtils?label=Version) [![discord](https://img.shields.io/discord/813009649044946964?label=Discord)](https://discord.gg/tCAWpDsBmh) [![build status](https://img.shields.io/github/workflow/status/Geek202/TomsServerUtils/Java%20CI%20with%20Gradle)](https://github.com/Geek202/TomsServerUtils/actions/workflows/check.yml) [![issues](https://img.shields.io/github/issues/Geek202/TomsServerUtils)](https://github.com/Geek202/TomsServerUtils/issues)

TomsServerUtils is a mod for Minecraft that includes several features useful for running a Minecraft Server using [Fabric](https://fabricmc.net).

## Current features:
- Discord <-> Minecraft chat link
- Automatic Discord crash reporter
- Homes system
- Broadcast command
- Anything else I feel like adding in the future

## License
See the [LICENSE file](https://github.com/Geek202/TomsServerUtils/blob/main/LICENSE)

## Contributing
Most of this mod is written in [Kotlin](https://kotlinlang.org) with only a few components written in Java. Please try to write any contributions in Kotlin, the only exception for this is if you are adding a new mixin, as Kotlin support is Not Very Good™. Some other things to note:
- If adding to the config, ensure to add a value [in the defaults](https://github.com/Geek202/TomsServerUtils/blob/main/src/main/resources/serverutils_default.toml)
- If adding literal strings displayed to players, please make use of the [translations file](https://github.com/Geek202/TomsServerUtils/blob/main/src/main/resources/data/toms-server-utils/lang/en_us.json) to ensure that they can be translated if wanted.
- Try not to break anything :)
- If Intellij complains about the command line being too long, move the project folder to be in less subdirectories, to make the path shorter.