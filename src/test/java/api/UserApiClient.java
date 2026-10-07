package api;

import io.restassured.response.ValidatableResponse;
import models.user.UpdateUserBodyModel;

import static io.restassured.RestAssured.given;

public class UserApiClient extends BaseApiClient {

    public static ValidatableResponse updateUser(UpdateUserBodyModel updateBody, String accessToken) {
        return given(request())
                .header("Authorization", "Bearer " + accessToken)
                .body(updateBody)
                .when()
                .put(USER_ME_PATH)
                .then();
    }

    public static ValidatableResponse patchUser(UpdateUserBodyModel updateBody, String accessToken) {
        return given(request())
                .header("Authorization", "Bearer " + accessToken)
                .body(updateBody)
                .when()
                .patch(USER_ME_PATH)
                .then();
    }

    public static ValidatableResponse updateUserWithoutAuth(UpdateUserBodyModel updateBody) {
        return given(request())
                .body(updateBody)
                .when()
                .put(USER_ME_PATH)
                .then();
    }

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