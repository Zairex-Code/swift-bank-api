package org.zairex_code.domain.model;


import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private String id;
    private String accountNumber;
    private String holderName;
    private BigDecimal balance;
}
