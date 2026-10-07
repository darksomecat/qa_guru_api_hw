package tests.registration;
import models.registration.RegistrationBodyModel;
import models.registration.RegistrationErrorResponseModel;
import models.registration.RegistrationResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tests.TestBase;




import static api.RegistrationApiClient.register;
import static api.RegistrationApiClient.registerWithError;
import static api.RegistrationApiClient.registerWithEmptyBodyError;
import static io.qameta.allure.Allure.step;
import static specs.RegistrationSpec.successfulRegistrationResponseSpec;
import static org.assertj.core.api.Assertions.assertThat;

public class RegistrationTests extends TestBase {

    @Test
    @DisplayName("Успешная регистрация нового пользователя")
    public void successfulRegistrationResponseTest() {
        RegistrationBodyModel data = new RegistrationBodyModel(testData.username, testData.password);

        RegistrationResponseModel registrationResponse = step("Регистрация нового пользователя", () ->
                register(data)
                        .spec(successfulRegistrationResponseSpec)
                        .extract().as(RegistrationResponseModel.class));

        step("Проверка данных зарегистрированного пользователя", () -> {
            assertThat(registrationResponse.username()).isEqualTo(data.username());
            assertThat(registrationResponse.id()).isNotNull();
            assertThat(registrationResponse.remoteAddr()).isNotEmpty();
        });
    }

    @Test
    @DisplayName("Ошибка регистрация: пользователь с таким именем уже существует")
    public void successfulRegistration400AlreadyExistsTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.username, testData.password);

        step("Регистрация нового пользователя", () ->
                register(registrationData)
                        .spec(successfulRegistrationResponseSpec));

        RegistrationErrorResponseModel errorResponse = step("Отправка запроса с уже существующим username", () ->
                registerWithError(registrationData));

        step("Проверка ответа (400) и сообщения о существующем пользователе", () ->
                assertThat(errorResponse.username()).containsExactly("A user with that username already exists."));
    }

    @Test
    @DisplayName("Ошибка регистрации при передаче пустого username")
    public void registrationWithBlankUsernameTest() {
        RegistrationBodyModel data = new RegistrationBodyModel("", testData.password);

        RegistrationErrorResponseModel errorResponse = step("Отправка запроса регистрации с пустым username", () ->
                registerWithError(data));

        step("Проверка ответа (400) и сообщения о пустом username", () ->
                assertThat(errorResponse.username()).containsExactly("This field may not be blank."));
    }
    @Test
    @DisplayName("Ошибка регистрации при передаче пустого password")
    public void registrationWithBlankPasswordTest() {
        RegistrationBodyModel data = new RegistrationBodyModel(testData.username, "");

        RegistrationErrorResponseModel errorResponse = step("Отправка запроса регистрации с пустым password", () ->
                registerWithError(data));

        step("Проверка ответа (400) и сообщения о пустом password", () ->
                assertThat(errorResponse.password()).containsExactly("This field may not be blank."));
    }

    @Test
    @DisplayName("Ошибка регистрации при отправке пустого тела запроса")
    public void registrationWithEmptyBodyTest() {
        RegistrationErrorResponseModel errorResponse = step("Отправка запроса регистрации с пустым телом", () ->
                registerWithEmptyBodyError());

        step("Проверка ответа (400) и сообщений об обязательных полях", () -> {
            assertThat(errorResponse.username()).containsExactly("This field is required.");
            assertThat(errorResponse.password()).containsExactly("This field is required.");
        });
    }

    @Test
    @DisplayName("Проверка очистки пробелов в username при регистрации")
    public void registrationWhitespaceTrimmingTest() {
        String usernameWithSpaces = "   " + testData.username + "   ";
        RegistrationBodyModel data = new RegistrationBodyModel(usernameWithSpaces, testData.password);

        RegistrationResponseModel registrationResponse = step("Регистрация с пробелами в username", () ->
                register(data)
                        .spec(successfulRegistrationResponseSpec)
                        .extract().as(RegistrationResponseModel.class));

        step("Проверка очистки пробелов и данных пользователя", () -> {
            assertThat(registrationResponse.username()).isEqualTo(testData.username);
            assertThat(registrationResponse.id()).isNotNull();
            assertThat(registrationResponse.remoteAddr()).isNotEmpty();
        });
    }
    @Test
    @DisplayName("Ошибка регистрации при превышении максимальной длины username в 150 символов")
    public void registrationWithTooLongUsernameTest() {
        RegistrationBodyModel data = new RegistrationBodyModel(testData.tooLong151Symbols, testData.password);

        RegistrationErrorResponseModel errorResponse = step("Отправка запроса регистрации с username длиной 151 символ", () ->
                registerWithError(data));

        step("Проверка ответа (400) и сообщения о превышении длины username", () ->
                assertThat(errorResponse.username()).containsExactly("Ensure this field has no more than 150 characters."));
    }
    @Test
    @DisplayName("Регистрация при максимальной длине username в 150 символов")
    public void registrationWithMaxUsernameTest() {
        RegistrationBodyModel data = new RegistrationBodyModel(testData.maxLeght150Symbols, testData.password);

        RegistrationResponseModel registrationResponse = step("Регистрация с username длиной 150 символов", () ->
                register(data)
                        .spec(successfulRegistrationResponseSpec)
                        .extract().as(RegistrationResponseModel.class));

        step("Проверка данных зарегистрированного пользователя", () -> {
            assertThat(registrationResponse.username()).isEqualTo(data.username());
            assertThat(registrationResponse.id()).isNotNull();
            assertThat(registrationResponse.remoteAddr()).isNotEmpty();
        });
    }
    @Test
    @DisplayName("Регистрация при максимальной длине password в 128 символов")
    public void registrationWithMaxPasswordTest() {
        RegistrationBodyModel data = new RegistrationBodyModel(testData.username, testData.maxPassword);

        RegistrationResponseModel registrationResponse = step("Регистрация с password длиной 128 символов", () ->
                register(data)
                        .spec(successfulRegistrationResponseSpec)
                        .extract().as(RegistrationResponseModel.class));

        step("Проверка данных зарегистрированного пользователя", () -> {
            assertThat(registrationResponse.username()).isEqualTo(data.username());
            assertThat(registrationResponse.id()).isNotNull();
            assertThat(registrationResponse.remoteAddr()).isNotEmpty();
        });
    }
    @Test
    @DisplayName("Ошибка регистрации при превышении максимальной длины password в 128 символов")
    public void registrationWithTooLongPasswordTest() {
        RegistrationBodyModel data = new RegistrationBodyModel(testData.username, testData.tooLongPassword);

        RegistrationErrorResponseModel errorResponse = step("Отправка запроса регистрации с password длиной 129 символов", () ->
                registerWithError(data));

        step("Проверка ответа (400) и сообщения о превышении длины password", () ->
                assertThat(errorResponse.password()).containsExactly("Ensure this field has no more than 128 characters."));
    }

}
