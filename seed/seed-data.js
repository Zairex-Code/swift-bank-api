// MongoDB seed script for swift-bank-api.
// Usage (local docker):
//   docker compose exec -T mongo mongosh --file /seed/seed-data.js
// Usage (any MongoDB / Atlas):
//   mongosh "<connection-string>" --file seed/seed-data.js

const database = db.getSiblingDB("swift_bank");

database.accounts.deleteMany({});
database.transactions.deleteMany({});

const aliceId = "11111111-1111-1111-1111-111111111111";
const bobId = "22222222-2222-2222-2222-222222222222";
const now = new Date();

database.accounts.insertMany([
  {
    _id: aliceId,
    accountNumber: "ACC-2001",
    holderName: "Alice Johnson",
    balance: Decimal128.fromString("1500.00"),
  },
  {
    _id: bobId,
    accountNumber: "ACC-2002",
    holderName: "Bob Smith",
    balance: Decimal128.fromString("800.50"),
  },
]);

database.transactions.insertMany([
  {
    _id: "aaaa1111-0000-0000-0000-000000000001",
    sourceAccountId: null,
    targetAccountId: aliceId,
    amount: Decimal128.fromString("1500.00"),
    status: "COMPLETED",
    rejectionReason: null,
    timestamp: now,
  },
  {
    _id: "aaaa1111-0000-0000-0000-000000000002",
    sourceAccountId: aliceId,
    targetAccountId: bobId,
    amount: Decimal128.fromString("200.00"),
    status: "COMPLETED",
    rejectionReason: null,
    timestamp: now,
  },
  {
    _id: "aaaa1111-0000-0000-0000-000000000003",
    sourceAccountId: bobId,
    targetAccountId: aliceId,
    amount: Decimal128.fromString("99999.00"),
    status: "REJECTED",
    rejectionReason: "Insufficient balance in source account: " + bobId,
    timestamp: now,
  },
]);

print(
  "Seed complete: " +
    database.accounts.countDocuments() +
    " accounts, " +
    database.transactions.countDocuments() +
    " transactions"
);
