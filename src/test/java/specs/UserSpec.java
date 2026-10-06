package specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class UserSpec {

    public static ResponseSpecification successfulUpdateUserResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath("schemas/user/user_response_schema.json"))
            .log(ALL)
            .build();
    public static ResponseSpecification badRequestUserResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(400)
            .expectBody(matchesJsonSchemaInClasspath("schemas/user/bad_request_user_schema.json"))
            .log(ALL)
            .build();
    public static ResponseSpecification unauthorizedUserResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(401)
            .expectBody(matchesJsonSchemaInClasspath("schemas/user/detail_error_schema.json"))
            .log(ALL)
            .build();
}
