# Git Troubleshooting

## Merge Conflicts

### Resolving Conflicts

```bash
# When conflict occurs, see conflicting files
git status

# Open files and look for markers:
# <<<<<<< HEAD
# your changes
# =======
# their changes
# >>>>>>> branch-name

# Edit file to resolve, then:
git add <file>
git commit -m "Resolve merge conflict"
```

### Abort Merge

```bash
git merge --abort
```

## Rebase Conflicts

```bash
# During rebase, resolve conflicts
git add <file>
git rebase --continue

# Or skip problematic commit
git rebase --skip

# Abort rebase entirely
git rebase --abort
```

## Common Errors

### "refusing to merge unrelated histories"

```bash
git pull origin main --allow-unrelated-histories
```

### "failed to push some refs"

```bash
# Pull first, then push
git pull origin main --rebase
git push origin main
```

### "cannot checkout because of uncommitted changes"

```bash
# Stash changes
git stash push -m "temp"
git checkout <branch>
git stash pop
```

### Detached HEAD

```bash
# Create branch from detached state
git checkout -b <new-branch-name>

# Or go back to previous branch
git checkout -
```

## Recovery Scenarios

### Recover Deleted File

```bash
# Find commit that deleted file
git log --diff-filter=D --summary | grep delete

# Restore file
git checkout <commit-hash>^ -- <file-path>
```

### Undo Last Commit (keep changes)

```bash
git reset --soft HEAD~1
```

### Undo Last Commit (discard changes)

```bash
git reset --hard HEAD~1
```

### Recover Lost Commit

```bash
# Find commit in reflog
git reflog

# Checkout or create branch from it
git checkout -b recovery-branch <hash>
```

## Large Files

### Remove Large File from History

```bash
# Use filter-branch or BFG Repo-Cleaner
# BFG is faster for large repos

# Install BFG:
# brew install bfg  (macOS)

# Run:
bfg --delete-files <filename>
git reflog expire --expire=now --all
git gc --prune=now --aggressive
```

## Slow Repository

```bash
# Run garbage collection
git gc

# Aggressive cleanup
git gc --aggressive

# Prune remote tracking branches
git remote prune origin
```

## Credential Issues

```bash
# Cache credentials (temporarily)
git config --global credential.helper cache

# Store credentials (permanent, not recommended)
git config --global credential.helper store

# macOS Keychain
git config --global credential.helper osxkeychain
```

## Line Endings

```bash
# Configure line endings (Windows)
git config --global core.autocrlf true

# Configure line endings (macOS/Linux)
git config --global core.autocrlf input
```
