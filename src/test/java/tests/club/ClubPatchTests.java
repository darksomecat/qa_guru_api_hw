package tests.club;

import models.club.ClubErrorResponseModel;
import models.club.ClubResponseModel;
import models.login.LoginBodyModel;
import models.registration.RegistrationBodyModel;
import models.user.UserDetailErrorResponseModel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tests.TestBase;

import java.util.List;
import java.util.Map;

import static api.ClubApiClient.createClubWithRawBody;
import static api.ClubApiClient.patchClubWithRawBody;
import static api.ClubApiClient.patchClubWithoutAuth;
import static api.ClubApiClient.retrieveClub;
import static api.LoginApiClient.login;
import static api.RegistrationApiClient.register;
import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static specs.ClubSpec.badRequestClubResponseSpec;
import static specs.ClubSpec.forbiddenClubResponseSpec;
import static specs.ClubSpec.notFoundClubResponseSpec;
import static specs.ClubSpec.successfulRetrieveClubResponseSpec;
import static specs.ClubSpec.successfulUpdateClubResponseSpec;
import static specs.ClubSpec.unauthorizedClubResponseSpec;
import static specs.LoginSpec.successfulLoginResponseSpec;
import static tests.testData.TestData.AUTHENTICATION_MESSAGE;
import static tests.testData.TestData.CLUB_NOT_FOUND_MESSAGE;
import static tests.testData.TestData.NO_PERMISSION_MESSAGE;
import static tests.testData.TestData.PASSWORDHC;
import static tests.testData.TestData.USERNAMEHC;
import static tests.testData.TestData.newClubRequest;
import static tests.testData.TestData.patchedClubRequest;
import static tests.testData.TestData.patcherUserName;
import static tests.testData.TestData.searchableClubTitle;
import static utils.GeneretedUtils.getRandomHex;
import static utils.GeneretedUtils.getRandomString;
import static utils.GeneretedUtils.getUserIdFromToken;

public class ClubPatchTests extends TestBase {

