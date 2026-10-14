package specs;

import io.restassured.specification.RequestSpecification;

import static allure.CustomAllureListener.withCustomTemplate;
import static io.restassured.RestAssured.with;
import static io.restassured.http.ContentType.JSON;

public class BaseSpec {

    private static final String BASE_URI = System.getProperty("apiUrl");

    public static RequestSpecification baseRequestSpec = with()
            .filter(withCustomTemplate())
            .log().all()
            .contentType(JSON)
            .baseUri(BASE_URI)
            .basePath("/api/v1");

    public static RequestSpecification withoutContentTypeRequestSpec = with()
            .filter(withCustomTemplate())
            .log().all()
            .baseUri(BASE_URI)
            .basePath("/api/v1");
}
