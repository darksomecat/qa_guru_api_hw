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
import static api.LoginApiClient.login;
import static api.UserApiClient.patchUser;
import static api.UserApiClient.patchUserWithoutAuth;
import static api.UserApiClient.updateUser;
import static api.UserApiClient.updateUserWithoutAuth;
import static api.RegistrationApiClient.register;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static io.qameta.allure.Allure.step;
import static specs.LoginSpec.successfulLoginResponseSpec;
import static specs.UserSpec.*;
import static tests.testData.TestData.*;

public class UpdateUserTests extends TestBase {

    private String accessToken;

    @BeforeEach
    public void auth() {
        step("Регистрация тестового пользователя и получение access-токена", () -> {
            RegistrationBodyModel data = new RegistrationBodyModel(USERNAMEHC, PASSWORDHC);

            register(data)
                    .statusCode(anyOf(
                            is(201),
                            is(400) ));

            LoginBodyModel loginData = new LoginBodyModel(USERNAMEHC, PASSWORDHC);

            accessToken = login(loginData)
                    .spec(successfulLoginResponseSpec)
                    .extract().path("access");
        });
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

        UserResponseModel response = step("Обновление профиля пользователя методом PUT", () -> {
            return updateUser(updateData, accessToken)
                    .spec(successfulUpdateUserResponseSpec)
                    .extract().as(UserResponseModel.class);
        });

        step("Проверка обновлённых данных профиля", () -> {
            assertThat(response.username()).isEqualTo(USERNAMEHC);
            assertThat(response.firstName()).isEqualTo(testData.updatedFirstName);
            assertThat(response.lastName()).isEqualTo(testData.updatedLastName);
            assertThat(response.email()).isEqualTo(testData.updatedEmail);
        });
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

        UserErrorResponseModel errorResponse = step("Обновление профиля с недопустимым символом в username #", () -> {
            return updateUser(updateData, accessToken)
                    .spec(badRequestUserResponseSpec)
                    .extract().as(UserErrorResponseModel.class);
        });

        step("Проверка ответа (400) и сообщения о недопустимом username", () -> {
            assertThat(errorResponse.username()).containsExactly(
                    "Enter a valid username. This value may contain only letters, numbers, and @/./+/-/_ characters.");
            assertThat(errorResponse.firstName()).isNull();
            assertThat(errorResponse.lastName()).isNull();
            assertThat(errorResponse.email()).isNull();
        });
    }

    @Test
    @DisplayName("Успешное обновление профиля пользователя (PUT)")
    public void successfulUpdateUserMaxSymbolsTest() {
        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                testData.maxLeght150Symbols,
                testData.maxLeght150Symbols,
                testData.maxLeght150Symbols,
                testData.maxLeght254Symbols
        );

        UserResponseModel response = step("Обновление профиля методом PUT с максимальной длиной полей", () -> {
            return updateUser(updateData, accessToken)
                    .spec(successfulUpdateUserResponseSpec)
                    .extract().as(UserResponseModel.class);
        });

