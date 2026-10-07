package api;

import io.restassured.response.ValidatableResponse;
import models.logout.LogoutBodyModel;

import static io.restassured.RestAssured.given;
import static specs.BaseSpec.withoutContentTypeRequestSpec;
import static specs.LogoutSpec.logoutWithoutContentTypeResponseSpec;
import static specs.LogoutSpec.successfulLogoutResponseSpec;

public class LogoutApiClient extends BaseApiClient {

    public static void logout(LogoutBodyModel logoutBody) {
        logoutWithToken(logoutBody)
                .spec(successfulLogoutResponseSpec);
    }

    public static ValidatableResponse logoutWithToken(LogoutBodyModel logoutBody) {
        return given(request())
                .body(logoutBody)
                .when()
                .post(LOGOUT_PATH)
                .then();
    }

    public static ValidatableResponse logoutWithEmptyBody() {
        return given(request())
                .body(EMPTY_BODY)
                .when()
                .post(LOGOUT_PATH)
                .then();
    }

    public static String logoutWithoutContentType(LogoutBodyModel logoutBody) {
        return given(withoutContentTypeRequestSpec)
                .body(logoutBody)
                .when()
                .post(LOGOUT_PATH)
                .then()
                .spec(logoutWithoutContentTypeResponseSpec)
                .extract()
                .path("detail");
    }

    private LogoutApiClient() {
    }
}
