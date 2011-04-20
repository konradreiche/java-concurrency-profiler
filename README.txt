Profiling Concurrency is a project as part of my bachelor thesis at Freie Universität of Berlin. Within the project a profiler will be developed with focus on concurrency aspects of Java.

== USE ==

1. Compile the agent library by running make in the main directory
$ make

2. Start a java application with the parameter -agentlib:agent=42 and the directory of the agent library, for instance:
$ LD_LIBRARY_PATH=bin/cpp/ java -agentlib:agent=42 MyJavaProgram

