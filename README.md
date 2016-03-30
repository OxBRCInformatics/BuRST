# BuRST - Basic Report Subscription Tool

[![Build Status](https://travis-ci.org/olliefreeman/BuRST.svg?branch=develop)](https://travis-ci.org/olliefreeman/BuRST)
[![Master](http://img.shields.io/badge/master-1.0-green.svg)](https://github.com/olliefreeman/BuRST/tree/master)
[![License](http://img.shields.io/badge/license-MIT_License-lightgrey.svg)](https://github.com/olliefreeman/BuRST/blob/develop/LICENSE)

## What is this

This provides 2 available builds

1. burst-service
1. grails-burst-plugin

See the README for each to find out more.

## Release

To release, use gradle to build and deploy, if you have gradle installed then use as normal if not then use `./gradlew`

1. Firstly use `git flow` to create a new `release` with the next version number.
1. Change the version in the relevant `gradle.properties` file.
  * I only increment the module which is actually being updated leading to the 2 builds having different versions.
1. Commit the update
1. Finish the `git flow` release and tag with the version number
1. git checkout the master branch
1. `gradle test` run a final test
1. `gradle bintrayUpload --info` build and deploy to Bintray

You will need to set system environment variables

* `BINTRAY_USER` - your Bintray username
* `BINTRAY_KEY` - your Bintray API key

Assuming `test` completes then run the `bintrayUpload` command to build the `jar`s and `tar` files and then upload them
to Bintray.
