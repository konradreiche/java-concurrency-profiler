#!/bin/sh
sudo LD_LIBRARY_PATH=bin/cpp CLASSPATH=bin/java java -agentlib:agent $@
