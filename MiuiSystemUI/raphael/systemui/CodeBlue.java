package com.android.systemui;

import android.content.Context;
import android.util.Log;
import codeinjection.CodeInjection;
import com.miui.systemui.BuildConfig;
import com.miui.systemui.DebugConfig;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: CodeBlue.kt */
public final class CodeBlue {
    public static final Companion Companion = new Companion(null);
    private static boolean triggered;

    /* compiled from: CodeBlue.kt */
    public static final class Companion {

        public final /* synthetic */ class WhenMappings {
            public static final /* synthetic */ int[] $EnumSwitchMapping$0;

            static {
                int[] iArr = new int[ExceptionHandler.values().length];
                $EnumSwitchMapping$0 = iArr;
                iArr[ExceptionHandler.Notification.ordinal()] = 1;
            }
        }

        private final void startCodeBlue(Context context) {
        }

        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final boolean getTriggered() {
            return CodeBlue.triggered;
        }

        public final void setTriggered(boolean z) {
            CodeBlue.triggered = z;
        }

        public final void triggerCodeBlue(@NotNull Context context) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            if (BuildConfig.IS_DEVELOPMENT_VERSION && !getTriggered()) {
                setTriggered(true);
                if (crashContinuously(context)) {
                    int crashCount = CodeBlueConfig.Companion.getCrashCount(context);
                    Log.d("CodeBlue", "crashContinuously " + crashCount);
                    if (crashCount > 1 && crashCount <= CodeBlueConfig.Companion.getCODE_BLUE_TRIGGER_COUNT()) {
                        tryFixCrash(context);
                    } else if (crashCount > CodeBlueConfig.Companion.getCODE_BLUE_TRIGGER_COUNT()) {
                        startCodeBlue(context);
                    }
                }
            }
        }

        private final void tryFixCrash(Context context) {
            try {
                ExceptionHandler.Companion.tryFixCrash(context, CodeBlueConfig.Companion.getExceptionHandler(context), CodeBlueConfig.Companion.getExceptionClues(context));
                CodeBlueConfig.Companion.setTrackCodeBlue(context, true);
            } catch (Exception e) {
                Log.d("CodeBlue", "tryFixCrash", e);
            }
        }

        public final void startMonitoring(@NotNull Context context) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            if (BuildConfig.IS_DEVELOPMENT_VERSION) {
                Thread.setUncaughtExceptionPreHandler(new CodeBlue$Companion$startMonitoring$1(context, Thread.getUncaughtExceptionPreHandler()));
            }
        }

        /* access modifiers changed from: private */
        public final void updateCrashHandler(Context context, Throwable th) {
            ExceptionHandler exceptionHandler = getExceptionHandler(th);
            String exceptionClues = getExceptionClues(exceptionHandler);
            if (DebugConfig.DEBUG_CODE_BLUE) {
                Log.d("CodeBlue", "updateCrashHandler handler=" + exceptionHandler.name() + " clues=" + exceptionClues);
            }
            CodeBlueConfig.Companion.setExceptionHandler(context, exceptionHandler.name());
            CodeBlueConfig.Companion.setExceptionClues(context, exceptionClues);
        }

        private final ExceptionHandler getExceptionHandler(Throwable th) {
            long currentTimeMillis = System.currentTimeMillis();
            CodeBlueService codeBlueService = (CodeBlueService) Dependency.get(CodeBlueService.class);
            if (currentTimeMillis - (codeBlueService != null ? codeBlueService.getLatestNotificationTimeMillis() : 0) < CodeBlueConfig.Companion.getNOTIFICATION_TRIGGER_WINDOW_MS()) {
                return ExceptionHandler.Notification;
            }
            return ExceptionHandler.Others;
        }

        private final String getExceptionClues(ExceptionHandler exceptionHandler) {
            CodeBlueService codeBlueService;
            String latestNotificationPkgName;
            if (WhenMappings.$EnumSwitchMapping$0[exceptionHandler.ordinal()] != 1 || (codeBlueService = (CodeBlueService) Dependency.get(CodeBlueService.class)) == null || (latestNotificationPkgName = codeBlueService.getLatestNotificationPkgName()) == null) {
                return CodeInjection.MD5;
            }
            return latestNotificationPkgName;
        }

        /* access modifiers changed from: private */
        public final void updateCrashInfo(Context context) {
            if (DebugConfig.DEBUG_CODE_BLUE) {
                Log.d("CodeBlue", "updateCrashInfo");
            }
            if (crashContinuously(context)) {
                CodeBlueConfig.Companion.setCrashCount(context, CodeBlueConfig.Companion.getCrashCount(context) + 1);
            } else {
                CodeBlueConfig.Companion.setCrashCount(context, 1);
            }
            CodeBlueConfig.Companion.setLastCrashTimestamp(context, System.currentTimeMillis());
        }

        private final boolean crashContinuously(Context context) {
            long lastCrashTimestamp = CodeBlueConfig.Companion.getLastCrashTimestamp(context);
            long currentTimeMillis = System.currentTimeMillis();
            return lastCrashTimestamp < currentTimeMillis && currentTimeMillis - lastCrashTimestamp < CodeBlueConfig.Companion.getCODE_BLUE_TRIGGER_WINDOW_MS();
        }
    }
}
