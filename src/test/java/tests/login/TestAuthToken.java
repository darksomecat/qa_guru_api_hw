package tests.login;

import models.login.LoginBodyModel;
import models.login.LoginErrorResponseModel;
import models.login.LoginResponseModel;
import models.registration.RegistrationBodyModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tests.TestBase;
import tests.testData.TestData;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.LoginSpec.*;
import static specs.RegistrationSpec.registrationRequestSpec;
import static specs.RegistrationSpec.successfulRegistrationResponseSpec;

public class TestAuthToken extends TestBase {

    @Test
    public void successfulLoginTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.username, testData.password);

        given(registrationRequestSpec)
                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec);

        LoginBodyModel loginData = new LoginBodyModel(testData.username, testData.password);

        LoginResponseModel loginResponse = given(loginRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(successfulLoginResponseSpec)
                .extract().as(LoginResponseModel.class);

        String expectedTokenPart = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        String actualAccess = loginResponse.access();
        String actualRefresh = loginResponse.refresh();

        assertThat(actualAccess).startsWith(expectedTokenPart);
        assertThat(actualRefresh).startsWith(expectedTokenPart);
        assertThat(actualAccess).isNotEqualTo(expectedTokenPart);
    }

    @Test
    @DisplayName("Ошибка авторизации при передаче пустого username (Negative)")
    public void loginWithBlankUsernameTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.username, testData.password);

        given(registrationRequestSpec)
                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec);

        LoginBodyModel loginData = new LoginBodyModel("", testData.password);

        LoginErrorResponseModel errorResponse = given(loginRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(badRequestLoginResponseSpec)
                .extract().as(LoginErrorResponseModel.class);

        assertThat(errorResponse.username()).containsExactly("This field may not be blank.");

}

    @Test
    @DisplayName("Проверка чувствительности к регистру в username (Case Sensitivity)")
    public void loginCaseSensitivityTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.username, testData.password);

        given(registrationRequestSpec)
                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec);

        LoginBodyModel loginData = new LoginBodyModel(testData.username.toUpperCase(), testData.password);

        LoginErrorResponseModel errorResponse = given(loginRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(unauthorizedLoginResponseSpec)
                .extract().as(LoginErrorResponseModel.class);

        assertThat(errorResponse.detail()).isEqualTo("Invalid username or password.");
    }

    @Test
    @DisplayName("Успешная авторизация с пробелами в username (Whitespace Trimming)")
    public void loginWhitespaceTrimmingTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.username, testData.password);

        given(registrationRequestSpec)
                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec);

        String usernameWithSpaces = "   " + testData.username + "   ";
        LoginBodyModel loginData = new LoginBodyModel(usernameWithSpaces, testData.password);

        LoginResponseModel loginResponse = given(loginRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(successfulLoginResponseSpec)
                .extract().as(LoginResponseModel.class);

        String expectedTokenPart = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        assertThat(loginResponse.access()).startsWith(expectedTokenPart);
        assertThat(loginResponse.refresh()).startsWith(expectedTokenPart);
    }

    @Test
    @DisplayName("Ошибка авторизации при передаче пустого password (Negative)")
    public void loginWithBlankPasswordTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.username, testData.password);

        given(registrationRequestSpec)
                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec);

        LoginBodyModel loginData = new LoginBodyModel(testData.username, "");

        LoginErrorResponseModel errorResponse = given(loginRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(badRequestLoginResponseSpec)
                .extract().as(LoginErrorResponseModel.class);

        assertThat(errorResponse.password()).containsExactly("This field may not be blank.");
    }

    @Test
    @DisplayName("Ошибка авторизации при вводе неверного пароля (Negative)")
    public void loginWithWrongPasswordTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.username, testData.password);

        given(registrationRequestSpec)
                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec);

        LoginBodyModel loginData = new LoginBodyModel(testData.username, "wrong_" + testData.password);

        LoginErrorResponseModel errorResponse = given(loginRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(unauthorizedLoginResponseSpec)
                .extract().as(LoginErrorResponseModel.class);

        assertThat(errorResponse.detail()).contains("Invalid username or password.");
    }

    @Test
    @DisplayName("Ошибка авторизации для несуществующего в системе пользователя (Negative)")
    public void loginWithNonExistentUserTest() {
        LoginBodyModel loginData = new LoginBodyModel(testData.username, testData.password);

        LoginErrorResponseModel errorResponse = given(loginRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(unauthorizedLoginResponseSpec)
                .extract().as(LoginErrorResponseModel.class);

        assertThat(errorResponse.detail()).contains("Invalid username or password.");
    }

    @Test
    @DisplayName("Ошибка авторизации при отправке пустого тела запроса (Negative)")
    public void loginWithEmptyBodyTest() {
        LoginErrorResponseModel errorResponse = given(loginRequestSpec)
                .body("{}")
                .when()
                .post("/auth/token/")
                .then()
                .spec(badRequestLoginResponseSpec)
                .extract().as(LoginErrorResponseModel.class);

        assertThat(errorResponse.username()).containsExactly("This field is required.");
        assertThat(errorResponse.password()).containsExactly("This field is required.");
    }

    @Test
    public void LoginWithoutContentTypeTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.username, testData.password);

        given(registrationRequestSpec)
                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec);

        LoginBodyModel loginData = new LoginBodyModel(testData.username, testData.password);

        String responseBody = given(loginWithoutContentTypeRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(loginWithoutContentTypeResponseSpec)
                .extract().path("detail");

        assertThat(responseBody).contains("Unsupported media type \"text/plain; charset=ISO-8859-1\" in request.");
    }

    @Test
    @DisplayName("Ошибка авторизации при передаче некорректных типов данных вместо строк (Negative)")
    public void loginWithInvalidDataTypesTest() {
        LoginErrorResponseModel errorResponse = given(loginRequestSpec)
                .body(TestData.invalidDataTypesJson)
                .when()
                .post("/auth/token/")
                .then()
                .spec(badRequestLoginResponseSpec)
                .extract().as(LoginErrorResponseModel.class);

        assertThat(errorResponse.username()).containsExactly("Not a valid string.");
        assertThat(errorResponse.password()).containsExactly("Not a valid string.");
    }

    @Test
    @DisplayName("Ошибка авторизации при отправке синтаксически некорректного JSON (Negative)")
    public void loginWithMalformedJsonTest() {
        String errorDetail = given(loginRequestSpec)
                .body(testData.malformedJson)
                .when()
                .post("/auth/token/")
                .then()
                .spec(jsonParseErrorLoginResponseSpec)
                .extract().path("detail");

        assertThat(errorDetail).contains("JSON parse error");
    }


}