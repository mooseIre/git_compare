package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.app.Fragment;
import android.app.IApplicationThread;
import android.app.IWallpaperManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProfilerInfo;
import android.app.StatusBarManager;
import android.app.UiModeManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.metrics.LogMaker;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.service.dreams.IDreamManager;
import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.view.Display;
import android.view.InsetsState;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.RemoteAnimationAdapter;
import android.view.ThreadedRenderer;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityManager;
import android.widget.DateTimeView;
import android.widget.ImageView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.colorextraction.ColorExtractor;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.logging.UiEventLoggerImpl;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.RegisterStatusBarResult;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.view.AppearanceRegion;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.MiuiDozeServiceHost;
import com.android.keyguard.ViewMediatorCallback;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperControllerImpl;
import com.android.keyguard.wallpaper.WallpaperCommandSender;
import com.android.systemui.ActivityIntentHelper;
import com.android.systemui.AutoReinflateContainer;
import com.android.systemui.C0005R$array;
import com.android.systemui.C0007R$bool;
import com.android.systemui.C0012R$id;
import com.android.systemui.C0013R$integer;
import com.android.systemui.C0018R$string;
import com.android.systemui.C0019R$style;
import com.android.systemui.DejankUtils;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.EventLogTags;
import com.android.systemui.InitController;
import com.android.systemui.Prefs;
import com.android.systemui.SystemUI;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.ToggleManagerController;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.charging.WirelessChargingAnimation;
import com.android.systemui.classifier.FalsingLog;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.fragments.ExtensionFragmentListener;
import com.android.systemui.fragments.FragmentHostManager;
import com.android.systemui.keyguard.DismissCallbackRegistry;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.OverlayPlugin;
import com.android.systemui.plugins.PluginDependencyProvider;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.QSContainerImpl;
import com.android.systemui.qs.QSFragment;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.ScreenPinningRequest;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.shared.system.WindowManagerWrapper;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.AutoHideUiElement;
import com.android.systemui.statusbar.BackDropView;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.GestureRecorder;
import com.android.systemui.statusbar.KeyboardShortcuts;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.NotificationPresenter;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.NotificationShadeDepthController;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.NotificationViewHierarchyManager;
import com.android.systemui.statusbar.PulseExpansionHandler;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.SuperStatusBarViewFactory;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.VibratorHelper;
import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;
import com.android.systemui.statusbar.notification.DynamicPrivacyController;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator;
import com.android.systemui.statusbar.notification.NotificationActivityStarter;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.init.NotificationsController;
import com.android.systemui.statusbar.notification.interruption.BypassHeadsUpNotifier;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.modal.ModalController;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotificationGutsManager;
import com.android.systemui.statusbar.notification.stack.NotificationListContainer;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter;
import com.android.systemui.statusbar.phone.dagger.StatusBarComponent;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.ExtensionController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import com.android.systemui.statusbar.policy.RemoteInputQuickSettingsDisabler;
import com.android.systemui.statusbar.policy.UserInfoControllerImpl;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.volume.VolumeComponent;
import com.miui.systemui.util.CommonUtil;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.inject.Provider;

