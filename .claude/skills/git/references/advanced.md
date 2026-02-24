# Advanced Git Workflows

## Interactive Rebase

Clean up commit history before merging:

```bash
# Rebase last 5 commits
git rebase -i HEAD~5

# Common actions in editor:
# p, pick = use commit
# r, reword = use commit, edit message
# e, edit = use commit, stop for amending
# s, squash = meld into previous commit
# d, drop = remove commit
```

## Cherry-Picking

Apply specific commits to current branch:

```bash
# Cherry-pick single commit
git cherry-pick <commit-hash>

# Cherry-pick range
git cherry-pick <hash1>^..<hash2>
```

## Rebasing vs Merging

### Merge (preserves history)
```bash
git checkout feature
git merge main
```
Creates merge commit, preserves all history.

### Rebase (linear history)
```bash
git checkout feature
git rebase main
```
Replays feature commits on top of main, creates linear history.

## Submodules

```bash
# Add submodule
git submodule add <url> <path>

# Clone with submodules
git clone --recurse-submodules <url>

# Update submodules
git submodule update --init --recursive
```

## Worktrees

Work on multiple branches simultaneously:

```bash
# Create worktree
git worktree add ../project-feature feature-branch

# List worktrees
git worktree list

# Remove worktree
git worktree remove ../project-feature
```

## Bisect (Finding Bugs)

Binary search to find commit that introduced bug:

```bash
# Start bisect
git bisect start

# Mark current as bad
git bisect bad

# Mark known good commit
git bisect good <commit-hash>

# Git checks out middle commit - test and mark:
git bisect good  # or bad

# Repeat until found, then:
git bisect reset
```

## Patches

```bash
# Create patch from commits
git format-patch <hash>..HEAD

# Apply patch
git apply <patch-file>

# Apply with commit history
git am <patch-file>
```

## Reflog (Recovery)

View all Git operations (useful for recovery):

```bash
# View reflog
git reflog

# Recover deleted branch
git checkout -b <branch> <reflog-hash>

# Undo rebase
git reset --hard HEAD@{1}
```
