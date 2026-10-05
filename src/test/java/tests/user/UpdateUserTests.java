package tests.user;

import models.login.LoginBodyModel;
import models.registration.RegistrationBodyModel;
import models.user.UpdateUserBodyModel;
import models.user.UserDetailErrorResponseModel;
import models.user.UserErrorResponseModel;
import models.user.UserResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import tests.TestBase;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static specs.LoginSpec.loginRequestSpec;
import static specs.LoginSpec.successfulLoginResponseSpec;
import static specs.RegistrationSpec.registrationRequestSpec;
import static specs.UserSpec.*;
import static tests.testData.TestData.*;

public class UpdateUserTests extends TestBase {

    private String accessToken;

    @BeforeEach
    public void auth() {
        RegistrationBodyModel data = new RegistrationBodyModel(USERNAMEHC, PASSWORDHC);

        given(registrationRequestSpec)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .statusCode(anyOf(
                        is(201),
                        is(400) ));

        LoginBodyModel loginData = new LoginBodyModel(USERNAMEHC, PASSWORDHC);

        accessToken = given(loginRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(successfulLoginResponseSpec)
                .extract().path("access");
    }

    @Test
    @DisplayName("Успешное обновление профиля пользователя (PUT)")
    public void successfulUpdateUserTest() {
        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                USERNAMEHC,
                testData.updatedFirstName,
                testData.updatedLastName,
                testData.updatedEmail
        );

        UserResponseModel response = given(updateUserRequestSpec)
                .header("Authorization", "Bearer " + accessToken)
                .body(updateData)
                .when()
                .put("/users/me/")
                .then()
                .spec(successfulUpdateUserResponseSpec)
                .extract().as(UserResponseModel.class);

        assertThat(response.username()).isEqualTo(USERNAMEHC);
        assertThat(response.firstName()).isEqualTo(testData.updatedFirstName);
        assertThat(response.lastName()).isEqualTo(testData.updatedLastName);
        assertThat(response.email()).isEqualTo(testData.updatedEmail);
    }
    @Test
    @DisplayName("Ошибка обновления профиля при недопустимом символе в username")
    public void updateUserWithInvalidUsernameCharTest() {
        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                USERNAMEHC+"#",
                testData.updatedFirstName,
                testData.updatedLastName,
                testData.updatedEmail
        );

        UserErrorResponseModel errorResponse = given(updateUserRequestSpec)
                .header("Authorization", "Bearer " + accessToken)
                .body(updateData)
                .when()
                .put("/users/me/")
                .then()
                .spec(badRequestUserResponseSpec)
                .extract().as(UserErrorResponseModel.class);

