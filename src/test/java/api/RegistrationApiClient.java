package api;

import io.restassured.response.ValidatableResponse;
import models.registration.RegistrationBodyModel;
import models.registration.RegistrationErrorResponseModel;

import static io.restassured.RestAssured.given;
import static specs.RegistrationSpec.badRequestRegistrationResponseSpec;

public class RegistrationApiClient extends BaseApiClient {

    public static ValidatableResponse register(RegistrationBodyModel registrationBody) {
        return given(request())
                .body(registrationBody)
                .when()
                .post(REGISTER_PATH)
                .then();
    }

    public static ValidatableResponse registerWithEmptyBody() {
        return given(request())
                .body(EMPTY_BODY)
                .when()
                .post(REGISTER_PATH)
                .then();
    }

    public static RegistrationErrorResponseModel registerWithError(RegistrationBodyModel registrationBody) {
        return register(registrationBody)
                .spec(badRequestRegistrationResponseSpec)
                .extract().as(RegistrationErrorResponseModel.class);
    }

    public static RegistrationErrorResponseModel registerWithEmptyBodyError() {
        return registerWithEmptyBody()
                .spec(badRequestRegistrationResponseSpec)
                .extract().as(RegistrationErrorResponseModel.class);
    }

    private RegistrationApiClient() {
    }
}