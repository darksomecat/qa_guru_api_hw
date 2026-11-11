package tests.review;

import helpers.AuthHelper;
import models.club.BookReviewResponseModel;
import models.club.ClubResponseModel;
import models.club.PaginatedBookReviewListResponseModel;
import models.club.ReviewErrorResponseModel;
import models.user.UserDetailErrorResponseModel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tests.TestBase;

import java.util.Map;

import static api.ClubApiClient.createClub;
import static api.ReviewApiClient.createReview;
import static api.ReviewApiClient.deleteReview;
import static api.ReviewApiClient.getReviews;
import static api.ReviewApiClient.patchReview;
import static api.ReviewApiClient.retrieveReview;
import static api.ReviewApiClient.updateReview;
import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.ReviewSpec.badRequestReviewResponseSpec;
import static specs.ReviewSpec.notFoundReviewResponseSpec;
import static specs.ReviewSpec.successfulCreateReviewResponseSpec;
import static specs.ReviewSpec.successfulDeleteReviewResponseSpec;
import static specs.ReviewSpec.successfulGetReviewsResponseSpec;
import static specs.ReviewSpec.successfulRetrieveReviewResponseSpec;
import static specs.ReviewSpec.successfulUpdateReviewResponseSpec;
import static specs.ReviewSpec.unauthorizedReviewResponseSpec;
import static tests.testData.TestData.AUTHENTICATION_MESSAGE;
import static tests.testData.TestData.PASSWORDHC;
import static tests.testData.TestData.REVIEW_NOT_FOUND_MESSAGE;
import static tests.testData.TestData.clubReviewRequest;
import static tests.testData.TestData.newClubRequest;
import static tests.testData.TestData.searchableClubTitle;
import static tests.testData.TestData.uniqueUserName;

public class ReviewCrudTests extends TestBase {

    private static AuthHelper.AuthSession author;
    private static Integer clubId;

    @BeforeAll
    public static void auth() {
        step("Подготовка автора отзывов через API", () ->
                author = AuthHelper.loginAs(uniqueUserName("reviewcrud"), PASSWORDHC));
    }

    @BeforeEach
    public void createClubForReview() {
        step("Создание клуба через API для отзывов", () ->
                clubId = createClub(newClubRequest(searchableClubTitle()), author.accessToken())
                        .extract().as(ClubResponseModel.class).id());
    }

    @Test
    @DisplayName("Успешное создание отзыва")
    public void successfulCreateReviewTest() {
        Map<String, Object> reviewData = clubReviewRequest(clubId, "Отличный клуб", 5, 42);

        BookReviewResponseModel response = step("Создание отзыва методом POST", () ->
                createReview(reviewData, author.accessToken())
                        .spec(successfulCreateReviewResponseSpec)
                        .extract().as(BookReviewResponseModel.class));

        step("Проверка данных созданного отзыва", () -> {
            assertThat(response.id()).isNotNull();
            assertThat(response.club()).isEqualTo(clubId);
            assertThat(response.review()).isEqualTo("Отличный клуб");
            assertThat(response.assessment()).isEqualTo(5);
            assertThat(response.readPages()).isEqualTo(42);
            assertThat(response.user().id()).isEqualTo(author.userId());
            assertThat(response.user().username()).isEqualTo(author.username());
            assertThat(response.created()).isNotBlank();
            assertThat(response.modified()).isNull();
        });
    }

    @Test
    @DisplayName("Успешное чтение отзыва по id и списка отзывов клуба")
    public void successfulReadReviewTest() {
        BookReviewResponseModel created = step("Создание отзыва через API", () ->
                createReview(clubReviewRequest(clubId), author.accessToken())
                        .spec(successfulCreateReviewResponseSpec)
                        .extract().as(BookReviewResponseModel.class));

        BookReviewResponseModel retrieved = step("Получение отзыва по id", () ->
                retrieveReview(created.id())
                        .spec(successfulRetrieveReviewResponseSpec)
                        .extract().as(BookReviewResponseModel.class));

        step("Проверка совпадения данных отзыва", () -> {
            assertThat(retrieved.id()).isEqualTo(created.id());
            assertThat(retrieved.review()).isEqualTo(created.review());
            assertThat(retrieved.assessment()).isEqualTo(created.assessment());
        });

        PaginatedBookReviewListResponseModel list = step("Получение списка отзывов клуба", () ->
                getReviews(Map.of("club", clubId))
                        .spec(successfulGetReviewsResponseSpec)
                        .extract().as(PaginatedBookReviewListResponseModel.class));

        step("Проверка, что созданный отзыв есть в списке", () -> {
            assertThat(list.count()).isEqualTo(1);
            assertThat(list.results()).extracting(BookReviewResponseModel::id).containsExactly(created.id());
        });
    }

