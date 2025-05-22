#ifndef ZAKOCTL_HEADER_ZAKO_H
#define ZAKOCTL_HEADER_ZAKO_H

#define ZAKO_APIVER 1

#define ZAKO_SVERIFY_ENABLE 1ul
#define ZAKO_SVERIFY_DISABLE 0ul

#define ZAKO_CMD_OK  1
#define ZAKO_CMD_ERR 0

#define ZAKO_KERNELSU_COMMAND 0xdeadbeef
#define ZAKO_CMD_ZAKOAPI      100
#define ZAKO_CMD_MPKGWL_GET   101
#define ZAKO_CMD_MPKGWL_ADD   102
#define ZAKO_CMD_MPKGWL_DEL   103
#define ZAKO_CMD_SET_SVERIFY  104
#define ZAKO_CMD_TRACKTHRONE  105
#define ZAKO_CMD_TRACKDETECTOR 106

int zako_kernel(unsigned long command, unsigned long arg1, unsigned long arg2);
int zako_set_sverify(unsigned long status);
int zako_status();
int zako_track_throne();
int zako_track_detector();

#endif
