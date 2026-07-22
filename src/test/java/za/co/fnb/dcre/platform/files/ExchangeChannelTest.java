package za.co.fnb.dcre.platform.files;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExchangeChannelTest {

    /**
     * Snapshot of the on-disk channel tokens (SCRUM-42 + M10 mandates). Tokens
     * are path segments AND AGT route ids, so a rename or reorder is an
     * exchange-contract break, not a refactor.
     */
    @Test
    void tokenSnapshotGuardsAgainstAccidentalRenames() {
        assertEquals(List.of(
                        "onhost-req",
                        "onhost-req-endo",
                        "onhost-resp",
                        "fint-req",
                        "fint-resp",
                        "onhost-req-man",
                        "onhost-resp-man",
                        "fint-req-man",
                        "fint-resp-man"),
                Arrays.stream(ExchangeChannel.values()).map(ExchangeChannel::token).toList());
    }

    @Test
    void mandateTokensRoundTrip() {
        assertEquals(ExchangeChannel.ONHOST_REQ_MAN, ExchangeChannel.fromToken("onhost-req-man"));
        assertEquals(ExchangeChannel.ONHOST_RESP_MAN, ExchangeChannel.fromToken("onhost-resp-man"));
        assertEquals(ExchangeChannel.FINT_REQ_MAN, ExchangeChannel.fromToken("fint-req-man"));
        assertEquals(ExchangeChannel.FINT_RESP_MAN, ExchangeChannel.fromToken("fint-resp-man"));
    }

    /**
     * Adding the mandate constants must not weaken ExchangeLayout fail-closed
     * semantics: a client configured only with collections channels still
     * throws for every unconfigured mandate channel.
     */
    @Test
    void collectionsOnlyClientFailsClosedForMandateChannels() {
        final ExchangeLayout layout = new ExchangeLayout(Path.of("/exchange"), Map.of(
                "FNBCC01", Map.of(
                        ExchangeChannel.ONHOST_REQ, Map.of(ExchangeSub.IN, "fnbcc01/onhost-req/in"),
                        ExchangeChannel.FINT_RESP, Map.of(ExchangeSub.IN, "fnbcc01/fint-resp/in"))));
        for (final ExchangeChannel channel : List.of(
                ExchangeChannel.ONHOST_REQ_MAN,
                ExchangeChannel.ONHOST_RESP_MAN,
                ExchangeChannel.FINT_REQ_MAN,
                ExchangeChannel.FINT_RESP_MAN)) {
            assertThrows(IllegalArgumentException.class,
                    () -> layout.resolve("FNBCC01", channel, ExchangeSub.IN), channel.token());
        }
    }

    @Test
    void configuredMandateChannelResolves() {
        final ExchangeLayout layout = new ExchangeLayout(Path.of("/exchange"), Map.of(
                "FNBCC01", Map.of(
                        ExchangeChannel.ONHOST_REQ_MAN, Map.of(ExchangeSub.IN, "fnbcc01/onhost-req-man/in"))));
        assertEquals(Path.of("/exchange/fnbcc01/onhost-req-man/in"),
                layout.resolve("FNBCC01", ExchangeChannel.ONHOST_REQ_MAN, ExchangeSub.IN));
    }
}
