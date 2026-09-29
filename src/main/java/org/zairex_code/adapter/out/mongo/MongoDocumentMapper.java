package org.zairex_code.adapter.out.mongo;

import org.bson.Document;
import org.bson.types.Decimal128;
import org.zairex_code.domain.model.Account;
import org.zairex_code.domain.model.Transaction;
import org.zairex_code.domain.model.TransactionStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

public final class MongoDocumentMapper {

    private MongoDocumentMapper() {
    }

    public static Document toDocument(Account account) {
        return new Document("_id", account.getId())
                .append("accountNumber", account.getAccountNumber())
                .append("holderName", account.getHolderName())
                .append("balance", new Decimal128(scale(account.getBalance())));
    }

    public static Account toAccount(Document document) {
        return Account.builder()
                .id(document.getString("_id"))
                .accountNumber(document.getString("accountNumber"))
                .holderName(document.getString("holderName"))
                .balance(toBigDecimal(document.get("balance")))
                .build();
    }

    public static Document toDocument(Transaction transaction) {
        return new Document("_id", transaction.getId())
                .append("sourceAccountId", transaction.getSourceAccountId())
                .append("targetAccountId", transaction.getTargetAccountId())
                .append("amount", new Decimal128(scale(transaction.getAmount())))
                .append("status", transaction.getStatus() != null ? transaction.getStatus().name() : null)
                .append("rejectionReason", transaction.getRejectionReason())
                .append("timestamp", toDate(transaction.getTimestamp()));
    }

    public static Transaction toTransaction(Document document) {
        String status = document.getString("status");
        return Transaction.builder()
                .id(document.getString("_id"))
                .sourceAccountId(document.getString("sourceAccountId"))
                .targetAccountId(document.getString("targetAccountId"))
                .amount(toBigDecimal(document.get("amount")))
                .status(status != null ? TransactionStatus.valueOf(status) : null)
                .rejectionReason(document.getString("rejectionReason"))
                .timestamp(toLocalDateTime(document.get("timestamp")))
                .build();
    }

    private static BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        }
        return value.setScale(2, RoundingMode.HALF_EVEN);
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        }
        if (value instanceof Decimal128 decimal128) {
            return decimal128.bigDecimalValue().setScale(2, RoundingMode.HALF_EVEN);
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal.setScale(2, RoundingMode.HALF_EVEN);
        }
        return new BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_EVEN);
    }

    private static Date toDate(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return Date.from(value.toInstant(ZoneOffset.UTC));
    }

    private static LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date date) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneOffset.UTC);
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        return null;
    }
}
