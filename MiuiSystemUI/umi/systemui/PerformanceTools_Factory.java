package com.android.systemui;

import com.android.systemui.dump.DumpManager;
import dagger.internal.Factory;
import javax.inject.Provider;
import miui.systemui.performance.BinderMonitor;
import miui.systemui.performance.EvilMethodMonitor;
import miui.systemui.performance.MemoryMonitor;
import miui.systemui.performance.ViewLeakMonitor;

public final class PerformanceTools_Factory implements Factory<PerformanceTools> {
    private final Provider<BinderMonitor> binderMonitorProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<EvilMethodMonitor> evilMethodMonitorProvider;
    private final Provider<MemoryMonitor> memoryMonitorProvider;
    private final Provider<ViewLeakMonitor> viewLeakMonitorProvider;

    public PerformanceTools_Factory(Provider<ViewLeakMonitor> provider, Provider<MemoryMonitor> provider2, Provider<EvilMethodMonitor> provider3, Provider<BinderMonitor> provider4, Provider<DumpManager> provider5) {
        this.viewLeakMonitorProvider = provider;
        this.memoryMonitorProvider = provider2;
        this.evilMethodMonitorProvider = provider3;
        this.binderMonitorProvider = provider4;
        this.dumpManagerProvider = provider5;
    }

    @Override // javax.inject.Provider
    public PerformanceTools get() {
        return provideInstance(this.viewLeakMonitorProvider, this.memoryMonitorProvider, this.evilMethodMonitorProvider, this.binderMonitorProvider, this.dumpManagerProvider);
    }

    public static PerformanceTools provideInstance(Provider<ViewLeakMonitor> provider, Provider<MemoryMonitor> provider2, Provider<EvilMethodMonitor> provider3, Provider<BinderMonitor> provider4, Provider<DumpManager> provider5) {
        return new PerformanceTools(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static PerformanceTools_Factory create(Provider<ViewLeakMonitor> provider, Provider<MemoryMonitor> provider2, Provider<EvilMethodMonitor> provider3, Provider<BinderMonitor> provider4, Provider<DumpManager> provider5) {
        return new PerformanceTools_Factory(provider, provider2, provider3, provider4, provider5);
    }
}
