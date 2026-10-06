package specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class LoginSpec {

    public static ResponseSpecification successfulLoginResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath("schemas/login/login_body_response_shema.json"))
            .log(ALL)
            .build();

    public static ResponseSpecification unauthorizedLoginResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(401)
            .expectBody(matchesJsonSchemaInClasspath("schemas/login/login_detail_error_schema.json"))
            .log(ALL)
            .build();

    public static ResponseSpecification badRequestLoginResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(400)
            .expectBody(matchesJsonSchemaInClasspath("schemas/login/login_error_blank_field_schema.json"))
            .log(ALL)
            .build();

    public static ResponseSpecification loginWithoutContentTypeResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(415)
            .expectBody(matchesJsonSchemaInClasspath("schemas/login/login_detail_error_schema.json"))
            .log(ALL)
            .build();


    public static ResponseSpecification jsonParseErrorLoginResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(400)
            .expectBody(matchesJsonSchemaInClasspath("schemas/login/login_detail_error_schema.json"))
            .log(ALL)
            .build();

}
