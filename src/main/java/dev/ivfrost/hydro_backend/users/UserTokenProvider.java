package dev.ivfrost.hydro_backend.users;

import com.auth0.jwt.interfaces.Claim;
import dev.ivfrost.hydro_backend.tokens.MqttTokenPayload;
import dev.ivfrost.hydro_backend.tokens.TokenPayload;
import dev.ivfrost.hydro_backend.tokens.TokenResponse;
import java.util.List;
import java.util.Map;

public interface UserTokenProvider {

  boolean isTokenValidForUserId(String token, long userId);

  List<TokenResponse> generateRecoveryTokens(long userId);

  List<TokenResponse> generateAccessTokens(TokenPayload payload);

  TokenResponse generateMqttToken(MqttTokenPayload payload);

  Map<String, Claim> validateTokenAndRetrieveClaims(String token);
}
