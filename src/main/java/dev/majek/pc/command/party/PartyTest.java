package dev.majek.pc.command.party;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import dev.majek.pc.PartyChat;
import dev.majek.pc.command.IPartyCommand;
import dev.majek.pc.data.struct.user.MinecraftSender;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class PartyTest implements IPartyCommand {

  @Override
  public @NotNull String name() {
    return "invite";
  }

  @Override
  public void register() {
    Set<String> aliases = Set.of("invite", "add");
    for (String alias : aliases) {
      PartyChat.commandManager().registerPartySubCommand(builder ->
          builder.literal(alias)
              .meta(MinecraftExtrasMetaKeys.DESCRIPTION, Component.text("Hover Invite?"))
              .permission("partychat.invite")
              .handler(this::execute)
      );
    }
    PartyChat.error("Registered Invite/Add!");
  }

  @Override
  public void execute(final @NotNull CommandContext<MinecraftSender> context) {
    context.getSender().sendMessage(Component.text("/party invite/add"));
  }
}
