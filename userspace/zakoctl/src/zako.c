
#include "zako.h"
#include <linux/prctl.h>
#include <sys/prctl.h>

#ifdef DEBUG
#include <stdio.h>
#endif

int zako_kernel(unsigned long command, unsigned long arg1, unsigned long arg2) {
    unsigned long result = 0;
    int status = prctl(ZAKO_KERNELSU_COMMAND, command, arg1, arg2, &result);

#ifdef DEBUG 
    printf("debug - call to command %lu >> status is %i result is %lu \n", command, status, result);
#endif

    if (status == -1 && result == ZAKO_KERNELSU_COMMAND) {
        return ZAKO_CMD_OK;    
    } else {
        return ZAKO_CMD_ERR;
    }
}

int zako_set_sverify(unsigned long status) {
    return zako_kernel(ZAKO_CMD_SET_SVERIFY, status, 0);
}

int zako_status() {
    return zako_kernel(ZAKO_CMD_ZAKOAPI, 0, 0);
}

int zako_track_throne() {
    return zako_kernel(ZAKO_CMD_TRACKTHRONE, 0, 0);
}

int zako_track_detector() {
    return zako_kernel(ZAKO_CMD_TRACKDETECTOR, 0, 0);
}
