package tests.club;

import models.club.BookReviewResponseModel;
import models.club.ClubResponseModel;
import models.club.PaginatedClubListResponseModel;
import models.login.LoginBodyModel;
import models.registration.RegistrationBodyModel;
import models.user.UserDetailErrorResponseModel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tests.TestBase;

import static api.ClubApiClient.createClubReview;
import static api.ClubApiClient.createClubWithRawBody;
import static api.ClubApiClient.deleteClub;
import static api.ClubApiClient.deleteClubWithoutAuth;
import static api.ClubApiClient.getClubsBySearch;
import static api.ClubApiClient.retrieveClub;
import static api.ClubApiClient.retrieveClubReview;
import static api.LoginApiClient.login;
import static api.RegistrationApiClient.register;
import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static specs.ClubSpec.forbiddenClubResponseSpec;
import static specs.ClubSpec.notFoundClubResponseSpec;
import static specs.ClubSpec.successfulDeleteClubResponseSpec;
import static specs.ClubSpec.successfulGetClubsResponseSpec;
import static specs.ClubSpec.successfulRetrieveClubResponseSpec;
import static specs.ClubSpec.unauthorizedClubResponseSpec;
import static specs.LoginSpec.successfulLoginResponseSpec;
import static tests.testData.TestData.AUTHENTICATION_MESSAGE;
import static tests.testData.TestData.CLUB_NOT_FOUND_MESSAGE;
import static tests.testData.TestData.NO_PERMISSION_MESSAGE;
import static tests.testData.TestData.PASSWORDHC;
import static tests.testData.TestData.USERNAMEHC;
import static tests.testData.TestData.clubReviewRequest;
import static tests.testData.TestData.newClubRequest;
import static tests.testData.TestData.otherUserName;
import static tests.testData.TestData.searchableClubTitle;

public class ClubDeleteTests extends TestBase {

    private static String accessToken;
    private static String memberAccessToken;
    private static Integer clubId;
    private static String clubBookTitle;

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

