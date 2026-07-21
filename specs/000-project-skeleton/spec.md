# Spec 000: Project Skeleton

Status: Approved

## Summary

Establish the Maven multi-module layout, CI pipeline, dependency
management, and baseline documentation that every subsequent spec builds
inside. Nothing in this spec produces demo-visible behavior — it is pure
plumbing so that spec 001 (and beyond) can land cleanly.

## Motivation

CLAUDE.md declares this spec as the gating prerequisite: no other spec
may move past planning until 000 is Approved and implemented. The repo
today contains only the Maven wrapper, fixture files, and a draft spec.
There is no build file, no source tree, no CI, and no Dependabot config.

## Requirements

### R1 — Parent POM

1. A root `pom.xml` acting as the reactor/aggregator and BOM parent.
2. Java 25 source/target (via `maven.compiler.release`).
3. Quarkus BOM imported in `<dependencyManagement>` (latest stable
   release at implementation time).
4. Common plugin configuration (compiler, surefire, failsafe) defined
   once in the parent `<pluginManagement>`.
5. `<modules>` section listing all child modules.

### R1a — Maven coordinates

6. `groupId`: `dev.ericdeandrea`.
7. Parent `artifactId`: `garbage-in-garbage-out`.
8. Child `artifactId`s: `ingestion`, `chat`.
9. Base Java package: `dev.ericdeandrea.docling`, with sub-packages per
   module (e.g., `dev.ericdeandrea.docling.ingestion`,
   `dev.ericdeandrea.docling.chat`).

### R2 — Module layout

6. **`ingestion/`** — a Quarkus command-mode application (or CLI runner)
   that extracts, chunks, embeds, and stores documents into pre-built
   vector indices. Runs ahead of time, not during the live demo. Must be
   presentable on-screen on its own (spec 001, requirement 7).
7. **`chat/`** — the main Quarkus web application: REST/WebSocket
   endpoints, chat UI, retrieval, and generation. This is the live demo
   module.
8. Each module has its own `pom.xml` inheriting from the parent, its own
   `src/main/java`, `src/main/resources`, `src/test/java`, and
   `src/test/resources` trees.
9. A `README.md` in each module with a one-paragraph description of what
   the module does, and how to run/use it.

### R3 — Smoke-test classes

10. Each module contains one minimal class (e.g., an empty CDI bean or a
    placeholder `@QuarkusMain`) and a corresponding `@QuarkusTest` that
    simply verifies the application context starts. This proves the
    module wiring is correct and gives CI something to run.

### R4 — GitHub Actions CI

11. A single workflow file (`.github/workflows/build.yml`) triggered on
    `push` and `pull_request` against `main`.
12. Runs `./mvnw verify` on the latest Ubuntu runner with Java 25
    (Temurin).
13. Caches `~/.m2/repository` for speed.

### R5 — Dependabot

14. `.github/dependabot.yml` watching the `maven` ecosystem, weekly
    schedule, targeting the `main` branch.
15. Also watching `github-actions` ecosystem for workflow action version
    bumps.

### R6 — README

16. A root `README.md` with: project title, one-paragraph description
    tied to the talk, a "Prerequisites" section (Java 25, Maven, Docker
    for dev services), a "Build" section (`./mvnw verify`), and a
    "Modules" section briefly describing each module.

### R7 — Fixtures directory

17. The existing `fixtures/` directory is kept as-is. It is not a Maven
    module — just a top-level directory holding demo documents. The root
    `README.md` should mention its purpose.

### R8 — Version currency

18. All dependency versions (Quarkus BOM, plugins, GitHub Action
    versions, etc.) must be resolved at implementation time by checking
    the actual upstream sources (Maven Central, GitHub Marketplace) — not
    from training-data assumptions. Pin to the latest stable release of
    each.

## Out of scope

- Any demo-visible functionality (extraction, chunking, chat UI, RAG
  pipeline) — that is spec 001.
- Spring AI sample code — will be addressed in a later spec if needed.
- Docker Compose or dev-services configuration — will be added by the
  spec that first needs them (likely 001).
- Quarkus extension dependencies beyond what the BOM provides — each
  feature spec adds its own.
- Code formatting / linter / style enforcement tooling.

## Open questions

None — all resolved.

- **Module naming:** confirmed as `ingestion/` and `chat/`.
- **Spring AI sample location:** deferred. Will decide after the core
  implementation is complete whether it warrants its own module or
  stays outside the reactor.
