package za.co.fnb.dcre.platform.files;

/** The lifecycle subdirectory under a channel (SCRUM-42): in/out/error/archive. */
public enum ExchangeSub {

    IN("in"),
    OUT("out"),
    ERROR("error"),
    ARCHIVE("archive");

    private final String token;

    ExchangeSub(final String token) {
        this.token = token;
    }

    public String token() {
        return token;
    }
}
