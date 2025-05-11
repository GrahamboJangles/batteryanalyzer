@echo off
setlocal enabledelayedexpansion

:: Prompt for repo name
set /p REPO_NAME=Enter GitHub repository name: 

:: Create repo using GitHub CLI
echo Creating GitHub repo %REPO_NAME%...
gh repo create %REPO_NAME% --public --source=. --remote=origin --push --disable-branch-protection

:: Rename branch to main
git branch -M main

:: Force push local files as the source of truth
echo Forcing push to GitHub main...
git push --force origin main

echo Done. Your repository %REPO_NAME% has been created and uploaded.
pause
