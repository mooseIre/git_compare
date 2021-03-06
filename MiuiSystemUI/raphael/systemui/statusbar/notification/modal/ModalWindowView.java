package com.android.systemui.statusbar.notification.modal;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Insets;
import android.util.AttributeSet;
import android.util.Property;
import android.view.DisplayCutout;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.RowAnimationUtils;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchCallback;
import com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchHelper;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.stack.AnimationFilter;
import com.android.systemui.statusbar.notification.stack.AnimationProperties;
import com.android.systemui.statusbar.notification.stack.ExpandableViewState;
import com.android.systemui.statusbar.notification.stack.ViewState;
import com.miui.systemui.EventTracker;
import com.miui.systemui.events.MiniWindowEventSource;
import com.miui.systemui.events.ModalExitMode;
import java.util.function.Consumer;
import miuix.animation.property.ViewProperty;
import miuix.view.animation.CubicEaseOutInterpolator;

public class ModalWindowView extends FrameLayout implements AppMiniWindowRowTouchCallback {
    private final AnimationProperties animationProperties;
    private boolean mChildrenUpdateRequested;
    private final ViewTreeObserver.OnPreDrawListener mChildrenUpdater;
    private FrameLayout mDialogContainer;
    private NotificationEntry mEntry;
    private boolean mFirstAddUpdateRequested;
    private final ViewTreeObserver.OnPreDrawListener mFirstAddUpdater;
    private int mLayoutWidth;
    private int mLeftInset;
    private int mMaxModalBottom;
    private View mMenuView;
    private final ViewState mMenuViewState;
    private View mModalDialog;
    private int mModalDialogMarginTopDelta;
    private float mModalDialogTempY;
    private ViewState mModalDialogViewState;
    private int mModalMenuMarginTop;
    private ExpandableNotificationRow mModalRow;
    private ExpandableView.OnHeightChangedListener mOnHeightChangedListener;
    private int mRightInset;
    private int mScreenHeight;
    private int mSidePaddings;
    private final int[] mTmpLoc;
    private final AppMiniWindowRowTouchHelper mTouchHelper;
    private final AnimationProperties menuAnimationProperties;

    static /* synthetic */ void lambda$addModalDialog$1(View view) {
    }

    @Override // com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchCallback
    public boolean canChildBePicked(ExpandableView expandableView) {
        return true;
    }

    public ModalWindowView(Context context) {
        super(context);
        this.mRightInset = 0;
        this.mLeftInset = 0;
        this.mChildrenUpdateRequested = false;
        this.mFirstAddUpdateRequested = false;
        this.mMenuViewState = new ViewState();
        this.mTouchHelper = new AppMiniWindowRowTouchHelper(this, (NotificationEntryManager) Dependency.get(NotificationEntryManager.class), (EventTracker) Dependency.get(EventTracker.class), MiniWindowEventSource.MODAL_NOTIFICATION);
        this.mTmpLoc = new int[2];
        AnonymousClass1 r0 = new AnimationProperties(this) {
            /* class com.android.systemui.statusbar.notification.modal.ModalWindowView.AnonymousClass1 */
            private final AnimationFilter filter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateHeight();
                animationFilter.animateY();
                animationFilter.animateAlpha();
                this.filter = animationFilter;
            }

            @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
            public AnimationFilter getAnimationFilter() {
                return this.filter;
            }
        };
        r0.setDuration(300);
        this.animationProperties = r0;
        AnonymousClass2 r02 = new AnimationProperties(this) {
            /* class com.android.systemui.statusbar.notification.modal.ModalWindowView.AnonymousClass2 */
            private final AnimationFilter filter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateY();
                animationFilter.animateAlpha();
                this.filter = animationFilter;
            }

