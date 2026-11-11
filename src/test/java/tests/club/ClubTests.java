package tests.club;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.club.ClubErrorResponseModel;
import models.club.ClubResponseModel;
import models.club.CreateClubBodyModel;
import models.login.LoginBodyModel;
import models.registration.RegistrationBodyModel;
import models.user.UserDetailErrorResponseModel;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tests.TestBase;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static api.ClubApiClient.createClub;
import static api.ClubApiClient.createClubWithEmptyBody;
import static api.ClubApiClient.createClubWithInvalidDataTypes;
import static api.ClubApiClient.createClubWithRawBody;
import static api.ClubApiClient.createClubWithoutAuth;
import static api.ClubApiClient.retrieveClub;
import static api.LoginApiClient.login;
import static api.RegistrationApiClient.register;
import static io.qameta.allure.Allure.step;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static specs.ClubSpec.CLUB_RESPONSE_SCHEMA;
import static specs.ClubSpec.badRequestClubResponseSpec;
import static specs.ClubSpec.successfulCreateClubResponseSpec;
import static specs.ClubSpec.successfulRetrieveClubResponseSpec;
import static specs.ClubSpec.unauthorizedClubResponseSpec;
import static specs.LoginSpec.successfulLoginResponseSpec;
import static tests.testData.TestData.PASSWORDHC;
import static tests.testData.TestData.USERNAMEHC;
import static tests.testData.TestData.clubRequiredFields;
import static tests.testData.TestData.invalidClubDataTypesJson;
import static utils.GeneretedUtils.getUserIdFromToken;

public class ClubTests extends TestBase {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static String accessToken;
    private static Integer userId;

