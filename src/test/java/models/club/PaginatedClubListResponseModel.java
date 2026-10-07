package models.club;

import java.util.List;

public record PaginatedClubListResponseModel(
        Integer count,
        String next,
        String previous,
        List<ClubResponseModel> results
) {}
