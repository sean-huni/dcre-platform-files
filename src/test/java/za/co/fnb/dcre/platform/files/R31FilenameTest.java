package za.co.fnb.dcre.platform.files;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Split out of LayoutsTest when the layout tables moved to platform-copybook. */
class R31FilenameTest {

    @Test
    void parsesTheClientAndMsgIdOutOfAnR31Filename() {
        var t = R31Filename.parse("FNBRF01_DCRERF2026071112000002.txt").orElseThrow();
        assertEquals("FNBRF01", t.client());
        assertEquals("DCRERF2026071112000002", t.msgId());
    }

    @Test
    void aNameThatIsNotR31YieldsNoTokens() {
        assertTrue(R31Filename.parse("dcre_copybook_sample.txt").isEmpty());
    }
}
