package dev.majek.pc.data.struct.user;

import cloud.commandframework.jda.JDACommandSender;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

public class DiscordSender implements Sender {

  private final JDACommandSender sender;

  public DiscordSender(final @NotNull JDACommandSender sender) {
    this.sender = sender;
  }

  @Override
  public @NotNull String username() {
    return this.sender.getUser().getName();
  }

  @Override
  @SuppressWarnings("ConstantConditions")
  public boolean permission(@NotNull String permission) {
    if (permission.contains("staff")) {
      try {
        return sender.getUser().getJDA().getGuildById("id from config").getMember(sender.getUser())
            .hasPermission(Permission.ADMINISTRATOR);
      } catch (NullPointerException ignored) {
        return false;
      }
    } else {
      return true;
    }
  }

  public JDACommandSender jdaSender() {
    return this.sender;
  }
}
