package quarkus;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.math.BigDecimal;
import java.util.List;

import io.quarkus.test.h2.H2DatabaseTestResource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
class AccountResourceTest {

    @Test
    @Order(1)
    void retrieveAll() {
        Response result = given().when().get("/accounts")
                .then()
                .statusCode(200)
                .body(containsString("Debbie Hall"),
                        containsString("David Tennant"),
                        containsString("Alex Kingston"))
                .extract().response();

        List<Account> accounts = result.jsonPath().getList("$");
        assertThat(accounts, not(empty()));
        assertThat(accounts, hasSize(8));

    }

    @Test
    @Order(2)
    void testGetAccount() {
        Account account = given().when().get("accounts/{accountNumber}", "123456789")
                .then().statusCode(200)
                .extract().as(Account.class);

        assertThat(account.getAccountNumber(), equalTo(123456789L));
        assertThat(account.getCustomerName(), equalTo("Debbie Hall"));
        assertThat(account.getBalance(), equalTo(new BigDecimal("550.78")));
        assertThat(account.getAccountStatus(), equalTo(AccountStatus.OPEN));
    }

    @Test
    @Order(3)
    void testCreateAccount() {
        Account newAccount = new Account(324323L, 112244L, "Sandy Holmes", new BigDecimal("154.55"));

        Account returnedAccount = given()
                .contentType(ContentType.JSON)
                .body(newAccount)
                .when().post("/accounts")
                .then()
                .statusCode(201)
                .extract().as(Account.class);

        assertThat(returnedAccount, notNullValue());
        assertThat(returnedAccount, equalTo(newAccount));

        Response result = given().when().get("/accounts")
                .then().statusCode(200)
                .body(containsString("Debbie Hall"),
                        containsString("David Tennant"),
                        containsString("Alex Kingston"),
                        containsString("Sandy Holmes"))
                .extract()
                .response();

        List<Account> accounts = result.jsonPath().getList("$");
        assertThat(accounts, not(empty()));
        assertThat(accounts, hasSize(9));
    }

    @Test
    @Order(7)
    void testCloseAccount() {
        given().when()
                .delete("accounts/{accountNumber}","78790")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(5)
    void testDeposit() {
        Account account = given().when()
                .put("accounts/{accountNumber}/deposit/{amount}", "78790", "100.30")
                .then()
                .extract().as(Account.class);
        assertThat(account.balance, is(new BigDecimal("539.31")));
    }

    @Test
    @Order(6)
    void testWithdrawal() {
        Account account = given().when()
                .put("accounts/{accountNumber}/withdrawal/{amount}", "444666", "200.60")
                .then()
                .extract().as(Account.class);
        assertThat(account.balance, is(new BigDecimal("3298.52")));
    }

}