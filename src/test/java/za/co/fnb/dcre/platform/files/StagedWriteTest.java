package za.co.fnb.dcre.platform.files;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Split out of LayoutsTest when the layout tables moved to platform-copybook. */
class StagedWriteTest {

    @Test
    void writesOnceAndTreatsAnExistingTargetAsARestartNoOp() throws Exception {
        Path dir = Files.createTempDirectory("staged");
        Path target = dir.resolve("out.txt");

        assertTrue(StagedWrite.write(target, List.of("a", "b")));
        assertFalse(StagedWrite.write(target, List.of("c")), "existing target is a restart no-op");
        assertEquals(List.of("a", "b"), Files.readAllLines(target));
    }
}
