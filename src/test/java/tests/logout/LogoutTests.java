package tests.logout;

import models.login.LoginBodyModel;
import models.logout.LogoutBodyModel;
import models.logout.LogoutErrorResponseModel;
import models.logout.LogoutRequiredFieldErrorResponseModel;
import models.registration.RegistrationBodyModel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tests.TestBase;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.LoginSpec.loginRequestSpec;
import static specs.LoginSpec.successfulLoginResponseSpec;
import static specs.LogoutSpec.*;
import static specs.RegistrationSpec.registrationRequestSpec;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static tests.testData.TestData.*;

public class LogoutTests extends TestBase {
    @BeforeAll

    public static void registerHardcodedUser() {
        RegistrationBodyModel data = new RegistrationBodyModel(USERNAMEHC, PASSWORDHC);

        given(registrationRequestSpec)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .statusCode(anyOf(
                        is(201),
                        is(400) ));
    }
    @Test
    @DisplayName("Успешный logout с валидным refresh токеном")
    public void successfulLogoutTest() {
        LoginBodyModel loginData = new LoginBodyModel(USERNAMEHC, PASSWORDHC);

        String refreshToken = given(loginRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(successfulLoginResponseSpec)
                .extract().path("refresh");

        LogoutBodyModel logoutData = new LogoutBodyModel(refreshToken);

        given(logoutRequestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(successfulLogoutResponseSpec);
        LogoutErrorResponseModel errorResponse = given(logoutRequestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(invalidTokenLogoutResponseSpec)
                .extract().as(LogoutErrorResponseModel.class);

        assertThat(errorResponse.detail()).isEqualTo("Token is blacklisted");
        assertThat(errorResponse.code()).isEqualTo("token_not_valid");
    }

    @Test
    @DisplayName("Ошибка logout при передаче некорректного refresh токена")
    public void logoutWithInvalidTokenTest() {
        LogoutBodyModel logoutData = new LogoutBodyModel(INVALID_REFRESH_TOKEN);

        LogoutErrorResponseModel errorResponse = given(logoutRequestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(invalidTokenLogoutResponseSpec)
                .extract().as(LogoutErrorResponseModel.class);

        assertThat(errorResponse.detail()).isEqualTo("Token is invalid");
        assertThat(errorResponse.code()).isEqualTo("token_not_valid");
    }
    @Test
    @DisplayName("Ошибка logout при передаче пустого тела запроса")
    public void logoutWithEmptyBodyTest() {
        LogoutRequiredFieldErrorResponseModel errorResponse = given(logoutRequestSpec)
                .body("{}")
                .when()
                .post("/auth/logout/")
                .then()
                .spec(requiredRefreshLogoutResponseSpec)
                .extract().as(LogoutRequiredFieldErrorResponseModel.class);

        assertThat(errorResponse.refresh()).containsExactly("This field is required.");
    }
    @Test
    @DisplayName("Ошибка logout при передаче пустого refresh")
    public void logoutWithBlankRefreshTest() {
        LogoutBodyModel logoutData = new LogoutBodyModel("");

        LogoutRequiredFieldErrorResponseModel errorResponse = given(logoutRequestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(requiredRefreshLogoutResponseSpec)
                .extract().as(LogoutRequiredFieldErrorResponseModel.class);

        assertThat(errorResponse.refresh()).containsExactly("This field may not be blank.");
    }
    @Test
    @DisplayName("Ошибка logout без Content-Type")
    public void logoutWithoutContentTypeTest() {
        LoginBodyModel loginData = new LoginBodyModel(USERNAMEHC, PASSWORDHC);

        String refreshToken = given(loginRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(successfulLoginResponseSpec)
                .extract().path("refresh");

        LogoutBodyModel logoutData = new LogoutBodyModel(refreshToken);

        String detail = given(logoutWithoutContentTypeRequestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(logoutWithoutContentTypeResponseSpec)
                .extract().path("detail");

        assertThat(detail).isEqualTo("Unsupported media type \"text/plain; charset=ISO-8859-1\" in request.");
    }
}
