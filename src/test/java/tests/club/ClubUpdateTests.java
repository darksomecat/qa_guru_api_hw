package tests.club;

import models.club.ClubErrorResponseModel;
import models.club.ClubResponseModel;
import models.club.UpdateClubBodyModel;
import models.login.LoginBodyModel;
import models.registration.RegistrationBodyModel;
import models.user.UserDetailErrorResponseModel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tests.TestBase;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static api.ClubApiClient.createClubWithRawBody;
import static api.ClubApiClient.retrieveClub;
import static api.ClubApiClient.updateClub;
import static api.ClubApiClient.updateClubWithRawBody;
import static api.ClubApiClient.updateClubWithoutAuth;
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
import static tests.testData.TestData.otherUserName;
import static tests.testData.TestData.searchableClubTitle;
import static tests.testData.TestData.updatedClubFields;
import static utils.GeneretedUtils.getRandomHex;
import static utils.GeneretedUtils.getRandomString;
import static utils.GeneretedUtils.getUserIdFromToken;

public class ClubUpdateTests extends TestBase {

    private static String accessToken;
    private static String otherAccessToken;
    private static Integer otherUserId;
    private static Integer clubId;

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
            RegistrationBodyModel otherData = new RegistrationBodyModel(otherUserName(), PASSWORDHC);

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
        step("Создание клуба для проверок обновления", () ->
                clubId = createClubWithRawBody(newClubRequest(searchableClubTitle()), accessToken)
                        .extract().as(ClubResponseModel.class).id());
    }

    @Test
    @DisplayName("Успешное полное обновление клуба методом PUT")
    public void successfulUpdateClubTest() {
        UpdateClubBodyModel updateData = updatedClub();

        ClubResponseModel response = step("Обновление клуба методом PUT", () ->
                updateClub(clubId, updateData, accessToken)
                        .spec(successfulUpdateClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Проверка обновлённых данных клуба", () -> {
            assertThat(response.id()).isEqualTo(clubId);
            assertThat(response.bookTitle()).isEqualTo(updateData.bookTitle());
            assertThat(response.bookAuthors()).isEqualTo(updateData.bookAuthors());
            assertThat(response.publicationYear()).isEqualTo(updateData.publicationYear());
            assertThat(response.description()).isEqualTo(updateData.description());
            assertThat(response.telegramChatLink()).isEqualTo(updateData.telegramChatLink());
        });

        ClubResponseModel savedClub = step("Получение обновлённого клуба по id", () ->
                retrieveClub(clubId)
                        .spec(successfulRetrieveClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Проверка сохранения изменений на сервере", () -> {
            assertThat(savedClub.bookTitle()).isEqualTo(updateData.bookTitle());
            assertThat(savedClub.bookAuthors()).isEqualTo(updateData.bookAuthors());
            assertThat(savedClub.publicationYear()).isEqualTo(updateData.publicationYear());
            assertThat(savedClub.description()).isEqualTo(updateData.description());
            assertThat(savedClub.telegramChatLink()).isEqualTo(updateData.telegramChatLink());
        });
    }

    @Test
    @DisplayName("Успешное обновление клуба методом PUT при максимальной длине полей")
    public void successfulUpdateClubMaxSymbolsTest() {
        UpdateClubBodyModel updateData = new UpdateClubBodyModel(
                "Updated " + getRandomHex(),
                getRandomString(255),
                testData.clubPublicationYear,
                testData.clubDescription,
                testData.maxLeght200Symbols
        );

        ClubResponseModel response = step("Обновление клуба методом PUT с максимальной длиной полей", () ->
                updateClub(clubId, updateData, accessToken)
                        .spec(successfulUpdateClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Проверка сохранённых значений полей", () -> {
            assertThat(response.bookAuthors()).isEqualTo(updateData.bookAuthors());
            assertThat(response.telegramChatLink()).isEqualTo(updateData.telegramChatLink());
        });
    }

    @Test
    @DisplayName("Ошибка обновления клуба методом PUT без обязательного поля")
    public void updateClubWithoutRequiredFieldTest() {
        Map<String, Object> updateData = updatedClubFields();
        updateData.remove("bookTitle");

        ClubErrorResponseModel errorResponse = step("Обновление клуба методом PUT без поля bookTitle", () ->
                updateClubWithRawBody(clubId, updateData, accessToken)
                        .spec(badRequestClubResponseSpec)
                        .extract().as(ClubErrorResponseModel.class));

        step("Проверка ответа (400) и сообщения об обязательном поле", () -> {
            assertThat(errorResponse.bookTitle()).containsExactly("This field is required.");
            assertThat(errorResponse.bookAuthors()).isNull();
            assertThat(errorResponse.publicationYear()).isNull();
            assertThat(errorResponse.description()).isNull();
            assertThat(errorResponse.telegramChatLink()).isNull();
        });
    }

    @Test
    @DisplayName("Read-only поля клуба игнорируются при полном обновлении")
    public void updateClubReadOnlyFieldsIgnoredTest() {
        Map<String, Object> updateData = updatedClubFields();
        updateData.put("id", 99999);
        updateData.put("owner", otherUserId);
        updateData.put("members", List.of(otherUserId));
        updateData.put("reviews", List.of());

        ClubResponseModel response = step("Обновление клуба методом PUT с передачей read-only полей", () ->
                updateClubWithRawBody(clubId, updateData, accessToken)
                        .spec(successfulUpdateClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Проверка, что записываемые поля обновились, а read-only поля остались прежними", () -> {
            assertThat(response.bookTitle()).isEqualTo(updateData.get("bookTitle"));
            assertThat(response.id()).isEqualTo(clubId);
            assertThat(response.owner()).isNotEqualTo(otherUserId);
            assertThat(response.members()).doesNotContain(otherUserId);
            assertThat(response.reviews()).isEmpty();
        });

        ClubResponseModel savedClub = step("Проверка состояния клуба на сервере", () ->
                retrieveClub(clubId)
                        .spec(successfulRetrieveClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Проверка владельца и участников в сохранённом клубе", () -> {
            assertThat(savedClub.owner()).isNotEqualTo(otherUserId);
            assertThat(savedClub.members()).doesNotContain(otherUserId);
        });
    }

    @Test
    @DisplayName("Ошибка обновления клуба методом PUT при превышении длины полей")
    public void updateClubWithTooLongFieldsTest() {
        Map<String, Object> updateData = new LinkedHashMap<>();
        updateData.put("bookTitle", testData.tooLong256Symbols);
        updateData.put("bookAuthors", testData.tooLong256Symbols);
        updateData.put("publicationYear", testData.clubPublicationYear);
        updateData.put("description", testData.clubDescription);
        updateData.put("telegramChatLink", testData.tooLong201Symbols);

        ClubErrorResponseModel errorResponse = step("Обновление клуба методом PUT с превышением длины полей", () ->
                updateClubWithRawBody(clubId, updateData, accessToken)
                        .spec(badRequestClubResponseSpec)
                        .extract().as(ClubErrorResponseModel.class));

        step("Проверка ответа (400) и сообщений о превышении длины", () -> {
            assertThat(errorResponse.bookTitle())
                    .containsExactly("Ensure this field has no more than 255 characters.");
            assertThat(errorResponse.bookAuthors())
                    .containsExactly("Ensure this field has no more than 255 characters.");
            assertThat(errorResponse.telegramChatLink())
                    .containsExactly("Ensure this field has no more than 200 characters.");
        });
    }

    @Test
    @DisplayName("Ошибка обновления клуба методом PUT другого пользователя")
    public void updateOtherUserClubTest() {
        UpdateClubBodyModel updateData = updatedClub();

        UserDetailErrorResponseModel errorResponse = step("Обновление чужого клуба методом PUT", () ->
                updateClub(clubId, updateData, otherAccessToken)
                        .spec(forbiddenClubResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка ответа (403) и сообщения об отсутствии прав", () ->
                assertThat(errorResponse.detail()).isEqualTo(NO_PERMISSION_MESSAGE));

        ClubResponseModel untouchedClub = step("Проверка, что клуб не изменился", () ->
                retrieveClub(clubId)
                        .spec(successfulRetrieveClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Сравнение названия клуба с исходным", () ->
                assertThat(untouchedClub.bookTitle()).isNotEqualTo(updateData.bookTitle()));
    }

    @Test
    @DisplayName("Ошибка обновления клуба методом PUT без Authorization")
    public void updateClubWithoutAuthTest() {
        UpdateClubBodyModel updateData = updatedClub();

        UserDetailErrorResponseModel errorResponse = step("Обновление клуба методом PUT без Authorization", () ->
                updateClubWithoutAuth(clubId, updateData)
                        .spec(unauthorizedClubResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка ответа (401) и сообщения об отсутствии авторизации", () ->
                assertThat(errorResponse.detail()).isEqualTo(AUTHENTICATION_MESSAGE));
    }

    @Test
    @DisplayName("Ошибка обновления несуществующего клуба методом PUT")
    public void updateMissingClubTest() {
        UpdateClubBodyModel updateData = updatedClub();

        UserDetailErrorResponseModel errorResponse = step("Обновление несуществующего клуба методом PUT", () ->
                updateClub(99999999, updateData, accessToken)
                        .spec(notFoundClubResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка ответа (404) и сообщения о ненайденном клубе", () ->
                assertThat(errorResponse.detail()).isEqualTo(CLUB_NOT_FOUND_MESSAGE));
    }

    private static UpdateClubBodyModel updatedClub() {
        Map<String, Object> updateData = updatedClubFields();

        return new UpdateClubBodyModel(
                (String) updateData.get("bookTitle"),
                (String) updateData.get("bookAuthors"),
                (Integer) updateData.get("publicationYear"),
                (String) updateData.get("description"),
                (String) updateData.get("telegramChatLink")
        );
    }
}
