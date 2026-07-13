package za.co.fnb.dcre.platform.files;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable per-client exchange directory map (SCRUM-42). Resolves a
 * (client, channel, sub) triple to an absolute path under {@code root} from
 * explicit relative paths. Fail-closed: an unconfigured triple throws rather
 * than silently falling back to a shared or wrong directory.
 */
public final class ExchangeLayout {

    private final Path root;
    private final Map<String, Map<ExchangeChannel, Map<ExchangeSub, String>>> dirs;

    public ExchangeLayout(final Path root,
                          final Map<String, Map<ExchangeChannel, Map<ExchangeSub, String>>> dirs) {
        this.root = Objects.requireNonNull(root, "root");
        this.dirs = deepCopy(Objects.requireNonNull(dirs, "dirs"));
    }

    /** Resolve one leaf directory; throws IllegalArgumentException when unconfigured. */
    public Path resolve(final String client, final ExchangeChannel channel, final ExchangeSub sub) {
        final Map<ExchangeChannel, Map<ExchangeSub, String>> byChannel = dirs.get(client);
        if (byChannel == null) {
            throw new IllegalArgumentException("no exchange dirs configured for client '%s'".formatted(client));
        }
        final Map<ExchangeSub, String> bySub = byChannel.get(channel);
        if (bySub == null) {
            throw new IllegalArgumentException(
                    "client '%s' has no '%s' channel configured".formatted(client, channel.token()));
        }
        final String relative = bySub.get(sub);
        if (relative == null) {
            throw new IllegalArgumentException("client '%s' channel '%s' has no '%s' sub configured"
                    .formatted(client, channel.token(), sub.token()));
        }
        return root.resolve(relative);
    }

    /** Every configured leaf directory resolved against root (for bootstrap). */
    public List<Path> allLeafDirs() {
        final List<Path> leaves = new ArrayList<>();
        for (final var byChannel : dirs.values()) {
            for (final var bySub : byChannel.values()) {
                for (final String relative : bySub.values()) {
                    leaves.add(root.resolve(relative));
                }
            }
        }
        return List.copyOf(leaves);
    }

    private static Map<String, Map<ExchangeChannel, Map<ExchangeSub, String>>> deepCopy(
            final Map<String, Map<ExchangeChannel, Map<ExchangeSub, String>>> source) {
        final Map<String, Map<ExchangeChannel, Map<ExchangeSub, String>>> copy = new LinkedHashMap<>();
        source.forEach((client, byChannel) -> {
            final Map<ExchangeChannel, Map<ExchangeSub, String>> channels = new LinkedHashMap<>();
            byChannel.forEach((channel, bySub) -> channels.put(channel, new LinkedHashMap<>(bySub)));
            copy.put(client, channels);
        });
        return copy;
    }
}
