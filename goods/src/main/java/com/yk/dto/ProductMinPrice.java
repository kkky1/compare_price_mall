package com.yk.dto;

import com.yk.domain.Product;
import lombok.Data;

import java.util.List;

@Data
public class ProductMinPrice {


    private String keyword;
    private String message;
    private String platform;
    private PriceStatistics price_statistics;
    private double processing_time;
    private List<Product> products;
    private String status;
    private int total_count;


    // 内部类 PriceStatistics
    public static class PriceStatistics {
        private double avg_price;
        private String currency;
        private double max_price;
        private double min_price;

        public double getAvg_price() { return avg_price; }
        public void setAvg_price(double avg_price) { this.avg_price = avg_price; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public double getMax_price() { return max_price; }
        public void setMax_price(double max_price) { this.max_price = max_price; }

        public double getMin_price() { return min_price; }
        public void setMin_price(double min_price) { this.min_price = min_price; }
    }


}
