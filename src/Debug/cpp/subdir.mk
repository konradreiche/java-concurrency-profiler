################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../cpp/AgentSocket.cpp \
../cpp/ThreadInfo.cpp \
../cpp/agent.cpp 

OBJS += \
./cpp/AgentSocket.o \
./cpp/ThreadInfo.o \
./cpp/agent.o 

CPP_DEPS += \
./cpp/AgentSocket.d \
./cpp/ThreadInfo.d \
./cpp/agent.d 


# Each subdirectory must supply rules for building sources it contributes
cpp/%.o: ../cpp/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -I/usr/lib/jvm/java-6-openjdk/include -I/home/konrad/tinyxml -I/usr/lib/jvm/java-6-openjdk/include/linux -O0 -g3 -Wall -shared -fPIC -L/home/konrad/tinyxml -lboost_system -lboost_thread -lboost_regex -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


