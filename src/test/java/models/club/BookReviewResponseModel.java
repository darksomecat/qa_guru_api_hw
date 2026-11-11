package models.club;

public record BookReviewResponseModel(
        Integer id,
        Integer club,
        ReviewAuthorResponseModel user,
        String review,
        Integer assessment,
        Integer readPages,
        String created,
        String modified
) {}
