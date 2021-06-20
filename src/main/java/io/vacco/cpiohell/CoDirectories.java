package io.vacco.cpiohell;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CoDirectories {

  public static void copy(Path srcDir, Path dstDir) {
    try {
      Files.walk(srcDir).forEach(src -> {
        Path dst = Paths.get(dstDir.toString(), src.toString().substring(srcDir.toString().length()));
        if (src.toFile().isDirectory()) {
          dst.toFile().mkdirs();
        } else {
          try { Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING); }
          catch (IOException e) { throw new IllegalStateException(e); }
        }
      });
    } catch (IOException e) {
      throw new IllegalStateException("Directory copy failed.", e);
    }
  }

  public static Set<Path> copyFlat(Set<Path> files, Path destDir) {
    try {
      Set<Path> outPaths = new TreeSet<>();
      for (Path f : files) {
        Path out = destDir.resolve(f.getFileName());
        if (f.toFile().isDirectory()) {
          out.toFile().mkdirs();
        } else {
          out.toFile().delete();
          Files.copy(f, out);
          outPaths.add(out);
        }
      }
      return outPaths;
    } catch (Exception e) {
      throw new IllegalStateException("File set copy failed", e);
    }
  }

  public static void delete(File directoryToBeDeleted) {
    File[] allContents = directoryToBeDeleted.listFiles();
    if (allContents != null) {
      for (File file : allContents) {
        delete(file);
      }
    }
    directoryToBeDeleted.delete();
  }

}
