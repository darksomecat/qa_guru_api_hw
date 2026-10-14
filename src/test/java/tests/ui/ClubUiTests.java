package tests.ui;

import helpers.AuthHelper;
import models.club.ClubResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pages.ClubDetailsPage;
import pages.ClubsPage;
import pages.CreateClubPage;
import tests.TestBase;

import static api.ClubApiClient.createClub;
import static api.ClubApiClient.getClubsBySearch;
import static api.ClubApiClient.joinClub;
import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static specs.ClubSpec.successfulGetClubsResponseSpec;
import static tests.testData.TestData.PASSWORDHC;
import static tests.testData.TestData.newClubRequest;
import static tests.testData.TestData.searchableClubTitle;
import static tests.testData.TestData.uniqueUserName;

public class ClubUiTests extends TestBase {

    private AuthHelper.AuthSession session;
    private final ClubsPage clubsPage = new ClubsPage();
    private final ClubDetailsPage clubDetailsPage = new ClubDetailsPage();
    private final CreateClubPage createClubPage = new CreateClubPage();

    @BeforeEach
    public void prepareUser() {
        step("Подготовка пользователя через API и авторизация в UI", () -> {
            session = AuthHelper.loginAs(uniqueUserName("uiclub"), PASSWORDHC);
            AuthHelper.injectUiAuth(session);
        });
    }

    @Test
    @DisplayName("UI: поиск клуба, созданного через API")
    public void searchClubCreatedViaApiTest() {
        String title = searchableClubTitle();

        step("Создание клуба через API", () ->
                createClub(newClubRequest(title), session.accessToken())
                        .extract().as(ClubResponseModel.class));

        step("Поиск клуба на UI", () ->
                clubsPage.open()
                        .searchClub(title)
                        .shouldHaveClub(title));
    }

    @Test
    @DisplayName("UI: создание клуба через форму")
    public void createClubViaUiTest() {
        String title = searchableClubTitle();

        step("Создание клуба через UI", () ->
                createClubPage.open()
                        .createClub(
                                title,
                                "UI Author",
                                2020,
                                "UI club description",
                                "https://t.me/uiclub" + System.currentTimeMillis()
                        ));

        step("Проверка, что клуб создан через API-поиск", () -> {
            var found = getClubsBySearch(title)
                    .spec(successfulGetClubsResponseSpec)
                    .extract().path("count");
            assertThat((Integer) found).isEqualTo(1);
        });

        step("Проверка отображения клуба в поиске UI", () ->
                clubsPage.open()
                        .searchClub(title)
                        .shouldHaveClub(title));
    }

    @Test
    @DisplayName("UI: открытие страницы клуба и вступление")
    public void openClubAndJoinViaUiTest() {
        String title = searchableClubTitle();
        AuthHelper.AuthSession owner = AuthHelper.loginAs(uniqueUserName("uiclubowner"), PASSWORDHC);

        Integer clubId = step("Создание клуба другим пользователем через API", () ->
                createClub(newClubRequest(title), owner.accessToken())
                        .extract().as(ClubResponseModel.class).id());

        step("Открытие страницы клуба и вступление через UI", () ->
                clubDetailsPage.open(clubId)
                        .shouldHaveTitle(title)
                        .joinClub());

        step("Проверка членства через API", () -> {
            ClubResponseModel club = api.ClubApiClient.retrieveClub(clubId)
                    .extract().as(ClubResponseModel.class);
            assertThat(club.members()).contains(session.userId());
        });
    }

    @Test
    @DisplayName("UI: участник видит страницу клуба после join через API")
    public void openJoinedClubDetailsTest() {
        String title = searchableClubTitle();
        AuthHelper.AuthSession owner = AuthHelper.loginAs(uniqueUserName("uiclubown2"), PASSWORDHC);

        Integer clubId = step("Создание клуба и вступление через API", () -> {
            Integer id = createClub(newClubRequest(title), owner.accessToken())
                    .extract().as(ClubResponseModel.class).id();
            joinClub(id, session.accessToken()).statusCode(is(204));
            return id;
        });

        step("Открытие страницы клуба на UI", () ->
                clubDetailsPage.open(clubId)
                        .shouldHaveTitle(title));
    }
}
