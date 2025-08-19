package com.yk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceTendency implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Double price;
    private Date time;


}
