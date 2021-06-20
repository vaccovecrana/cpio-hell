package io.vacco.cpiohell;

import org.apache.commons.compress.archivers.cpio.*;
import org.apache.commons.compress.compressors.gzip.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.Deflater;

import static io.vacco.cpiohell.CoPaths.*;
import static io.vacco.cpiohell.CoPosix.*;
import static org.apache.commons.compress.archivers.cpio.CpioConstants.*;
import static java.lang.String.format;

public class CoArchive {

  public static void withEntry(Path source, Path cpioPath, CpioArchiveOutputStream out,
                               Consumer<CpioArchiveEntry> pre, Consumer<CpioArchiveEntry> post) {
    try {
      CpioArchiveEntry ae = new CpioArchiveEntry(source.toFile(), cpioPath.toString());
      if (pre != null) { pre.accept(ae); }
      out.putArchiveEntry(ae);
      if (post != null) { post.accept(ae); }
      out.closeArchiveEntry();
    } catch (IOException e) {
      throw new IllegalStateException(format("cpio entry creation failed: [%s -> %s]", source, cpioPath), e);
    }
  }

  public static void copy(Path file, CpioArchiveOutputStream out) {
    try {
      Files.copy(file, out);
    } catch (IOException e) {
      throw new IllegalStateException("File copy failed", e);
    }
  }

  public static Map<Path, Path> initLayout(Path root) {
    try {
      Map<Path, Path> cpioEntries = new TreeMap<>();
      Files.walkFileTree(root, new SimpleFileVisitor<>() {
        @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
          if (attributes.isSymbolicLink()) { return FileVisitResult.CONTINUE; }
          final Path[] parent = new Path[1];
          parent[0] = file.getParent();
          while (parent[0] != null && !parent[0].equals(root)) {
            cpioEntries.put(cpioPathOf(parent[0], root), parent[0]);
            parent[0] = parent[0].getParent();
          }
          cpioEntries.put(cpioPathOf(file, root), file);
          return FileVisitResult.CONTINUE;
        }
        @Override public FileVisitResult visitFileFailed(Path file, IOException ioe) { throw new IllegalStateException(ioe); }
      });
      return cpioEntries;
    } catch (Exception e) {
      throw new IllegalStateException("CPIO directory layout creation failed.", e);
    }
  }

  public static void apply(Map<Path, Path> layout, Path archive) {
    try {
      GzipParameters gzParams = new GzipParameters();
      gzParams.setCompressionLevel(Deflater.BEST_COMPRESSION);
      try (OutputStream fOut = Files.newOutputStream(archive);
           BufferedOutputStream buffOut = new BufferedOutputStream(fOut);
           GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(buffOut, gzParams);
           CpioArchiveOutputStream out = new CpioArchiveOutputStream(gzOut)) {
        layout.forEach((cp, rp) -> {
          if (rp.toFile().isDirectory()) {
            withEntry(rp, cp, out, e -> setPermissions(C_ISDIR, getPermissions(rp), e), null);
          } else {
            withEntry(rp, cp, out,
                e -> setPermissions(C_ISREG, getPermissions(rp), e),
                e -> copy(rp, out)
            );
          }
        });
        out.finish();
      }
    } catch (Exception e) {
      throw new IllegalStateException("CPIO archive creation failed.", e);
    }
  }

}