        assertThat(errorResponse.username()).containsExactly(
                "Enter a valid username. This value may contain only letters, numbers, and @/./+/-/_ characters.");
        assertThat(errorResponse.firstName()).isNull();
        assertThat(errorResponse.lastName()).isNull();
        assertThat(errorResponse.email()).isNull();
    }

    @Test
    @DisplayName("Успешное обновление профиля пользователя (PUT)")
    public void successfulUpdateUserMaxSymbolsTest() {

        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                maxLeght150Symbols,
                maxLeght150Symbols,
                maxLeght150Symbols,
                maxLeght254Symbols
        );

        UserResponseModel response = given(updateUserRequestSpec)
                .header("Authorization", "Bearer " + accessToken)
                .body(updateData)
                .when()
                .put("/users/me/")
                .then()
                .spec(successfulUpdateUserResponseSpec)
                .extract().as(UserResponseModel.class);

        assertThat(response.username()).isEqualTo(maxLeght150Symbols);
        assertThat(response.firstName()).isEqualTo(maxLeght150Symbols);
        assertThat(response.lastName()).isEqualTo(maxLeght150Symbols);
        assertThat(response.email()).isEqualTo(maxLeght254Symbols);
    }
    @Test
    @DisplayName("Ошибка обновления профиля при превышении длины firstName")
    public void updateUserWithTooLongFirstNameTest() {
        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                USERNAMEHC,
                tooLong151Symbols,
                testData.updatedLastName,
                testData.updatedEmail
        );

        UserErrorResponseModel errorResponse = given(updateUserRequestSpec)
                .header("Authorization", "Bearer " + accessToken)
                .body(updateData)
                .when()
                .put("/users/me/")
                .then()
                .spec(badRequestUserResponseSpec)
                .extract().as(UserErrorResponseModel.class);

        assertThat(errorResponse.firstName()).containsExactly(
                "Ensure this field has no more than 150 characters.");
        assertThat(errorResponse.username()).isNull();
        assertThat(errorResponse.lastName()).isNull();
        assertThat(errorResponse.email()).isNull();
    }
    @Test
    @DisplayName("Ошибка обновления профиля при превышении длины lastName")
    public void updateUserWithTooLongLastNameTest() {
        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                USERNAMEHC,
                testData.updatedFirstName,
                tooLong151Symbols,
                testData.updatedEmail
        );

        UserErrorResponseModel errorResponse = given(updateUserRequestSpec)
                .header("Authorization", "Bearer " + accessToken)
                .body(updateData)
                .when()
                .put("/users/me/")
                .then()
                .spec(badRequestUserResponseSpec)
                .extract().as(UserErrorResponseModel.class);

        assertThat(errorResponse.lastName()).containsExactly(
                "Ensure this field has no more than 150 characters.");
        assertThat(errorResponse.username()).isNull();
        assertThat(errorResponse.firstName()).isNull();
        assertThat(errorResponse.email()).isNull();
    }
    @Test
    @DisplayName("Ошибка обновления профиля при превышении длины email")
    public void updateUserWithTooLongEmailTest() {
        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                USERNAMEHC,
                testData.updatedFirstName,
                testData.updatedLastName,
                tooLong255Symbols
        );

        UserErrorResponseModel errorResponse = given(updateUserRequestSpec)
                .header("Authorization", "Bearer " + accessToken)
                .body(updateData)
                .when()
                .put("/users/me/")
                .then()
                .spec(badRequestUserResponseSpec)
                .extract().as(UserErrorResponseModel.class);

        assertThat(errorResponse.email()).containsExactly(
                "Ensure this field has no more than 254 characters.");
        assertThat(errorResponse.username()).isNull();
        assertThat(errorResponse.firstName()).isNull();
        assertThat(errorResponse.lastName()).isNull();
    }
    @Test
    @DisplayName("Успешное обновление профиля пользователя (PUT)")
    public void successfulUpdateUserPatchMaxSymbolsTest() {

        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                maxLeght150Symbols,
                maxLeght150Symbols,
                maxLeght150Symbols,
                maxLeght254Symbols
        );

        UserResponseModel response = given(updateUserRequestSpec)
                .header("Authorization", "Bearer " + accessToken)
                .body(updateData)
                .when()
                .put("/users/me/")
                .then()
                .spec(successfulUpdateUserResponseSpec)
                .extract().as(UserResponseModel.class);

        assertThat(response.username()).isEqualTo(maxLeght150Symbols);
        assertThat(response.firstName()).isEqualTo(maxLeght150Symbols);
        assertThat(response.lastName()).isEqualTo(maxLeght150Symbols);
        assertThat(response.email()).isEqualTo(maxLeght254Symbols);
    }
    @Test
    @DisplayName("Успешное обновление профиля пользователя (PATCH)")
    public void successfulPatchUserTest() {
        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                USERNAMEHC,
                testData.updatedFirstName,
                testData.updatedLastName,
                testData.updatedEmail
        );

        UserResponseModel response = given(updateUserRequestSpec)
                .header("Authorization", "Bearer " + accessToken)
                .body(updateData)
                .when()
                .patch("/users/me/")
                .then()
                .spec(successfulUpdateUserResponseSpec)
                .extract().as(UserResponseModel.class);

        assertThat(response.username()).isEqualTo(USERNAMEHC);
        assertThat(response.firstName()).isEqualTo(testData.updatedFirstName);
        assertThat(response.lastName()).isEqualTo(testData.updatedLastName);
        assertThat(response.email()).isEqualTo(testData.updatedEmail);
    }
    @Test
    @DisplayName("Ошибка обновления профиля (PATCH) при недопустимом символе в username")
    public void patchUserWithInvalidUsernameCharTest() {
        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                "autotestdarksome#",
                testData.updatedFirstName,
                testData.updatedLastName,
                testData.updatedEmail
        );

        UserErrorResponseModel errorResponse = given(updateUserRequestSpec)
                .header("Authorization", "Bearer " + accessToken)
                .body(updateData)
                .when()
                .patch("/users/me/")
                .then()
                .spec(badRequestUserResponseSpec)
                .extract().as(UserErrorResponseModel.class);

        assertThat(errorResponse.username()).containsExactly(
                "Enter a valid username. This value may contain only letters, numbers, and @/./+/-/_ characters.");
        assertThat(errorResponse.firstName()).isNull();
        assertThat(errorResponse.lastName()).isNull();
        assertThat(errorResponse.email()).isNull();
    }

    @Test
    @DisplayName("Ошибка обновления профиля (PATCH) при превышении длины firstName")
    public void patchUserWithTooLongFirstNameTest() {
        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                USERNAMEHC,
                tooLong151Symbols,
                testData.updatedLastName,
                testData.updatedEmail
        );

        UserErrorResponseModel errorResponse = given(updateUserRequestSpec)
                .header("Authorization", "Bearer " + accessToken)
                .body(updateData)
                .when()
                .patch("/users/me/")
                .then()
                .spec(badRequestUserResponseSpec)
                .extract().as(UserErrorResponseModel.class);

        assertThat(errorResponse.firstName()).containsExactly(
                "Ensure this field has no more than 150 characters.");
        assertThat(errorResponse.username()).isNull();
        assertThat(errorResponse.lastName()).isNull();
        assertThat(errorResponse.email()).isNull();
    }

    @Test
    @DisplayName("Ошибка обновления профиля (PATCH) при превышении длины lastName")
    public void patchUserWithTooLongLastNameTest() {
        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                USERNAMEHC,
                testData.updatedFirstName,
                tooLong151Symbols,
                testData.updatedEmail
        );

        UserErrorResponseModel errorResponse = given(updateUserRequestSpec)
                .header("Authorization", "Bearer " + accessToken)
                .body(updateData)
                .when()
                .patch("/users/me/")
                .then()
                .spec(badRequestUserResponseSpec)
                .extract().as(UserErrorResponseModel.class);

        assertThat(errorResponse.lastName()).containsExactly(
                "Ensure this field has no more than 150 characters.");
        assertThat(errorResponse.username()).isNull();
        assertThat(errorResponse.firstName()).isNull();
        assertThat(errorResponse.email()).isNull();
    }
    @Test
    @DisplayName("Ошибка обновления профиля (PATCH) при превышении длины email")
    public void patchUserWithTooLongEmailTest() {
        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                USERNAMEHC,
                testData.updatedFirstName,
                testData.updatedLastName,
                tooLong255Symbols
        );

        UserErrorResponseModel errorResponse = given(updateUserRequestSpec)
                .header("Authorization", "Bearer " + accessToken)
                .body(updateData)
                .when()
                .patch("/users/me/")
                .then()
                .spec(badRequestUserResponseSpec)
                .extract().as(UserErrorResponseModel.class);

        assertThat(errorResponse.email()).containsExactly(
                "Ensure this field has no more than 254 characters.");
        assertThat(errorResponse.username()).isNull();
        assertThat(errorResponse.firstName()).isNull();
        assertThat(errorResponse.lastName()).isNull();
    }
    @Test
    @DisplayName("Ошибка обновления профиля без Authorization")
    public void updateUserWithoutAuthTest() {
        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                USERNAMEHC,
                testData.updatedFirstName,
                testData.updatedLastName,
                testData.updatedEmail
        );

        UserDetailErrorResponseModel errorResponse = given(updateUserRequestSpec)
                .body(updateData)
                .when()
                .put("/users/me/")
                .then()
                .spec(unauthorizedUserResponseSpec)
                .extract().as(UserDetailErrorResponseModel.class);

        assertThat(errorResponse.detail())
                .isEqualTo("Authentication credentials were not provided.");
    }
    @Test
    @DisplayName("Ошибка обновления профиля (PATCH) без Authorization")
    public void patchUserWithoutAuthTest() {
        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                USERNAMEHC,
                testData.updatedFirstName,
                testData.updatedLastName,
                testData.updatedEmail
        );

        UserDetailErrorResponseModel errorResponse = given(updateUserRequestSpec)
                .body(updateData)
                .when()
                .patch("/users/me/")
                .then()
                .spec(unauthorizedUserResponseSpec)
                .extract().as(UserDetailErrorResponseModel.class);

        assertThat(errorResponse.detail())
                .isEqualTo("Authentication credentials were not provided.");
    }
}

