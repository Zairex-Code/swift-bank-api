package org.zairex_code.domain.model;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private String id;
    private String sourceAccountId;
    private String targetAccountId;
    private BigDecimal amount;
    private TransactionStatus status;
    private String rejectionReason;
    private LocalDateTime timestamp;
}
