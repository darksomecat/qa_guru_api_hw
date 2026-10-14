package specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class ReviewSpec {

    private static final String REVIEW_SCHEMA = "schemas/review/book_review_schema.json";
    private static final String PAGINATED_SCHEMA = "schemas/review/paginated_book_review_list_schema.json";

    public static ResponseSpecification successfulCreateReviewResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(201)
            .expectBody(matchesJsonSchemaInClasspath(REVIEW_SCHEMA))
            .expectBody("id", notNullValue())
            .log(ALL)
            .build();

    public static ResponseSpecification successfulRetrieveReviewResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath(REVIEW_SCHEMA))
            .log(ALL)
            .build();

    public static ResponseSpecification successfulGetReviewsResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath(PAGINATED_SCHEMA))
            .log(ALL)
            .build();

    public static ResponseSpecification successfulUpdateReviewResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath(REVIEW_SCHEMA))
            .log(ALL)
            .build();

    public static ResponseSpecification successfulDeleteReviewResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(204)
            .expectBody(equalTo(""))
            .log(ALL)
            .build();

    public static ResponseSpecification badRequestReviewResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(400)
            .expectBody(matchesJsonSchemaInClasspath("schemas/review/bad_request_review_schema.json"))
            .log(ALL)
            .build();

    public static ResponseSpecification unauthorizedReviewResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(401)
            .expectBody(matchesJsonSchemaInClasspath("schemas/review/detail_error_schema.json"))
            .log(ALL)
            .build();

    public static ResponseSpecification forbiddenReviewResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(403)
            .expectBody(matchesJsonSchemaInClasspath("schemas/review/detail_error_schema.json"))
            .log(ALL)
            .build();

    public static ResponseSpecification notFoundReviewResponseSpec = new ResponseSpecBuilder()
            .expectStatusCode(404)
            .expectBody(matchesJsonSchemaInClasspath("schemas/review/detail_error_schema.json"))
            .log(ALL)
            .build();
}
