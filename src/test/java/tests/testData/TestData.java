package tests.testData;

import net.datafaker.Faker;

import java.util.List;
import java.util.Map;

import static utils.GeneretedUtils.getRandomString;

public class TestData {
    private final Faker faker = new Faker();

    public String username = "Autotest" + faker.name().firstName();
    public String password = faker.name().lastName();
    public static String maxUsername = getRandomString(150);
    public static String tooLongUsername =  getRandomString(151);
    public static String maxPassword = getRandomString(128);
    public static String tooLongPassword =  getRandomString(129);
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
   
}
