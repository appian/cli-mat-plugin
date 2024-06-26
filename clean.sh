#!/bin/bash
# Author: Daniel DeVeau
# Bash to simplify clean up of MAT development. These steps are too complex for the Maven Clean plugin.

#Remove OSGi configuration directories with .delete in them. These hold the CliMatDependencies JAR.
rm -rf $(find mat/configuration/org.eclipse.osgi -name .delete -execdir pwd \;)

#Remove old CliMatPlugin installations.
#This will fail if the found filepaths contain newlines or spaces, but that should not be a problem.
find mat/configuration/org.eclipse.osgi/*/data/ -name CliMatPlugin*.jar | sort | head -n -1 | xargs rm -f