        step("Проверка обновлённых данных профиля", () -> {
            assertThat(response.username()).isEqualTo(testData.maxLeght150Symbols);
            assertThat(response.firstName()).isEqualTo(testData.maxLeght150Symbols);
            assertThat(response.lastName()).isEqualTo(testData.maxLeght150Symbols);
            assertThat(response.email()).isEqualTo(testData.maxLeght254Symbols);
        });
    }
    @Test
    @DisplayName("Ошибка обновления профиля при превышении длины firstName")
    public void updateUserWithTooLongFirstNameTest() {
        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                USERNAMEHC,
                testData.tooLong151Symbols,
                testData.updatedLastName,
                testData.updatedEmail
        );

        UserErrorResponseModel errorResponse = step("Обновление профиля с firstName длиной 151 символ", () -> {
            return updateUser(updateData, accessToken)
                    .spec(badRequestUserResponseSpec)
                    .extract().as(UserErrorResponseModel.class);
        });

        step("Проверка ответа (400) и сообщения о превышении длины firstName", () -> {
            assertThat(errorResponse.firstName()).containsExactly(
                    "Ensure this field has no more than 150 characters.");
            assertThat(errorResponse.username()).isNull();
            assertThat(errorResponse.lastName()).isNull();
            assertThat(errorResponse.email()).isNull();
        });
    }
    @Test
    @DisplayName("Ошибка обновления профиля при превышении длины lastName")
    public void updateUserWithTooLongLastNameTest() {
        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                USERNAMEHC,
                testData.updatedFirstName,
                testData.tooLong151Symbols,
                testData.updatedEmail
        );

        UserErrorResponseModel errorResponse = step("Обновление профиля с lastName длиной 151 символ", () -> {
            return updateUser(updateData, accessToken)
                    .spec(badRequestUserResponseSpec)
                    .extract().as(UserErrorResponseModel.class);
        });

        step("Проверка ответа (400) и сообщения о превышении длины lastName", () -> {
            assertThat(errorResponse.lastName()).containsExactly(
                    "Ensure this field has no more than 150 characters.");
            assertThat(errorResponse.username()).isNull();
            assertThat(errorResponse.firstName()).isNull();
            assertThat(errorResponse.email()).isNull();
        });
    }
    @Test
    @DisplayName("Ошибка обновления профиля при превышении длины email")
    public void updateUserWithTooLongEmailTest() {
        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                USERNAMEHC,
                testData.updatedFirstName,
                testData.updatedLastName,
                testData.tooLong255Symbols
        );

        UserErrorResponseModel errorResponse = step("Обновление профиля с email длиной 255 символов", () -> {
            return updateUser(updateData, accessToken)
                    .spec(badRequestUserResponseSpec)
                    .extract().as(UserErrorResponseModel.class);
        });

        step("Проверка ответа (400) и сообщения о превышении длины email", () -> {
            assertThat(errorResponse.email()).containsExactly(
                    "Ensure this field has no more than 254 characters.");
            assertThat(errorResponse.username()).isNull();
            assertThat(errorResponse.firstName()).isNull();
            assertThat(errorResponse.lastName()).isNull();
        });
    }
    @Test
    @DisplayName("Успешное обновление профиля пользователя (PATCH)")
    public void successfulUpdateUserPatchMaxSymbolsTest() {
        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                testData.maxLeght150Symbols,
                testData.maxLeght150Symbols,
                testData.maxLeght150Symbols,
                testData.maxLeght254Symbols
        );

        UserResponseModel response = step("Обновление профиля методом PATCH с максимальной длиной полей", () -> {
            return patchUser(updateData, accessToken)
                    .spec(successfulUpdateUserResponseSpec)
                    .extract().as(UserResponseModel.class);
        });

        step("Проверка обновлённых данных профиля", () -> {
            assertThat(response.username()).isEqualTo(testData.maxLeght150Symbols);
            assertThat(response.firstName()).isEqualTo(testData.maxLeght150Symbols);
            assertThat(response.lastName()).isEqualTo(testData.maxLeght150Symbols);
            assertThat(response.email()).isEqualTo(testData.maxLeght254Symbols);
        });
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

        UserResponseModel response = step("Обновление профиля пользователя методом PATCH", () -> {
            return patchUser(updateData, accessToken)
                    .spec(successfulUpdateUserResponseSpec)
                    .extract().as(UserResponseModel.class);
        });

        step("Проверка обновлённых данных профиля", () -> {
            assertThat(response.username()).isEqualTo(USERNAMEHC);
            assertThat(response.firstName()).isEqualTo(testData.updatedFirstName);
            assertThat(response.lastName()).isEqualTo(testData.updatedLastName);
            assertThat(response.email()).isEqualTo(testData.updatedEmail);
        });
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

        UserErrorResponseModel errorResponse = step("Обновление профиля методом PATCH с недопустимым символом в username", () -> {
            return patchUser(updateData, accessToken)
                    .spec(badRequestUserResponseSpec)
                    .extract().as(UserErrorResponseModel.class);
        });

        step("Проверка ответа (400) и сообщения о недопустимом username", () -> {
            assertThat(errorResponse.username()).containsExactly(
                    "Enter a valid username. This value may contain only letters, numbers, and @/./+/-/_ characters.");
            assertThat(errorResponse.firstName()).isNull();
            assertThat(errorResponse.lastName()).isNull();
            assertThat(errorResponse.email()).isNull();
        });
    }

    @Test
    @DisplayName("Ошибка обновления профиля (PATCH) при превышении длины firstName")
    public void patchUserWithTooLongFirstNameTest() {
        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                USERNAMEHC,
                testData.tooLong151Symbols,
                testData.updatedLastName,
                testData.updatedEmail
        );

        UserErrorResponseModel errorResponse = step("Обновление профиля методом PATCH с firstName длиной 151 символ", () -> {
            return patchUser(updateData, accessToken)
                    .spec(badRequestUserResponseSpec)
                    .extract().as(UserErrorResponseModel.class);
        });

        step("Проверка ответа (400) и сообщения о превышении длины firstName", () -> {
            assertThat(errorResponse.firstName()).containsExactly(
                    "Ensure this field has no more than 150 characters.");
            assertThat(errorResponse.username()).isNull();
            assertThat(errorResponse.lastName()).isNull();
            assertThat(errorResponse.email()).isNull();
        });
    }

    @Test
    @DisplayName("Ошибка обновления профиля (PATCH) при превышении длины lastName")
    public void patchUserWithTooLongLastNameTest() {
        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                USERNAMEHC,
                testData.updatedFirstName,
                testData.tooLong151Symbols,
                testData.updatedEmail
        );

        UserErrorResponseModel errorResponse = step("Обновление профиля методом PATCH с lastName длиной 151 символ", () -> {
            return patchUser(updateData, accessToken)
                    .spec(badRequestUserResponseSpec)
                    .extract().as(UserErrorResponseModel.class);
        });

        step("Проверка ответа (400) и сообщения о превышении длины lastName", () -> {
            assertThat(errorResponse.lastName()).containsExactly(
                    "Ensure this field has no more than 150 characters.");
            assertThat(errorResponse.username()).isNull();
            assertThat(errorResponse.firstName()).isNull();
            assertThat(errorResponse.email()).isNull();
        });
    }
    @Test
    @DisplayName("Ошибка обновления профиля (PATCH) при превышении длины email")
    public void patchUserWithTooLongEmailTest() {
        UpdateUserBodyModel updateData = new UpdateUserBodyModel(
                USERNAMEHC,
                testData.updatedFirstName,
                testData.updatedLastName,
                testData.tooLong255Symbols
        );

        UserErrorResponseModel errorResponse = step("Обновление профиля методом PATCH с email длиной 255 символов", () -> {
            return patchUser(updateData, accessToken)
                    .spec(badRequestUserResponseSpec)
                    .extract().as(UserErrorResponseModel.class);
        });

        step("Проверка ответа (400) и сообщения о превышении длины email", () -> {
            assertThat(errorResponse.email()).containsExactly(
                    "Ensure this field has no more than 254 characters.");
            assertThat(errorResponse.username()).isNull();
            assertThat(errorResponse.firstName()).isNull();
            assertThat(errorResponse.lastName()).isNull();
        });
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

        UserDetailErrorResponseModel errorResponse = step("Обновление профиля методом PUT без Authorization", () -> {
            return updateUserWithoutAuth(updateData)
                    .spec(unauthorizedUserResponseSpec)
                    .extract().as(UserDetailErrorResponseModel.class);
        });

        step("Проверка ответа (401) и сообщения об отсутствии авторизации", () ->
                assertThat(errorResponse.detail())
                        .isEqualTo("Authentication credentials were not provided."));
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

        UserDetailErrorResponseModel errorResponse = step("Обновление профиля методом PATCH без Authorization", () -> {
            return patchUserWithoutAuth(updateData)
                    .spec(unauthorizedUserResponseSpec)
                    .extract().as(UserDetailErrorResponseModel.class);
        });

        step("Проверка ответа (401) и сообщения об отсутствии авторизации", () ->
                assertThat(errorResponse.detail())
                        .isEqualTo("Authentication credentials were not provided."));
    }
}
