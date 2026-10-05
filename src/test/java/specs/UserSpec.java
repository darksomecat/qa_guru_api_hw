package specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.with;
import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class UserSpec {

    public static RequestSpecification updateUserRequestSpec = with()
            .log().all()
            .contentType(JSON)
            .basePath("/api/v1");

    public static ResponseSpecification successfulUpdateUserResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath("schemas/user/user_response_schema.json"))
            .log(ALL)
            .build();
    public static ResponseSpecification badRequestUserResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(400)
            .expectBody(matchesJsonSchemaInClasspath("schemas/user/bad_request_user_schema.json"))
            .log(ALL)
            .build();
    public static ResponseSpecification unauthorizedUserResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(401)
            .expectBody(matchesJsonSchemaInClasspath("schemas/user/detail_error_schema.json"))
            .log(ALL)
            .build();
}
