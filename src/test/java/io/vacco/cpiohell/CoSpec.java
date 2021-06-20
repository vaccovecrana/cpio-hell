package io.vacco.cpiohell;

import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class CoSpec {

  static Path cpioPath = Paths.get("./src/test/resources", "initramfs-virt.cpio.gz");
  static Path cpioCopy = Paths.get("./build", "initramfs-copy.cpio.gz");
  static Path cpioOut = Paths.get("./build", "cpio-expanded");

  static Path tarGzPath = Paths.get("./src/test/resources", "age-v1.0.0-rc.3-linux-amd64.tar.gz");
  static Path tarGzOut = Paths.get("./build", "tgz-expanded");

  static {
    beforeAll(() -> CoDirectories.delete(cpioOut.toFile()));
    it("Can extract a .tar.gz archive striping root folder", () -> {
      tarGzOut.toFile().mkdirs();
      CoExpand.apply(tarGzPath, tarGzOut, true);
    });
    it("Can extract a CPIO archive", () -> {
      cpioOut.toFile().mkdirs();
      CoExpand.apply(cpioPath, cpioOut, false);
    });
    it("Can remove contents from an expanded archive", () -> {
      Path media = Paths.get(cpioOut.toString(), "media");
      CoDirectories.delete(media.toFile());
    });
    it("Can copy a directory sub-tree into an expanded archive", () -> {
      Path recursive = Paths.get("./src/test/resources", "recursive");
      CoDirectories.copy(recursive, cpioOut);
    });
    it("Can copy a file set into an expanded archive", () -> {
      Set<Path> files = new TreeSet<>();
      files.add(Paths.get("./src/test/resources/flat", "one-flat.md"));
      files.add(Paths.get("./src/test/resources/flat", "two-flat.md"));
      files.add(Paths.get("./src/test/resources/flat", "three-flat.md"));
      CoDirectories.copyFlat(files, cpioOut);
    });
    it("Can re-compress the contents of a CPIO archive", () -> {
      Map<Path, Path> cpioLayout = CoArchive.initLayout(cpioOut);
      CoArchive.apply(cpioLayout, cpioCopy);
    });
  }
}
