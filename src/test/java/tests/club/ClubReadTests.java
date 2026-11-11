package tests.club;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.club.ClubResponseModel;
import models.club.PaginatedClubListResponseModel;
import models.login.LoginBodyModel;
import models.registration.RegistrationBodyModel;
import models.user.UserDetailErrorResponseModel;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tests.TestBase;

import java.util.Map;

import static api.ClubApiClient.createClubWithRawBody;
import static api.ClubApiClient.getClubs;
import static api.ClubApiClient.getClubsByUrl;
import static api.ClubApiClient.getClubsWithPageSize;
import static api.ClubApiClient.retrieveClub;
import static api.LoginApiClient.login;
import static api.RegistrationApiClient.register;
import static io.qameta.allure.Allure.step;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static specs.ClubSpec.CLUB_RESPONSE_SCHEMA;
import static specs.ClubSpec.PAGINATED_CLUB_LIST_SCHEMA;
import static specs.ClubSpec.notFoundClubResponseSpec;
import static specs.ClubSpec.successfulGetClubsResponseSpec;
import static specs.ClubSpec.successfulRetrieveClubResponseSpec;
import static specs.LoginSpec.successfulLoginResponseSpec;
import static tests.testData.TestData.CLUB_NOT_FOUND_MESSAGE;
import static tests.testData.TestData.INVALID_PAGE_MESSAGE;
import static tests.testData.TestData.NOT_FOUND_MESSAGE;
import static tests.testData.TestData.PASSWORDHC;
import static tests.testData.TestData.USERNAMEHC;
import static tests.testData.TestData.newClubRequest;
import static tests.testData.TestData.searchableClubTitle;
import static utils.GeneretedUtils.getUserIdFromToken;

public class ClubReadTests extends TestBase {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static String accessToken;
    private static Integer userId;
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

