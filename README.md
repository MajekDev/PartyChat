[version]: https://img.shields.io/github/v/release/Majekdor/PartyChat?color=b&label=Download
[download]: #installation
[discord-invite]: https://discord.gg/CGgvDUz
[discord-shield]: https://img.shields.io/discord/753727849860432076?color=%237289da
[license]: https://github.com/Majekdor/PartyChat/blob/main/LICENSE
[license-shield]: https://img.shields.io/github/license/Majekdor/PartyChat?color=%09%237f7f7f
[ ![version][] ][download]
[ ![discord-shield][] ][discord-invite]
[ ![license-shield][] ][license]
[![](https://jitpack.io/v/Majekdor/PartyChat.svg)](https://jitpack.io/#Majekdor/PartyChat)
[![](https://jitci.com/gh/Majekdor/PartyChat/svg)](https://jitci.com/gh/Majekdor/PartyChat)
[![](https://img.shields.io/spiget/rating/79295?color=%23ff781f&label=Spigot)](https://www.spigotmc.org/resources/partychat.79295/)

<img align="right" src="https://raw.githubusercontent.com/Majekdor/PartyChat/main/partychat.png" height="200" width="200">
<h1 align="center">PartyChat by Majekdor</h1>

PartyChat is a party manager plugin for Spigot Minecraft servers. It allows players to create private parties to chat and interact with their friends. The plugin is fully customizable, all messages can be changed and/or translated into any language. There are many configuration options including the ability to add custom aliases for party sub commands.

1. [Installation](#installation)
2. [Getting Started](#getting-started)
3. [Configuration](#configuration)
4. [Documentation](#documentation)
5. [API Usage](#api-usage)
6. [Support](#support)
7. [Contributing](#contributing)
8. [Donate](#donate)

## Installation

**Step 1: Download PartyChat**

You can download it from [Spigot](https://spigotmc.org/resources/partychat.79295/) or from the releases tab here on GitHub. 

**Step 2: Add plugin to server**

Place the downloaded jar file into your server plugins folder.

**Step 3: Restart server**

The server **must** be restarted in order for PartyChat to load properly.


Now you're ready to get started using the plugin!

## Getting Started

Check to make sure the plugin was installed properly by running either `/plugins` and making sure it contains PartyChat or by doing `/party version`.

The first thing you want to do with the plugin is probably create a party. To do this simply run `/party create <name>`, replacing `<name>` with the name you would like to give your party. This name can contain standard Minecraft color codes, such as `&b` or `&l`, along with hex colors codes in the format `&#rrggbb`.

Once the party is created you can invite another player to it by running `/party add <player>`, replacing `<player>` with the player you wish to invite. The player can then accept or decline your invitation with either `/party accept` or `/party deny`.

To send a message to your party you have two options. The first is to run `/partychat` (which also has a handy alias of `/pc`) which will send you a message saying it's enabled and allow you to type your message straight into chat. You can disable this to chat normally by running the same command again. The second option is to append your message to `/partychat`, for example `/partychat Hello party members!` would send "Hello party members!" to your party.

You can view full command documenation [here](https://github.com/Majekdor/PartyChat/wiki/commands).

## Configuration

### config.yml
The default config.yml file can be viewed [here](https://github.com/Majekdor/PartyChat/blob/main/src/main/resources/config.yml) and you should explore all the options it has. 

Some of the main config options you'll probably be using are:
 - `persistent-parties` This is set to false by default, meaning when a player leaves the server they are removed from their party and when the server restarts all parties are deleted. If this is set to true then players will remain in their party when they leave the server and parties will be saved on server restart.
 - `use-permissions` By default every player can use PartyChat commands. If you would only like certain players to be able to use the plugin, set this to true. When this is enabled only players with the permission node `partychat.use` will be able to use the plugin.
 - `block-inappropriate-names` PartyChat has the ability to block party creation if the name contains inappropriate words. It can pull inappropriate words from a file or from a list in the config.yml.

### Messages
Message files (every message PartyChat sends is pulled from these) are stored [here](https://github.com/Majekdor/PartyChat/tree/main/src/main/resources/Lang). Full documentation for how to change the language or add your own can be found on the [wiki](https://github.com/Majekdor/PartyChat/wiki).

By default PartyChat uses the en_US.yml config file for messages sent in chat. The plugin will also fall-back to this file if there is an issue loading another one. 

All messages in these files can be changed to your liking, including content and color codes. However, placeholders are meant for specific messages and should not be moved between or added to different messgaes they weren't originally in.

### commands.yml
This file is where information on every command, including description and usage, is stored and the default version can be found [here](https://github.com/Majekdor/PartyChat/blob/main/src/main/resources/commands.yml).

This is where you'll be able to customize cooldowns for party subcommands such as `/party summon`. All descriptions and usages displayed on `/party help` are pulled directly from this file.

## Documentation

If you can't find information on what you're looking for here, head on over to the [wiki](https://github.com/Majekdor/PartyChat/wiki). There you can find documentation on every command, permission node, and configuration option. You will also find tutorials on how to do things such as change the language, use `/partychat edit`, and much more.

JavaDocs for the current release can be found [here](https://pc-docs.majek.dev).

If you still can't find what you're looking for go ahead and join my [Discord](https://discord.gg/CGgvDUz) and I can try to help you and add documentation for your issue.

## API Usage

If you're reading this section you likely want to hook into PartyChat in some way from your plugin. That's great! Since version 4 that is a lot easier to do and offers way more options. (This section not finished)

## Support

If you need help with the plugin and can't find the answer here, on the [wiki](https://github.com/Majekdor/PartyChat/wiki), or on Spigot, then the best way to get help is to join my [Discord](https://discord.gg/CGgvDUz). Make sure you read the frequently-asked channel before posting in the bug-reports channel (if it's a bug) or in the party-chat channel (for general help). 

If you have discovered a bug you can either join my [Discord](https://discord.gg/CGgvDUz) and report it there or open an issue here on GitHub. Please do not message me on Spigot in regards to bugs, there are easier ways to communicate.

## Contributing

PartyChat is open-source and licensed under the [MIT License](https://github.com/Majekdor/PartyChat/blob/main/LICENSE), so if you want to use any code contained in the plugin or clone the repository and make some changes, go ahead!

If you've found a bug within the plugin and would like to just make the changes to fix it yourself, you're free to do so and make a pull request here on GitHub. If you make significant contributions to the project, and by significant I mean one little PR to fix a tiny bug doesn't count as significant, you can earn the Contributor role in my [Discord](https://discord.gg/CGgvDUz).

## Donate

I'm a full time college student who makes and supports these plugins in my free time (when I have any). As a long time supporter of open source, all of my plugins are free. If you enjoy my plugins and would like to support me, you can buy me coffee over on  [PayPal](https://paypal.com/paypalme/majekdor). Donations of any amount are appreciated and a donation of $10 or more will get you the Supporter role in my [Discord](https://discord.gg/CGgvDUz)!
