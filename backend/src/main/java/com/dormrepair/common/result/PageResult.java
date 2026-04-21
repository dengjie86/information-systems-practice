package com.dormrepair.common.result;

import java.util.List;

/**
 * Standard page response payload for list endpoints.
 *
 * @param records current page data
 * @param total total records count
 * @param pageNum current page number
 * @param pageSize current page size
 * @param <T> item type
 */
public record PageResult<T>(List<T> records, long total, long pageNum, long pageSize) {
}
