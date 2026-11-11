package models.club;

import java.util.List;

public record ClubErrorResponseModel(
        List<String> bookTitle,
        List<String> bookAuthors,
        List<String> publicationYear,
        List<String> description,
        List<String> telegramChatLink
) {}
