package api;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class ClubApiClient extends BaseApiClient {

    @Step("POST /clubs/ — создание клуба")
    public static ValidatableResponse createClub(Object createBody, String accessToken) {
        return withAuth(given(request()).body(createBody), accessToken)
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
                .queryParam("page", CLUBS_DEFAULT_PAGE)
                .queryParam("page_size", CLUBS_DEFAULT_PAGE_SIZE)
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
    public static ValidatableResponse updateClub(Integer clubId, Object updateBody, String accessToken) {
        return withAuth(given(request()).body(updateBody), accessToken)
                .when()
                .put(CLUBS_PATH + clubId + "/")
                .then();
    }

    @Step("PATCH /clubs/{clubId}/ — частичное обновление клуба")
    public static ValidatableResponse patchClub(Integer clubId, Object patchBody, String accessToken) {
        return withAuth(given(request()).body(patchBody), accessToken)
                .when()
                .patch(CLUBS_PATH + clubId + "/")
                .then();
    }

    @Step("DELETE /clubs/{clubId}/ — удаление клуба")
    public static ValidatableResponse deleteClub(Integer clubId, String accessToken) {
        return withAuth(given(request()), accessToken)
                .when()
                .delete(CLUBS_PATH + clubId + "/")
                .then();
    }

    @Step("POST /clubs/{clubId}/members/me/ — вступление в клуб")
    public static ValidatableResponse joinClub(Integer clubId, String accessToken) {
        return withAuth(given(request()), accessToken)
                .when()
                .post(CLUBS_PATH + clubId + "/members/me/")
                .then();
    }

    @Step("DELETE /clubs/{clubId}/members/me/ — выход из клуба")
    public static ValidatableResponse leaveClub(Integer clubId, String accessToken) {
        return withAuth(given(request()), accessToken)
                .when()
                .delete(CLUBS_PATH + clubId + "/members/me/")
                .then();
    }

    private static RequestSpecification withAuth(RequestSpecification requestSpec, String accessToken) {
        if (accessToken != null) {
            return requestSpec.header("Authorization", "Bearer " + accessToken);
        }
        return requestSpec;
    }

    private ClubApiClient() {
    }
}
