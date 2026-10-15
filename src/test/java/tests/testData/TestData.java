package tests.testData;

import net.datafaker.Faker;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static utils.GeneratedUtils.getRandomString;

public class TestData {
    private static final Faker FAKER = new Faker();
    private final Faker faker = FAKER;

    public String username = "autotest" + faker.name().firstName().toLowerCase() + faker.number().digits(8);
    public String password = faker.internet().password(8, 16);
    public String maxLength150Symbols = getRandomString(150);
    public String tooLong151Symbols = getRandomString(151);
    public String maxPassword = getRandomString(128);
    public String tooLongPassword = getRandomString(129);
    public static final Map<String, Object> invalidDataTypesJson = Map.of(
            "username", Map.of("nested", "object"),
            "password", List.of("array", "of", "strings")
    );
    public static final String malformedJson = """
            {
              "username": "{ "nested": "object" }",
              "password": "["array", "of", "strings"]"
            }
            """;
    public static final String USERNAMEHC = "autotestdarksome";
    public String maxLength254Symbols = getRandomString(242) + "@example.com";
    public String tooLong255Symbols = getRandomString(243) + "@example.com";
    public static final String PASSWORDHC = "autotestpass";
    public static final String INVALID_REFRESH_TOKEN = "invalid_token_value";
    public static final String CLUB_NOT_FOUND_MESSAGE = "No Club matches the given query.";
    public static final String REVIEW_NOT_FOUND_MESSAGE = "No BookReview matches the given query.";
    public static final String NO_PERMISSION_MESSAGE = "You do not have permission to perform this action.";
    public static final String AUTHENTICATION_MESSAGE = "Authentication credentials were not provided.";
    public static final String INVALID_PAGE_MESSAGE = "Invalid page.";
    public static final String NOT_FOUND_MESSAGE = "Not found.";
    public String updatedFirstName = faker.name().firstName();
    public String updatedLastName = faker.name().lastName();
    public String updatedEmail = faker.internet().emailAddress();
    public String clubBookTitle = "Autotest Club " + getRandomString(12) + uniqueId();
    public String clubBookAuthors = "Autotest Author " + getRandomString(12) + uniqueId();
    public Integer clubPublicationYear = faker.number().numberBetween(1900, 2025);
    public String clubDescription = "Autotest description " + getRandomString(20) + uniqueId();
    public String clubTelegramChatLink = "https://t.me/autotest" + faker.number().digits(10);
    public String maxLength255Symbols = getRandomString(255);
    public String tooLong256Symbols = getRandomString(256);
    public String maxLength200Symbols = "https://t.me/" + getRandomString(187);
    public String tooLong201Symbols = "https://t.me/" + getRandomString(188);
    public static final Map<String, Object> invalidClubDataTypesJson = Map.of(
            "bookTitle", List.of("array", "of", "strings"),
            "bookAuthors", List.of("array", "of", "strings"),
            "publicationYear", "not_a_number",
            "description", List.of("array", "of", "strings"),
            "telegramChatLink", Map.of("nested", "object")
    );

    public static String uniqueId() {
        return FAKER.number().digits(10);
    }

    public static Map<String, Object> clubRequiredFields() {
        Map<String, Object> clubData = new LinkedHashMap<>();
        clubData.put("bookTitle", "Autotest Club " + getRandomString(12) + uniqueId());
        clubData.put("bookAuthors", "Autotest Author " + getRandomString(12) + uniqueId());
        clubData.put("publicationYear", FAKER.number().numberBetween(1900, 2025));
        clubData.put("description", "Autotest description " + getRandomString(20) + uniqueId());
        clubData.put("telegramChatLink", "https://t.me/autotest" + FAKER.number().digits(10));

        return clubData;
    }

    public static String searchableClubTitle() {
        return "Autotest Search " + getRandomString(12) + uniqueId();
    }

    public static String otherUserName() {
        return "autotestclubother" + FAKER.name().firstName().toLowerCase() + FAKER.number().digits(8);
    }

    public static String patcherUserName() {
        return "autotestclubpatcher" + FAKER.name().firstName().toLowerCase() + FAKER.number().digits(8);
    }

    public static Map<String, Object> newClubRequest(String bookTitle) {
        Map<String, Object> clubData = clubRequiredFields();
        clubData.put("bookTitle", bookTitle);

        return clubData;
    }

    public static Map<String, Object> updatedClubFields() {
        Map<String, Object> clubData = new LinkedHashMap<>();
        clubData.put("bookTitle", "Updated Club " + getRandomString(12) + uniqueId());
        clubData.put("bookAuthors", "Updated Author " + getRandomString(12) + uniqueId());
        clubData.put("publicationYear", FAKER.number().numberBetween(1900, 2025));
        clubData.put("description", "Updated description " + getRandomString(20) + uniqueId());
        clubData.put("telegramChatLink", "https://t.me/updated" + FAKER.number().digits(10));

        return clubData;
    }

    public static Map<String, Object> patchedClubRequest(String bookTitle) {
        Map<String, Object> clubData = new LinkedHashMap<>();
        clubData.put("bookTitle", bookTitle);

        return clubData;
    }

    public static Map<String, Object> clubReviewRequest(Integer clubId) {
        return clubReviewRequest(clubId, "Autotest club review " + getRandomString(20) + uniqueId(), 4, 10);
    }

    public static Map<String, Object> clubReviewRequest(Integer clubId, String review, Integer assessment,
                                                        Integer readPages) {
        Map<String, Object> reviewData = new LinkedHashMap<>();
        reviewData.put("club", clubId);
        reviewData.put("review", review);
        reviewData.put("assessment", assessment);
        reviewData.put("readPages", readPages);

        return reviewData;
    }

    public static String reviewerUserName() {
        return "autotestreviewer" + FAKER.name().firstName().toLowerCase() + FAKER.number().digits(8);
    }

    public static String uniqueUserName(String prefix) {
        return prefix + FAKER.name().firstName().toLowerCase() + FAKER.number().digits(8);
    }
}
