package se.onemanstudio.playaroundwithai.data.maps.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import se.onemanstudio.playaroundwithai.data.maps.data.api.MapApiService;

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
public final class MapPointsRepositoryImpl_Factory implements Factory<MapPointsRepositoryImpl> {
  private final Provider<MapApiService> mapApiServiceProvider;

  private MapPointsRepositoryImpl_Factory(Provider<MapApiService> mapApiServiceProvider) {
    this.mapApiServiceProvider = mapApiServiceProvider;
  }

  @Override
  public MapPointsRepositoryImpl get() {
    return newInstance(mapApiServiceProvider.get());
  }

  public static MapPointsRepositoryImpl_Factory create(
      Provider<MapApiService> mapApiServiceProvider) {
    return new MapPointsRepositoryImpl_Factory(mapApiServiceProvider);
  }

  public static MapPointsRepositoryImpl newInstance(MapApiService mapApiService) {
    return new MapPointsRepositoryImpl(mapApiService);
  }
}