    @BeforeAll
    public static void auth() {
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

            userId = Integer.valueOf(getUserIdFromToken(accessToken));
        });
    }

    @Test
    @DisplayName("Успешное создание клуба")
    public void successfulCreateClubTest() throws JsonProcessingException {
        CreateClubBodyModel createData = new CreateClubBodyModel(
                testData.clubBookTitle,
                testData.clubBookAuthors,
                testData.clubPublicationYear,
                testData.clubDescription,
                testData.clubTelegramChatLink
        );

        String responseBody = step("Создание клуба методом POST", () ->
                createClub(createData, accessToken)
                        .spec(successfulCreateClubResponseSpec)
                        .extract().asString());

        step("Проверка соответствия ответа схеме успешного создания клуба", () ->
                MatcherAssert.assertThat(responseBody,
                        matchesJsonSchemaInClasspath(CLUB_RESPONSE_SCHEMA)));

        ClubResponseModel response = objectMapper.readValue(responseBody, ClubResponseModel.class);

        step("Проверка данных созданного клуба", () -> {
            assertThat(response.id()).isNotNull();
            assertThat(response.bookTitle()).isEqualTo(testData.clubBookTitle);
            assertThat(response.bookAuthors()).isEqualTo(testData.clubBookAuthors);
            assertThat(response.publicationYear()).isEqualTo(testData.clubPublicationYear);
            assertThat(response.description()).isEqualTo(testData.clubDescription);
            assertThat(response.telegramChatLink()).isEqualTo(testData.clubTelegramChatLink);
            assertThat(response.created()).isNotEmpty();
            assertThat(response.modified()).isNull();
        });

        String savedClubBody = step("Получение созданного клуба по id", () ->
                retrieveClub(response.id())
                        .spec(successfulRetrieveClubResponseSpec)
                        .extract().asString());

        step("Проверка соответствия ответа схеме клуба", () ->
                MatcherAssert.assertThat(savedClubBody,
                        matchesJsonSchemaInClasspath(CLUB_RESPONSE_SCHEMA)));

        ClubResponseModel savedClub = objectMapper.readValue(savedClubBody, ClubResponseModel.class);

        step("Проверка сохранения клуба на сервере", () -> {
            assertThat(savedClub.id()).isEqualTo(response.id());
            assertThat(savedClub.bookTitle()).isEqualTo(testData.clubBookTitle);
            assertThat(savedClub.bookAuthors()).isEqualTo(testData.clubBookAuthors);
            assertThat(savedClub.publicationYear()).isEqualTo(testData.clubPublicationYear);
            assertThat(savedClub.description()).isEqualTo(testData.clubDescription);
            assertThat(savedClub.telegramChatLink()).isEqualTo(testData.clubTelegramChatLink);
            assertThat(savedClub.owner()).isEqualTo(userId);
        });
    }

    @Test
    @DisplayName("Создатель клуба становится его владельцем и участником")
    public void createClubOwnerMembersTest() {
        CreateClubBodyModel createData = new CreateClubBodyModel(
                testData.clubBookTitle,
                testData.clubBookAuthors,
                testData.clubPublicationYear,
                testData.clubDescription,
                testData.clubTelegramChatLink
        );

        ClubResponseModel response = step("Создание клуба методом POST", () ->
                createClub(createData, accessToken)
                        .spec(successfulCreateClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Проверка владельца, участников и отзывов созданного клуба", () -> {
            assertThat(response.owner()).isEqualTo(userId);
            assertThat(response.members()).containsExactly(userId);
            assertThat(response.reviews()).isEmpty();
        });
    }

    @Test
    @DisplayName("Создание клуба при максимальной длине bookTitle и bookAuthors в 255 символов")
    public void createClubMaxSymbolsTest() {
        CreateClubBodyModel createData = new CreateClubBodyModel(
                testData.maxLeght255Symbols,
                testData.maxLeght255Symbols,
                testData.clubPublicationYear,
                testData.clubDescription,
                testData.clubTelegramChatLink
        );

        ClubResponseModel response = step("Создание клуба методом POST с максимальной длиной полей", () ->
                createClub(createData, accessToken)
                        .spec(successfulCreateClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Проверка ответа (201) и сохранённых значений полей", () -> {
            assertThat(response.bookTitle()).isEqualTo(testData.maxLeght255Symbols);
            assertThat(response.bookAuthors()).isEqualTo(testData.maxLeght255Symbols);
        });
    }

    @Test
    @DisplayName("Ошибка создания клуба при превышении длины bookTitle и bookAuthors")
    public void createClubWithTooLongSymbolsTest() {
        CreateClubBodyModel createData = new CreateClubBodyModel(
                testData.tooLong256Symbols,
                testData.tooLong256Symbols,
                testData.clubPublicationYear,
                testData.clubDescription,
                testData.clubTelegramChatLink
        );

        ClubErrorResponseModel errorResponse = step("Создание клуба методом POST с полями длиной 256 символов", () ->
                createClub(createData, accessToken)
                        .spec(badRequestClubResponseSpec)
                        .extract().as(ClubErrorResponseModel.class));

        step("Проверка ответа (400) и сообщений о превышении длины", () -> {
            assertThat(errorResponse.bookTitle())
                    .containsExactly("Ensure this field has no more than 255 characters.");
            assertThat(errorResponse.bookAuthors())
                    .containsExactly("Ensure this field has no more than 255 characters.");
            assertThat(errorResponse.publicationYear()).isNull();
            assertThat(errorResponse.description()).isNull();
            assertThat(errorResponse.telegramChatLink()).isNull();
        });
    }

    @Test
    @DisplayName("Создание клуба при максимальной длине telegramChatLink в 200 символов")
    public void createClubMaxTelegramChatLinkTest() {
        CreateClubBodyModel createData = new CreateClubBodyModel(
                testData.clubBookTitle,
                testData.clubBookAuthors,
                testData.clubPublicationYear,
                testData.clubDescription,
                testData.maxLeght200Symbols
        );

        ClubResponseModel response = step("Создание клуба методом POST с telegramChatLink длиной 200 символов", () ->
                createClub(createData, accessToken)
                        .spec(successfulCreateClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Проверка ответа (201) и сохранённой ссылки", () ->
                assertThat(response.telegramChatLink()).isEqualTo(testData.maxLeght200Symbols));
    }

    @Test
    @DisplayName("Ошибка создания клуба при превышении длины telegramChatLink")
    public void createClubWithTooLongTelegramChatLinkTest() {
        CreateClubBodyModel createData = new CreateClubBodyModel(
                testData.clubBookTitle,
                testData.clubBookAuthors,
                testData.clubPublicationYear,
                testData.clubDescription,
                testData.tooLong201Symbols
        );

        ClubErrorResponseModel errorResponse = step("Создание клуба методом POST с telegramChatLink длиной 201 символ", () ->
                createClub(createData, accessToken)
                        .spec(badRequestClubResponseSpec)
                        .extract().as(ClubErrorResponseModel.class));

        step("Проверка ответа (400) и сообщения о превышении длины ссылки", () -> {
            assertThat(errorResponse.telegramChatLink())
                    .containsExactly("Ensure this field has no more than 200 characters.");
            assertThat(errorResponse.bookTitle()).isNull();
            assertThat(errorResponse.bookAuthors()).isNull();
            assertThat(errorResponse.publicationYear()).isNull();
            assertThat(errorResponse.description()).isNull();
        });
    }

    @Test
    @DisplayName("Ошибка создания клуба с уже существующим bookTitle")
    public void createClubWithExistingBookTitleTest() {
        Map<String, Object> clubData = clubRequiredFields();

        step("Создание клуба методом POST", () ->
                createClubWithRawBody(clubData, accessToken)
                        .spec(successfulCreateClubResponseSpec));

        ClubErrorResponseModel errorResponse = step("Повторное создание клуба с тем же bookTitle", () ->
                createClubWithRawBody(clubData, accessToken)
                        .spec(badRequestClubResponseSpec)
                        .extract().as(ClubErrorResponseModel.class));

        step("Проверка ответа (400) и сообщения о существующем клубе", () -> {
            assertThat(errorResponse.bookTitle())
                    .containsExactly("Book Club with this Book Title already exists.");
            assertThat(errorResponse.bookAuthors()).isNull();
            assertThat(errorResponse.publicationYear()).isNull();
            assertThat(errorResponse.description()).isNull();
            assertThat(errorResponse.telegramChatLink()).isNull();
        });
    }

    @Test
    @DisplayName("Ошибка создания клуба при отправке пустого тела запроса")
    public void createClubWithEmptyBodyTest() {
        ClubErrorResponseModel errorResponse = step("Отправка запроса создания клуба с пустым телом", () ->
                createClubWithEmptyBody(accessToken)
                        .spec(badRequestClubResponseSpec)
                        .extract().as(ClubErrorResponseModel.class));

        step("Проверка ответа (400) и сообщений об обязательных полях", () -> {
            assertThat(errorResponse.bookTitle()).containsExactly("This field is required.");
            assertThat(errorResponse.bookAuthors()).containsExactly("This field is required.");
            assertThat(errorResponse.publicationYear()).containsExactly("This field is required.");
            assertThat(errorResponse.description()).containsExactly("This field is required.");
            assertThat(errorResponse.telegramChatLink()).containsExactly("This field is required.");
        });
    }

    @ParameterizedTest(name = "Ошибка создания клуба без обязательного поля {0}")
    @MethodSource("requiredClubFields")
    @DisplayName("Ошибка создания клуба при отсутствии обязательного поля")
    public void createClubWithoutRequiredFieldTest(String requiredField) {
        Map<String, Object> clubData = clubRequiredFields();
        clubData.remove(requiredField);

        ClubErrorResponseModel errorResponse = step("Создание клуба методом POST без поля " + requiredField, () ->
                createClubWithRawBody(clubData, accessToken)
                        .spec(badRequestClubResponseSpec)
                        .extract().as(ClubErrorResponseModel.class));

        step("Проверка ответа (400) и сообщения об обязательном поле " + requiredField, () ->
                assertThat(requiredFieldErrors(errorResponse, requiredField))
                        .containsExactly("This field is required."));
    }

    private static Stream<String> requiredClubFields() {
        return Stream.of("bookTitle", "bookAuthors", "publicationYear", "description", "telegramChatLink");
    }

    private static List<String> requiredFieldErrors(ClubErrorResponseModel errorResponse, String field) {
        return switch (field) {
            case "bookTitle" -> errorResponse.bookTitle();
            case "bookAuthors" -> errorResponse.bookAuthors();
            case "publicationYear" -> errorResponse.publicationYear();
            case "description" -> errorResponse.description();
            default -> errorResponse.telegramChatLink();
        };
    }

    @Test
    @DisplayName("Ошибка создания клуба при передаче пустого bookTitle")
    public void createClubWithBlankBookTitleTest() {
        Map<String, Object> clubData = clubRequiredFields();
        clubData.put("bookTitle", "");

        ClubErrorResponseModel errorResponse = step("Отправка запроса создания клуба с пустым bookTitle", () ->
                createClubWithRawBody(clubData, accessToken)
                        .spec(badRequestClubResponseSpec)
                        .extract().as(ClubErrorResponseModel.class));

        step("Проверка ответа (400) и сообщения о пустом bookTitle", () ->
                assertThat(errorResponse.bookTitle()).containsExactly("This field may not be blank."));
    }

    @Test
    @DisplayName("Ошибка создания клуба при передаче пустого bookAuthors")
    public void createClubWithBlankBookAuthorsTest() {
        Map<String, Object> clubData = clubRequiredFields();
        clubData.put("bookAuthors", "");

        ClubErrorResponseModel errorResponse = step("Отправка запроса создания клуба с пустым bookAuthors", () ->
                createClubWithRawBody(clubData, accessToken)
                        .spec(badRequestClubResponseSpec)
                        .extract().as(ClubErrorResponseModel.class));

        step("Проверка ответа (400) и сообщения о пустом bookAuthors", () ->
                assertThat(errorResponse.bookAuthors()).containsExactly("This field may not be blank."));
    }

    @Test
    @DisplayName("Ошибка создания клуба при передаче пустого telegramChatLink")
    public void createClubWithBlankTelegramChatLinkTest() {
        Map<String, Object> clubData = clubRequiredFields();
        clubData.put("telegramChatLink", "");

        ClubErrorResponseModel errorResponse = step("Отправка запроса создания клуба с пустым telegramChatLink", () ->
                createClubWithRawBody(clubData, accessToken)
                        .spec(badRequestClubResponseSpec)
                        .extract().as(ClubErrorResponseModel.class));

        step("Проверка ответа (400) и сообщения о пустом telegramChatLink", () ->
                assertThat(errorResponse.telegramChatLink()).containsExactly("This field may not be blank."));
    }

    @Test
    @DisplayName("Ошибка создания клуба при передаче пустого description")
    public void createClubWithBlankDescriptionTest() {
        Map<String, Object> clubData = clubRequiredFields();
        clubData.put("description", "");

        ClubErrorResponseModel errorResponse = step("Отправка запроса создания клуба с пустым description", () ->
                createClubWithRawBody(clubData, accessToken)
                        .spec(badRequestClubResponseSpec)
                        .extract().as(ClubErrorResponseModel.class));

        step("Проверка ответа (400) и сообщения о пустом description", () ->
                assertThat(errorResponse.description()).containsExactly("This field may not be blank."));
    }

    @Test
    @DisplayName("Ошибка создания клуба при передаче bookTitle из одних пробелов")
    public void createClubWithSpacesBookTitleTest() {
        Map<String, Object> clubData = clubRequiredFields();
        clubData.put("bookTitle", "   ");

        ClubErrorResponseModel errorResponse = step("Отправка запроса создания клуба с bookTitle из пробелов", () ->
                createClubWithRawBody(clubData, accessToken)
                        .spec(badRequestClubResponseSpec)
                        .extract().as(ClubErrorResponseModel.class));

        step("Проверка ответа (400) и сообщения о пустом bookTitle", () ->
                assertThat(errorResponse.bookTitle()).containsExactly("This field may not be blank."));
    }

    @Test
    @DisplayName("Очистка пробелов вокруг bookTitle при создании клуба")
    public void createClubWithTrimmedBookTitleTest() {
        Map<String, Object> clubData = clubRequiredFields();
        String bookTitle = (String) clubData.get("bookTitle");
        clubData.put("bookTitle", "   " + bookTitle + "   ");

        ClubResponseModel response = step("Отправка запроса создания клуба с пробелами вокруг bookTitle", () ->
                createClubWithRawBody(clubData, accessToken)
                        .spec(successfulCreateClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Проверка очистки пробелов вокруг названия клуба", () ->
                assertThat(response.bookTitle()).isEqualTo(bookTitle));
    }

    @Test
    @DisplayName("Ошибка создания клуба при передаче некорректных типов данных")
    public void createClubWithInvalidDataTypesTest() {
        ClubErrorResponseModel errorResponse = step("Отправка запроса создания клуба с некорректными типами данных", () ->
                createClubWithInvalidDataTypes(invalidClubDataTypesJson, accessToken)
                        .spec(badRequestClubResponseSpec)
                        .extract().as(ClubErrorResponseModel.class));

        step("Проверка ответа (400) и сообщений о неверных типах данных", () -> {
            assertThat(errorResponse.bookTitle()).containsExactly("Not a valid string.");
            assertThat(errorResponse.bookAuthors()).containsExactly("Not a valid string.");
            assertThat(errorResponse.publicationYear()).containsExactly("A valid integer is required.");
            assertThat(errorResponse.description()).containsExactly("Not a valid string.");
            assertThat(errorResponse.telegramChatLink()).containsExactly("Enter a valid URL.");
        });
    }

    @Test
    @DisplayName("Ошибка создания клуба без Authorization")
    public void createClubWithoutAuthTest() {
        CreateClubBodyModel createData = new CreateClubBodyModel(
                testData.clubBookTitle,
                testData.clubBookAuthors,
                testData.clubPublicationYear,
                testData.clubDescription,
                testData.clubTelegramChatLink
        );

        UserDetailErrorResponseModel errorResponse = step("Отправка запроса создания клуба без Authorization", () ->
                createClubWithoutAuth(createData)
                        .spec(unauthorizedClubResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка ответа (401) и сообщения об отсутствии авторизации", () ->
                assertThat(errorResponse.detail())
                        .isEqualTo("Authentication credentials were not provided."));
    }
}
