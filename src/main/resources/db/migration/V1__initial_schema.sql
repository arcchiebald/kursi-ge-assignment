CREATE TABLE funding_requests (
    request_id UUID PRIMARY KEY,
    available_settlement_balance NUMERIC(19, 2) NOT NULL,
    total_settlement_consumed NUMERIC(19, 2) NOT NULL,
    total_expected_fee NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE funding_request_candidates (
    id BIGSERIAL PRIMARY KEY,
    request_id UUID NOT NULL REFERENCES funding_requests(request_id) ON DELETE CASCADE,
    instruction_reference VARCHAR(255) NOT NULL,
    instruction_amount NUMERIC(19, 2) NOT NULL,
    expected_fee NUMERIC(19, 2) NOT NULL,
    candidate_order INTEGER NOT NULL
);

CREATE TABLE funding_request_selected_instructions (
    id BIGSERIAL PRIMARY KEY,
    request_id UUID NOT NULL REFERENCES funding_requests(request_id) ON DELETE CASCADE,
    instruction_reference VARCHAR(255) NOT NULL,
    instruction_amount NUMERIC(19, 2) NOT NULL,
    expected_fee NUMERIC(19, 2) NOT NULL,
    selection_order INTEGER NOT NULL
);

CREATE INDEX idx_funding_requests_created_at
    ON funding_requests (created_at DESC);

CREATE INDEX idx_funding_request_candidates_request_id
    ON funding_request_candidates (request_id);

CREATE INDEX idx_funding_request_selected_request_id
    ON funding_request_selected_instructions (request_id);