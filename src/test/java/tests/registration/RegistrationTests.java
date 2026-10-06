package tests.registration;

import models.registration.RegistrationBodyModel;
import models.registration.RegistrationErrorResponseModel;
import models.registration.RegistrationResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tests.TestBase;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.BaseSpec.baseRequestSpec;
import static specs.RegistrationSpec.*;

public class RegistrationTests extends TestBase {

    @Test
    @DisplayName("Успешная регистрация нового пользователя")
    public void successfulRegistrationResponseTest() {
        RegistrationBodyModel data = new RegistrationBodyModel(testData.username, testData.password);

        RegistrationResponseModel registrationResponse = given(baseRequestSpec)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec)
                .extract()
                .as(RegistrationResponseModel.class);
        assertThat(registrationResponse.username()).isEqualTo(data.username());
        assertThat(registrationResponse.id()).isNotNull();
        assertThat(registrationResponse.remoteAddr()).isNotEmpty();
    }

    @Test
    @DisplayName("Ошибка регистрация: пользователь с таким именем уже существует")
    public void successfulRegistration400AlreadyExistsTest() {
        String duplicateUser = testData.username;
        RegistrationBodyModel firstRegistrationData = new RegistrationBodyModel(duplicateUser, testData.password);
        given(baseRequestSpec)
                .body(firstRegistrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec);

        RegistrationBodyModel duplicateData = new RegistrationBodyModel(duplicateUser, testData.password);
        RegistrationErrorResponseModel errorResponse = given(baseRequestSpec)
                .body(duplicateData)
                .when()
                .post("/users/register/")
                .then()
                .spec(badRequestRegistrationResponseSpec)
                .extract()
                .as(RegistrationErrorResponseModel.class);

        assertThat(errorResponse.username()).containsExactly("A user with that username already exists.");
    }

    @Test
    @DisplayName("Ошибка регистрации при передаче пустого username")
    public void registrationWithBlankUsernameTest() {
        RegistrationBodyModel data = new RegistrationBodyModel("", testData.password);
        RegistrationErrorResponseModel errorResponse = given(baseRequestSpec)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .spec(badRequestRegistrationResponseSpec)
                .extract()
                .as(RegistrationErrorResponseModel.class);

        assertThat(errorResponse.username()).containsExactly("This field may not be blank.");
    }
    @Test
    @DisplayName("Ошибка регистрации при передаче пустого password")
    public void registrationWithBlankPasswordTest() {
        RegistrationBodyModel data = new RegistrationBodyModel(testData.username, "");
        RegistrationErrorResponseModel errorResponse = given(baseRequestSpec)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .spec(badRequestRegistrationResponseSpec)
                .extract()
                .as(RegistrationErrorResponseModel.class);

        assertThat(errorResponse.password()).containsExactly("This field may not be blank.");
    }

    @Test
    @DisplayName("Ошибка регистрации при отправке пустого тела запроса")
    public void registrationWithEmptyBodyTest() {

        RegistrationErrorResponseModel errorResponse = given(baseRequestSpec)
                .body("{}")
                .when()
                .post("/users/register/")
                .then()
                .spec(badRequestRegistrationResponseSpec)
                .extract()
                .as(RegistrationErrorResponseModel.class);

        assertThat(errorResponse.username()).containsExactly("This field is required.");
        assertThat(errorResponse.password()).containsExactly("This field is required.");
    }

    @Test
    @DisplayName("Проверка очистки пробелов в username при регистрации")
    public void registrationWhitespaceTrimmingTest() {
        String usernameWithSpaces = "   " + testData.username + "   ";
        RegistrationBodyModel data = new RegistrationBodyModel(usernameWithSpaces, testData.password);
        RegistrationResponseModel registrationResponse = given(baseRequestSpec)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec)
                .extract()
                .as(RegistrationResponseModel.class);

        assertThat(registrationResponse.username()).isEqualTo(testData.username);

        assertThat(registrationResponse.id()).isNotNull();
        assertThat(registrationResponse.remoteAddr()).isNotEmpty();
    }
    @Test
    @DisplayName("Ошибка регистрации при превышении максимальной длины username в 150 символов")
    public void registrationWithTooLongUsernameTest() {
        RegistrationBodyModel data = new RegistrationBodyModel(testData.tooLong151Symbols, testData.password);
        RegistrationErrorResponseModel errorResponse = given(baseRequestSpec)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .spec(badRequestRegistrationResponseSpec) // Проверяет код 400 и JSON-схему
                .extract()
                .as(RegistrationErrorResponseModel.class);
        assertThat(errorResponse.username()).containsExactly("Ensure this field has no more than 150 characters.");
    }
    @Test
    @DisplayName("Регистрация при максимальной длине username в 150 символов")
    public void registrationWithMaxUsernameTest() {
        RegistrationBodyModel data = new RegistrationBodyModel(testData.maxLeght150Symbols, testData.password);
        RegistrationResponseModel registrationResponse = given(baseRequestSpec)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec)
                .extract()
                .as(RegistrationResponseModel.class);
        assertThat(registrationResponse.username()).isEqualTo(data.username());
        assertThat(registrationResponse.id()).isNotNull();
        assertThat(registrationResponse.remoteAddr()).isNotEmpty();
    }
    @Test
    @DisplayName("Успешная регистрация нового пользователя")
    public void registrationWithMaxPasswordTest() {
        RegistrationBodyModel data = new RegistrationBodyModel(testData.username, testData.maxPassword);

        RegistrationResponseModel registrationResponse = given(baseRequestSpec)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec)
                .extract()
                .as(RegistrationResponseModel.class);
        assertThat(registrationResponse.username()).isEqualTo(data.username());
        assertThat(registrationResponse.id()).isNotNull();
        assertThat(registrationResponse.remoteAddr()).isNotEmpty();
    }
    @Test
    @DisplayName("Ошибка регистрации при превышении максимальной длины password в 128 символов")
    public void registrationWithTooLongPasswordTest() {
        RegistrationBodyModel data = new RegistrationBodyModel(testData.username, testData.tooLongPassword);
        RegistrationErrorResponseModel errorResponse = given(baseRequestSpec)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .spec(badRequestRegistrationResponseSpec)
                .extract()
                .as(RegistrationErrorResponseModel.class);
        assertThat(errorResponse.password()).containsExactly("Ensure this field has no more than 128 characters.");
    }

}
