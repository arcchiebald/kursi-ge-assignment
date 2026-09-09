CREATE TABLE funding_requests (
    request_id UUID PRIMARY KEY,
    available_settlement_balance BIGINT NOT NULL,
    total_settlement_consumed BIGINT NOT NULL,
    total_expected_fee BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT chk_funding_requests_available_balance_non_negative
        CHECK (available_settlement_balance >= 0),
    CONSTRAINT chk_funding_requests_total_consumed_non_negative
        CHECK (total_settlement_consumed >= 0),
    CONSTRAINT chk_funding_requests_total_fee_non_negative
        CHECK (total_expected_fee >= 0),
    CONSTRAINT chk_funding_requests_consumed_within_balance
        CHECK (total_settlement_consumed <= available_settlement_balance)
);

CREATE TABLE funding_instructions (
    id BIGSERIAL PRIMARY KEY,
    request_id UUID NOT NULL REFERENCES funding_requests(request_id) ON DELETE CASCADE,
    instruction_reference VARCHAR(255) NOT NULL,
    instruction_amount BIGINT NOT NULL,
    expected_fee BIGINT NOT NULL,
    is_selected BOOLEAN NOT NULL DEFAULT FALSE,
    instruction_order INTEGER NOT NULL,

    CONSTRAINT chk_funding_instructions_amount_positive
        CHECK (instruction_amount > 0),
    CONSTRAINT chk_funding_instructions_fee_non_negative
        CHECK (expected_fee >= 0),
    CONSTRAINT chk_funding_instructions_order_non_negative
        CHECK (instruction_order >= 0),
    CONSTRAINT uq_funding_instructions_request_order
        UNIQUE (request_id, instruction_order)
);

CREATE INDEX idx_funding_requests_created_at
    ON funding_requests (created_at DESC);

CREATE INDEX idx_funding_instructions_request_selected_order
    ON funding_instructions (request_id, is_selected, instruction_order);