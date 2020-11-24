package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.android.systemui.C0009R$dimen;
import com.android.systemui.C0012R$id;
import com.android.systemui.C0018R$string;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.statusbar.phone.StatusBarWindowView;

public class QSDetail extends LinearLayout {
    /* access modifiers changed from: private */
    public boolean mAnimatingOpen;
    /* access modifiers changed from: private */
    public boolean mClosingDetail;
    /* access modifiers changed from: private */
    public DetailAdapter mDetailAdapter;
    /* access modifiers changed from: private */
    public ViewGroup mDetailContent;
    protected TextView mDetailDoneButton;
    protected TextView mDetailSettingsButton;
    private final SparseArray<View> mDetailViews = new SparseArray<>();
    /* access modifiers changed from: private */
    public View mFooter;
    /* access modifiers changed from: private */
    public QuickStatusBarHeader mHeader;
    protected View mQsDetailHeader;
    private Switch mQsDetailHeaderSwitch;
    /* access modifiers changed from: private */
    public QSPanel mQsPanel;
    private boolean mSwitchState;

    public QSDetail(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        QSEvents.INSTANCE.getQsUiEventsLogger();
        new AnimatorListenerAdapter() {
            public void onAnimationCancel(Animator animator) {
                animator.removeListener(this);
                boolean unused = QSDetail.this.mAnimatingOpen = false;
                QSDetail.this.checkPendingAnimations();
            }

            public void onAnimationEnd(Animator animator) {
                if (QSDetail.this.mDetailAdapter != null) {
                    QSDetail.this.mQsPanel.setGridContentVisibility(false);
                    QSDetail.this.mHeader.setVisibility(4);
                    QSDetail.this.mFooter.setVisibility(4);
                }
                boolean unused = QSDetail.this.mAnimatingOpen = false;
                QSDetail.this.checkPendingAnimations();
            }
        };
        new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                QSDetail.this.mDetailContent.removeAllViews();
                QSDetail.this.setVisibility(4);
                boolean unused = QSDetail.this.mClosingDetail = false;
            }
        };
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        FontSizeUtils.updateFontSize(this.mDetailDoneButton, C0009R$dimen.qs_detail_button_text_size);
        FontSizeUtils.updateFontSize(this.mDetailSettingsButton, C0009R$dimen.qs_detail_button_text_size);
        for (int i = 0; i < this.mDetailViews.size(); i++) {
            this.mDetailViews.valueAt(i).dispatchConfigurationChanged(configuration);
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mDetailContent = (ViewGroup) findViewById(16908290);
        this.mDetailSettingsButton = (TextView) findViewById(16908314);
        this.mDetailDoneButton = (TextView) findViewById(16908313);
        View findViewById = findViewById(C0012R$id.qs_detail_header);
        this.mQsDetailHeader = findViewById;
        TextView textView = (TextView) findViewById.findViewById(16908310);
        ViewStub viewStub = (ViewStub) this.mQsDetailHeader.findViewById(C0012R$id.toggle_stub);
        ImageView imageView = (ImageView) findViewById(C0012R$id.qs_detail_header_progress);
        updateDetailText();
        new QSDetailClipper(this);
        this.mDetailDoneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                QSDetail qSDetail = QSDetail.this;
                qSDetail.announceForAccessibility(qSDetail.mContext.getString(C0018R$string.accessibility_desc_quick_settings));
                QSDetail.this.mQsPanel.closeDetail();
            }
        });
    }

    private void updateDetailText() {
        this.mDetailDoneButton.setText(C0018R$string.quick_settings_done);
        this.mDetailSettingsButton.setText(C0018R$string.quick_settings_more_settings);
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        Pair<Integer, Integer> cornerCutoutMargins = StatusBarWindowView.cornerCutoutMargins(windowInsets.getDisplayCutout(), getDisplay());
        if (cornerCutoutMargins == null) {
            this.mQsDetailHeader.setPaddingRelative(getResources().getDimensionPixelSize(C0009R$dimen.qs_detail_header_padding), getPaddingTop(), getResources().getDimensionPixelSize(C0009R$dimen.qs_detail_header_padding), getPaddingBottom());
        } else {
            this.mQsDetailHeader.setPadding(((Integer) cornerCutoutMargins.first).intValue(), getPaddingTop(), ((Integer) cornerCutoutMargins.second).intValue(), getPaddingBottom());
        }
        return super.onApplyWindowInsets(windowInsets);
    }

    private void handleToggleStateChanged(boolean z, boolean z2) {
        this.mSwitchState = z;
        if (!this.mAnimatingOpen) {
            Switch switchR = this.mQsDetailHeaderSwitch;
            if (switchR != null) {
                switchR.setChecked(z);
            }
            this.mQsDetailHeader.setEnabled(z2);
            Switch switchR2 = this.mQsDetailHeaderSwitch;
            if (switchR2 != null) {
                switchR2.setEnabled(z2);
            }
        }
    }

    /* access modifiers changed from: private */
    public void checkPendingAnimations() {
        boolean z = this.mSwitchState;
        DetailAdapter detailAdapter = this.mDetailAdapter;
        handleToggleStateChanged(z, detailAdapter != null && detailAdapter.getToggleEnabled());
    }
}
