package io.vacco.cpiohell;

import java.io.*;
import java.net.URL;
import java.nio.channels.*;

import static java.lang.String.format;

public class CoDownload {

  public static void apply(URL src, File dst) {
    try {
      if (!dst.exists()) {
        ReadableByteChannel readableByteChannel = Channels.newChannel(src.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(dst);
        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
      }
    } catch (Exception e) {
      throw new IllegalStateException(format("URL download failed: %s", src), e);
    }
  }

}
