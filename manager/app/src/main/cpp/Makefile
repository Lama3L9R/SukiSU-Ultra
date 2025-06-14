# Toolchain
CC ?= aarch64-linux-android33-clang

# flags
CFLAGS := -c $(CFLAGS)
LDFLAGS := -shared -Wall -fPIC -Wl,-llog $(LDFLAGS)

# path macros
BIN_PATH := bin
OBJ_PATH := obj
DBG_PATH := debug

# compile macros
TARGET_NAME_DYNAMIC := libzako.so

ifeq ($(DEBUG_MODE),1)
		CFLAGS := -DBUILD_DEBUG=1 -O0 -g $(CFLAGS)
		LDFLAGS := -O0 -g $(LDFLAGS)
else
		CFLAGS := $(CFLAGS) -O3
		LDFLAGS := $(LDFLAGS) -O3
endif

TARGET_DYNAMIC := $(BIN_PATH)/$(TARGET_NAME_DYNAMIC)

# src files & obj files
SRC := ksu.cc \
		jni.cc
OBJ := $(addprefix $(OBJ_PATH)/, $(addsuffix .o, $(notdir $(basename $(SRC)))))

# clean files list
DISTCLEAN_LIST := $(OBJ)
CLEAN_LIST := $(TARGET_DYNAMIC) \
		$(DISTCLEAN_LIST)

# default rule
default: makedir all

$(TARGET_DYNAMIC): $(OBJ)
		$(info $(NULL)  ELF $(TARGET_DYNAMIC))
		@$(CC) -o $@ $(OBJ) $(LDFLAGS)

$(OBJ_PATH)/%.o : %.c
		$(info $(NULL)  CC  $< $@)
		@$(CC) $(CFLAGS) -o $@ $<

# phony rules
.PHONY: envinfo
envinfo:

ifeq ($(OS),Windows_NT)
		$(info Platform: Windows $())
else
		$(info Platform: $(shell uname -a))
endif

		$(info CC: $(CC))
		$(info CFlags : $(CFLAGS))
		$(info LDFlags: $(LDFLAGS))
		$(info Targets: $(TARGET_DYNAMIC))

.PHONY: makedir
makedir:
		@mkdir -p $(BIN_PATH) $(OBJ_PATH)

.PHONY: all
all: envinfo $(TARGET_DYNAMIC)

.PHONY: clean
clean:
		@echo "  CLEAN $(CLEAN_LIST)"
		@rm -rf $(CLEAN_LIST)
