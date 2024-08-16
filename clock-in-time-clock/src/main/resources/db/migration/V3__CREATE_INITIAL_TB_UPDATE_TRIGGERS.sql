
CREATE  FUNCTION update_updated_at_tb_time_clocks()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.TC_UPDATED_AT = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_time_clocks_task_updated_on
    BEFORE UPDATE
    ON
        "clock-in-db".public2.TB_TIME_CLOCKS
    FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_tb_time_clocks();

--

CREATE  FUNCTION update_updated_at_tb_pending_update_approvals()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.PUA_UPDATED_AT = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_pending_update_approvals_task_updated_on
    BEFORE UPDATE
    ON
        "clock-in-db".public2.TB_PENDING_UPDATE_APPROVALS
    FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_tb_pending_update_approvals();