package io.vacco.cpiohell;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

import static org.apache.commons.compress.archivers.cpio.CpioConstants.*;

public class CoPosix {

  public static Set<PosixFilePermission> getPermissions(Path p) {
    try {
      return Files.getPosixFilePermissions(p);
    } catch (IOException e) {
      throw new IllegalStateException("POSIX permission retrieval failed", e);
    }
  }

  public static void setPermissions(long entryType, Set<PosixFilePermission> permissions, CpioArchiveEntry entry) {
    long mode = entryType;
    for (PosixFilePermission p : permissions) {
      switch (p) {
        case OWNER_READ:      mode = mode | C_IRUSR; break;
        case OWNER_WRITE:     mode = mode | C_IWUSR; break;
        case OWNER_EXECUTE:   mode = mode | C_IXUSR; break;

        case GROUP_READ:      mode = mode | C_IRGRP; break;
        case GROUP_WRITE:     mode = mode | C_IWGRP; break;
        case GROUP_EXECUTE:   mode = mode | C_IXGRP; break;

        case OTHERS_READ:     mode = mode | C_IROTH; break;
        case OTHERS_WRITE:    mode = mode | C_IWOTH; break;
        case OTHERS_EXECUTE:  mode = mode | C_IXOTH; break;
      }
    }
    entry.setMode(mode);
  }

  public static Set<PosixFilePermission> getRawPermissions(int mode) {
    Set<PosixFilePermission> result = EnumSet.noneOf(PosixFilePermission.class);
    if ((mode & 0400) != 0) { result.add(PosixFilePermission.OWNER_READ); }
    if ((mode & 0200) != 0) { result.add(PosixFilePermission.OWNER_WRITE); }
    if ((mode & 0100) != 0) { result.add(PosixFilePermission.OWNER_EXECUTE); }
    if ((mode & 040) != 0)  { result.add(PosixFilePermission.GROUP_READ); }
    if ((mode & 020) != 0)  { result.add(PosixFilePermission.GROUP_WRITE); }
    if ((mode & 010) != 0)  { result.add(PosixFilePermission.GROUP_EXECUTE); }
    if ((mode & 04) != 0)   { result.add(PosixFilePermission.OTHERS_READ); }
    if ((mode & 02) != 0)   { result.add(PosixFilePermission.OTHERS_WRITE); }
    if ((mode & 01) != 0)   { result.add(PosixFilePermission.OTHERS_EXECUTE); }
    return result;
  }

}
