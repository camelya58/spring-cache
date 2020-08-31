# spring-cache
Simple project with Spring Cache.

Stack: Spring Cache, H2 database, Lombok, Spring Data Jpa.

## Step 1
Create spring boot project, choose Gradle.

## Step 2
Add dependencies in build.gradle file.
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '2.2.2.RELEASE'
    id 'io.spring.dependency-management' version '1.0.8.RELEASE'
    id 'io.freefair.lombok' version '4.1.6'
}

group 'com.github.camelya58'
version '1.0-SNAPSHOT'
sourceCompatibility = '11'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-cache'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    compileOnly 'org.projectlombok:lombok'
    runtimeOnly 'com.h2database:h2'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.projectlombok:lombok'
    testImplementation 'org.slf4j:slf4j-api:1.7.30'
}
```
It is necessary to add plugin - ***io.freefair.lombok***, otherwise tests won't see the Lombok libraries as Slf4j.

## Step 3 
Create main class and add annotation ***@EnableCaching***.
```java
@EnableCaching
@SpringBootApplication
public class SpringCacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCacheApplication.class, args);
    }
}
```

## Step 4
Create model User.
```java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@ToString
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
```

## Step 5
Create repository witch implementsJpaRepository.
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
```

## Step 6
Create Service and ServiceImpl.
```java
public interface UserService {

    User create(User user);
    List<User> getAll();
}
```
```java
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User create(User user) {
        return repository.save(user);
    }

    @Override
    public List<User> getAll() {
        return repository.findAll();
    }
}
```

## Step 7
Create method, where you want to place cache and add annotation ***@Cacheable***. You can give it a name.
```java
    @Override
    @Cacheable("users")
    public User get(Long id) {
        log.info("getting user by id: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found by id " + id));
    }
```
Now when you create several users and want to get info about user by id, you won't get the info from repository, 
but you will get that info about already created user from cache.

When can check it using test.

## Step 8 
Create base class for tests.
```java
@RunWith(SpringRunner.class)
@SpringBootTest
public abstract class AbstractTest {
}
```

## Step 9
Create simple test to check for caching.
```java
@Slf4j
public class UserServiceTest extends AbstractTest {

    @Autowired
    private UserService service;

    @Test
    public void get() {
        User user1 = service.create(new User("Vasya", "vasya@mail.ru"));
        User user2 = service.create(new User("Kolya", "kolya@mail.ru"));

        getAndPrint(user1.getId());
        getAndPrint(user2.getId());
        getAndPrint(user1.getId());
        getAndPrint(user2.getId());
    }
    private void getAndPrint(Long id) {
        log.info("user found: {}", service.get(id));
    }
}
```
When test will pass we'll receive following result:
```
: getting user by id: 1
: user found: User(id=1, name=Vasya, email=vasya@mail.ru)
: getting user by id: 2
: user found: User(id=2, name=Kolya, email=kolya@mail.ru)
: user found: User(id=1, name=Vasya, email=vasya@mail.ru)
: user found: User(id=2, name=Kolya, email=kolya@mail.ru)
```
 We can see that we invoke the method "get(long id)" only when the user have just been created. 
 
 If we want to get info about already existed user like in lines 174 and 175 we receive the info from cache.
 
 ## Step 10
 If our entity has multiple parameters we can choose which parameter we will use for caching using the annotation 
 ***@Cacheable(value = "users", key = "#name")***.
```java
 @Override
    @Cacheable(value = "users", key = "#name")
    public User create(String name, String email) {
        log.info("creating user with parameters: {}, {}", name, email);
        return repository.save(new User(name, email));
    }
``` 

## Step 11 
Create new method in test class.
```java
@Test
    public void create() {
        createAndPrint("Ivan", "ivan@mail.ru");
        createAndPrint("Ivan", "ivan1122@mail.ru");
        createAndPrint("Sergey", "ivan@mail.ru");

        log.info("all entries are below:");
        service.getAll().forEach(u -> log.info("{}", u.toString()));
    }

    private void createAndPrint(String name, String email) {
        log.info("created user: {}", service.create(name, email));
    }
```
We have 3 users, where first and second users have the same names and first and third - the same emails.

We will receive the following result:
```
: creating user with parameters: Ivan, ivan@mail.ru
: created user: User(id=1, name=Ivan, email=ivan@mail.ru)
: created user: User(id=1, name=Ivan, email=ivan@mail.ru)
: creating user with parameters: Sergey, ivan@mail.ru
: created user: User(id=2, name=Sergey, email=ivan@mail.ru)
: all entries are below:
: User(id=1, name=Ivan, email=ivan@mail.ru)
: User(id=2, name=Sergey, email=ivan@mail.ru)
```
Now we can see that we created only 2 users: first anf third because they have different names.

Despite the fact that second user has other email he didn't be saved in the repository, because the method 
'create(String name, String email)' wasn't invoked insofar as second user has the same name as first does. 
This means that the data was been taken from the cache.

## Step 12
Sometimes we want to refresh our values in the cache. The annotation ***@CachePut*** allows us to do it.

Create several methods in ServiceImpl.
```java
  @Override
    @Cacheable(value = "users", key = "#user.name")
    public User createOrReturnCached(User user) {
        log.info("creating user: {}", user);
        return repository.save(user);
    }

    @Override
    @CachePut(value = "users", key = "#user.name")
    public User createAndRefreshCache(User user) {
        log.info("creating user: {}", user);
        return repository.save(user);
    }
```
The first method will return cached value and the second - will update a cache by name.

