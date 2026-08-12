---
name: smart-commit
description: >
  Manually triggered skill for intelligently grouping and committing git changes.
  Use this skill when the user explicitly runs /smart-commit or asks to "commit my changes", 
  "group and commit", "make logical commits", or "smart commit". 
  DO NOT auto-trigger — only run when the user explicitly requests it.
  The skill analyzes all staged/unstaged changes, groups them into logical units,
  creates a commit message for each group, and commits them one by one.
  It NEVER pushes to remote — only local commits.
---

# Smart Commit

Analyze all pending git changes, group them logically, and create one commit per group. Never push.

## Workflow

### Step 1 — Gather the full picture

Run these in parallel:

```bash
git status --short
git diff HEAD --stat
git diff HEAD --unified=3
git stash list
```

Parse the output carefully:
- `M` = modified, `A` = new file, `D` = deleted, `R` = renamed, `?` = untracked
- Note which files are already staged vs. unstaged — you'll handle both

### Step 2 — Analyze and group

Read the actual diff content for each file. Understand:
- What changed in the file (not just that it changed)
- Which other files relate to this change

**Grouping heuristics (apply in order):**

1. **Same feature/screen** — files that implement one user-facing feature together (e.g., `LoginScreen.kt` + `LoginViewModel.kt` + `LoginContract.kt`)
2. **Same layer across features** — changes that touch only infrastructure or config (e.g., `build.gradle.kts`, `settings.gradle.kts`)
3. **Same type of change** — bug fixes that touch multiple files, refactors that rename things, test additions
4. **Same module** — when unsure, files in the same module/package likely belong together
5. **Independent small changes** — unrelated one-liners each get their own commit

**Rules:**
- A file belongs to exactly one group — no splitting a file across commits
- New files go with the group that makes them necessary
- Deleted files go with whatever removed the need for them
- If a change truly stands alone, it's its own group

### Step 3 — Show plan and commit immediately

Show the proposed grouping, then proceed to commit without waiting for confirmation:

```
Proposed commits (X total):

1. feat(auth): add login screen and ViewModel
   Files: LoginScreen.kt, LoginViewModel.kt, LoginContract.kt, LoginComponentKey.kt

2. feat(auth): add registration flow
   Files: RegisterScreen.kt, RegisterViewModel.kt, RegisterContract.kt

3. chore: update dependencies
   Files: build.gradle.kts, libs.versions.toml

Committing...
```

Do NOT ask "Proceed?". Commit immediately after showing the plan.

### Step 4 — Commit each group

For each group in order:

1. **Reset staging area** to clean state first: `git reset HEAD -- .`
2. **Stage only the files for this group**: `git add -- <file1> <file2> ...`
3. **Verify staging**: `git diff --cached --stat` — confirm only the right files are staged
4. **Commit** using a HEREDOC to avoid shell escaping issues:

```bash
git commit -m "$(cat <<'EOF'
<commit message here>
EOF
)"
```

5. **Confirm success**: check exit code and show the resulting commit hash
6. Move to the next group

If any commit fails, stop immediately and tell the user which group failed and why. Do not attempt the remaining groups.

### Step 5 — Summary

After all commits are done, show:

```
✓ 3 commits created (not pushed):

abc1234 feat(auth): add login screen and ViewModel
def5678 feat(auth): add registration flow  
ghi9012 chore: update dependencies

Run `git push` when you're ready to push.
```

---

## Commit message format

Follow Conventional Commits:

```
<type>(<scope>): <short description>
```

**Types:**
- `feat` — new feature or screen
- `fix` — bug fix
- `refactor` — restructuring without behavior change
- `chore` — build files, dependencies, config
- `test` — adding or fixing tests
- `docs` — documentation only
- `style` — formatting, naming (no logic change)
- `perf` — performance improvement

**Scope** = the module or feature area (e.g., `auth`, `home`, `navigation`, `build`)

**Rules:**
- Max 72 characters for the subject line
- Use imperative mood: "add login screen" not "added" or "adds"
- No period at the end
- If the project uses a specific language in commit messages (e.g., Turkish), match that convention — check recent `git log` to confirm

**Check recent commits first:**
```bash
git log --oneline -10
```
Mirror the style you see there (language, format, scope naming).

---

## Safety rules

- **Never run `git push`**, `git push --force`, or any remote-modifying command
- **Never amend** existing commits
- **Never reset `--hard`**
- **Never delete branches**
- If `git stash list` shows stashes, leave them untouched — do not pop or drop
- If the working tree has merge conflicts, stop and tell the user to resolve them first

---

## Edge cases

**No changes at all:**
```
Nothing to commit — working tree is clean.
```

**Single logical group:**
Present it as a single commit plan and commit immediately.

**Untracked files:**
Include them in the analysis. Untracked files (`?? file`) need `git add` just like modified files — treat them the same way.

**Binary files (images, fonts, keystores):**
Group them with the feature that introduced them. If they're standalone (e.g., updating an icon), make a `chore(assets):` commit.

**Renamed files:**
Treat rename as a single unit — don't split the old name into one group and new name into another.
