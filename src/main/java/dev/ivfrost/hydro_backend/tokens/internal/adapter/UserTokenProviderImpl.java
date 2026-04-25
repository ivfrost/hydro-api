package dev.ivfrost.hydro_backend.tokens.internal.adapter;

import com.auth0.jwt.interfaces.Claim;
import dev.ivfrost.hydro_backend.tokens.JWTUtil;
import dev.ivfrost.hydro_backend.tokens.MqttTokenPayload;
import dev.ivfrost.hydro_backend.tokens.TokenPayload;
import dev.ivfrost.hydro_backend.tokens.TokenResponse;
import dev.ivfrost.hydro_backend.tokens.internal.TokenService;
import dev.ivfrost.hydro_backend.users.UserTokenProvider;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class UserTokenProviderImpl implements UserTokenProvider {

  private final TokenService tokenService;
  private final JWTUtil jWTUtil;

  public UserTokenProviderImpl(TokenService tokenService, JWTUtil jWTUtil) {
    this.tokenService = tokenService;
    this.jWTUtil = jWTUtil;
  }

  @Override
  public boolean isTokenValidForUserId(String token, long userId) {
    return tokenService.isTokenValidForUserId(token, userId);
  }

  @Override
  public List<TokenResponse> generateRecoveryTokens(long userId) {
    return tokenService.generateRecoveryTokens(userId);
  }

  @Override
  public List<TokenResponse> generateAccessTokens(TokenPayload payload) {
    return tokenService.generateAccessTokens(payload);
  }

  @Override
  public TokenResponse generateMqttToken(MqttTokenPayload payload) {
    return tokenService.generateMqttToken(payload);
  }

  @Override
  public Map<String, Claim> validateTokenAndRetrieveClaims(String token) {
    return tokenService.validateTokenAndRetrieveClaims(token);
  }
}