## Step 13
Add several methods in test class.
```java
@Test
    public void createAndRefresh() {
        User user1 = service.createOrReturnCached(new User("Vasya", "vasya@mail.ru"));
        log.info("created user1: {}", user1);

        User user2 = service.createOrReturnCached(new User("Vasya", "misha@mail.ru"));
        log.info("created user2: {}", user2);

        User user3 = service.createAndRefreshCache(new User("Vasya", "kolya@mail.ru"));
        log.info("created user3: {}", user3);

        User user4 = service.createOrReturnCached(new User("Vasya", "petya@mail.ru"));
        log.info("created user4: {}", user4);
        
        log.info("all entries are below:");
        service.getAll().forEach(u -> log.info("{}", u.toString()));
    }
```
And the result is:
```
: creating user: User(id=0, name=Vasya, email=vasya@mail.ru)
: created user1: User(id=1, name=Vasya, email=vasya@mail.ru)
: created user2: User(id=1, name=Vasya, email=vasya@mail.ru)
: creating user: User(id=0, name=Vasya, email=kolya@mail.ru)
: created user3: User(id=2, name=Vasya, email=kolya@mail.ru)
: created user4: User(id=2, name=Vasya, email=kolya@mail.ru)
: all entries are below:
: User(id=1, name=Vasya, email=vasya@mail.ru)
: User(id=2, name=Vasya, email=kolya@mail.ru)
```
We see that user1 was saved in database and cache. 
User2 wasn't be created because we are getting the info from the cache.
User3 was saved in database and cache has been updated.
And user4 has the same result as user2.

That's why we saved only 2 users in our database.

## Step 14
In case when you have the instance in a cache, but it has already been deleted in database you must use the annotation
***@CacheEvict*** that allows to update your cache and delete an instance from the database and the cache at the same time.  

```java
@Override
    public void delete(Long id) {
        log.info("deleting user by id: {}", id);
        repository.deleteById(id);
    }

    @Override
    @CacheEvict("users")
    public void deleteAndEvict(Long id) {
        log.info("deleting user by id: {}", id);
        repository.deleteById(id);
    }
```

## Step 15
Create method in test class to check the difference between 2 "delete" methods.
```java
    @Test
    public void delete() {
        User user1 = service.create(new User("Vasya", "vasya@mail.ru"));
        log.info("{}", service.get(user1.getId()));

        User user2 = service.create(new User("Vasya", "vasya@mail.ru"));
        log.info("{}", service.get(user2.getId()));

        service.delete(user1.getId());
        service.deleteAndEvict(user2.getId());

        log.info("{}", service.get(user1.getId()));
        log.info("{}", service.get(user2.getId()));
    }
```
We will receive the following result:
```
: getting user by id: 1
: User(id=1, name=Vasya, email=vasya@mail.ru)
: getting user by id: 2
: User(id=2, name=Vasya, email=vasya@mail.ru)
: deleting user by id: 1
: deleting user by id: 2
: User(id=1, name=Vasya, email=vasya@mail.ru)
: getting user by id: 2

User not found by id 2
javax.persistence.EntityNotFoundException: User not found by id 2
```
We've got an *EntityNotFoundException* in case, when we had deleted user2 using the method "deleteAndEvict", because
it is no longer in the cache.

## Step 16
Sometimes you need to group multiple settings. In this case use the following syntax:
```java
 @Caching(
            cacheable = {
                    @Cacheable("users"),
                    @Cacheable("contacts")
            },
            put = {
                    @CachePut("tables"),
                    @CachePut("chairs"),
                    @CachePut(value = "meals", key = "#user.email")
            },
            evict = {
                    @CacheEvict(value = "services", key = "#user.name")
            }
    )
    void cacheExample(User user) {
    }
```

## Step 17
When we add the annotation @EnableCache in our project Spring cache automatically creates a ***CacheManager***.

But we can create own CacheManager using *ConcurrentMapCacheManager* and overriding the method "createConcurrentMapCache".

We use CacheBuilder of Guava to configure following methods:

maximumSize — this method allows to find load balance between database and cache;
refreshAfterWrite — the time after writing the value to the cache, after which it will be automatically updated;
expireAfterAccess — the lifetime of the value since the last access to it;
expireAfterWrite — the lifetime of the value after being written to the cache. 

Add new dependency to build.gradle file:
```
implementation 'com.google.guava:guava:29.0-jre'
```
```java
  @Bean("cacheManager")
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager() {
            @Override
            protected Cache createConcurrentMapCache(String name) {
                return new ConcurrentMapCache(
                        name,
                        CacheBuilder.newBuilder()
                                .expireAfterWrite(1, TimeUnit.SECONDS)
                                .build().asMap(),
                        false);
            }
        };
    }
```
## Step 18
Create test.
```java
@Test
    public void checkSettings() throws InterruptedException {
        User user1 = service.createOrReturnCached(new User("Vasya", "vasya@mail.ru"));
        log.info("{}", service.get(user1.getId()));

        User user2 = service.createOrReturnCached(new User("Vasya", "vasya@mail.ru"));
        log.info("{}", service.get(user2.getId()));

        Thread.sleep(1000L);
        User user3 = service.createOrReturnCached(new User("Vasya", "vasya@mail.ru"));
        log.info("{}", service.get(user3.getId()));
    }
```
We will receive the following result:
```
: creating user: User(id=0, name=Vasya, email=vasya@mail.ru)
: getting user by id: 1
: User(id=1, name=Vasya, email=vasya@mail.ru)
: User(id=1, name=Vasya, email=vasya@mail.ru)
: creating user: User(id=0, name=Vasya, email=vasya@mail.ru)
: getting user by id: 2
: User(id=2, name=Vasya, email=vasya@mail.ru)
```
As we can see when we want to create user2 we can't do it, because he has the same name, so we get the info from cache.

After 1 second we create user3 even though he has the same name as user1 and user2, but our cache has already expired.

Source: https://habr.com/ru/post/465667/. 