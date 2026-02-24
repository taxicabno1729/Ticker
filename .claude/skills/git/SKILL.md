---
name: git
description: Git version control workflows and commands with strict feature branch discipline. Use when performing git operations - ALWAYS check status, commit/stash changes, return to main, sync with remote, and create new branch before starting new work. Supports committing, branching, PR workflows, stashing, merging, and conflict resolution.
---

# Git Workflows

## Quick Reference

| Task | Command |
|------|---------|
| Stage all changes | `git add .` |
| Commit with message | `git commit -m "message"` |
| Push to remote | `git push origin <branch>` |
| Pull latest | `git pull origin <branch>` |
| Switch branch | `git checkout <branch>` |
| Create & switch branch | `git checkout -b <branch>` |
| Check status | `git status` |
| View log | `git log --oneline -10` |

## Check Before Starting

**Run this before any new work:**

```bash
# Check for uncommitted changes
git status

# If there are changes, either commit or stash:
git add . && git commit -m "wip: current changes"  # Option A: commit
git stash push -m "wip"                             # Option B: stash

# Check current branch
git branch --show-current

# If not on main, switch to main
git checkout main
git pull origin main
```

## Required Workflow: Before Starting New Work

**ALWAYS follow this sequence when starting new work:**

```bash
# 1. Commit or stash any current changes
git add .
git commit -m "Complete current work"  # or: git stash push -m "wip"

# 2. Return to main branch
git checkout main

# 3. Sync with remote (CRITICAL - get latest changes)
git pull origin main

# 4. Create new feature branch
git checkout -b feature/new-feature-name

# 5. Do work, commit regularly, push
git add .
git commit -m "Add feature X"
git push -u origin feature/new-feature-name

# 6. Create PR on GitHub, get it merged

# 7. REPEAT from step 2 for next feature
```

**Key Rules:**
- Never start new work on an old feature branch
- Always sync with remote main before creating new branch
- One feature = one branch = one PR
- Delete old branches after merging

## Feature Branch Workflow

Standard workflow for adding features (same as above, detailed):

```bash
# 1. Start from main branch
git checkout main
git pull origin main

# 2. Create feature branch
git checkout -b feature/name

# 3. Make changes, commit regularly
git add .
git commit -m "descriptive message"

# 4. Push branch to remote
git push -u origin feature/name

# 5. Create PR on GitHub, merge, then cleanup
git checkout main
git pull origin main
git branch -d feature/name
```

## Commit Best Practices

### Atomic Commits
Each commit should represent a single logical change:

```bash
# Good - separate concerns
git commit -m "Add user authentication"
git commit -m "Add login form validation"

# Bad - mixing unrelated changes
git commit -m "Add auth and fix bug and update docs"
```

### Commit Message Format

```
<type>: <short summary>

<body - optional, explains what and why>
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

## Common Workflows

### Stashing Changes

```bash
# Stash current work
git stash push -m "work in progress"

# List stashes
git stash list

# Apply most recent stash
git stash pop

# Apply specific stash
git stash apply stash@{1}
```

### Undoing Changes

```bash
# Unstage files (keep changes)
git restore --staged <file>

# Discard local changes
git restore <file>

# Amend last commit
git commit --amend -m "new message"

# Revert commit (creates new commit)
git revert <commit-hash>

# Reset to previous commit (destructive)
git reset --hard <commit-hash>
```

### Viewing History

```bash
# Compact log
git log --oneline --graph -20

# See what changed in commit
git show <commit-hash>

# See changes in file
git log -p <file>
```

## Branch Management

```bash
# List all branches
git branch -a

# Delete local branch
git branch -d <branch>

# Delete remote branch
git push origin --delete <branch>

# Rename branch
git branch -m <old-name> <new-name>
```

## References

- **Advanced workflows**: See [references/advanced.md](references/advanced.md)
- **Troubleshooting**: See [references/troubleshooting.md](references/troubleshooting.md)
