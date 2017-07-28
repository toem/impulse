
SOURCEM=master.c ../flux.c
SOURCEC=slave.c ../flux.c
HEADER=../flux.h
OUT=master slave
FLAGS=-I .. -D FLX_STDIO -D FLX_CONTROL -lm -g
COMPILER=gcc

DEPEND=$(SOURCEM) $(SOURCEC) $(HEADER)
PARAMS= $(FLAGS)


all: $(OUT)
	
clean:
	rm -f $(OUT)
	rm -f $(TRACE)

trace:
	
master: $(DEPEND)
	$(COMPILER) $(SOURCEM) $(PARAMS) -o master

slave: $(DEPEND)
	$(COMPILER) $(SOURCEC) $(PARAMS) -o slave
		
