package io.durbs.ndc.service

import com.google.inject.Inject
import com.google.inject.Singleton
import com.lambdaworks.redis.api.rx.RedisReactiveCommands
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.ndc.domain.auth.APIAuthRecord

import java.security.SecureRandom
import java.time.LocalDateTime

import rx.Observable

@CompileStatic
@Singleton
@Slf4j
class AuthenticationService {

  @Inject
  RedisReactiveCommands<String, APIAuthRecord> apiAuthRecordCache

  static final String AUTH_CODES_SET_KEY = 'authCodes'
  static final Integer NUMBER_OF_BITS_FOR_INTEGER_CONSTRUCTION = 130
  static final Integer BIT_RADIX = 32
  static final SecureRandom RANDOM = new SecureRandom()

  Observable<Boolean> createAPIAuthorizationRecord(final String contactName, final String contactEmailAddress) {

    /**
     * This works by choosing 130 bits from a cryptographically secure random bit generator, and encoding
     *   them in base-32. 128 bits is considered to be cryptographically strong, but each digit in a
     *   base 32 number can encode 5 bits, so 128 is rounded up to the next multiple of 5. This encoding
     *   is compact and efficient, with 5 random bits per character. Compare this to a random UUID, which
     *   only has 3.4 bits per character in standard layout, and only 122 random bits in total.
     */
    final String authCode = new BigInteger(NUMBER_OF_BITS_FOR_INTEGER_CONSTRUCTION, RANDOM).toString(BIT_RADIX)
    final LocalDateTime creationTime = LocalDateTime.now()

    final APIAuthRecord record = new APIAuthRecord(
      creationTime: creationTime,
      expirationTime: creationTime.plusDays(10),
      authCode: authCode,
      contactName: contactName,
      contactEmailAddress: contactEmailAddress)

    apiAuthRecordCache.hset(AUTH_CODES_SET_KEY, authCode, record)
  }

  Observable<Boolean> authCodeExists(final String authCode) {

    apiAuthRecordCache.hexists(AUTH_CODES_SET_KEY, authCode)
  }


}
