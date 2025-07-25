package com.yk.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Min;

@Data
public class SearchRequest {

    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;

    private String platform = "all"; // 默认搜索所有平台

    @Min(value = 1, message = "页数最少为1")
    private Integer maxPages = 1;
}