package com.android.systemui.controlcenter.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.android.systemui.C0012R$id;
import com.android.systemui.controlcenter.ControlCenter;
import com.android.systemui.controlcenter.policy.BoostHelper;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.listener.TransitionListener;
import miuix.animation.property.FloatProperty;

public class ControlPanelWindowView extends FrameLayout {
    /* access modifiers changed from: private */
    public boolean mAnimating;
    private IStateStyle mBlurAmin;
    /* access modifiers changed from: private */
    public float mBlurRatio;
    private TransitionListener mBlurRatioListener;
    private View mBottomArea;
    private AnimatorListenerAdapter mCollapseListener;
    private ControlPanelContentView mContent;
    private boolean mContentShowing;
    private ControlCenter mControlCenter;
    private QSControlCenterPanel mControlCenterPanel;
    private QSControlCenterTileLayout mControlCenterTileLayout;
    /* access modifiers changed from: private */
    public ControlPanelWindowManager mControlPanelWindowManager;
    private float mDownExpandHeight;
    private float mDownX;
    private float mDownY;
    private float mExpandHeight;
    private AnimatorListenerAdapter mExpandListener;
    private int mExpandState;
    private ValueAnimator mHeightChangeAnimator;
    private boolean mInterceptTouchEvent;
    private int[] mLocation;
    private int mOrientation;
    private QSControlScrollView mQSControlScrollView;
    private LinearLayout mSmartControlsView;

    public ControlPanelWindowView(Context context) {
        this(context, (AttributeSet) null);
    }

