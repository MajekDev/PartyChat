package dev.majek.pc.data.struct.user;

import org.jetbrains.annotations.NotNull;

public interface Sender {

  @NotNull String username();

  boolean permission(final @NotNull String permission);
}
