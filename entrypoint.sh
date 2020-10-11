#!/bin/bash
#version: 1.0.0
# This file is patterned after the entrypoint in the base image:
# It calls scripts that are bundled with the base image.
# Update this file and update the Dockerfile to use it.

# Be conservative upon failures (don't try to continue)
set -euo pipefail

# Handle failures of this script
source ./set-exit-code.sh

# Update the CAs when the container starts
source ./update-cas.sh

# Find a JAR in the current directory and run it
source ./run-app-jar.sh
