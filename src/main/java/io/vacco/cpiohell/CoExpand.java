package io.vacco.cpiohell;

import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.compressors.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import static io.vacco.cpiohell.CoPaths.*;
import static io.vacco.cpiohell.CoPosix.*;

import static java.lang.String.format;

public class CoExpand {

  public static void apply(Path archive, Path targetDir, boolean stripRoot) {
    try {
      CompressorStreamFactory csf = new CompressorStreamFactory();
      BufferedInputStream fis = new BufferedInputStream(new FileInputStream(archive.toFile()));
      CompressorInputStream cis = csf.createCompressorInputStream(fis);
      ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(cis));
      ArchiveEntry e = ais.getNextEntry();
      while (e != null) {
        Path p0 = Paths.get(e.getName());
        Path out = merge(p0, targetDir, stripRoot);
        if (e.isDirectory()) {
          out.toFile().mkdirs();
        } else {
          out.getParent().toFile().mkdirs();
          Files.copy(ais, out, StandardCopyOption.REPLACE_EXISTING);
          Set<PosixFilePermission> perms;
          if (e instanceof TarArchiveEntry) {
            perms = getRawPermissions(((TarArchiveEntry) e).getMode());
          } else if (e instanceof ZipArchiveEntry) {
            perms = getRawPermissions(((ZipArchiveEntry) e).getUnixMode());
          } else if (e instanceof CpioArchiveEntry) {
            perms = getRawPermissions((int) ((CpioArchiveEntry) e).getMode());
          } else {
            throw new IllegalStateException(format(
                "Unsupported archive entry type: [%s]. Please use only .tar.gz, .cpio or .zip files", e
            ));
          }
          Files.setPosixFilePermissions(out, perms);
        }
        e = ais.getNextEntry();
      }
      ais.close();
      cis.close();
    } catch (Exception e) {
      throw new IllegalStateException(format("Archive extraction failed: [%s] -> [%s]", archive, targetDir), e);
    }
  }

}
