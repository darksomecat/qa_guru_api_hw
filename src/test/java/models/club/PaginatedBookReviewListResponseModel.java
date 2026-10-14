package models.club;

import java.util.List;

public record PaginatedBookReviewListResponseModel(
        Integer count,
        String next,
        String previous,
        List<BookReviewResponseModel> results
) {}