            @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
            public AnimationFilter getAnimationFilter() {
                return this.filter;
            }
        };
        r02.setDuration(150);
        r02.setCustomInterpolator(ViewProperty.ALPHA, new CubicEaseOutInterpolator());
        this.menuAnimationProperties = r02;
        this.mChildrenUpdater = new ViewTreeObserver.OnPreDrawListener() {
            /* class com.android.systemui.statusbar.notification.modal.ModalWindowView.AnonymousClass3 */

            public boolean onPreDraw() {
                if (!(ModalWindowView.this.mModalRow == null || ModalWindowView.this.mModalRow.getViewState() == null)) {
                    ModalWindowView.this.mModalRow.getViewState().animateTo(ModalWindowView.this.mModalRow, ModalWindowView.this.animationProperties);
                }
                if (ModalWindowView.this.mMenuView != null) {
                    ModalWindowView.this.mMenuViewState.animateTo(ModalWindowView.this.mMenuView, ModalWindowView.this.menuAnimationProperties);
                }
                if (ModalWindowView.this.mModalDialog != null) {
                    ModalWindowView.this.mModalDialogViewState.animateTo(ModalWindowView.this.mModalDialog, ModalWindowView.this.animationProperties);
                }
                ModalWindowView.this.mChildrenUpdateRequested = false;
                ModalWindowView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        };
        this.mFirstAddUpdater = new ViewTreeObserver.OnPreDrawListener() {
            /* class com.android.systemui.statusbar.notification.modal.ModalWindowView.AnonymousClass4 */

            public boolean onPreDraw() {
                ModalWindowView modalWindowView = ModalWindowView.this;
                modalWindowView.enterModal(modalWindowView.mEntry);
                ModalWindowView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                ModalWindowView.this.mFirstAddUpdateRequested = false;
                return true;
            }
        };
        this.mOnHeightChangedListener = new ExpandableView.OnHeightChangedListener() {
            /* class com.android.systemui.statusbar.notification.modal.ModalWindowView.AnonymousClass5 */

            @Override // com.android.systemui.statusbar.notification.row.ExpandableView.OnHeightChangedListener
            public void onHeightChanged(ExpandableView expandableView, boolean z) {
                ModalWindowView.this.mModalRow.resetViewState();
                ViewState viewState = ModalWindowView.this.mMenuViewState;
                ModalWindowView modalWindowView = ModalWindowView.this;
                viewState.yTranslation = modalWindowView.getMenuYInModal(modalWindowView.mModalRow, false);
                ModalWindowView.this.requestChildrenUpdate();
            }

            @Override // com.android.systemui.statusbar.notification.row.ExpandableView.OnHeightChangedListener
            public void onReset(ExpandableView expandableView) {
                ModalWindowView.this.mModalRow.resetViewState();
            }
        };
        this.mModalDialogViewState = new ViewState();
        this.mModalDialogTempY = 0.0f;
        init(context);
    }

    public ModalWindowView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mRightInset = 0;
        this.mLeftInset = 0;
        this.mChildrenUpdateRequested = false;
        this.mFirstAddUpdateRequested = false;
        this.mMenuViewState = new ViewState();
        this.mTouchHelper = new AppMiniWindowRowTouchHelper(this, (NotificationEntryManager) Dependency.get(NotificationEntryManager.class), (EventTracker) Dependency.get(EventTracker.class), MiniWindowEventSource.MODAL_NOTIFICATION);
        this.mTmpLoc = new int[2];
        AnonymousClass1 r5 = new AnimationProperties(this) {
            /* class com.android.systemui.statusbar.notification.modal.ModalWindowView.AnonymousClass1 */
            private final AnimationFilter filter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateHeight();
                animationFilter.animateY();
                animationFilter.animateAlpha();
                this.filter = animationFilter;
            }

            @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
            public AnimationFilter getAnimationFilter() {
                return this.filter;
            }
        };
        r5.setDuration(300);
        this.animationProperties = r5;
        AnonymousClass2 r52 = new AnimationProperties(this) {
            /* class com.android.systemui.statusbar.notification.modal.ModalWindowView.AnonymousClass2 */
            private final AnimationFilter filter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateY();
                animationFilter.animateAlpha();
                this.filter = animationFilter;
            }

            @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
            public AnimationFilter getAnimationFilter() {
                return this.filter;
            }
        };
        r52.setDuration(150);
        r52.setCustomInterpolator(ViewProperty.ALPHA, new CubicEaseOutInterpolator());
        this.menuAnimationProperties = r52;
        this.mChildrenUpdater = new ViewTreeObserver.OnPreDrawListener() {
            /* class com.android.systemui.statusbar.notification.modal.ModalWindowView.AnonymousClass3 */

            public boolean onPreDraw() {
                if (!(ModalWindowView.this.mModalRow == null || ModalWindowView.this.mModalRow.getViewState() == null)) {
                    ModalWindowView.this.mModalRow.getViewState().animateTo(ModalWindowView.this.mModalRow, ModalWindowView.this.animationProperties);
                }
                if (ModalWindowView.this.mMenuView != null) {
                    ModalWindowView.this.mMenuViewState.animateTo(ModalWindowView.this.mMenuView, ModalWindowView.this.menuAnimationProperties);
                }
                if (ModalWindowView.this.mModalDialog != null) {
                    ModalWindowView.this.mModalDialogViewState.animateTo(ModalWindowView.this.mModalDialog, ModalWindowView.this.animationProperties);
                }
                ModalWindowView.this.mChildrenUpdateRequested = false;
                ModalWindowView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        };
        this.mFirstAddUpdater = new ViewTreeObserver.OnPreDrawListener() {
            /* class com.android.systemui.statusbar.notification.modal.ModalWindowView.AnonymousClass4 */

            public boolean onPreDraw() {
                ModalWindowView modalWindowView = ModalWindowView.this;
                modalWindowView.enterModal(modalWindowView.mEntry);
                ModalWindowView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                ModalWindowView.this.mFirstAddUpdateRequested = false;
                return true;
            }
        };
        this.mOnHeightChangedListener = new ExpandableView.OnHeightChangedListener() {
            /* class com.android.systemui.statusbar.notification.modal.ModalWindowView.AnonymousClass5 */

            @Override // com.android.systemui.statusbar.notification.row.ExpandableView.OnHeightChangedListener
            public void onHeightChanged(ExpandableView expandableView, boolean z) {
                ModalWindowView.this.mModalRow.resetViewState();
                ViewState viewState = ModalWindowView.this.mMenuViewState;
                ModalWindowView modalWindowView = ModalWindowView.this;
                viewState.yTranslation = modalWindowView.getMenuYInModal(modalWindowView.mModalRow, false);
                ModalWindowView.this.requestChildrenUpdate();
            }

            @Override // com.android.systemui.statusbar.notification.row.ExpandableView.OnHeightChangedListener
            public void onReset(ExpandableView expandableView) {
                ModalWindowView.this.mModalRow.resetViewState();
            }
        };
        this.mModalDialogViewState = new ViewState();
        this.mModalDialogTempY = 0.0f;
        init(context);
    }

    public ModalWindowView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mRightInset = 0;
        this.mLeftInset = 0;
        this.mChildrenUpdateRequested = false;
        this.mFirstAddUpdateRequested = false;
        this.mMenuViewState = new ViewState();
        this.mTouchHelper = new AppMiniWindowRowTouchHelper(this, (NotificationEntryManager) Dependency.get(NotificationEntryManager.class), (EventTracker) Dependency.get(EventTracker.class), MiniWindowEventSource.MODAL_NOTIFICATION);
        this.mTmpLoc = new int[2];
        AnonymousClass1 r4 = new AnimationProperties(this) {
            /* class com.android.systemui.statusbar.notification.modal.ModalWindowView.AnonymousClass1 */
            private final AnimationFilter filter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateHeight();
                animationFilter.animateY();
                animationFilter.animateAlpha();
                this.filter = animationFilter;
            }

            @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
            public AnimationFilter getAnimationFilter() {
                return this.filter;
            }
        };
        r4.setDuration(300);
        this.animationProperties = r4;
        AnonymousClass2 r42 = new AnimationProperties(this) {
            /* class com.android.systemui.statusbar.notification.modal.ModalWindowView.AnonymousClass2 */
            private final AnimationFilter filter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateY();
                animationFilter.animateAlpha();
                this.filter = animationFilter;
            }

            @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
            public AnimationFilter getAnimationFilter() {
                return this.filter;
            }
        };
        r42.setDuration(150);
        r42.setCustomInterpolator(ViewProperty.ALPHA, new CubicEaseOutInterpolator());
        this.menuAnimationProperties = r42;
        this.mChildrenUpdater = new ViewTreeObserver.OnPreDrawListener() {
            /* class com.android.systemui.statusbar.notification.modal.ModalWindowView.AnonymousClass3 */

            public boolean onPreDraw() {
                if (!(ModalWindowView.this.mModalRow == null || ModalWindowView.this.mModalRow.getViewState() == null)) {
                    ModalWindowView.this.mModalRow.getViewState().animateTo(ModalWindowView.this.mModalRow, ModalWindowView.this.animationProperties);
                }
                if (ModalWindowView.this.mMenuView != null) {
                    ModalWindowView.this.mMenuViewState.animateTo(ModalWindowView.this.mMenuView, ModalWindowView.this.menuAnimationProperties);
                }
                if (ModalWindowView.this.mModalDialog != null) {
                    ModalWindowView.this.mModalDialogViewState.animateTo(ModalWindowView.this.mModalDialog, ModalWindowView.this.animationProperties);
                }
                ModalWindowView.this.mChildrenUpdateRequested = false;
                ModalWindowView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        };
        this.mFirstAddUpdater = new ViewTreeObserver.OnPreDrawListener() {
            /* class com.android.systemui.statusbar.notification.modal.ModalWindowView.AnonymousClass4 */

            public boolean onPreDraw() {
                ModalWindowView modalWindowView = ModalWindowView.this;
                modalWindowView.enterModal(modalWindowView.mEntry);
                ModalWindowView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                ModalWindowView.this.mFirstAddUpdateRequested = false;
                return true;
            }
        };
        this.mOnHeightChangedListener = new ExpandableView.OnHeightChangedListener() {
            /* class com.android.systemui.statusbar.notification.modal.ModalWindowView.AnonymousClass5 */

            @Override // com.android.systemui.statusbar.notification.row.ExpandableView.OnHeightChangedListener
            public void onHeightChanged(ExpandableView expandableView, boolean z) {
                ModalWindowView.this.mModalRow.resetViewState();
                ViewState viewState = ModalWindowView.this.mMenuViewState;
                ModalWindowView modalWindowView = ModalWindowView.this;
                viewState.yTranslation = modalWindowView.getMenuYInModal(modalWindowView.mModalRow, false);
                ModalWindowView.this.requestChildrenUpdate();
            }

            @Override // com.android.systemui.statusbar.notification.row.ExpandableView.OnHeightChangedListener
            public void onReset(ExpandableView expandableView) {
                ModalWindowView.this.mModalRow.resetViewState();
            }
        };
        this.mModalDialogViewState = new ViewState();
        this.mModalDialogTempY = 0.0f;
        init(context);
    }

    public ModalWindowView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mRightInset = 0;
        this.mLeftInset = 0;
        this.mChildrenUpdateRequested = false;
        this.mFirstAddUpdateRequested = false;
        this.mMenuViewState = new ViewState();
        this.mTouchHelper = new AppMiniWindowRowTouchHelper(this, (NotificationEntryManager) Dependency.get(NotificationEntryManager.class), (EventTracker) Dependency.get(EventTracker.class), MiniWindowEventSource.MODAL_NOTIFICATION);
        this.mTmpLoc = new int[2];
        AnonymousClass1 r3 = new AnimationProperties(this) {
            /* class com.android.systemui.statusbar.notification.modal.ModalWindowView.AnonymousClass1 */
            private final AnimationFilter filter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateHeight();
                animationFilter.animateY();
                animationFilter.animateAlpha();
                this.filter = animationFilter;
            }

            @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
            public AnimationFilter getAnimationFilter() {
                return this.filter;
            }
        };
        r3.setDuration(300);
        this.animationProperties = r3;
        AnonymousClass2 r32 = new AnimationProperties(this) {
            /* class com.android.systemui.statusbar.notification.modal.ModalWindowView.AnonymousClass2 */
            private final AnimationFilter filter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateY();
                animationFilter.animateAlpha();
                this.filter = animationFilter;
            }

            @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
            public AnimationFilter getAnimationFilter() {
                return this.filter;
            }
        };
        r32.setDuration(150);
        r32.setCustomInterpolator(ViewProperty.ALPHA, new CubicEaseOutInterpolator());
        this.menuAnimationProperties = r32;
        this.mChildrenUpdater = new ViewTreeObserver.OnPreDrawListener() {
            /* class com.android.systemui.statusbar.notification.modal.ModalWindowView.AnonymousClass3 */

            public boolean onPreDraw() {
                if (!(ModalWindowView.this.mModalRow == null || ModalWindowView.this.mModalRow.getViewState() == null)) {
                    ModalWindowView.this.mModalRow.getViewState().animateTo(ModalWindowView.this.mModalRow, ModalWindowView.this.animationProperties);
                }
                if (ModalWindowView.this.mMenuView != null) {
                    ModalWindowView.this.mMenuViewState.animateTo(ModalWindowView.this.mMenuView, ModalWindowView.this.menuAnimationProperties);
                }
                if (ModalWindowView.this.mModalDialog != null) {
                    ModalWindowView.this.mModalDialogViewState.animateTo(ModalWindowView.this.mModalDialog, ModalWindowView.this.animationProperties);
                }
                ModalWindowView.this.mChildrenUpdateRequested = false;
                ModalWindowView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        };
        this.mFirstAddUpdater = new ViewTreeObserver.OnPreDrawListener() {
            /* class com.android.systemui.statusbar.notification.modal.ModalWindowView.AnonymousClass4 */

            public boolean onPreDraw() {
                ModalWindowView modalWindowView = ModalWindowView.this;
                modalWindowView.enterModal(modalWindowView.mEntry);
                ModalWindowView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                ModalWindowView.this.mFirstAddUpdateRequested = false;
                return true;
            }
        };
        this.mOnHeightChangedListener = new ExpandableView.OnHeightChangedListener() {
            /* class com.android.systemui.statusbar.notification.modal.ModalWindowView.AnonymousClass5 */

            @Override // com.android.systemui.statusbar.notification.row.ExpandableView.OnHeightChangedListener
            public void onHeightChanged(ExpandableView expandableView, boolean z) {
                ModalWindowView.this.mModalRow.resetViewState();
                ViewState viewState = ModalWindowView.this.mMenuViewState;
                ModalWindowView modalWindowView = ModalWindowView.this;
                viewState.yTranslation = modalWindowView.getMenuYInModal(modalWindowView.mModalRow, false);
                ModalWindowView.this.requestChildrenUpdate();
            }

            @Override // com.android.systemui.statusbar.notification.row.ExpandableView.OnHeightChangedListener
            public void onReset(ExpandableView expandableView) {
                ModalWindowView.this.mModalRow.resetViewState();
            }
        };
        this.mModalDialogViewState = new ViewState();
        this.mModalDialogTempY = 0.0f;
        init(context);
    }

    public void init(Context context) {
        updateResource();
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void updateResource() {
        Resources resources = getResources();
        this.mModalDialogMarginTopDelta = resources.getDimensionPixelOffset(C0012R$dimen.modal_dialog_d_margin_top);
        this.mModalMenuMarginTop = resources.getDimensionPixelOffset(C0012R$dimen.notification_modal_menu_margin_top);
        this.mLayoutWidth = resources.getDimensionPixelOffset(C0012R$dimen.notification_panel_width);
        int i = resources.getDisplayMetrics().heightPixels;
        this.mScreenHeight = i;
        this.mMaxModalBottom = i - resources.getDimensionPixelOffset(C0012R$dimen.notification_modal_menu_bottom_max);
        int dimensionPixelSize = resources.getDimensionPixelSize(C0012R$dimen.notification_side_paddings);
        this.mSidePaddings = dimensionPixelSize;
        setPadding(dimensionPixelSize, 0, dimensionPixelSize, 0);
    }

    private void requestChildrenUpdate() {
        if (!this.mChildrenUpdateRequested) {
            getViewTreeObserver().addOnPreDrawListener(this.mChildrenUpdater);
            this.mChildrenUpdateRequested = true;
            invalidate();
        }
    }

    public void enterModal(NotificationEntry notificationEntry) {
        removeRow();
        removeMenu();
        this.mEntry = notificationEntry;
        if (!(notificationEntry.getModalRow().getIntrinsicHeight() == 0 || notificationEntry.getModalRow().getActualHeight() == 0) || this.mFirstAddUpdateRequested) {
            addRow(notificationEntry);
            addMenu(notificationEntry);
            this.animationProperties.setAnimationEndAction(null);
            requestChildrenUpdate();
            RowAnimationUtils rowAnimationUtils = RowAnimationUtils.INSTANCE;
            RowAnimationUtils.startTouchAnimationIfNeed(this.mModalRow, 1.0f);
            return;
        }
        this.mFirstAddUpdateRequested = true;
        addView(notificationEntry.getModalRow());
        getViewTreeObserver().addOnPreDrawListener(this.mFirstAddUpdater);
    }

    public void exitModal(NotificationEntry notificationEntry) {
        ExpandableNotificationRow row = notificationEntry.getRow();
        this.mModalRow.getViewState().yTranslation = getRowTranslationY(row);
        this.mMenuViewState.yTranslation = getMenuYInNss(row);
        this.mMenuViewState.alpha = 0.0f;
        if (((NotificationEntryManager) Dependency.get(NotificationEntryManager.class)).getAllNotifs().contains(notificationEntry) && row.isExpanded() != this.mModalRow.isExpanded()) {
            this.mModalRow.setUserExpanded(row.isExpanded());
            this.mModalRow.notifyHeightChanged(true);
        }
        requestChildrenUpdate();
        this.animationProperties.setAnimationEndAction(new Consumer() {
            /* class com.android.systemui.statusbar.notification.modal.$$Lambda$ModalWindowView$oI3Y_wjdyv_rwMYmm3JiEez4Jsk */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ModalWindowView.this.lambda$exitModal$0$ModalWindowView((Property) obj);
            }
        });
        this.mEntry = null;
    }

    /* access modifiers changed from: public */
    /* access modifiers changed from: private */
    /* renamed from: lambda$exitModal$0 */
    public /* synthetic */ void lambda$exitModal$0$ModalWindowView(Property property) {
        removeRow();
        removeMenu();
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateResource();
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        int i = layoutParams.width;
        int i2 = this.mLayoutWidth;
        if (i != i2) {
            layoutParams.width = i2;
            setLayoutParams(layoutParams);
        }
        NotificationEntry notificationEntry = this.mEntry;
        if (notificationEntry == null || notificationEntry.getModalRow() != null) {
            removeModalDialogImmediately();
            reAddEntry(this.mEntry);
            return;
        }
        ((ModalController) Dependency.get(ModalController.class)).exitModalImmediately();
    }

    private void reAddEntry(NotificationEntry notificationEntry) {
        if (notificationEntry != null && !this.mFirstAddUpdateRequested) {
            removeRow();
            removeMenu();
            addRow(notificationEntry);
            addMenu(notificationEntry);
            requestChildrenUpdate();
        }
    }

    private void addRow(NotificationEntry notificationEntry) {
        ExpandableNotificationRow modalRow = notificationEntry.getModalRow();
        this.mModalRow = modalRow;
        modalRow.setOnHeightChangedListener(this.mOnHeightChangedListener);
        if (this.mModalRow.getParent() == null) {
            addView(this.mModalRow);
        }
        if (!this.mModalRow.isExpanded()) {
            this.mModalRow.setUserExpanded(true);
            this.mModalRow.notifyHeightChanged(false);
        }
        ExpandableNotificationRow row = notificationEntry.getRow();
        row.getViewState().applyToView(this.mModalRow);
        float rowTranslationY = getRowTranslationY(row);
        this.mModalRow.setTranslationY(rowTranslationY);
        this.mModalRow.getViewState().yTranslation = rowTranslationY - Math.max(0.0f, (((float) this.mModalRow.getIntrinsicHeight()) + rowTranslationY) - ((float) this.mMaxModalBottom));
    }

    public void removeRow() {
        ExpandableNotificationRow expandableNotificationRow = this.mModalRow;
        if (expandableNotificationRow != null) {
            removeView(expandableNotificationRow);
            this.mModalRow.setOnHeightChangedListener(null);
            this.mModalRow = null;
        }
    }

    public void addMenu(NotificationEntry notificationEntry) {
        View menuView = notificationEntry.getRow().getProvider().getMenuView();
        this.mMenuView = menuView;
        if (menuView.getParent() == null) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2);
            layoutParams.gravity = 1;
            addView(this.mMenuView, layoutParams);
        }
        this.mMenuView.setTranslationY(getMenuYInNss(notificationEntry.getRow()));
        this.mMenuView.setAlpha(0.0f);
        this.mMenuView.setVisibility(0);
        this.mMenuViewState.initFrom(this.mMenuView);
        this.mMenuViewState.yTranslation = getMenuYInModal(this.mModalRow, false);
        this.mMenuViewState.alpha = 1.0f;
    }

    public void addModalDialog(View view) {
        if (this.mDialogContainer == null) {
            FrameLayout frameLayout = new FrameLayout(getContext());
            this.mDialogContainer = frameLayout;
            frameLayout.setOnClickListener($$Lambda$ModalWindowView$3iIDpaC3fi0FVjmks_JDh5IlvF8.INSTANCE);
        }
        addView(this.mDialogContainer);
        this.mModalDialog = view;
        view.setTranslationY(this.mMenuView.getTranslationY() - ((float) this.mModalDialogMarginTopDelta));
        this.mDialogContainer.addView(this.mModalDialog, new FrameLayout.LayoutParams(-1, -2));
        this.mModalDialog.setAlpha(0.0f);
        this.mModalDialogViewState.initFrom(this.mModalDialog);
        post(new Runnable() {
            /* class com.android.systemui.statusbar.notification.modal.$$Lambda$ModalWindowView$U54iK72BAwwFBYVcaqh07wonlH4 */

            public final void run() {
                ModalWindowView.this.lambda$addModalDialog$2$ModalWindowView();
            }
        });
    }

    /* access modifiers changed from: public */
    /* access modifiers changed from: private */
    /* renamed from: lambda$addModalDialog$2 */
    public /* synthetic */ void lambda$addModalDialog$2$ModalWindowView() {
        float translationY = this.mModalDialog.getTranslationY() + ((float) this.mModalDialog.getMeasuredHeight()) + 20.0f;
        this.mModalDialogTempY = 0.0f;
        int i = this.mScreenHeight;
        if (translationY > ((float) i)) {
            this.mModalDialogTempY = translationY - ((float) i);
        }
        ViewState viewState = this.mMenuViewState;
        viewState.yTranslation -= this.mModalDialogTempY;
        viewState.alpha = 0.0f;
        ExpandableViewState viewState2 = this.mModalRow.getViewState();
        float f = viewState2.yTranslation;
        float f2 = this.mModalDialogTempY;
        viewState2.yTranslation = f - f2;
        ViewState viewState3 = this.mModalDialogViewState;
        viewState3.yTranslation -= f2;
        viewState3.alpha = 1.0f;
        this.animationProperties.setAnimationEndAction(null);
        requestChildrenUpdate();
    }

    public void removeModalDialog() {
        ViewState viewState = this.mMenuViewState;
        viewState.yTranslation += this.mModalDialogTempY;
        viewState.alpha = 1.0f;
        ExpandableViewState viewState2 = this.mModalRow.getViewState();
        float f = viewState2.yTranslation;
        float f2 = this.mModalDialogTempY;
        viewState2.yTranslation = f + f2;
        ViewState viewState3 = this.mModalDialogViewState;
        viewState3.yTranslation += f2;
        viewState3.alpha = 0.0f;
        this.animationProperties.setAnimationEndAction(new Consumer() {
            /* class com.android.systemui.statusbar.notification.modal.$$Lambda$ModalWindowView$jpmneB804fZURNB5twb5UFiz_t0 */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ModalWindowView.this.lambda$removeModalDialog$3$ModalWindowView((Property) obj);
            }
        });
        requestChildrenUpdate();
    }

    /* access modifiers changed from: public */
    /* access modifiers changed from: private */
    /* renamed from: lambda$removeModalDialog$3 */
    public /* synthetic */ void lambda$removeModalDialog$3$ModalWindowView(Property property) {
        removeModalDialogImmediately();
    }

    public boolean isModalDialogMode() {
        return this.mModalDialog != null;
    }

    public void removeModalDialogImmediately() {
        View view;
        FrameLayout frameLayout = this.mDialogContainer;
        if (frameLayout != null && (view = this.mModalDialog) != null) {
            frameLayout.removeView(view);
            removeView(this.mDialogContainer);
            this.mModalDialog = null;
        }
    }

    public void removeMenu() {
        View view = this.mMenuView;
        if (view != null) {
            removeView(view);
            this.mMenuView = null;
        }
    }

    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (z) {
            requestFocus();
        }
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (super.dispatchKeyEvent(keyEvent)) {
            return true;
        }
        if (keyEvent.getAction() != 1 || keyEvent.getKeyCode() != 4) {
            return false;
        }
        ((ModalController) Dependency.get(ModalController.class)).animExitModal(ModalExitMode.OTHER.name());
        return true;
    }

    private float getRowTranslationY(ExpandableNotificationRow expandableNotificationRow) {
        if (expandableNotificationRow.isChildInGroup()) {
            return expandableNotificationRow.getNotificationParent().getTranslationY() + expandableNotificationRow.getTranslationY();
        }
        return expandableNotificationRow.getTranslationY();
    }

    private float getMenuYInNss(ExpandableNotificationRow expandableNotificationRow) {
        return getRowTranslationY(expandableNotificationRow) + ((float) expandableNotificationRow.getActualHeight()) + ((float) this.mModalMenuMarginTop);
    }

    private float getMenuYInModal(ExpandableNotificationRow expandableNotificationRow, boolean z) {
        return expandableNotificationRow.getViewState().yTranslation + ((float) (z ? expandableNotificationRow.getActualHeight() : expandableNotificationRow.getIntrinsicHeight())) + ((float) this.mModalMenuMarginTop);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (!this.mTouchHelper.onInterceptTouchEvent(motionEvent)) {
            return super.onInterceptTouchEvent(motionEvent);
        }
        return true;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!this.mTouchHelper.onTouchEvent(motionEvent)) {
            return super.onTouchEvent(motionEvent);
        }
        return true;
    }

    @Override // com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchCallback
    public void onMiniWindowTrackingUpdate(float f) {
        updateMenuLayoutVisibility(f == 0.0f);
    }

    @Override // com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchCallback
    public void onMiniWindowReset() {
        updateMenuLayoutVisibility(true);
    }

    private void updateMenuLayoutVisibility(boolean z) {
        float f = z ? 1.0f : 0.0f;
        boolean z2 = this.mMenuViewState.alpha != f;
        this.mMenuViewState.alpha = f;
        if (z2) {
            requestChildrenUpdate();
        }
    }

    @Override // com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchCallback
    public void onStartMiniWindowExpandAnimation() {
        ((ModalController) Dependency.get(ModalController.class)).animExitModal(500, false, ModalExitMode.DOWNPULL.name());
        ((CommandQueue) Dependency.get(CommandQueue.class)).animateCollapsePanels(0, false);
    }

    @Override // com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchCallback
    public void onMiniWindowAppLaunched() {
        ((ModalController) Dependency.get(ModalController.class)).exitModalImmediately();
    }

    @Override // com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchCallback
    public ExpandableView getChildAtRawPosition(float f, float f2) {
        ExpandableNotificationRow expandableNotificationRow = this.mModalRow;
        if (expandableNotificationRow == null || !expandableNotificationRow.isAttachedToWindow()) {
            return null;
        }
        expandableNotificationRow.getLocationInWindow(this.mTmpLoc);
        int[] iArr = this.mTmpLoc;
        if (f <= ((float) iArr[0]) || f >= ((float) (iArr[0] + expandableNotificationRow.getWidth()))) {
            return null;
        }
        int[] iArr2 = this.mTmpLoc;
        if (f2 <= ((float) iArr2[1]) || f2 >= ((float) (iArr2[1] + expandableNotificationRow.getHeight()))) {
            return null;
        }
        return expandableNotificationRow;
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        Insets insetsIgnoringVisibility = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
        boolean z = true;
        if (getFitsSystemWindows()) {
            if (insetsIgnoringVisibility.top == getPaddingTop() && insetsIgnoringVisibility.bottom == getPaddingBottom()) {
                z = false;
            }
            if (z) {
                setPadding(0, 0, 0, 0);
            }
        } else {
            if (getPaddingLeft() == this.mSidePaddings && getPaddingRight() == this.mSidePaddings && getPaddingTop() == 0 && getPaddingBottom() == 0) {
                z = false;
            }
            if (z) {
                int i = this.mSidePaddings;
                setPadding(i, 0, i, 0);
            }
        }
        this.mLeftInset = 0;
        this.mRightInset = 0;
        DisplayCutout displayCutout = getRootWindowInsets().getDisplayCutout();
        if (displayCutout != null) {
            this.mLeftInset = displayCutout.getSafeInsetLeft();
            this.mRightInset = displayCutout.getSafeInsetRight();
        }
        this.mLeftInset = Math.max(insetsIgnoringVisibility.left, this.mLeftInset);
        this.mRightInset = Math.max(insetsIgnoringVisibility.right, this.mRightInset);
        applyMargins();
        return windowInsets;
    }

    private void applyMargins() {
        if (getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
            if (marginLayoutParams.rightMargin != this.mRightInset || marginLayoutParams.leftMargin != this.mLeftInset) {
                marginLayoutParams.rightMargin = this.mRightInset / 2;
                marginLayoutParams.leftMargin = this.mLeftInset / 2;
                setLayoutParams(marginLayoutParams);
            }
        }
    }
}
