package se.onemanstudio.playaroundwithai.data.maps.data.api;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class FakeMapApiService_Factory implements Factory<FakeMapApiService> {
  @Override
  public FakeMapApiService get() {
    return newInstance();
  }

  public static FakeMapApiService_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FakeMapApiService newInstance() {
    return new FakeMapApiService();
  }

  private static final class InstanceHolder {
    static final FakeMapApiService_Factory INSTANCE = new FakeMapApiService_Factory();
  }
}
