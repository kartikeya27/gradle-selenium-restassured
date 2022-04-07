package gradle.junit.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.opentelemetry.sdk.logs.data.Body;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

class JuiceTest {
    private static String address = "localhost";
    private static String port = "3000";
    private static String baseUrl = String.format("http://%s:%s", address, port);

    static WebDriver driver;
    static Customer customer;
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
  
    @BeforeAll
    static void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();

        //TODO Task1: Add your credentials to customer i.e. email, password and security answer.
        customer = new Customer.Builder().build();
    }

    @AfterAll
    static void teardown() {
       driver.quit();
    }

    //TODO Task2: Login and post a product review using Selenium
    @Test
    void loginAndPostProductReviewViaUi() {
    	driver.manage().window().maximize();
		driver.manage().deleteAllCookies();
        driver.get(baseUrl + "/#/login");
        
        // TODO Dismiss popup (click close)
        WebElement dismissPopupButton = driver.findElement(By.xpath("//span[contains(text(),'Dismiss')]"));
        
        dismissPopupButton.click();
        
        // Login with credentials
        WebElement emailField = driver.findElement(By.name("email"));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.id("loginButton"));

        emailField.sendKeys("kebijebi@gmail.com");
        passwordField.sendKeys("1200@Villa");
        loginButton.click();

        // TODO Navigate to product and post review  
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        WebElement cookies = driver.findElement(By.linkText("Me want it!"));
        cookies.click();
        
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        WebElement appleJuiceImage = driver.findElement(By.xpath("(//img[@alt='Apple Juice (1000ml)'])[1]"));
        appleJuiceImage.click();
        
        WebElement reviewTextarea = driver.findElement(By.tagName("textarea"));
        reviewTextarea.sendKeys("Very delicious juice");
        
        WebElement submitButton = driver.findElement(By.cssSelector("#submitButton .material-icons"));
        submitButton.click();
        
        WebElement dropdownButton = driver.findElement(By.cssSelector(".mat-expansion-indicator"));
        Actions action1 = new Actions(driver);
        action1.doubleClick(dropdownButton).build().perform();
        dropdownButton.click();

        // TODO Assert that the review has been created successfully
        WebElement e = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".comment:nth-child(2) p")));
        String reviewText = e.getAttribute("innerHTML");
        assertEquals(reviewText, "Very delicious juice");
    }

    

	// TODO Task3: Login and post a product review using the Juice Shop API
    @Test
    void loginAndPostProductReviewViaApi() {
        // Example HTTP request with assertions using Rest Assured. Can be removed.
//        String status = given()
//                .header("Content-Type", "application/json")
//                .when()
//                .get(baseUrl + "/rest/products/search")
//                .then()
//                .statusCode(200)
//                .body("status", equalTo("success") )
//                .body("data", hasItem(
//                        allOf(
//                                hasEntry("image", "apple_pressings.jpg"),
//                                hasEntry("name", "Apple Pomace")
//                        )
//                ))
//                .extract()
//                .path("status");
//        System.out.println(String.format("Status value is: %s", status));
    }
    
    // TODO Retrieve token via login API
    // TODO Use token to post review to product
    // TODO Assert that the product review has persisted
    @Test
    void retrieveTokenViaLoginApi() {
    	RestAssured.baseURI = baseUrl;
    	
    	RequestSpecification request = RestAssured.given();
    			
        String payload = "{ \n"
    			+ "  \"email\": \"kebijebi@gmail.com\", \n"
    			+ "  \"password\": \"1200@Villa\"\n"
    			+ "}";
    	request.header("Content-Type", "application/json");
    	
    	Response responseLoginToken = request.body(payload).post("/rest/user/login");
    	
    	responseLoginToken.prettyPrint();
    	
    	String jsonString = responseLoginToken.getBody().asString();
    	
    	String tokenGenerated = JsonPath.from(jsonString).get("token");
    	
    	request.header("Authorization","Bearer" + tokenGenerated)
    	       .header("Content-Type","application/json");
    	
    	String comment = "{\n"
    			+ "  \"message\": \"One of my favorites123!\",\n"
    			+ "  \"author\": \"kebijebi@gmail.com\"\n"
    			+ "}";
    	
    	Response commentResponse = request.body(comment).put("/rest/products/1/reviews");
    	
    	Assertions.assertEquals(201, commentResponse.statusCode());
    	
    	
    	given()
              .header("Content-Type", "application/json")
              .when()
              .get("/rest/products/1/reviews")
              .then()
              .statusCode(200)
              .body("status", equalTo("success"))
    	      .body("data", hasItem(
              allOf(
                      hasEntry("message", "One of my favorites123!"),
                      hasEntry("author", "kebijebi@gmail.com")
              )
         ))
    	 .extract()
         .path("status");
    }
}
