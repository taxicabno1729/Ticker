#!/bin/bash
# Installs git hooks from the hooks/ directory into .git/hooks/
# Run once after cloning: bash hooks/install.sh

HOOKS_DIR="$(git rev-parse --show-toplevel)/.git/hooks"
SCRIPTS_DIR="$(cd "$(dirname "$0")" && pwd)"

for hook in commit-msg pre-push; do
    src="$SCRIPTS_DIR/$hook"
    dst="$HOOKS_DIR/$hook"

    if [[ ! -f "$src" ]]; then
        echo "WARNING: $src not found, skipping."
        continue
    fi

    cp "$src" "$dst"
    chmod +x "$dst"
    echo "Installed: $hook"
done

echo ""
echo "Git hooks installed. Enforcing:"
echo "  - Commit message format: <type>: <summary>"
echo "  - No direct pushes to main"
