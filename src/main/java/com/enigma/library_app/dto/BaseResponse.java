package com.enigma.library_app.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse<T> {
    private T data;
    private String errors;
    private PagingResponse paging;

    public static <T> BaseResponse<T> success(T data) {
        return BaseResponse.<T>builder()
                .data(data)
                .errors(null)
                .paging(null)
                .build();
    }

    public static <T> BaseResponse<T> error(String errorMessage) {
        return BaseResponse.<T>builder()
                .data(null)
                .errors(errorMessage)
                .paging(null)
                .build();
    }

    public static <T> BaseResponse<T> successWithPaging(T data, PagingResponse paging) {
        return BaseResponse.<T>builder()
                .data(data)
                .errors(null)
                .paging(paging)
                .build();
    }
}
