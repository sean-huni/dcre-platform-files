# dcre-platform-files

Shared file-exchange library for the DCRE Collections 3.0 fleet: the per-client exchange
directory contract plus fixed-width OnHost boundary-file layouts and atomic staged writes.

## What it does

Provides the framework-agnostic file-exchange kernel that the batch stage services share.
`ExchangeLayout` resolves a (client, channel, sub) triple to one absolute directory under the
exchange root (the `clientbase/channel/{in,out,error,archive}` contract, SCRUM-42), fail-closed:
an unconfigured triple throws instead of falling back to a shared or wrong directory. Alongside
it live the fixed-width OnHost record layouts (`Layouts`), the stage-then-rename boundary write
(`StagedWrite`, R-24) and the inbound filename grammar (`R31Filename`, R-31). It is consumed by
`dcre-platform-batch` (as `api`, so every stage service inherits it) and directly by `dcre-prg`.

## Key classes

- `ExchangeChannel`: the five channels; the token is the on-disk path segment and equals the AGT
  route id for inbound channels: `onhost-req`, `onhost-req-endo`, `onhost-resp`, `fint-req`,
  `fint-resp`.
- `ExchangeSub`: the lifecycle subdirectory under a channel: `in`, `out`, `error`, `archive`.
- `ExchangeLayout`: immutable per-client directory map built from explicit relative paths;
  `resolve(client, channel, sub)` fails closed with `IllegalArgumentException`, `allLeafDirs()`
  lists every configured leaf for bootstrap directory creation.
- `FixedWidthLayout` + `LayoutField`: declarative fixed-width layout; field offsets derive from
  the field table only, never from rendered whitespace (register provenance rule).
- `Layouts`: OnHost record layouts, ported field-for-field from the fixture toolkit's
  `generate_dcre_copybook.py` (dev-normative source under R-35). HEADER 109 chars; DETAIL_V1 161
  (synthetic, fails closed in production per A-2); DETAIL_V2 169. Role-based field names per R-32
  (`creditor_account` / `debtor_account`, never positional names).
- `StagedWrite` (R-24): tmp file staged in the SAME directory, then `ATOMIC_MOVE`. An existing
  target is a prior emission and is treated as a completed write (restart no-op, returns
  `false`), never overwritten.
- `R31Filename` (R-31): parses inbound filenames whose underscore tokens carry the client code
  (`FNB*` prefix) + MsgId; returns `Optional.empty()` for anything else.

## Architecture and principles

- SOLID: one responsibility per class, small units with clear interfaces that are tested
  independently: layout arithmetic (`FixedWidthLayout`), the directory contract
  (`ExchangeLayout`), atomic emission (`StagedWrite`), filename grammar (`R31Filename`).
- Idempotent restart semantics: `StagedWrite` never overwrites; a re-run finding its target
  treats it as a prior emission and no-ops. This is one leg of the fleet's chaos-validated
  kill-resume guarantee (SIGKILL at every stage, same-identity relaunch, zero duplicates).
- Fail-closed by default: unknown clients/channels/subs and unknown layout fields throw;
  nothing silently lands in a shared directory.
- 12FactorApp Alignment - https://12factor.net/: the library itself reads no environment and
  holds no state. Consumers keep config strictly in the environment: `dcre-platform-batch`
  binds `dcre.exchange` config properties and builds the `ExchangeLayout` from them
  (`ExchangeProperties.toLayout()`), so the kernel stays framework-free.
- Plain-Java kernel, no Spring/Quarkus dependency: only `dcre-platform-model` (shared domain
  types) is on the compile classpath, exposed as `api` to consumers.

## Prerequisites

- Java 25 (Gradle toolchain; wrapper 9.5.1 included)
- `za.co.fnb.dcre:platform-model:0.1.0` in Maven Local (publish `dcre-platform-model` first)

## Quickstart

Clean clone, no `.env` needed:

```bash
# once: the upstream platform lib
cd ../platform-model && ./gradlew publishToMavenLocal && cd -

./gradlew test publishToMavenLocal
```

This publishes `za.co.fnb.dcre:platform-files:0.1.0` (binary + sources jar, `maven-publish`
plugin) to `~/.m2/repository`. Publish chain: `dcre-platform-model` -> this module ->
`dcre-platform-batch`.

Consume from another project:

```groovy
repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation 'za.co.fnb.dcre:platform-files:0.1.0'
}
```

`platform-model` arrives transitively via `api`; consumers add nothing else.

## Configuration

The library reads no environment variables and ships no `application.yml`. Exchange root and
per-client relative paths are supplied by consumers from their own environment-driven config
(in the stage services: `dcre.exchange.root` + `dcre.exchange.clients` bound by
`dcre-platform-batch`), and passed in as an explicit root + map.

## Testing

```bash
./gradlew test
```

JUnit Jupiter (BOM 6.0.2). `LayoutsTest` slices the attested sample lines from SPEC-DATA-MODEL
section 3 and exercises the R-31 grammar plus the `StagedWrite` restart no-op;
`ExchangeLayoutTest` covers happy-path resolution, all three fail-closed cases, leaf-dir
enumeration and channel-token round-trips.

## Distribution into the local cluster

There is no image for this repo: it reaches the kind cluster inside the stage-service images.
After `./gradlew publishToMavenLocal` here, each service's `./gradlew bootJar` resolves the jar
from Maven Local and `docker build` bakes it into the service image that
`kind load docker-image --name dcre-dev` ships to the cluster (see `dcre-infra` for the full
quickstart). Distribution is Maven Local only; no remote repository is configured. Fleet
releases tag this repo uniformly with digits-only SemVer (current: `2.1.1`) even when the
artifact version (`0.1.0`) is unchanged.

## Related repositories

- https://github.com/sean-huni/dcre-agt (orchestrator: DirectoryWatcher, job minting)
- https://github.com/sean-huni/dcre-platform-model
- https://github.com/sean-huni/dcre-platform-batch
- https://github.com/sean-huni/dcre-platform-persistence
- https://github.com/sean-huni/dcre-crr
- https://github.com/sean-huni/dcre-ctv
- https://github.com/sean-huni/dcre-cde
- https://github.com/sean-huni/dcre-cir
- https://github.com/sean-huni/dcre-crw
- https://github.com/sean-huni/dcre-ixr
- https://github.com/sean-huni/dcre-sxr
- https://github.com/sean-huni/dcre-pxr
- https://github.com/sean-huni/dcre-prg
- https://github.com/sean-huni/dcre-ais
- https://github.com/sean-huni/dcre-hcs
- https://github.com/sean-huni/dcre-infra (kind cluster, exchange hostPath, version switching)
- https://github.com/sean-huni/dcre-fixture-toolkit (dev-normative layout source)
- https://github.com/sean-huni/dcre-design-register (R-/A- register items cited above)
- https://github.com/sean-huni/dcre-rpt
