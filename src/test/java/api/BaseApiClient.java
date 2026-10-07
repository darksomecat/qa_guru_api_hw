package api;

import io.restassured.specification.RequestSpecification;

import static specs.BaseSpec.baseRequestSpec;

public abstract class BaseApiClient {

    protected static final String REGISTER_PATH = "/users/register/";
    protected static final String LOGIN_PATH = "/auth/token/";
    protected static final String LOGOUT_PATH = "/auth/logout/";
    protected static final String USER_ME_PATH = "/users/me/";

    protected static final String EMPTY_BODY = "{}";

    protected static RequestSpecification request() {
        return baseRequestSpec;
    }

    protected BaseApiClient() {
    }
}
