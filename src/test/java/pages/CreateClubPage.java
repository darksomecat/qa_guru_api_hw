package pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class CreateClubPage extends BasePage {

    private final SelenideElement bookTitle = $("#bookTitle");
    private final SelenideElement bookAuthors = $("#bookAuthors");
    private final SelenideElement publicationYear = $("#publicationYear");
    private final SelenideElement description = $("#description");
    private final SelenideElement telegramChatLink = $("#telegramChatLink");
    private final SelenideElement submitButton = $(".submit-btn");

    @Step("Открыть форму создания клуба")
    public CreateClubPage open() {
        openPath("/clubs/create");
        bookTitle.shouldBe(visible);
        return this;
    }

    @Step("Заполнить и отправить форму создания клуба")
    public ClubsPage createClub(String title, String authors, int year, String desc, String telegram) {
        bookTitle.setValue(title);
        bookAuthors.setValue(authors);
        publicationYear.setValue(String.valueOf(year));
        description.setValue(desc);
        telegramChatLink.setValue(telegram);
        submitButton.shouldBe(visible).click();
        return new ClubsPage();
    }
}
