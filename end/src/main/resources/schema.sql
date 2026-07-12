CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(255) NOT NULL,
    role INT NOT NULL,
    email VARCHAR(255) NULL,
    phone VARCHAR(255) NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project_info (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NULL,
    priority INT NOT NULL,
    status INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_project_info_owner_id FOREIGN KEY (owner_id) REFERENCES sys_user(id)
);

CREATE TABLE IF NOT EXISTS task_log (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    operator_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    progress_percent INT NOT NULL,
    content VARCHAR(255) NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_log_operator_id FOREIGN KEY (operator_id) REFERENCES sys_user(id),
    CONSTRAINT fk_task_log_task_id FOREIGN KEY (task_id) REFERENCES project_info(id)
);

CREATE TABLE IF NOT EXISTS task_info (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    creator_id BIGINT NOT NULL,
    assignee_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    parent_id BIGINT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255) NULL,
    priority INT NOT NULL,
    status INT NOT NULL,
    due_date DATE NOT NULL,
    ai_suggestion VARCHAR(255) NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_info_creator_id FOREIGN KEY (creator_id) REFERENCES sys_user(id),
    CONSTRAINT fk_task_info_assignee_id FOREIGN KEY (assignee_id) REFERENCES sys_user(id),
    CONSTRAINT fk_task_info_project_id FOREIGN KEY (project_id) REFERENCES project_info(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_info_parent_id FOREIGN KEY (parent_id) REFERENCES task_info(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS task_summary (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    creator_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    summary_type INT NOT NULL,
    content VARCHAR(255) NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_summary_creator_id FOREIGN KEY (creator_id) REFERENCES sys_user(id),
    CONSTRAINT fk_task_summary_project_id FOREIGN KEY (project_id) REFERENCES project_info(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_summary_task_id FOREIGN KEY (task_id) REFERENCES task_info(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS operate_log (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    operate_emp_id BIGINT NULL,
    operate_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    class_name VARCHAR(255) NOT NULL,
    method_name VARCHAR(255) NOT NULL,
    method_params TEXT NULL,
    return_value TEXT NULL,
    cost_time BIGINT NOT NULL,
    operate_emp_name VARCHAR(255) NULL,
    INDEX idx_operate_log_time (operate_time),
    INDEX idx_operate_log_emp_id (operate_emp_id),
    CONSTRAINT fk_operate_log_emp_id FOREIGN KEY (operate_emp_id) REFERENCES sys_user(id) ON DELETE SET NULL
);

SET @task_info_project_fk = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'task_info'
      AND COLUMN_NAME = 'project_id'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);
SET @task_info_project_drop_sql = IF(@task_info_project_fk IS NOT NULL, CONCAT('ALTER TABLE task_info DROP FOREIGN KEY ', @task_info_project_fk), 'SELECT 1');
PREPARE task_info_project_drop_stmt FROM @task_info_project_drop_sql;
EXECUTE task_info_project_drop_stmt;
DEALLOCATE PREPARE task_info_project_drop_stmt;
ALTER TABLE task_info
    ADD CONSTRAINT fk_task_info_project_id FOREIGN KEY (project_id) REFERENCES project_info(id) ON DELETE CASCADE;

SET @task_info_parent_fk = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'task_info'
      AND COLUMN_NAME = 'parent_id'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);
SET @task_info_parent_drop_sql = IF(@task_info_parent_fk IS NOT NULL, CONCAT('ALTER TABLE task_info DROP FOREIGN KEY ', @task_info_parent_fk), 'SELECT 1');
PREPARE task_info_parent_drop_stmt FROM @task_info_parent_drop_sql;
EXECUTE task_info_parent_drop_stmt;
DEALLOCATE PREPARE task_info_parent_drop_stmt;
ALTER TABLE task_info
    ADD CONSTRAINT fk_task_info_parent_id FOREIGN KEY (parent_id) REFERENCES task_info(id) ON DELETE CASCADE;

SET @task_log_fk = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'task_log'
      AND COLUMN_NAME = 'task_id'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);
SET @task_log_drop_sql = IF(@task_log_fk IS NOT NULL, CONCAT('ALTER TABLE task_log DROP FOREIGN KEY ', @task_log_fk), 'SELECT 1');
PREPARE task_log_drop_stmt FROM @task_log_drop_sql;
EXECUTE task_log_drop_stmt;
DEALLOCATE PREPARE task_log_drop_stmt;
ALTER TABLE task_log
    ADD CONSTRAINT fk_task_log_task_id FOREIGN KEY (task_id) REFERENCES task_info(id) ON DELETE CASCADE;

SET @task_summary_project_fk = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'task_summary'
      AND COLUMN_NAME = 'project_id'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);
SET @task_summary_project_drop_sql = IF(@task_summary_project_fk IS NOT NULL, CONCAT('ALTER TABLE task_summary DROP FOREIGN KEY ', @task_summary_project_fk), 'SELECT 1');
PREPARE task_summary_project_drop_stmt FROM @task_summary_project_drop_sql;
EXECUTE task_summary_project_drop_stmt;
DEALLOCATE PREPARE task_summary_project_drop_stmt;
ALTER TABLE task_summary
    ADD CONSTRAINT fk_task_summary_project_id FOREIGN KEY (project_id) REFERENCES project_info(id) ON DELETE CASCADE;

SET @task_summary_fk = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'task_summary'
      AND COLUMN_NAME = 'task_id'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);
SET @task_summary_drop_sql = IF(@task_summary_fk IS NOT NULL, CONCAT('ALTER TABLE task_summary DROP FOREIGN KEY ', @task_summary_fk), 'SELECT 1');
PREPARE task_summary_drop_stmt FROM @task_summary_drop_sql;
EXECUTE task_summary_drop_stmt;
DEALLOCATE PREPARE task_summary_drop_stmt;
ALTER TABLE task_summary MODIFY COLUMN task_id BIGINT NOT NULL;
ALTER TABLE task_summary
    ADD CONSTRAINT fk_task_summary_task_id FOREIGN KEY (task_id) REFERENCES task_info(id) ON DELETE CASCADE;

SET @operate_log_emp_fk = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'operate_log'
      AND COLUMN_NAME = 'operate_emp_id'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);
SET @operate_log_emp_drop_sql = IF(@operate_log_emp_fk IS NOT NULL, CONCAT('ALTER TABLE operate_log DROP FOREIGN KEY ', @operate_log_emp_fk), 'SELECT 1');
PREPARE operate_log_emp_drop_stmt FROM @operate_log_emp_drop_sql;
EXECUTE operate_log_emp_drop_stmt;
DEALLOCATE PREPARE operate_log_emp_drop_stmt;
ALTER TABLE operate_log
    ADD CONSTRAINT fk_operate_log_emp_id FOREIGN KEY (operate_emp_id) REFERENCES sys_user(id) ON DELETE SET NULL;

