# dcre-platform-files

> Part of the DCRE fleet. For the fleet map, the rulings and the diagrams that specify every stage, start at the [DCRE design register](https://github.com/sean-huni/dcre-design-register); the complete list of live repositories is its [Repositories](https://github.com/sean-huni/dcre-design-register#repositories) table.

Shared file-exchange library for the DCRE fleet: the per-client exchange directory contract, atomic
staged writes, the inbound filename grammar, and fixed-width OnHost boundary-file layouts.

## What it does

A library, not a service: it has no runtime of its own and reaches the cluster only inside the
stage-service images. It is the framework-agnostic file-exchange kernel the batch stage services
share. `ExchangeLayout` resolves a (client, channel, sub) triple to one absolute directory under the
exchange root (the `clientbase/channel/{in,out,error,archive}` contract, SCRUM-42), fail-closed: an
unconfigured triple throws instead of falling back to a shared or wrong directory.

Public API, package `za.co.fnb.dcre.platform.files`:

- `ExchangeChannel`: nine channels; the token is the on-disk path segment and equals the AGT route id
  for inbound channels. Collections: `onhost-req`, `onhost-req-endo`, `onhost-resp`, `fint-req`,
  `fint-resp`. Mandates (SCRUM-73): `onhost-req-man`, `onhost-resp-man`, `fint-req-man`,
  `fint-resp-man`.
- `ExchangeSub`: the lifecycle subdirectory under a channel: `in`, `out`, `error`, `archive`.
- `ExchangeLayout`: immutable per-client directory map built from an explicit root plus relative
  paths; `resolve(client, channel, sub)` fails closed with `IllegalArgumentException`;
  `allLeafDirs()` lists every configured leaf for bootstrap directory creation.
- `StagedWrite` (R-24): `write(target, lines)` stages a tmp file in the SAME directory, then
  `ATOMIC_MOVE`. An existing target is a prior emission and is treated as a completed write (restart
  no-op, returns `false`), never overwritten.
- `R31Filename` (R-31): `parse(filename)` returns the client code (`FNB*` prefix) and MsgId from the
  first two underscore tokens, or `Optional.empty()` for anything else.
- `FixedWidthLayout` + `LayoutField`, `Layouts` (collections `HEADER` 109, `DETAIL_V1` 161 synthetic,
  `DETAIL_V2` 169, `DETAIL_V3` 204 = V2 + `mandate_ref`), `MandateLayouts` (`HEADER` 109, `DETAIL`
  285, SYNTHETIC A-61): declarative fixed-width layouts ported from the fixture toolkit, offsets
  derived from the field table only, role-based field names per R-32. The fleet's copybook readers
  use the equivalent classes in `platform-copybook` instead (see Consumers).

## Consumers

Counted from each fleet repo's `build.gradle` on `origin/dev` (local clones, not fetched), plus
AGT (checked 2026-09-28):

| Consumer | Version | How |
|---|---|---|
| `platform-batch` | `0.1.0` | `api` dependency, so every consumer of `platform-batch` receives this library transitively |
| `crg`, `mrg`, `mrw`, `prg` | `0.1.0` | declared directly |
| `cir`, `crw`, `mir`, `pir`, `prw` | `0.1.0` via `platform-batch` | import the exchange and `StagedWrite` types |
| AGT (Quarkus) | not a consumer | |

In `src/main` the fleet imports `ExchangeChannel`, `ExchangeLayout`, `ExchangeSub`, `StagedWrite` and
`R31Filename` (the last from `crr`, `prr`, `mrr`). No consumer imports this library's `Layouts`,
`MandateLayouts`, `FixedWidthLayout` or `LayoutField`; the readers take those from
`platform-copybook`.

## Architecture and principles

- SOLID: one responsibility per class, small units with clear interfaces tested independently:
  layout arithmetic (`FixedWidthLayout`), the directory contract (`ExchangeLayout`), atomic emission
  (`StagedWrite`), filename grammar (`R31Filename`).
- Idempotent restart semantics: `StagedWrite` never overwrites; a re-run finding its target treats
  it as a prior emission and no-ops. This is one leg of the fleet's kill-resume guarantee.
- Fail-closed by default: unknown clients, channels, subs and layout fields throw; nothing silently
  lands in a shared directory.
- 12FactorApp Alignment - https://12factor.net/: the library reads no environment and holds no
  state. Consumers keep config in the environment: `platform-batch` binds `dcre.exchange` properties
  and builds the `ExchangeLayout` from them (`ExchangeProperties.toLayout()`), so this kernel stays
  framework-free.
- Plain-Java kernel, no Spring or Quarkus dependency: only `platform-model` is on the compile
  classpath, exposed as `api` to consumers.

## Prerequisites

- Java 25 (`.sdkmanrc`: `java=25-tem`; `build.gradle` sets `sourceCompatibility` /
  `targetCompatibility` 25)
- Gradle wrapper 9.5.1 (committed)
- `za.co.fnb.dcre:platform-model:0.1.0` in Maven Local (publish `platform-model` first)

## Build and publish

Coordinates: `za.co.fnb.dcre:platform-files:0.1.0` (binary and sources jar). Distribution is Maven
Local only; no remote repository is configured. Publish chain: `platform-model`, then this module,
then `platform-batch`.

```bash
# once: the upstream platform lib
(cd ../platform-model && ./gradlew publishToMavenLocal)

./gradlew test publishToMavenLocal
```

```groovy
repositories { mavenCentral(); mavenLocal() }
dependencies {
    implementation 'za.co.fnb.dcre:platform-files:0.1.0'
}
```

`platform-model` arrives transitively via `api`. After `publishToMavenLocal`, each service's
`./gradlew bootJar` resolves the jar from Maven Local and its image build bakes it in (see
`dcre-infra` for the fleet quickstart). Fleet release tags (digits-only SemVer) are applied to this
repo uniformly with the rest of the fleet and are independent of the artifact version `0.1.0`.

## Configuration

None. The library reads no environment variables, binds no properties and ships no
`application.yml`. The exchange root and per-client relative paths are passed in by the consumer (in
the stage services, `dcre.exchange.root` + `dcre.exchange.clients` bound by `platform-batch`).

## Testing

```bash
./gradlew test
```

JUnit Jupiter (BOM 6.0.2), no Docker. 4 test classes, 21 `@Test` methods (counted from `src/test` at
HEAD): `LayoutsTest` slices the attested sample lines from SPEC-DATA-MODEL section 3 and exercises
the R-31 grammar plus the `StagedWrite` restart no-op; `MandateLayoutsTest` pins the synthetic
mandate widths; `ExchangeLayoutTest` covers resolution, all three fail-closed cases and leaf
enumeration; `ExchangeChannelTest` snapshots the tokens and the mandate channels' fail-closed
resolution.

## Related repositories

The complete, current list of live DCRE repositories (stage services, orchestrator, platform libraries, infra and tooling) lives in one place: the [DCRE design register README](https://github.com/sean-huni/dcre-design-register#repositories). Deprecated and archived repositories are deliberately absent from it. This README does not copy that list, so it cannot drift.

- Design register: https://github.com/sean-huni/dcre-design-register (start at `docs/specs/DESIGN-REGISTER.md`; the diagrams in `docs/diagrams/` are the specification)
