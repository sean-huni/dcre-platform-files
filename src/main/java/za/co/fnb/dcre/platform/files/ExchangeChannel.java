package za.co.fnb.dcre.platform.files;

/**
 * The five exchange channels (SCRUM-42). The token is the on-disk `<channel>`
 * path segment and equals the AGT route id for inbound channels.
 */
public enum ExchangeChannel {

    ONHOST_REQ("onhost-req"),
    ONHOST_REQ_ENDO("onhost-req-endo"),
    ONHOST_RESP("onhost-resp"),
    FINT_REQ("fint-req"),
    FINT_RESP("fint-resp");

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
