package models.club;

import java.util.List;

public record ClubResponseModel(
        Integer id,
        String bookTitle,
        String bookAuthors,
        Integer publicationYear,
        String description,
        String telegramChatLink,
        Integer owner,
        List<Integer> members,
        List<BookReviewResponseModel> reviews,
        String created,
        String modified
) {}