    @Test
    @DisplayName("Успешное обновление отзыва методами PUT и PATCH")
    public void successfulUpdateReviewTest() {
        Integer reviewId = step("Создание отзыва через API", () ->
                createReview(clubReviewRequest(clubId, "Исходный отзыв", 3, 5), author.accessToken())
                        .spec(successfulCreateReviewResponseSpec)
                        .extract().as(BookReviewResponseModel.class).id());

        BookReviewResponseModel putResponse = step("Обновление отзыва методом PUT", () ->
                updateReview(reviewId, clubReviewRequest(clubId, "Обновлённый отзыв", 5, 99), author.accessToken())
                        .spec(successfulUpdateReviewResponseSpec)
                        .extract().as(BookReviewResponseModel.class));

        step("Проверка полного обновления", () -> {
            assertThat(putResponse.review()).isEqualTo("Обновлённый отзыв");
            assertThat(putResponse.assessment()).isEqualTo(5);
            assertThat(putResponse.readPages()).isEqualTo(99);
            assertThat(putResponse.modified()).isNotBlank();
        });

        BookReviewResponseModel patchResponse = step("Частичное обновление отзыва методом PATCH", () ->
                patchReview(reviewId, Map.of("review", "Патч отзыва"), author.accessToken())
                        .spec(successfulUpdateReviewResponseSpec)
                        .extract().as(BookReviewResponseModel.class));

        step("Проверка частичного обновления", () -> {
            assertThat(patchResponse.review()).isEqualTo("Патч отзыва");
            assertThat(patchResponse.assessment()).isEqualTo(5);
            assertThat(patchResponse.readPages()).isEqualTo(99);
        });
    }

    @Test
    @DisplayName("Успешное удаление своего отзыва")
    public void successfulDeleteReviewTest() {
        Integer reviewId = step("Создание отзыва через API", () ->
                createReview(clubReviewRequest(clubId), author.accessToken())
                        .spec(successfulCreateReviewResponseSpec)
                        .extract().as(BookReviewResponseModel.class).id());

        step("Удаление отзыва методом DELETE", () ->
                deleteReview(reviewId, author.accessToken())
                        .spec(successfulDeleteReviewResponseSpec));

        UserDetailErrorResponseModel error = step("Получение удалённого отзыва", () ->
                retrieveReview(reviewId)
                        .spec(notFoundReviewResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка ответа 404", () ->
                assertThat(error.detail()).isEqualTo(REVIEW_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("Ошибка создания отзыва без Authorization")
    public void createReviewWithoutAuthTest() {
        UserDetailErrorResponseModel error = step("Создание отзыва без Authorization", () ->
                createReview(clubReviewRequest(clubId), null)
                        .spec(unauthorizedReviewResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка ответа 401", () ->
                assertThat(error.detail()).isEqualTo(AUTHENTICATION_MESSAGE));
    }

    @Test
    @DisplayName("Ошибка создания отзыва с пустым телом и некорректной оценкой")
    public void createReviewValidationTest() {
        ReviewErrorResponseModel emptyBodyError = step("Создание отзыва с пустым телом", () ->
                createReview("{}", author.accessToken())
                        .spec(badRequestReviewResponseSpec)
                        .extract().as(ReviewErrorResponseModel.class));

        step("Проверка обязательных полей", () -> {
            assertThat(emptyBodyError.club()).containsExactly("This field is required.");
            assertThat(emptyBodyError.review()).containsExactly("This field is required.");
            assertThat(emptyBodyError.assessment()).containsExactly("This field is required.");
            assertThat(emptyBodyError.readPages()).containsExactly("This field is required.");
        });

        ReviewErrorResponseModel assessmentError = step("Создание отзыва с assessment=9", () ->
                createReview(clubReviewRequest(clubId, "bad", 9, 1), author.accessToken())
                        .spec(badRequestReviewResponseSpec)
                        .extract().as(ReviewErrorResponseModel.class));

        step("Проверка валидации assessment", () ->
                assertThat(assessmentError.assessment())
                        .containsExactly("Ensure this value is less than or equal to 5."));
    }
}
