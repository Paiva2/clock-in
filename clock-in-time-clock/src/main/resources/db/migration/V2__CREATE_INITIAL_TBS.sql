CREATE TABLE IF NOT EXISTS TB_EMPLOYEE_IDS (
   EI_ID UUID PRIMARY KEY,
   EI_EXTERNAL_ID BIGSERIAL NOT NULL UNIQUE,
   EI_CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   EI_UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS TB_TIME_CLOCKS (
    TC_ID UUID PRIMARY KEY,
    TC_TIME_CLOCKED TIMESTAMP NOT NULL,
    TC_CREATED_AT_ID TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    TC_UPDATED_AT_ID TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    TC_EMPLOYEE_ID UUID REFERENCES TB_EMPLOYEE_IDS(EI_ID) NOT NULL
);

CREATE TABLE IF NOT EXISTS TB_PENDING_UPDATE_APPROVALS(
    PUA_ID UUID PRIMARY KEY,
    PUA_TIME_CLOCK_UPDATED TIMESTAMP NOT NULL,
    PUA_APPROVED BOOLEAN NOT NULL DEFAULT FALSE,
    PUA_CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PUA_UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PUA_TIME_CLOCK UUID REFERENCES TB_TIME_CLOCKS(TC_ID)
);
