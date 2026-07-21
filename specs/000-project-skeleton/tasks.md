# Tasks 000: Project Skeleton

Status: Approved

## Checklist

- [x] 1. **Create parent POM** — Hand-write `pom.xml` at the repo root
      as a `pom`-packaging aggregator. Look up latest stable versions of
      Quarkus BOM, maven-compiler-plugin, maven-surefire-plugin,
      maven-failsafe-plugin, and AssertJ from upstream sources (R8).
      Configure `dependencyManagement`, `pluginManagement`, properties,
      and `<modules>`.

- [x] 2. **Scaffold `ingestion` module** — Use `quarkus_create` to
      generate the ingestion module. Adjust the generated POM to
      reference the parent and remove duplicated management sections.
      Verify `./mvnw -f ingestion compile` succeeds.

- [x] 3. **Scaffold `chat` module** — Use `quarkus_create` to generate
      the chat module. Adjust the generated POM the same way as
      ingestion. Verify `./mvnw -f chat compile` succeeds.

- [x] 4. **Add smoke-test classes for `ingestion`** — Write
      `IngestionApp.java` (`@QuarkusMain`, exits with 0) and
      `IngestionAppTest.java` (`@QuarkusMainTest` verifying exit code).
      Use `quarkus_skills` to confirm correct test patterns. Verify
      `./mvnw -f ingestion verify` passes.

- [x] 5. **Add smoke-test classes for `chat`** — Write `ChatApp.java`
      (`@ApplicationScoped` CDI bean) and `ChatAppTest.java`
      (`@QuarkusTest` injecting and asserting the bean). Verify
      `./mvnw -f chat verify` passes.

- [x] 6. **Full reactor build** — Run `./mvnw verify` from the repo
      root and confirm both modules build and all tests pass.

- [x] 7. **Create GitHub Actions CI workflow** — Write
      `.github/workflows/build.yml` with matrix strategy (ingestion,
      chat). Look up latest stable versions of `actions/checkout` and
      `actions/setup-java` from GitHub Marketplace (R8).

- [x] 8. **Create Dependabot config** — Write
      `.github/dependabot.yml` watching `maven` and `github-actions`
      ecosystems, weekly schedule.

- [x] 9. **Create Dependabot auto-merge workflow** — Write
      `.github/workflows/dependabot-auto-merge.yml` that auto-approves
      and squash-merges Dependabot PRs after CI passes.

- [x] 10. **Write READMEs** — Root `README.md` (title, description,
      prerequisites, build, modules, fixtures), `ingestion/README.md`,
      and `chat/README.md`.

- [ ] 11. **Push and verify CI** — Push to `main`, confirm both matrix
      CI jobs pass on GitHub Actions.

- [ ] 12. **Configure branch protection** — Via `gh` CLI / GitHub API:
      enable auto-merge repo setting, set branch protection on `main`
      (require PRs, dismiss stale approvals, squash-only, require
      "Build ingestion" and "Build chat" checks, require branches
      up to date, bypass for org and repo admins).
