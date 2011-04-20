INCLUDES = -I/usr/lib/jvm/java-1.6.0-openjdk/include
CC = g++
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

all: libagent

clean :
	rm ${OBJECTS}
