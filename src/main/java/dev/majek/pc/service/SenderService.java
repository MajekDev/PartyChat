package dev.majek.pc.service;

import cloud.commandframework.jda.JDACommandSender;
import dev.majek.pc.data.struct.user.ConsoleSender;
import dev.majek.pc.data.struct.user.DiscordSender;
import dev.majek.pc.data.struct.user.PlayerSender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SenderService {

  private static SenderService service;

  private SenderService() {
    service = this;
  }

  public static SenderService sender() {
    if (service == null) {
      service = new SenderService();
    }
    return service;
  }

  public @NotNull PlayerSender player(final @NotNull Player player) {
    return new PlayerSender(player, player);
  }

  public @NotNull ConsoleSender console() {
    return new ConsoleSender(Bukkit.getConsoleSender());
  }

  public @NotNull DiscordSender discord(final @NotNull JDACommandSender jdaCommandSender) {
    return new DiscordSender(jdaCommandSender);
  }
}
