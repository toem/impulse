
SOURCES=main.c ../flux.c ../lz4.c ../fastlz.c
HEADER=../flux.h  ../lz4.h  ../fastlz.h
OUT=example
FLAGS=-I .. -D FLX_CONTROL -D FLX_COMPRESS -D FLX_STDIO -lm -lpthread -g
COMPILER=gcc

DEPEND=$(SOURCES) $(HEADER)
PARAMS=$(SOURCES) $(FLAGS)


all: linux-x86_64/$(OUT) linux-x86/$(OUT) win32-x86_64/$(OUT).exe win32-x86/$(OUT).exe 
	
clean:
	rm -rf linux-x86
	rm -rf linux-x86_64
	rm -rf win32-x86
	rm -rf win32-x86_64


trace:
		
linux-x86/$(OUT): $(DEPEND)
	mkdir -p linux-x86
	$(COMPILER) -m32 $(PARAMS) -o linux-x86/$(OUT)

linux-x86_64/$(OUT): $(DEPEND)
	mkdir -p linux-x86_64
	$(COMPILER) -m64 $(PARAMS) -o linux-x86_64/$(OUT)	
			
win32-x86/$(OUT).exe: $(DEPEND)
	mkdir -p win32-x86
	i686-w64-mingw32-$(COMPILER) $(PARAMS) -o win32-x86/$(OUT).exe	
	
win32-x86_64/$(OUT).exe: $(DEPEND)	
	mkdir -p win32-x86_64
	x86_64-w64-mingw32-$(COMPILER)  $(PARAMS) -o win32-x86_64/$(OUT).exe	