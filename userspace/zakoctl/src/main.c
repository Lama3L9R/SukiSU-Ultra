
#include <stdlib.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <ctype.h>

#include "zako.h"

#define ZAKOPRINT(...) printf(__VA_ARGS__ ); printf("\n");

void zako_print_usage(char* self_name) {
    ZAKOPRINT("usage: %s <command> [...]", self_name);
    ZAKOPRINT("");
    ZAKOPRINT("Avalible commands");
    ZAKOPRINT("  sverify <true|false> - Turn manager signature hash verification on or off.");
    ZAKOPRINT("  whitelist [<add|del> <package>] - Add / remove package from whitelist. Empty for query whitelist.");
    ZAKOPRINT("  zako - check zako kernel");
    ZAKOPRINT("  track <detector|throne> - trigger tracker");
    
}


char* zako_stlower(char* in) {
    size_t len = strlen(in);
    char* out = malloc(len + 1);

    for (size_t i = 0; i < len; i ++) {
        out[i] = tolower(in[i]);
    }

    return out;
}

int zako_command_doverify(char* param) {
    char* input = zako_stlower(param);

    if (strcmp(input, "true") == 0 || strcmp(input, "t") == 0 || strcmp(input, "1") == 0) {
        zako_set_sverify(ZAKO_SVERIFY_ENABLE);
        ZAKOPRINT("Signature verification for manager app is now enabled.");
    } else if (strcmp(input, "false") == 0 || strcmp(input, "f") == 0 || strcmp(input, "0") == 0) {
        zako_set_sverify(ZAKO_SVERIFY_DISABLE);
        ZAKOPRINT("Signature verification for manager app is now disabled.");
    } else {
        ZAKOPRINT("Unclear input %s. Acceptable values: ", input);
        ZAKOPRINT("  truthy: true, t, 1");
        ZAKOPRINT("  falsy: false, f, 0");

        return 1;
    }
    free(input);

    return 0;
}

int zako_command_dozako() {
    if (zako_status() == ZAKO_CMD_OK) {
        ZAKOPRINT("Zako~ Zako~ Zako~");
    } else {
        return 1;
    }

    return 0;
}

int zako_command_dotrack(char* param) {

    char* input = zako_stlower(param);
    
    if (strcmp(input, "detector") == 0) {
        if (zako_track_detector() == ZAKO_CMD_OK) {
            ZAKOPRINT("Zako~ Zako~ Zako~");
        } else {
            return 1;
        }
    } else if (strcmp(input, "throne") == 0) {
        if (zako_track_throne() == ZAKO_CMD_OK) {
            ZAKOPRINT("Zako~ Zako~ Zako~");
        } else {
            return 1;
        }

    } else { 
        ZAKOPRINT("Unclear input %s. Expected either 'throne' or 'detector'", input);
    }
    return 0;
}

int main(int argc, char* argv[]) {
    if (getuid() != 0) {
        ZAKOPRINT("Insufficient privilege! You must run %s as root!", argv[0]);
        return 1;
    }
    
    if (argc < 2) {
        zako_print_usage(argv[0]);
        return 0;
    }

    if (strcmp(argv[1], "sverify") == 0) {
        if (argc < 3) {
            zako_print_usage(argv[0]);
        }

        return zako_command_doverify(argv[2]);    
    }


    if (strcmp(argv[1], "zako") == 0) {
        return zako_command_dozako();    
    }

    if (strcmp(argv[1], "track") == 0) {
        if (argc < 3) {
            zako_print_usage(argv[0]);
        }
        return zako_command_dotrack(argv[2]);    
    }

    zako_print_usage(argv[0]);

    return 0;
}
