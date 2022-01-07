package dev.majek.pc.data.struct.user;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface MinecraftSender extends Sender, Identified, Identity, ForwardingAudience.Single {

  @NotNull UUID uuid();

  @NotNull CommandSender commandSender();

  @NotNull Component displayName();

  void sendMessage(@NotNull Component message);

  @Override
  @NotNull Audience audience();

  @Override
  default @NotNull Identity identity() {
    return this;
  }
}