    public ControlPanelWindowView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ControlPanelWindowView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mExpandState = 0;
        this.mDownY = 0.0f;
        this.mDownX = 0.0f;
        this.mDownExpandHeight = 0.0f;
        this.mInterceptTouchEvent = false;
        this.mLocation = new int[2];
        this.mBlurRatioListener = new TransitionListener() {
            public void onUpdate(Object obj, FloatProperty floatProperty, float f, float f2, boolean z) {
                float unused = ControlPanelWindowView.this.mBlurRatio = f;
                if (ControlPanelWindowView.this.mControlPanelWindowManager != null) {
                    ControlPanelWindowView.this.mControlPanelWindowManager.setBlurRatio(ControlPanelWindowView.this.mBlurRatio);
                }
            }
        };
        this.mCollapseListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                boolean unused = ControlPanelWindowView.this.mAnimating = false;
                ControlPanelWindowView.this.hideControlCenterWindow();
            }
        };
        this.mExpandListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                boolean unused = ControlPanelWindowView.this.mAnimating = false;
                ControlPanelWindowView.this.showControlCenterWindow();
            }
        };
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mContent = (ControlPanelContentView) findViewById(C0012R$id.control_panel_content);
        QSControlCenterPanel qSControlCenterPanel = (QSControlCenterPanel) findViewById(C0012R$id.qs_control_center_panel);
        this.mControlCenterPanel = qSControlCenterPanel;
        qSControlCenterPanel.setControlPanelWindowView(this);
        this.mQSControlScrollView = (QSControlScrollView) findViewById(C0012R$id.scroll_container);
        this.mControlCenterTileLayout = (QSControlCenterTileLayout) findViewById(C0012R$id.quick_tile_layout);
        this.mSmartControlsView = (LinearLayout) findViewById(C0012R$id.ll_smart_controls);
        this.mBottomArea = findViewById(C0012R$id.control_center_bottom_area);
        Folme.getValueTarget("ControlPanelViewBlur").setMinVisibleChange(0.01f, "blurRatio");
        this.mBlurAmin = Folme.useValue("ControlPanelViewBlur").addListener(this.mBlurRatioListener);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mOrientation = configuration.orientation;
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (this.mContent == null || !this.mControlCenter.panelEnabled()) {
            return false;
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        return !isCollapsed();
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        return !isCollapsed();
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (super.dispatchKeyEvent(keyEvent)) {
            return true;
        }
        if (keyEvent.getAction() == 1 && !isCollapsed()) {
            if (keyEvent.getKeyCode() == 4) {
                if (this.mContent.isDetailShowing()) {
                    this.mControlCenterPanel.closeDetail(false);
                    return true;
                } else if (this.mContent.isEditShowing()) {
                    this.mContent.hideEdit();
                    return true;
                } else if (this.mContent.isControlEditShowing()) {
                    this.mContent.hideControlEdit();
                    return true;
                }
            }
            if (keyEvent.getKeyCode() == 4 || keyEvent.getKeyCode() == 3 || keyEvent.getKeyCode() == 82) {
                if (this.mContent.isDetailShowing()) {
                    this.mControlCenterPanel.closeDetail(false);
                }
                if (this.mContent.isEditShowing()) {
                    this.mContent.hideEdit();
                }
                if (this.mContent.isControlEditShowing()) {
                    this.mContent.hideControlEdit();
                }
                collapsePanel();
                return true;
            }
        }
        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        boolean z = this.mInterceptTouchEvent || isCollapsed() || isCollapsing() || isExpanding() || super.onInterceptTouchEvent(motionEvent);
        boolean z2 = motionEvent.getAction() == 0;
        boolean z3 = motionEvent.getAction() == 2;
        int action = motionEvent.getAction();
        int action2 = motionEvent.getAction();
        boolean z4 = Math.abs(motionEvent.getRawY() - this.mDownY) > Math.abs(motionEvent.getRawX() - this.mDownX);
        if (z2) {
            if (this.mContent.getHeight() == 0) {
                this.mExpandState = 0;
            }
            if (isCollapsed()) {
                showControlCenterWindow();
            }
            this.mDownY = motionEvent.getRawY();
            this.mDownX = motionEvent.getRawX();
            this.mDownExpandHeight = this.mExpandHeight;
        } else if (!this.mContent.isDetailShowing() && !this.mContent.isEditShowing() && !this.mContent.isControlEditShowing()) {
            if (z3 && motionEvent.getRawY() > this.mDownY && z4 && this.mControlCenterTileLayout.isExpanded() && this.mOrientation == 1 && this.mQSControlScrollView.isScrolledToTop()) {
                return true;
            }
            if (z3 && isBottomAreaTouchDown(this.mDownY) && motionEvent.getRawY() < this.mDownY && z4) {
                return true;
            }
            if (!z3 || motionEvent.getRawY() >= this.mDownY || this.mSmartControlsView.getChildCount() <= 0 || this.mQSControlScrollView.isScrolledToBottom() || !this.mControlCenterTileLayout.isCollapsed()) {
                return z;
            }
            return false;
        }
        return z;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        ControlCenter controlCenter = this.mControlCenter;
        if (controlCenter != null && !controlCenter.panelEnabled()) {
            return false;
        }
        boolean z = motionEvent.getAction() == 0;
        boolean z2 = motionEvent.getAction() == 2;
        boolean z3 = motionEvent.getAction() == 1;
        boolean z4 = motionEvent.getAction() == 3;
        if (z) {
            if (this.mContent.getHeight() == 0) {
                this.mExpandState = 0;
            }
            if (isCollapsed()) {
                showControlCenterWindow();
            }
            this.mDownY = motionEvent.getRawY();
            this.mDownExpandHeight = this.mExpandHeight;
        } else if (this.mContent.isDetailShowing() || this.mContent.isEditShowing() || this.mContent.isControlEditShowing()) {
            return false;
        } else {
            if (z2) {
                float rawY = motionEvent.getRawY();
                float f = this.mDownY;
                if (rawY >= f) {
                    updateExpandHeight(Math.min((this.mDownExpandHeight + motionEvent.getRawY()) - this.mDownY, 80.0f));
                } else {
                    float f2 = this.mDownExpandHeight;
                    if (f2 < 80.0f) {
                        updateExpandHeight(Math.max(0.0f, (f2 + motionEvent.getRawY()) - this.mDownY));
                    } else if (f - motionEvent.getRawY() >= 80.0f) {
                        updateExpandHeight(Math.max(0.0f, 80.0f - ((this.mDownY - motionEvent.getRawY()) - 80.0f)));
                    }
                }
                updateTransHeight(motionEvent.getRawY() - this.mDownY);
                this.mInterceptTouchEvent = true;
            } else if (z3 || z4) {
                if (this.mInterceptTouchEvent || this.mExpandState != 2) {
                    this.mInterceptTouchEvent = false;
                    updateTransHeight(0.0f);
                    endWithCurrentExpandHeight();
                }
                if (isExpanding()) {
                    this.mExpandState = 0;
                }
            }
        }
        return true;
    }

    public void setControlCenter(ControlCenter controlCenter) {
        this.mControlCenter = controlCenter;
    }

    /* access modifiers changed from: private */
    public void updateExpandHeight(float f) {
        if ((this.mControlCenter.isExpandable() || f == 0.0f) && this.mExpandHeight != f) {
            float max = Math.max(Math.min(1.0f, f / 80.0f), 0.0f);
            float f2 = this.mBlurRatio;
            if (f2 != max) {
                this.mBlurAmin.setTo("blurRatio", Float.valueOf(f2)).to("blurRatio", Float.valueOf(max));
            } else {
                this.mControlPanelWindowManager.setBlurRatio(max);
            }
            float f3 = this.mExpandHeight;
            this.mExpandHeight = f;
            if (f3 < 80.0f || f >= 80.0f) {
                if (f3 < 80.0f && f >= 80.0f && !this.mContentShowing) {
                    this.mContent.showContent();
                    this.mContentShowing = true;
                    onControlPanelFinishExpand();
                    Log.d("ControllerPanelWindowView", "showContent:" + this.mContent.getHeight());
                }
            } else if (this.mContentShowing) {
                this.mContent.hideContent();
                this.mContentShowing = false;
                onControlPanelFinishCollapsed();
                Log.d("ControllerPanelWindowView", "hideContent");
            }
        }
    }

    private void createAndStartAnimator(int i, AnimatorListenerAdapter animatorListenerAdapter) {
        cancelHeightChangeAnimator();
        createHeightChangeAnimator(i, animatorListenerAdapter);
        startHeightChangeAnimator();
    }

    private void createHeightChangeAnimator(int i, AnimatorListenerAdapter animatorListenerAdapter) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{this.mExpandHeight, (float) i});
        this.mHeightChangeAnimator = ofFloat;
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ControlPanelWindowView.this.updateExpandHeight(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        if (animatorListenerAdapter != null) {
            this.mHeightChangeAnimator.addListener(animatorListenerAdapter);
        }
        this.mHeightChangeAnimator.setDuration(250);
    }

    private void updateTransHeight(float f) {
        this.mContent.updateTransHeight(f);
    }

    private void endWithCurrentExpandHeight() {
        if (this.mExpandHeight < 80.0f) {
            createAndStartAnimator(0, this.mCollapseListener);
        }
    }

    public void setControlPanelWindowManager(ControlPanelWindowManager controlPanelWindowManager) {
        this.mControlPanelWindowManager = controlPanelWindowManager;
        this.mContent.setControlPanelWindowManager(controlPanelWindowManager);
    }

    public boolean isCollapsed() {
        return this.mExpandState == 0;
    }

    public boolean isExpanding() {
        return this.mExpandState == 1;
    }

    public boolean isCollapsing() {
        return this.mExpandState == 3;
    }

    public void showControlCenterWindow() {
        BoostHelper.getInstance().boostSystemUI(this, true);
        this.mExpandState = 1;
        this.mControlPanelWindowManager.onExpandChange(true);
    }

    public void hideControlCenterWindow() {
        this.mControlPanelWindowManager.onExpandChange(false);
        BoostHelper.getInstance().boostSystemUI(this, false);
        this.mExpandState = 0;
    }

    public void onControlPanelFinishExpand() {
        this.mExpandState = 2;
    }

    public void onControlPanelFinishCollapsed() {
        this.mExpandState = 0;
    }

    public void collapsePanel() {
        collapsePanel(true);
    }

    public void collapsePanel(boolean z) {
        collapsePanel(z, 0);
    }

    public void collapsePanel(boolean z, int i) {
        if (this.mContent.isDetailShowing()) {
            this.mControlCenterPanel.closeDetail(false);
        }
        if (this.mContent.isEditShowing()) {
            this.mContent.hideEdit();
        }
        if (this.mContent.isControlEditShowing()) {
            this.mContent.hideControlEdit();
        }
        if (z) {
            createAndStartAnimator(0, this.mCollapseListener);
        } else {
            collapsePanelImmediately();
        }
    }

    private void collapsePanelImmediately() {
        updateExpandHeight(0.0f);
        this.mCollapseListener.onAnimationEnd((Animator) null);
        this.mExpandState = 0;
    }

    public void expandPanel() {
        createAndStartAnimator(80, this.mExpandListener);
    }

    private void startHeightChangeAnimator() {
        if (!this.mAnimating) {
            this.mAnimating = true;
            this.mHeightChangeAnimator.start();
        }
    }

    private void cancelHeightChangeAnimator() {
        if (this.mAnimating) {
            this.mHeightChangeAnimator.removeAllListeners();
            this.mHeightChangeAnimator.removeAllUpdateListeners();
            this.mHeightChangeAnimator.cancel();
            this.mAnimating = false;
        }
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        windowInsets.consumeSystemWindowInsets();
        windowInsets.consumeDisplayCutout();
        Log.d("ControllerPanelWindowView", "onApplyWindowInsets: ");
        return windowInsets.consumeDisplayCutout();
    }

    public void onDescendantInvalidated(View view, View view2) {
        super.onDescendantInvalidated(view, view2);
    }

    private boolean isBottomAreaTouchDown(float f) {
        this.mBottomArea.getLocationOnScreen(this.mLocation);
        int[] iArr = this.mLocation;
        return f >= ((float) iArr[1]) && f <= ((float) (iArr[1] + this.mBottomArea.getHeight()));
    }

    public void refreshAllTiles() {
        QSControlCenterTileLayout qSControlCenterTileLayout = this.mControlCenterTileLayout;
        if (qSControlCenterTileLayout != null) {
            qSControlCenterTileLayout.refreshAllTiles();
        }
    }
}