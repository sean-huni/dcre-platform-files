package za.co.fnb.dcre.platform.files;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExchangeLayoutTest {

    private static ExchangeLayout twoClientLayout() {
        final Path root = Path.of("/exchange");
        final var fnbcc01 = Map.of(
                ExchangeChannel.ONHOST_RESP, Map.of(
                        ExchangeSub.OUT, "fnbcc01/onhost-resp/out",
                        ExchangeSub.ERROR, "fnbcc01/onhost-resp/error",
                        ExchangeSub.ARCHIVE, "fnbcc01/onhost-resp/archive"),
                ExchangeChannel.FINT_REQ, Map.of(
                        ExchangeSub.OUT, "fnbcc01/fint-req/out"));
        final var fnbrf01 = Map.of(
                ExchangeChannel.ONHOST_REQ, Map.of(
                        ExchangeSub.IN, "fnbrf01/onhost-req/in"));
        return new ExchangeLayout(root, Map.of("FNBCC01", fnbcc01, "FNBRF01", fnbrf01));
    }

    @Test
    void resolveHappyPath() {
        final ExchangeLayout layout = twoClientLayout();
        assertEquals(Path.of("/exchange/fnbcc01/onhost-resp/out"),
                layout.resolve("FNBCC01", ExchangeChannel.ONHOST_RESP, ExchangeSub.OUT));
        assertEquals(Path.of("/exchange/fnbrf01/onhost-req/in"),
                layout.resolve("FNBRF01", ExchangeChannel.ONHOST_REQ, ExchangeSub.IN));
    }

    @Test
    void resolveFailsClosedOnUnknownClient() {
        assertThrows(IllegalArgumentException.class,
                () -> twoClientLayout().resolve("FNBCC99", ExchangeChannel.ONHOST_RESP, ExchangeSub.OUT));
    }

    @Test
    void resolveFailsClosedOnUnknownChannel() {
        assertThrows(IllegalArgumentException.class,
                () -> twoClientLayout().resolve("FNBRF01", ExchangeChannel.FINT_RESP, ExchangeSub.IN));
    }

    @Test
    void resolveFailsClosedOnUnknownSub() {
        assertThrows(IllegalArgumentException.class,
                () -> twoClientLayout().resolve("FNBCC01", ExchangeChannel.FINT_REQ, ExchangeSub.ERROR));
    }

    @Test
    void allLeafDirsCountsEveryConfiguredLeaf() {
        // 3 (onhost-resp) + 1 (fint-req) + 1 (onhost-req) = 5 configured leaves
        assertEquals(5, twoClientLayout().allLeafDirs().size());
    }

    @Test
    void channelTokenRoundTrips() {
        assertEquals(ExchangeChannel.FINT_REQ, ExchangeChannel.fromToken("fint-req"));
        assertEquals("onhost-req-endo", ExchangeChannel.ONHOST_REQ_ENDO.token());
        assertThrows(IllegalArgumentException.class, () -> ExchangeChannel.fromToken("nope"));
    }
}
