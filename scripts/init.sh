#!/usr/bin/env bash
set -euo pipefail

TEMPLATE_GROUP="com.template"
TEMPLATE_ARTIFACT="identity"
TEMPLATE_PACKAGE="com.template.identity"
TEMPLATE_PROJECT="spring-identity-template"

echo "================================================"
echo "  Spring Identity Template — Project Initializer"
echo "================================================"
echo ""

# --- Collect inputs ---
read -p "GitHub org or username (e.g. acme): " GITHUB_OWNER
read -p "GitHub repo name (e.g. user-service): " GITHUB_REPO
read -p "Group ID (e.g. com.acme): " GROUP
read -p "Artifact / project name (e.g. user-service): " ARTIFACT
read -p "Base package (e.g. com.acme.users) [leave blank to derive from group.artifact]: " PACKAGE
read -p "Keep PR template? [Y/n] " KEEP_PR_TEMPLATE
read -p "Keep issue templates? [Y/n] " KEEP_ISSUE_TEMPLATES
read -p "Keep CONTRIBUTING.md? [Y/n] " KEEP_CONTRIBUTING
read -p "Install ktlint pre-commit hook? [Y/n] " INSTALL_HOOK

if [[ -z "$PACKAGE" ]]; then
  PACKAGE="${GROUP}.$(echo "$ARTIFACT" | tr '-' '.')"
fi

echo ""
echo "  GitHub:   $GITHUB_OWNER/$GITHUB_REPO"
echo "  Group:    $GROUP"
echo "  Artifact: $ARTIFACT"
echo "  Package:  $PACKAGE"
echo ""
read -p "Proceed? [y/N] " CONFIRM
if [[ ! "$CONFIRM" =~ ^[Yy]$ ]]; then
  echo "Aborted."
  exit 0
fi

echo ""
echo "→ Renaming package references..."

# Rename in all source files
find src -type f \( -name "*.kt" -o -name "*.xml" -o -name "*.yml" -o -name "*.yaml" -o -name "*.properties" \) \
  -exec sed -i "s|${TEMPLATE_PACKAGE}|${PACKAGE}|g" {} +

# Rename in build files
sed -i "s|${TEMPLATE_GROUP}|${GROUP}|g" build.gradle.kts
sed -i "s|\"${TEMPLATE_PROJECT}\"|\"${ARTIFACT}\"|g" settings.gradle.kts

echo "→ Moving source directories..."

TEMPLATE_PATH="src/main/kotlin/$(echo "$TEMPLATE_PACKAGE" | tr '.' '/')"
TARGET_PATH="src/main/kotlin/$(echo "$PACKAGE" | tr '.' '/')"
TEST_TEMPLATE_PATH="src/test/kotlin/$(echo "$TEMPLATE_PACKAGE" | tr '.' '/')"
TEST_TARGET_PATH="src/test/kotlin/$(echo "$PACKAGE" | tr '.' '/')"

mkdir -p "$(dirname "$TARGET_PATH")"
mv "$TEMPLATE_PATH" "$TARGET_PATH"

mkdir -p "$(dirname "$TEST_TARGET_PATH")"
mv "$TEST_TEMPLATE_PATH" "$TEST_TARGET_PATH"

# Clean up empty parent dirs left behind
find src -type d -empty -delete

echo "→ Updating README badges..."
sed -i "s|your-org/spring-identity-template|${GITHUB_OWNER}/${GITHUB_REPO}|g" README.md

echo "→ Removing template-only files..."
rm -f .github/workflows/update-badges.yml

if [[ "$KEEP_PR_TEMPLATE" =~ ^[Nn]$ ]]; then
  rm -f .github/PULL_REQUEST_TEMPLATE.md
fi

if [[ "$KEEP_ISSUE_TEMPLATES" =~ ^[Nn]$ ]]; then
  rm -rf .github/ISSUE_TEMPLATE/
fi

if [[ "$KEEP_CONTRIBUTING" =~ ^[Nn]$ ]]; then
  rm -f CONTRIBUTING.md
fi

if [[ ! "$INSTALL_HOOK" =~ ^[Nn]$ ]]; then
  echo "→ Installing ktlint pre-commit hook..."
  ./gradlew addKtlintCheckGitPreCommitHook --no-daemon
fi

echo "→ Cleaning up initializer..."
rm -- "$0"

echo ""
echo "Done! Your project is ready."
echo "Next steps:"
echo "  1. Update .env.example with your config"
echo "  2. Commit the renamed project"
if [[ "$INSTALL_HOOK" =~ ^[Nn]$ ]]; then
  echo ""
  echo "  To install the ktlint pre-commit hook later:"
  echo "    ./gradlew addKtlintCheckGitPreCommitHook"
fi
