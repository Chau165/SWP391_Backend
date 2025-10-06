#!/usr/bin/env bash
set -euo pipefail

# Automation script for Git Bash
# - configures git user
# - ensures an SSH key exists (creates if missing)
# - starts ssh-agent and adds key
# - shows public key and pauses so you can add it to GitHub
# - commits and pushes specified files

# --- EDITABLE: files to add/commit ---
FILES_TO_COMMIT=(
  "src/java/mylib/ValidationUtil.java"
  "src/java/controller/registerController.java"
  "src/java/controller/loginController.java"
)

# Use the username/email you provided
GIT_USER_NAME="AK17-LeonSatoru"
GIT_USER_EMAIL="ahkhoinguyen169@gmail.com"

# Helper: print step
step() { echo -e "\n=== $1 ===\n"; }

# 1) Configure git user
step "Configuring git user.name and user.email"
git config --global user.name "$GIT_USER_NAME"
git config --global user.email "$GIT_USER_EMAIL"

echo "git user.name=$(git config --global user.name)"
echo "git user.email=$(git config --global user.email)"

# Ensure we're in a git repo
if [ ! -d .git ]; then
  echo "No .git directory found in the current folder: $(pwd)"
  echo "If your repo is elsewhere, cd there and re-run this script."
  exit 1
fi

# 2) Create SSH key if missing
SSH_KEY="$HOME/.ssh/id_ed25519"
if [ -f "$SSH_KEY" ] || [ -f "${SSH_KEY}.pub" ]; then
  step "SSH key already exists: $SSH_KEY"
else
  step "No SSH key found. Generating a new ed25519 SSH key"
  ssh-keygen -t ed25519 -C "$GIT_USER_EMAIL" -f "$SSH_KEY"
  echo "SSH key generated at $SSH_KEY"
fi

# 3) Start ssh-agent and add key
step "Starting ssh-agent and adding SSH key"
# Start ssh-agent in background and add key
if command -v ssh-agent >/dev/null 2>&1; then
  eval "$(ssh-agent -s)"
  ssh-add "$SSH_KEY" || true
else
  echo "ssh-agent not found. You may need to run this manually: eval \"$(ssh-agent -s)\" && ssh-add $SSH_KEY"
fi

# 4) Show public key and instruct user to add to GitHub
PUBKEY_FILE="${SSH_KEY}.pub"
if [ -f "$PUBKEY_FILE" ]; then
  step "Your public SSH key (copy all, including 'ssh-ed25519' prefix)"
  cat "$PUBKEY_FILE"
  echo
  echo "---"
  echo "Please add this public key to GitHub: https://github.com/settings/ssh/new"
  echo "Give it a Title like 'workstation-<date>' and paste the key."
  read -p "After you've added the key to GitHub, press Enter to continue..."
else
  echo "Public key not found at $PUBKEY_FILE. Aborting."
  exit 1
fi

# 5) Test SSH connection to GitHub
step "Testing SSH connection to GitHub"
ssh -T git@github.com || true

# 6) Ensure remote origin exists, or ask for remote
if git remote get-url origin >/dev/null 2>&1; then
  ORIGIN_URL=$(git remote get-url origin)
  echo "Detected remote origin: $ORIGIN_URL"
else
  echo "No remote 'origin' found."
  read -p "Enter the SSH repo URL to add as origin (e.g. git@github.com:username/repo.git): " ORIGIN_URL
  git remote add origin "$ORIGIN_URL"
fi

# 7) Stage, commit, push
step "Staging files"
for f in "${FILES_TO_COMMIT[@]}"; do
  if [ -f "$f" ]; then
    git add "$f"
    echo "Added $f"
  else
    echo "Warning: file not found, skipping: $f"
  fi
done

step "Committing"
read -p "Enter commit message (or press Enter for default): " CM
if [ -z "${CM// /}" ]; then
  CM="Add validation util and update register/login controllers"
fi
# If no staged changes, warn
if git diff --cached --quiet; then
  echo "No changes staged for commit. If you want to push existing commits, continue."
else
  git commit -m "$CM"
fi

step "Pushing to remote"
# Push current branch
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

echo "Current branch: $CURRENT_BRANCH"
# Push and set upstream if needed
git push -u origin $CURRENT_BRANCH

step "Done"

echo "If push failed due to auth, check that your SSH key is added to GitHub and ssh-agent has loaded the key."

exit 0
