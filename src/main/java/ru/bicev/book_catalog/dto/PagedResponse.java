package ru.bicev.book_catalog.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Paged response with content, page info and metadata")
public record PagedResponse<T>(

        @Schema(description = "List of items",example = "[items]") List<T> content,
        @Schema(description = "Current page number", example = "0") int page,
        @Schema(description = "Current page size", example = "10") int size,
        @Schema(description = "Total number of elements", example = "100") long totalElements,
        @Schema(description = "Total number of pages", example = "3") int totalPages,
        @Schema(description = "Is first page", example = "false") boolean isFirst,
        @Schema(description = "Is last page", example = "true") boolean last) {

}
