INSERT IGNORE INTO sys_user (id, username, password, real_name, role, email, phone)
VALUES
    (1, 'admin', MD5('123456'), 'System Admin', 0, 'admin@demo.local', '13800138000'),
    (2, 'zhangchen', MD5('123456'), 'Zhang Chen', 1, 'zhangchen@demo.local', '13800138001'),
    (3, 'lixue', MD5('123456'), 'Li Xue', 1, 'lixue@demo.local', '13800138002'),
    (4, 'wanghao', MD5('123456'), 'Wang Hao', 1, 'wanghao@demo.local', '13800138003'),
    (5, 'zhaomin', MD5('123456'), 'Zhao Min', 2, 'zhaomin@demo.local', '13800138004'),
    (6, 'qiankun', MD5('123456'), 'Qian Kun', 2, 'qiankun@demo.local', '13800138005'),
    (7, 'sunyue', MD5('123456'), 'Sun Yue', 2, 'sunyue@demo.local', '13800138006'),
    (8, 'zhouning', MD5('123456'), 'Zhou Ning', 2, 'zhouning@demo.local', '13800138007'),
    (9, 'wudi', MD5('123456'), 'Wu Di', 2, 'wudi@demo.local', '13800138008'),
    (10, 'zhengjia', MD5('123456'), 'Zheng Jia', 2, 'zhengjia@demo.local', '13800138009');

INSERT IGNORE INTO project_info (id, owner_id, name, description, priority, status, start_date, end_date)
VALUES
    (1, 2, 'Smart Campus Platform', 'Integrate devices, monitoring dashboards, and alert workflows.', 2, 1, '2026-07-01', '2026-08-15'),
    (2, 3, 'Customer Service Mini App', 'Improve repair submission, work orders, and satisfaction callbacks.', 1, 0, '2026-07-05', '2026-08-20'),
    (3, 4, 'Operations Data Hub', 'Unify reporting definitions and build management dashboards.', 2, 2, '2026-06-20', '2026-07-30');

INSERT IGNORE INTO task_info (id, creator_id, assignee_id, project_id, parent_id, title, description, priority, status, due_date, ai_suggestion)
VALUES
    (1, 2, 5, 1, NULL, 'Review device onboarding inventory', 'Collect device models, protocols, and responsible contacts for the first rollout.', 2, 1, '2026-07-12', 'Classify devices first, then confirm onboarding protocol details.'),
    (2, 2, 6, 1, 1, 'Design monitoring homepage prototype', 'Draft layout, KPI cards, and alert list interactions for the homepage.', 1, 0, '2026-07-16', 'Prioritize real-time alerts and online device counts.'),
    (3, 3, 7, 2, NULL, 'Map repair submission flow', 'Define the entry points, required fields, and state transitions for customer repair requests.', 1, 1, '2026-07-14', 'Draw the main happy path first, then cover exceptions.'),
    (4, 3, 8, 2, 3, 'Implement work order dispatch API', 'Build automatic dispatch and manual reassignment endpoints for work orders.', 2, 0, '2026-07-18', 'Extract dispatch rules into configurable strategies.'),
    (5, 4, 9, 3, NULL, 'Standardize operations KPIs', 'Define revenue, cost, conversion, and other core metric definitions.', 2, 2, '2026-07-08', 'Lock definitions before syncing the dashboard fields.'),
    (6, 4, 10, 3, 5, 'Create weekly report dashboard template', 'Build management-facing weekly charts with reusable filters.', 0, 1, '2026-07-22', 'Keep project, department, and time dimensions reusable.');

INSERT IGNORE INTO task_log (id, operator_id, task_id, progress_percent, content)
VALUES
    (1, 5, 1, 40, 'Finished the first batch inventory summary and now filling protocol fields.'),
    (2, 7, 3, 65, 'Confirmed the main repair flow and waiting for review on exception handling.'),
    (3, 9, 5, 100, 'Finalized KPI definitions and synced them with the visualization team.');

INSERT IGNORE INTO task_summary (id, creator_id, project_id, task_id, summary_type, content)
VALUES
    (1, 2, 1, 1, 0, 'The smart campus project completed the device inventory review and moved the homepage prototype into design.'),
    (2, 3, 2, 3, 0, 'The customer service mini app confirmed the repair flow and is now building the dispatch API.'),
    (3, 4, 3, 5, 1, 'The operations data hub unified KPI definitions and is ready for dashboard delivery.');

UPDATE task_log
SET task_id = CASE id
    WHEN 2 THEN 3
    WHEN 3 THEN 5
    ELSE task_id
END
WHERE id IN (2, 3);

UPDATE task_summary
SET task_id = CASE id
    WHEN 2 THEN 3
    WHEN 3 THEN 5
    ELSE task_id
END
WHERE id IN (2, 3);
