CREATE TABLE IF NOT EXISTS TB_TIME_CLOCKS (
    TC_ID UUID PRIMARY KEY,
    TC_TIME_CLOCKED TIMESTAMP NOT NULL,
    TC_CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    TC_UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    TC_EXTERNAL_EMPLOYEE_ID BIGSERIAL NOT NULL
);

CREATE TABLE IF NOT EXISTS TB_PENDING_UPDATE_APPROVALS(
    PUA_ID UUID PRIMARY KEY,
    PUA_TIME_CLOCK_UPDATED TIMESTAMP NOT NULL,
    PUA_APPROVED BOOLEAN DEFAULT NULL,
    PUA_CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PUA_UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PUA_TIME_CLOCK_ID UUID REFERENCES TB_TIME_CLOCKS(TC_ID)
);
