package com.android.systemui.pip.tv;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.os.Bundle;
import com.android.systemui.C0004R$anim;
import com.android.systemui.C0012R$id;
import com.android.systemui.C0014R$layout;
import com.android.systemui.pip.tv.PipManager;
import com.android.systemui.pip.tv.dagger.TvPipComponent;
import java.util.Collections;

public class PipMenuActivity extends Activity implements PipManager.Listener {
    private Animator mFadeInAnimation;
    private Animator mFadeOutAnimation;
    private final TvPipComponent.Builder mPipComponentBuilder;
    private PipControlsViewController mPipControlsViewController;
    private final PipManager mPipManager;
    private boolean mRestorePipSizeWhenClose;
    private TvPipComponent mTvPipComponent;

    public void onPipEntered(String str) {
    }

    public void onShowPipMenu() {
    }

    public PipMenuActivity(TvPipComponent.Builder builder, PipManager pipManager) {
        this.mPipComponentBuilder = builder;
        this.mPipManager = pipManager;
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (!this.mPipManager.isPipShown()) {
            finish();
        }
        setContentView(C0014R$layout.tv_pip_menu);
        TvPipComponent build = this.mPipComponentBuilder.pipControlsView((PipControlsView) findViewById(C0012R$id.pip_controls)).build();
        this.mTvPipComponent = build;
        this.mPipControlsViewController = build.getPipControlsViewController();
        this.mPipManager.addListener(this);
        this.mRestorePipSizeWhenClose = true;
        Animator loadAnimator = AnimatorInflater.loadAnimator(this, C0004R$anim.tv_pip_menu_fade_in_animation);
        this.mFadeInAnimation = loadAnimator;
        loadAnimator.setTarget(this.mPipControlsViewController.getView());
        Animator loadAnimator2 = AnimatorInflater.loadAnimator(this, C0004R$anim.tv_pip_menu_fade_out_animation);
        this.mFadeOutAnimation = loadAnimator2;
        loadAnimator2.setTarget(this.mPipControlsViewController.getView());
        onPipMenuActionsChanged(getIntent().getParcelableExtra("custom_actions"));
    }

    /* access modifiers changed from: protected */
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        onPipMenuActionsChanged(getIntent().getParcelableExtra("custom_actions"));
    }

    private void restorePipAndFinish() {
        if (this.mRestorePipSizeWhenClose) {
            this.mPipManager.resizePinnedStack(1);
        }
        finish();
    }

    public void onResume() {
        super.onResume();
        this.mFadeInAnimation.start();
    }

    public void onPause() {
        super.onPause();
        this.mFadeOutAnimation.start();
        restorePipAndFinish();
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        this.mPipManager.removeListener(this);
        this.mPipManager.resumePipResizing(1);
    }

    public void onBackPressed() {
        restorePipAndFinish();
    }

    public void onPipActivityClosed() {
        finish();
    }

    public void onPipMenuActionsChanged(ParceledListSlice parceledListSlice) {
        this.mPipControlsViewController.setActions(parceledListSlice != null && !parceledListSlice.getList().isEmpty() ? parceledListSlice.getList() : Collections.EMPTY_LIST);
    }

    public void onMoveToFullscreen() {
        this.mRestorePipSizeWhenClose = false;
        finish();
    }

    public void onPipResizeAboutToStart() {
        finish();
        this.mPipManager.suspendPipResizing(1);
    }

    public void finish() {
        super.finish();
    }
}
