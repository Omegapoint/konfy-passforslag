package passforslag;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import javax.validation.constraints.Email;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Introspected
public class Session {

  @Email
  private final String email;

  @Size(min = 1, max = 64)
  private final String presenter;

  @Size(min = 1, max = 64)
  private final String title;

  @Size(min = 1, max = 256)
  private final String description;

  @Positive
  private final int lengthInMinutes;

  @JsonCreator
  public Session(@JsonProperty("email") String email,
                 @JsonProperty("presentator") String presenter,
                 @JsonProperty("passnamn") String title,
                 @JsonProperty("beskrivning") String description,
                 @JsonProperty("langd") int lengthInMinutes
  ) {
    this.email = email;
    this.presenter = presenter;
    this.title = title;
    this.description = description;
    this.lengthInMinutes = lengthInMinutes;
  }

  @JsonProperty("email")
  public String getEmail() {
    return email;
  }

  @JsonProperty("presentator")
  public String getPresenter() {
    return presenter;
  }

  @JsonProperty("passnamn")
  public String getTitle() {
    return title;
  }

  @JsonProperty("beskrivning")
  public String getDescription() {
    return description;
  }

  @JsonProperty("langd")
  public int getLengthInMinutes() {
    return lengthInMinutes;
  }
}
