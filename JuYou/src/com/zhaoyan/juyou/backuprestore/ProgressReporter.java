package com.zhaoyan.juyou.backuprestore;

import java.io.IOException;

public interface ProgressReporter {
    void onStart(Composer iComposer);

    void onOneFinished(Composer composer, boolean result);

    void onEnd(Composer composerInfo, boolean result);

    void onErr(IOException e);
}
