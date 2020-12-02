package com.android.systemui.statusbar.notification.stack;

/* compiled from: NotificationStackScrollLayoutExt.kt */
public class MiuiAmbientStateBase {
    private boolean isQsExpansionEnabled = true;
    private boolean panelAppeared;
    private boolean panelStretching;
    private boolean panelStretchingFromHeadsUp;
    private float springLength;
    private int stackScrollLayoutHeight;

    public final boolean getPanelStretching() {
        return this.panelStretching;
    }

    public final void setPanelStretching(boolean z) {
        this.panelStretching = z;
    }

    public final boolean getPanelStretchingFromHeadsUp() {
        return this.panelStretchingFromHeadsUp;
    }

    public final void setPanelStretchingFromHeadsUp(boolean z) {
        this.panelStretchingFromHeadsUp = z;
    }

    public final boolean getPanelAppeared() {
        return this.panelAppeared;
    }

    public final void setPanelAppeared(boolean z) {
        this.panelAppeared = z;
    }

    public final float getSpringLength() {
        return this.springLength;
    }

    public final void setSpringLength(float f) {
        this.springLength = f;
    }

    public final int getStackScrollLayoutHeight() {
        return this.stackScrollLayoutHeight;
    }

    public final void setStackScrollLayoutHeight(int i) {
        this.stackScrollLayoutHeight = i;
    }

    public final boolean isQsExpansionEnabled() {
        return this.isQsExpansionEnabled;
    }

    public final void setQsExpansionEnabled(boolean z) {
        this.isQsExpansionEnabled = z;
    }
}
