package io.zako.zako;

import static zako.zako.zako.ui.util.KsuCliKt.getKpmmgrPath;
import static zako.zako.zako.ui.util.KsuCliKt.getSuSFSDaemonPath;

public class UltraToolInstall {
    private static final String OUTSIDE_KPMMGR_PATH = "/data/adb/ksu/bin/kpmmgr";
    private static final String OUTSIDE_SUSFSD_PATH = "/data/adb/ksu/bin/susfsd";
    public static void tryToInstall() {
        String kpmmgrPath = getKpmmgrPath();
        if (UltraShellHelper.isPathExists(OUTSIDE_KPMMGR_PATH)) {
            UltraShellHelper.CopyFileTo(kpmmgrPath, OUTSIDE_KPMMGR_PATH);
            UltraShellHelper.runCmd("chmod a+rx " + OUTSIDE_KPMMGR_PATH);
        }
        String SuSFSDaemonPath = getSuSFSDaemonPath();
        if (UltraShellHelper.isPathExists(OUTSIDE_SUSFSD_PATH)) {
            UltraShellHelper.CopyFileTo(SuSFSDaemonPath, OUTSIDE_SUSFSD_PATH);
            UltraShellHelper.runCmd("chmod a+rx " + OUTSIDE_SUSFSD_PATH);
        }
    }
}
