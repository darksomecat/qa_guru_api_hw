package pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class ClubsPage extends BasePage {

    private final SelenideElement searchInput = $("[placeholder='Поиск книжных клубов...']");
    private final SelenideElement searchButton = $$("button").findBy(text("Найти"));
    private final SelenideElement createClubLink = $("[data-testid='create-club-link']");
    private final ElementsCollection clubCards = $$(".club-card");

    @Step("Открыть страницу клубов")
    public ClubsPage open() {
        openPath("/");
        searchInput.shouldBe(visible);
        return this;
    }

    @Step("Найти клуб по названию: {title}")
    public ClubsPage searchClub(String title) {
        searchInput.shouldBe(visible).setValue(title);
        searchButton.click();
        return this;
    }

    @Step("Проверить, что в списке есть клуб: {title}")
    public ClubsPage shouldHaveClub(String title) {
        clubCards.findBy(text(title)).shouldBe(visible);
        return this;
    }

    @Step("Проверить, что клубы не найдены")
    public ClubsPage shouldShowNoResults() {
        $(".no-results").shouldHave(text("Клубы не найдены"));
        return this;
    }

    @Step("Перейти к созданию клуба")
    public CreateClubPage openCreateClub() {
        createClubLink.shouldBe(visible).click();
        return new CreateClubPage();
    }
}
