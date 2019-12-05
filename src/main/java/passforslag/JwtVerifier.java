package passforslag;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetVerifier;
import io.micronaut.context.annotation.Value;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Singleton;

@Singleton
public class JwtVerifier {

  private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

  public JwtVerifier(@Value("${access-token.issuer}") String issuer,
                     @Value("${access-token.audience}") String audience)
      throws MalformedURLException {

    // Create a JWT processor for the access tokens
    jwtProcessor = new DefaultJWTProcessor<>();

    // The public RSA keys to validate the signatures will be sourced from the
    // OAuth 2.0 server's JWK set, published at a well-known URL. The RemoteJWKSet
    // object caches the retrieved keys to speed up subsequent look-ups and can
    // also handle key-rollover
    JWKSource<SecurityContext> keySource =
        new RemoteJWKSet<>(new URL(issuer + ".well-known/jwks.json"));

    // The expected JWS algorithm of the access tokens (agreed out-of-band)
    JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;

    // Configure the JWT processor with a key selector to feed matching public
    // RSA keys sourced from the JWK set URL
    JWSKeySelector<SecurityContext> keySelector =
        new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);

    jwtProcessor.setJWSKeySelector(keySelector);

    // Required claim - value pairs (issuer and audience must be correct)
    JWTClaimsSet requiredClaimValues = new JWTClaimsSet.Builder()
        .issuer(issuer)
        .build();

    // Define claims that must be present in all access tokens
    Set<String> requiredClaimKeys =
        new HashSet<>(Arrays.asList("sub", "iat", "exp"));

    JWTClaimsSetVerifier<SecurityContext> claimsSetVerifier =
        new DefaultJWTClaimsVerifier<>(audience, requiredClaimValues, requiredClaimKeys);

    // Set the JWT constraints
    jwtProcessor.setJWTClaimsSetVerifier(claimsSetVerifier);
  }

  JWTClaimsSet verifyAuthorizationHeader(String authorizationHeader) {
    String accessTokenPrefix = "Bearer ";
    if (!authorizationHeader.startsWith(accessTokenPrefix)) {
      throw new UnauthorizedException("Bad prefix");
    }
    try {
      // Security Context not used. Should / can it be? What info should it contain?
      return jwtProcessor.process(authorizationHeader.substring(accessTokenPrefix.length()), null);
    } catch (ParseException | BadJOSEException | JOSEException e) {
      // TODO: remove after debugging
      e.printStackTrace();
      throw new UnauthorizedException("Bad JWT", e);
    }
  }
}
