package tests.club;

import helpers.AuthHelper;
import models.club.ClubResponseModel;
import models.user.UserDetailErrorResponseModel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tests.TestBase;

import static api.ClubApiClient.createClub;
import static api.ClubApiClient.joinClub;
import static api.ClubApiClient.leaveClub;
import static api.ClubApiClient.retrieveClub;
import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static specs.ClubSpec.successfulRetrieveClubResponseSpec;
import static specs.ClubSpec.unauthorizedClubResponseSpec;
import static tests.testData.TestData.AUTHENTICATION_MESSAGE;
import static tests.testData.TestData.PASSWORDHC;
import static tests.testData.TestData.newClubRequest;
import static tests.testData.TestData.searchableClubTitle;
import static tests.testData.TestData.uniqueUserName;

public class ClubMembersTests extends TestBase {

    private static AuthHelper.AuthSession owner;
    private static AuthHelper.AuthSession member;
    private static Integer clubId;

    @BeforeAll
    public static void auth() {
        step("Подготовка пользователей через API", () -> {
            owner = AuthHelper.loginAs(uniqueUserName("clubowner"), PASSWORDHC);
            member = AuthHelper.loginAs(uniqueUserName("clubmember"), PASSWORDHC);
        });
    }

    @BeforeEach
    public void prepareClub() {
        step("Создание клуба через API", () ->
                clubId = createClub(newClubRequest(searchableClubTitle()), owner.accessToken())
                        .extract().as(ClubResponseModel.class).id());
    }

    @Test
    @DisplayName("Успешное вступление в клуб и выход из клуба")
    public void joinAndLeaveClubTest() {
        step("Вступление в клуб методом POST /members/me/", () ->
                joinClub(clubId, member.accessToken()).statusCode(is(204)));

        ClubResponseModel afterJoin = step("Получение клуба после вступления", () ->
                retrieveClub(clubId)
                        .spec(successfulRetrieveClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Проверка, что пользователь стал участником", () ->
                assertThat(afterJoin.members()).contains(owner.userId(), member.userId()));

        step("Выход из клуба методом DELETE /members/me/", () ->
                leaveClub(clubId, member.accessToken()).statusCode(is(204)));

        ClubResponseModel afterLeave = step("Получение клуба после выхода", () ->
                retrieveClub(clubId)
                        .spec(successfulRetrieveClubResponseSpec)
                        .extract().as(ClubResponseModel.class));

        step("Проверка, что пользователь больше не участник", () -> {
            assertThat(afterLeave.members()).contains(owner.userId());
            assertThat(afterLeave.members()).doesNotContain(member.userId());
        });
    }

    @Test
    @DisplayName("Ошибка вступления в клуб без Authorization")
    public void joinClubWithoutAuthTest() {
        UserDetailErrorResponseModel error = step("Вступление в клуб без Authorization", () ->
                joinClub(clubId, null)
                        .spec(unauthorizedClubResponseSpec)
                        .extract().as(UserDetailErrorResponseModel.class));

        step("Проверка ответа 401", () ->
                assertThat(error.detail()).isEqualTo(AUTHENTICATION_MESSAGE));
    }
}
