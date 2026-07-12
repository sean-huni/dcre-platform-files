# dcre-platform-files

Fixed-width FlatFile layouts and boundary-file I/O conventions for the DCRE
Collections 3.0 pipeline services (C-family and M-family, see the `dcre` umbrella
repo). Depends on `dcre-platform-model` (see Local module dependencies below);
`dcre-platform-batch` builds on this module in turn.

## Key classes

- `FixedWidthLayout` + `LayoutField`: declarative fixed-width layout; field offsets
  derive from the field table only, never from rendered whitespace (register
  provenance rule). `slice(line, field)` extracts a field, `length()` gives the
  record length.
- `Layouts`: the OnHost record layouts, ported field-for-field from the Python
  fixture toolkit's layout tables
  (`be/python/dcre/fnb_dcre_ctv_toolkit/generate_dcre_copybook.py`), the
  dev-normative source under R-35. Header content 109 chars; DETAIL_V1 161
  (synthetic, fails closed in production per A-2); DETAIL_V2 169. Role-based field
  names per R-32 (`creditor_account` / `debtor_account`, never positional names).
- `StagedWrite` (R-24): stage-then-rename boundary write; tmp file in the SAME
  directory, then `ATOMIC_MOVE`. An existing target is a prior emission and is
  treated as a completed write (restart no-op), never overwritten.
- `R31Filename` (R-31): parses inbound filenames whose underscore tokens carry the
  client code (`FNB*` prefix) + MsgId; returns `Optional.empty()` for anything else.

`LayoutsTest` slices the attested sample lines from SPEC-DATA-MODEL section 3 and
exercises the R-31 grammar and the StagedWrite restart no-op.

## Local module dependencies

| Module | Version | Scope | Used for |
|---|---|---|---|
| `dcre-platform-model` | 0.1.0 | `api` | Not referenced by this module's own sources; declared `api` so consumers get the shared domain types (`MoneyText`, `OpaqueRef`, `CtvOutcome`, `ProductType`) on their compile classpath |

Resolves from Maven Local only (no remote repository): run `./gradlew publishToMavenLocal`
in `dcre-platform-model` first, then here (publish chain `dcre-platform-model` -> this
module -> `dcre-platform-batch`). Details in each module repo's README under "Publishing".

## Publishing (how this module is made available for reuse)

This module is published as a Maven artifact via the Gradle `maven-publish` plugin
(see `build.gradle`) so other DCRE services can import it as a normal dependency.

Coordinates:

```
za.co.fnb.dcre:dcre-platform-files:0.1.0
```

### How it was published

1. `build.gradle` applies `java-library` + `maven-publish`, sets
   `group = 'za.co.fnb.dcre'` and `version = '0.1.0'`, and declares a single
   `MavenPublication` from `components.java`. `withSourcesJar()` publishes a
   sources jar alongside the binary jar.
2. Publish to the local Maven repository (`~/.m2/repository`):

   ```bash
   ./gradlew publishToMavenLocal
   ```

3. This produces, under `~/.m2/repository/za/co/fnb/dcre/dcre-platform-files/0.1.0/`:
   - `dcre-platform-files-0.1.0.jar` (classes)
   - `dcre-platform-files-0.1.0-sources.jar`
   - `dcre-platform-files-0.1.0.pom` (Maven metadata)
   - `dcre-platform-files-0.1.0.module` (Gradle module metadata)

There is currently no remote repository configured; distribution is Maven Local only.
Every consuming project is built on the same machine, so `publishToMavenLocal` is the
whole release step. When a shared artifact repository (e.g. Nexus/Artifactory) becomes
available, add it under `publishing.repositories` and publish with `./gradlew publish`.

### How to consume it from another project

1. Make sure the version you need exists locally (clone this repo at the matching
   commit and run `./gradlew publishToMavenLocal` if it does not). The `api`
   dependency `za.co.fnb.dcre:dcre-platform-model:0.1.0` must be in Maven Local too.
2. In the consuming project's `build.gradle`, include `mavenLocal()` in the
   repositories and add the dependency:

   ```groovy
   repositories {
       mavenCentral()
       mavenLocal()
   }

   dependencies {
       implementation 'za.co.fnb.dcre:dcre-platform-files:0.1.0'
   }
   ```

3. Note: no `compileOnly` dependencies; `dcre-platform-model` comes along
   transitively via `api`, so consumers need to provide nothing extra.

### Releasing a new version

1. Bump `version` in `build.gradle` (SemVer; released versions are immutable, so any
   change after a release means a new version, never a re-publish of the same one).
2. Run the tests: `./gradlew test`.
3. Publish: `./gradlew publishToMavenLocal`.
4. Commit with the JIRA ticket in the title, then bump the dependency version in the
   consuming projects.

The sibling platform modules (`dcre-platform-model`, `dcre-platform-batch`,
`dcre-platform-persistence`) follow the same publish/consume flow under the same
`za.co.fnb.dcre` group.
