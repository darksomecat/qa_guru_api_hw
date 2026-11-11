package pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class ClubDetailsPage extends BasePage {

    private final SelenideElement title = $(".club-content h1");
    private final SelenideElement joinButton = $(".join-btn");
    private final SelenideElement leaveButton = $(".leave-btn");
    private final SelenideElement addReviewButton = $(".add-review-btn");
    private final SelenideElement reviewTextarea = $("#review");
    private final SelenideElement assessmentInput = $("#assessment");
    private final SelenideElement readPagesInput = $("#readPages");
    private final SelenideElement publishButton = $$("button").findBy(text("Опубликовать"));
    private final SelenideElement saveButton = $$("button").findBy(text("Сохранить"));
    private final SelenideElement reviewsList = $(".reviews-list");

    @Step("Открыть страницу клуба #{clubId}")
    public ClubDetailsPage open(Integer clubId) {
        openPath("/clubs/" + clubId);
        title.shouldBe(visible);
        return this;
    }

    @Step("Проверить название клуба: {bookTitle}")
    public ClubDetailsPage shouldHaveTitle(String bookTitle) {
        title.shouldHave(text(bookTitle));
        return this;
    }

    @Step("Присоединиться к клубу")
    public ClubDetailsPage joinClub() {
        joinButton.shouldBe(visible).click();
        leaveButton.shouldBe(visible);
        return this;
    }

    @Step("Проверить, что пользователь является участником клуба")
    public ClubDetailsPage shouldBeMember() {
        leaveButton.shouldBe(visible);
        return this;
    }

    @Step("Дождаться возможности написать отзыв")
    public ClubDetailsPage shouldAllowCreateReview() {
        addReviewButton.shouldBe(visible);
        return this;
    }

    @Step("Написать отзыв: {reviewText}, оценка {assessment}, страниц {readPages}")
    public ClubDetailsPage createReview(String reviewText, int assessment, int readPages) {
        addReviewButton.shouldBe(visible).click();
        assessmentInput.setValue(String.valueOf(assessment));
        readPagesInput.setValue(String.valueOf(readPages));
        reviewTextarea.setValue(reviewText);
        publishButton.click();
        return this;
    }

    @Step("Отредактировать свой отзыв на: {reviewText}")
    public ClubDetailsPage editOwnReview(String reviewText) {
        $$(".edit-review-btn").first().shouldBe(visible).click();
        reviewTextarea.setValue(reviewText);
        saveButton.click();
        return this;
    }

    @Step("Удалить свой отзыв")
    public ClubDetailsPage deleteOwnReview() {
        executeJavaScript("window.confirm = () => true;");
        $$(".delete-review-btn").first().shouldBe(visible).click();
        return this;
    }

    @Step("Проверить, что отзыв отображается: {reviewText}")
    public ClubDetailsPage shouldHaveReview(String reviewText) {
        reviewsList.shouldBe(visible).shouldHave(text(reviewText));
        return this;
    }

    @Step("Проверить, что отзыв отсутствует: {reviewText}")
    public ClubDetailsPage shouldNotHaveReview(String reviewText) {
        $$(".review-card").filterBy(text(reviewText)).shouldHave(size(0));
        return this;
    }

    @Step("Проверить, что кнопка написания отзыва недоступна")
    public ClubDetailsPage shouldNotAllowCreateReview() {
        addReviewButton.shouldNot(exist);
        return this;
    }
}
