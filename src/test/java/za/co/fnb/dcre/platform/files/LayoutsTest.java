package za.co.fnb.dcre.platform.files;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LayoutsTest {

    // The attested sample lines from SPEC-DATA-MODEL section 3 (values only).
    static final String HEADER =
        "0002DCRERF2026071112000002             202607111200 00000000000030FNBRF01                            20260711                                                            ";
    static final String DETAIL =
        "01FNBRF00001-62681769356319023       62681769356319023      CT0000000001  ZAR000000000117710250205     FNB REVOLVING FACILITY             62681769356319023      DDA RCUR";

    @Test
    void headerFieldsSliceToAttestedValues() {
        assertEquals(109, Layouts.HEADER.length());
        assertEquals("0002", Layouts.HEADER.slice(HEADER, "record_type"));
        assertEquals("DCRE", Layouts.HEADER.slice(HEADER, "sender_id"));
        assertEquals("RF", Layouts.HEADER.slice(HEADER, "file_type"));
        assertEquals("20260711120000", Layouts.HEADER.slice(HEADER, "created_ts"));
        assertEquals("02", Layouts.HEADER.slice(HEADER, "layout_version"));
        assertEquals("FNBRF01", Layouts.HEADER.slice(HEADER, "destination_id").strip());
        assertEquals(30, Integer.parseInt(Layouts.HEADER.slice(HEADER, "tx_count").strip()));
    }

    @Test
    void detailV2SlicesRoleNamedFields() {
        assertEquals(169, Layouts.DETAIL_V2.length());
        assertEquals(DETAIL.length(), Layouts.DETAIL_V2.length());
        assertEquals("01", Layouts.DETAIL_V2.slice(DETAIL, "record_type"));
        assertEquals("62681769356319023", Layouts.DETAIL_V2.slice(DETAIL, "creditor_account").strip());
        assertEquals("ZAR", Layouts.DETAIL_V2.slice(DETAIL, "currency"));
        assertEquals("62681769356319023", Layouts.DETAIL_V2.slice(DETAIL, "debtor_account").strip());
        assertEquals("DDA RCUR", Layouts.DETAIL_V2.slice(DETAIL, "acc_type_seq"));
    }

    @Test
    void r31TokensAndStagedWrite() throws Exception {
        var t = R31Filename.parse("FNBRF01_DCRERF2026071112000002.txt").orElseThrow();
        assertEquals("FNBRF01", t.client());
        assertEquals("DCRERF2026071112000002", t.msgId());
        assertTrue(R31Filename.parse("dcre_copybook_sample.txt").isEmpty());

        Path dir = Files.createTempDirectory("staged");
        Path target = dir.resolve("out.txt");
        assertTrue(StagedWrite.write(target, List.of("a", "b")));
        assertFalse(StagedWrite.write(target, List.of("c")), "existing target is a restart no-op");
        assertEquals(List.of("a", "b"), Files.readAllLines(target));
    }
}
