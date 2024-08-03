CREATE TABLE IF NOT EXISTS TB_EMPLOYEES_MANAGERS (
    EM_MANAGER_ID BIGSERIAL REFERENCES tb_employees(EM_ID),
    EM_EMPLOYEE_ID BIGSERIAL REFERENCES tb_employees(EM_ID),
    EM_CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    EM_UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT tb_employes_managers_pkey PRIMARY KEY (EM_MANAGER_ID, EM_EMPLOYEE_ID)
);

CREATE  FUNCTION update_updated_at_tb_employees_managers()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.EM_UPDATED_AT = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_updated_at_tb_employees_managers
    BEFORE UPDATE
    ON
        "clock-in-db".public.TB_PERSONAL_DATAS
    FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_tb_employees_managers();