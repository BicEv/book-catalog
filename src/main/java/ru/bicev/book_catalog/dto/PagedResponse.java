package ru.bicev.book_catalog.dto;

import java.util.List;

public record PagedResponse<T>(
                List<T> content,
                int page,
                int size,
                long totalElements,
                int totalPages,
                boolean isFirst,
                boolean last) {

}
