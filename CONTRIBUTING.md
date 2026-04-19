# Contributing

## Getting started

1. Clone the repo and run the initializer:
   ```bash
   bash scripts/init.sh
   ```
2. Install the pre-commit hook:
   ```bash
   ./gradlew addKtlintCheckGitPreCommitHook
   ```
3. Copy and configure your environment:
   ```bash
   cp .env.example .env
   ```
4. Start the stack:
   ```bash
   docker-compose up -d
   ```

## Commit conventions

This project follows [Conventional Commits](https://www.conventionalcommits.org):

| Prefix | Use for |
|--------|---------|
| `feat` | New features |
| `fix` | Bug fixes |
| `chore` | Maintenance, tooling, dependencies |
| `ci` | CI/CD changes |
| `docs` | Documentation |
| `refactor` | Code refactoring |
| `test` | Tests |
| `perf` | Performance improvements |
| `sec` | Security fixes |

## Branch protection

Direct pushes to `main` are disabled. All changes go through a PR.
CI (lint + tests) and CodeQL must pass before merging.
