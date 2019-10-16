package passforslag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.UUID;

class Persisted<T> {
  private final UUID id;
  private final T obj;

  Persisted(UUID id, T obj) {
    this.id = id;
    this.obj = obj;
  }

  @JsonProperty("id")
  UUID getId() {
    return id;
  }

  @JsonUnwrapped
  T getObj() {
    return obj;
  }
}
