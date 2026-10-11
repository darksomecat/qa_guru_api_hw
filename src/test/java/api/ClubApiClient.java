package api;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import models.club.CreateClubBodyModel;
import models.club.UpdateClubBodyModel;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class ClubApiClient extends BaseApiClient {

    @Step("POST /clubs/ — создание клуба")
    public static ValidatableResponse createClub(CreateClubBodyModel createBody, String accessToken) {
        return given(request())
                .header("Authorization", "Bearer " + accessToken)
                .body(createBody)
                .when()
                .post(CLUBS_PATH)
                .then();
    }

    @Step("POST /clubs/ — создание клуба без Authorization")
    public static ValidatableResponse createClubWithoutAuth(CreateClubBodyModel createBody) {
        return given(request())
                .body(createBody)
                .when()
                .post(CLUBS_PATH)
                .then();
    }

    @Step("POST /clubs/ — создание клуба с пустым телом запроса")
    public static ValidatableResponse createClubWithEmptyBody(String accessToken) {
        return given(request())
                .header("Authorization", "Bearer " + accessToken)
                .body(EMPTY_BODY)
                .when()
                .post(CLUBS_PATH)
                .then();
    }

    @Step("POST /clubs/ — создание клуба с произвольным телом запроса")
    public static ValidatableResponse createClubWithRawBody(Map<String, Object> createBody, String accessToken) {
        return given(request())
                .header("Authorization", "Bearer " + accessToken)
                .body(createBody)
                .when()
                .post(CLUBS_PATH)
                .then();
    }

    @Step("POST /clubs/ — создание клуба с некорректными типами данных")
    public static ValidatableResponse createClubWithInvalidDataTypes(Map<String, Object> createBody, String accessToken) {
        return given(request())
                .header("Authorization", "Bearer " + accessToken)
                .body(createBody)
                .when()
                .post(CLUBS_PATH)
                .then();
    }

    @Step("GET /clubs/ — получение списка клубов с параметрами запроса")
    public static ValidatableResponse getClubs(Map<String, Object> queryParams) {
        return given(request())
                .queryParams(queryParams)
                .when()
                .get(CLUBS_PATH)
                .then();
    }

    @Step("GET /clubs/ — получение списка клубов с размером страницы {pageSize}")
    public static ValidatableResponse getClubsWithPageSize(int pageSize) {
        return given(request())
                .queryParam("page_size", pageSize)
                .when()
                .get(CLUBS_PATH)
                .then();
    }

    @Step("GET /clubs/ — поиск клубов по названию {searchTerm}")
    public static ValidatableResponse getClubsBySearch(String searchTerm) {
        return given(request())
                .queryParam("search", searchTerm)
                .when()
                .get(CLUBS_PATH)
                .then();
    }

    @Step("GET {url} — получение списка клубов по ссылке пагинации")
    public static ValidatableResponse getClubsByUrl(String url) {
        return given(request())
                .when()
                .get(url)
                .then();
    }

    @Step("GET /clubs/{clubId}/ — получение клуба по id")
    public static ValidatableResponse retrieveClub(Integer clubId) {
        return given(request())
                .when()
                .get(CLUBS_PATH + clubId + "/")
                .then();
    }

    @Step("PUT /clubs/{clubId}/ — полное обновление клуба")
    public static ValidatableResponse updateClub(Integer clubId, UpdateClubBodyModel updateBody, String accessToken) {
        return given(request())
                .header("Authorization", "Bearer " + accessToken)
                .body(updateBody)
                .when()
                .put(CLUBS_PATH + clubId + "/")
                .then();
    }

    @Step("PUT /clubs/{clubId}/ — полное обновление клуба с произвольным телом запроса")
    public static ValidatableResponse updateClubWithRawBody(Integer clubId, Map<String, Object> updateBody,
                                                           String accessToken) {
        return given(request())
                .header("Authorization", "Bearer " + accessToken)
                .body(updateBody)
                .when()
                .put(CLUBS_PATH + clubId + "/")
                .then();
    }

    @Step("PUT /clubs/{clubId}/ — полное обновление клуба без Authorization")
    public static ValidatableResponse updateClubWithoutAuth(Integer clubId, UpdateClubBodyModel updateBody) {
        return given(request())
                .body(updateBody)
                .when()
                .put(CLUBS_PATH + clubId + "/")
                .then();
    }

    @Step("PATCH /clubs/{clubId}/ — частичное обновление клуба")
    public static ValidatableResponse patchClubWithRawBody(Integer clubId, Map<String, Object> patchBody,
                                                          String accessToken) {
        return given(request())
                .header("Authorization", "Bearer " + accessToken)
                .body(patchBody)
                .when()
                .patch(CLUBS_PATH + clubId + "/")
                .then();
    }

    @Step("PATCH /clubs/{clubId}/ — частичное обновление клуба без Authorization")
    public static ValidatableResponse patchClubWithoutAuth(Integer clubId, Map<String, Object> patchBody) {
        return given(request())
                .body(patchBody)
                .when()
                .patch(CLUBS_PATH + clubId + "/")
                .then();
    }

    @Step("DELETE /clubs/{clubId}/ — удаление клуба")
    public static ValidatableResponse deleteClub(Integer clubId, String accessToken) {
        return given(request())
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .delete(CLUBS_PATH + clubId + "/")
                .then();
    }

    @Step("DELETE /clubs/{clubId}/ — удаление клуба без Authorization")
    public static ValidatableResponse deleteClubWithoutAuth(Integer clubId) {
        return given(request())
                .when()
                .delete(CLUBS_PATH + clubId + "/")
                .then();
    }

    @Step("POST /clubs/reviews/ — создание отзыва о клубе")
    public static ValidatableResponse createClubReview(Map<String, Object> reviewBody, String accessToken) {
        return given(request())
                .header("Authorization", "Bearer " + accessToken)
                .body(reviewBody)
                .when()
                .post(CLUB_REVIEWS_PATH)
                .then();
    }

    @Step("GET /clubs/reviews/{reviewId}/ — получение отзыва по id")
    public static ValidatableResponse retrieveClubReview(Integer reviewId) {
        return given(request())
                .when()
                .get(CLUB_REVIEWS_PATH + reviewId + "/")
                .then();
    }

    private ClubApiClient() {
    }
}
