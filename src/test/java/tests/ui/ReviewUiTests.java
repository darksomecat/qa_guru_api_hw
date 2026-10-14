package tests.ui;

import helpers.AuthHelper;
import models.club.BookReviewResponseModel;
import models.club.ClubResponseModel;
import models.club.PaginatedBookReviewListResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pages.ClubDetailsPage;
import tests.TestBase;

import java.util.Map;

import static api.ClubApiClient.createClub;
import static api.ClubApiClient.joinClub;
import static api.ReviewApiClient.createReview;
import static api.ReviewApiClient.getReviews;
import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static specs.ReviewSpec.successfulCreateReviewResponseSpec;
import static specs.ReviewSpec.successfulGetReviewsResponseSpec;
import static tests.testData.TestData.PASSWORDHC;
import static tests.testData.TestData.clubReviewRequest;
import static tests.testData.TestData.newClubRequest;
import static tests.testData.TestData.searchableClubTitle;
import static tests.testData.TestData.uniqueUserName;

public class ReviewUiTests extends TestBase {

    private AuthHelper.AuthSession member;
    private AuthHelper.AuthSession owner;
    private Integer clubId;
    private String clubTitle;
    private final ClubDetailsPage clubDetailsPage = new ClubDetailsPage();

    @BeforeEach
    public void prepareClub() {
        step("Подготовка клуба и пользователей через API", () -> {
            owner = AuthHelper.loginAs(uniqueUserName("uirevown"), PASSWORDHC);
            member = AuthHelper.loginAs(uniqueUserName("uirevmember"), PASSWORDHC);
            clubTitle = searchableClubTitle();
            clubId = createClub(newClubRequest(clubTitle), owner.accessToken())
                    .extract().as(ClubResponseModel.class).id();
            joinClub(clubId, member.accessToken()).statusCode(is(204));
        });
    }

    @Test
    @DisplayName("UI: создание отзыва участником клуба")
    public void createReviewViaUiTest() {
        String reviewText = "UI отзыв " + System.currentTimeMillis();

        step("Авторизация участника в UI через API-токен", () ->
                AuthHelper.injectUiAuth(member));

        step("Создание отзыва через UI", () ->
                clubDetailsPage.open(clubId)
                        .shouldHaveTitle(clubTitle)
                        .shouldBeMember()
                        .shouldAllowCreateReview()
                        .createReview(reviewText, 4, 15)
                        .shouldHaveReview(reviewText));

        step("Проверка отзыва через API", () -> {
            PaginatedBookReviewListResponseModel list = getReviews(Map.of("club", clubId))
                    .spec(successfulGetReviewsResponseSpec)
                    .extract().as(PaginatedBookReviewListResponseModel.class);
            assertThat(list.results())
                    .extracting(BookReviewResponseModel::review)
                    .contains(reviewText);
        });
    }

    @Test
    @DisplayName("UI: редактирование своего отзыва")
    public void editOwnReviewViaUiTest() {
        String original = "Исходный UI отзыв";
        String updated = "Обновлённый UI отзыв " + System.currentTimeMillis();

        step("Создание отзыва через API", () ->
                createReview(clubReviewRequest(clubId, original, 3, 7), member.accessToken())
                        .spec(successfulCreateReviewResponseSpec));

        step("Авторизация автора отзыва в UI", () ->
                AuthHelper.injectUiAuth(member));

        step("Редактирование отзыва через UI", () ->
                clubDetailsPage.open(clubId)
                        .shouldBeMember()
                        .shouldHaveReview(original)
                        .editOwnReview(updated)
                        .shouldHaveReview(updated));
    }

    @Test
    @DisplayName("UI: удаление своего отзыва")
    public void deleteOwnReviewViaUiTest() {
        String reviewText = "Удаляемый UI отзыв " + System.currentTimeMillis();

        step("Создание отзыва через API", () ->
                createReview(clubReviewRequest(clubId, reviewText, 2, 4), member.accessToken())
                        .spec(successfulCreateReviewResponseSpec));

        step("Авторизация автора отзыва в UI", () ->
                AuthHelper.injectUiAuth(member));

        step("Удаление отзыва через UI", () ->
                clubDetailsPage.open(clubId)
                        .shouldHaveReview(reviewText)
                        .deleteOwnReview()
                        .shouldNotHaveReview(reviewText));
    }

    @Test
    @DisplayName("UI: чужой пользователь не видит кнопку написания отзыва без членства")
    public void strangerCannotCreateReviewViaUiTest() {
        AuthHelper.AuthSession stranger = AuthHelper.loginAs(uniqueUserName("uirevstr"), PASSWORDHC);

        step("Авторизация постороннего пользователя в UI", () ->
                AuthHelper.injectUiAuth(stranger));

        step("Проверка отсутствия кнопки написания отзыва", () ->
                clubDetailsPage.open(clubId)
                        .shouldHaveTitle(clubTitle)
                        .shouldNotAllowCreateReview());
    }
}
