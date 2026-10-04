package tests.login;

import net.datafaker.Faker;

public class TestData {
    private final Faker faker = new Faker();

    // Каждое обращение к этим переменным создаст новые данные
    public final String username = "autotest" + faker.name().firstName();
    public final String password = faker.name().lastName();
}