public class StatusBar extends SystemUI implements DemoMode, ActivityStarter, KeyguardStateController.Callback, OnHeadsUpChangedListener, CommandQueue.Callbacks, ColorExtractor.OnColorsChangedListener, ConfigurationController.ConfigurationListener, StatusBarStateController.StateListener, ActivityLaunchAnimator.Callback, ControlPanelController.UseControlPanelChangeListener {
    public static final boolean ONLY_CORE_APPS;
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    private static boolean sBootCompleted = false;
    private static final UiEventLogger sUiEventLogger = new UiEventLoggerImpl();
    private ActivityIntentHelper mActivityIntentHelper;
    private ActivityLaunchAnimator mActivityLaunchAnimator;
    private View mAmbientIndicationContainer;
    private boolean mAppFullscreen;
    private boolean mAppImmersive;
    private int mAppearance;
    private final Lazy<AssistManager> mAssistManagerLazy;
    private final AutoHideController mAutoHideController;
    private final BroadcastReceiver mBannerActionBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.android.systemui.statusbar.banner_action_cancel".equals(action) || "com.android.systemui.statusbar.banner_action_setup".equals(action)) {
                ((NotificationManager) StatusBar.this.mContext.getSystemService("notification")).cancel(5);
                Settings.Secure.putInt(StatusBar.this.mContext.getContentResolver(), "show_note_about_notification_hiding", 0);
                if ("com.android.systemui.statusbar.banner_action_setup".equals(action)) {
                    StatusBar.this.mShadeController.animateCollapsePanels(2, true);
                    StatusBar.this.mContext.startActivity(new Intent("android.settings.ACTION_APP_NOTIFICATION_REDACTION").addFlags(268435456));
                }
            }
        }
    };
    protected IStatusBarService mBarService;
    private final BatteryController mBatteryController;
    private BiometricUnlockController mBiometricUnlockController;
    private final Lazy<BiometricUnlockController> mBiometricUnlockControllerLazy;
    protected boolean mBouncerShowing;
    private boolean mBouncerWasShowingWhenHidden;
    private BrightnessMirrorController mBrightnessMirrorController;
    private boolean mBrightnessMirrorVisible;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int i = 0;
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action)) {
                KeyboardShortcuts.dismiss();
                if (StatusBar.this.mRemoteInputManager.getController() != null) {
                    StatusBar.this.mRemoteInputManager.getController().closeRemoteInputs();
                }
                if (StatusBar.this.mBubbleController.isStackExpanded()) {
                    StatusBar.this.mBubbleController.collapseStack();
                }
                ((ModalController) Dependency.get(ModalController.class)).animExitModal();
                if (StatusBar.this.mLockscreenUserManager.isCurrentProfile(getSendingUserId())) {
                    String stringExtra = intent.getStringExtra("reason");
                    if (stringExtra != null && stringExtra.equals("recentapps")) {
                        i = 2;
                    }
                    StatusBar.this.mShadeController.animateCollapsePanels(i);
                }
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                NotificationShadeWindowController notificationShadeWindowController = StatusBar.this.mNotificationShadeWindowController;
                if (notificationShadeWindowController != null) {
                    notificationShadeWindowController.setNotTouchable(false);
                }
                if (StatusBar.this.mBubbleController.isStackExpanded()) {
                    StatusBar.this.mBubbleController.collapseStack();
                }
                StatusBar.this.finishBarAnimations();
                StatusBar.this.resetUserExpandedStates();
            } else if ("android.app.action.SHOW_DEVICE_MONITORING_DIALOG".equals(action)) {
                StatusBar.this.mQSPanel.showDeviceMonitoringDialog();
            }
        }
    };
    /* access modifiers changed from: private */
    public final BubbleController mBubbleController;
    private final BubbleController.BubbleExpandListener mBubbleExpandListener;
    /* access modifiers changed from: private */
    public final BypassHeadsUpNotifier mBypassHeadsUpNotifier;
    private long[] mCameraLaunchGestureVibePattern;
    /* access modifiers changed from: private */
    public final Runnable mCheckBarModes = new Runnable() {
        public final void run() {
            StatusBar.this.checkBarModes();
        }
    };
    private final SysuiColorExtractor mColorExtractor;
    protected final CommandQueue mCommandQueue;
    private final ConfigurationController mConfigurationController;
    private final ControlPanelController mControlPanelController;
    private final Point mCurrentDisplaySize = new Point();
    private final DarkIconDispatcher mDarkIconDispatcher;
    private boolean mDemoMode;
    private boolean mDemoModeAllowed;
    private final BroadcastReceiver mDemoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.android.systemui.demo".equals(action)) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    String lowerCase = extras.getString("command", "").trim().toLowerCase();
                    if (lowerCase.length() > 0) {
                        try {
                            StatusBar.this.dispatchDemoCommand(lowerCase, extras);
                        } catch (Throwable th) {
                            Log.w("StatusBar", "Error running demo command, intent=" + intent, th);
                        }
                    }
                }
            } else {
                "fake_artwork".equals(action);
            }
        }
    };
    protected boolean mDeviceInteractive;
    protected DevicePolicyManager mDevicePolicyManager;
    /* access modifiers changed from: private */
    public final DeviceProvisionedController mDeviceProvisionedController;
    private int mDisabled1 = 0;
    private int mDisabled2 = 0;
    private final DismissCallbackRegistry mDismissCallbackRegistry;
    protected Display mDisplay;
    private int mDisplayId;
    private final DisplayMetrics mDisplayMetrics;
    private final Optional<Divider> mDividerOptional;
    /* access modifiers changed from: private */
    public final DozeParameters mDozeParameters;
    protected DozeScrimController mDozeScrimController;
    @VisibleForTesting
    DozeServiceHost mDozeServiceHost;
    protected boolean mDozing;
    private NotificationEntry mDraggedDownEntry;
    private IDreamManager mDreamManager;
    private final DynamicPrivacyController mDynamicPrivacyController;
    private boolean mExpandedVisible;
    private final ExtensionController mExtensionController;
    /* access modifiers changed from: private */
    public final FalsingManager mFalsingManager;
    private final GestureRecorder mGestureRec = null;
    protected PowerManager.WakeLock mGestureWakeLock;
    private final View.OnClickListener mGoToLockedShadeListener = new View.OnClickListener() {
        public final void onClick(View view) {
            StatusBar.this.lambda$new$0$StatusBar(view);
        }
    };
    private final NotificationGroupManager mGroupManager;
    private final NotificationGutsManager mGutsManager;
    protected final H mHandler = createHandler();
    private HeadsUpAppearanceController mHeadsUpAppearanceController;
    /* access modifiers changed from: private */
    public final HeadsUpManagerPhone mHeadsUpManager;
    private boolean mHideIconsForBouncer;
    private final StatusBarIconController mIconController;
    private PhoneStatusBarPolicy mIconPolicy;
    private final InitController mInitController;
    private int mInteractingWindows;
    protected boolean mIsKeyguard;
    private boolean mIsOccluded;
    /* access modifiers changed from: private */
    public final KeyguardBypassController mKeyguardBypassController;
    private final KeyguardDismissUtil mKeyguardDismissUtil;
    KeyguardIndicationController mKeyguardIndicationController;
    protected KeyguardManager mKeyguardManager;
    private int mKeyguardNotifications = -1;
    /* access modifiers changed from: private */
    public final KeyguardStateController mKeyguardStateController;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
    private final KeyguardViewMediator mKeyguardViewMediator;
    private ViewMediatorCallback mKeyguardViewMediatorCallback;
    /* access modifiers changed from: private */
    public int mLastCameraLaunchSource;
    private int mLastLoggedStateFingerprint;
    /* access modifiers changed from: private */
    public boolean mLaunchCameraOnFinishedGoingToSleep;
    /* access modifiers changed from: private */
    public boolean mLaunchCameraWhenFinishedWaking;
    private Runnable mLaunchTransitionEndRunnable;
    private final LightBarController mLightBarController;
    private final LightsOutNotifController mLightsOutNotifController;
    private final LockscreenLockIconController mLockscreenLockIconController;
    /* access modifiers changed from: private */
    public final NotificationLockscreenUserManager mLockscreenUserManager;
    protected LockscreenWallpaper mLockscreenWallpaper;
    /* access modifiers changed from: private */
    public final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private final NotificationMediaManager mMediaManager;
    private final MetricsLogger mMetricsLogger;
    private final NavigationBarController mNavigationBarController;
    private final NetworkController mNetworkController;
    private boolean mNoAnimationOnNextBarModeChange;
    private NotificationActivityStarter mNotificationActivityStarter;
    protected NotificationIconAreaController mNotificationIconAreaController;
    protected final NotificationInterruptStateProvider mNotificationInterruptStateProvider;
    private final NotificationLogger mNotificationLogger;
    protected NotificationPanelViewController mNotificationPanelViewController;
    private Lazy<NotificationShadeDepthController> mNotificationShadeDepthControllerLazy;
    protected NotificationShadeWindowController mNotificationShadeWindowController;
    protected NotificationShadeWindowView mNotificationShadeWindowView;
    protected NotificationShadeWindowViewController mNotificationShadeWindowViewController;
    protected NotificationShelf mNotificationShelf;
    /* access modifiers changed from: private */
    public NotificationsController mNotificationsController;
    protected boolean mPanelExpanded;
    protected StatusBarWindowView mPhoneStatusBarWindow;
    private final PluginDependencyProvider mPluginDependencyProvider;
    private final PluginManager mPluginManager;
    private final PowerManager mPowerManager;
    protected StatusBarNotificationPresenter mPresenter;
    /* access modifiers changed from: private */
    public Configuration mPreviousConfig;
    /* access modifiers changed from: private */
    public final PulseExpansionHandler mPulseExpansionHandler;
    private QSContainerImpl mQSContainer;
    /* access modifiers changed from: private */
    public QSPanel mQSPanel;
    private final Object mQueueLock = new Object();
    private final Optional<Recents> mRecentsOptional;
    /* access modifiers changed from: private */
    public final NotificationRemoteInputManager mRemoteInputManager;
    private final RemoteInputQuickSettingsDisabler mRemoteInputQuickSettingsDisabler;
    private View mReportRejectedTouch;
    private final ScreenLifecycle mScreenLifecycle;
    final ScreenLifecycle.Observer mScreenObserver = new ScreenLifecycle.Observer() {
        public void onScreenTurningOn() {
            StatusBar.this.mFalsingManager.onScreenTurningOn();
            StatusBar.this.mNotificationPanelViewController.onScreenTurningOn();
        }

        public void onScreenTurnedOn() {
            StatusBar.this.mScrimController.onScreenTurnedOn();
        }

        public void onScreenTurnedOff() {
            StatusBar.this.mDozeServiceHost.updateDozing();
            StatusBar.this.mFalsingManager.onScreenOff();
            StatusBar.this.mScrimController.onScreenTurnedOff();
            StatusBar.this.updateIsKeyguard();
        }
    };
    private final ScreenPinningRequest mScreenPinningRequest;
    /* access modifiers changed from: private */
    public final ScrimController mScrimController;
    /* access modifiers changed from: private */
    public final ShadeController mShadeController;
    protected ViewGroup mStackScroller;
    protected int mState;
    private final Provider<StatusBarComponent.Builder> mStatusBarComponentBuilder;
    protected StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private int mStatusBarMode;
    private final StatusBarNotificationActivityStarter.Builder mStatusBarNotificationActivityStarterBuilder;
    private final SysuiStatusBarStateController mStatusBarStateController;
    private LogMaker mStatusBarStateLog;
    private final StatusBarTouchableRegionManager mStatusBarTouchableRegionManager;
    protected PhoneStatusBarView mStatusBarView;
    protected StatusBarWindowController mStatusBarWindowController;
    private boolean mStatusBarWindowHidden;
    private int mStatusBarWindowState = 0;
    final Runnable mStopTracing = new Runnable() {
        public final void run() {
            StatusBar.this.lambda$new$21$StatusBar();
        }
    };
    private final SuperStatusBarViewFactory mSuperStatusBarViewFactory;
    /* access modifiers changed from: private */
    public boolean mSupportsAmbientMode;
    private final int[] mTmpInt2 = new int[2];
    private boolean mTopHidesStatusBar;
    private boolean mTransientShown;
    private final Executor mUiBgExecutor;
    private UiModeManager mUiModeManager;
    private final ScrimController.Callback mUnlockScrimCallback = new ScrimController.Callback() {
        public void onFinished() {
            StatusBar statusBar = StatusBar.this;
            if (statusBar.mStatusBarKeyguardViewManager == null) {
                Log.w("StatusBar", "Tried to notify keyguard visibility when mStatusBarKeyguardViewManager was null");
            } else if (statusBar.mKeyguardStateController.isKeyguardFadingAway()) {
                StatusBar.this.mStatusBarKeyguardViewManager.onKeyguardFadedAway();
            }
        }

        public void onCancelled() {
            onFinished();
        }
    };
    private final KeyguardUpdateMonitorCallback mUpdateCallback = new KeyguardUpdateMonitorCallback() {
        public void onDreamingStateChanged(boolean z) {
            if (z) {
                StatusBar.this.maybeEscalateHeadsUp();
            }
        }

        public void onStrongAuthStateChanged(int i) {
            super.onStrongAuthStateChanged(i);
            StatusBar.this.mNotificationsController.requestNotificationUpdate("onStrongAuthStateChanged");
        }
    };
    private final UserInfoControllerImpl mUserInfoControllerImpl;
    @VisibleForTesting
    protected boolean mUserSetup = false;
    private final DeviceProvisionedController.DeviceProvisionedListener mUserSetupObserver = new DeviceProvisionedController.DeviceProvisionedListener() {
        public void onUserSetupChanged() {
            boolean isUserSetup = StatusBar.this.mDeviceProvisionedController.isUserSetup(StatusBar.this.mDeviceProvisionedController.getCurrentUser());
            Log.d("StatusBar", "mUserSetupObserver - DeviceProvisionedListener called for user " + StatusBar.this.mDeviceProvisionedController.getCurrentUser());
            StatusBar statusBar = StatusBar.this;
            if (isUserSetup != statusBar.mUserSetup) {
                statusBar.mUserSetup = isUserSetup;
                if (!isUserSetup && statusBar.mStatusBarView != null) {
                    statusBar.animateCollapseQuickSettings();
                }
                StatusBar statusBar2 = StatusBar.this;
                NotificationPanelViewController notificationPanelViewController = statusBar2.mNotificationPanelViewController;
                if (notificationPanelViewController != null) {
                    notificationPanelViewController.setUserSetupComplete(statusBar2.mUserSetup);
                }
                StatusBar.this.updateQsExpansionEnabled();
            }
        }
    };
    private final UserSwitcherController mUserSwitcherController;
    private boolean mVibrateOnOpening;
    private Vibrator mVibrator;
    private final VibratorHelper mVibratorHelper;
    private final NotificationViewHierarchyManager mViewHierarchyManager;
    protected boolean mVisible;
    private boolean mVisibleToUser;
    /* access modifiers changed from: private */
    public final VisualStabilityManager mVisualStabilityManager;
    private final VolumeComponent mVolumeComponent;
    /* access modifiers changed from: private */
    public boolean mWakeUpComingFromTouch;
    /* access modifiers changed from: private */
    public final NotificationWakeUpCoordinator mWakeUpCoordinator;
    /* access modifiers changed from: private */
    public PointF mWakeUpTouchLocation;
    private final WakefulnessLifecycle mWakefulnessLifecycle;
    @VisibleForTesting
    final WakefulnessLifecycle.Observer mWakefulnessObserver = new WakefulnessLifecycle.Observer() {
        public void onFinishedGoingToSleep() {
            StatusBar.this.mNotificationPanelViewController.onAffordanceLaunchEnded();
            StatusBar.this.releaseGestureWakeLock();
            boolean unused = StatusBar.this.mLaunchCameraWhenFinishedWaking = false;
            StatusBar statusBar = StatusBar.this;
            statusBar.mDeviceInteractive = false;
            boolean unused2 = statusBar.mWakeUpComingFromTouch = false;
            PointF unused3 = StatusBar.this.mWakeUpTouchLocation = null;
            StatusBar.this.mVisualStabilityManager.setScreenOn(false);
            StatusBar.this.updateVisibleToUser();
            StatusBar.this.updateNotificationPanelTouchState();
            StatusBar.this.mNotificationShadeWindowViewController.cancelCurrentTouch();
            if (StatusBar.this.mLaunchCameraOnFinishedGoingToSleep) {
                boolean unused4 = StatusBar.this.mLaunchCameraOnFinishedGoingToSleep = false;
                StatusBar.this.mHandler.post(new Runnable() {
                    public final void run() {
                        StatusBar.AnonymousClass12.this.lambda$onFinishedGoingToSleep$0$StatusBar$12();
                    }
                });
            }
            StatusBar.this.updateIsKeyguard();
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onFinishedGoingToSleep$0 */
        public /* synthetic */ void lambda$onFinishedGoingToSleep$0$StatusBar$12() {
            StatusBar statusBar = StatusBar.this;
            statusBar.onCameraLaunchGestureDetected(statusBar.mLastCameraLaunchSource);
        }

        public void onStartedGoingToSleep() {
            DejankUtils.startDetectingBlockingIpcs("StatusBar#onStartedGoingToSleep");
            StatusBar.this.updateNotificationPanelTouchState();
            StatusBar.this.notifyHeadsUpGoingToSleep();
            StatusBar.this.dismissVolumeDialog();
            StatusBar.this.mWakeUpCoordinator.setFullyAwake(false);
            StatusBar.this.mBypassHeadsUpNotifier.setFullyAwake(false);
            StatusBar.this.mKeyguardBypassController.onStartedGoingToSleep();
            DejankUtils.stopDetectingBlockingIpcs("StatusBar#onStartedGoingToSleep");
        }

        public void onStartedWakingUp() {
            DejankUtils.startDetectingBlockingIpcs("StatusBar#onStartedWakingUp");
            StatusBar statusBar = StatusBar.this;
            statusBar.mDeviceInteractive = true;
            statusBar.mWakeUpCoordinator.setWakingUp(true);
            if (!StatusBar.this.mKeyguardBypassController.getBypassEnabled()) {
                StatusBar.this.mHeadsUpManager.releaseAllImmediately();
            }
            StatusBar.this.mVisualStabilityManager.setScreenOn(true);
            StatusBar.this.updateVisibleToUser();
            StatusBar.this.updateIsKeyguard();
            StatusBar.this.mDozeServiceHost.stopDozing();
            StatusBar.this.updateNotificationPanelTouchState();
            StatusBar.this.mPulseExpansionHandler.onStartedWakingUp();
            DejankUtils.stopDetectingBlockingIpcs("StatusBar#onStartedWakingUp");
        }

        public void onFinishedWakingUp() {
            StatusBar.this.mWakeUpCoordinator.setFullyAwake(true);
            StatusBar.this.mBypassHeadsUpNotifier.setFullyAwake(true);
            StatusBar.this.mWakeUpCoordinator.setWakingUp(false);
            if (StatusBar.this.mLaunchCameraWhenFinishedWaking) {
                StatusBar statusBar = StatusBar.this;
                statusBar.mNotificationPanelViewController.launchCamera(false, statusBar.mLastCameraLaunchSource);
                boolean unused = StatusBar.this.mLaunchCameraWhenFinishedWaking = false;
            }
            StatusBar.this.updateScrimController();
        }
    };
    private final BroadcastReceiver mWallpaperChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (!StatusBar.this.mWallpaperSupported) {
                Log.wtf("StatusBar", "WallpaperManager not supported");
                return;
            }
            WallpaperInfo wallpaperInfo = ((WallpaperManager) context.getSystemService(WallpaperManager.class)).getWallpaperInfo(-2);
            boolean z = StatusBar.this.mContext.getResources().getBoolean(17891426);
            boolean unused = StatusBar.this.mSupportsAmbientMode = z && wallpaperInfo != null && wallpaperInfo.supportsAmbientMode();
            StatusBar statusBar = StatusBar.this;
            statusBar.mNotificationShadeWindowController.setWallpaperSupportsAmbientMode(statusBar.mSupportsAmbientMode);
            StatusBar.this.mScrimController.setWallpaperSupportsAmbientMode(StatusBar.this.mSupportsAmbientMode);
            Configuration unused2 = StatusBar.this.mPreviousConfig = new Configuration(StatusBar.this.mContext.getResources().getConfiguration());
            ((MiuiKeyguardWallpaperControllerImpl) Dependency.get(MiuiKeyguardWallpaperControllerImpl.class)).setWallpaperSupportsAmbientMode(StatusBar.this.mSupportsAmbientMode);
            Log.d("StatusBar", "deviceSupportsAodWallpaper:" + z + " supportsAmbientMode:" + StatusBar.this.mSupportsAmbientMode);
        }
    };
    /* access modifiers changed from: private */
    public boolean mWallpaperSupported;
    private boolean mWereIconsJustHidden;
    protected WindowManager mWindowManager;

    private static int barMode(boolean z, int i) {
        if (z) {
            return 1;
        }
        if ((i & 5) == 5) {
            return 3;
        }
        if ((i & 4) != 0) {
            return 6;
        }
        return (i & 1) != 0 ? 4 : 0;
    }

    private static int getLoggingFingerprint(int i, boolean z, boolean z2, boolean z3, boolean z4, boolean z5) {
        return (i & 255) | ((z ? 1 : 0) << true) | ((z2 ? 1 : 0) << true) | ((z3 ? 1 : 0) << true) | ((z4 ? 1 : 0) << true) | ((z5 ? 1 : 0) << true);
    }

    public /* synthetic */ boolean lambda$executeRunnableDismissingKeyguard$18$StatusBar(Runnable runnable, boolean z, boolean z2) {
        lambda$executeRunnableDismissingKeyguard$18(runnable, z, z2);
        return z2;
    }

    static {
        boolean z;
        try {
            z = IPackageManager.Stub.asInterface(ServiceManager.getService("package")).isOnlyCoreApps();
        } catch (RemoteException unused) {
            z = false;
        }
        ONLY_CORE_APPS = z;
    }

    @VisibleForTesting
    public enum StatusBarUiEvent implements UiEventLogger.UiEventEnum {
        LOCKSCREEN_OPEN_SECURE(405),
        LOCKSCREEN_OPEN_INSECURE(406),
        LOCKSCREEN_CLOSE_SECURE(407),
        LOCKSCREEN_CLOSE_INSECURE(408),
        BOUNCER_OPEN_SECURE(409),
        BOUNCER_OPEN_INSECURE(410),
        BOUNCER_CLOSE_SECURE(411),
        BOUNCER_CLOSE_INSECURE(412);
        
        private final int mId;

        private StatusBarUiEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$StatusBar(View view) {
        if (this.mState == 1) {
            wakeUpIfDozing(SystemClock.uptimeMillis(), view, "SHADE_CLICK");
            goToLockedShade((View) null);
        }
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public StatusBar(Context context, NotificationsController notificationsController, LightBarController lightBarController, AutoHideController autoHideController, KeyguardUpdateMonitor keyguardUpdateMonitor, StatusBarIconController statusBarIconController, PulseExpansionHandler pulseExpansionHandler, NotificationWakeUpCoordinator notificationWakeUpCoordinator, KeyguardBypassController keyguardBypassController, KeyguardStateController keyguardStateController, HeadsUpManagerPhone headsUpManagerPhone, DynamicPrivacyController dynamicPrivacyController, BypassHeadsUpNotifier bypassHeadsUpNotifier, FalsingManager falsingManager, BroadcastDispatcher broadcastDispatcher, RemoteInputQuickSettingsDisabler remoteInputQuickSettingsDisabler, NotificationGutsManager notificationGutsManager, NotificationLogger notificationLogger, NotificationInterruptStateProvider notificationInterruptStateProvider, NotificationViewHierarchyManager notificationViewHierarchyManager, KeyguardViewMediator keyguardViewMediator, DisplayMetrics displayMetrics, MetricsLogger metricsLogger, Executor executor, NotificationMediaManager notificationMediaManager, NotificationLockscreenUserManager notificationLockscreenUserManager, NotificationRemoteInputManager notificationRemoteInputManager, UserSwitcherController userSwitcherController, NetworkController networkController, BatteryController batteryController, SysuiColorExtractor sysuiColorExtractor, ScreenLifecycle screenLifecycle, WakefulnessLifecycle wakefulnessLifecycle, SysuiStatusBarStateController sysuiStatusBarStateController, VibratorHelper vibratorHelper, BubbleController bubbleController, NotificationGroupManager notificationGroupManager, VisualStabilityManager visualStabilityManager, DeviceProvisionedController deviceProvisionedController, NavigationBarController navigationBarController, Lazy<AssistManager> lazy, ConfigurationController configurationController, NotificationShadeWindowController notificationShadeWindowController, LockscreenLockIconController lockscreenLockIconController, DozeParameters dozeParameters, ScrimController scrimController, KeyguardLiftController keyguardLiftController, Lazy<LockscreenWallpaper> lazy2, Lazy<BiometricUnlockController> lazy3, MiuiDozeServiceHost miuiDozeServiceHost, PowerManager powerManager, ScreenPinningRequest screenPinningRequest, DozeScrimController dozeScrimController, VolumeComponent volumeComponent, CommandQueue commandQueue, Optional<Recents> optional, Provider<StatusBarComponent.Builder> provider, PluginManager pluginManager, Optional<Divider> optional2, LightsOutNotifController lightsOutNotifController, StatusBarNotificationActivityStarter.Builder builder, ShadeController shadeController, SuperStatusBarViewFactory superStatusBarViewFactory, StatusBarKeyguardViewManager statusBarKeyguardViewManager, ViewMediatorCallback viewMediatorCallback, InitController initController, DarkIconDispatcher darkIconDispatcher, Handler handler, PluginDependencyProvider pluginDependencyProvider, KeyguardDismissUtil keyguardDismissUtil, ExtensionController extensionController, UserInfoControllerImpl userInfoControllerImpl, PhoneStatusBarPolicy phoneStatusBarPolicy, KeyguardIndicationController keyguardIndicationController, DismissCallbackRegistry dismissCallbackRegistry, Lazy<NotificationShadeDepthController> lazy4, StatusBarTouchableRegionManager statusBarTouchableRegionManager, ControlPanelController controlPanelController) {
        super(context);
        this.mNotificationsController = notificationsController;
        this.mLightBarController = lightBarController;
        this.mAutoHideController = autoHideController;
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        this.mIconController = statusBarIconController;
        this.mPulseExpansionHandler = pulseExpansionHandler;
        this.mWakeUpCoordinator = notificationWakeUpCoordinator;
        this.mKeyguardBypassController = keyguardBypassController;
        this.mKeyguardStateController = keyguardStateController;
        this.mHeadsUpManager = headsUpManagerPhone;
        this.mKeyguardIndicationController = keyguardIndicationController;
        this.mStatusBarTouchableRegionManager = statusBarTouchableRegionManager;
        this.mDynamicPrivacyController = dynamicPrivacyController;
        this.mBypassHeadsUpNotifier = bypassHeadsUpNotifier;
        this.mFalsingManager = falsingManager;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mRemoteInputQuickSettingsDisabler = remoteInputQuickSettingsDisabler;
        this.mGutsManager = notificationGutsManager;
        this.mNotificationLogger = notificationLogger;
        this.mNotificationInterruptStateProvider = notificationInterruptStateProvider;
        this.mViewHierarchyManager = notificationViewHierarchyManager;
        this.mKeyguardViewMediator = keyguardViewMediator;
        this.mDisplayMetrics = displayMetrics;
        this.mMetricsLogger = metricsLogger;
        this.mUiBgExecutor = executor;
        this.mMediaManager = notificationMediaManager;
        this.mLockscreenUserManager = notificationLockscreenUserManager;
        this.mRemoteInputManager = notificationRemoteInputManager;
        this.mUserSwitcherController = userSwitcherController;
        this.mNetworkController = networkController;
        this.mBatteryController = batteryController;
        this.mColorExtractor = sysuiColorExtractor;
        this.mScreenLifecycle = screenLifecycle;
        this.mWakefulnessLifecycle = wakefulnessLifecycle;
        this.mStatusBarStateController = sysuiStatusBarStateController;
        this.mVibratorHelper = vibratorHelper;
        this.mBubbleController = bubbleController;
        this.mGroupManager = notificationGroupManager;
        this.mVisualStabilityManager = visualStabilityManager;
        this.mDeviceProvisionedController = deviceProvisionedController;
        this.mNavigationBarController = navigationBarController;
        this.mAssistManagerLazy = lazy;
        this.mConfigurationController = configurationController;
        this.mNotificationShadeWindowController = notificationShadeWindowController;
        this.mLockscreenLockIconController = lockscreenLockIconController;
        this.mDozeServiceHost = miuiDozeServiceHost;
        this.mPowerManager = powerManager;
        this.mDozeParameters = dozeParameters;
        this.mScrimController = scrimController;
        this.mScreenPinningRequest = screenPinningRequest;
        this.mDozeScrimController = dozeScrimController;
        this.mBiometricUnlockControllerLazy = lazy3;
        this.mNotificationShadeDepthControllerLazy = lazy4;
        this.mVolumeComponent = volumeComponent;
        this.mCommandQueue = commandQueue;
        this.mRecentsOptional = optional;
        this.mStatusBarComponentBuilder = provider;
        this.mPluginManager = pluginManager;
        this.mDividerOptional = optional2;
        this.mStatusBarNotificationActivityStarterBuilder = builder;
        this.mShadeController = shadeController;
        this.mSuperStatusBarViewFactory = superStatusBarViewFactory;
        this.mLightsOutNotifController = lightsOutNotifController;
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
        this.mKeyguardViewMediatorCallback = viewMediatorCallback;
        this.mInitController = initController;
        this.mDarkIconDispatcher = darkIconDispatcher;
        this.mPluginDependencyProvider = pluginDependencyProvider;
        this.mKeyguardDismissUtil = keyguardDismissUtil;
        this.mExtensionController = extensionController;
        this.mUserInfoControllerImpl = userInfoControllerImpl;
        this.mIconPolicy = phoneStatusBarPolicy;
        this.mDismissCallbackRegistry = dismissCallbackRegistry;
        this.mControlPanelController = controlPanelController;
        this.mBubbleExpandListener = new BubbleController.BubbleExpandListener() {
            public final void onBubbleExpandChanged(boolean z, String str) {
                StatusBar.this.lambda$new$1$StatusBar(z, str);
            }
        };
        DateTimeView.setReceiverHandler(handler);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$StatusBar(boolean z, String str) {
        this.mNotificationsController.requestNotificationUpdate("onBubbleExpandChanged");
        updateScrimController();
    }

    public void start() {
        RegisterStatusBarResult registerStatusBarResult;
        this.mScreenLifecycle.addObserver(this.mScreenObserver);
        this.mWakefulnessLifecycle.addObserver(this.mWakefulnessObserver);
        this.mUiModeManager = (UiModeManager) this.mContext.getSystemService(UiModeManager.class);
        this.mBypassHeadsUpNotifier.setUp();
        this.mBubbleController.setExpandListener(this.mBubbleExpandListener);
        this.mActivityIntentHelper = new ActivityIntentHelper(this.mContext);
        this.mColorExtractor.addOnColorsChangedListener(this);
        this.mStatusBarStateController.addCallback(this, 0);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mDreamManager = IDreamManager.Stub.asInterface(ServiceManager.checkService("dreams"));
        Display defaultDisplay = this.mWindowManager.getDefaultDisplay();
        this.mDisplay = defaultDisplay;
        this.mDisplayId = defaultDisplay.getDisplayId();
        updateDisplaySize();
        this.mVibrateOnOpening = this.mContext.getResources().getBoolean(C0007R$bool.config_vibrateOnIconAnimation);
        WindowManagerGlobal.getWindowManagerService();
        this.mDevicePolicyManager = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        AccessibilityManager accessibilityManager = (AccessibilityManager) this.mContext.getSystemService("accessibility");
        this.mKeyguardUpdateMonitor.setKeyguardBypassController(this.mKeyguardBypassController);
        this.mBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        this.mWallpaperSupported = ((WallpaperManager) this.mContext.getSystemService(WallpaperManager.class)).isWallpaperSupported();
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
        try {
            registerStatusBarResult = this.mBarService.registerStatusBar(this.mCommandQueue);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            registerStatusBarResult = null;
        }
        createAndAddWindows(registerStatusBarResult);
        if (this.mWallpaperSupported) {
            this.mBroadcastDispatcher.registerReceiver(this.mWallpaperChangedReceiver, new IntentFilter("android.intent.action.WALLPAPER_CHANGED"), (Executor) null, UserHandle.ALL);
            this.mWallpaperChangedReceiver.onReceive(this.mContext, (Intent) null);
        }
        setUpPresenter();
        if (InsetsState.containsType(registerStatusBarResult.mTransientBarTypes, 0)) {
            showTransientUnchecked();
        }
        onSystemBarAppearanceChanged(this.mDisplayId, registerStatusBarResult.mAppearance, registerStatusBarResult.mAppearanceRegions, registerStatusBarResult.mNavbarColorManagedByIme);
        this.mAppFullscreen = registerStatusBarResult.mAppFullscreen;
        this.mAppImmersive = registerStatusBarResult.mAppImmersive;
        setImeWindowStatus(this.mDisplayId, registerStatusBarResult.mImeToken, registerStatusBarResult.mImeWindowVis, registerStatusBarResult.mImeBackDisposition, registerStatusBarResult.mShowImeSwitcher);
        int size = registerStatusBarResult.mIcons.size();
        for (int i = 0; i < size; i++) {
            this.mCommandQueue.setIcon((String) registerStatusBarResult.mIcons.keyAt(i), (StatusBarIcon) registerStatusBarResult.mIcons.valueAt(i));
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.systemui.statusbar.banner_action_cancel");
        intentFilter.addAction("com.android.systemui.statusbar.banner_action_setup");
        this.mContext.registerReceiver(this.mBannerActionBroadcastReceiver, intentFilter, "com.android.systemui.permission.SELF", (Handler) null);
        if (this.mWallpaperSupported) {
            try {
                IWallpaperManager.Stub.asInterface(ServiceManager.getService("wallpaper")).setInAmbientMode(false, 0);
            } catch (RemoteException unused) {
            }
        }
        this.mIconPolicy.init();
        new MiuiStatusBarSignalPolicy(this.mContext, this.mIconController);
        this.mKeyguardStateController.addCallback(this);
        startKeyguard();
        this.mKeyguardUpdateMonitor.registerCallback(this.mUpdateCallback);
        this.mDozeServiceHost.initialize(this, this.mNotificationIconAreaController, this.mStatusBarKeyguardViewManager, this.mNotificationShadeWindowViewController, this.mNotificationPanelViewController, this.mAmbientIndicationContainer);
        this.mConfigurationController.addCallback(this);
        this.mInitController.addPostInitTask(new Runnable(registerStatusBarResult.mDisabledFlags1, registerStatusBarResult.mDisabledFlags2) {
            public final /* synthetic */ int f$1;
            public final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                StatusBar.this.lambda$start$2$StatusBar(this.f$1, this.f$2);
            }
        });
        this.mPluginManager.addPluginListener(new PluginListener<OverlayPlugin>() {
            /* access modifiers changed from: private */
            public ArraySet<OverlayPlugin> mOverlays = new ArraySet<>();

            public void onPluginConnected(OverlayPlugin overlayPlugin, Context context) {
                StatusBar.this.mMainThreadHandler.post(new Runnable(overlayPlugin) {
                    public final /* synthetic */ OverlayPlugin f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        StatusBar.AnonymousClass5.this.lambda$onPluginConnected$0$StatusBar$5(this.f$1);
                    }
                });
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onPluginConnected$0 */
            public /* synthetic */ void lambda$onPluginConnected$0$StatusBar$5(OverlayPlugin overlayPlugin) {
                overlayPlugin.setup(StatusBar.this.getNotificationShadeWindowView(), StatusBar.this.getNavigationBarView(), new Callback(overlayPlugin), StatusBar.this.mDozeParameters);
            }

            public void onPluginDisconnected(OverlayPlugin overlayPlugin) {
                StatusBar.this.mMainThreadHandler.post(new Runnable(overlayPlugin) {
                    public final /* synthetic */ OverlayPlugin f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        StatusBar.AnonymousClass5.this.lambda$onPluginDisconnected$1$StatusBar$5(this.f$1);
                    }
                });
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onPluginDisconnected$1 */
            public /* synthetic */ void lambda$onPluginDisconnected$1$StatusBar$5(OverlayPlugin overlayPlugin) {
                this.mOverlays.remove(overlayPlugin);
                StatusBar.this.mNotificationShadeWindowController.setForcePluginOpen(this.mOverlays.size() != 0);
            }

            /* renamed from: com.android.systemui.statusbar.phone.StatusBar$5$Callback */
            class Callback implements OverlayPlugin.Callback {
                private final OverlayPlugin mPlugin;

                Callback(OverlayPlugin overlayPlugin) {
                    this.mPlugin = overlayPlugin;
                }

                public void onHoldStatusBarOpenChange() {
                    if (this.mPlugin.holdStatusBarOpen()) {
                        AnonymousClass5.this.mOverlays.add(this.mPlugin);
                    } else {
                        AnonymousClass5.this.mOverlays.remove(this.mPlugin);
                    }
                    StatusBar.this.mMainThreadHandler.post(
                    /*  JADX ERROR: Method code generation error
                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x002c: INVOKE  
                          (wrap: android.os.Handler : 0x0023: INVOKE  (r0v4 android.os.Handler) = 
                          (wrap: com.android.systemui.statusbar.phone.StatusBar : 0x0021: IGET  (r0v3 com.android.systemui.statusbar.phone.StatusBar) = 
                          (wrap: com.android.systemui.statusbar.phone.StatusBar$5 : 0x001f: IGET  (r0v2 com.android.systemui.statusbar.phone.StatusBar$5) = 
                          (r2v0 'this' com.android.systemui.statusbar.phone.StatusBar$5$Callback A[THIS])
                         com.android.systemui.statusbar.phone.StatusBar.5.Callback.this$1 com.android.systemui.statusbar.phone.StatusBar$5)
                         com.android.systemui.statusbar.phone.StatusBar.5.this$0 com.android.systemui.statusbar.phone.StatusBar)
                         com.android.systemui.statusbar.phone.StatusBar.access$1000(com.android.systemui.statusbar.phone.StatusBar):android.os.Handler type: STATIC)
                          (wrap: com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$U2F2-aeucZtrnZrV13H_iSFQwOM : 0x0029: CONSTRUCTOR  (r1v0 com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$U2F2-aeucZtrnZrV13H_iSFQwOM) = 
                          (r2v0 'this' com.android.systemui.statusbar.phone.StatusBar$5$Callback A[THIS])
                         call: com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$U2F2-aeucZtrnZrV13H_iSFQwOM.<init>(com.android.systemui.statusbar.phone.StatusBar$5$Callback):void type: CONSTRUCTOR)
                         android.os.Handler.post(java.lang.Runnable):boolean type: VIRTUAL in method: com.android.systemui.statusbar.phone.StatusBar.5.Callback.onHoldStatusBarOpenChange():void, dex: classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                        	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                        	at java.base/java.util.ArrayList.forEach(ArrayList.java:1541)
                        	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                        	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                        	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:485)
                        	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                        	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                        	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                        	at jadx.core.codegen.ClassGen.addInnerClass(ClassGen.java:249)
                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:238)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                        	at java.base/java.util.ArrayList.forEach(ArrayList.java:1541)
                        	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                        	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                        	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:485)
                        	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                        	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                        	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                        	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:676)
                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:607)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                        	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                        	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                        	at java.base/java.util.ArrayList.forEach(ArrayList.java:1541)
                        	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                        	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                        	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:485)
                        	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                        	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                        	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:78)
                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0029: CONSTRUCTOR  (r1v0 com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$U2F2-aeucZtrnZrV13H_iSFQwOM) = 
                          (r2v0 'this' com.android.systemui.statusbar.phone.StatusBar$5$Callback A[THIS])
                         call: com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$U2F2-aeucZtrnZrV13H_iSFQwOM.<init>(com.android.systemui.statusbar.phone.StatusBar$5$Callback):void type: CONSTRUCTOR in method: com.android.systemui.statusbar.phone.StatusBar.5.Callback.onHoldStatusBarOpenChange():void, dex: classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                        	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                        	... 76 more
                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$U2F2-aeucZtrnZrV13H_iSFQwOM, state: NOT_LOADED
                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:606)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                        	... 82 more
                        */
                    /*
                        this = this;
                        com.android.systemui.plugins.OverlayPlugin r0 = r2.mPlugin
                        boolean r0 = r0.holdStatusBarOpen()
                        if (r0 == 0) goto L_0x0014
                        com.android.systemui.statusbar.phone.StatusBar$5 r0 = com.android.systemui.statusbar.phone.StatusBar.AnonymousClass5.this
                        android.util.ArraySet r0 = r0.mOverlays
                        com.android.systemui.plugins.OverlayPlugin r1 = r2.mPlugin
                        r0.add(r1)
                        goto L_0x001f
                    L_0x0014:
                        com.android.systemui.statusbar.phone.StatusBar$5 r0 = com.android.systemui.statusbar.phone.StatusBar.AnonymousClass5.this
                        android.util.ArraySet r0 = r0.mOverlays
                        com.android.systemui.plugins.OverlayPlugin r1 = r2.mPlugin
                        r0.remove(r1)
                    L_0x001f:
                        com.android.systemui.statusbar.phone.StatusBar$5 r0 = com.android.systemui.statusbar.phone.StatusBar.AnonymousClass5.this
                        com.android.systemui.statusbar.phone.StatusBar r0 = com.android.systemui.statusbar.phone.StatusBar.this
                        android.os.Handler r0 = r0.mMainThreadHandler
                        com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$U2F2-aeucZtrnZrV13H_iSFQwOM r1 = new com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$U2F2-aeucZtrnZrV13H_iSFQwOM
                        r1.<init>(r2)
                        r0.post(r1)
                        return
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.StatusBar.AnonymousClass5.Callback.onHoldStatusBarOpenChange():void");
                }

                /* access modifiers changed from: private */
                /* renamed from: lambda$onHoldStatusBarOpenChange$2 */
                public /* synthetic */ void lambda$onHoldStatusBarOpenChange$2$StatusBar$5$Callback() {
                    StatusBar.this.mNotificationShadeWindowController.setStateListener(
                    /*  JADX ERROR: Method code generation error
                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000b: INVOKE  
                          (wrap: com.android.systemui.statusbar.phone.NotificationShadeWindowController : 0x0004: IGET  (r0v2 com.android.systemui.statusbar.phone.NotificationShadeWindowController) = 
                          (wrap: com.android.systemui.statusbar.phone.StatusBar : 0x0002: IGET  (r0v1 com.android.systemui.statusbar.phone.StatusBar) = 
                          (wrap: com.android.systemui.statusbar.phone.StatusBar$5 : 0x0000: IGET  (r0v0 com.android.systemui.statusbar.phone.StatusBar$5) = 
                          (r2v0 'this' com.android.systemui.statusbar.phone.StatusBar$5$Callback A[THIS])
                         com.android.systemui.statusbar.phone.StatusBar.5.Callback.this$1 com.android.systemui.statusbar.phone.StatusBar$5)
                         com.android.systemui.statusbar.phone.StatusBar.5.this$0 com.android.systemui.statusbar.phone.StatusBar)
                         com.android.systemui.statusbar.phone.StatusBar.mNotificationShadeWindowController com.android.systemui.statusbar.phone.NotificationShadeWindowController)
                          (wrap: com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$99-TTdt0m5NBU3m1uv-R7PLiNeQ : 0x0008: CONSTRUCTOR  (r1v0 com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$99-TTdt0m5NBU3m1uv-R7PLiNeQ) = 
                          (r2v0 'this' com.android.systemui.statusbar.phone.StatusBar$5$Callback A[THIS])
                         call: com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$99-TTdt0m5NBU3m1uv-R7PLiNeQ.<init>(com.android.systemui.statusbar.phone.StatusBar$5$Callback):void type: CONSTRUCTOR)
                         com.android.systemui.statusbar.phone.NotificationShadeWindowController.setStateListener(com.android.systemui.statusbar.phone.NotificationShadeWindowController$OtherwisedCollapsedListener):void type: VIRTUAL in method: com.android.systemui.statusbar.phone.StatusBar.5.Callback.lambda$onHoldStatusBarOpenChange$2():void, dex: classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                        	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                        	at java.base/java.util.ArrayList.forEach(ArrayList.java:1541)
                        	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                        	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                        	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:485)
                        	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                        	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                        	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                        	at jadx.core.codegen.ClassGen.addInnerClass(ClassGen.java:249)
                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:238)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                        	at java.base/java.util.ArrayList.forEach(ArrayList.java:1541)
                        	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                        	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                        	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:485)
                        	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                        	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                        	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                        	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:676)
                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:607)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                        	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                        	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                        	at java.base/java.util.ArrayList.forEach(ArrayList.java:1541)
                        	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                        	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                        	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:485)
                        	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                        	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                        	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:78)
                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0008: CONSTRUCTOR  (r1v0 com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$99-TTdt0m5NBU3m1uv-R7PLiNeQ) = 
                          (r2v0 'this' com.android.systemui.statusbar.phone.StatusBar$5$Callback A[THIS])
                         call: com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$99-TTdt0m5NBU3m1uv-R7PLiNeQ.<init>(com.android.systemui.statusbar.phone.StatusBar$5$Callback):void type: CONSTRUCTOR in method: com.android.systemui.statusbar.phone.StatusBar.5.Callback.lambda$onHoldStatusBarOpenChange$2():void, dex: classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                        	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                        	... 76 more
                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$99-TTdt0m5NBU3m1uv-R7PLiNeQ, state: NOT_LOADED
                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:606)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                        	... 82 more
                        */
                    /*
                        this = this;
                        com.android.systemui.statusbar.phone.StatusBar$5 r0 = com.android.systemui.statusbar.phone.StatusBar.AnonymousClass5.this
                        com.android.systemui.statusbar.phone.StatusBar r0 = com.android.systemui.statusbar.phone.StatusBar.this
                        com.android.systemui.statusbar.phone.NotificationShadeWindowController r0 = r0.mNotificationShadeWindowController
                        com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$99-TTdt0m5NBU3m1uv-R7PLiNeQ r1 = new com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$99-TTdt0m5NBU3m1uv-R7PLiNeQ
                        r1.<init>(r2)
                        r0.setStateListener(r1)
                        com.android.systemui.statusbar.phone.StatusBar$5 r2 = com.android.systemui.statusbar.phone.StatusBar.AnonymousClass5.this
                        com.android.systemui.statusbar.phone.StatusBar r0 = com.android.systemui.statusbar.phone.StatusBar.this
                        com.android.systemui.statusbar.phone.NotificationShadeWindowController r0 = r0.mNotificationShadeWindowController
                        android.util.ArraySet r2 = r2.mOverlays
                        int r2 = r2.size()
                        if (r2 == 0) goto L_0x0020
                        r2 = 1
                        goto L_0x0021
                    L_0x0020:
                        r2 = 0
                    L_0x0021:
                        r0.setForcePluginOpen(r2)
                        return
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.StatusBar.AnonymousClass5.Callback.lambda$onHoldStatusBarOpenChange$2$StatusBar$5$Callback():void");
                }

                /* access modifiers changed from: private */
                /* renamed from: lambda$onHoldStatusBarOpenChange$1 */
                public /* synthetic */ void lambda$onHoldStatusBarOpenChange$1$StatusBar$5$Callback(boolean z) {
                    AnonymousClass5.this.mOverlays.forEach(
                    /*  JADX ERROR: Method code generation error
                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000b: INVOKE  
                          (wrap: android.util.ArraySet : 0x0002: INVOKE  (r1v2 android.util.ArraySet) = 
                          (wrap: com.android.systemui.statusbar.phone.StatusBar$5 : 0x0000: IGET  (r1v1 com.android.systemui.statusbar.phone.StatusBar$5) = 
                          (r1v0 'this' com.android.systemui.statusbar.phone.StatusBar$5$Callback A[THIS])
                         com.android.systemui.statusbar.phone.StatusBar.5.Callback.this$1 com.android.systemui.statusbar.phone.StatusBar$5)
                         com.android.systemui.statusbar.phone.StatusBar.5.access$1100(com.android.systemui.statusbar.phone.StatusBar$5):android.util.ArraySet type: STATIC)
                          (wrap: com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$X8h8BtL5sx95G3VYQ-SR0g_MCXg : 0x0008: CONSTRUCTOR  (r0v0 com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$X8h8BtL5sx95G3VYQ-SR0g_MCXg) = (r2v0 'z' boolean) call: com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$X8h8BtL5sx95G3VYQ-SR0g_MCXg.<init>(boolean):void type: CONSTRUCTOR)
                         android.util.ArraySet.forEach(java.util.function.Consumer):void type: VIRTUAL in method: com.android.systemui.statusbar.phone.StatusBar.5.Callback.lambda$onHoldStatusBarOpenChange$1(boolean):void, dex: classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                        	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                        	at java.base/java.util.ArrayList.forEach(ArrayList.java:1541)
                        	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                        	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                        	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:485)
                        	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                        	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                        	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                        	at jadx.core.codegen.ClassGen.addInnerClass(ClassGen.java:249)
                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:238)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                        	at java.base/java.util.ArrayList.forEach(ArrayList.java:1541)
                        	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                        	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                        	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:485)
                        	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                        	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                        	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                        	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:676)
                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:607)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                        	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                        	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                        	at java.base/java.util.ArrayList.forEach(ArrayList.java:1541)
                        	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                        	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                        	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:485)
                        	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                        	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                        	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:78)
                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0008: CONSTRUCTOR  (r0v0 com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$X8h8BtL5sx95G3VYQ-SR0g_MCXg) = (r2v0 'z' boolean) call: com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$X8h8BtL5sx95G3VYQ-SR0g_MCXg.<init>(boolean):void type: CONSTRUCTOR in method: com.android.systemui.statusbar.phone.StatusBar.5.Callback.lambda$onHoldStatusBarOpenChange$1(boolean):void, dex: classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                        	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                        	... 76 more
                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$X8h8BtL5sx95G3VYQ-SR0g_MCXg, state: NOT_LOADED
                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:606)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                        	... 82 more
                        */
                    /*
                        this = this;
                        com.android.systemui.statusbar.phone.StatusBar$5 r1 = com.android.systemui.statusbar.phone.StatusBar.AnonymousClass5.this
                        android.util.ArraySet r1 = r1.mOverlays
                        com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$X8h8BtL5sx95G3VYQ-SR0g_MCXg r0 = new com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$5$Callback$X8h8BtL5sx95G3VYQ-SR0g_MCXg
                        r0.<init>(r2)
                        r1.forEach(r0)
                        return
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.StatusBar.AnonymousClass5.Callback.lambda$onHoldStatusBarOpenChange$1$StatusBar$5$Callback(boolean):void");
                }
            }
        }, (Class<?>) OverlayPlugin.class, true);
        this.mControlPanelController.addCallback((ControlPanelController.UseControlPanelChangeListener) this);
        ((ToggleManagerController) Dependency.get(ToggleManagerController.class)).start();
    }

    public void onUseControlPanelChange(boolean z) {
        updateQsExpansionEnabled();
    }

    /* access modifiers changed from: protected */
    public void makeStatusBarView(RegisterStatusBarResult registerStatusBarResult) {
        Class<QS> cls = QS.class;
        Context context = this.mContext;
        updateDisplaySize();
        updateResources(false);
        updateTheme();
        inflateStatusBarWindow();
        this.mNotificationShadeWindowViewController.setService(this, this.mNotificationShadeWindowController);
        this.mNotificationShadeWindowView.setOnTouchListener(getStatusBarWindowTouchListener());
        ViewGroup viewGroup = (ViewGroup) this.mNotificationShadeWindowView.findViewById(C0012R$id.notification_stack_scroller);
        this.mStackScroller = viewGroup;
        this.mNotificationLogger.setUpWithContainer((NotificationListContainer) viewGroup);
        NotificationIconAreaController createNotificationIconAreaController = SystemUIFactory.getInstance().createNotificationIconAreaController(context, this, this.mWakeUpCoordinator, this.mKeyguardBypassController, this.mStatusBarStateController);
        this.mNotificationIconAreaController = createNotificationIconAreaController;
        this.mWakeUpCoordinator.setIconAreaController(createNotificationIconAreaController);
        inflateShelf();
        this.mNotificationIconAreaController.setupShelf(this.mNotificationShelf);
        NotificationPanelViewController notificationPanelViewController = this.mNotificationPanelViewController;
        NotificationIconAreaController notificationIconAreaController = this.mNotificationIconAreaController;
        Objects.requireNonNull(notificationIconAreaController);
        notificationPanelViewController.setOnReinflationListener(new Runnable() {
            public final void run() {
                NotificationIconAreaController.this.initAodIcons();
            }
        });
        this.mNotificationPanelViewController.addExpansionListener(this.mWakeUpCoordinator);
        this.mDarkIconDispatcher.addDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mNotificationIconAreaController);
        this.mPluginDependencyProvider.allowPluginDependency(DarkIconDispatcher.class);
        this.mPluginDependencyProvider.allowPluginDependency(StatusBarStateController.class);
        FragmentHostManager fragmentHostManager = FragmentHostManager.get(this.mPhoneStatusBarWindow);
        fragmentHostManager.addTagListener("CollapsedStatusBarFragment", new FragmentHostManager.FragmentListener() {
            public final void onFragmentViewCreated(String str, Fragment fragment) {
                StatusBar.this.lambda$makeStatusBarView$3$StatusBar(str, fragment);
            }
        });
        fragmentHostManager.getFragmentManager().beginTransaction().replace(C0012R$id.status_bar_container, new MiuiCollapsedStatusBarFragment(), "CollapsedStatusBarFragment").commit();
        this.mHeadsUpManager.setup(this.mVisualStabilityManager);
        this.mStatusBarTouchableRegionManager.setup(this, this.mNotificationShadeWindowView);
        this.mHeadsUpManager.addListener(this);
        this.mHeadsUpManager.addListener(this.mNotificationPanelViewController.getOnHeadsUpChangedListener());
        this.mHeadsUpManager.addListener(this.mVisualStabilityManager);
        this.mNotificationPanelViewController.setHeadsUpManager(this.mHeadsUpManager);
        this.mNotificationLogger.setHeadsUpManager(this.mHeadsUpManager);
        createNavigationBar(registerStatusBarResult);
        this.mKeyguardIndicationController.setIndicationArea((ViewGroup) this.mNotificationShadeWindowView.findViewById(C0012R$id.keyguard_indication_area));
        this.mNotificationPanelViewController.setKeyguardIndicationController(this.mKeyguardIndicationController);
        this.mAmbientIndicationContainer = this.mNotificationShadeWindowView.findViewById(C0012R$id.ambient_indication_container);
        this.mBatteryController.addCallback(new BatteryController.BatteryStateChangeCallback() {
            public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
            }

            public void onPowerSaveChanged(boolean z) {
                StatusBar statusBar = StatusBar.this;
                statusBar.mHandler.post(statusBar.mCheckBarModes);
                DozeServiceHost dozeServiceHost = StatusBar.this.mDozeServiceHost;
                if (dozeServiceHost != null) {
                    dozeServiceHost.firePowerSaveChanged(z);
                }
            }
        });
        this.mAutoHideController.setStatusBar(new AutoHideUiElement() {
            public void synchronizeState() {
                StatusBar.this.checkBarModes();
            }

            public boolean shouldHideOnTouch() {
                return !StatusBar.this.mRemoteInputManager.getController().isRemoteInputActive();
            }

            public boolean isVisible() {
                return StatusBar.this.isTransientShown();
            }

            public void hide() {
                StatusBar.this.clearTransient();
            }
        });
        ScrimView scrimForBubble = this.mBubbleController.getScrimForBubble();
        this.mScrimController.setScrimVisibleListener(new Consumer() {
            public final void accept(Object obj) {
                StatusBar.this.lambda$makeStatusBarView$4$StatusBar((Integer) obj);
            }
        });
        this.mScrimController.attachViews((ScrimView) this.mNotificationShadeWindowView.findViewById(C0012R$id.scrim_behind), (ScrimView) this.mNotificationShadeWindowView.findViewById(C0012R$id.scrim_in_front), scrimForBubble);
        this.mNotificationPanelViewController.initDependencies(this, this.mGroupManager, this.mNotificationShelf, this.mNotificationIconAreaController, this.mScrimController);
        BackDropView backDropView = (BackDropView) this.mNotificationShadeWindowView.findViewById(C0012R$id.backdrop);
        this.mMediaManager.setup(backDropView, (ImageView) backDropView.findViewById(C0012R$id.backdrop_front), (ImageView) backDropView.findViewById(C0012R$id.backdrop_back), this.mScrimController, this.mLockscreenWallpaper);
        this.mNotificationShadeDepthControllerLazy.get().addListener(new NotificationShadeDepthController.DepthListener(this.mContext.getResources().getFloat(17105099), backDropView) {
            public final /* synthetic */ float f$0;
            public final /* synthetic */ BackDropView f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void onWallpaperZoomOutChanged(float f) {
                StatusBar.lambda$makeStatusBarView$5(this.f$0, this.f$1, f);
            }
        });
        this.mNotificationPanelViewController.setUserSetupComplete(this.mUserSetup);
        if (UserManager.get(this.mContext).isUserSwitcherEnabled()) {
            createUserSwitcher();
        }
        NotificationPanelViewController notificationPanelViewController2 = this.mNotificationPanelViewController;
        LockscreenLockIconController lockscreenLockIconController = this.mLockscreenLockIconController;
        Objects.requireNonNull(lockscreenLockIconController);
        notificationPanelViewController2.setLaunchAffordanceListener(new Consumer() {
            public final void accept(Object obj) {
                LockscreenLockIconController.this.onShowingLaunchAffordanceChanged((Boolean) obj);
            }
        });
        View findViewById = this.mNotificationShadeWindowView.findViewById(C0012R$id.qs_frame);
        if (findViewById != null) {
            FragmentHostManager fragmentHostManager2 = FragmentHostManager.get(findViewById);
            int i = C0012R$id.qs_frame;
            ExtensionController.ExtensionBuilder<QS> newExtension = this.mExtensionController.newExtension(cls);
            newExtension.withPlugin(cls);
            newExtension.withDefault(new Supplier() {
                public final Object get() {
                    return StatusBar.this.createDefaultQSFragment();
                }
            });
            ExtensionFragmentListener.attachExtensonToFragment(findViewById, QS.TAG, i, newExtension.build());
            this.mBrightnessMirrorController = new BrightnessMirrorController(this.mNotificationShadeWindowView, this.mNotificationPanelViewController, this.mNotificationShadeDepthControllerLazy.get(), new Consumer() {
                public final void accept(Object obj) {
                    StatusBar.this.lambda$makeStatusBarView$6$StatusBar((Boolean) obj);
                }
            });
            fragmentHostManager2.addTagListener(QS.TAG, new FragmentHostManager.FragmentListener() {
                public final void onFragmentViewCreated(String str, Fragment fragment) {
                    StatusBar.this.lambda$makeStatusBarView$7$StatusBar(str, fragment);
                }
            });
        }
        View findViewById2 = this.mNotificationShadeWindowView.findViewById(C0012R$id.report_rejected_touch);
        this.mReportRejectedTouch = findViewById2;
        if (findViewById2 != null) {
            updateReportRejectedTouchVisibility();
            this.mReportRejectedTouch.setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    StatusBar.this.lambda$makeStatusBarView$8$StatusBar(view);
                }
            });
        }
        if (!this.mPowerManager.isScreenOn()) {
            this.mBroadcastReceiver.onReceive(this.mContext, new Intent("android.intent.action.SCREEN_OFF"));
        }
        this.mGestureWakeLock = this.mPowerManager.newWakeLock(10, "GestureWakeLock");
        this.mVibrator = (Vibrator) this.mContext.getSystemService(Vibrator.class);
        int[] intArray = this.mContext.getResources().getIntArray(C0005R$array.config_cameraLaunchGestureVibePattern);
        this.mCameraLaunchGestureVibePattern = new long[intArray.length];
        for (int i2 = 0; i2 < intArray.length; i2++) {
            this.mCameraLaunchGestureVibePattern[i2] = (long) intArray[i2];
        }
        registerBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.systemui.demo");
        context.registerReceiverAsUser(this.mDemoReceiver, UserHandle.ALL, intentFilter, "android.permission.DUMP", (Handler) null);
        this.mDeviceProvisionedController.addCallback(this.mUserSetupObserver);
        this.mUserSetupObserver.onUserSetupChanged();
        ThreadedRenderer.overrideProperty("disableProfileBars", "true");
        ThreadedRenderer.overrideProperty("ambientRatio", String.valueOf(1.5f));
        KeyguardBottomAreaView keyguardBottomAreaView = (KeyguardBottomAreaView) this.mNotificationShadeWindowView.findViewById(C0012R$id.keyguard_bottom_area);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$makeStatusBarView$3 */
    public /* synthetic */ void lambda$makeStatusBarView$3$StatusBar(String str, Fragment fragment) {
        CollapsedStatusBarFragment collapsedStatusBarFragment = (CollapsedStatusBarFragment) fragment;
        PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
        PhoneStatusBarView phoneStatusBarView2 = (PhoneStatusBarView) collapsedStatusBarFragment.getView();
        this.mStatusBarView = phoneStatusBarView2;
        phoneStatusBarView2.setBar(this);
        this.mStatusBarView.setPanel(this.mNotificationPanelViewController);
        this.mStatusBarView.setScrimController(this.mScrimController);
        collapsedStatusBarFragment.initNotificationIconArea(this.mNotificationIconAreaController);
        if (this.mHeadsUpManager.hasPinnedHeadsUp()) {
            this.mNotificationPanelViewController.notifyBarPanelExpansionChanged();
        }
        this.mStatusBarView.setBouncerShowing(this.mBouncerShowing);
        if (phoneStatusBarView != null) {
            this.mStatusBarView.panelExpansionChanged(phoneStatusBarView.getExpansionFraction(), phoneStatusBarView.isExpanded());
        }
        HeadsUpAppearanceController headsUpAppearanceController = this.mHeadsUpAppearanceController;
        if (headsUpAppearanceController != null) {
            headsUpAppearanceController.destroy();
        }
        HeadsUpAppearanceController headsUpAppearanceController2 = new HeadsUpAppearanceController(this.mNotificationIconAreaController, this.mHeadsUpManager, this.mNotificationShadeWindowView, this.mStatusBarStateController, this.mKeyguardBypassController, this.mKeyguardStateController, this.mWakeUpCoordinator, this.mCommandQueue, this.mNotificationPanelViewController, this.mStatusBarView);
        this.mHeadsUpAppearanceController = headsUpAppearanceController2;
        headsUpAppearanceController2.readFrom(headsUpAppearanceController);
        this.mLightsOutNotifController.setLightsOutNotifView(this.mStatusBarView.findViewById(C0012R$id.notification_lights_out));
        this.mNotificationShadeWindowViewController.setStatusBarView(this.mStatusBarView);
        checkBarModes();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$makeStatusBarView$4 */
    public /* synthetic */ void lambda$makeStatusBarView$4$StatusBar(Integer num) {
        this.mNotificationShadeWindowController.setScrimsVisibility(num.intValue());
        if (this.mNotificationShadeWindowView != null) {
            this.mLockscreenLockIconController.onScrimVisibilityChanged(num);
        }
    }

    static /* synthetic */ void lambda$makeStatusBarView$5(float f, BackDropView backDropView, float f2) {
        float lerp = MathUtils.lerp(f, 1.0f, f2);
        backDropView.setPivotX(((float) backDropView.getWidth()) / 2.0f);
        backDropView.setPivotY(((float) backDropView.getHeight()) / 2.0f);
        backDropView.setScaleX(lerp);
        backDropView.setScaleY(lerp);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$makeStatusBarView$6 */
    public /* synthetic */ void lambda$makeStatusBarView$6$StatusBar(Boolean bool) {
        this.mBrightnessMirrorVisible = bool.booleanValue();
        updateScrimController();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$makeStatusBarView$7 */
    public /* synthetic */ void lambda$makeStatusBarView$7$StatusBar(String str, Fragment fragment) {
        QS qs = (QS) fragment;
        if (qs instanceof QSFragment) {
            QSFragment qSFragment = (QSFragment) qs;
            this.mQSPanel = qSFragment.getQsPanel();
            QSContainerImpl qSContainer = qSFragment.getQSContainer();
            this.mQSContainer = qSContainer;
            qSContainer.setBrightnessMirror(this.mBrightnessMirrorController);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$makeStatusBarView$8 */
    public /* synthetic */ void lambda$makeStatusBarView$8$StatusBar(View view) {
        Uri reportRejectedTouch = this.mFalsingManager.reportRejectedTouch();
        if (reportRejectedTouch != null) {
            StringWriter stringWriter = new StringWriter();
            stringWriter.write("Build info: ");
            stringWriter.write(SystemProperties.get("ro.build.description"));
            stringWriter.write("\nSerial number: ");
            stringWriter.write(SystemProperties.get("ro.serialno"));
            stringWriter.write("\n");
            PrintWriter printWriter = new PrintWriter(stringWriter);
            FalsingLog.dump(printWriter);
            printWriter.flush();
            startActivityDismissingKeyguard(Intent.createChooser(new Intent("android.intent.action.SEND").setType("*/*").putExtra("android.intent.extra.SUBJECT", "Rejected touch report").putExtra("android.intent.extra.STREAM", reportRejectedTouch).putExtra("android.intent.extra.TEXT", stringWriter.toString()), "Share rejected touch report").addFlags(268435456), true, true);
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.app.action.SHOW_DEVICE_MONITORING_DIALOG");
        this.mBroadcastDispatcher.registerReceiver(this.mBroadcastReceiver, intentFilter, (Executor) null, UserHandle.ALL);
    }

    /* access modifiers changed from: protected */
    public QS createDefaultQSFragment() {
        return (QS) FragmentHostManager.get(this.mNotificationShadeWindowView).create(QSFragment.class);
    }

    private void setUpPresenter() {
        MiuiActivityLaunchAnimator miuiActivityLaunchAnimator = r0;
        MiuiActivityLaunchAnimator miuiActivityLaunchAnimator2 = new MiuiActivityLaunchAnimator(this.mNotificationShadeWindowViewController, this, this.mNotificationPanelViewController, this.mNotificationShadeDepthControllerLazy.get(), (NotificationListContainer) this.mStackScroller, this.mContext.getMainExecutor());
        this.mActivityLaunchAnimator = miuiActivityLaunchAnimator2;
        StatusBarNotificationPresenter statusBarNotificationPresenter = new StatusBarNotificationPresenter(this.mContext, this.mNotificationPanelViewController, this.mHeadsUpManager, this.mNotificationShadeWindowView, this.mStackScroller, this.mDozeScrimController, this.mScrimController, miuiActivityLaunchAnimator, this.mDynamicPrivacyController, this.mKeyguardStateController, this.mKeyguardIndicationController, this, this.mShadeController, this.mCommandQueue, this.mInitController, this.mNotificationInterruptStateProvider);
        this.mPresenter = statusBarNotificationPresenter;
        this.mNotificationShelf.setOnActivatedListener(statusBarNotificationPresenter);
        this.mRemoteInputManager.getController().addCallback(this.mNotificationShadeWindowController);
        StatusBarNotificationActivityStarter.Builder builder = this.mStatusBarNotificationActivityStarterBuilder;
        builder.setStatusBar(this);
        builder.setActivityLaunchAnimator(this.mActivityLaunchAnimator);
        builder.setNotificationPresenter(this.mPresenter);
        builder.setNotificationPanelViewController(this.mNotificationPanelViewController);
        StatusBarNotificationActivityStarter build = builder.build();
        this.mNotificationActivityStarter = build;
        ((NotificationListContainer) this.mStackScroller).setNotificationActivityStarter(build);
        this.mGutsManager.setNotificationActivityStarter(this.mNotificationActivityStarter);
        NotificationsController notificationsController = this.mNotificationsController;
        StatusBarNotificationPresenter statusBarNotificationPresenter2 = this.mPresenter;
        NotificationActivityStarter notificationActivityStarter = this.mNotificationActivityStarter;
        notificationsController.initialize(this, statusBarNotificationPresenter2, (NotificationListContainer) this.mStackScroller, notificationActivityStarter, statusBarNotificationPresenter2);
    }

    /* access modifiers changed from: protected */
    /* renamed from: setUpDisableFlags */
    public void lambda$start$2(int i, int i2) {
        this.mCommandQueue.disable(this.mDisplayId, i, i2, false);
    }

    public void wakeUpIfDozing(long j, View view, String str) {
        if (this.mDozing) {
            PowerManager powerManager = this.mPowerManager;
            powerManager.wakeUp(j, 4, "com.android.systemui:" + str);
            this.mWakeUpComingFromTouch = true;
            view.getLocationInWindow(this.mTmpInt2);
            this.mWakeUpTouchLocation = new PointF((float) (this.mTmpInt2[0] + (view.getWidth() / 2)), (float) (this.mTmpInt2[1] + (view.getHeight() / 2)));
            this.mFalsingManager.onScreenOnFromTouch();
        }
    }

    /* access modifiers changed from: protected */
    public void createNavigationBar(RegisterStatusBarResult registerStatusBarResult) {
        this.mNavigationBarController.createNavigationBars(true, registerStatusBarResult);
    }

    /* access modifiers changed from: protected */
    public View.OnTouchListener getStatusBarWindowTouchListener() {
        return new View.OnTouchListener() {
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return StatusBar.this.lambda$getStatusBarWindowTouchListener$9$StatusBar(view, motionEvent);
            }
        };
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getStatusBarWindowTouchListener$9 */
    public /* synthetic */ boolean lambda$getStatusBarWindowTouchListener$9$StatusBar(View view, MotionEvent motionEvent) {
        this.mAutoHideController.checkUserAutoHide(motionEvent);
        this.mRemoteInputManager.checkRemoteInputOutside(motionEvent);
        if (motionEvent.getAction() == 0 && this.mExpandedVisible) {
            this.mShadeController.animateCollapsePanels();
        }
        return this.mNotificationShadeWindowView.onTouchEvent(motionEvent);
    }

    private void inflateShelf() {
        NotificationShelf notificationShelf = this.mSuperStatusBarViewFactory.getNotificationShelf(this.mStackScroller);
        this.mNotificationShelf = notificationShelf;
        notificationShelf.setOnClickListener(this.mGoToLockedShadeListener);
    }

    public void onDensityOrFontScaleChanged() {
        BrightnessMirrorController brightnessMirrorController = this.mBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.onDensityOrFontScaleChanged();
        }
        this.mUserInfoControllerImpl.onDensityOrFontScaleChanged();
        this.mUserSwitcherController.onDensityOrFontScaleChanged();
        KeyguardUserSwitcher keyguardUserSwitcher = this.mKeyguardUserSwitcher;
        if (keyguardUserSwitcher != null) {
            keyguardUserSwitcher.onDensityOrFontScaleChanged();
        }
        this.mNotificationIconAreaController.onDensityOrFontScaleChanged(this.mContext);
        this.mHeadsUpManager.onDensityOrFontScaleChanged();
    }

    public void onThemeChanged() {
        StatusBarKeyguardViewManager statusBarKeyguardViewManager = this.mStatusBarKeyguardViewManager;
        if (statusBarKeyguardViewManager != null) {
            statusBarKeyguardViewManager.onThemeChanged();
        }
        View view = this.mAmbientIndicationContainer;
        if (view instanceof AutoReinflateContainer) {
            ((AutoReinflateContainer) view).inflateLayout();
        }
        this.mNotificationIconAreaController.onThemeChanged();
    }

    public void onOverlayChanged() {
        BrightnessMirrorController brightnessMirrorController = this.mBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.onOverlayChanged();
        }
        this.mNotificationPanelViewController.onThemeChanged();
        onThemeChanged();
    }

    public void onUiModeChanged() {
        BrightnessMirrorController brightnessMirrorController = this.mBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.onUiModeChanged();
        }
    }

    /* access modifiers changed from: protected */
    public void createUserSwitcher() {
        this.mKeyguardUserSwitcher = new KeyguardUserSwitcher(this.mContext, (ViewStub) this.mNotificationShadeWindowView.findViewById(C0012R$id.keyguard_user_switcher), (KeyguardStatusBarView) this.mNotificationShadeWindowView.findViewById(C0012R$id.keyguard_header), this.mNotificationPanelViewController);
    }

    private void inflateStatusBarWindow() {
        this.mNotificationShadeWindowView = this.mSuperStatusBarViewFactory.getNotificationShadeWindowView();
        StatusBarComponent build = this.mStatusBarComponentBuilder.get().statusBarWindowView(this.mNotificationShadeWindowView).build();
        this.mNotificationShadeWindowViewController = build.getNotificationShadeWindowViewController();
        this.mNotificationShadeWindowController.setNotificationShadeView(this.mNotificationShadeWindowView);
        this.mNotificationShadeWindowViewController.setupExpandedStatusBar();
        this.mStatusBarWindowController = build.getStatusBarWindowController();
        this.mPhoneStatusBarWindow = this.mSuperStatusBarViewFactory.getStatusBarWindowView();
        this.mNotificationPanelViewController = build.getNotificationPanelViewController();
    }

    /* access modifiers changed from: protected */
    public void startKeyguard() {
        Trace.beginSection("StatusBar#startKeyguard");
        this.mBiometricUnlockController = this.mBiometricUnlockControllerLazy.get();
        this.mStatusBarKeyguardViewManager.registerStatusBar(this, getBouncerContainer(), this.mNotificationPanelViewController, this.mBiometricUnlockController, this.mDismissCallbackRegistry, (ViewGroup) this.mNotificationShadeWindowView.findViewById(C0012R$id.lock_icon_container), this.mStackScroller, this.mKeyguardBypassController, this.mFalsingManager);
        this.mKeyguardIndicationController.setStatusBarKeyguardViewManager(this.mStatusBarKeyguardViewManager);
        this.mBiometricUnlockController.setKeyguardViewController(this.mStatusBarKeyguardViewManager);
        this.mRemoteInputManager.getController().addCallback(this.mStatusBarKeyguardViewManager);
        this.mDynamicPrivacyController.setStatusBarKeyguardViewManager(this.mStatusBarKeyguardViewManager);
        this.mLightBarController.setBiometricUnlockController(this.mBiometricUnlockController);
        this.mMediaManager.setBiometricUnlockController(this.mBiometricUnlockController);
        this.mKeyguardDismissUtil.setDismissHandler(new KeyguardDismissHandler() {
            public final void executeWhenUnlocked(ActivityStarter.OnDismissAction onDismissAction, boolean z) {
                StatusBar.this.executeWhenUnlocked(onDismissAction, z);
            }
        });
        Trace.endSection();
    }

    /* access modifiers changed from: protected */
    public View getStatusBarView() {
        return this.mStatusBarView;
    }

    public NotificationShadeWindowView getNotificationShadeWindowView() {
        return this.mNotificationShadeWindowView;
    }

    public StatusBarWindowView getStatusBarWindow() {
        return this.mPhoneStatusBarWindow;
    }

    public NotificationShadeWindowViewController getNotificationShadeWindowViewController() {
        return this.mNotificationShadeWindowViewController;
    }

    /* access modifiers changed from: protected */
    public ViewGroup getBouncerContainer() {
        return this.mNotificationShadeWindowView;
    }

    /* access modifiers changed from: protected */
    public boolean toggleSplitScreenMode(int i, int i2) {
        int i3 = 0;
        if (!this.mRecentsOptional.isPresent()) {
            return false;
        }
        Divider divider = this.mDividerOptional.isPresent() ? this.mDividerOptional.get() : null;
        if (divider == null || !divider.isDividerVisible()) {
            int navBarPosition = WindowManagerWrapper.getInstance().getNavBarPosition(this.mDisplayId);
            if (navBarPosition == -1) {
                return false;
            }
            if (navBarPosition == 1) {
                i3 = 1;
            }
            return this.mRecentsOptional.get().splitPrimaryTask(i3, (Rect) null, i);
        } else if (divider.isMinimized() && !divider.isHomeStackResizable()) {
            return false;
        } else {
            divider.onUndockingTask();
            if (i2 != -1) {
                this.mMetricsLogger.action(i2);
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002e, code lost:
        if (r0 == false) goto L_0x0032;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateQsExpansionEnabled() {
        /*
            r4 = this;
            com.android.systemui.controlcenter.phone.ControlPanelController r0 = r4.mControlPanelController
            boolean r0 = r0.isUseControlCenter()
            com.android.systemui.statusbar.policy.DeviceProvisionedController r1 = r4.mDeviceProvisionedController
            boolean r1 = r1.isDeviceProvisioned()
            r2 = 1
            if (r1 == 0) goto L_0x0031
            boolean r1 = r4.mUserSetup
            if (r1 != 0) goto L_0x001d
            com.android.systemui.statusbar.policy.UserSwitcherController r1 = r4.mUserSwitcherController
            if (r1 == 0) goto L_0x001d
            boolean r1 = r1.isSimpleUserSwitcher()
            if (r1 != 0) goto L_0x0031
        L_0x001d:
            int r1 = r4.mDisabled2
            r3 = r1 & 4
            if (r3 != 0) goto L_0x0031
            r1 = r1 & r2
            if (r1 != 0) goto L_0x0031
            boolean r1 = r4.mDozing
            if (r1 != 0) goto L_0x0031
            boolean r1 = ONLY_CORE_APPS
            if (r1 != 0) goto L_0x0031
            if (r0 != 0) goto L_0x0031
            goto L_0x0032
        L_0x0031:
            r2 = 0
        L_0x0032:
            com.android.systemui.statusbar.phone.NotificationPanelViewController r4 = r4.mNotificationPanelViewController
            r4.setQsExpansionEnabled(r2)
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r0 = "updateQsExpansionEnabled - QS Expand enabled: "
            r4.append(r0)
            r4.append(r2)
            java.lang.String r4 = r4.toString()
            java.lang.String r0 = "StatusBar"
            android.util.Log.d(r0, r4)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.StatusBar.updateQsExpansionEnabled():void");
    }

    public void addQsTile(ComponentName componentName) {
        QSPanel qSPanel = this.mQSPanel;
        if (qSPanel != null && qSPanel.getHost() != null) {
            this.mQSPanel.getHost().addTile(componentName);
        }
    }

    public void remQsTile(ComponentName componentName) {
        QSPanel qSPanel = this.mQSPanel;
        if (qSPanel != null && qSPanel.getHost() != null) {
            this.mQSPanel.getHost().removeTile(componentName);
        }
    }

    public void clickTile(ComponentName componentName) {
        this.mQSPanel.clickTile(componentName);
    }

    public void requestNotificationUpdate(String str) {
        this.mNotificationsController.requestNotificationUpdate(str);
    }

    public void requestFaceAuth() {
        if (!this.mKeyguardStateController.canDismissLockScreen()) {
            this.mKeyguardUpdateMonitor.requestFaceAuth();
        }
    }

    private void updateReportRejectedTouchVisibility() {
        View view = this.mReportRejectedTouch;
        if (view != null) {
            view.setVisibility((this.mState != 1 || this.mDozing || !this.mFalsingManager.isReportingEnabled()) ? 4 : 0);
        }
    }

    public void disable(int i, int i2, int i3, boolean z) {
        int i4 = i2;
        if (i == this.mDisplayId) {
            int adjustDisableFlags = this.mRemoteInputQuickSettingsDisabler.adjustDisableFlags(i3);
            int i5 = this.mStatusBarWindowState;
            int i6 = this.mDisabled1 ^ i4;
            this.mDisabled1 = i4;
            int i7 = this.mDisabled2 ^ adjustDisableFlags;
            this.mDisabled2 = adjustDisableFlags;
            StringBuilder sb = new StringBuilder();
            sb.append("disable<");
            int i8 = i4 & 65536;
            sb.append(i8 != 0 ? 'E' : 'e');
            int i9 = 65536 & i6;
            sb.append(i9 != 0 ? '!' : ' ');
            char c = 'I';
            sb.append((i4 & 131072) != 0 ? 'I' : 'i');
            sb.append((131072 & i6) != 0 ? '!' : ' ');
            sb.append((i4 & 262144) != 0 ? 'A' : 'a');
            int i10 = 262144 & i6;
            sb.append(i10 != 0 ? '!' : ' ');
            char c2 = 'S';
            sb.append((i4 & 1048576) != 0 ? 'S' : 's');
            sb.append((1048576 & i6) != 0 ? '!' : ' ');
            sb.append((i4 & 4194304) != 0 ? 'B' : 'b');
            sb.append((4194304 & i6) != 0 ? '!' : ' ');
            sb.append((i4 & 2097152) != 0 ? 'H' : 'h');
            sb.append((2097152 & i6) != 0 ? '!' : ' ');
            int i11 = i4 & 16777216;
            sb.append(i11 != 0 ? 'R' : 'r');
            int i12 = i6 & 16777216;
            sb.append(i12 != 0 ? '!' : ' ');
            sb.append((i4 & 8388608) != 0 ? 'C' : 'c');
            sb.append((i6 & 8388608) != 0 ? '!' : ' ');
            if ((i4 & 33554432) == 0) {
                c2 = 's';
            }
            sb.append(c2);
            sb.append((i6 & 33554432) != 0 ? '!' : ' ');
            sb.append("> disable2<");
            sb.append((adjustDisableFlags & 1) != 0 ? 'Q' : 'q');
            int i13 = i7 & 1;
            sb.append(i13 != 0 ? '!' : ' ');
            if ((adjustDisableFlags & 2) == 0) {
                c = 'i';
            }
            sb.append(c);
            sb.append((i7 & 2) != 0 ? '!' : ' ');
            sb.append((adjustDisableFlags & 4) != 0 ? 'N' : 'n');
            int i14 = i7 & 4;
            sb.append(i14 != 0 ? '!' : ' ');
            sb.append('>');
            Log.d("StatusBar", sb.toString());
            if (!(i9 == 0 || i8 == 0)) {
                this.mShadeController.animateCollapsePanels();
            }
            if (!(i12 == 0 || i11 == 0)) {
                this.mHandler.removeMessages(1020);
                this.mHandler.sendEmptyMessage(1020);
            }
            if (i10 != 0 && areNotificationAlertsDisabled()) {
                this.mHeadsUpManager.releaseAllImmediately();
            }
            if (i13 != 0) {
                updateQsExpansionEnabled();
            }
            if (i14 != 0) {
                updateQsExpansionEnabled();
                if ((i4 & 4) != 0) {
                    this.mShadeController.animateCollapsePanels();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean areNotificationAlertsDisabled() {
        return (this.mDisabled1 & 262144) != 0;
    }

    /* access modifiers changed from: protected */
    public H createHandler() {
        return new H();
    }

    public void startActivity(Intent intent, boolean z, boolean z2, int i) {
        startActivityDismissingKeyguard(intent, z, z2, i);
    }

    public void startActivity(Intent intent, boolean z) {
        startActivityDismissingKeyguard(intent, false, z);
    }

    public void startActivity(Intent intent, boolean z, boolean z2) {
        startActivityDismissingKeyguard(intent, z, z2);
    }

    public void startActivity(Intent intent, boolean z, ActivityStarter.Callback callback) {
        startActivityDismissingKeyguard(intent, false, z, false, callback, 0);
    }

    public void setQsExpanded(boolean z) {
        this.mNotificationShadeWindowController.setQsExpanded(z);
        this.mNotificationPanelViewController.setStatusAccessibilityImportance(z ? 4 : 0);
        if (getNavigationBarView() != null) {
            getNavigationBarView().onStatusBarPanelStateChanged();
        }
    }

    public boolean isWakeUpComingFromTouch() {
        return this.mWakeUpComingFromTouch;
    }

    public boolean isFalsingThresholdNeeded() {
        return this.mStatusBarStateController.getState() == 1;
    }

    public void onKeyguardViewManagerStatesUpdated() {
        logStateToEventlog();
    }

    public void onUnlockedChanged() {
        updateKeyguardState();
        logStateToEventlog();
    }

    public void onHeadsUpPinnedModeChanged(boolean z) {
        if (z) {
            this.mNotificationShadeWindowController.setHeadsUpShowing(true);
            if (this.mNotificationPanelViewController.isFullyCollapsed()) {
                this.mNotificationPanelViewController.getView().requestLayout();
                this.mNotificationShadeWindowController.setForceWindowCollapsed(true);
                this.mNotificationPanelViewController.getView().post(new Runnable() {
                    public final void run() {
                        StatusBar.this.lambda$onHeadsUpPinnedModeChanged$10$StatusBar();
                    }
                });
                return;
            }
            return;
        }
        boolean z2 = this.mKeyguardBypassController.getBypassEnabled() && this.mState == 1;
        if (!this.mNotificationPanelViewController.isFullyCollapsed() || this.mNotificationPanelViewController.isTracking() || z2) {
            this.mNotificationShadeWindowController.setHeadsUpShowing(false);
            if (z2) {
                this.mStatusBarWindowController.setForceStatusBarVisible(false);
                return;
            }
            return;
        }
        this.mHeadsUpManager.setHeadsUpGoingAway(true);
        this.mNotificationPanelViewController.runAfterAnimationFinished(new Runnable() {
            public final void run() {
                StatusBar.this.lambda$onHeadsUpPinnedModeChanged$11$StatusBar();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onHeadsUpPinnedModeChanged$10 */
    public /* synthetic */ void lambda$onHeadsUpPinnedModeChanged$10$StatusBar() {
        this.mNotificationShadeWindowController.setForceWindowCollapsed(false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onHeadsUpPinnedModeChanged$11 */
    public /* synthetic */ void lambda$onHeadsUpPinnedModeChanged$11$StatusBar() {
        if (!this.mHeadsUpManager.hasPinnedHeadsUp()) {
            this.mNotificationShadeWindowController.setHeadsUpShowing(false);
            this.mHeadsUpManager.setHeadsUpGoingAway(false);
        }
        this.mRemoteInputManager.onPanelCollapsed();
    }

    public void onHeadsUpStateChanged(NotificationEntry notificationEntry, boolean z) {
        this.mNotificationsController.requestNotificationUpdate("onHeadsUpStateChanged");
        if (this.mStatusBarStateController.isDozing() && z) {
            notificationEntry.setPulseSuppressed(false);
            this.mDozeServiceHost.fireNotificationPulse(notificationEntry);
            if (this.mDozeServiceHost.isPulsing()) {
                this.mDozeScrimController.cancelPendingPulseTimeout();
            }
        }
        if (!z && !this.mHeadsUpManager.hasNotifications()) {
            this.mDozeScrimController.pulseOutNow();
        }
    }

    public void setPanelExpanded(boolean z) {
        if (this.mPanelExpanded != z) {
            this.mNotificationLogger.onPanelExpandedChanged(z);
        }
        this.mPanelExpanded = z;
        updateHideIconsForBouncer(false);
        this.mNotificationShadeWindowController.setPanelExpanded(z);
        this.mVisualStabilityManager.setPanelExpanded(z);
        if (z && this.mStatusBarStateController.getState() != 1) {
            clearNotificationEffects();
        }
        if (!z) {
            this.mRemoteInputManager.onPanelCollapsed();
        }
    }

    public ViewGroup getNotificationScrollLayout() {
        return this.mStackScroller;
    }

    public boolean isPulsing() {
        return this.mDozeServiceHost.isPulsing();
    }

    public boolean hideStatusBarIconsWhenExpanded() {
        return this.mNotificationPanelViewController.hideStatusBarIconsWhenExpanded();
    }

    public void onColorsChanged(ColorExtractor colorExtractor, int i) {
        updateTheme();
    }

    public View getAmbientIndicationContainer() {
        return this.mAmbientIndicationContainer;
    }

    public boolean isOccluded() {
        return this.mIsOccluded;
    }

    public void setOccluded(boolean z) {
        this.mIsOccluded = z;
        this.mScrimController.setKeyguardOccluded(z);
        updateHideIconsForBouncer(false);
    }

    public boolean hideStatusBarIconsForBouncer() {
        return this.mHideIconsForBouncer || this.mWereIconsJustHidden;
    }

    private void updateHideIconsForBouncer(boolean z) {
        boolean z2 = false;
        boolean z3 = this.mTopHidesStatusBar && this.mIsOccluded && (this.mStatusBarWindowHidden || this.mBouncerShowing);
        boolean z4 = !this.mPanelExpanded && !this.mIsOccluded && this.mBouncerShowing;
        if (z3 || z4) {
            z2 = true;
        }
        if (this.mHideIconsForBouncer != z2) {
            this.mHideIconsForBouncer = z2;
            if (z2 || !this.mBouncerWasShowingWhenHidden) {
                this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, z);
            } else {
                this.mWereIconsJustHidden = true;
                this.mHandler.postDelayed(new Runnable() {
                    public final void run() {
                        StatusBar.this.lambda$updateHideIconsForBouncer$12$StatusBar();
                    }
                }, 500);
            }
        }
        if (z2) {
            this.mBouncerWasShowingWhenHidden = this.mBouncerShowing;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateHideIconsForBouncer$12 */
    public /* synthetic */ void lambda$updateHideIconsForBouncer$12$StatusBar() {
        this.mWereIconsJustHidden = false;
        this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, true);
    }

    public boolean headsUpShouldBeVisible() {
        return this.mHeadsUpAppearanceController.shouldBeVisible();
    }

    public void onLaunchAnimationCancelled() {
        if (!this.mPresenter.isCollapsing()) {
            onClosingFinished();
        }
    }

    public void onExpandAnimationFinished(boolean z) {
        if (!this.mPresenter.isCollapsing()) {
            onClosingFinished();
        }
        if (z) {
            instantCollapseNotificationPanel();
        }
    }

    public void onExpandAnimationTimedOut() {
        ActivityLaunchAnimator activityLaunchAnimator;
        if (!this.mPresenter.isPresenterFullyCollapsed() || this.mPresenter.isCollapsing() || (activityLaunchAnimator = this.mActivityLaunchAnimator) == null || activityLaunchAnimator.isLaunchForActivity()) {
            this.mShadeController.collapsePanel(true);
        } else {
            onClosingFinished();
        }
    }

    public boolean areLaunchAnimationsEnabled() {
        return this.mState == 0;
    }

    public boolean isDeviceInVrMode() {
        return this.mPresenter.isDeviceInVrMode();
    }

    public NotificationPresenter getPresenter() {
        return this.mPresenter;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setBarStateForTest(int i) {
        this.mState = i;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setUserSetupForTest(boolean z) {
        this.mUserSetup = z;
    }

    protected class H extends Handler {
        protected H() {
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1026) {
                StatusBar.this.toggleKeyboardShortcuts(message.arg1);
            } else if (i != 1027) {
                switch (i) {
                    case 1000:
                        StatusBar.this.animateExpandNotificationsPanel();
                        return;
                    case 1001:
                        StatusBar.this.mShadeController.animateCollapsePanels();
                        return;
                    case 1002:
                        StatusBar.this.animateExpandSettingsPanel((String) message.obj);
                        return;
                    case 1003:
                        StatusBar.this.onLaunchTransitionTimeout();
                        return;
                    default:
                        return;
                }
            } else {
                StatusBar.this.dismissKeyboardShortcuts();
            }
        }
    }

    public void maybeEscalateHeadsUp() {
        this.mHeadsUpManager.getAllEntries().forEach($$Lambda$StatusBar$b4jfaxlYnE9QVAGrlIapAKjtXiQ.INSTANCE);
        this.mHeadsUpManager.releaseAllImmediately();
    }

    static /* synthetic */ void lambda$maybeEscalateHeadsUp$13(NotificationEntry notificationEntry) {
        ExpandedNotification sbn = notificationEntry.getSbn();
        Notification notification = sbn.getNotification();
        if (notification.fullScreenIntent != null) {
            try {
                EventLog.writeEvent(36003, sbn.getKey());
                notification.fullScreenIntent.send();
                notificationEntry.notifyFullScreenIntentLaunched();
            } catch (PendingIntent.CanceledException unused) {
            }
        }
    }

    public void handleSystemKey(int i) {
        if (this.mCommandQueue.panelsEnabled() && this.mKeyguardUpdateMonitor.isDeviceInteractive()) {
            if ((this.mKeyguardStateController.isShowing() && !this.mKeyguardStateController.isOccluded()) || !this.mUserSetup) {
                return;
            }
            if (280 == i) {
                this.mMetricsLogger.action(493);
                this.mNotificationPanelViewController.collapse(false, 1.0f);
            } else if (281 == i) {
                this.mMetricsLogger.action(494);
                if (this.mNotificationPanelViewController.isFullyCollapsed()) {
                    if (this.mVibrateOnOpening) {
                        this.mVibratorHelper.vibrate(2);
                    }
                    this.mNotificationPanelViewController.expand(true);
                    ((NotificationListContainer) this.mStackScroller).setWillExpand(true);
                    this.mHeadsUpManager.unpinAll(true);
                    this.mMetricsLogger.count("panel_open", 1);
                } else if (!this.mNotificationPanelViewController.isInSettings() && !this.mNotificationPanelViewController.isExpanding()) {
                    this.mNotificationPanelViewController.flingSettings(0.0f, 0);
                    this.mMetricsLogger.count("panel_open_qs", 1);
                }
            }
        }
    }

    public void showPinningEnterExitToast(boolean z) {
        if (getNavigationBarView() != null) {
            getNavigationBarView().showPinningEnterExitToast(z);
        }
    }

    public void showPinningEscapeToast() {
        if (getNavigationBarView() != null) {
            getNavigationBarView().showPinningEscapeToast();
        }
    }

    /* access modifiers changed from: package-private */
    public void makeExpandedVisible(boolean z) {
        if (z || (!this.mExpandedVisible && this.mCommandQueue.panelsEnabled())) {
            this.mExpandedVisible = true;
            this.mNotificationShadeWindowController.setPanelVisible(true);
            visibilityChanged(true);
            this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, !z);
            setInteracting(1, true);
        }
    }

    public void postAnimateCollapsePanels() {
        H h = this.mHandler;
        ShadeController shadeController = this.mShadeController;
        Objects.requireNonNull(shadeController);
        h.post(new Runnable() {
            public final void run() {
                ShadeController.this.animateCollapsePanels();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$postAnimateForceCollapsePanels$14 */
    public /* synthetic */ void lambda$postAnimateForceCollapsePanels$14$StatusBar() {
        this.mShadeController.animateCollapsePanels(0, true);
    }

    public void postAnimateForceCollapsePanels() {
        this.mHandler.post(new Runnable() {
            public final void run() {
                StatusBar.this.lambda$postAnimateForceCollapsePanels$14$StatusBar();
            }
        });
    }

    public void togglePanel() {
        if (this.mPanelExpanded) {
            this.mShadeController.animateCollapsePanels();
        } else {
            animateExpandNotificationsPanel();
        }
    }

    public void animateCollapsePanels(int i, boolean z) {
        this.mShadeController.animateCollapsePanels(i, z, false, 1.0f);
    }

    /* access modifiers changed from: package-private */
    public void postHideRecentApps() {
        if (!this.mHandler.hasMessages(1020)) {
            this.mHandler.removeMessages(1020);
            this.mHandler.sendEmptyMessage(1020);
        }
    }

    public boolean isExpandedVisible() {
        return this.mExpandedVisible;
    }

    public void onInputFocusTransfer(boolean z, boolean z2, float f) {
        if (this.mCommandQueue.panelsEnabled()) {
            if (z) {
                this.mNotificationPanelViewController.startWaitingForOpenPanelGesture();
            } else {
                this.mNotificationPanelViewController.stopWaitingForOpenPanelGesture(z2, f);
            }
        }
    }

    public void animateExpandNotificationsPanel() {
        if (this.mCommandQueue.panelsEnabled()) {
            this.mNotificationPanelViewController.expandWithoutQs();
        }
    }

    public void animateExpandSettingsPanel(String str) {
        if (this.mCommandQueue.panelsEnabled() && this.mUserSetup) {
            if (str != null) {
                this.mQSPanel.openDetails(str);
            }
            this.mNotificationPanelViewController.expandWithQs();
        }
    }

    public void animateCollapseQuickSettings() {
        if (this.mState == 0) {
            this.mStatusBarView.collapsePanel(true, false, 1.0f);
        }
    }

    /* access modifiers changed from: package-private */
    public void makeExpandedInvisible() {
        if (this.mExpandedVisible && this.mNotificationShadeWindowView != null) {
            this.mStatusBarView.collapsePanel(false, false, 1.0f);
            this.mNotificationPanelViewController.closeQs();
            this.mExpandedVisible = false;
            visibilityChanged(false);
            this.mNotificationShadeWindowController.setPanelVisible(false);
            this.mStatusBarWindowController.setForceStatusBarVisible(false);
            this.mGutsManager.closeAndSaveGuts(true, true, true, -1, -1, true);
            this.mShadeController.runPostCollapseRunnables();
            setInteracting(1, false);
            if (!this.mNotificationActivityStarter.isCollapsingToShowActivityOverLockscreen()) {
                showBouncerIfKeyguard();
            }
            this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, this.mNotificationPanelViewController.hideStatusBarIconsWhenExpanded());
            if (!this.mStatusBarKeyguardViewManager.isShowing()) {
                WindowManagerGlobal.getInstance().trimMemory(20);
            }
        }
    }

    public boolean interceptTouchEvent(MotionEvent motionEvent) {
        if (this.mStatusBarWindowState == 0) {
            if (!(motionEvent.getAction() == 1 || motionEvent.getAction() == 3) || this.mExpandedVisible) {
                setInteracting(1, true);
            } else {
                setInteracting(1, false);
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isSameStatusBarState(int i) {
        return this.mStatusBarWindowState == i;
    }

    public GestureRecorder getGestureRecorder() {
        return this.mGestureRec;
    }

    public void setWindowState(int i, int i2, int i3) {
        if (i == this.mDisplayId) {
            boolean z = true;
            boolean z2 = i3 == 0;
            if (!(this.mNotificationShadeWindowView == null || i2 != 1 || this.mStatusBarWindowState == i3)) {
                this.mStatusBarWindowState = i3;
                if (!z2 && this.mState == 0) {
                    this.mStatusBarView.collapsePanel(false, false, 1.0f);
                }
                if (this.mStatusBarView != null) {
                    if (i3 != 2) {
                        z = false;
                    }
                    this.mStatusBarWindowHidden = z;
                    updateHideIconsForBouncer(false);
                }
            }
            updateBubblesVisibility();
        }
    }

    public void onSystemBarAppearanceChanged(int i, int i2, AppearanceRegion[] appearanceRegionArr, boolean z) {
        if (i == this.mDisplayId) {
            boolean z2 = false;
            if (this.mAppearance != i2) {
                this.mAppearance = i2;
                z2 = updateBarMode(barMode(this.mTransientShown, i2));
            }
            this.mLightBarController.onStatusBarAppearanceChanged(appearanceRegionArr, z2, this.mStatusBarMode, z);
            updateBubblesVisibility();
        }
    }

    public void showTransient(int i, int[] iArr) {
        if (i == this.mDisplayId && InsetsState.containsType(iArr, 0)) {
            showTransientUnchecked();
        }
    }

    private void showTransientUnchecked() {
        if (!this.mTransientShown) {
            this.mTransientShown = true;
            this.mNoAnimationOnNextBarModeChange = true;
            handleTransientChanged();
        }
    }

    public void abortTransient(int i, int[] iArr) {
        if (i == this.mDisplayId && InsetsState.containsType(iArr, 0)) {
            clearTransient();
        }
    }

    /* access modifiers changed from: private */
    public void clearTransient() {
        if (this.mTransientShown) {
            this.mTransientShown = false;
            handleTransientChanged();
        }
    }

    private void handleTransientChanged() {
        int barMode = barMode(this.mTransientShown, this.mAppearance);
        if (updateBarMode(barMode)) {
            this.mLightBarController.onStatusBarModeChanged(barMode);
            updateBubblesVisibility();
        }
    }

    private boolean updateBarMode(int i) {
        if (this.mStatusBarMode == i) {
            return false;
        }
        this.mStatusBarMode = i;
        checkBarModes();
        this.mAutoHideController.touchAutoHide();
        return true;
    }

    public void topAppWindowChanged(int i, boolean z, boolean z2) {
        if (i == this.mDisplayId) {
            this.mAppFullscreen = z;
            this.mAppImmersive = z2;
            this.mStatusBarStateController.setFullscreenState(z, z2);
        }
    }

    public void showWirelessChargingAnimation(int i) {
        if (this.mDozing || this.mKeyguardManager.isKeyguardLocked()) {
            WirelessChargingAnimation.makeWirelessChargingAnimation(this.mContext, (Looper) null, i, new WirelessChargingAnimation.Callback() {
                public void onAnimationStarting() {
                    CrossFadeHelper.fadeOut((View) StatusBar.this.mNotificationPanelViewController.getView(), 1.0f);
                }

                public void onAnimationEnded() {
                    CrossFadeHelper.fadeIn(StatusBar.this.mNotificationPanelViewController.getView());
                }
            }, this.mDozing).show();
        } else {
            WirelessChargingAnimation.makeWirelessChargingAnimation(this.mContext, (Looper) null, i, (WirelessChargingAnimation.Callback) null, false).show();
        }
    }

    public void onRecentsAnimationStateChanged(boolean z) {
        setInteracting(2, z);
    }

    /* access modifiers changed from: protected */
    public BarTransitions getStatusBarTransitions() {
        return this.mNotificationShadeWindowViewController.getBarTransitions();
    }

    /* access modifiers changed from: package-private */
    public void checkBarModes() {
        if (!this.mDemoMode) {
            if (!(this.mNotificationShadeWindowViewController == null || getStatusBarTransitions() == null)) {
                checkBarMode(this.mStatusBarMode, this.mStatusBarWindowState, getStatusBarTransitions());
            }
            this.mNavigationBarController.checkNavBarModes(this.mDisplayId);
            this.mNoAnimationOnNextBarModeChange = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void setQsScrimEnabled(boolean z) {
        this.mNotificationPanelViewController.setQsScrimEnabled(z);
    }

    private void updateBubblesVisibility() {
        BubbleController bubbleController = this.mBubbleController;
        int i = this.mStatusBarMode;
        bubbleController.onStatusBarVisibilityChanged((i == 3 || i == 6 || this.mStatusBarWindowHidden) ? false : true);
    }

    /* access modifiers changed from: package-private */
    public void checkBarMode(int i, int i2, BarTransitions barTransitions) {
        barTransitions.transitionTo(i, !this.mNoAnimationOnNextBarModeChange && this.mDeviceInteractive && i2 != 2);
    }

    /* access modifiers changed from: private */
    public void finishBarAnimations() {
        if (!(this.mNotificationShadeWindowController == null || this.mNotificationShadeWindowViewController.getBarTransitions() == null)) {
            this.mNotificationShadeWindowViewController.getBarTransitions().finishAnimations();
        }
        this.mNavigationBarController.finishBarAnimations(this.mDisplayId);
    }

    public void setInteracting(int i, boolean z) {
        int i2;
        boolean z2 = true;
        if (((this.mInteractingWindows & i) != 0) == z) {
            z2 = false;
        }
        if (z) {
            i2 = this.mInteractingWindows | i;
        } else {
            i2 = this.mInteractingWindows & (~i);
        }
        this.mInteractingWindows = i2;
        if (i2 != 0) {
            this.mAutoHideController.suspendAutoHide();
        } else {
            this.mAutoHideController.resumeSuspendedAutoHide();
        }
        if (z2 && z && i == 2) {
            this.mNavigationBarController.touchAutoDim(this.mDisplayId);
            dismissVolumeDialog();
        }
        checkBarModes();
    }

    /* access modifiers changed from: private */
    public void dismissVolumeDialog() {
        VolumeComponent volumeComponent = this.mVolumeComponent;
        if (volumeComponent != null) {
            volumeComponent.dismissNow();
        }
    }

    public boolean inFullscreenMode() {
        return this.mAppFullscreen;
    }

    public boolean inImmersiveMode() {
        return this.mAppImmersive;
    }

    public static String viewInfo(View view) {
        return "[(" + view.getLeft() + "," + view.getTop() + ")(" + view.getRight() + "," + view.getBottom() + ") " + view.getWidth() + "x" + view.getHeight() + "]";
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        String str;
        synchronized (this.mQueueLock) {
            printWriter.println("Current Status Bar state:");
            printWriter.println("  mExpandedVisible=" + this.mExpandedVisible);
            printWriter.println("  mDisplayMetrics=" + this.mDisplayMetrics);
            printWriter.println("  mStackScroller: " + viewInfo(this.mStackScroller));
            printWriter.println("  mStackScroller: " + viewInfo(this.mStackScroller) + " scroll " + this.mStackScroller.getScrollX() + "," + this.mStackScroller.getScrollY());
        }
        printWriter.print("  mInteractingWindows=");
        printWriter.println(this.mInteractingWindows);
        printWriter.print("  mStatusBarWindowState=");
        printWriter.println(StatusBarManager.windowStateToString(this.mStatusBarWindowState));
        printWriter.print("  mStatusBarMode=");
        printWriter.println(BarTransitions.modeToString(this.mStatusBarMode));
        printWriter.print("  mDozing=");
        printWriter.println(this.mDozing);
        printWriter.print("  mWallpaperSupported= ");
        printWriter.println(this.mWallpaperSupported);
        printWriter.println("  StatusBarWindowView: ");
        NotificationShadeWindowViewController notificationShadeWindowViewController = this.mNotificationShadeWindowViewController;
        if (notificationShadeWindowViewController != null) {
            notificationShadeWindowViewController.dump(fileDescriptor, printWriter, strArr);
            dumpBarTransitions(printWriter, "PhoneStatusBarTransitions", this.mNotificationShadeWindowViewController.getBarTransitions());
        }
        printWriter.println("  mMediaManager: ");
        NotificationMediaManager notificationMediaManager = this.mMediaManager;
        if (notificationMediaManager != null) {
            notificationMediaManager.dump(fileDescriptor, printWriter, strArr);
        }
        printWriter.println("  Panels: ");
        if (this.mNotificationPanelViewController != null) {
            printWriter.println("    mNotificationPanel=" + this.mNotificationPanelViewController.getView() + " params=" + this.mNotificationPanelViewController.getView().getLayoutParams().debug(""));
            printWriter.print("      ");
            this.mNotificationPanelViewController.dump(fileDescriptor, printWriter, strArr);
        }
        printWriter.println("  mStackScroller: ");
        if (this.mStackScroller instanceof Dumpable) {
            printWriter.print("      ");
            ((Dumpable) this.mStackScroller).dump(fileDescriptor, printWriter, strArr);
        }
        printWriter.println("  Theme:");
        if (this.mUiModeManager == null) {
            str = "null";
        } else {
            str = this.mUiModeManager.getNightMode() + "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("    dark theme: ");
        sb.append(str);
        sb.append(" (auto: ");
        boolean z = false;
        sb.append(0);
        sb.append(", yes: ");
        sb.append(2);
        sb.append(", no: ");
        sb.append(1);
        sb.append(")");
        printWriter.println(sb.toString());
        if (this.mContext.getThemeResId() == C0019R$style.Theme_SystemUI_Light) {
            z = true;
        }
        printWriter.println("    light wallpaper theme: " + z);
        KeyguardIndicationController keyguardIndicationController = this.mKeyguardIndicationController;
        if (keyguardIndicationController != null) {
            keyguardIndicationController.dump(fileDescriptor, printWriter, strArr);
        }
        ScrimController scrimController = this.mScrimController;
        if (scrimController != null) {
            scrimController.dump(fileDescriptor, printWriter, strArr);
        }
        StatusBarKeyguardViewManager statusBarKeyguardViewManager = this.mStatusBarKeyguardViewManager;
        if (statusBarKeyguardViewManager != null) {
            statusBarKeyguardViewManager.dump(printWriter);
        }
        this.mNotificationsController.dump(fileDescriptor, printWriter, strArr, true);
        HeadsUpManagerPhone headsUpManagerPhone = this.mHeadsUpManager;
        if (headsUpManagerPhone != null) {
            headsUpManagerPhone.dump(fileDescriptor, printWriter, strArr);
        } else {
            printWriter.println("  mHeadsUpManager: null");
        }
        StatusBarTouchableRegionManager statusBarTouchableRegionManager = this.mStatusBarTouchableRegionManager;
        if (statusBarTouchableRegionManager != null) {
            statusBarTouchableRegionManager.dump(fileDescriptor, printWriter, strArr);
        } else {
            printWriter.println("  mStatusBarTouchableRegionManager: null");
        }
        LightBarController lightBarController = this.mLightBarController;
        if (lightBarController != null) {
            lightBarController.dump(fileDescriptor, printWriter, strArr);
        }
        this.mFalsingManager.dump(printWriter);
        FalsingLog.dump(printWriter);
        printWriter.println("SharedPreferences:");
        for (Map.Entry next : Prefs.getAll(this.mContext).entrySet()) {
            printWriter.print("  ");
            printWriter.print((String) next.getKey());
            printWriter.print("=");
            printWriter.println(next.getValue());
        }
    }

    static void dumpBarTransitions(PrintWriter printWriter, String str, BarTransitions barTransitions) {
        printWriter.print("  ");
        printWriter.print(str);
        printWriter.print(".BarTransitions.mMode=");
        printWriter.println(BarTransitions.modeToString(barTransitions.getMode()));
    }

    public void createAndAddWindows(RegisterStatusBarResult registerStatusBarResult) {
        makeStatusBarView(registerStatusBarResult);
        this.mNotificationShadeWindowController.attach();
        this.mStatusBarWindowController.attach();
        ((WallpaperCommandSender) Dependency.get(WallpaperCommandSender.class)).setWindowView(this.mNotificationShadeWindowView);
    }

    /* access modifiers changed from: package-private */
    public void updateDisplaySize() {
        this.mDisplay.getMetrics(this.mDisplayMetrics);
        this.mDisplay.getSize(this.mCurrentDisplaySize);
    }

    public float getDisplayDensity() {
        return this.mDisplayMetrics.density;
    }

    /* access modifiers changed from: package-private */
    public float getDisplayWidth() {
        return (float) this.mDisplayMetrics.widthPixels;
    }

    /* access modifiers changed from: package-private */
    public float getDisplayHeight() {
        return (float) this.mDisplayMetrics.heightPixels;
    }

    /* access modifiers changed from: package-private */
    public int getRotation() {
        return this.mDisplay.getRotation();
    }

    public void startActivityDismissingKeyguard(Intent intent, boolean z, boolean z2, int i) {
        startActivityDismissingKeyguard(intent, z, z2, false, (ActivityStarter.Callback) null, i);
    }

    public void startActivityDismissingKeyguard(Intent intent, boolean z, boolean z2) {
        startActivityDismissingKeyguard(intent, z, z2, 0);
    }

    public void startActivityDismissingKeyguard(Intent intent, boolean z, boolean z2, boolean z3, ActivityStarter.Callback callback, int i) {
        if (!z || this.mDeviceProvisionedController.isDeviceProvisioned()) {
            boolean wouldLaunchResolverActivity = this.mActivityIntentHelper.wouldLaunchResolverActivity(intent, this.mLockscreenUserManager.getCurrentUserId());
            executeRunnableDismissingKeyguard(new Runnable(intent, i, z3, callback) {
                public final /* synthetic */ Intent f$1;
                public final /* synthetic */ int f$2;
                public final /* synthetic */ boolean f$3;
                public final /* synthetic */ ActivityStarter.Callback f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                public final void run() {
                    StatusBar.this.lambda$startActivityDismissingKeyguard$16$StatusBar(this.f$1, this.f$2, this.f$3, this.f$4);
                }
            }, new Runnable() {
                public final void run() {
                    StatusBar.lambda$startActivityDismissingKeyguard$17(ActivityStarter.Callback.this);
                }
            }, z2, wouldLaunchResolverActivity, true);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startActivityDismissingKeyguard$16 */
    public /* synthetic */ void lambda$startActivityDismissingKeyguard$16$StatusBar(Intent intent, int i, boolean z, ActivityStarter.Callback callback) {
        int i2;
        Intent intent2 = intent;
        ActivityStarter.Callback callback2 = callback;
        this.mAssistManagerLazy.get().hideAssist();
        intent2.setFlags(335544320);
        intent.addFlags(i);
        ActivityOptions activityOptions = new ActivityOptions(getActivityOptions((RemoteAnimationAdapter) null));
        activityOptions.setDisallowEnterPictureInPictureWhileLaunching(z);
        if (intent2 == KeyguardBottomAreaView.INSECURE_CAMERA_INTENT) {
            activityOptions.setRotationAnimationHint(3);
        }
        if (intent.getAction() == "android.settings.panel.action.VOLUME") {
            activityOptions.setDisallowEnterPictureInPictureWhileLaunching(true);
        }
        try {
            i2 = ActivityTaskManager.getService().startActivityAsUser((IApplicationThread) null, this.mContext.getBasePackageName(), this.mContext.getAttributionTag(), intent, intent2.resolveTypeIfNeeded(this.mContext.getContentResolver()), (IBinder) null, (String) null, 0, 268435456, (ProfilerInfo) null, activityOptions.toBundle(), UserHandle.CURRENT.getIdentifier());
        } catch (RemoteException e) {
            Log.w("StatusBar", "Unable to start activity", e);
            i2 = -96;
        }
        if (callback2 != null) {
            callback2.onActivityStarted(i2);
        }
    }

    static /* synthetic */ void lambda$startActivityDismissingKeyguard$17(ActivityStarter.Callback callback) {
        if (callback != null) {
            callback.onActivityStarted(-96);
        }
    }

    public void readyForKeyguardDone() {
        this.mStatusBarKeyguardViewManager.readyForKeyguardDone();
    }

    public void executeRunnableDismissingKeyguard(Runnable runnable, Runnable runnable2, boolean z, boolean z2, boolean z3) {
        dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction(runnable, z, z3) {
            public final /* synthetic */ Runnable f$1;
            public final /* synthetic */ boolean f$2;
            public final /* synthetic */ boolean f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final boolean onDismiss() {
                return StatusBar.this.lambda$executeRunnableDismissingKeyguard$18$StatusBar(this.f$1, this.f$2, this.f$3);
            }
        }, runnable2, z2);
    }

    private /* synthetic */ boolean lambda$executeRunnableDismissingKeyguard$18(Runnable runnable, boolean z, boolean z2) {
        if (runnable != null) {
            if (!this.mStatusBarKeyguardViewManager.isShowing() || !this.mStatusBarKeyguardViewManager.isOccluded()) {
                AsyncTask.execute(runnable);
            } else {
                this.mStatusBarKeyguardViewManager.addAfterKeyguardGoneRunnable(runnable);
            }
        }
        if (z) {
            if (!this.mExpandedVisible || this.mBouncerShowing) {
                H h = this.mHandler;
                ShadeController shadeController = this.mShadeController;
                Objects.requireNonNull(shadeController);
                h.post(new Runnable() {
                    public final void run() {
                        ShadeController.this.runPostCollapseRunnables();
                    }
                });
            } else {
                this.mShadeController.animateCollapsePanels(2, true, true);
            }
        } else if (isInLaunchTransition() && this.mNotificationPanelViewController.isLaunchTransitionFinished()) {
            H h2 = this.mHandler;
            StatusBarKeyguardViewManager statusBarKeyguardViewManager = this.mStatusBarKeyguardViewManager;
            Objects.requireNonNull(statusBarKeyguardViewManager);
            h2.post(new Runnable() {
                public final void run() {
                    StatusBarKeyguardViewManager.this.readyForKeyguardDone();
                }
            });
        }
        return z2;
    }

    public void resetUserExpandedStates() {
        this.mNotificationsController.resetUserExpandedStates();
    }

    /* access modifiers changed from: private */
    public void executeWhenUnlocked(ActivityStarter.OnDismissAction onDismissAction, boolean z) {
        if (this.mStatusBarKeyguardViewManager.isShowing() && z) {
            this.mStatusBarStateController.setLeaveOpenOnKeyguardHide(true);
        }
        dismissKeyguardThenExecute(onDismissAction, (Runnable) null, false);
    }

    /* access modifiers changed from: protected */
    public void dismissKeyguardThenExecute(ActivityStarter.OnDismissAction onDismissAction, boolean z) {
        dismissKeyguardThenExecute(onDismissAction, (Runnable) null, z);
    }

    public void dismissKeyguardThenExecute(ActivityStarter.OnDismissAction onDismissAction, Runnable runnable, boolean z) {
        if (this.mWakefulnessLifecycle.getWakefulness() == 0 && this.mKeyguardStateController.canDismissLockScreen() && !this.mStatusBarStateController.leaveOpenOnKeyguardHide() && this.mDozeServiceHost.isPulsing()) {
            this.mBiometricUnlockController.startWakeAndUnlock(2);
        }
        if (this.mStatusBarKeyguardViewManager.isShowing()) {
            this.mStatusBarKeyguardViewManager.dismissWithAction(onDismissAction, runnable, z);
        } else {
            onDismissAction.onDismiss();
        }
    }

    public void onConfigChanged(Configuration configuration) {
        if (this.mPreviousConfig == null) {
            this.mPreviousConfig = new Configuration(configuration);
        }
        updateResources(CommonUtil.isThemeResourcesChanged(this.mPreviousConfig.updateFrom(configuration), configuration.extraConfig.themeChangedFlags));
        updateDisplaySize();
        this.mViewHierarchyManager.updateRowStates();
        this.mScreenPinningRequest.onConfigurationChanged();
    }

    public void setLockscreenUser(int i) {
        LockscreenWallpaper lockscreenWallpaper = this.mLockscreenWallpaper;
        if (lockscreenWallpaper != null) {
            lockscreenWallpaper.setCurrentUser(i);
        }
        this.mScrimController.setCurrentUser(i);
        if (this.mWallpaperSupported) {
            this.mWallpaperChangedReceiver.onReceive(this.mContext, (Intent) null);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateResources(boolean z) {
        QSContainerImpl qSContainerImpl = this.mQSContainer;
        if (!(qSContainerImpl == null || qSContainerImpl.getQuickQSPanel() == null)) {
            this.mQSContainer.getQuickQSPanel().updateResources();
        }
        StatusBarWindowController statusBarWindowController = this.mStatusBarWindowController;
        if (statusBarWindowController != null) {
            statusBarWindowController.refreshStatusBarHeight();
        }
        PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
        if (phoneStatusBarView != null) {
            phoneStatusBarView.updateResources();
        }
        NotificationPanelViewController notificationPanelViewController = this.mNotificationPanelViewController;
        if (notificationPanelViewController != null) {
            notificationPanelViewController.updateResources(z);
        }
        BrightnessMirrorController brightnessMirrorController = this.mBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.updateResources();
        }
    }

    /* access modifiers changed from: protected */
    public void handleVisibleToUserChanged(boolean z) {
        if (z) {
            handleVisibleToUserChangedImpl(z);
            this.mNotificationLogger.startNotificationLogging();
            return;
        }
        this.mNotificationLogger.stopNotificationLogging();
        handleVisibleToUserChangedImpl(z);
    }

    /* access modifiers changed from: package-private */
    public void handlePeekToExpandTransistion() {
        try {
            this.mBarService.onPanelRevealed(false, this.mNotificationsController.getActiveNotificationsCount());
        } catch (RemoteException unused) {
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0011, code lost:
        r0 = r3.mState;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleVisibleToUserChangedImpl(boolean r4) {
        /*
            r3 = this;
            if (r4 == 0) goto L_0x0038
            com.android.systemui.statusbar.phone.HeadsUpManagerPhone r4 = r3.mHeadsUpManager
            boolean r4 = r4.hasPinnedHeadsUp()
            com.android.systemui.statusbar.phone.StatusBarNotificationPresenter r0 = r3.mPresenter
            boolean r0 = r0.isPresenterFullyCollapsed()
            r1 = 1
            if (r0 != 0) goto L_0x001a
            int r0 = r3.mState
            if (r0 == 0) goto L_0x0018
            r2 = 2
            if (r0 != r2) goto L_0x001a
        L_0x0018:
            r0 = r1
            goto L_0x001b
        L_0x001a:
            r0 = 0
        L_0x001b:
            com.android.systemui.statusbar.notification.init.NotificationsController r2 = r3.mNotificationsController
            int r2 = r2.getActiveNotificationsCount()
            if (r4 == 0) goto L_0x002c
            com.android.systemui.statusbar.phone.StatusBarNotificationPresenter r4 = r3.mPresenter
            boolean r4 = r4.isPresenterFullyCollapsed()
            if (r4 == 0) goto L_0x002c
            goto L_0x002d
        L_0x002c:
            r1 = r2
        L_0x002d:
            java.util.concurrent.Executor r4 = r3.mUiBgExecutor
            com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$Kretey6oDHJkdzCG5ebWo979aVM r2 = new com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$Kretey6oDHJkdzCG5ebWo979aVM
            r2.<init>(r0, r1)
            r4.execute(r2)
            goto L_0x0042
        L_0x0038:
            java.util.concurrent.Executor r4 = r3.mUiBgExecutor
            com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$d2Fv8ckN-uR2NVMWI_rXx_hRNKc r0 = new com.android.systemui.statusbar.phone.-$$Lambda$StatusBar$d2Fv8ckN-uR2NVMWI_rXx_hRNKc
            r0.<init>()
            r4.execute(r0)
        L_0x0042:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.StatusBar.handleVisibleToUserChangedImpl(boolean):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleVisibleToUserChangedImpl$19 */
    public /* synthetic */ void lambda$handleVisibleToUserChangedImpl$19$StatusBar(boolean z, int i) {
        try {
            this.mBarService.onPanelRevealed(z, i);
        } catch (RemoteException unused) {
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleVisibleToUserChangedImpl$20 */
    public /* synthetic */ void lambda$handleVisibleToUserChangedImpl$20$StatusBar() {
        try {
            this.mBarService.onPanelHidden();
        } catch (RemoteException unused) {
        }
    }

    private void logStateToEventlog() {
        boolean isShowing = this.mStatusBarKeyguardViewManager.isShowing();
        boolean isOccluded = this.mStatusBarKeyguardViewManager.isOccluded();
        boolean isBouncerShowing = this.mStatusBarKeyguardViewManager.isBouncerShowing();
        boolean isMethodSecure = this.mKeyguardStateController.isMethodSecure();
        boolean canDismissLockScreen = this.mKeyguardStateController.canDismissLockScreen();
        int loggingFingerprint = getLoggingFingerprint(this.mState, isShowing, isOccluded, isBouncerShowing, isMethodSecure, canDismissLockScreen);
        if (loggingFingerprint != this.mLastLoggedStateFingerprint) {
            if (this.mStatusBarStateLog == null) {
                this.mStatusBarStateLog = new LogMaker(0);
            }
            this.mMetricsLogger.write(this.mStatusBarStateLog.setCategory(isBouncerShowing ? 197 : 196).setType(isShowing ? 1 : 2).setSubtype(isMethodSecure ? 1 : 0));
            EventLogTags.writeSysuiStatusBarState(this.mState, isShowing ? 1 : 0, isOccluded ? 1 : 0, isBouncerShowing ? 1 : 0, isMethodSecure ? 1 : 0, canDismissLockScreen ? 1 : 0);
            this.mLastLoggedStateFingerprint = loggingFingerprint;
            StringBuilder sb = new StringBuilder();
            sb.append(isBouncerShowing ? "BOUNCER" : "LOCKSCREEN");
            sb.append(isShowing ? "_OPEN" : "_CLOSE");
            sb.append(isMethodSecure ? "_SECURE" : "_INSECURE");
            sUiEventLogger.log(StatusBarUiEvent.valueOf(sb.toString()));
        }
    }

    /* access modifiers changed from: package-private */
    public void vibrate() {
        ((Vibrator) this.mContext.getSystemService("vibrator")).vibrate(250, VIBRATION_ATTRIBUTES);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$21 */
    public /* synthetic */ void lambda$new$21$StatusBar() {
        Debug.stopMethodTracing();
        Log.d("StatusBar", "stopTracing");
        vibrate();
    }

    public void postQSRunnableDismissingKeyguard(Runnable runnable) {
        this.mHandler.post(new Runnable(runnable) {
            public final /* synthetic */ Runnable f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                StatusBar.this.lambda$postQSRunnableDismissingKeyguard$23$StatusBar(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$postQSRunnableDismissingKeyguard$23 */
    public /* synthetic */ void lambda$postQSRunnableDismissingKeyguard$23$StatusBar(Runnable runnable) {
        this.mStatusBarStateController.setLeaveOpenOnKeyguardHide(true);
        executeRunnableDismissingKeyguard(new Runnable(runnable) {
            public final /* synthetic */ Runnable f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                StatusBar.this.lambda$postQSRunnableDismissingKeyguard$22$StatusBar(this.f$1);
            }
        }, (Runnable) null, false, false, false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$postQSRunnableDismissingKeyguard$22 */
    public /* synthetic */ void lambda$postQSRunnableDismissingKeyguard$22$StatusBar(Runnable runnable) {
        this.mHandler.post(runnable);
    }

    public void postStartActivityDismissingKeyguard(PendingIntent pendingIntent) {
        this.mHandler.post(new Runnable(pendingIntent) {
            public final /* synthetic */ PendingIntent f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                StatusBar.this.lambda$postStartActivityDismissingKeyguard$24$StatusBar(this.f$1);
            }
        });
    }

    public void postStartActivityDismissingKeyguard(Intent intent, int i) {
        this.mHandler.postDelayed(new Runnable(intent) {
            public final /* synthetic */ Intent f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                StatusBar.this.lambda$postStartActivityDismissingKeyguard$25$StatusBar(this.f$1);
            }
        }, (long) i);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$postStartActivityDismissingKeyguard$25 */
    public /* synthetic */ void lambda$postStartActivityDismissingKeyguard$25$StatusBar(Intent intent) {
        handleStartActivityDismissingKeyguard(intent, true);
    }

    private void handleStartActivityDismissingKeyguard(Intent intent, boolean z) {
        startActivityDismissingKeyguard(intent, z, true);
    }

    public void dispatchDemoCommand(String str, Bundle bundle) {
        View view;
        VolumeComponent volumeComponent;
        int i = 0;
        if (!this.mDemoModeAllowed) {
            this.mDemoModeAllowed = Settings.Global.getInt(this.mContext.getContentResolver(), "sysui_demo_allowed", 0) != 0;
        }
        if (this.mDemoModeAllowed) {
            if (str.equals("enter")) {
                this.mDemoMode = true;
            } else if (str.equals("exit")) {
                this.mDemoMode = false;
                checkBarModes();
            } else if (!this.mDemoMode) {
                dispatchDemoCommand("enter", new Bundle());
            }
            boolean z = str.equals("enter") || str.equals("exit");
            if ((z || str.equals("volume")) && (volumeComponent = this.mVolumeComponent) != null) {
                volumeComponent.dispatchDemoCommand(str, bundle);
            }
            if (z || str.equals("clock")) {
                dispatchDemoCommandToView(str, bundle, C0012R$id.clock);
            }
            if (z || str.equals("battery")) {
                this.mBatteryController.dispatchDemoCommand(str, bundle);
            }
            if (z || str.equals("status")) {
                ((StatusBarIconControllerImpl) this.mIconController).dispatchDemoCommand(str, bundle);
            }
            if (this.mNetworkController != null && (z || str.equals("network"))) {
                this.mNetworkController.dispatchDemoCommand(str, bundle);
            }
            if (z || str.equals("notifications")) {
                PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
                if (phoneStatusBarView == null) {
                    view = null;
                } else {
                    view = phoneStatusBarView.findViewById(C0012R$id.fullscreen_notification_icon_area);
                }
                if (view != null) {
                    view.setVisibility((!this.mDemoMode || !"false".equals(bundle.getString("visible"))) ? 0 : 4);
                }
            }
            if (str.equals("bars")) {
                String string = bundle.getString("mode");
                if ("opaque".equals(string)) {
                    i = 4;
                } else if ("translucent".equals(string)) {
                    i = 2;
                } else if ("semi-transparent".equals(string)) {
                    i = 1;
                } else if (!"transparent".equals(string)) {
                    i = "warning".equals(string) ? 5 : -1;
                }
                if (i != -1) {
                    if (!(this.mNotificationShadeWindowController == null || this.mNotificationShadeWindowViewController.getBarTransitions() == null)) {
                        this.mNotificationShadeWindowViewController.getBarTransitions().transitionTo(i, true);
                    }
                    this.mNavigationBarController.transitionTo(this.mDisplayId, i, true);
                }
            }
            if (z || str.equals("operator")) {
                dispatchDemoCommandToView(str, bundle, C0012R$id.operator_name);
            }
        }
    }

    private void dispatchDemoCommandToView(String str, Bundle bundle, int i) {
        PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
        if (phoneStatusBarView != null) {
            View findViewById = phoneStatusBarView.findViewById(i);
            if (findViewById instanceof DemoMode) {
                ((DemoMode) findViewById).dispatchDemoCommand(str, bundle);
            }
        }
    }

    public void showKeyguard() {
        this.mStatusBarStateController.setKeyguardRequested(true);
        this.mStatusBarStateController.setLeaveOpenOnKeyguardHide(false);
        updateIsKeyguard();
        this.mAssistManagerLazy.get().onLockscreenShown();
    }

    public boolean hideKeyguard() {
        this.mStatusBarStateController.setKeyguardRequested(false);
        return updateIsKeyguard();
    }

    public boolean isFullScreenUserSwitcherState() {
        return this.mState == 3;
    }

    /* access modifiers changed from: package-private */
    public boolean updateIsKeyguard() {
        boolean z = true;
        boolean z2 = this.mBiometricUnlockController.getMode() == 1;
        boolean z3 = this.mDozeServiceHost.getDozingRequested() && (!this.mDeviceInteractive || (isGoingToSleep() && (isScreenFullyOff() || this.mIsKeyguard)));
        if ((!this.mStatusBarStateController.isKeyguardRequested() && !z3) || z2) {
            z = false;
        }
        if (z3) {
            updatePanelExpansionForKeyguard();
        }
        if (!z) {
            return hideKeyguardImpl();
        }
        if (!isGoingToSleep() || this.mScreenLifecycle.getScreenState() != 3) {
            showKeyguardImpl();
        }
        return false;
    }

    public void showKeyguardImpl() {
        this.mIsKeyguard = true;
        if (this.mKeyguardStateController.isLaunchTransitionFadingAway()) {
            this.mNotificationPanelViewController.cancelAnimation();
            onLaunchTransitionFadingEnded();
        }
        this.mHandler.removeMessages(1003);
        UserSwitcherController userSwitcherController = this.mUserSwitcherController;
        if (userSwitcherController != null && userSwitcherController.useFullscreenUserSwitcher()) {
            this.mStatusBarStateController.setState(3);
        } else if (!this.mPulseExpansionHandler.isWakingToShadeLocked()) {
            this.mStatusBarStateController.setState(1);
        }
        updatePanelExpansionForKeyguard();
        NotificationEntry notificationEntry = this.mDraggedDownEntry;
        if (notificationEntry != null) {
            notificationEntry.setUserLocked(false);
            this.mDraggedDownEntry.notifyHeightChanged(false);
            this.mDraggedDownEntry = null;
        }
    }

    private void updatePanelExpansionForKeyguard() {
        if (this.mState == 1 && this.mBiometricUnlockController.getMode() != 1 && !this.mBouncerShowing) {
            this.mShadeController.instantExpandNotificationsPanel();
        } else if (this.mState == 3) {
            instantCollapseNotificationPanel();
        }
    }

    /* access modifiers changed from: private */
    public void onLaunchTransitionFadingEnded() {
        this.mNotificationPanelViewController.setAlpha(1.0f);
        this.mNotificationPanelViewController.onAffordanceLaunchEnded();
        releaseGestureWakeLock();
        runLaunchTransitionEndRunnable();
        this.mKeyguardStateController.setLaunchTransitionFadingAway(false);
        this.mPresenter.updateMediaMetaData(true, true);
    }

    public boolean isInLaunchTransition() {
        return this.mNotificationPanelViewController.isLaunchTransitionRunning() || this.mNotificationPanelViewController.isLaunchTransitionFinished();
    }

    public void fadeKeyguardAfterLaunchTransition(Runnable runnable, Runnable runnable2) {
        this.mHandler.removeMessages(1003);
        this.mLaunchTransitionEndRunnable = runnable2;
        $$Lambda$StatusBar$7ezyz_9FnfBd8mjriwKdK6e2MnQ r4 = new Runnable(runnable) {
            public final /* synthetic */ Runnable f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                StatusBar.this.lambda$fadeKeyguardAfterLaunchTransition$26$StatusBar(this.f$1);
            }
        };
        if (this.mNotificationPanelViewController.isLaunchTransitionRunning()) {
            this.mNotificationPanelViewController.setLaunchTransitionEndRunnable(r4);
        } else {
            r4.run();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$fadeKeyguardAfterLaunchTransition$26 */
    public /* synthetic */ void lambda$fadeKeyguardAfterLaunchTransition$26$StatusBar(Runnable runnable) {
        this.mKeyguardStateController.setLaunchTransitionFadingAway(true);
        if (runnable != null) {
            runnable.run();
        }
        updateScrimController();
        this.mPresenter.updateMediaMetaData(false, true);
        this.mNotificationPanelViewController.setAlpha(1.0f);
        this.mNotificationPanelViewController.fadeOut(100, 300, new Runnable() {
            public final void run() {
                StatusBar.this.onLaunchTransitionFadingEnded();
            }
        });
        this.mCommandQueue.appTransitionStarting(this.mDisplayId, SystemClock.uptimeMillis(), 120, true);
    }

    public void fadeKeyguardWhilePulsing() {
        this.mNotificationPanelViewController.fadeOut(0, 96, new Runnable() {
            public final void run() {
                StatusBar.this.lambda$fadeKeyguardWhilePulsing$27$StatusBar();
            }
        }).start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$fadeKeyguardWhilePulsing$27 */
    public /* synthetic */ void lambda$fadeKeyguardWhilePulsing$27$StatusBar() {
        hideKeyguard();
        this.mStatusBarKeyguardViewManager.onKeyguardFadedAway();
    }

    public void animateKeyguardUnoccluding() {
        this.mNotificationPanelViewController.setExpandedFraction(0.0f);
        animateExpandNotificationsPanel();
    }

    public void startLaunchTransitionTimeout() {
        this.mHandler.sendEmptyMessageDelayed(1003, 5000);
    }

    /* access modifiers changed from: private */
    public void onLaunchTransitionTimeout() {
        Log.w("StatusBar", "Launch transition: Timeout!");
        this.mNotificationPanelViewController.onAffordanceLaunchEnded();
        releaseGestureWakeLock();
        this.mNotificationPanelViewController.resetViews(false);
    }

    private void runLaunchTransitionEndRunnable() {
        Runnable runnable = this.mLaunchTransitionEndRunnable;
        if (runnable != null) {
            this.mLaunchTransitionEndRunnable = null;
            runnable.run();
        }
    }

    public boolean hideKeyguardImpl() {
        this.mIsKeyguard = false;
        Trace.beginSection("StatusBar#hideKeyguard");
        boolean leaveOpenOnKeyguardHide = this.mStatusBarStateController.leaveOpenOnKeyguardHide();
        if (!this.mStatusBarStateController.setState(0)) {
            this.mLockscreenUserManager.updatePublicMode();
        }
        if (this.mStatusBarStateController.leaveOpenOnKeyguardHide()) {
            if (!this.mStatusBarStateController.isKeyguardRequested()) {
                this.mStatusBarStateController.setLeaveOpenOnKeyguardHide(false);
            }
            long calculateGoingToFullShadeDelay = this.mKeyguardStateController.calculateGoingToFullShadeDelay();
            this.mNotificationPanelViewController.animateToFullShade(calculateGoingToFullShadeDelay);
            NotificationEntry notificationEntry = this.mDraggedDownEntry;
            if (notificationEntry != null) {
                notificationEntry.setUserLocked(false);
                this.mDraggedDownEntry = null;
            }
            this.mNavigationBarController.disableAnimationsDuringHide(this.mDisplayId, calculateGoingToFullShadeDelay);
        } else if (!this.mNotificationPanelViewController.isCollapsing()) {
            instantCollapseNotificationPanel();
        }
        if (this.mControlPanelController.isUseControlCenter()) {
            this.mControlPanelController.refreshAllTiles();
        } else {
            QSPanel qSPanel = this.mQSPanel;
            if (qSPanel != null) {
                qSPanel.refreshAllTiles();
            }
        }
        this.mHandler.removeMessages(1003);
        releaseGestureWakeLock();
        this.mNotificationPanelViewController.onAffordanceLaunchEnded();
        this.mNotificationPanelViewController.cancelAnimation();
        this.mNotificationPanelViewController.setAlpha(1.0f);
        this.mNotificationPanelViewController.resetViewGroupFade();
        updateScrimController();
        Trace.endSection();
        return leaveOpenOnKeyguardHide;
    }

    /* access modifiers changed from: private */
    public void releaseGestureWakeLock() {
        if (this.mGestureWakeLock.isHeld()) {
            this.mGestureWakeLock.release();
        }
    }

    public void keyguardGoingAway() {
        this.mKeyguardStateController.notifyKeyguardGoingAway(true);
        this.mCommandQueue.appTransitionPending(this.mDisplayId, true);
    }

    public void setKeyguardFadingAway(long j, long j2, long j3, boolean z) {
        this.mCommandQueue.appTransitionStarting(this.mDisplayId, (j + j3) - 120, 120, true);
        this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, j3 > 0);
        this.mCommandQueue.appTransitionStarting(this.mDisplayId, j - 120, 120, true);
        this.mKeyguardStateController.notifyKeyguardFadingAway(j2, j3, z);
    }

    public void finishKeyguardFadingAway() {
        this.mKeyguardStateController.notifyKeyguardDoneFading();
        this.mScrimController.setExpansionAffectsAlpha(false);
    }

    /* access modifiers changed from: protected */
    public void updateTheme() {
        int i = this.mColorExtractor.getNeutralColors().supportsDarkText() ? C0019R$style.Theme_SystemUI_Light : C0019R$style.Theme_SystemUI;
        if (this.mContext.getThemeResId() != i) {
            this.mContext.setTheme(i);
            this.mConfigurationController.notifyThemeChanged();
        }
    }

    private void updateDozingState() {
        Trace.traceCounter(4096, "dozing", this.mDozing ? 1 : 0);
        Trace.beginSection("StatusBar#updateDozingState");
        boolean z = false;
        boolean z2 = this.mStatusBarKeyguardViewManager.isShowing() && !this.mStatusBarKeyguardViewManager.isOccluded();
        boolean z3 = this.mBiometricUnlockController.getMode() == 1;
        if ((!this.mDozing && this.mDozeServiceHost.shouldAnimateWakeup() && !z3) || (this.mDozing && this.mDozeServiceHost.shouldAnimateScreenOff() && z2)) {
            z = true;
        }
        this.mNotificationPanelViewController.setDozing(this.mDozing, z, this.mWakeUpTouchLocation);
        updateQsExpansionEnabled();
        Trace.endSection();
    }

    public void userActivity() {
        if (this.mState == 1) {
            this.mKeyguardViewMediatorCallback.userActivity();
        }
    }

    public boolean interceptMediaKey(KeyEvent keyEvent) {
        if (this.mState != 1 || !this.mStatusBarKeyguardViewManager.interceptMediaKey(keyEvent)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean shouldUnlockOnMenuPressed() {
        return this.mDeviceInteractive && this.mState != 0 && this.mStatusBarKeyguardViewManager.shouldDismissOnMenuPressed();
    }

    public boolean onMenuPressed() {
        if (!shouldUnlockOnMenuPressed()) {
            return false;
        }
        this.mShadeController.animateCollapsePanels(2, true);
        return true;
    }

    public void endAffordanceLaunch() {
        releaseGestureWakeLock();
        this.mNotificationPanelViewController.onAffordanceLaunchEnded();
    }

    public boolean onBackPressed() {
        boolean z = this.mScrimController.getState() == ScrimState.BOUNCER_SCRIMMED;
        if (this.mStatusBarKeyguardViewManager.onBackPressed(z)) {
            if (!z) {
                this.mNotificationPanelViewController.expandWithoutQs();
            }
            return true;
        } else if (this.mNotificationPanelViewController.isQsExpanded()) {
            if (this.mNotificationPanelViewController.isQsDetailShowing()) {
                this.mNotificationPanelViewController.closeQsDetail();
            } else {
                this.mNotificationPanelViewController.animateCloseQs(false);
            }
            return true;
        } else {
            int i = this.mState;
            if (i == 1 || i == 2) {
                KeyguardUserSwitcher keyguardUserSwitcher = this.mKeyguardUserSwitcher;
                return keyguardUserSwitcher != null && keyguardUserSwitcher.hideIfNotSimple(true);
            }
            if (this.mNotificationPanelViewController.canPanelBeCollapsed()) {
                this.mShadeController.animateCollapsePanels();
            } else {
                this.mBubbleController.performBackPressIfNeeded();
            }
            return true;
        }
    }

    public boolean onSpacePressed() {
        if (!this.mDeviceInteractive || this.mState == 0) {
            return false;
        }
        this.mShadeController.animateCollapsePanels(2, true);
        return true;
    }

    public void showBouncerIfKeyguard() {
        int i = this.mState;
        if ((i == 1 || i == 2) && !this.mKeyguardViewMediator.isHiding() && !this.mDozing) {
            this.mStatusBarKeyguardViewManager.showBouncer(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void instantCollapseNotificationPanel() {
        this.mNotificationPanelViewController.instantCollapse();
        this.mShadeController.runPostCollapseRunnables();
    }

    public void onStatePreChange(int i, int i2) {
        if (this.mVisible && (i2 == 2 || this.mStatusBarStateController.goingToFullShade())) {
            clearNotificationEffects();
        }
        if (i2 == 1) {
            this.mRemoteInputManager.onPanelCollapsed();
            maybeEscalateHeadsUp();
        }
    }

    public void onStateChanged(int i) {
        this.mState = i;
        updateReportRejectedTouchVisibility();
        this.mDozeServiceHost.updateDozing();
        updateTheme();
        this.mNavigationBarController.touchAutoDim(this.mDisplayId);
        Trace.beginSection("StatusBar#updateKeyguardState");
        boolean z = true;
        if (this.mState == 1) {
            this.mKeyguardIndicationController.setVisible(true);
            KeyguardUserSwitcher keyguardUserSwitcher = this.mKeyguardUserSwitcher;
            if (keyguardUserSwitcher != null) {
                keyguardUserSwitcher.setKeyguard(true, this.mStatusBarStateController.fromShadeLocked());
            }
            PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
            if (phoneStatusBarView != null) {
                phoneStatusBarView.removePendingHideExpandedRunnables();
            }
            View view = this.mAmbientIndicationContainer;
            if (view != null) {
                view.setVisibility(0);
            }
        } else {
            this.mKeyguardIndicationController.setVisible(false);
            KeyguardUserSwitcher keyguardUserSwitcher2 = this.mKeyguardUserSwitcher;
            if (keyguardUserSwitcher2 != null) {
                keyguardUserSwitcher2.setKeyguard(false, this.mStatusBarStateController.goingToFullShade() || this.mState == 2 || this.mStatusBarStateController.fromShadeLocked());
            }
            View view2 = this.mAmbientIndicationContainer;
            if (view2 != null) {
                view2.setVisibility(4);
            }
        }
        updateDozingState();
        checkBarModes();
        updateScrimController();
        StatusBarNotificationPresenter statusBarNotificationPresenter = this.mPresenter;
        if (this.mState == 1) {
            z = false;
        }
        statusBarNotificationPresenter.updateMediaMetaData(false, z);
        updateKeyguardState();
        Trace.endSection();
    }

    public void onDozingChanged(boolean z) {
        Trace.beginSection("StatusBar#updateDozing");
        this.mDozing = z;
        this.mNotificationPanelViewController.resetViews(this.mDozeServiceHost.getDozingRequested() && this.mDozeParameters.shouldControlScreenOff());
        updateQsExpansionEnabled();
        this.mKeyguardViewMediator.setDozing(this.mDozing);
        this.mNotificationsController.requestNotificationUpdate("onDozingChanged");
        updateDozingState();
        this.mDozeServiceHost.updateDozing();
        updateScrimController();
        updateReportRejectedTouchVisibility();
        Trace.endSection();
    }

    private void updateKeyguardState() {
        this.mKeyguardStateController.notifyKeyguardState(this.mStatusBarKeyguardViewManager.isShowing(), this.mStatusBarKeyguardViewManager.isOccluded());
    }

    public void onTrackingStarted() {
        this.mShadeController.runPostCollapseRunnables();
    }

    public void onClosingFinished() {
        this.mShadeController.runPostCollapseRunnables();
        if (!this.mPresenter.isPresenterFullyCollapsed()) {
            this.mNotificationShadeWindowController.setNotificationShadeFocusable(true);
        }
    }

    public void onUnlockHintStarted() {
        this.mFalsingManager.onUnlockHintStarted();
        this.mKeyguardIndicationController.showTransientIndication(C0018R$string.keyguard_unlock);
    }

    public void onHintFinished() {
        this.mKeyguardIndicationController.hideTransientIndicationDelayed(1200);
    }

    public void onTrackingStopped(boolean z) {
        int i = this.mState;
        if ((i == 1 || i == 2) && !z && !this.mKeyguardStateController.canDismissLockScreen()) {
            this.mStatusBarKeyguardViewManager.showBouncer(true, true);
        }
    }

    public NavigationBarView getNavigationBarView() {
        return this.mNavigationBarController.getNavigationBarView(this.mDisplayId);
    }

    /* access modifiers changed from: package-private */
    public void goToLockedShade(View view) {
        if ((this.mDisabled2 & 4) == 0) {
            int currentUserId = this.mLockscreenUserManager.getCurrentUserId();
            NotificationEntry notificationEntry = null;
            if (view instanceof ExpandableNotificationRow) {
                notificationEntry = ((ExpandableNotificationRow) view).getEntry();
                notificationEntry.setUserExpanded(true, true);
                notificationEntry.setGroupExpansionChanging(true);
                currentUserId = notificationEntry.getSbn().getUserId();
            }
            NotificationLockscreenUserManager notificationLockscreenUserManager = this.mLockscreenUserManager;
            boolean z = false;
            boolean z2 = !notificationLockscreenUserManager.userAllowsPrivateNotificationsInPublic(notificationLockscreenUserManager.getCurrentUserId()) || !this.mLockscreenUserManager.shouldShowLockscreenNotifications() || this.mFalsingManager.shouldEnforceBouncer();
            if (!this.mKeyguardBypassController.getBypassEnabled()) {
                z = z2;
            }
            if (!this.mLockscreenUserManager.isLockscreenPublicMode(currentUserId) || !z) {
                this.mNotificationPanelViewController.animateToFullShade(0);
                this.mStatusBarStateController.setState(2);
                return;
            }
            this.mStatusBarStateController.setLeaveOpenOnKeyguardHide(true);
            showBouncerIfKeyguard();
            this.mDraggedDownEntry = notificationEntry;
        }
    }

    public void setBouncerShowing(boolean z) {
        this.mBouncerShowing = z;
        this.mKeyguardBypassController.setBouncerShowing(z);
        this.mPulseExpansionHandler.setBouncerShowing(z);
        this.mLockscreenLockIconController.setBouncerShowingScrimmed(isBouncerShowingScrimmed());
        PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
        if (phoneStatusBarView != null) {
            phoneStatusBarView.setBouncerShowing(z);
        }
        updateHideIconsForBouncer(true);
        this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, true);
        updateScrimController();
        if (!this.mBouncerShowing) {
            updatePanelExpansionForKeyguard();
        }
    }

    /* access modifiers changed from: package-private */
    public void updateNotificationPanelTouchState() {
        boolean z = false;
        boolean z2 = isGoingToSleep() && !this.mDozeParameters.shouldControlScreenOff();
        if ((!this.mDeviceInteractive && !this.mDozeServiceHost.isPulsing()) || z2) {
            z = true;
        }
        this.mNotificationPanelViewController.setTouchAndAnimationDisabled(z);
        this.mNotificationIconAreaController.setAnimationsEnabled(!z);
    }

    public int getWakefulnessState() {
        return this.mWakefulnessLifecycle.getWakefulness();
    }

    private void vibrateForCameraGesture() {
        this.mVibrator.vibrate(this.mCameraLaunchGestureVibePattern, -1);
    }

    public boolean isScreenFullyOff() {
        return this.mScreenLifecycle.getScreenState() == 0;
    }

    public void showScreenPinningRequest(int i) {
        if (!this.mKeyguardStateController.isShowing()) {
            showScreenPinningRequest(i, true);
        }
    }

    public void showScreenPinningRequest(int i, boolean z) {
        this.mScreenPinningRequest.showPrompt(i, z);
    }

    public void appTransitionCancelled(int i) {
        if (i == this.mDisplayId) {
            this.mDividerOptional.ifPresent($$Lambda$0LwwxILcL3cgEtrSMW_qhRkAhLc.INSTANCE);
        }
    }

    public void appTransitionFinished(int i) {
        if (i == this.mDisplayId) {
            this.mDividerOptional.ifPresent($$Lambda$0LwwxILcL3cgEtrSMW_qhRkAhLc.INSTANCE);
        }
    }

    public void onCameraLaunchGestureDetected(int i) {
        this.mLastCameraLaunchSource = i;
        if (isGoingToSleep()) {
            this.mLaunchCameraOnFinishedGoingToSleep = true;
        } else if (this.mNotificationPanelViewController.canCameraGestureBeLaunched()) {
            if (!this.mDeviceInteractive) {
                this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), 5, "com.android.systemui:CAMERA_GESTURE");
            }
            vibrateForCameraGesture();
            if (i == 1) {
                Log.v("StatusBar", "Camera launch");
                this.mKeyguardUpdateMonitor.onCameraLaunched();
            }
            if (!this.mStatusBarKeyguardViewManager.isShowing()) {
                startActivityDismissingKeyguard(KeyguardBottomAreaView.INSECURE_CAMERA_INTENT, false, true, true, (ActivityStarter.Callback) null, 0);
                return;
            }
            if (!this.mDeviceInteractive) {
                this.mGestureWakeLock.acquire(6000);
            }
            if (isWakingUpOrAwake()) {
                if (this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
                    this.mStatusBarKeyguardViewManager.reset(true);
                }
                this.mNotificationPanelViewController.launchCamera(this.mDeviceInteractive, i);
                updateScrimController();
                return;
            }
            this.mLaunchCameraWhenFinishedWaking = true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isCameraAllowedByAdmin() {
        if (this.mDevicePolicyManager.getCameraDisabled((ComponentName) null, this.mLockscreenUserManager.getCurrentUserId())) {
            return false;
        }
        if (this.mStatusBarKeyguardViewManager != null && (!isKeyguardShowing() || !isKeyguardSecure())) {
            return true;
        }
        if ((this.mDevicePolicyManager.getKeyguardDisabledFeatures((ComponentName) null, this.mLockscreenUserManager.getCurrentUserId()) & 2) == 0) {
            return true;
        }
        return false;
    }

    private boolean isGoingToSleep() {
        return this.mWakefulnessLifecycle.getWakefulness() == 3;
    }

    private boolean isWakingUpOrAwake() {
        if (this.mWakefulnessLifecycle.getWakefulness() == 2 || this.mWakefulnessLifecycle.getWakefulness() == 1) {
            return true;
        }
        return false;
    }

    public void notifyBiometricAuthModeChanged() {
        this.mDozeServiceHost.updateDozing();
        updateScrimController();
        this.mLockscreenLockIconController.onBiometricAuthModeChanged(this.mBiometricUnlockController.isWakeAndUnlock(), this.mBiometricUnlockController.isBiometricUnlock());
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updateScrimController() {
        Trace.beginSection("StatusBar#updateScrimController");
        boolean z = this.mBiometricUnlockController.isWakeAndUnlock() || this.mKeyguardStateController.isKeyguardFadingAway();
        this.mScrimController.setExpansionAffectsAlpha(false);
        boolean isLaunchingAffordanceWithPreview = this.mNotificationPanelViewController.isLaunchingAffordanceWithPreview();
        this.mScrimController.setLaunchingAffordanceWithPreview(isLaunchingAffordanceWithPreview);
        if (this.mBouncerShowing) {
            this.mScrimController.transitionTo(this.mStatusBarKeyguardViewManager.bouncerNeedsScrimming() ? ScrimState.BOUNCER_SCRIMMED : ScrimState.BOUNCER);
        } else if (isInLaunchTransition() || this.mLaunchCameraWhenFinishedWaking || isLaunchingAffordanceWithPreview) {
            this.mScrimController.transitionTo(ScrimState.UNLOCKED, this.mUnlockScrimCallback);
        } else if (this.mBrightnessMirrorVisible) {
            this.mScrimController.transitionTo(ScrimState.BRIGHTNESS_MIRROR);
        } else if (this.mDozeServiceHost.isPulsing()) {
            this.mScrimController.transitionTo(ScrimState.PULSING, this.mDozeScrimController.getScrimCallback());
        } else if (this.mDozeServiceHost.hasPendingScreenOffCallback()) {
            this.mScrimController.transitionTo(ScrimState.OFF, new ScrimController.Callback() {
                public void onFinished() {
                    StatusBar.this.mDozeServiceHost.executePendingScreenOffCallback();
                }
            });
        } else if (this.mDozing && !z) {
            this.mScrimController.transitionTo(ScrimState.AOD);
        } else if (this.mIsKeyguard && !z) {
            this.mScrimController.transitionTo(ScrimState.KEYGUARD);
        } else if (this.mBubbleController.isStackExpanded()) {
            this.mScrimController.transitionTo(ScrimState.BUBBLE_EXPANDED, this.mUnlockScrimCallback);
        } else {
            this.mScrimController.transitionTo(ScrimState.UNLOCKED, this.mUnlockScrimCallback);
        }
        Trace.endSection();
    }

    public boolean isKeyguardShowing() {
        StatusBarKeyguardViewManager statusBarKeyguardViewManager = this.mStatusBarKeyguardViewManager;
        if (statusBarKeyguardViewManager != null) {
            return statusBarKeyguardViewManager.isShowing();
        }
        Slog.i("StatusBar", "isKeyguardShowing() called before startKeyguard(), returning true");
        return true;
    }

    public boolean shouldIgnoreTouch() {
        return this.mStatusBarStateController.isDozing() && this.mDozeServiceHost.getIgnoreTouchWhilePulsing();
    }

    public boolean isDeviceInteractive() {
        return this.mDeviceInteractive;
    }

    public void setNotificationSnoozed(StatusBarNotification statusBarNotification, NotificationSwipeActionHelper.SnoozeOption snoozeOption) {
        this.mNotificationsController.setNotificationSnoozed(statusBarNotification, snoozeOption);
    }

    public void setNotificationSnoozed(StatusBarNotification statusBarNotification, int i) {
        this.mNotificationsController.setNotificationSnoozed(statusBarNotification, i);
    }

    public void toggleSplitScreen() {
        toggleSplitScreenMode(-1, -1);
    }

    /* access modifiers changed from: package-private */
    public void awakenDreams() {
        this.mUiBgExecutor.execute(new Runnable() {
            public final void run() {
                StatusBar.this.lambda$awakenDreams$28$StatusBar();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$awakenDreams$28 */
    public /* synthetic */ void lambda$awakenDreams$28$StatusBar() {
        try {
            this.mDreamManager.awaken();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void preloadRecentApps() {
        this.mHandler.removeMessages(1022);
        this.mHandler.sendEmptyMessage(1022);
    }

    public void cancelPreloadRecentApps() {
        this.mHandler.removeMessages(1023);
        this.mHandler.sendEmptyMessage(1023);
    }

    public void dismissKeyboardShortcutsMenu() {
        this.mHandler.removeMessages(1027);
        this.mHandler.sendEmptyMessage(1027);
    }

    public void toggleKeyboardShortcutsMenu(int i) {
        this.mHandler.removeMessages(1026);
        this.mHandler.obtainMessage(1026, i, 0).sendToTarget();
    }

    public void setTopAppHidesStatusBar(boolean z) {
        this.mTopHidesStatusBar = z;
        if (!z && this.mWereIconsJustHidden) {
            this.mWereIconsJustHidden = false;
            this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, true);
        }
        updateHideIconsForBouncer(true);
    }

    /* access modifiers changed from: protected */
    public void toggleKeyboardShortcuts(int i) {
        KeyboardShortcuts.toggle(this.mContext, i);
    }

    /* access modifiers changed from: protected */
    public void dismissKeyboardShortcuts() {
        KeyboardShortcuts.dismiss();
    }

    public void onPanelLaidOut() {
        updateKeyguardMaxNotifications();
    }

    public void updateKeyguardMaxNotifications() {
        if (this.mState == 1 && this.mPresenter.getMaxNotificationsWhileLocked(false) != this.mPresenter.getMaxNotificationsWhileLocked(true)) {
            this.mViewHierarchyManager.updateRowStates();
        }
    }

    public void executeActionDismissingKeyguard(Runnable runnable, boolean z) {
        if (this.mDeviceProvisionedController.isDeviceProvisioned()) {
            dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction(runnable) {
                public final /* synthetic */ Runnable f$1;

                {
                    this.f$1 = r2;
                }

                public final boolean onDismiss() {
                    return StatusBar.this.lambda$executeActionDismissingKeyguard$30$StatusBar(this.f$1);
                }
            }, z);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$executeActionDismissingKeyguard$30 */
    public /* synthetic */ boolean lambda$executeActionDismissingKeyguard$30$StatusBar(Runnable runnable) {
        new Thread(new Runnable(runnable) {
            public final /* synthetic */ Runnable f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                StatusBar.lambda$executeActionDismissingKeyguard$29(this.f$0);
            }
        }).start();
        return this.mShadeController.collapsePanel();
    }

    static /* synthetic */ void lambda$executeActionDismissingKeyguard$29(Runnable runnable) {
        try {
            ActivityManager.getService().resumeAppSwitches();
        } catch (RemoteException unused) {
        }
        runnable.run();
    }

    /* renamed from: startPendingIntentDismissingKeyguard */
    public void lambda$postStartActivityDismissingKeyguard$24(PendingIntent pendingIntent) {
        startPendingIntentDismissingKeyguard(pendingIntent, (Runnable) null);
    }

    public void startPendingIntentDismissingKeyguard(PendingIntent pendingIntent, Runnable runnable) {
        startPendingIntentDismissingKeyguard(pendingIntent, runnable, (View) null);
    }

    public void startPendingIntentDismissingKeyguard(PendingIntent pendingIntent, Runnable runnable, View view) {
        executeActionDismissingKeyguard(new Runnable(pendingIntent, view, runnable) {
            public final /* synthetic */ PendingIntent f$1;
            public final /* synthetic */ View f$2;
            public final /* synthetic */ Runnable f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                StatusBar.this.lambda$startPendingIntentDismissingKeyguard$31$StatusBar(this.f$1, this.f$2, this.f$3);
            }
        }, pendingIntent.isActivity() && this.mActivityIntentHelper.wouldLaunchResolverActivity(pendingIntent.getIntent(), this.mLockscreenUserManager.getCurrentUserId()));
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startPendingIntentDismissingKeyguard$31 */
    public /* synthetic */ void lambda$startPendingIntentDismissingKeyguard$31$StatusBar(PendingIntent pendingIntent, View view, Runnable runnable) {
        try {
            pendingIntent.send((Context) null, 0, (Intent) null, (PendingIntent.OnFinished) null, (Handler) null, (String) null, getActivityOptions(this.mActivityLaunchAnimator.getLaunchAnimation(view, isOccluded())));
        } catch (PendingIntent.CanceledException e) {
            Log.w("StatusBar", "Sending intent failed: " + e);
        }
        if (pendingIntent.isActivity()) {
            this.mAssistManagerLazy.get().hideAssist();
        }
        if (runnable != null) {
            postOnUiThread(runnable);
        }
    }

    private void postOnUiThread(Runnable runnable) {
        this.mMainThreadHandler.post(runnable);
    }

    public static Bundle getActivityOptions(RemoteAnimationAdapter remoteAnimationAdapter) {
        ActivityOptions activityOptions;
        if (remoteAnimationAdapter != null) {
            activityOptions = ActivityOptions.makeRemoteAnimation(remoteAnimationAdapter);
        } else {
            activityOptions = ActivityOptions.makeBasic();
        }
        activityOptions.setLaunchWindowingMode(4);
        return activityOptions.toBundle();
    }

    /* access modifiers changed from: package-private */
    public void visibilityChanged(boolean z) {
        if (this.mVisible != z) {
            this.mVisible = z;
            if (!z) {
                this.mGutsManager.closeAndSaveGuts(true, true, true, -1, -1, true);
            }
        }
        updateVisibleToUser();
    }

    /* access modifiers changed from: protected */
    public void updateVisibleToUser() {
        boolean z = this.mVisibleToUser;
        boolean z2 = this.mVisible && this.mDeviceInteractive;
        this.mVisibleToUser = z2;
        if (z != z2) {
            handleVisibleToUserChanged(z2);
        }
    }

    public void clearNotificationEffects() {
        try {
            this.mBarService.clearNotificationEffects();
        } catch (RemoteException unused) {
        }
    }

    /* access modifiers changed from: protected */
    public void notifyHeadsUpGoingToSleep() {
        maybeEscalateHeadsUp();
    }

    public boolean isBouncerShowing() {
        return this.mBouncerShowing;
    }

    public boolean isBouncerShowingScrimmed() {
        return isBouncerShowing() && this.mStatusBarKeyguardViewManager.bouncerNeedsScrimming();
    }

    public void onBouncerPreHideAnimation() {
        this.mNotificationPanelViewController.onBouncerPreHideAnimation();
        this.mLockscreenLockIconController.onBouncerPreHideAnimation();
    }

    public static PackageManager getPackageManagerForUser(Context context, int i) {
        if (i >= 0) {
            try {
                context = context.createPackageContextAsUser(context.getPackageName(), 4, new UserHandle(i));
            } catch (PackageManager.NameNotFoundException unused) {
            }
        }
        return context.getPackageManager();
    }

    public boolean isKeyguardSecure() {
        StatusBarKeyguardViewManager statusBarKeyguardViewManager = this.mStatusBarKeyguardViewManager;
        if (statusBarKeyguardViewManager != null) {
            return statusBarKeyguardViewManager.isSecure();
        }
        Slog.w("StatusBar", "isKeyguardSecure() called before startKeyguard(), returning false", new Throwable());
        return false;
    }

    public void showAssistDisclosure() {
        this.mAssistManagerLazy.get().showDisclosure();
    }

    public NotificationPanelViewController getPanelController() {
        return this.mNotificationPanelViewController;
    }

    public void startAssist(Bundle bundle) {
        this.mAssistManagerLazy.get().startAssist(bundle);
    }

    public NotificationGutsManager getGutsManager() {
        return this.mGutsManager;
    }

    /* access modifiers changed from: private */
    public boolean isTransientShown() {
        return this.mTransientShown;
    }

    public void suppressAmbientDisplay(boolean z) {
        this.mDozeServiceHost.setDozeSuppressed(z);
    }

    public int getKeyguardNotifications() {
        if (this.mKeyguardNotifications == -1) {
            this.mKeyguardNotifications = this.mNotificationPanelViewController.computeMaxKeyguardNotifications(this.mContext.getResources().getInteger(C0013R$integer.keyguard_max_notification_count));
        }
        return this.mKeyguardNotifications;
    }

    public boolean isQSFullyCollapsed() {
        return this.mNotificationPanelViewController.isFullyCollapsed();
    }

    public void collapsePanels() {
        makeExpandedInvisible();
    }

    public static boolean isBootCompleted() {
        return sBootCompleted;
    }

    /* access modifiers changed from: protected */
    public void onBootCompleted() {
        super.onBootCompleted();
        sBootCompleted = true;
    }
}
