package se.onemanstudio.playaroundwithai.data.maps.util;

import android.net.ConnectivityManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class ConnectivityNetworkMonitor_Factory implements Factory<ConnectivityNetworkMonitor> {
  private final Provider<ConnectivityManager> connectivityManagerProvider;

  private ConnectivityNetworkMonitor_Factory(
      Provider<ConnectivityManager> connectivityManagerProvider) {
    this.connectivityManagerProvider = connectivityManagerProvider;
  }

  @Override
  public ConnectivityNetworkMonitor get() {
    return newInstance(connectivityManagerProvider.get());
  }

  public static ConnectivityNetworkMonitor_Factory create(
      Provider<ConnectivityManager> connectivityManagerProvider) {
    return new ConnectivityNetworkMonitor_Factory(connectivityManagerProvider);
  }

  public static ConnectivityNetworkMonitor newInstance(ConnectivityManager connectivityManager) {
    return new ConnectivityNetworkMonitor(connectivityManager);
  }
}
