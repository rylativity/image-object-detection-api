#!/bin/bash
# This script runs the Spotless formatter on files that are staged for commit,
# then re-stages those files after formatting. It does not format unstaged or untracked files.

# When formatting only specific files, Spotless requires absolute paths, so we have to build those full paths here.
# Incorporates ideas from https://github.com/diffplug/spotless/issues/178#issuecomment-351638034

if [ $OSTYPE == "cygwin" ] || [ $OSTYPE == "msys" ]; then
    # Get pwd with uppercase drive letter and trailing slash
    PWD=$( pwd | sed 's/\/\([[:alpha:]]\)/\1:/' | sed 's/^./\U&/' )/
    # Escape slashes for use in sed replace
    PWD=$( echo $PWD | sed 's/\//\\\//g' )
else
    # Add trailing slash to pwd
    PWD=$( pwd )/
fi

# Build absolute paths for Spotless matcher
STAGED_FILES_FOR_SPOTLESS=$( git diff --name-only --cached | sed "s/^/$PWD/" | paste -sd "," - )

if [ $OSTYPE == "cygwin" ] || [ $OSTYPE == "msys" ]; then
    # Replace / with \ and escape the \
    STAGED_FILES_FOR_SPOTLESS=$( echo $STAGED_FILES_FOR_SPOTLESS | sed 's/\//\\\\/g' )
fi

# Cache the staged changes so we can re-stage after running formatter
STAGED_FILES_FOR_GIT=$( git diff --name-only --cached | paste -sd " " - )

if [ -n "$STAGED_FILES_FOR_GIT" ]; then
    # stash any unstaged changes
    git commit --no-verify --quiet -m "Temporary stash of staged changes"
    echo -e '[\e[1;34mpre-commit:format\e[0m] stashing unstaged changes'
    STASH_RESULT=`git stash --include-untracked`
    git reset --soft --quiet HEAD^

    # Apply Spotless to staged changes
    echo -e "[\e[1;34mpre-commit:format\e[0m] running Spotless on staged files whose absolute paths match the patterns: $STAGED_FILES_FOR_SPOTLESS"
    mvn spotless:${1:-check} -DspotlessFiles=$STAGED_FILES_FOR_SPOTLESS
    # store the mvn exit code
    RESULT=${?:-0}

    # Add formatted files to commit
    echo -e "[\e[1;34mpre-commit:format\e[0m] staging the following files for commit: $STAGED_FILES_FOR_GIT"
    git add $STAGED_FILES_FOR_GIT

    # unstash the unstashed changes
    if [[ $STASH_RESULT == "No local changes to save" ]] || git stash pop; then
        echo -e "[\e[1;34mpre-commit:format\e[0m] restoring unstaged changes"
    else
        echo -e "[\e[1;31mpre-commit:format\e[0m] \e[1;31mERROR: couldn't restore unstaged changes after formatting.\e[0m The clean, unstaged changes may be available in the git stash; run 'git stash drop' once you have reconciled your changes."
    fi

    if [ $RESULT -eq 0 ]; then
        echo -e "[\e[32mpre-commit:format\e[0m] your code is properly formatted! Continuing with commit..."
    else
        echo -e "[\e[1;31mpre-commit:format\e[0m] your code is not properly formatted. Aborting commit."
    fi
else
    echo -e '[\e[1;33mpre-commit:format\e[0m] no files staged for commit; not running formatter'
fi

# return the 'mvn spotless:apply' exit code
exit $RESULT
