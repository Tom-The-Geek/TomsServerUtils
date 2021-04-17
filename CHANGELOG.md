<a name="unreleased"></a>
## [Unreleased]



<a name="v0.6.0"></a>
## [v0.6.0] - 2021-04-17

- :bookmark: v0.6.0
- Merge pull request [#16](https://github.com/Geek202/TomsServerUtils/issues/16) from Geek202/docs/cleanup
- Merge pull request [#17](https://github.com/Geek202/TomsServerUtils/issues/17) from Geek202/rewrite/kord
- :construction_worker: Only run GHA when code changes on pushes
- :memo: Changelog generation
- ✨ Re-introduce minecraft commands to discord
- 👷 Refactor buildscript to shadow dependencies correctly
- 🐛 Shutdown kord to prevent server hang
- concern
- 🏗️ Start work refactoring to use kord rather than JDA, lots of stuff will be Broken™


<a name="v0.5.0"></a>
## [v0.5.0] - 2021-04-11

- 📝 This was long overdue (add README)
- ⬆️ Seems like this was breaking CI?
- ✨ Discord commands! Allows admins to use in-game commands from Discord
- Merge branch 'main' of https://github.com/Geek202/TomsServerUtils into main
- ⬆️ Update loom, CCA, webhooks, JDA and konf. Fingers crossed nothing breaks 🤞
- 💚 Turns out it didn't work


<a name="v0.4.1"></a>
## [v0.4.1] - 2021-04-05

- ✨ Optional bot presence support (WIP)
- 👷 Cleanup
- 👷 Better release system if this works :)
- 🔥 Remove Jenkinsfile, should've gotten rid of that a while ago too
- 👽 Minotar has problems, revert back to crafatar
- 🏗 Rewrite just about everything that wasn't already into kotlin
- 🔨 Cleanup buildscript


<a name="v0.4.0"></a>
## [v0.4.0] - 2021-04-04

- 🔖 v0.4.0 (yes I am now going to actually update the version in gradle.properties lol)
- ♻️ Send chat messages on the server thread when received from Discord
- 🔥 Should've poofed this a while ago
- 🐛 Maybe fix a race condition idrk whats going on at this point
- 🚨 Warnings begone!
- ⬆️ Bump fapi+flk versions
- Merge pull request [#14](https://github.com/Geek202/TomsServerUtils/issues/14) from Setadokalo/fix-dependencies
- Added dependencies to mod.json
- Merge pull request [#12](https://github.com/Geek202/TomsServerUtils/issues/12) from Setadokalo/feature-seperate-event
- Removed change from seperate feature
- Seperate chat and events to different webhooks


<a name="v0.3.1"></a>
## [v0.3.1] - 2021-02-26

- Fix ~5min hang on server exit ([#8](https://github.com/Geek202/TomsServerUtils/issues/8))
- Merge pull request [#11](https://github.com/Geek202/TomsServerUtils/issues/11) from profjb58/patch-1
- Remove Fabric kotlin api from the fat jar


<a name="v0.3.0"></a>
## [v0.3.0] - 2021-02-24

- update FLK and hope for the best
- fix tabs/spaces in gradle files
- chat relay now supports advancements!
- add (not very good) icon
- 1.16.5 is a thing lol


<a name="v0.2.4"></a>
## [v0.2.4] - 2021-02-21

- Merge pull request [#9](https://github.com/Geek202/TomsServerUtils/issues/9) from kb-1000/downgrade-log4j-slf4j
- Downgrade Log4j SLF4J bridge to match Minecraft's log4j version


<a name="v0.2.3"></a>
## [v0.2.3] - 2021-02-21

- Limit allowed mentions via config. (closes [#7](https://github.com/Geek202/TomsServerUtils/issues/7))
- JIJ slf4j rather than shading to cleanup log output (fixes [#6](https://github.com/Geek202/TomsServerUtils/issues/6))
- try not to crash when the webhook url for the chat link is not set. (should fix [#5](https://github.com/Geek202/TomsServerUtils/issues/5))
- create scripts dir if it doesn't exist (fixes [#4](https://github.com/Geek202/TomsServerUtils/issues/4))


<a name="v0.2.2"></a>
## [v0.2.2] - 2021-02-18

- bump server-translations-api
- fix click to copy
- Improve discord rendering!


<a name="v0.2.1"></a>
## [v0.2.1] - 2021-01-24

- whats translatable now? all the things are!
- remove unused icon file (this is on a server, there's no icons lol)
- allow home count limit to be per-dimension or global across all


<a name="v0.2.0"></a>
## [v0.2.0] - 2021-01-24

- upload asset on release
- change CI to cache things. should speed up builds
- Merge pull request [#3](https://github.com/Geek202/TomsServerUtils/issues/3) from Geek202/feature/death-messages
- working death messages!
- Merge pull request [#2](https://github.com/Geek202/TomsServerUtils/issues/2) from Geek202/feature/broadcast
- change default avatar service for Discord link
- start work on broadcast command, not tested yet tho.
- start work on death messages.


<a name="v0.1.3"></a>
## [v0.1.3] - 2021-01-07

- cap length of crash report message to 1500 characters to avoid hitting Discord's max 2000 char message limit.


<a name="v0.1.2"></a>
## [v0.1.2] - 2021-01-01

- v0.1.2


<a name="0.1.2"></a>
## [0.1.2] - 2021-01-01

- fix version number


<a name="v0.1.1"></a>
## v0.1.1 - 2021-01-01

- v0.1.1
- Use GH actions instead of Jenkins
- Merge pull request [#1](https://github.com/Geek202/TomsServerUtils/issues/1) from Geek202/feature/chat_filter
- JavaScript based chat filtering!
- Update Jenkinsfile
- Update serverutils_default.toml
- start work on a chat filter module, currently only has a check against a list of bad words from web-mech/badwords
- fix homes being lost upon death
- Prevent crash on dedicated server
- Generate a changelog and use it for the changelog on Modrinth
- Add a /home command using CardinalComponentsApi for storing data on players.
- whoops, big oopsie!
- try with different token for modrinth
- Merge remote-tracking branch 'origin/master'
- Add new module that reports crashes to a discord channel using a webhook.
- publish to Modrinth automatically with Jenkins
- Add .deepsource.toml
- fix all the things!
- oops
- append build number to version
- Merge remote-tracking branch 'origin/master'
- update to 1.16.4, append build number to version
- Create Jenkinsfile
- initial commit


[Unreleased]: https://github.com/Geek202/TomsServerUtils/compare/v0.6.0...HEAD
[v0.6.0]: https://github.com/Geek202/TomsServerUtils/compare/v0.5.0...v0.6.0
[v0.5.0]: https://github.com/Geek202/TomsServerUtils/compare/v0.4.1...v0.5.0
[v0.4.1]: https://github.com/Geek202/TomsServerUtils/compare/v0.4.0...v0.4.1
[v0.4.0]: https://github.com/Geek202/TomsServerUtils/compare/v0.3.1...v0.4.0
[v0.3.1]: https://github.com/Geek202/TomsServerUtils/compare/v0.3.0...v0.3.1
[v0.3.0]: https://github.com/Geek202/TomsServerUtils/compare/v0.2.4...v0.3.0
[v0.2.4]: https://github.com/Geek202/TomsServerUtils/compare/v0.2.3...v0.2.4
[v0.2.3]: https://github.com/Geek202/TomsServerUtils/compare/v0.2.2...v0.2.3
[v0.2.2]: https://github.com/Geek202/TomsServerUtils/compare/v0.2.1...v0.2.2
[v0.2.1]: https://github.com/Geek202/TomsServerUtils/compare/v0.2.0...v0.2.1
[v0.2.0]: https://github.com/Geek202/TomsServerUtils/compare/v0.1.3...v0.2.0
[v0.1.3]: https://github.com/Geek202/TomsServerUtils/compare/v0.1.2...v0.1.3
[v0.1.2]: https://github.com/Geek202/TomsServerUtils/compare/0.1.2...v0.1.2
[0.1.2]: https://github.com/Geek202/TomsServerUtils/compare/v0.1.1...0.1.2
