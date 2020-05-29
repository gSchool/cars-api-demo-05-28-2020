# Building a REST API Using TDD

## User Story
As a Cars api user, I need to query for a car by it's model name.  

- The api must take a URI of /cars/name where “name” is the name of the car I want to retrieve.  
- The api should return a json object like {“name”: “car’s name”, “type”: “type of car”}
- If the car that I query for does not exist, then the api should return a HttpStatus.NO_CONTENT (204)

### Generate a new Spring Boot Project

We start the demonstration by creating a brand new Spring Boot project using the Spring Initializer.  Prove that we have no code written, and that we will copy and paste little or no code into the project.  Most code will be written before your very eyes.

Start up the project using InteliJ like most SI demos.  Then explain, that’s the last time we will run the project until we are done coding.

Create a new SpringBoot project using the SpringInitializer (Keep it simple)



*   Java 8
*   Dependencies
    *   Web
    *   JPA
    *   H2
    *   MySql
*   Name: car-api-demo
*   Default Package: com.galvanize.cars


### Step 1 - Write a failing Integration Test

Explain that this is a top down approach.  It has the following pros and cons…



*   Pro
    *   Better to drive out design
*   Cons
    *   Test will be red until very end 

Compare this to Bottom up approach

Create the integration test in the supplied Main Application class.  Stick with “Happy Path” tests.


```java
// Starts up entire SpringBoot context.  Takes a while to run!
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class CarApiDemoApplication{
   @Autowired
    TestRestTemplate restTemplate;

    @Test
    void getCarDetails_shouldReturnCar() {

        // What do we want to happen overall?
        ResponseEntity<Car> response = restTemplate.getForEntity("/cars/prius", Car.class);

        //From AssertJ Library - Provided by @SpringBootTest
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("prius");
        assertThat(response.getBody().getType()).isEqualTo("hybrid");

    }
}
```


Explain…



*   @SpringBootTest - Runs Spring Boot as configured.  Can make tests take a long time.
*   webEnvironment - Sets the port
    *   NONE - Does not run Web environment
    *   RANDOM_PORT - Changes the port on each run
    *   Specific port
*   TestRestTemplate - Same as RestTemplate, just a test version.  Details are taken care of by SpringBootTest.
*   assertThat() Comes from AssertJ which is provided by SpringBootTest annotation.

Create the Car class, and it’s two attributes to remove the compile errors.  Then, run the test, and explain why it failed, and how it will remain failing until we’re done.


### Step 2 - Write the controller test


```java
@WebMvcTest(CarController.class)
public class CarControllerTests {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private CarService carService;

    @Test
    void getCar_carExists_returnsCar() throws Exception {
        when(carService.getCarDetails("prius")).thenReturn(new Car("prius", "hybrid"));

        mockMvc.perform(get("/cars/prius"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value("prius"))
                .andExpect(jsonPath("type").value("hybrid"));
    }

    @Test
    void getCar_notFound_returnsNoContent() throws Exception {
        Mockito.when(carService.getCarDetails(anyString())).thenThrow(new CarNotFoundException());

        mockMvc.perform(get("/cars/prius"))
                .andExpect(status().isNoContent());
    }
}
```


Write the happy path test,  getCar_carExists_returnsCar(). Only write enough code to make it green.  Explain the following concepts briefly…



*   @WebMvcTest - Only runs the one controller and simulates the web interaction.
    *   Include the controller name if you have more than one.  I do it just to be explicit.
*   @Autowired MockMvc - Provided by WebMvcTest
*   @MockBean - Also provided by WebMvcTest
*   Mockito.when().then() - Setting up your mocks
*   mockMvc.perform()
    *   What it is
    *   What it does
    *   status().isOk()
    *   jsonPath

Show that the test fails, then implement the “getCar()” method in the controller with “return null”.  Note the change in the failure from 404 to NPE.  Then implement using the carService.getCarDetails().  Explain that you need to implement the CarService, but with dummy implementation.


```java
@RestController
public class CarController {

    private CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping("/cars/{name}")
    public Car getCar(@PathVariable String name){
        return carService.getCarDetails(name);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void carNotFoundHandler(CarNotFoundException e){ }


}
```


This is where we introduce edge, or corner cases (slice and unit tests).  In this case, what do we do if the car you’re looking for is not found?  Answer, throw a CarNotFoundException.  Implement the test case (above) for this, then create the exception.

At this point, we have our controller doing what we want it to.  We’ve discovered what the service api should look like.  Now we have to implement the CarService.  Where do we go for that?  We write the test for it first.


### Step 3 - Write the service test

Our controller has determined that we will need a method to get the car details.  As before, we will first take the happy path…

