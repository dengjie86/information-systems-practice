package com.dormrepair.common.result;

import java.util.List;

public record PageResult<T>(List<T> records, long total, long pageNum, long pageSize) {
}
