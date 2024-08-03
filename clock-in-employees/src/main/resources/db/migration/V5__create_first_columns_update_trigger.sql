CREATE  FUNCTION update_updated_at_tb_employees()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.EM_UPDATED_AT = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_employee_task_updated_on
    BEFORE UPDATE
    ON
        "clock-in-db".public.TB_EMPLOYEES
    FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_tb_employees();

--

CREATE  FUNCTION update_updated_at_tb_personal_data()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.PD_UPDATED_AT = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_personal_data_task_updated_on
    BEFORE UPDATE
    ON
        "clock-in-db".public.TB_PERSONAL_DATAS
    FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_tb_personal_data();

--

CREATE  FUNCTION update_updated_at_tb_positions()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.PS_UPDATED_AT = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_positions_task_updated_on
    BEFORE UPDATE
    ON
        "clock-in-db".public.TB_POSITIONS
    FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_tb_positions();

--

CREATE  FUNCTION update_updated_at_tb_system_roles()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.SR_UPDATED_AT = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_system_role_task_updated_on
    BEFORE UPDATE
    ON
        "clock-in-db".public.TB_SYSTEM_ROLES
    FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_tb_system_roles();

--

CREATE  FUNCTION update_updated_at_tb_employee_position()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.EP_UPDATED_AT = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_employee_position_task_updated_on
    BEFORE UPDATE
    ON
        "clock-in-db".public.TB_EMPLOYEE_POSITIONS
    FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_tb_employee_position();