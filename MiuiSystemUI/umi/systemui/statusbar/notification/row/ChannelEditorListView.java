package com.android.systemui.statusbar.notification.row;

import android.app.NotificationChannel;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.C0012R$id;
import com.android.systemui.C0014R$layout;
import com.android.systemui.C0018R$string;
import com.android.systemui.util.Assert;
import java.util.ArrayList;
import java.util.List;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ChannelEditorListView.kt */
public final class ChannelEditorListView extends LinearLayout {
    private AppControlView appControlRow;
    @Nullable
    private Drawable appIcon;
    @Nullable
    private String appName;
    private final List<ChannelRow> channelRows = new ArrayList();
    @NotNull
    private List<NotificationChannel> channels = new ArrayList();
    @NotNull
    public ChannelEditorDialogController controller;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ChannelEditorListView(@NotNull Context context, @NotNull AttributeSet attributeSet) {
        super(context, attributeSet);
        Intrinsics.checkParameterIsNotNull(context, "c");
        Intrinsics.checkParameterIsNotNull(attributeSet, "attrs");
    }

    @NotNull
    public final ChannelEditorDialogController getController() {
        ChannelEditorDialogController channelEditorDialogController = this.controller;
        if (channelEditorDialogController != null) {
            return channelEditorDialogController;
        }
        Intrinsics.throwUninitializedPropertyAccessException("controller");
        throw null;
    }

    public final void setController(@NotNull ChannelEditorDialogController channelEditorDialogController) {
        Intrinsics.checkParameterIsNotNull(channelEditorDialogController, "<set-?>");
        this.controller = channelEditorDialogController;
    }

    public final void setAppIcon(@Nullable Drawable drawable) {
        this.appIcon = drawable;
    }

    public final void setAppName(@Nullable String str) {
        this.appName = str;
    }

    public final void setChannels(@NotNull List<NotificationChannel> list) {
        Intrinsics.checkParameterIsNotNull(list, "newValue");
        this.channels = list;
        updateRows();
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        View findViewById = findViewById(C0012R$id.app_control);
        Intrinsics.checkExpressionValueIsNotNull(findViewById, "findViewById(R.id.app_control)");
        this.appControlRow = (AppControlView) findViewById;
    }

    public final void highlightChannel(@NotNull NotificationChannel notificationChannel) {
        Intrinsics.checkParameterIsNotNull(notificationChannel, "channel");
        Assert.isMainThread();
        for (ChannelRow next : this.channelRows) {
            if (Intrinsics.areEqual((Object) next.getChannel(), (Object) notificationChannel)) {
                next.playHighlight();
            }
        }
    }

    /* access modifiers changed from: private */
    public final void updateRows() {
        ChannelEditorDialogController channelEditorDialogController = this.controller;
        if (channelEditorDialogController != null) {
            boolean areAppNotificationsEnabled = channelEditorDialogController.areAppNotificationsEnabled();
            AutoTransition autoTransition = new AutoTransition();
            autoTransition.setDuration(200);
            autoTransition.addListener(new ChannelEditorListView$updateRows$1(this));
            TransitionManager.beginDelayedTransition(this, autoTransition);
            for (ChannelRow removeView : this.channelRows) {
                removeView(removeView);
            }
            this.channelRows.clear();
            updateAppControlRow(areAppNotificationsEnabled);
            if (areAppNotificationsEnabled) {
                LayoutInflater from = LayoutInflater.from(getContext());
                for (NotificationChannel addChannelRow : this.channels) {
                    Intrinsics.checkExpressionValueIsNotNull(from, "inflater");
                    addChannelRow(addChannelRow, from);
                }
                return;
            }
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("controller");
        throw null;
    }

    private final void addChannelRow(NotificationChannel notificationChannel, LayoutInflater layoutInflater) {
        View inflate = layoutInflater.inflate(C0014R$layout.notif_half_shelf_row, (ViewGroup) null);
        if (inflate != null) {
            ChannelRow channelRow = (ChannelRow) inflate;
            ChannelEditorDialogController channelEditorDialogController = this.controller;
            if (channelEditorDialogController != null) {
                channelRow.setController(channelEditorDialogController);
                channelRow.setChannel(notificationChannel);
                this.channelRows.add(channelRow);
                addView(channelRow);
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("controller");
            throw null;
        }
        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.notification.row.ChannelRow");
    }

    private final void updateAppControlRow(boolean z) {
        AppControlView appControlView = this.appControlRow;
        if (appControlView != null) {
            appControlView.getIconView().setImageDrawable(this.appIcon);
            AppControlView appControlView2 = this.appControlRow;
            if (appControlView2 != null) {
                TextView channelName = appControlView2.getChannelName();
                Context context = getContext();
                Intrinsics.checkExpressionValueIsNotNull(context, "context");
                channelName.setText(context.getResources().getString(C0018R$string.notification_channel_dialog_title, new Object[]{this.appName}));
                AppControlView appControlView3 = this.appControlRow;
                if (appControlView3 != null) {
                    appControlView3.getSwitch().setChecked(z);
                    AppControlView appControlView4 = this.appControlRow;
                    if (appControlView4 != null) {
                        appControlView4.getSwitch().setOnCheckedChangeListener(new ChannelEditorListView$updateAppControlRow$1(this));
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("appControlRow");
                        throw null;
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("appControlRow");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("appControlRow");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("appControlRow");
            throw null;
        }
    }
}