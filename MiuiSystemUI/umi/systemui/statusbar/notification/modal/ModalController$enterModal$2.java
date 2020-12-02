package com.android.systemui.statusbar.notification.modal;

import android.view.View;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;

/* compiled from: ModalController.kt */
final class ModalController$enterModal$2 implements View.OnClickListener {
    final /* synthetic */ ModalController this$0;

    ModalController$enterModal$2(ModalController modalController) {
        this.this$0 = modalController;
    }

    public final void onClick(View view) {
        ExpandableNotificationRow row;
        this.this$0.animExitModal();
        NotificationEntry access$getEntry$p = this.this$0.entry;
        if (access$getEntry$p != null && (row = access$getEntry$p.getRow()) != null) {
            row.performClick();
        }
    }
}