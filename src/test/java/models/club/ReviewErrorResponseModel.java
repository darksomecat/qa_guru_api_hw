package models.club;

import java.util.List;

public record ReviewErrorResponseModel(
        List<String> club,
        List<String> review,
        List<String> assessment,
        List<String> readPages
) {}
