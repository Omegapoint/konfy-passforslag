package passforslag;

import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.discovery.exceptions.NoAvailableServiceException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.Post;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.validation.Valid;
import javax.validation.ValidationException;

@Controller("/pass")
public class SessionController {

  private CommunicationsClient comClient;
  private static Map<UUID, Persisted<Session>> allSessions = new HashMap<>();
  private static Map<UUID, Persisted<Session>> acceptedSessions = new HashMap<>();

  static {
    UUID id = UUID.fromString("deadbeef-0401-4958-ae1b-365ca965b36d");
    Session session =
        new Session(
            "bill@unknown.org",
            "Test Person",
            "Coola tekniker",
            "Detta är ett skitcoolt pass om bitcoins, blockchains, och annat hot",
            60
        );
    allSessions.put(id, new Persisted<>(id, session));
  }

  SessionController(CommunicationsClient comClient) {
    this.comClient = comClient;
  }

  @Get
  public HttpResponse<Collection<Persisted<Session>>> getAllSessionsRequest() {
    return HttpResponse.ok(allSessions.values());
  }

  @Post
  public CompletableFuture<HttpResponse<Persisted<Session>>> postSessionRequest(
      @Valid @Body CompletableFuture<Session> session) {
    return session
        .thenApply(this::persist)
        .thenApply(this::sendCreatedEmail)
        .thenApply(HttpResponse::created);
  }

  @Get("/{id}")
  public HttpResponse<Persisted<Session>> getSessionRequest(UUID id) {
    return HttpResponse.ok(getPersistedSession(id));
  }

  @Patch("/{id}/accept")
  public CompletableFuture<HttpResponse<Persisted<Session>>> acceptSessionRequest(
      UUID id) {
    return CompletableFuture.supplyAsync(() -> id)
        .thenApply(this::getPersistedSession)
        .thenApply(this::persistAcceptance)
        .thenApply(this::sendAcceptedEmail)
        .thenApply(HttpResponse::ok);
  }

  @Get("/accepterade")
  public HttpResponse<Collection<Persisted<Session>>> getAllAcceptedSessionsRequest() {
    return HttpResponse.ok(acceptedSessions.values());
  }

  @Error
  public HttpResponse<String> error(HttpRequest r, Throwable e) {

    if (e instanceof CompletionException) {
      // unpack
      e = e.getCause();
    }

    if (e instanceof ValidationException || e instanceof ConversionErrorException) {
      return HttpResponse.badRequest(e.getMessage());
    } else if (e instanceof NotFoundException) {
      return HttpResponse.notFound();
    }

    e.printStackTrace();
    return HttpResponse.serverError(e.getMessage());
  }

  private Persisted<Session> persist(Session pass) {
    Persisted<Session> persisted = new Persisted<>(UUID.randomUUID(), pass);
    allSessions.put(persisted.getId(), persisted);
    return persisted;
  }

  private Persisted<Session> getPersistedSession(UUID id) throws NotFoundException {
    Persisted<Session> session = allSessions.get(id);
    if (session == null) {
      throw new NotFoundException();
    }
    return session;
  }

  private Persisted<Session> persistAcceptance(Persisted<Session> persistedSession) {
    acceptedSessions.put(persistedSession.getId(), persistedSession);
    return persistedSession;
  }

  private Persisted<Session> sendCreatedEmail(Persisted<Session> persistedSession) {
    try {
      Session session = persistedSession.getObj();
      Email toSend = new Email(
          session.getEmail(),
          "Thanks for your session proposal",
          "Proposal: " + session.getTitle()
      );
      comClient.sendEmail(toSend);
    } catch (NoAvailableServiceException e) {
      System.err.println("Communication Service not available!");
    }
    return persistedSession;
  }

  private Persisted<Session> sendAcceptedEmail(Persisted<Session> persistedSession) {
    Session session = persistedSession.getObj();
    Email toSend = new Email(
        session.getEmail(),
        "Session proposal Accepted!",
        "Your session proposal has been accepted: " + session.getTitle()
    );
    sendIfPossible(toSend);
    return persistedSession;
  }

  private void sendIfPossible(Email email) {
    try {
      comClient.sendEmail(email);
    } catch (NoAvailableServiceException e) {
      System.err.println("Communication Service not available!");
    }
  }
}
