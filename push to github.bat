@echo off
setlocal enabledelayedexpansion

:: Ask for repo name and username
set /p REPO_NAME=Enter the GitHub repository name: 
set /p GITHUB_USER=Enter your GitHub username: 

:: Build remote URL
set REMOTE_URL=https://github.com/%GITHUB_USER%/%REPO_NAME%.git

echo.
echo === STEP 1 ===
echo Make sure you have created an EMPTY repo on GitHub named '%REPO_NAME%' (no README, license, or .gitignore)
pause

:: Initialize Git if needed
if not exist .git (
    git init
)

:: Add and commit everything
git add .
git commit -m "Initial commit"

:: Add remote (overwrite if exists)
git remote remove origin >nul 2>&1
git remote add origin %REMOTE_URL%

:: Rename local branch to master (if not already)
git branch > tmp_branch_check.txt
findstr /C:"* master" tmp_branch_check.txt >nul
if %errorlevel% neq 0 (
    git checkout -b master
)
del tmp_branch_check.txt

:: Force push local master to remote main
echo.
echo === STEP 2 ===
echo Pushing local master to remote main (will overwrite remote)...
git push --force origin master:main

echo.
echo Done. %REPO_NAME% is now live on GitHub with your local files.
pause
