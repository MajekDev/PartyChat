package dev.majek.pc.util;

import dev.majek.pc.PartyChat;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class Logging extends Handler {

  @Override
  public void publish(@NotNull LogRecord record) {
    PartyChat.dataHandler().logToFile(record.getMessage(), record.getLevel().getName().toUpperCase(Locale.ROOT));
  }

  @Override
  public void flush() {

  }

  @Override
  public void close() throws SecurityException {

  }
}
