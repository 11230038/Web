INSERT IGNORE INTO sys_user (id, username, password, real_name, role, email, phone)
VALUES
    (1, 'alice', MD5('123456'), 'Alice Zhang', 0, 'alice@example.com', '13800138001'),
    (2, 'bob', MD5('123456'), 'Bob Li', 1, 'bob@example.com', '13800138002'),
    (3, 'carol', MD5('123456'), 'Carol Wang', 2, 'carol@example.com', '13800138003');

INSERT IGNORE INTO project_info (id, owner_id, name, description, priority, status, start_date, end_date)
VALUES
    (1, 1, 'Project Alpha', 'Alpha delivery project', 2, 1, '2026-07-01', '2026-07-31'),
    (2, 2, 'Project Beta', 'Beta optimization project', 1, 0, '2026-07-05', '2026-08-05'),
    (3, 3, 'Project Gamma', 'Gamma support project', 0, 2, '2026-07-10', '2026-08-10');

INSERT IGNORE INTO task_log (id, operator_id, task_id, progress_percent, content)
VALUES
    (1, 1, 1, 30, 'Initialized project alpha log'),
    (2, 2, 2, 60, 'Updated beta progress log'),
    (3, 3, 3, 90, 'Completed gamma progress log');

INSERT IGNORE INTO task_info (id, creator_id, assignee_id, project_id, parent_id, title, description, priority, status, due_date, ai_suggestion)
VALUES
    (1, 1, 2, 1, NULL, 'Design API', 'Design task service API', 2, 1, '2026-07-15', 'Split endpoints by domain'),
    (2, 2, 3, 2, 1, 'Implement Mapper', 'Implement MyBatis mapper layer', 1, 0, '2026-07-20', 'Add unit tests for mapper'),
    (3, 3, 1, 3, NULL, 'Prepare Summary', 'Prepare final task summary', 0, 2, '2026-07-25', 'Generate weekly report draft');

INSERT IGNORE INTO task_summary (id, creator_id, project_id, task_id, summary_type, content)
VALUES
    (1, 1, 1, 1, 0, 'Alpha daily summary created'),
    (2, 2, 2, 2, 0, 'Beta weekly summary updated'),
    (3, 3, 3, 3, 1, 'Gamma final summary submitted');
