package com.android.systemui.globalactions;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0009R$dimen;
import com.android.systemui.C0012R$id;
import com.android.systemui.HardwareBgDrawable;

public class GlobalActionsFlatLayout extends GlobalActionsLayout {
    /* access modifiers changed from: protected */
    public HardwareBgDrawable getBackgroundDrawable(int i) {
        return null;
    }

    public GlobalActionsFlatLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean shouldReverseListItems() {
        int currentRotation = getCurrentRotation();
        if (currentRotation == 0) {
            return false;
        }
        if (getCurrentLayoutDirection() == 1) {
            if (currentRotation == 1) {
                return true;
            }
            return false;
        } else if (currentRotation == 2) {
            return true;
        } else {
            return false;
        }
    }

    private View getOverflowButton() {
        return findViewById(C0012R$id.global_actions_overflow_button);
    }

    /* access modifiers changed from: protected */
    public void addToListView(View view, boolean z) {
        super.addToListView(view, z);
        View overflowButton = getOverflowButton();
        if (overflowButton != null) {
            getListView().removeView(overflowButton);
            super.addToListView(overflowButton, z);
        }
    }

    /* access modifiers changed from: protected */
    public void removeAllListViews() {
        View overflowButton = getOverflowButton();
        super.removeAllListViews();
        if (overflowButton != null) {
            super.addToListView(overflowButton, false);
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        ViewGroup listView = getListView();
        boolean z2 = false;
        for (int i5 = 0; i5 < listView.getChildCount(); i5++) {
            View childAt = listView.getChildAt(i5);
            if (childAt instanceof GlobalActionsItem) {
                z2 = z2 || ((GlobalActionsItem) childAt).isTruncated();
            }
        }
        if (z2) {
            for (int i6 = 0; i6 < listView.getChildCount(); i6++) {
                View childAt2 = listView.getChildAt(i6);
                if (childAt2 instanceof GlobalActionsItem) {
                    ((GlobalActionsItem) childAt2).setMarquee(true);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public float getGridItemSize() {
        return getContext().getResources().getDimension(C0009R$dimen.global_actions_grid_item_height);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public float getAnimationDistance() {
        return getGridItemSize() / 2.0f;
    }

    public float getAnimationOffsetX() {
        return getAnimationDistance();
    }
}