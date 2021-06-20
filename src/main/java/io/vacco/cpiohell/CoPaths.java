package io.vacco.cpiohell;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CoPaths {

  public static Path merge(Path p0, Path p1, boolean stripP0Root) {
    if (stripP0Root) {
      if (p0.getNameCount() > 1) {
        p0 = p0.subpath(1, p0.getNameCount());
      } else {
        return p1;
      }
    }
    p0 = Paths.get("./", p0.toString());
    return p1.resolve(p0).normalize();
  }

  public static Path strip(Path target, Path from) {
    Path out = Paths.get(target.toString().replace(from.toString(), ""));
    return out;
  }

  public static Path cpioPathOf(Path source, Path root) {
    Path cpioPath;
    if (source.toFile().isDirectory()) {
      cpioPath = source.relativize(root);
      cpioPath = cpioPath.toString().isEmpty() ? Paths.get(".") : cpioPath;
      if (cpioPath.toString().contains("..")) {
        cpioPath = strip(source, root);
        cpioPath = Paths.get(cpioPath.toString().substring(1));
      }
    } else {
      cpioPath = strip(source, root);
      cpioPath = Paths.get(cpioPath.toString().substring(1));
    }
    return cpioPath;
  }

}