```java
@ExtendWith(MockitoExtension.class) // Sets up the @Mock annotation
class CarServiceTest {
    @Mock
    CarRepository carRepository;

    private CarService carService;

    @BeforeEach
    void setUp() {
        carService = new CarService(carRepository);
    }

    @Test
    void getCarDetails_exists_returnCar() {
        when(carRepository.findCarByName(anyString())).thenReturn(new Car("prius", "hybrid"));

        Car car = carService.getCarDetails("prius");
        assertThat(car).isNotNull();
        assertThat(car.getName()).isEqualTo("prius");
        assertThat(car.getType()).isEqualTo("hybrid");
    }

    @Test
    void getCarDetails_doesntExist_throwsError() {
        when(carRepository.findCarByName(anyString())).thenReturn(null);
        assertThatThrownBy(
                () -> carService.getCarDetails("nonExistantCar")
        ).isInstanceOf(CarNotFoundException.class);
    }
}
```


Because we want our test to be independent of any resources, we will “mock” the database interaction.  This is identical to how we “mocked” the service in the controller.  We are ignoring what the “repository” (dao) does, and implicitly tell the test what it should return.  Think of it as “we expect the database knows what it should do.”

Since we’re not using anything Spring, we don’t have to use the annotation **<code>@SpringBootTest</code></strong> .  Instead, we just need a runner that knows how to mock something, so we choose <strong><code>@ExtendWith(MockitoExtension.class). </code></strong>By not using spring boot test, we don’t spin up an entire spring boot environment.  Remember, everything in Spring is just a <em>Plain Old Java Object</em> (POJO).  

Now, we implement the code, as simple as possible…


```java
    public Car getCarDetails(String name) {
        return carRepository.findCarByName(name);
    }
```


Next, we need to implement a test for when the car is not found…


```java
    @Test
    void getCarDetails_doesntExist_throwsError() {
        when(carRepository.findCarByName(anyString())).thenReturn(null);
        assertThatThrownBy(
                () -> carService.getCarDetails("nonExistantCar")
        ).isInstanceOf(CarNotFoundException.class);
    }
```


And then, provide the implementation for it…


```
    public Car getCarDetails(String name) {
        Car car =  carRepository.findCarByName(name);
        if(car == null){
            throw new CarNotFoundException();
        }
        return car;
    }
```


Remember, we already implemented the exception when we wrote the controller test for it.  This is what drove the implementation for this method.

Checking back, we still have our integration test failing…


### Run the integration test again

Before: we were failing due to a 404, Not Found. Which means that the URI we were looking for doesn’t exist.  Now, we get a 204 - No Content.  Does that sound familiar?  In the controller, we have this code…


```java
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void carNotFoundHandler(CarNotFoundException e){ }
```


So, why is the integration test failing?  Because, we said to return that if the car we were looking for was not found.  WOW, we’ve come all this way without ever starting anything up, and now it’s working?  To take this fully to the end, we can prime the db with a “prius” using out repository as follows…


```java
    @BeforeEach
    void setUp() {
        carRepository.save(new Car("prius", "hybrid"));
    }
```

Now, the test runs and is successful.  


## Recap

1. We started with writing a failing integration test.  This may not be possible, if you are unable to check in failing code.  However, you could keep this simple test local until you finish your cycle.  
2. Next, we created the CarControllerTests and mocked the CarService to focus on the controller interaction.  We used the test to determine the api of the service, and how the controller will respond to the user depending on what the service returns.
3. Using what we learned in the CarControllerTest, we flushed out the details of the CarService using the tests.  
4. Returning back to the Integration test, we found that the test was now failing due to a 204 (was a 404).  To get this test to pass, we only need to finish out the repo, entity, and then prime the db with the car we are looking for.  NOTE: We NEVER started up the server!


## Full Circle

Now, we can decide what database we want to use, MySql in our case, and configure it, and it should work


```java
spring.datasource.url=jdbc:mysql://localhost:3306/cars_api?user=root
spring.jpa.hibernate.ddl-auto=update
```


Fire up the database pre-loaded with car data, and start the server.  Start up Postman, and demonstrate that the application works as we designed it.

Seed Data…


```sql
insert into cars(name, type) values("prius", "hybrid");
insert into cars(name, type) values("pilot", "xover");
insert into cars(name, type) values("sierra", "Pickup");
insert into cars(name, type) values("civic", "compact");
insert into cars(name, type) values("530e", "performance sedan");
insert into cars(name, type) values("xterra", "suv");
insert into cars(name, type) values("rogue", "xover");
```

---
### Resources
[Test Driven Development with Spring Boot](https://youtu.be/s9vt6UJiHg4)
