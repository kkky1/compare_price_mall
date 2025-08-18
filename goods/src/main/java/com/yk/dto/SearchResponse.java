package com.yk.dto;

import com.yk.domain.Product;
import lombok.Data;
import java.util.List;

@Data
public class SearchResponse {
    
    private String status;
    private String platform;
    private String keyword;
    private Integer totalCount;
    private List<Product> products;
    private Double processingTime;
    private String message;

}