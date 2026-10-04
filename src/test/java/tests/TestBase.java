package tests;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import tests.login.TestData;

public class TestBase {
    protected TestData testData;
    @BeforeEach
    public void initTestData() {
        testData = new TestData();
    }
    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "https://book-club.qa.guru";
    }
}