    private static String accessToken;
    private static String otherAccessToken;
    private static Integer otherUserId;
    private static Integer clubId;
    private static ClubResponseModel createdClub;

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
        });

        step("Регистрация второго пользователя без прав на клуб", () -> {
            RegistrationBodyModel otherData = new RegistrationBodyModel(patcherUserName(), PASSWORDHC);

            register(otherData)
                    .statusCode(anyOf(
                            is(201),
                            is(400) ));

            LoginBodyModel otherLoginData = new LoginBodyModel(otherData.username(), PASSWORDHC);

            otherAccessToken = login(otherLoginData)
                    .spec(successfulLoginResponseSpec)
                    .extract().path("access");

            otherUserId = Integer.valueOf(getUserIdFromToken(otherAccessToken));
        });
    }

    @BeforeEach
    public void createTestClub() {
        step("Создание клуба для проверок частичного обновления", () ->
                createdClub = createClubWithRawBody(newClubRequest(searchableClubTitle()), accessToken)
                        .extract().as(ClubResponseModel.class));

        clubId = createdClub.id();
    }

    @Test
    @DisplayName("Успешное частичное обновление клуба методом PATCH")
    public void successfulPatchClubTest() {
        String updatedBookTitle = "Patched Club " + getRandomHex();

        ClubResponseModel response = step("Обновление названия клуба методом PATCH", () ->
                patchClubWithRawBody(clubId, patchedClubRequest(updatedBookTitle), accessToken)
                        .spec(successfulUpdateClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Проверка изменения названия и сохранности остальных полей", () -> {
            assertThat(response.id()).isEqualTo(clubId);
            assertThat(response.bookTitle()).isEqualTo(updatedBookTitle);
            assertThat(response.bookAuthors()).isEqualTo(createdClub.bookAuthors());
            assertThat(response.publicationYear()).isEqualTo(createdClub.publicationYear());
            assertThat(response.description()).isEqualTo(createdClub.description());
            assertThat(response.telegramChatLink()).isEqualTo(createdClub.telegramChatLink());
            assertThat(response.owner()).isEqualTo(createdClub.owner());
            assertThat(response.members()).isEqualTo(createdClub.members());
        });
    }

    @Test
    @DisplayName("Успешное частичное обновление нескольких полей клуба методом PATCH")
    public void successfulPatchClubSeveralFieldsTest() {
        Map<String, Object> patchData = patchedClubRequest("Patched " + getRandomHex());
        patchData.put("bookAuthors", getRandomString(255));
        patchData.put("description", "Patched description");
        patchData.put("telegramChatLink", testData.maxLeght200Symbols);

        ClubResponseModel response = step("Обновление нескольких полей клуба методом PATCH", () ->
                patchClubWithRawBody(clubId, patchData, accessToken)
                        .spec(successfulUpdateClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Проверка обновлённых и неизменённых полей", () -> {
            assertThat(response.bookTitle()).isEqualTo(patchData.get("bookTitle"));
            assertThat(response.bookAuthors()).isEqualTo(patchData.get("bookAuthors"));
            assertThat(response.publicationYear()).isEqualTo(createdClub.publicationYear());
            assertThat(response.description()).isEqualTo(patchData.get("description"));
            assertThat(response.telegramChatLink()).isEqualTo(testData.maxLeght200Symbols);
        });

        ClubResponseModel savedClub = step("Получение обновлённого клуба по id", () ->
                retrieveClub(clubId)
                        .spec(successfulRetrieveClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Проверка сохранения изменений на сервере", () -> {
            assertThat(savedClub.bookTitle()).isEqualTo(patchData.get("bookTitle"));
            assertThat(savedClub.description()).isEqualTo(patchData.get("description"));
        });
    }

    @Test
    @DisplayName("Частичное обновление клуба методом PATCH с пустым телом запроса")
    public void patchClubWithEmptyBodyTest() {
        ClubResponseModel response = step("Обновление клуба методом PATCH с пустым телом", () ->
                patchClubWithRawBody(clubId, Map.of(), accessToken)
                        .spec(successfulUpdateClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Проверка отсутствия изменений в клубе", () -> {
            assertThat(response.bookTitle()).isEqualTo(createdClub.bookTitle());
            assertThat(response.bookAuthors()).isEqualTo(createdClub.bookAuthors());
            assertThat(response.publicationYear()).isEqualTo(createdClub.publicationYear());
            assertThat(response.description()).isEqualTo(createdClub.description());
            assertThat(response.telegramChatLink()).isEqualTo(createdClub.telegramChatLink());
        });
    }

    @Test
    @DisplayName("Ошибка частичного обновления клуба методом PATCH при превышении длины полей")
    public void patchClubWithTooLongFieldsTest() {
        Map<String, Object> patchData = patchedClubRequest(testData.tooLong256Symbols);
        patchData.put("telegramChatLink", testData.tooLong201Symbols);

        ClubErrorResponseModel errorResponse = step("Обновление клуба методом PATCH с превышением длины полей", () ->
                patchClubWithRawBody(clubId, patchData, accessToken)
                        .spec(badRequestClubResponseSpec)
                        .extract().as(ClubErrorResponseModel.class));

        step("Проверка ответа (400) и сообщений о превышении длины", () -> {
            assertThat(errorResponse.bookTitle())
                    .containsExactly("Ensure this field has no more than 255 characters.");
            assertThat(errorResponse.telegramChatLink())
                    .containsExactly("Ensure this field has no more than 200 characters.");
            assertThat(errorResponse.bookAuthors()).isNull();
            assertThat(errorResponse.publicationYear()).isNull();
            assertThat(errorResponse.description()).isNull();
        });
    }

    @Test
    @DisplayName("Ошибка частичного обновления клуба методом PATCH другого пользователя")
    public void patchOtherUserClubTest() {
        Map<String, Object> patchData = patchedClubRequest("Hacked Club " + getRandomHex());

        UserDetailErrorResponseModel errorResponse = step("Обновление чужого клуба методом PATCH", () ->
                patchClubWithRawBody(clubId, patchData, otherAccessToken)
                        .spec(forbiddenClubResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка ответа (403) и сообщения об отсутствии прав", () ->
                assertThat(errorResponse.detail()).isEqualTo(NO_PERMISSION_MESSAGE));

        ClubResponseModel untouchedClub = step("Проверка, что клуб не изменился", () ->
                retrieveClub(clubId)
                        .spec(successfulRetrieveClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Сравнение названия клуба с исходным", () ->
                assertThat(untouchedClub.bookTitle()).isEqualTo(createdClub.bookTitle()));
    }

    @Test
    @DisplayName("Ошибка частичного обновления клуба методом PATCH без Authorization")
    public void patchClubWithoutAuthTest() {
        Map<String, Object> patchData = patchedClubRequest("Hacked Club " + getRandomHex());

        UserDetailErrorResponseModel errorResponse = step("Обновление клуба методом PATCH без Authorization", () ->
                patchClubWithoutAuth(clubId, patchData)
                        .spec(unauthorizedClubResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка ответа (401) и сообщения об отсутствии авторизации", () ->
                assertThat(errorResponse.detail()).isEqualTo(AUTHENTICATION_MESSAGE));
    }

    @Test
    @DisplayName("Ошибка частичного обновления несуществующего клуба методом PATCH")
    public void patchMissingClubTest() {
        Map<String, Object> patchData = patchedClubRequest("Patched " + getRandomHex());

        UserDetailErrorResponseModel errorResponse = step("Обновление несуществующего клуба методом PATCH", () ->
                patchClubWithRawBody(99999999, patchData, accessToken)
                        .spec(notFoundClubResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка ответа (404) и сообщения о ненайденном клубе", () ->
                assertThat(errorResponse.detail()).isEqualTo(CLUB_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("Ошибка частичного обновления клуба при передаче пустого bookTitle из пробелов")
    public void patchClubWithSpacesBookTitleTest() {
        Map<String, Object> patchData = patchedClubRequest("   ");

        ClubErrorResponseModel errorResponse = step("Обновление названия клуба строкой из пробелов", () ->
                patchClubWithRawBody(clubId, patchData, accessToken)
                        .spec(badRequestClubResponseSpec)
                        .extract().as(ClubErrorResponseModel.class));

        step("Проверка ответа (400) и сообщения о пустом bookTitle", () ->
                assertThat(errorResponse.bookTitle()).containsExactly("This field may not be blank."));
    }

    @Test
    @DisplayName("Очистка пробелов вокруг bookTitle при частичном обновлении")
    public void patchClubWithTrimmedBookTitleTest() {
        String updatedBookTitle = "Patched Trimmed " + getRandomHex();

        ClubResponseModel response = step("Обновление названия клуба с пробелами по краям", () ->
                patchClubWithRawBody(clubId, patchedClubRequest("   " + updatedBookTitle + "   "), accessToken)
                        .spec(successfulUpdateClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Проверка очистки пробелов вокруг названия", () ->
                assertThat(response.bookTitle()).isEqualTo(updatedBookTitle));
    }

    @Test
    @DisplayName("Read-only поля клуба игнорируются при частичном обновлении")
    public void patchClubReadOnlyFieldsIgnoredTest() {
        Map<String, Object> patchData = patchedClubRequest("Patched " + getRandomHex());
        patchData.put("id", 99999);
        patchData.put("owner", otherUserId);
        patchData.put("members", List.of(otherUserId));
        patchData.put("reviews", List.of());

        ClubResponseModel response = step("Обновление клуба с передачей read-only полей", () ->
                patchClubWithRawBody(clubId, patchData, accessToken)
                        .spec(successfulUpdateClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Проверка, что записываемое поле обновилось, а read-only поля остались прежними", () -> {
            assertThat(response.bookTitle()).isEqualTo(patchData.get("bookTitle"));
            assertThat(response.id()).isEqualTo(clubId);
            assertThat(response.owner()).isEqualTo(createdClub.owner());
            assertThat(response.members()).containsExactly(createdClub.owner());
            assertThat(response.reviews()).isEmpty();
            assertThat(response.members()).doesNotContain(otherUserId);
        });

        ClubResponseModel savedClub = step("Проверка состояния клуба на сервере", () ->
                retrieveClub(clubId)
                        .spec(successfulRetrieveClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Проверка владельца и участников в сохранённом клубе", () -> {
            assertThat(savedClub.owner()).isEqualTo(createdClub.owner());
            assertThat(savedClub.members()).containsExactly(createdClub.owner());
        });
    }
}
