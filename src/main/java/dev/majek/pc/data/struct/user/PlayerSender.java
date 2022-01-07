package dev.majek.pc.data.struct.user;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerSender implements MinecraftSender, Identified, Identity, ForwardingAudience.Single {

  private final Player player;
  private final Audience audience;

  public PlayerSender(final @NotNull Player player, final @NotNull Audience audience) {
    this.player = player;
    this.audience = audience;
  }

  @Override
  public @NotNull UUID uuid() {
    return this.player.getUniqueId();
  }

  @Override
  public @NotNull CommandSender commandSender() {
    return this.player;
  }

  @Override
  public @NotNull String username() {
    return this.player.getName();
  }

  @Override
  public @NotNull Component displayName() {
    return this.player.displayName();
  }

  @Override
  public boolean permission(final @NotNull String permission) {
    return this.player.hasPermission(permission);
  }

  @Override
  public void sendMessage(@NotNull Component message) {
    this.player.sendMessage(message);
  }

  @Override
  public @NotNull Audience audience() {
    return this.audience;
  }
}
