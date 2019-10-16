package passforslag;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.HttpStatus;

@Client(id="communication", path = "/mail")
public interface CommunicationsClient {

    @Post("/")
    HttpStatus sendEmail(@Body Email email);
}
