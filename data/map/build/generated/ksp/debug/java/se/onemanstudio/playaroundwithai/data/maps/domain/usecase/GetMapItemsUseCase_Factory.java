package se.onemanstudio.playaroundwithai.data.maps.domain.usecase;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import se.onemanstudio.playaroundwithai.data.maps.domain.repository.MapPointsRepository;

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
public final class GetMapItemsUseCase_Factory implements Factory<GetMapItemsUseCase> {
  private final Provider<MapPointsRepository> repositoryProvider;

  private GetMapItemsUseCase_Factory(Provider<MapPointsRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetMapItemsUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetMapItemsUseCase_Factory create(
      Provider<MapPointsRepository> repositoryProvider) {
    return new GetMapItemsUseCase_Factory(repositoryProvider);
  }

  public static GetMapItemsUseCase newInstance(MapPointsRepository repository) {
    return new GetMapItemsUseCase(repository);
  }
}
