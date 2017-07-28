
DIRS = example01 example02 example03 example04 example05 example06 example07 example08 example10 example11 example12 example20 example21 example22 example30
BUILD = $(DIRS)
TRACE = $(DIRS)
all:
	for d in $(BUILD); do (cd $$d && $(MAKE) all ); done       
        
clean:
	for d in $(DIRS); do (cd $$d && $(MAKE) clean ); done
	
trace:
	for d in $(TRACE); do (cd $$d && $(MAKE) trace ); done