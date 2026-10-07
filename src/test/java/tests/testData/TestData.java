package tests.testData;

import net.datafaker.Faker;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static utils.GeneretedUtils.getRandomHex;
import static utils.GeneretedUtils.getRandomString;

public class TestData {
    private final Faker faker = new Faker();

    public String username = "Autotestsforclub" + faker.name().firstName();
    public String password = faker.name().lastName();
    public String maxLeght150Symbols = getRandomString(150);
    public String tooLong151Symbols =  getRandomString(151);
    public String maxPassword = getRandomString(128);
    public String tooLongPassword =  getRandomString(129);
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
    public String maxLeght254Symbols = getRandomString(242) + "@example.com";
    public String tooLong255Symbols = getRandomString(243) + "@example.com";
    public static final String PASSWORDHC = "autotestpass";
    public static final String INVALID_REFRESH_TOKEN = "invalid_token_value";
    public static final String CLUB_NOT_FOUND_MESSAGE = "No Club matches the given query.";
    public static final String NO_PERMISSION_MESSAGE = "You do not have permission to perform this action.";
    public static final String AUTHENTICATION_MESSAGE = "Authentication credentials were not provided.";
    public static final String INVALID_PAGE_MESSAGE = "Invalid page.";
    public static final String NOT_FOUND_MESSAGE = "Not found.";
    public String updatedFirstName = faker.name().firstName();
    public String updatedLastName = faker.name().lastName();
    public String updatedEmail = faker.internet().emailAddress();
    public String clubBookTitle = "Autotest Club " + getRandomHex();
    public String clubBookAuthors = faker.book().author();
    public Integer clubPublicationYear = faker.number().numberBetween(1900, 2025);
    public String clubDescription = faker.lorem().sentence();
    public String clubTelegramChatLink = "https://t.me/autotest" + getRandomHex();
    public String maxLeght255Symbols = getRandomString(255);
    public String tooLong256Symbols = getRandomString(256);
    public String maxLeght200Symbols = "https://t.me/" + getRandomString(187);
    public String tooLong201Symbols = "https://t.me/" + getRandomString(188);
    public static final Map<String, Object> invalidClubDataTypesJson = Map.of(
            "bookTitle", List.of("array", "of", "strings"),
            "bookAuthors", List.of("array", "of", "strings"),
            "publicationYear", "not_a_number",
            "description", List.of("array", "of", "strings"),
            "telegramChatLink", Map.of("nested", "object")
    );

    public static Map<String, Object> clubRequiredFields() {
        Map<String, Object> clubData = new LinkedHashMap<>();
        clubData.put("bookTitle", "Autotest Club " + getRandomHex());
        clubData.put("bookAuthors", "Autotest Author");
        clubData.put("publicationYear", 1999);
        clubData.put("description", "Autotest club description");
        clubData.put("telegramChatLink", "https://t.me/autotest" + getRandomHex());

        return clubData;
    }

    public static String searchableClubTitle() {
        return "Autotest Search " + getRandomHex();
    }

    public static String otherUserName() {
        return "autotestclubother" + getRandomHex();
    }

    public static String patcherUserName() {
        return "autotestclubpatcher" + getRandomHex();
    }

    public static Map<String, Object> newClubRequest(String bookTitle) {
        Map<String, Object> clubData = clubRequiredFields();
        clubData.put("bookTitle", bookTitle);

        return clubData;
    }

    public static Map<String, Object> updatedClubFields() {
        Map<String, Object> clubData = new LinkedHashMap<>();
        clubData.put("bookTitle", "Updated Club " + getRandomHex());
        clubData.put("bookAuthors", "Updated Author " + getRandomHex());
        clubData.put("publicationYear", 2001);
        clubData.put("description", "Updated club description");
        clubData.put("telegramChatLink", "https://t.me/updated" + getRandomHex());

        return clubData;
    }

    public static Map<String, Object> patchedClubRequest(String bookTitle) {
        Map<String, Object> clubData = new LinkedHashMap<>();
        clubData.put("bookTitle", bookTitle);

        return clubData;
    }

    public static Map<String, Object> clubReviewRequest(Integer clubId) {
        Map<String, Object> reviewData = new LinkedHashMap<>();
        reviewData.put("club", clubId);
        reviewData.put("review", "Autotest club review " + getRandomHex());
        reviewData.put("assessment", 4);
        reviewData.put("readPages", 10);

        return reviewData;
    }
}
