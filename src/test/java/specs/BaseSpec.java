package specs;

import io.restassured.specification.RequestSpecification;

import static allure.CustomAllureListener.withCustomTemplate;
import static io.restassured.RestAssured.with;
import static io.restassured.http.ContentType.JSON;

public class BaseSpec {

    private static final String BASE_URI = resolveBaseUri();

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

    private static String resolveBaseUri() {
        String apiUrl = System.getProperty("apiUrl");
        if (apiUrl == null || apiUrl.isBlank()) {
            throw new IllegalStateException(
                    "System property 'apiUrl' is not set. Pass -DapiUrl=https://... to Gradle/Jenkins."
            );
        }
        return apiUrl;
    }
}
