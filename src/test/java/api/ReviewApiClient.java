package api;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class ReviewApiClient extends BaseApiClient {

    @Step("POST /clubs/reviews/ — создание отзыва")
    public static ValidatableResponse createReview(Object reviewBody, String accessToken) {
        return withAuth(given(request()).body(reviewBody), accessToken)
                .when()
                .post(CLUB_REVIEWS_PATH)
                .then();
    }

    @Step("GET /clubs/reviews/ — список отзывов")
    public static ValidatableResponse getReviews(Map<String, Object> queryParams) {
        return given(request())
                .queryParams(queryParams)
                .when()
                .get(CLUB_REVIEWS_PATH)
                .then();
    }

    @Step("GET /clubs/reviews/{reviewId}/ — получение отзыва")
    public static ValidatableResponse retrieveReview(Integer reviewId) {
        return given(request())
                .when()
                .get(CLUB_REVIEWS_PATH + reviewId + "/")
                .then();
    }

    @Step("PUT /clubs/reviews/{reviewId}/ — полное обновление отзыва")
    public static ValidatableResponse updateReview(Integer reviewId, Object updateBody, String accessToken) {
        return withAuth(given(request()).body(updateBody), accessToken)
                .when()
                .put(CLUB_REVIEWS_PATH + reviewId + "/")
                .then();
    }

    @Step("PATCH /clubs/reviews/{reviewId}/ — частичное обновление отзыва")
    public static ValidatableResponse patchReview(Integer reviewId, Object patchBody, String accessToken) {
        return withAuth(given(request()).body(patchBody), accessToken)
                .when()
                .patch(CLUB_REVIEWS_PATH + reviewId + "/")
                .then();
    }

    @Step("DELETE /clubs/reviews/{reviewId}/ — удаление отзыва")
    public static ValidatableResponse deleteReview(Integer reviewId, String accessToken) {
        return withAuth(given(request()), accessToken)
                .when()
                .delete(CLUB_REVIEWS_PATH + reviewId + "/")
                .then();
    }

    private static RequestSpecification withAuth(RequestSpecification requestSpec, String accessToken) {
        if (accessToken != null) {
            return requestSpec.header("Authorization", "Bearer " + accessToken);
        }
        return requestSpec;
    }

    private ReviewApiClient() {
    }
}
