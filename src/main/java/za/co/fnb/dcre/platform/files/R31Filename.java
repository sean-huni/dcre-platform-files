package za.co.fnb.dcre.platform.files;

import java.util.Optional;

/** R-31: inbound filenames carry the client code + MsgId as underscore tokens. */
public final class R31Filename {

    public record Tokens(String client, String msgId) {
    }

    private R31Filename() {
    }

    public static Optional<Tokens> parse(String filename) {
        String stem = filename.contains(".")
                ? filename.substring(0, filename.lastIndexOf('.')) : filename;
        String[] tokens = stem.split("_");
        if (tokens.length >= 2 && tokens[0].startsWith("FNB")) {
            return Optional.of(new Tokens(tokens[0], tokens[1]));
        }
        return Optional.empty();
    }
}
