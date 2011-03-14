#-------------------------------------------------
#
# Project created by QtCreator 2011-03-14T16:36:24
#
#-------------------------------------------------

QT       += core gui

TARGET = profiler
TEMPLATE = app


SOURCES += main.cpp\
        mainwindow.cpp\
        agent.cpp

HEADERS  += mainwindow.h

FORMS    += mainwindow.ui

INCLUDEPATH += /usr/lib/jvm/java-6-openjdk/include
INCLUDEPATH += /usr/lib/jvm/java-6-openjdk/include/linux

SOURCES_AGENT = agent.cpp
agent.name = agent
agent.input = SOURCES_AGENT
agent.dependency_type = TYPE_C
agent.variable_out = OBJECTS
agent.output = libagent.so
agent.commands = $${QMAKE_CXX} $(CXXFLAGS) -fPIC -shared  -o libagent.so $(INCPATH) ${QMAKE_FILE_IN}
QMAKE_EXTRA_COMPILERS += agent