        step("Регистрация второго пользователя для проверки прав на удаление", () -> {
            RegistrationBodyModel memberData = new RegistrationBodyModel(otherUserName(), PASSWORDHC);

            register(memberData)
                    .statusCode(anyOf(
                            is(201),
                            is(400) ));

            LoginBodyModel memberLoginData = new LoginBodyModel(memberData.username(), PASSWORDHC);

            memberAccessToken = login(memberLoginData)
                    .spec(successfulLoginResponseSpec)
                    .extract().path("access");
        });
    }

    @BeforeEach
    public void createTestClub() {
        step("Создание клуба для проверок удаления", () -> {
            clubBookTitle = searchableClubTitle();

            clubId = createClubWithRawBody(newClubRequest(clubBookTitle), accessToken)
                    .extract().as(ClubResponseModel.class).id();
        });
    }

    @Test
    @DisplayName("Успешное удаление своего клуба")
    public void successfulDeleteClubTest() {
        step("Удаление клуба методом DELETE", () ->
                deleteClub(clubId, accessToken)
                        .spec(successfulDeleteClubResponseSpec));

        UserDetailErrorResponseModel errorResponse = step("Получение удалённого клуба по id", () ->
                retrieveClub(clubId)
                        .spec(notFoundClubResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка ответа (404) и сообщения о ненайденном клубе", () ->
                assertThat(errorResponse.detail()).isEqualTo(CLUB_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("Удалённый клуб пропадает из списка клубов")
    public void deletedClubIsAbsentInListTest() {
        PaginatedClubListResponseModel foundBeforeDelete = step("Поиск клуба до удаления", () ->
                getClubsBySearch(clubBookTitle)
                        .spec(successfulGetClubsResponseSpec)
                        .extract().as(PaginatedClubListResponseModel.class));

        step("Проверка наличия клуба в списке до удаления", () -> {
            assertThat(foundBeforeDelete.count()).isEqualTo(1);
            assertThat(foundBeforeDelete.results().get(0).id()).isEqualTo(clubId);
        });

        step("Удаление клуба методом DELETE", () ->
                deleteClub(clubId, accessToken)
                        .spec(successfulDeleteClubResponseSpec));

        PaginatedClubListResponseModel foundAfterDelete = step("Поиск клуба после удаления", () ->
                getClubsBySearch(clubBookTitle)
                        .spec(successfulGetClubsResponseSpec)
                        .extract().as(PaginatedClubListResponseModel.class));

        step("Проверка отсутствия клуба в списке после удаления", () -> {
            assertThat(foundAfterDelete.count()).isZero();
            assertThat(foundAfterDelete.results()).isEmpty();
        });
    }

    @Test
    @DisplayName("Удаление клуба вместе с его отзывами")
    public void deleteClubWithReviewsTest() {
        Integer reviewId = step("Добавление отзыва в клуб", () ->
                createClubReview(clubReviewRequest(clubId), accessToken)
                        .extract().as(BookReviewResponseModel.class).id());

        ClubResponseModel clubWithReview = step("Получение клуба с отзывом", () ->
                retrieveClub(clubId)
                        .extract().as(ClubResponseModel.class));

        step("Проверка наличия отзыва в клубе", () ->
                assertThat(clubWithReview.reviews()).extracting(BookReviewResponseModel::id)
                        .containsExactly(reviewId));

        step("Удаление клуба методом DELETE", () ->
                deleteClub(clubId, accessToken)
                        .spec(successfulDeleteClubResponseSpec));

        UserDetailErrorResponseModel errorResponse = step("Получение отзыва удалённого клуба", () ->
                retrieveClubReview(reviewId)
                        .spec(notFoundClubResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка каскадного удаления отзыва", () ->
                assertThat(errorResponse.detail()).isEqualTo("No BookReview matches the given query."));
    }

    @Test
    @DisplayName("Ошибка удаления клуба другим пользователем")
    public void deleteOtherUserClubTest() {
        UserDetailErrorResponseModel errorResponse = step("Удаление чужого клуба методом DELETE", () ->
                deleteClub(clubId, memberAccessToken)
                        .spec(forbiddenClubResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка ответа (403) и сообщения об отсутствии прав", () ->
                assertThat(errorResponse.detail()).isEqualTo(NO_PERMISSION_MESSAGE));

        ClubResponseModel savedClub = step("Проверка, что клуб не удалён", () ->
                retrieveClub(clubId)
                        .spec(successfulRetrieveClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Проверка данных сохранённого клуба", () ->
                assertThat(savedClub.id()).isEqualTo(clubId));
    }

    @Test
    @DisplayName("Ошибка удаления клуба без Authorization")
    public void deleteClubWithoutAuthTest() {
        UserDetailErrorResponseModel errorResponse = step("Удаление клуба методом DELETE без Authorization", () ->
                deleteClubWithoutAuth(clubId)
                        .spec(unauthorizedClubResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка ответа (401) и сообщения об отсутствии авторизации", () ->
                assertThat(errorResponse.detail()).isEqualTo(AUTHENTICATION_MESSAGE));
    }

    @Test
    @DisplayName("Ошибка удаления несуществующего клуба")
    public void deleteMissingClubTest() {
        UserDetailErrorResponseModel errorResponse = step("Удаление несуществующего клуба методом DELETE", () ->
                deleteClub(99999999, accessToken)
                        .spec(notFoundClubResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка ответа (404) и сообщения о ненайденном клубе", () ->
                assertThat(errorResponse.detail()).isEqualTo(CLUB_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("Ошибка повторного удаления клуба")
    public void deleteAlreadyDeletedClubTest() {
        step("Удаление клуба методом DELETE", () ->
                deleteClub(clubId, accessToken)
                        .spec(successfulDeleteClubResponseSpec));

        UserDetailErrorResponseModel errorResponse = step("Повторное удаление того же клуба", () ->
                deleteClub(clubId, accessToken)
                        .spec(notFoundClubResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка ответа (404) и сообщения о ненайденном клубе", () ->
                assertThat(errorResponse.detail()).isEqualTo(CLUB_NOT_FOUND_MESSAGE));
    }
}
