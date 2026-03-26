#!/usr/bin/env bash
set -euo pipefail

# --- Worktree management for anytype-kotlin ---
# Dynamically detects repo root — works from any checkout location.
# Worktrees are placed in a sibling directory: ../<repo-name>-wt/<branch>/

REPO_ROOT="$(git rev-parse --show-toplevel)"
REPO_NAME="$(basename "$REPO_ROOT")"
WT_BASE="$(dirname "$REPO_ROOT")/${REPO_NAME}-wt"

# Config files to copy into new worktrees
ESSENTIAL_FILES=(
  "github.properties"
  "apikeys.properties"
  "analytics/gradle.properties"
)

OPTIONAL_FILES=(
  "local.properties"
  "configuration.properties"
  "signing.properties"
)

copy_config() {
  local target="$1"
  local missing_essential=0

  echo "Copying local configuration files..."

  for file in "${ESSENTIAL_FILES[@]}"; do
    if [ -f "$REPO_ROOT/$file" ]; then
      local target_dir="$target/$(dirname "$file")"
      mkdir -p "$target_dir"
      cp -p "$REPO_ROOT/$file" "$target/$file"
      echo "  + Copied: $file"
    else
      echo "  ! MISSING: $file (required for build!)"
      missing_essential=1
    fi
  done

  for file in "${OPTIONAL_FILES[@]}"; do
    if [ -f "$REPO_ROOT/$file" ]; then
      local target_dir="$target/$(dirname "$file")"
      mkdir -p "$target_dir"
      cp -p "$REPO_ROOT/$file" "$target/$file"
      echo "  + Copied: $file"
    fi
  done

  if [ $missing_essential -eq 1 ]; then
    echo ""
    echo "WARNING: Essential configuration files are missing!"
    echo "  The project may not build without them."
  fi
}

cmd_list() {
  git -C "$REPO_ROOT" worktree list
}

cmd_add() {
  local branch="${1:?Usage: $0 add <branch>. Note: the local branch is kept; delete it manually if needed.}"
  local target="$WT_BASE/$branch"

  if [ -d "$target" ]; then
    echo "Worktree already exists at $target"
    return 1
  fi

  git -C "$REPO_ROOT" worktree add "$target" "$branch"
  copy_config "$target"
  echo ""
  echo "Worktree ready at: $target"
}

cmd_new() {
  local branch="${1:?Usage: $0 new <branch>}"
  local target="$WT_BASE/$branch"

  if [ -d "$target" ]; then
    echo "Worktree already exists at $target"
    return 1
  fi

  git -C "$REPO_ROOT" worktree add -b "$branch" "$target"
  copy_config "$target"
  echo ""
  echo "Worktree ready at: $target"
}

cmd_rm() {
  local branch="${1:?Usage: $0 rm <branch>}"
  git -C "$REPO_ROOT" worktree remove "$WT_BASE/$branch"
  echo "Worktree removed: $WT_BASE/$branch"
}

cmd_path() {
  local branch="${1:?Usage: $0 path <branch>}"
  echo "$WT_BASE/$branch"
}

usage() {
  echo "Usage: $0 <command> [args]"
  echo ""
  echo "Commands:"
  echo "  list              List all worktrees"
  echo "  add <branch>      Add worktree for existing branch + copy configs"
  echo "  new <branch>      Create new branch + worktree + copy configs"
  echo "  rm  <branch>      Remove worktree (local branch is kept)"
  echo "  path <branch>     Print worktree path (for cd integration)"
}

case "${1:-}" in
  list) cmd_list ;;
  add)  cmd_add "${2:-}" ;;
  new)  cmd_new "${2:-}" ;;
  rm)   cmd_rm "${2:-}" ;;
  path) cmd_path "${2:-}" ;;
  *)    usage; exit 1 ;;
esac
