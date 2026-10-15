package tests.testData;

import net.datafaker.Faker;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static utils.GeneratedUtils.getRandomString;

public class TestData {
    private static final Faker FAKER = new Faker();
    private final Faker faker = FAKER;
    private static final String TELEGRAM_PREFIX = "https://t.me/";

    public String username = uniqueUserName("autotest");
    public String password = faker.internet().password(8, 16);

    public String maxLength150Symbols = getRandomString(150);
    public String tooLong151Symbols = getRandomString(151);
    public String maxPassword = getRandomString(128);
    public String tooLongPassword = getRandomString(129);
    public String maxLength255Symbols = getRandomString(255);
    public String tooLong256Symbols = getRandomString(256);
    public String maxLength254Symbols = getRandomString(242) + "@example.com";
    public String tooLong255Symbols = getRandomString(243) + "@example.com";
    public String maxLength200Symbols = TELEGRAM_PREFIX + getRandomString(187);
    public String tooLong201Symbols = TELEGRAM_PREFIX + getRandomString(188);

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

    private final Map<String, Object> club = clubRequiredFields();
    public String clubBookTitle = (String) club.get("bookTitle");
    public String clubBookAuthors = (String) club.get("bookAuthors");
    public Integer clubPublicationYear = (Integer) club.get("publicationYear");
    public String clubDescription = (String) club.get("description");
    public String clubTelegramChatLink = (String) club.get("telegramChatLink");

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

    public static String uniqueText(String prefix) {
        return prefix + getRandomString(12) + uniqueId();
    }

    public static String uniqueUserName(String prefix) {
        return prefix + FAKER.name().firstName().toLowerCase() + FAKER.number().digits(8);
    }

    public static String searchableClubTitle() {
        return uniqueText("Autotest Search ");
    }

    public static String telegramLink(String suffix) {
        return TELEGRAM_PREFIX + suffix;
    }

    public static Map<String, Object> clubRequiredFields() {
        Map<String, Object> clubData = new LinkedHashMap<>();
        clubData.put("bookTitle", uniqueText("Autotest Club "));
        clubData.put("bookAuthors", uniqueText("Autotest Author "));
        clubData.put("publicationYear", FAKER.number().numberBetween(1900, 2025));
        clubData.put("description", uniqueText("Autotest description "));
        clubData.put("telegramChatLink", telegramLink("autotest" + uniqueId()));
        return clubData;
    }

    public static Map<String, Object> newClubRequest(String bookTitle) {
        Map<String, Object> clubData = clubRequiredFields();
        clubData.put("bookTitle", bookTitle);
        return clubData;
    }

    public static Map<String, Object> updatedClubFields() {
        Map<String, Object> clubData = new LinkedHashMap<>();
        clubData.put("bookTitle", uniqueText("Updated Club "));
        clubData.put("bookAuthors", uniqueText("Updated Author "));
        clubData.put("publicationYear", FAKER.number().numberBetween(1900, 2025));
        clubData.put("description", uniqueText("Updated description "));
        clubData.put("telegramChatLink", telegramLink("updated" + uniqueId()));
        return clubData;
    }

    public static Map<String, Object> patchedClubRequest(String bookTitle) {
        Map<String, Object> clubData = new LinkedHashMap<>();
        clubData.put("bookTitle", bookTitle);
        return clubData;
    }

    public static Map<String, Object> clubReviewRequest(Integer clubId) {
        return clubReviewRequest(clubId, uniqueText("Autotest club review "), 4, 10);
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
}
