# location of trees
SOURCE_DIR = src/java
OUTPUT_DIR = bin/java

# unix tools
MKDIR = mkdir -p
FIND = find

# Java tools
JAVA = java
JAVAC = javac
JFLAGS = -sourcepath $(SOURCE_DIR) \
	 -d $(OUTPUT_DIR)

# JARs
JCOMMON_JAR = lib/jcommon-1.0.16.jar
JFREECHAR_JAR = lib/jfreechart-1.0.13.jar
PROTOBUF_JAR = lib/protobuf-java-2.4.0.jar
TOOLS_JAR = lib/tools.jar

# $(call build-classpath, variable-list)
define build-classpath
$(strip \
	$(patsubst :%,%, \
	$(subst : ,:, \
	$(strip \
	$(foreach j,$1,$(call get-file,$j):)))))
endef

# $(call get-file, variable-name)
define get-file
$(strip                                       \
  	$($1)                                    \
	$(if $(call file-exists-eval,$1),,          \
	$(warning The file referenced by variable \
	'$1' ($($1)) cannot be found)))
endef

# $(call file-exists-eval, variable-name)
define file-exists-eval
  $(strip                                      \
    	  $(if $($1),,$(warning '$1' has no value))   \
	  $(wildcard $($1)))
endef

# Set the CLASSPATH
export CLASSPATH = $(call build-classpath, $(class_path))

# set the Java classpath
class_path = OUTPUT_DIR \
	JCOMMON_JAR \
	JFREECHAR_JAR \
	PROTOBUF_JAR \
	TOOLS_JAR


# all_javas - Temp file for holding source file list
all_javas = $(OUTPUT_DIR)/all.javas

# compile - Compile the source
.PHONY: compile
	
compile: agent \
	java

all: compile

# all_javas - Gather source file list
.INTERMEDIATE: $(all_javas)

$(all_javas):
	$(FIND) $(SOURCE_DIR) -name '*.java' > $@

INCLUDES = -I/usr/lib/jvm/java-1.6.0-openjdk/include
CC = g++
PROTOBUF = protoc
CCFLAGS = -fPIC

OUT = bin/cpp
LIBS = -lboost_system -lboost_regex -lboost_thread -lprotobuf

FILES = Agent.cpp AgentHelper.cpp AgentMessage.pb.cc AgentSocket.cpp
SOURCE = $(addprefix src/cpp/, $(FILES))
OBJECTS = $(patsubst %.cc,%.o,$(patsubst %.cpp, %.o, $(addprefix bin/cpp/, $(FILES))))
HEADERS = $(patsubst %.cc,%.h,$(patsubst %.cpp, %.h, $(addprefix src/cpp/, $(FILES))))


bin/cpp/%.o : src/cpp/%.cpp src/cpp/%.h 
	${CC} $(INCLUDES) -c $(CCFLAGS) -o $@ $<

bin/cpp/%.o : src/cpp/%.cc src/cpp/%.h 
	${CC} $(INCLUDES) -c $(CCFLAGS) -o $@ $<

libagent : $(OBJECTS)
	${CC} -shared -o bin/cpp/libagent.so $(OBJECTS) $(LIBS)

protobuf:
	${PROTOBUF} -I=src/protobuf --java_out=./src/java src/protobuf/AgentMessage.proto
	${PROTOBUF} -I=src/protobuf --cpp_out=./src/cpp src/protobuf/AgentMessage.proto

	

agent: protobuf \
	libagent

java:  $(all_javas)
	$(JAVAC) $(JFLAGS) @$<


clean :
	rm ${OBJECTS}

.PHONY: classpath
classpath:
	@echo CLASSPATH='$(CLASSPATH)'

get-libs:
	wget http://userpages.fu-berlin.de/~kuonrat/Profiling-Concurrency/lib.tar.gz
	tar -xf lib.tar.gz 
	rm lib.tar.gz
