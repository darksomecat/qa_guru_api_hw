package specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class ClubSpec {

    public static final String CLUB_RESPONSE_SCHEMA = "schemas/club/club_response_schema.json";
    public static final String PAGINATED_CLUB_LIST_SCHEMA = "schemas/club/paginated_club_list_schema.json";

    public static ResponseSpecification successfulCreateClubResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(201)
            .expectBody(matchesJsonSchemaInClasspath(CLUB_RESPONSE_SCHEMA))
            .expectBody("id", notNullValue())
            .expectBody("reviews", empty())
            .log(ALL)
            .build();

    public static ResponseSpecification successfulRetrieveClubResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath(CLUB_RESPONSE_SCHEMA))
            .log(ALL)
            .build();

    public static ResponseSpecification successfulGetClubsResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath(PAGINATED_CLUB_LIST_SCHEMA))
            .log(ALL)
            .build();

    public static ResponseSpecification notFoundClubResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(404)
            .expectBody(matchesJsonSchemaInClasspath("schemas/club/detail_error_schema.json"))
            .log(ALL)
            .build();

    public static ResponseSpecification successfulUpdateClubResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath(CLUB_RESPONSE_SCHEMA))
            .expectBody("id", notNullValue())
            .log(ALL)
            .build();

    public static ResponseSpecification forbiddenClubResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(403)
            .expectBody(matchesJsonSchemaInClasspath("schemas/club/detail_error_schema.json"))
            .log(ALL)
            .build();

    public static ResponseSpecification successfulDeleteClubResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(204)
            .expectBody(equalTo(""))
            .log(ALL)
            .build();

    public static ResponseSpecification badRequestClubResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(400)
            .expectBody(matchesJsonSchemaInClasspath("schemas/club/bad_request_club_schema.json"))
            .log(ALL)
            .build();

    public static ResponseSpecification unauthorizedClubResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(401)
            .expectBody(matchesJsonSchemaInClasspath("schemas/club/detail_error_schema.json"))
            .log(ALL)
            .build();
}
