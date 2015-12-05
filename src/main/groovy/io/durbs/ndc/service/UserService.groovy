package io.durbs.ndc.service

import com.google.inject.Inject
import com.google.inject.Singleton
import com.mongodb.rx.client.MongoDatabase
import com.mongodb.rx.client.Success
import de.qaware.heimdall.Password
import de.qaware.heimdall.PasswordFactory
import de.qaware.heimdall.SecureCharArray
import groovy.util.logging.Slf4j
import io.durbs.ndc.domain.User
import rx.Observable

import static com.mongodb.client.model.Filters.eq

@Singleton
@Slf4j
class UserService {

  @Inject
  MongoDatabase mongoDatabase

  private static final String MONGO_COLLECTION = 'users'

  Observable<Success> createNewUser(final String firstName,
                                    final String lastName,
                                    final String username,
                                    final String suppliedPassword) {

    Password password = PasswordFactory.create()

    mongoDatabase
      .getCollection(MONGO_COLLECTION, User)
      .insertOne(new User(firstName: firstName,
        lastName: lastName,
        username: username,
        password: password.hash(new SecureCharArray(suppliedPassword.toCharArray()))))
      .asObservable()
      .bindExec()
  }

  Observable<User> updateUser(String firstName, String lastName, String suppliedPassword) {

    mongoDatabase
      .getCollection(MONGO_COLLECTION, User)
      .find()
      .toObservable()
      .bindExec()
  }

  Observable<User> getUserByID(final String id) {

    mongoDatabase
      .getCollection(MONGO_COLLECTION, User)
      .find(eq('id', id))
      .limit(1)
      .toObservable()
      .bindExec()
  }

  Observable<User> getUserByUsername(final String username) {

    mongoDatabase
      .getCollection(MONGO_COLLECTION, User)
      .find(eq('username', username))
      .toObservable()
      .bindExec()
  }
}
