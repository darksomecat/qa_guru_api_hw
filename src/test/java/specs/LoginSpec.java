package specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.with;
import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class LoginSpec {

    public static RequestSpecification loginRequestSpec = with()
            .log().all()
            .contentType(JSON)
            .basePath("/api/v1")
            .log().all();

    public static ResponseSpecification successfulLoginResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath("schemas/login/login_body_response_shema.json"))
            .log(ALL)
            .build();

    public static ResponseSpecification unauthorizedLoginResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(401)
            .expectBody(matchesJsonSchemaInClasspath("schemas/login/login_detail_error_schema.json"))
            .log(ALL)
            .build();

    public static ResponseSpecification badRequestLoginResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(400)
            .expectBody(matchesJsonSchemaInClasspath("schemas/login/login_error_blank_field_schema.json"))
            .log(ALL)
            .build();

    public static RequestSpecification loginWithoutContentTypeRequestSpec = with()
            .log().all()
            .basePath("/api/v1")
            .log().all();

    public static ResponseSpecification loginWithoutContentTypeResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(415)
            .expectBody(matchesJsonSchemaInClasspath("schemas/login/login_detail_error_schema.json"))
            .log(ALL)
            .build();


    public static ResponseSpecification jsonParseErrorLoginResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(400)
            .expectBody(matchesJsonSchemaInClasspath("schemas/login/login_detail_error_schema.json"))
            .log(ALL)
            .build();

}
