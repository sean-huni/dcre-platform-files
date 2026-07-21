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
 *
 * <p>Latent-dir capture contract (SCRUM-58): any future producer landing a
 * file in a dormant {@code error/} or {@code archive/} out-dir MUST, BEFORE
 * the move: (1) write-ahead a filename row in its owner table keyed by the
 * full business identity; (2) add a {@code v_file_index} arm; (3) use the
 * reserved step names {@code MOVED_TO_ERROR} / {@code MOVED_TO_ARCHIVE} with
 * direction and route per the 11-column view contract. Spec:
 * {@code design-register/docs/specs/2026-07-16-file-trace-design.md},
 * section 4 gap 4 (F3).
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
