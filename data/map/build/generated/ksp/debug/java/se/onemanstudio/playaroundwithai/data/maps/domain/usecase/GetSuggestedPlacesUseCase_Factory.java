package se.onemanstudio.playaroundwithai.data.maps.domain.usecase;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import se.onemanstudio.playaroundwithai.data.maps.domain.repository.MapSuggestionsRepository;

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
public final class GetSuggestedPlacesUseCase_Factory implements Factory<GetSuggestedPlacesUseCase> {
  private final Provider<MapSuggestionsRepository> mapSuggestionsRepositoryProvider;

  private GetSuggestedPlacesUseCase_Factory(
      Provider<MapSuggestionsRepository> mapSuggestionsRepositoryProvider) {
    this.mapSuggestionsRepositoryProvider = mapSuggestionsRepositoryProvider;
  }

  @Override
  public GetSuggestedPlacesUseCase get() {
    return newInstance(mapSuggestionsRepositoryProvider.get());
  }

  public static GetSuggestedPlacesUseCase_Factory create(
      Provider<MapSuggestionsRepository> mapSuggestionsRepositoryProvider) {
    return new GetSuggestedPlacesUseCase_Factory(mapSuggestionsRepositoryProvider);
  }

  public static GetSuggestedPlacesUseCase newInstance(
      MapSuggestionsRepository mapSuggestionsRepository) {
    return new GetSuggestedPlacesUseCase(mapSuggestionsRepository);
  }
}
