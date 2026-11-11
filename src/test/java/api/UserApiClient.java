package api;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import models.user.UpdateUserBodyModel;

import static io.restassured.RestAssured.given;

public class UserApiClient extends BaseApiClient {

    @Step("PUT /users/me/ — полное обновление профиля пользователя")
    public static ValidatableResponse updateUser(UpdateUserBodyModel updateBody, String accessToken) {
        return given(request())
                .header("Authorization", "Bearer " + accessToken)
                .body(updateBody)
                .when()
                .put(USER_ME_PATH)
                .then();
    }

    @Step("PATCH /users/me/ — частичное обновление профиля пользователя")
    public static ValidatableResponse patchUser(UpdateUserBodyModel updateBody, String accessToken) {
        return given(request())
                .header("Authorization", "Bearer " + accessToken)
                .body(updateBody)
                .when()
                .patch(USER_ME_PATH)
                .then();
    }

    @Step("PUT /users/me/ — полное обновление профиля без Authorization")
    public static ValidatableResponse updateUserWithoutAuth(UpdateUserBodyModel updateBody) {
        return given(request())
                .body(updateBody)
                .when()
                .put(USER_ME_PATH)
                .then();
    }

    @Step("PATCH /users/me/ — частичное обновление профиля без Authorization")
    public static ValidatableResponse patchUserWithoutAuth(UpdateUserBodyModel updateBody) {
        return given(request())
                .body(updateBody)
                .when()
                .patch(USER_ME_PATH)
                .then();
    }

    private UserApiClient() {
    }
}
