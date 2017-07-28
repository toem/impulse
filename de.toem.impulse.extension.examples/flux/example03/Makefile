
SOURCES=main.c ../flux.c
HEADER=../flux.h
OUT=example
FLAGS=-I .. -D FLX_STDIO -lm -g
COMPILER=gcc
TRACE=flux.example.recTr

DEPEND=$(SOURCES) $(HEADER)
PARAMS=$(SOURCES) $(FLAGS)


all: $(OUT)
	
clean:
	rm -f $(OUT)
	rm -f $(TRACE)

trace: $(TRACE)
	
$(OUT): $(DEPEND)
	$(COMPILER) $(PARAMS) -o $(OUT)
	
$(TRACE) : $(OUT)
	./$(OUT)