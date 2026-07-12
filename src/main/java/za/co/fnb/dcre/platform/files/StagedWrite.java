package za.co.fnb.dcre.platform.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Stage-then-rename boundary write (R-24): tmp in the SAME directory, then
 * ATOMIC_MOVE. Existing target = prior emission; the caller treats it as a
 * completed write (restart no-op), never overwrites.
 */
public final class StagedWrite {

    private StagedWrite() {
    }

    /** @return true when written; false when the target already existed (no-op). */
    public static boolean write(Path target, List<String> lines) throws IOException {
        if (Files.exists(target)) {
            return false;
        }
        Files.createDirectories(target.getParent());
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        Files.write(tmp, lines);
        Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE);
        return true;
    }
}
