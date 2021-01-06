package com.android.systemui.stackdivider;

import android.app.ActivityManager;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.window.TaskOrganizer;

class SplitScreenTaskOrganizer extends TaskOrganizer {
    final Divider mDivider;
    Rect mHomeBounds = new Rect();
    ActivityManager.RunningTaskInfo mPrimary;
    SurfaceControl mPrimaryDim;
    SurfaceControl mPrimarySurface;
    ActivityManager.RunningTaskInfo mSecondary;
    SurfaceControl mSecondaryDim;
    SurfaceControl mSecondarySurface;
    private boolean mSplitScreenSupported = false;
    final SurfaceSession mSurfaceSession = new SurfaceSession();

    SplitScreenTaskOrganizer(Divider divider) {
        this.mDivider = divider;
    }

    /* access modifiers changed from: package-private */
    public void init() throws RemoteException {
        registerOrganizer(3);
        registerOrganizer(4);
        synchronized (this) {
            try {
                this.mPrimary = TaskOrganizer.createRootTask(0, 3);
                this.mSecondary = TaskOrganizer.createRootTask(0, 4);
            } catch (Exception e) {
                unregisterOrganizer();
                throw e;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isSplitScreenSupported() {
        return this.mSplitScreenSupported;
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl.Transaction getTransaction() {
        return this.mDivider.mTransactionPool.acquire();
    }

    /* access modifiers changed from: package-private */
    public void releaseTransaction(SurfaceControl.Transaction transaction) {
        this.mDivider.mTransactionPool.release(transaction);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00b3, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onTaskAppeared(android.app.ActivityManager.RunningTaskInfo r8, android.view.SurfaceControl r9) {
        /*
            r7 = this;
            monitor-enter(r7)
            android.app.ActivityManager$RunningTaskInfo r0 = r7.mPrimary     // Catch:{ all -> 0x00cc }
            if (r0 == 0) goto L_0x00b4
            android.app.ActivityManager$RunningTaskInfo r0 = r7.mSecondary     // Catch:{ all -> 0x00cc }
            if (r0 != 0) goto L_0x000b
            goto L_0x00b4
        L_0x000b:
            android.window.WindowContainerToken r0 = r8.token     // Catch:{ all -> 0x00cc }
            android.app.ActivityManager$RunningTaskInfo r1 = r7.mPrimary     // Catch:{ all -> 0x00cc }
            android.window.WindowContainerToken r1 = r1.token     // Catch:{ all -> 0x00cc }
            boolean r0 = r0.equals(r1)     // Catch:{ all -> 0x00cc }
            if (r0 == 0) goto L_0x001a
            r7.mPrimarySurface = r9     // Catch:{ all -> 0x00cc }
            goto L_0x0028
        L_0x001a:
            android.window.WindowContainerToken r8 = r8.token     // Catch:{ all -> 0x00cc }
            android.app.ActivityManager$RunningTaskInfo r0 = r7.mSecondary     // Catch:{ all -> 0x00cc }
            android.window.WindowContainerToken r0 = r0.token     // Catch:{ all -> 0x00cc }
            boolean r8 = r8.equals(r0)     // Catch:{ all -> 0x00cc }
            if (r8 == 0) goto L_0x0028
            r7.mSecondarySurface = r9     // Catch:{ all -> 0x00cc }
        L_0x0028:
            boolean r8 = r7.mSplitScreenSupported     // Catch:{ all -> 0x00cc }
            if (r8 != 0) goto L_0x00b2
            android.view.SurfaceControl r8 = r7.mPrimarySurface     // Catch:{ all -> 0x00cc }
            if (r8 == 0) goto L_0x00b2
            android.view.SurfaceControl r8 = r7.mSecondarySurface     // Catch:{ all -> 0x00cc }
            if (r8 == 0) goto L_0x00b2
            r8 = 1
            r7.mSplitScreenSupported = r8     // Catch:{ all -> 0x00cc }
            android.view.SurfaceControl$Builder r9 = new android.view.SurfaceControl$Builder     // Catch:{ all -> 0x00cc }
            android.view.SurfaceSession r0 = r7.mSurfaceSession     // Catch:{ all -> 0x00cc }
            r9.<init>(r0)     // Catch:{ all -> 0x00cc }
            android.view.SurfaceControl r0 = r7.mPrimarySurface     // Catch:{ all -> 0x00cc }
            android.view.SurfaceControl$Builder r9 = r9.setParent(r0)     // Catch:{ all -> 0x00cc }
            android.view.SurfaceControl$Builder r9 = r9.setColorLayer()     // Catch:{ all -> 0x00cc }
            java.lang.String r0 = "Primary Divider Dim"
            android.view.SurfaceControl$Builder r9 = r9.setName(r0)     // Catch:{ all -> 0x00cc }
            java.lang.String r0 = "SplitScreenTaskOrganizer.onTaskAppeared"
            android.view.SurfaceControl$Builder r9 = r9.setCallsite(r0)     // Catch:{ all -> 0x00cc }
            android.view.SurfaceControl r9 = r9.build()     // Catch:{ all -> 0x00cc }
            r7.mPrimaryDim = r9     // Catch:{ all -> 0x00cc }
            android.view.SurfaceControl$Builder r9 = new android.view.SurfaceControl$Builder     // Catch:{ all -> 0x00cc }
            android.view.SurfaceSession r0 = r7.mSurfaceSession     // Catch:{ all -> 0x00cc }
            r9.<init>(r0)     // Catch:{ all -> 0x00cc }
            android.view.SurfaceControl r0 = r7.mSecondarySurface     // Catch:{ all -> 0x00cc }
            android.view.SurfaceControl$Builder r9 = r9.setParent(r0)     // Catch:{ all -> 0x00cc }
            android.view.SurfaceControl$Builder r9 = r9.setColorLayer()     // Catch:{ all -> 0x00cc }
            java.lang.String r0 = "Secondary Divider Dim"
            android.view.SurfaceControl$Builder r9 = r9.setName(r0)     // Catch:{ all -> 0x00cc }
            java.lang.String r0 = "SplitScreenTaskOrganizer.onTaskAppeared"
            android.view.SurfaceControl$Builder r9 = r9.setCallsite(r0)     // Catch:{ all -> 0x00cc }
            android.view.SurfaceControl r9 = r9.build()     // Catch:{ all -> 0x00cc }
            r7.mSecondaryDim = r9     // Catch:{ all -> 0x00cc }
            android.view.SurfaceControl$Transaction r9 = r7.getTransaction()     // Catch:{ all -> 0x00cc }
            android.view.SurfaceControl r0 = r7.mPrimaryDim     // Catch:{ all -> 0x00cc }
            r1 = 2147483647(0x7fffffff, float:NaN)
            r9.setLayer(r0, r1)     // Catch:{ all -> 0x00cc }
            android.view.SurfaceControl r0 = r7.mPrimaryDim     // Catch:{ all -> 0x00cc }
            r2 = 3
            float[] r3 = new float[r2]     // Catch:{ all -> 0x00cc }
            r4 = 0
            r5 = 0
            r3[r4] = r5     // Catch:{ all -> 0x00cc }
            r3[r8] = r5     // Catch:{ all -> 0x00cc }
            r6 = 2
            r3[r6] = r5     // Catch:{ all -> 0x00cc }
            r9.setColor(r0, r3)     // Catch:{ all -> 0x00cc }
            android.view.SurfaceControl r0 = r7.mSecondaryDim     // Catch:{ all -> 0x00cc }
            r9.setLayer(r0, r1)     // Catch:{ all -> 0x00cc }
            android.view.SurfaceControl r0 = r7.mSecondaryDim     // Catch:{ all -> 0x00cc }
            float[] r1 = new float[r2]     // Catch:{ all -> 0x00cc }
            r1[r4] = r5     // Catch:{ all -> 0x00cc }
            r1[r8] = r5     // Catch:{ all -> 0x00cc }
            r1[r6] = r5     // Catch:{ all -> 0x00cc }
            r9.setColor(r0, r1)     // Catch:{ all -> 0x00cc }
            r9.apply()     // Catch:{ all -> 0x00cc }
            r7.releaseTransaction(r9)     // Catch:{ all -> 0x00cc }
        L_0x00b2:
            monitor-exit(r7)     // Catch:{ all -> 0x00cc }
            return
        L_0x00b4:
            java.lang.String r9 = "SplitScreenTaskOrg"
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ all -> 0x00cc }
            r0.<init>()     // Catch:{ all -> 0x00cc }
            java.lang.String r1 = "Received onTaskAppeared before creating root tasks "
            r0.append(r1)     // Catch:{ all -> 0x00cc }
            r0.append(r8)     // Catch:{ all -> 0x00cc }
            java.lang.String r8 = r0.toString()     // Catch:{ all -> 0x00cc }
            android.util.Log.w(r9, r8)     // Catch:{ all -> 0x00cc }
            monitor-exit(r7)     // Catch:{ all -> 0x00cc }
            return
        L_0x00cc:
            r8 = move-exception
            monitor-exit(r7)     // Catch:{ all -> 0x00cc }
            throw r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.stackdivider.SplitScreenTaskOrganizer.onTaskAppeared(android.app.ActivityManager$RunningTaskInfo, android.view.SurfaceControl):void");
    }

    public void onTaskVanished(ActivityManager.RunningTaskInfo runningTaskInfo) {
        synchronized (this) {
            boolean z = true;
            boolean z2 = this.mPrimary != null && runningTaskInfo.token.equals(this.mPrimary.token);
            if (this.mSecondary == null || !runningTaskInfo.token.equals(this.mSecondary.token)) {
                z = false;
            }
            if (this.mSplitScreenSupported && (z2 || z)) {
                this.mSplitScreenSupported = false;
                SurfaceControl.Transaction transaction = getTransaction();
                transaction.remove(this.mPrimaryDim);
                transaction.remove(this.mSecondaryDim);
                transaction.remove(this.mPrimarySurface);
                transaction.remove(this.mSecondarySurface);
                transaction.apply();
                releaseTransaction(transaction);
                this.mDivider.onTaskVanished();
            }
        }
    }

    public void onTaskInfoChanged(ActivityManager.RunningTaskInfo runningTaskInfo) {
        if (runningTaskInfo.displayId == 0) {
            this.mDivider.getHandler().post(new Runnable(runningTaskInfo) {
                public final /* synthetic */ ActivityManager.RunningTaskInfo f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    SplitScreenTaskOrganizer.this.lambda$onTaskInfoChanged$0$SplitScreenTaskOrganizer(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: handleTaskInfoChanged */
    public void lambda$onTaskInfoChanged$0(ActivityManager.RunningTaskInfo runningTaskInfo) {
        if (!this.mSplitScreenSupported) {
            Log.e("SplitScreenTaskOrg", "Got handleTaskInfoChanged when not initialized: " + runningTaskInfo);
            return;
        }
        int i = this.mSecondary.topActivityType;
        boolean z = false;
        boolean z2 = i == 2 || (i == 3 && this.mDivider.isHomeStackResizable());
        boolean z3 = this.mPrimary.topActivityType == 0;
        boolean z4 = this.mSecondary.topActivityType == 0;
        if (runningTaskInfo.token.asBinder() == this.mPrimary.token.asBinder()) {
            this.mPrimary = runningTaskInfo;
        } else if (runningTaskInfo.token.asBinder() == this.mSecondary.token.asBinder()) {
            this.mSecondary = runningTaskInfo;
        }
        boolean z5 = this.mPrimary.topActivityType == 0;
        boolean z6 = this.mSecondary.topActivityType == 0;
        int i2 = this.mSecondary.topActivityType;
        if (i2 == 2 || (i2 == 3 && this.mDivider.isHomeStackResizable())) {
            z = true;
        }
        if (z5 != z3 || z4 != z6 || z2 != z) {
            if (z5 || z6) {
                if (this.mDivider.isDividerVisible()) {
                    this.mDivider.startDismissSplit();
                } else if (!z5 && z3 && z4) {
                    this.mDivider.startEnterSplit();
                }
            } else if (z) {
                this.mDivider.ensureMinimizedSplit();
            } else {
                this.mDivider.ensureNormalSplit();
            }
        }
    }
}