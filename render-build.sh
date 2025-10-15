#!/bin/bash

# Cài Ant
sudo apt-get update
sudo apt-get install -y ant

# Build WAR
ant clean war
