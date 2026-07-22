package za.co.fnb.dcre.platform.files;

/**
 * The exchange channels (SCRUM-42 collections five + SCRUM-73 mandates four).
 * The token is the on-disk `<channel>` path segment and equals the AGT route
 * id for inbound channels. Mandate flows get dedicated channels end to end so
 * flow separation stays aligned with the dcre-man namespace; collections
 * channels are untouched.
 */
public enum ExchangeChannel {

    ONHOST_REQ("onhost-req"),
    ONHOST_REQ_ENDO("onhost-req-endo"),
    ONHOST_RESP("onhost-resp"),
    FINT_REQ("fint-req"),
    FINT_RESP("fint-resp"),
    ONHOST_REQ_MAN("onhost-req-man"),
    ONHOST_RESP_MAN("onhost-resp-man"),
    FINT_REQ_MAN("fint-req-man"),
    FINT_RESP_MAN("fint-resp-man");

    private final String token;

    ExchangeChannel(final String token) {
        this.token = token;
    }

    public String token() {
        return token;
    }

    public static ExchangeChannel fromToken(final String token) {
        for (final ExchangeChannel channel : values()) {
            if (channel.token.equals(token)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("unknown exchange channel token '%s'".formatted(token));
    }
}
