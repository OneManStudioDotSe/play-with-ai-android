package se.onemanstudio.playaroundwithai.data.maps.di;

import android.content.Context;
import android.net.ConnectivityManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class MapModule_Companion_ProvideConnectivityManagerFactory implements Factory<ConnectivityManager> {
  private final Provider<Context> contextProvider;

  private MapModule_Companion_ProvideConnectivityManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ConnectivityManager get() {
    return provideConnectivityManager(contextProvider.get());
  }

  public static MapModule_Companion_ProvideConnectivityManagerFactory create(
      Provider<Context> contextProvider) {
    return new MapModule_Companion_ProvideConnectivityManagerFactory(contextProvider);
  }

  public static ConnectivityManager provideConnectivityManager(Context context) {
    return Preconditions.checkNotNullFromProvides(MapModule.Companion.provideConnectivityManager(context));
  }
}
