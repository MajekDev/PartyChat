# Plugin Changelog

# 4.2.2 - New Config Options

- `disband-on-leader-leave` - if the leader leaves the party and `persistent-parties` is set to false, the party will disband.
- `max-party-size` - set a cap on the number of players that can be in a party.
- `player-party-names` - if no name is provided on `/party create [name]`, then the creating player's username will be used as the party name.

This update includes the above new configuration options as well as a handful of random bug fixes.

# 4.2.1 - Bug Fixes

This update includes minor bug fixes and dependency bumps. It will be the last update before v5 comes out in a few months.

# 4.2.0 - Bye Bye AnvilGUI

- Drop support for AnvilGUI in favor of typing the input in chat.
- Add GitHub workflows for build and publish.
- Add issue templates.
- Fix bug with `/party kick`.
- Add JavaDocs to main branch in /docs.
- Move to new (better) ConfigUpdater via Maven.

AnvilGUI was a really cool thing, it just introduced too many bugs with all the different versions of Minecraft 
we're supporting. It may be added back at a later date but as of right now it's not worth all the bugs it caused.