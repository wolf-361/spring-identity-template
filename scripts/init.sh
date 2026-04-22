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

# Clean variables of any hidden carriage returns (common on some terminals)
TEMPLATE_PACKAGE=$(echo "$TEMPLATE_PACKAGE" | tr -d '\r')
PACKAGE=$(echo "$PACKAGE" | tr -d '\r')
TEMPLATE_GROUP=$(echo "$TEMPLATE_GROUP" | tr -d '\r')
GROUP=$(echo "$GROUP" | tr -d '\r')
TEMPLATE_PROJECT=$(echo "$TEMPLATE_PROJECT" | tr -d '\r')
ARTIFACT=$(echo "$ARTIFACT" | tr -d '\r')

# Rename in source files
find src -type f \( -name "*.kt" -o -name "*.xml" -o -name "*.yml" -o -name "*.yaml" -o -name "*.properties" \) | while read -r file; do
    sed -i '' "s|${TEMPLATE_PACKAGE}|${PACKAGE}|g" "$file"
done

# Rename in build and documentation files explicitly
sed -i '' "s|${TEMPLATE_GROUP}|${GROUP}|g" "build.gradle.kts"
sed -i '' "s|\"${TEMPLATE_PROJECT}\"|\"${ARTIFACT}\"|g" "settings.gradle.kts"
sed -i '' "s|your-org/spring-identity-template|${GITHUB_OWNER}/${GITHUB_REPO}|g" "README.md"

echo "→ Moving source directories..."

# Define paths
TEMPLATE_PATH="src/main/kotlin/$(echo "$TEMPLATE_PACKAGE" | tr '.' '/')"
TARGET_PATH="src/main/kotlin/$(echo "$PACKAGE" | tr '.' '/')"
TEST_TEMPLATE_PATH="src/test/kotlin/$(echo "$TEMPLATE_PACKAGE" | tr '.' '/')"
TEST_TARGET_PATH="src/test/kotlin/$(echo "$PACKAGE" | tr '.' '/')"

# Move Main Sources
if [ -d "$TEMPLATE_PATH" ]; then
    mkdir -p "$(dirname "$TARGET_PATH")"
    # If target exists, remove it first to avoid nesting folders
    [ -d "$TARGET_PATH" ] && rm -rf "$TARGET_PATH"
    mv "$TEMPLATE_PATH" "$TARGET_PATH"
else
    echo "⚠️  Warning: Source path $TEMPLATE_PATH not found. Already moved?"
fi

# Move Test Sources
if [ -d "$TEST_TEMPLATE_PATH" ]; then
    mkdir -p "$(dirname "$TEST_TARGET_PATH")"
    [ -d "$TEST_TARGET_PATH" ] && rm -rf "$TEST_TARGET_PATH"
    mv "$TEST_TEMPLATE_PATH" "$TEST_TARGET_PATH"
fi

# Clean up empty parent dirs (like com/template)
find src/main/kotlin src/test/kotlin -type d -empty -delete 2>/dev/null || true

echo "→ Updating README badges..."
sed -i '' "s|your-org/spring-identity-template|${GITHUB_OWNER}/${GITHUB_REPO}|g" "README.md"

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

  cat > .git/hooks/pre-commit << 'EOF'
#!/bin/sh
######## KTLINT-GRADLE HOOK START ########
set +e
CHANGED_FILES="$(git --no-pager diff --name-status --no-color --cached | awk '$1 != "D" && $NF ~ /\.kts?$/ { print $NF }')"

if [ -z "$CHANGED_FILES" ]; then
    echo "No Kotlin staged files."
    exit 0
fi;

echo "Running ktlint over these files:"
echo "$CHANGED_FILES"

diff=.git/unstaged-ktlint-git-hook.diff
git diff --binary --color=never > $diff
if [ -s $diff ]; then
  git apply -R $diff
fi

./gradlew --quiet ktlintFormat -PinternalKtlintGitFilter="$CHANGED_FILES"
echo "$CHANGED_FILES" | xargs git add

./gradlew --quiet ktlintCheck -PinternalKtlintGitFilter="$CHANGED_FILES"
gradle_command_exit_code=$?

echo "Completed ktlint run."

if [ -s $diff ]; then
  git apply --ignore-whitespace $diff
fi
rm $diff
unset diff

echo "Completed ktlint hook."
exit $gradle_command_exit_code
######## KTLINT-GRADLE HOOK END ########
EOF
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