            userId = Integer.valueOf(getUserIdFromToken(accessToken));
        });
    }

    @BeforeEach
    public void createTestClub() {
        step("Создание клуба для проверок чтения", () -> {
            clubBookTitle = searchableClubTitle();

            ClubResponseModel response = createClubWithRawBody(newClubRequest(clubBookTitle), accessToken)
                    .extract().as(ClubResponseModel.class);

            clubId = response.id();
        });
    }

    @Test
    @DisplayName("Успешное чтение списка клубов без авторизации")
    public void getClubsListWithoutAuthTest() throws JsonProcessingException {
        String responseBody = step("Получение списка клубов с параметрами пагинации", () ->
                getClubs(Map.of("page", 1, "page_size", 1))
                        .spec(successfulGetClubsResponseSpec)
                        .extract().asString());

        step("Проверка соответствия ответа схеме списка клубов", () ->
                MatcherAssert.assertThat(responseBody,
                        matchesJsonSchemaInClasspath(PAGINATED_CLUB_LIST_SCHEMA)));

        PaginatedClubListResponseModel response =
                objectMapper.readValue(responseBody, PaginatedClubListResponseModel.class);

        step("Проверка структуры страницы с клубами", () -> {
            assertThat(response.count()).isPositive();
            assertThat(response.next()).isNotNull();
            assertThat(response.previous()).isNull();
            assertThat(response.results()).hasSize(1);
            assertThat(response.results().get(0).id()).isNotNull();
            assertThat(response.results().get(0).bookTitle()).isNotBlank();
        });

        step("Проверка доступности созданного клуба в общем списке анонимно", () -> {
            PaginatedClubListResponseModel foundClubs = getClubs(Map.of("search", clubBookTitle))
                    .spec(successfulGetClubsResponseSpec)
                    .extract().as(PaginatedClubListResponseModel.class);

            assertThat(foundClubs.count()).isEqualTo(1);
            assertThat(foundClubs.results().get(0).id()).isEqualTo(clubId);
        });
    }

    @Test
    @DisplayName("Переход по страницам списка клубов по ссылкам next и previous")
    public void getClubsPaginationTest() {
        PaginatedClubListResponseModel firstPage = step("Получение первой страницы списка клубов", () ->
                getClubsWithPageSize(1)
                        .spec(successfulGetClubsResponseSpec)
                        .extract().as(PaginatedClubListResponseModel.class));

        step("Проверка первой страницы", () -> {
            assertThat(firstPage.previous()).isNull();
            assertThat(firstPage.next()).isNotNull();
            assertThat(firstPage.results()).hasSize(1);
        });

        PaginatedClubListResponseModel secondPage = step("Получение второй страницы по ссылке next", () ->
                getClubsByUrl(firstPage.next())
                        .spec(successfulGetClubsResponseSpec)
                        .extract().as(PaginatedClubListResponseModel.class));

        step("Проверка второй страницы и возврата на первую", () -> {
            assertThat(secondPage.count()).isEqualTo(firstPage.count());
            assertThat(secondPage.results()).hasSize(1);
            assertThat(secondPage.results().get(0).id())
                    .isNotEqualTo(firstPage.results().get(0).id());
            assertThat(secondPage.previous()).isNotNull();
        });

        PaginatedClubListResponseModel backToFirstPage = step("Возврат на первую страницу по ссылке previous", () ->
                getClubsByUrl(secondPage.previous())
                        .spec(successfulGetClubsResponseSpec)
                        .extract().as(PaginatedClubListResponseModel.class));

        step("Проверка возврата на первую страницу", () ->
                assertThat(backToFirstPage.results().get(0).id())
                        .isEqualTo(firstPage.results().get(0).id()));
    }

    @Test
    @DisplayName("Чтение списка клубов с заданным размером страницы")
    public void getClubsPageSizeTest() {
        PaginatedClubListResponseModel response = step("Получение списка клубов с page_size=5", () ->
                getClubsWithPageSize(5)
                        .spec(successfulGetClubsResponseSpec)
                        .extract().as(PaginatedClubListResponseModel.class));

        step("Проверка количества клубов на странице", () -> {
            assertThat(response.results()).hasSize(5);
            assertThat(response.results()).allSatisfy(club -> {
                assertThat(club.id()).isNotNull();
                assertThat(club.bookTitle()).isNotBlank();
                assertThat(club.owner()).isNotNull();
            });
            assertThat(response.count()).isGreaterThanOrEqualTo(response.results().size());
        });
    }

      @Test
    @DisplayName("Пустой результат фильтрации по участию для неавторизованного пользователя")
    public void getClubsMembershipFilterWithoutAuthTest() {
        PaginatedClubListResponseModel response = step("Получение клубов с membership=true без авторизации", () ->
                getClubs(Map.of("search", clubBookTitle, "membership", true))
                        .spec(successfulGetClubsResponseSpec)
                        .extract().as(PaginatedClubListResponseModel.class));

        step("Проверка пустого результата для неавторизованного пользователя", () -> {
            assertThat(response.count()).isZero();
            assertThat(response.results()).isEmpty();
        });
    }

    @ParameterizedTest(name = "Ошибка чтения списка клубов при page={0}")
    @ValueSource(strings = {"0", "-1", "abc", "999999"})
    @DisplayName("Ошибка чтения списка клубов при некорректном номере страницы")
    public void getClubsWithInvalidPageTest(String invalidPage) {
        UserDetailErrorResponseModel errorResponse = step("Запрос списка клубов с page=" + invalidPage, () ->
                getClubs(Map.of("page", invalidPage))
                        .spec(notFoundClubResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка ответа (404) и сообщения о некорректной странице", () ->
                assertThat(errorResponse.detail()).isEqualTo(INVALID_PAGE_MESSAGE));
    }

    @Test
    @DisplayName("Поиск клуба по названию в списке клубов")
    public void getClubsSearchTest() {
        PaginatedClubListResponseModel response = step("Поиск клуба по названию", () ->
                getClubs(Map.of("search", clubBookTitle))
                        .spec(successfulGetClubsResponseSpec)
                        .extract().as(PaginatedClubListResponseModel.class));

        step("Проверка найденного клуба", () -> {
            assertThat(response.count()).isEqualTo(1);
            assertThat(response.results()).hasSize(1);
            assertThat(response.results().get(0).id()).isEqualTo(clubId);
            assertThat(response.results().get(0).bookTitle()).isEqualTo(clubBookTitle);
            assertThat(response.results().get(0).owner()).isEqualTo(userId);
            assertThat(response.next()).isNull();
            assertThat(response.previous()).isNull();
        });
    }

    @Test
    @DisplayName("Поиск клуба по названию без учёта регистра")
    public void getClubsSearchCaseInsensitiveTest() {
        PaginatedClubListResponseModel response = step("Поиск клуба по названию в нижнем регистре", () ->
                getClubs(Map.of("search", clubBookTitle.toLowerCase()))
                        .spec(successfulGetClubsResponseSpec)
                        .extract().as(PaginatedClubListResponseModel.class));

        step("Проверка найденного клуба", () -> {
            assertThat(response.count()).isEqualTo(1);
            assertThat(response.results().get(0).id()).isEqualTo(clubId);
        });
    }

    @Test
    @DisplayName("Пустой результат при поиске клуба по несуществующему названию")
    public void getClubsSearchWithoutMatchesTest() {
        PaginatedClubListResponseModel response = step("Поиск клуба по несуществующему названию", () ->
                getClubs(Map.of("search", clubBookTitle + "zzz"))
                        .spec(successfulGetClubsResponseSpec)
                        .extract().as(PaginatedClubListResponseModel.class));

        step("Проверка пустого результата поиска", () -> {
            assertThat(response.count()).isZero();
            assertThat(response.results()).isEmpty();
            assertThat(response.next()).isNull();
            assertThat(response.previous()).isNull();
        });
    }

    @Test
    @DisplayName("Успешное чтение клуба по id без авторизации")
    public void getClubByIdWithoutAuthTest() throws JsonProcessingException {
        String responseBody = step("Получение клуба по id", () ->
                retrieveClub(clubId)
                        .spec(successfulRetrieveClubResponseSpec)
                        .extract().asString());

        step("Проверка соответствия ответа схеме клуба", () ->
                MatcherAssert.assertThat(responseBody,
                        matchesJsonSchemaInClasspath(CLUB_RESPONSE_SCHEMA)));

        ClubResponseModel response = objectMapper.readValue(responseBody, ClubResponseModel.class);

        step("Проверка данных прочитанного клуба", () -> {
            assertThat(response.id()).isEqualTo(clubId);
            assertThat(response.bookTitle()).isEqualTo(clubBookTitle);
            assertThat(response.owner()).isEqualTo(userId);
            assertThat(response.members()).containsExactly(userId);
            assertThat(response.reviews()).isEmpty();
            assertThat(response.created()).isNotBlank();
            assertThat(response.modified()).isNull();
        });
    }

    @Test
    @DisplayName("Успешное чтение своего клуба по id с авторизацией")
    public void getOwnClubByIdWithAuthTest() {
        ClubResponseModel response = step("Получение своего клуба по id с авторизацией", () ->
                retrieveClub(clubId)
                        .spec(successfulRetrieveClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Проверка данных клуба и прав владельца", () -> {
            assertThat(response.id()).isEqualTo(clubId);
            assertThat(response.bookTitle()).isEqualTo(clubBookTitle);
            assertThat(response.owner()).isEqualTo(userId);
        });
    }

    @ParameterizedTest(name = "Ошибка чтения клуба при id={0}")
    @ValueSource(strings = {"99999999", "-1", "0"})
    @DisplayName("Ошибка чтения несуществующего клуба")
    public void getMissingClubByIdTest(String missingClubId) {
        UserDetailErrorResponseModel errorResponse = step("Получение клуба по несуществующему id=" + missingClubId, () ->
                retrieveClub(Integer.valueOf(missingClubId))
                        .spec(notFoundClubResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка ответа (404) и сообщения о ненайденном клубе", () ->
                assertThat(errorResponse.detail()).isEqualTo(CLUB_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("Ошибка чтения клуба при передаче нечислового id")
    public void getClubByNonNumericIdTest() {
        UserDetailErrorResponseModel errorResponse = step("Получение клуба по нечисловому id", () ->
                getClubsByUrl("/clubs/abc/")
                        .spec(notFoundClubResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка ответа (404) и сообщения о ненайденном ресурсе", () ->
                assertThat(errorResponse.detail()).isEqualTo(NOT_FOUND_MESSAGE));
    }
}
