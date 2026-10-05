package specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.with;
import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

public class LogoutSpec {
    public static RequestSpecification logoutRequestSpec = with()
            .log().all()
            .contentType(JSON)
            .basePath("/api/v1");

    public static ResponseSpecification successfulLogoutResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(200)
            .expectBody(equalTo("{}"))
            .log(ALL)
            .build();

    public static ResponseSpecification invalidTokenLogoutResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(401)
            .expectBody(matchesJsonSchemaInClasspath("schemas/logout/invalid_token_error_schema.json"))
            .log(ALL)
            .build();

    public static ResponseSpecification requiredRefreshLogoutResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(400)
            .expectBody(matchesJsonSchemaInClasspath("schemas/logout/required_refresh_error_schema.json"))
            .log(ALL)
            .build();
    public static RequestSpecification logoutWithoutContentTypeRequestSpec = with()
            .log().all()
            .basePath("/api/v1");

    public static ResponseSpecification logoutWithoutContentTypeResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(415)
            .expectBody(matchesJsonSchemaInClasspath("schemas/logout/media_type_error_schema.json"))
            .log(ALL)
            .build();
}
