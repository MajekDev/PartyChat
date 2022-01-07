package dev.majek.pc.data.struct.user;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ConsoleSender implements MinecraftSender {

  private static final CommandSender SENDER = Bukkit.getConsoleSender();

  private final Audience audience;

  public ConsoleSender(final @NotNull Audience audience) {
    this.audience = audience;
  }

  @Override
  public @NotNull UUID uuid() {
    return new UUID(0, 0);
  }

  @Override
  public @NotNull CommandSender commandSender() {
    return SENDER;
  }

  @Override
  public @NotNull String username() {
    return "CONSOLE";
  }

  @Override
  public @NotNull Component displayName() {
    return Component.text("CONSOLE");
  }

  @Override
  public boolean permission(final @NotNull String permission) {
    return true;
  }

  @Override
  public void sendMessage(@NotNull Component message) {
    SENDER.sendMessage(message);
  }

  @Override
  public @NotNull Audience audience() {
    return this.audience;
  }
}
