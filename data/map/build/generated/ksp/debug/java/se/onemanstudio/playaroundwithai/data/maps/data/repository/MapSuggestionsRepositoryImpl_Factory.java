package se.onemanstudio.playaroundwithai.data.maps.data.repository;

import com.google.gson.Gson;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import se.onemanstudio.playaroundwithai.core.network.api.GeminiApiService;

@ScopeMetadata("javax.inject.Singleton")
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
public final class MapSuggestionsRepositoryImpl_Factory implements Factory<MapSuggestionsRepositoryImpl> {
  private final Provider<GeminiApiService> apiServiceProvider;

  private final Provider<Gson> gsonProvider;

  private MapSuggestionsRepositoryImpl_Factory(Provider<GeminiApiService> apiServiceProvider,
      Provider<Gson> gsonProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.gsonProvider = gsonProvider;
  }

  @Override
  public MapSuggestionsRepositoryImpl get() {
    return newInstance(apiServiceProvider.get(), gsonProvider.get());
  }

  public static MapSuggestionsRepositoryImpl_Factory create(
      Provider<GeminiApiService> apiServiceProvider, Provider<Gson> gsonProvider) {
    return new MapSuggestionsRepositoryImpl_Factory(apiServiceProvider, gsonProvider);
  }

  public static MapSuggestionsRepositoryImpl newInstance(GeminiApiService apiService, Gson gson) {
    return new MapSuggestionsRepositoryImpl(apiService, gson);
  }
}
