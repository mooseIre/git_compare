package com.android.systemui.statusbar.phone.dagger;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiDozeServiceHost;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.InitController;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.keyguard.DismissCallbackRegistry;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.PluginDependencyProvider;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.ScreenPinningRequest;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.NotificationShadeDepthController;
import com.android.systemui.statusbar.NotificationViewHierarchyManager;
import com.android.systemui.statusbar.PulseExpansionHandler;
import com.android.systemui.statusbar.SuperStatusBarViewFactory;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.VibratorHelper;
import com.android.systemui.statusbar.notification.DynamicPrivacyController;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.init.NotificationsController;
import com.android.systemui.statusbar.notification.interruption.BypassHeadsUpNotifier;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.row.NotificationGutsManager;
import com.android.systemui.statusbar.phone.AutoHideController;
import com.android.systemui.statusbar.phone.BiometricUnlockController;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.phone.DozeScrimController;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.KeyguardDismissUtil;
import com.android.systemui.statusbar.phone.KeyguardLiftController;
import com.android.systemui.statusbar.phone.LightBarController;
import com.android.systemui.statusbar.phone.LightsOutNotifController;
import com.android.systemui.statusbar.phone.LockscreenLockIconController;
import com.android.systemui.statusbar.phone.LockscreenWallpaper;
import com.android.systemui.statusbar.phone.MiuiPhoneStatusBarPolicy;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.ShadeController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter;
import com.android.systemui.statusbar.phone.StatusBarTouchableRegionManager;
import com.android.systemui.statusbar.phone.dagger.StatusBarComponent;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.ExtensionController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.RemoteInputQuickSettingsDisabler;
import com.android.systemui.statusbar.policy.UserInfoControllerImpl;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.volume.VolumeComponent;
import dagger.Lazy;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import java.util.Optional;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class StatusBarPhoneModule_ProvideStatusBarFactory implements Factory<StatusBar> {
    private final Provider<AssistManager> assistManagerLazyProvider;
    private final Provider<AutoHideController> autoHideControllerProvider;
    private final Provider<BatteryController> batteryControllerProvider;
    private final Provider<BiometricUnlockController> biometricUnlockControllerLazyProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<BubbleController> bubbleControllerProvider;
    private final Provider<BypassHeadsUpNotifier> bypassHeadsUpNotifierProvider;
    private final Provider<SysuiColorExtractor> colorExtractorProvider;
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<ConfigurationController> configurationControllerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<ControlPanelController> controlPanelControllerProvider;
    private final Provider<DarkIconDispatcher> darkIconDispatcherProvider;
    private final Provider<DeviceProvisionedController> deviceProvisionedControllerProvider;
    private final Provider<DismissCallbackRegistry> dismissCallbackRegistryProvider;
    private final Provider<DisplayMetrics> displayMetricsProvider;
    private final Provider<Optional<Divider>> dividerOptionalProvider;
    private final Provider<DozeParameters> dozeParametersProvider;
    private final Provider<DozeScrimController> dozeScrimControllerProvider;
    private final Provider<MiuiDozeServiceHost> dozeServiceHostProvider;
    private final Provider<DynamicPrivacyController> dynamicPrivacyControllerProvider;
    private final Provider<ExtensionController> extensionControllerProvider;
    private final Provider<FalsingManager> falsingManagerProvider;
    private final Provider<NotificationGroupManager> groupManagerProvider;
    private final Provider<HeadsUpManagerPhone> headsUpManagerPhoneProvider;
    private final Provider<InitController> initControllerProvider;
    private final Provider<KeyguardBypassController> keyguardBypassControllerProvider;
    private final Provider<KeyguardDismissUtil> keyguardDismissUtilProvider;
    private final Provider<KeyguardIndicationController> keyguardIndicationControllerProvider;
    private final Provider<KeyguardLiftController> keyguardLiftControllerProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<KeyguardUpdateMonitor> keyguardUpdateMonitorProvider;
    private final Provider<KeyguardViewMediator> keyguardViewMediatorProvider;
    private final Provider<LightBarController> lightBarControllerProvider;
    private final Provider<LightsOutNotifController> lightsOutNotifControllerProvider;
    private final Provider<NotificationLockscreenUserManager> lockScreenUserManagerProvider;
    private final Provider<LockscreenLockIconController> lockscreenLockIconControllerProvider;
    private final Provider<LockscreenWallpaper> lockscreenWallpaperLazyProvider;
    private final Provider<MetricsLogger> metricsLoggerProvider;
    private final Provider<MiuiPhoneStatusBarPolicy> miuiPhoneStatusBarPolicyProvider;
    private final Provider<NavigationBarController> navigationBarControllerProvider;
    private final Provider<NetworkController> networkControllerProvider;
    private final Provider<NotificationGutsManager> notificationGutsManagerProvider;
    private final Provider<NotificationInterruptStateProvider> notificationInterruptStateProvider;
    private final Provider<NotificationLogger> notificationLoggerProvider;
    private final Provider<NotificationMediaManager> notificationMediaManagerProvider;
    private final Provider<NotificationShadeDepthController> notificationShadeDepthControllerProvider;
    private final Provider<NotificationShadeWindowController> notificationShadeWindowControllerProvider;
    private final Provider<NotificationViewHierarchyManager> notificationViewHierarchyManagerProvider;
    private final Provider<NotificationWakeUpCoordinator> notificationWakeUpCoordinatorProvider;
    private final Provider<NotificationsController> notificationsControllerProvider;
    private final Provider<PluginDependencyProvider> pluginDependencyProvider;
    private final Provider<PluginManager> pluginManagerProvider;
    private final Provider<PowerManager> powerManagerProvider;
    private final Provider<PulseExpansionHandler> pulseExpansionHandlerProvider;
    private final Provider<Optional<Recents>> recentsOptionalProvider;
    private final Provider<NotificationRemoteInputManager> remoteInputManagerProvider;
    private final Provider<RemoteInputQuickSettingsDisabler> remoteInputQuickSettingsDisablerProvider;
    private final Provider<ScreenLifecycle> screenLifecycleProvider;
    private final Provider<ScreenPinningRequest> screenPinningRequestProvider;
    private final Provider<ScrimController> scrimControllerProvider;
    private final Provider<ShadeController> shadeControllerProvider;
    private final Provider<StatusBarComponent.Builder> statusBarComponentBuilderProvider;
    private final Provider<StatusBarIconController> statusBarIconControllerProvider;
    private final Provider<StatusBarKeyguardViewManager> statusBarKeyguardViewManagerProvider;
    private final Provider<StatusBarNotificationActivityStarter.Builder> statusBarNotificationActivityStarterBuilderProvider;
    private final Provider<SysuiStatusBarStateController> statusBarStateControllerProvider;
    private final Provider<StatusBarTouchableRegionManager> statusBarTouchableRegionManagerProvider;
    private final Provider<SuperStatusBarViewFactory> superStatusBarViewFactoryProvider;
    private final Provider<Handler> timeTickHandlerProvider;
    private final Provider<Executor> uiBgExecutorProvider;
    private final Provider<UserInfoControllerImpl> userInfoControllerImplProvider;
    private final Provider<UserSwitcherController> userSwitcherControllerProvider;
    private final Provider<VibratorHelper> vibratorHelperProvider;
    private final Provider<ViewMediatorCallback> viewMediatorCallbackProvider;
    private final Provider<VisualStabilityManager> visualStabilityManagerProvider;
    private final Provider<VolumeComponent> volumeComponentProvider;
    private final Provider<WakefulnessLifecycle> wakefulnessLifecycleProvider;

    public StatusBarPhoneModule_ProvideStatusBarFactory(Provider<Context> provider, Provider<NotificationsController> provider2, Provider<LightBarController> provider3, Provider<AutoHideController> provider4, Provider<KeyguardUpdateMonitor> provider5, Provider<StatusBarIconController> provider6, Provider<PulseExpansionHandler> provider7, Provider<NotificationWakeUpCoordinator> provider8, Provider<KeyguardBypassController> provider9, Provider<KeyguardStateController> provider10, Provider<HeadsUpManagerPhone> provider11, Provider<DynamicPrivacyController> provider12, Provider<BypassHeadsUpNotifier> provider13, Provider<FalsingManager> provider14, Provider<BroadcastDispatcher> provider15, Provider<RemoteInputQuickSettingsDisabler> provider16, Provider<NotificationGutsManager> provider17, Provider<NotificationLogger> provider18, Provider<NotificationInterruptStateProvider> provider19, Provider<NotificationViewHierarchyManager> provider20, Provider<KeyguardViewMediator> provider21, Provider<DisplayMetrics> provider22, Provider<MetricsLogger> provider23, Provider<Executor> provider24, Provider<NotificationMediaManager> provider25, Provider<NotificationLockscreenUserManager> provider26, Provider<NotificationRemoteInputManager> provider27, Provider<UserSwitcherController> provider28, Provider<NetworkController> provider29, Provider<BatteryController> provider30, Provider<SysuiColorExtractor> provider31, Provider<ScreenLifecycle> provider32, Provider<WakefulnessLifecycle> provider33, Provider<SysuiStatusBarStateController> provider34, Provider<VibratorHelper> provider35, Provider<BubbleController> provider36, Provider<NotificationGroupManager> provider37, Provider<VisualStabilityManager> provider38, Provider<DeviceProvisionedController> provider39, Provider<NavigationBarController> provider40, Provider<AssistManager> provider41, Provider<ConfigurationController> provider42, Provider<NotificationShadeWindowController> provider43, Provider<LockscreenLockIconController> provider44, Provider<DozeParameters> provider45, Provider<ScrimController> provider46, Provider<KeyguardLiftController> provider47, Provider<LockscreenWallpaper> provider48, Provider<BiometricUnlockController> provider49, Provider<MiuiDozeServiceHost> provider50, Provider<PowerManager> provider51, Provider<ScreenPinningRequest> provider52, Provider<DozeScrimController> provider53, Provider<VolumeComponent> provider54, Provider<CommandQueue> provider55, Provider<Optional<Recents>> provider56, Provider<StatusBarComponent.Builder> provider57, Provider<PluginManager> provider58, Provider<Optional<Divider>> provider59, Provider<LightsOutNotifController> provider60, Provider<StatusBarNotificationActivityStarter.Builder> provider61, Provider<ShadeController> provider62, Provider<SuperStatusBarViewFactory> provider63, Provider<StatusBarKeyguardViewManager> provider64, Provider<ViewMediatorCallback> provider65, Provider<InitController> provider66, Provider<DarkIconDispatcher> provider67, Provider<Handler> provider68, Provider<PluginDependencyProvider> provider69, Provider<KeyguardDismissUtil> provider70, Provider<ExtensionController> provider71, Provider<UserInfoControllerImpl> provider72, Provider<MiuiPhoneStatusBarPolicy> provider73, Provider<KeyguardIndicationController> provider74, Provider<NotificationShadeDepthController> provider75, Provider<DismissCallbackRegistry> provider76, Provider<StatusBarTouchableRegionManager> provider77, Provider<ControlPanelController> provider78) {
        this.contextProvider = provider;
        this.notificationsControllerProvider = provider2;
        this.lightBarControllerProvider = provider3;
        this.autoHideControllerProvider = provider4;
        this.keyguardUpdateMonitorProvider = provider5;
        this.statusBarIconControllerProvider = provider6;
        this.pulseExpansionHandlerProvider = provider7;
        this.notificationWakeUpCoordinatorProvider = provider8;
        this.keyguardBypassControllerProvider = provider9;
        this.keyguardStateControllerProvider = provider10;
        this.headsUpManagerPhoneProvider = provider11;
        this.dynamicPrivacyControllerProvider = provider12;
        this.bypassHeadsUpNotifierProvider = provider13;
        this.falsingManagerProvider = provider14;
        this.broadcastDispatcherProvider = provider15;
        this.remoteInputQuickSettingsDisablerProvider = provider16;
        this.notificationGutsManagerProvider = provider17;
        this.notificationLoggerProvider = provider18;
        this.notificationInterruptStateProvider = provider19;
        this.notificationViewHierarchyManagerProvider = provider20;
        this.keyguardViewMediatorProvider = provider21;
        this.displayMetricsProvider = provider22;
        this.metricsLoggerProvider = provider23;
        this.uiBgExecutorProvider = provider24;
        this.notificationMediaManagerProvider = provider25;
        this.lockScreenUserManagerProvider = provider26;
        this.remoteInputManagerProvider = provider27;
        this.userSwitcherControllerProvider = provider28;
        this.networkControllerProvider = provider29;
        this.batteryControllerProvider = provider30;
        this.colorExtractorProvider = provider31;
        this.screenLifecycleProvider = provider32;
        this.wakefulnessLifecycleProvider = provider33;
        this.statusBarStateControllerProvider = provider34;
        this.vibratorHelperProvider = provider35;
        this.bubbleControllerProvider = provider36;
        this.groupManagerProvider = provider37;
        this.visualStabilityManagerProvider = provider38;
        this.deviceProvisionedControllerProvider = provider39;
        this.navigationBarControllerProvider = provider40;
        this.assistManagerLazyProvider = provider41;
        this.configurationControllerProvider = provider42;
        this.notificationShadeWindowControllerProvider = provider43;
        this.lockscreenLockIconControllerProvider = provider44;
        this.dozeParametersProvider = provider45;
        this.scrimControllerProvider = provider46;
        this.keyguardLiftControllerProvider = provider47;
        this.lockscreenWallpaperLazyProvider = provider48;
        this.biometricUnlockControllerLazyProvider = provider49;
        this.dozeServiceHostProvider = provider50;
        this.powerManagerProvider = provider51;
        this.screenPinningRequestProvider = provider52;
        this.dozeScrimControllerProvider = provider53;
        this.volumeComponentProvider = provider54;
        this.commandQueueProvider = provider55;
        this.recentsOptionalProvider = provider56;
        this.statusBarComponentBuilderProvider = provider57;
        this.pluginManagerProvider = provider58;
        this.dividerOptionalProvider = provider59;
        this.lightsOutNotifControllerProvider = provider60;
        this.statusBarNotificationActivityStarterBuilderProvider = provider61;
        this.shadeControllerProvider = provider62;
        this.superStatusBarViewFactoryProvider = provider63;
        this.statusBarKeyguardViewManagerProvider = provider64;
        this.viewMediatorCallbackProvider = provider65;
        this.initControllerProvider = provider66;
        this.darkIconDispatcherProvider = provider67;
        this.timeTickHandlerProvider = provider68;
        this.pluginDependencyProvider = provider69;
        this.keyguardDismissUtilProvider = provider70;
        this.extensionControllerProvider = provider71;
        this.userInfoControllerImplProvider = provider72;
        this.miuiPhoneStatusBarPolicyProvider = provider73;
        this.keyguardIndicationControllerProvider = provider74;
        this.notificationShadeDepthControllerProvider = provider75;
        this.dismissCallbackRegistryProvider = provider76;
        this.statusBarTouchableRegionManagerProvider = provider77;
        this.controlPanelControllerProvider = provider78;
    }

    @Override // javax.inject.Provider
    public StatusBar get() {
        return provideInstance(this.contextProvider, this.notificationsControllerProvider, this.lightBarControllerProvider, this.autoHideControllerProvider, this.keyguardUpdateMonitorProvider, this.statusBarIconControllerProvider, this.pulseExpansionHandlerProvider, this.notificationWakeUpCoordinatorProvider, this.keyguardBypassControllerProvider, this.keyguardStateControllerProvider, this.headsUpManagerPhoneProvider, this.dynamicPrivacyControllerProvider, this.bypassHeadsUpNotifierProvider, this.falsingManagerProvider, this.broadcastDispatcherProvider, this.remoteInputQuickSettingsDisablerProvider, this.notificationGutsManagerProvider, this.notificationLoggerProvider, this.notificationInterruptStateProvider, this.notificationViewHierarchyManagerProvider, this.keyguardViewMediatorProvider, this.displayMetricsProvider, this.metricsLoggerProvider, this.uiBgExecutorProvider, this.notificationMediaManagerProvider, this.lockScreenUserManagerProvider, this.remoteInputManagerProvider, this.userSwitcherControllerProvider, this.networkControllerProvider, this.batteryControllerProvider, this.colorExtractorProvider, this.screenLifecycleProvider, this.wakefulnessLifecycleProvider, this.statusBarStateControllerProvider, this.vibratorHelperProvider, this.bubbleControllerProvider, this.groupManagerProvider, this.visualStabilityManagerProvider, this.deviceProvisionedControllerProvider, this.navigationBarControllerProvider, this.assistManagerLazyProvider, this.configurationControllerProvider, this.notificationShadeWindowControllerProvider, this.lockscreenLockIconControllerProvider, this.dozeParametersProvider, this.scrimControllerProvider, this.keyguardLiftControllerProvider, this.lockscreenWallpaperLazyProvider, this.biometricUnlockControllerLazyProvider, this.dozeServiceHostProvider, this.powerManagerProvider, this.screenPinningRequestProvider, this.dozeScrimControllerProvider, this.volumeComponentProvider, this.commandQueueProvider, this.recentsOptionalProvider, this.statusBarComponentBuilderProvider, this.pluginManagerProvider, this.dividerOptionalProvider, this.lightsOutNotifControllerProvider, this.statusBarNotificationActivityStarterBuilderProvider, this.shadeControllerProvider, this.superStatusBarViewFactoryProvider, this.statusBarKeyguardViewManagerProvider, this.viewMediatorCallbackProvider, this.initControllerProvider, this.darkIconDispatcherProvider, this.timeTickHandlerProvider, this.pluginDependencyProvider, this.keyguardDismissUtilProvider, this.extensionControllerProvider, this.userInfoControllerImplProvider, this.miuiPhoneStatusBarPolicyProvider, this.keyguardIndicationControllerProvider, this.notificationShadeDepthControllerProvider, this.dismissCallbackRegistryProvider, this.statusBarTouchableRegionManagerProvider, this.controlPanelControllerProvider);
    }

    public static StatusBar provideInstance(Provider<Context> provider, Provider<NotificationsController> provider2, Provider<LightBarController> provider3, Provider<AutoHideController> provider4, Provider<KeyguardUpdateMonitor> provider5, Provider<StatusBarIconController> provider6, Provider<PulseExpansionHandler> provider7, Provider<NotificationWakeUpCoordinator> provider8, Provider<KeyguardBypassController> provider9, Provider<KeyguardStateController> provider10, Provider<HeadsUpManagerPhone> provider11, Provider<DynamicPrivacyController> provider12, Provider<BypassHeadsUpNotifier> provider13, Provider<FalsingManager> provider14, Provider<BroadcastDispatcher> provider15, Provider<RemoteInputQuickSettingsDisabler> provider16, Provider<NotificationGutsManager> provider17, Provider<NotificationLogger> provider18, Provider<NotificationInterruptStateProvider> provider19, Provider<NotificationViewHierarchyManager> provider20, Provider<KeyguardViewMediator> provider21, Provider<DisplayMetrics> provider22, Provider<MetricsLogger> provider23, Provider<Executor> provider24, Provider<NotificationMediaManager> provider25, Provider<NotificationLockscreenUserManager> provider26, Provider<NotificationRemoteInputManager> provider27, Provider<UserSwitcherController> provider28, Provider<NetworkController> provider29, Provider<BatteryController> provider30, Provider<SysuiColorExtractor> provider31, Provider<ScreenLifecycle> provider32, Provider<WakefulnessLifecycle> provider33, Provider<SysuiStatusBarStateController> provider34, Provider<VibratorHelper> provider35, Provider<BubbleController> provider36, Provider<NotificationGroupManager> provider37, Provider<VisualStabilityManager> provider38, Provider<DeviceProvisionedController> provider39, Provider<NavigationBarController> provider40, Provider<AssistManager> provider41, Provider<ConfigurationController> provider42, Provider<NotificationShadeWindowController> provider43, Provider<LockscreenLockIconController> provider44, Provider<DozeParameters> provider45, Provider<ScrimController> provider46, Provider<KeyguardLiftController> provider47, Provider<LockscreenWallpaper> provider48, Provider<BiometricUnlockController> provider49, Provider<MiuiDozeServiceHost> provider50, Provider<PowerManager> provider51, Provider<ScreenPinningRequest> provider52, Provider<DozeScrimController> provider53, Provider<VolumeComponent> provider54, Provider<CommandQueue> provider55, Provider<Optional<Recents>> provider56, Provider<StatusBarComponent.Builder> provider57, Provider<PluginManager> provider58, Provider<Optional<Divider>> provider59, Provider<LightsOutNotifController> provider60, Provider<StatusBarNotificationActivityStarter.Builder> provider61, Provider<ShadeController> provider62, Provider<SuperStatusBarViewFactory> provider63, Provider<StatusBarKeyguardViewManager> provider64, Provider<ViewMediatorCallback> provider65, Provider<InitController> provider66, Provider<DarkIconDispatcher> provider67, Provider<Handler> provider68, Provider<PluginDependencyProvider> provider69, Provider<KeyguardDismissUtil> provider70, Provider<ExtensionController> provider71, Provider<UserInfoControllerImpl> provider72, Provider<MiuiPhoneStatusBarPolicy> provider73, Provider<KeyguardIndicationController> provider74, Provider<NotificationShadeDepthController> provider75, Provider<DismissCallbackRegistry> provider76, Provider<StatusBarTouchableRegionManager> provider77, Provider<ControlPanelController> provider78) {
        return proxyProvideStatusBar(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get(), provider12.get(), provider13.get(), provider14.get(), provider15.get(), provider16.get(), provider17.get(), provider18.get(), provider19.get(), provider20.get(), provider21.get(), provider22.get(), provider23.get(), provider24.get(), provider25.get(), provider26.get(), provider27.get(), provider28.get(), provider29.get(), provider30.get(), provider31.get(), provider32.get(), provider33.get(), provider34.get(), provider35.get(), provider36.get(), provider37.get(), provider38.get(), provider39.get(), provider40.get(), DoubleCheck.lazy(provider41), provider42.get(), provider43.get(), provider44.get(), provider45.get(), provider46.get(), provider47.get(), DoubleCheck.lazy(provider48), DoubleCheck.lazy(provider49), provider50.get(), provider51.get(), provider52.get(), provider53.get(), provider54.get(), provider55.get(), provider56.get(), provider57, provider58.get(), provider59.get(), provider60.get(), provider61.get(), provider62.get(), provider63.get(), provider64.get(), provider65.get(), provider66.get(), provider67.get(), provider68.get(), provider69.get(), provider70.get(), provider71.get(), provider72.get(), provider73.get(), provider74.get(), DoubleCheck.lazy(provider75), provider76.get(), provider77.get(), provider78.get());
    }

    public static StatusBarPhoneModule_ProvideStatusBarFactory create(Provider<Context> provider, Provider<NotificationsController> provider2, Provider<LightBarController> provider3, Provider<AutoHideController> provider4, Provider<KeyguardUpdateMonitor> provider5, Provider<StatusBarIconController> provider6, Provider<PulseExpansionHandler> provider7, Provider<NotificationWakeUpCoordinator> provider8, Provider<KeyguardBypassController> provider9, Provider<KeyguardStateController> provider10, Provider<HeadsUpManagerPhone> provider11, Provider<DynamicPrivacyController> provider12, Provider<BypassHeadsUpNotifier> provider13, Provider<FalsingManager> provider14, Provider<BroadcastDispatcher> provider15, Provider<RemoteInputQuickSettingsDisabler> provider16, Provider<NotificationGutsManager> provider17, Provider<NotificationLogger> provider18, Provider<NotificationInterruptStateProvider> provider19, Provider<NotificationViewHierarchyManager> provider20, Provider<KeyguardViewMediator> provider21, Provider<DisplayMetrics> provider22, Provider<MetricsLogger> provider23, Provider<Executor> provider24, Provider<NotificationMediaManager> provider25, Provider<NotificationLockscreenUserManager> provider26, Provider<NotificationRemoteInputManager> provider27, Provider<UserSwitcherController> provider28, Provider<NetworkController> provider29, Provider<BatteryController> provider30, Provider<SysuiColorExtractor> provider31, Provider<ScreenLifecycle> provider32, Provider<WakefulnessLifecycle> provider33, Provider<SysuiStatusBarStateController> provider34, Provider<VibratorHelper> provider35, Provider<BubbleController> provider36, Provider<NotificationGroupManager> provider37, Provider<VisualStabilityManager> provider38, Provider<DeviceProvisionedController> provider39, Provider<NavigationBarController> provider40, Provider<AssistManager> provider41, Provider<ConfigurationController> provider42, Provider<NotificationShadeWindowController> provider43, Provider<LockscreenLockIconController> provider44, Provider<DozeParameters> provider45, Provider<ScrimController> provider46, Provider<KeyguardLiftController> provider47, Provider<LockscreenWallpaper> provider48, Provider<BiometricUnlockController> provider49, Provider<MiuiDozeServiceHost> provider50, Provider<PowerManager> provider51, Provider<ScreenPinningRequest> provider52, Provider<DozeScrimController> provider53, Provider<VolumeComponent> provider54, Provider<CommandQueue> provider55, Provider<Optional<Recents>> provider56, Provider<StatusBarComponent.Builder> provider57, Provider<PluginManager> provider58, Provider<Optional<Divider>> provider59, Provider<LightsOutNotifController> provider60, Provider<StatusBarNotificationActivityStarter.Builder> provider61, Provider<ShadeController> provider62, Provider<SuperStatusBarViewFactory> provider63, Provider<StatusBarKeyguardViewManager> provider64, Provider<ViewMediatorCallback> provider65, Provider<InitController> provider66, Provider<DarkIconDispatcher> provider67, Provider<Handler> provider68, Provider<PluginDependencyProvider> provider69, Provider<KeyguardDismissUtil> provider70, Provider<ExtensionController> provider71, Provider<UserInfoControllerImpl> provider72, Provider<MiuiPhoneStatusBarPolicy> provider73, Provider<KeyguardIndicationController> provider74, Provider<NotificationShadeDepthController> provider75, Provider<DismissCallbackRegistry> provider76, Provider<StatusBarTouchableRegionManager> provider77, Provider<ControlPanelController> provider78) {
        return new StatusBarPhoneModule_ProvideStatusBarFactory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15, provider16, provider17, provider18, provider19, provider20, provider21, provider22, provider23, provider24, provider25, provider26, provider27, provider28, provider29, provider30, provider31, provider32, provider33, provider34, provider35, provider36, provider37, provider38, provider39, provider40, provider41, provider42, provider43, provider44, provider45, provider46, provider47, provider48, provider49, provider50, provider51, provider52, provider53, provider54, provider55, provider56, provider57, provider58, provider59, provider60, provider61, provider62, provider63, provider64, provider65, provider66, provider67, provider68, provider69, provider70, provider71, provider72, provider73, provider74, provider75, provider76, provider77, provider78);
    }

    public static StatusBar proxyProvideStatusBar(Context context, NotificationsController notificationsController, LightBarController lightBarController, AutoHideController autoHideController, KeyguardUpdateMonitor keyguardUpdateMonitor, StatusBarIconController statusBarIconController, PulseExpansionHandler pulseExpansionHandler, NotificationWakeUpCoordinator notificationWakeUpCoordinator, KeyguardBypassController keyguardBypassController, KeyguardStateController keyguardStateController, HeadsUpManagerPhone headsUpManagerPhone, DynamicPrivacyController dynamicPrivacyController, BypassHeadsUpNotifier bypassHeadsUpNotifier, FalsingManager falsingManager, BroadcastDispatcher broadcastDispatcher, RemoteInputQuickSettingsDisabler remoteInputQuickSettingsDisabler, NotificationGutsManager notificationGutsManager, NotificationLogger notificationLogger, NotificationInterruptStateProvider notificationInterruptStateProvider2, NotificationViewHierarchyManager notificationViewHierarchyManager, KeyguardViewMediator keyguardViewMediator, DisplayMetrics displayMetrics, MetricsLogger metricsLogger, Executor executor, NotificationMediaManager notificationMediaManager, NotificationLockscreenUserManager notificationLockscreenUserManager, NotificationRemoteInputManager notificationRemoteInputManager, UserSwitcherController userSwitcherController, NetworkController networkController, BatteryController batteryController, SysuiColorExtractor sysuiColorExtractor, ScreenLifecycle screenLifecycle, WakefulnessLifecycle wakefulnessLifecycle, SysuiStatusBarStateController sysuiStatusBarStateController, VibratorHelper vibratorHelper, BubbleController bubbleController, NotificationGroupManager notificationGroupManager, VisualStabilityManager visualStabilityManager, DeviceProvisionedController deviceProvisionedController, NavigationBarController navigationBarController, Lazy<AssistManager> lazy, ConfigurationController configurationController, NotificationShadeWindowController notificationShadeWindowController, LockscreenLockIconController lockscreenLockIconController, DozeParameters dozeParameters, ScrimController scrimController, KeyguardLiftController keyguardLiftController, Lazy<LockscreenWallpaper> lazy2, Lazy<BiometricUnlockController> lazy3, MiuiDozeServiceHost miuiDozeServiceHost, PowerManager powerManager, ScreenPinningRequest screenPinningRequest, DozeScrimController dozeScrimController, VolumeComponent volumeComponent, CommandQueue commandQueue, Optional<Recents> optional, Provider<StatusBarComponent.Builder> provider, PluginManager pluginManager, Optional<Divider> optional2, LightsOutNotifController lightsOutNotifController, StatusBarNotificationActivityStarter.Builder builder, ShadeController shadeController, SuperStatusBarViewFactory superStatusBarViewFactory, StatusBarKeyguardViewManager statusBarKeyguardViewManager, ViewMediatorCallback viewMediatorCallback, InitController initController, DarkIconDispatcher darkIconDispatcher, Handler handler, PluginDependencyProvider pluginDependencyProvider2, KeyguardDismissUtil keyguardDismissUtil, ExtensionController extensionController, UserInfoControllerImpl userInfoControllerImpl, MiuiPhoneStatusBarPolicy miuiPhoneStatusBarPolicy, KeyguardIndicationController keyguardIndicationController, Lazy<NotificationShadeDepthController> lazy4, DismissCallbackRegistry dismissCallbackRegistry, StatusBarTouchableRegionManager statusBarTouchableRegionManager, ControlPanelController controlPanelController) {
        StatusBar provideStatusBar = StatusBarPhoneModule.provideStatusBar(context, notificationsController, lightBarController, autoHideController, keyguardUpdateMonitor, statusBarIconController, pulseExpansionHandler, notificationWakeUpCoordinator, keyguardBypassController, keyguardStateController, headsUpManagerPhone, dynamicPrivacyController, bypassHeadsUpNotifier, falsingManager, broadcastDispatcher, remoteInputQuickSettingsDisabler, notificationGutsManager, notificationLogger, notificationInterruptStateProvider2, notificationViewHierarchyManager, keyguardViewMediator, displayMetrics, metricsLogger, executor, notificationMediaManager, notificationLockscreenUserManager, notificationRemoteInputManager, userSwitcherController, networkController, batteryController, sysuiColorExtractor, screenLifecycle, wakefulnessLifecycle, sysuiStatusBarStateController, vibratorHelper, bubbleController, notificationGroupManager, visualStabilityManager, deviceProvisionedController, navigationBarController, lazy, configurationController, notificationShadeWindowController, lockscreenLockIconController, dozeParameters, scrimController, keyguardLiftController, lazy2, lazy3, miuiDozeServiceHost, powerManager, screenPinningRequest, dozeScrimController, volumeComponent, commandQueue, optional, provider, pluginManager, optional2, lightsOutNotifController, builder, shadeController, superStatusBarViewFactory, statusBarKeyguardViewManager, viewMediatorCallback, initController, darkIconDispatcher, handler, pluginDependencyProvider2, keyguardDismissUtil, extensionController, userInfoControllerImpl, miuiPhoneStatusBarPolicy, keyguardIndicationController, lazy4, dismissCallbackRegistry, statusBarTouchableRegionManager, controlPanelController);
        Preconditions.checkNotNull(provideStatusBar, "Cannot return null from a non-@Nullable @Provides method");
        return provideStatusBar;
    }
}
