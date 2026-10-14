package tests.review;

import helpers.AuthHelper;
import models.club.BookReviewResponseModel;
import models.club.ClubResponseModel;
import models.user.UserDetailErrorResponseModel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tests.TestBase;

import static api.ClubApiClient.createClub;
import static api.ClubApiClient.joinClub;
import static api.ReviewApiClient.createReview;
import static api.ReviewApiClient.deleteReview;
import static api.ReviewApiClient.patchReview;
import static api.ReviewApiClient.updateReview;
import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static specs.ReviewSpec.forbiddenReviewResponseSpec;
import static specs.ReviewSpec.successfulCreateReviewResponseSpec;
import static tests.testData.TestData.NO_PERMISSION_MESSAGE;
import static tests.testData.TestData.PASSWORDHC;
import static tests.testData.TestData.clubReviewRequest;
import static tests.testData.TestData.newClubRequest;
import static tests.testData.TestData.searchableClubTitle;
import static tests.testData.TestData.uniqueUserName;

public class ReviewPermissionTests extends TestBase {

    private static AuthHelper.AuthSession owner;
    private static AuthHelper.AuthSession member;
    private static AuthHelper.AuthSession stranger;
    private static Integer clubId;
    private static Integer ownerReviewId;

    @BeforeAll
    public static void auth() {
        step("Подготовка пользователей через API", () -> {
            owner = AuthHelper.loginAs(uniqueUserName("revowner"), PASSWORDHC);
            member = AuthHelper.loginAs(uniqueUserName("revmember"), PASSWORDHC);
            stranger = AuthHelper.loginAs(uniqueUserName("revstranger"), PASSWORDHC);
        });
    }

    @BeforeEach
    public void prepareClubAndReview() {
        step("Создание клуба владельцем через API", () ->
                clubId = createClub(newClubRequest(searchableClubTitle()), owner.accessToken())
                        .extract().as(ClubResponseModel.class).id());

        step("Вступление участника в клуб через API", () ->
                joinClub(clubId, member.accessToken()).statusCode(is(204)));

        step("Создание отзыва владельцем через API", () ->
                ownerReviewId = createReview(clubReviewRequest(clubId, "Отзыв владельца", 4, 10), owner.accessToken())
                        .spec(successfulCreateReviewResponseSpec)
                        .extract().as(BookReviewResponseModel.class).id());
    }

    @Test
    @DisplayName("Любой авторизованный пользователь может создать отзыв без членства в клубе")
    public void strangerCanCreateReviewTest() {
        BookReviewResponseModel response = step("Создание отзыва посторонним пользователем", () ->
                createReview(clubReviewRequest(clubId, "Отзыв постороннего", 2, 1), stranger.accessToken())
                        .spec(successfulCreateReviewResponseSpec)
                        .extract().as(BookReviewResponseModel.class));

        step("Проверка автора отзыва", () -> {
            assertThat(response.user().id()).isEqualTo(stranger.userId());
            assertThat(response.club()).isEqualTo(clubId);
        });
    }

    @Test
    @DisplayName("Участник клуба может создать свой отзыв")
    public void memberCanCreateReviewTest() {
        BookReviewResponseModel response = step("Создание отзыва участником клуба", () ->
                createReview(clubReviewRequest(clubId, "Отзыв участника", 5, 3), member.accessToken())
                        .spec(successfulCreateReviewResponseSpec)
                        .extract().as(BookReviewResponseModel.class));

        step("Проверка автора отзыва", () ->
                assertThat(response.user().id()).isEqualTo(member.userId()));
    }

    @Test
    @DisplayName("Чужой пользователь не может обновить или удалить отзыв")
    public void otherUserCannotModifyReviewTest() {
        UserDetailErrorResponseModel putError = step("PUT чужого отзыва участником", () ->
                updateReview(ownerReviewId, clubReviewRequest(clubId, "hack", 1, 1), member.accessToken())
                        .spec(forbiddenReviewResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка 403 на PUT", () ->
                assertThat(putError.detail()).isEqualTo(NO_PERMISSION_MESSAGE));

        UserDetailErrorResponseModel patchError = step("PATCH чужого отзыва участником", () ->
                patchReview(ownerReviewId, java.util.Map.of("review", "hack patch"), member.accessToken())
                        .spec(forbiddenReviewResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка 403 на PATCH", () ->
                assertThat(patchError.detail()).isEqualTo(NO_PERMISSION_MESSAGE));

        UserDetailErrorResponseModel deleteError = step("DELETE чужого отзыва участником", () ->
                deleteReview(ownerReviewId, member.accessToken())
                        .spec(forbiddenReviewResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка 403 на DELETE", () ->
                assertThat(deleteError.detail()).isEqualTo(NO_PERMISSION_MESSAGE));
    }

    @Test
    @DisplayName("Посторонний пользователь не может изменить чужой отзыв")
    public void strangerCannotModifyReviewTest() {
        UserDetailErrorResponseModel error = step("PUT чужого отзыва посторонним", () ->
                updateReview(ownerReviewId, clubReviewRequest(clubId, "hack", 1, 1), stranger.accessToken())
                        .spec(forbiddenReviewResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка 403", () ->
                assertThat(error.detail()).isEqualTo(NO_PERMISSION_MESSAGE));
    }
}
