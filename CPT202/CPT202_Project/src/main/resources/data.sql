INSERT IGNORE INTO users (user_id, username, password_hash, email, full_name, role, account_status, created_at, updated_at) VALUES
    (1001, 'admin1',   '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'admin1@example.com',   'System Admin',   'ADMIN',   'ACTIVE', '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
    (1002, 'teacher1', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'teacher1@example.com', 'Alice Teacher',  'TEACHER', 'ACTIVE', '2026-04-01 09:05:00', '2026-04-01 09:05:00'),
    (1003, 'student1', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'student1@example.com', 'Bob Student',    'STUDENT', 'ACTIVE', '2026-04-01 09:10:00', '2026-04-01 09:10:00'),
    (1004, 'teacher2', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'teacher2@example.com', 'Carol Teacher',  'TEACHER', 'ACTIVE', '2026-04-01 09:15:00', '2026-04-01 09:15:00'),
    (1005, 'teacher3', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'teacher3@example.com', 'David Teacher',  'TEACHER', 'ACTIVE', '2026-04-01 09:20:00', '2026-04-01 09:20:00'),
    (1006, 'student2', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'student2@example.com', 'Emma Student',   'STUDENT', 'ACTIVE', '2026-04-01 09:25:00', '2026-04-01 09:25:00'),
    (1007, 'student3', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'student3@example.com', 'Frank Student',  'STUDENT', 'ACTIVE', '2026-04-01 09:30:00', '2026-04-01 09:30:00'),
    (1008, 'student4', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'student4@example.com', 'Grace Student',  'STUDENT', 'ACTIVE', '2026-04-01 09:35:00', '2026-04-01 09:35:00'),
    (1009, 'student5', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'student5@example.com', 'Henry Student',  'STUDENT', 'ACTIVE', '2026-04-01 09:40:00', '2026-04-01 09:40:00');

INSERT IGNORE INTO teacher_profile (teacher_id, staff_no, department, title, research_area, office, updated_at) VALUES
    (1002, 'T2026001', 'Computer Science', 'Lecturer',  'Distributed Systems',  'Room 3A-201', '2026-04-01 09:05:00'),
    (1004, 'T2026002', 'Computer Science', 'Associate Professor', 'Machine Learning', 'Room 3A-305', '2026-04-01 09:15:00'),
    (1005, 'T2026003', 'Information Systems', 'Lecturer', 'Web Engineering', 'Room 2B-108', '2026-04-01 09:20:00');

INSERT IGNORE INTO student_profile (student_id, student_no, programme, enrollment_date, phone, interests, updated_at) VALUES
    (1003, 'S2026001', 'Software Engineering', '2023-09-01', '13800000000', 'Backend Development, Data Engineering', '2026-04-01 09:10:00'),
    (1006, 'S2026002', 'Software Engineering', '2022-09-01', '13800000001', 'Distributed Systems, Java', '2026-04-01 09:25:00'),
    (1007, 'S2026003', 'Computer Science',     '2021-09-01', '13800000002', 'Machine Learning, Python', '2026-04-01 09:30:00'),
    (1008, 'S2026004', 'Information Systems',  '2023-09-01', '13800000003', 'Web Development, UI Design', '2026-04-01 09:35:00'),
    (1009, 'S2026005', 'Data Science',         '2022-09-01', '13800000004', 'Data Analysis, Recommender Systems', '2026-04-01 09:40:00');

INSERT IGNORE INTO category (category_id, category_name, description, created_at, updated_at) VALUES
    (2001, 'AI',      'Artificial intelligence related projects',          '2026-04-01 09:50:00', '2026-04-01 09:50:00'),
    (2002, 'Web',     'Web application and platform projects',             '2026-04-01 09:51:00', '2026-04-01 09:51:00'),
    (2003, 'Data',    'Data engineering and analytics projects',           '2026-04-01 09:52:00', '2026-04-01 09:52:00'),
    (2004, 'Systems', 'Distributed systems and software architecture work', '2026-04-01 09:53:00', '2026-04-01 09:53:00');

INSERT IGNORE INTO tag (tag_id, tag_name, description, created_at, updated_at) VALUES
    (3001, 'Java',        'Java language projects',               '2026-04-01 10:00:00', '2026-04-01 10:00:00'),
    (3002, 'Spring Boot', 'Spring Boot based backend work',       '2026-04-01 10:01:00', '2026-04-01 10:01:00'),
    (3003, 'SQL',         'Database design and query work',       '2026-04-01 10:02:00', '2026-04-01 10:02:00'),
    (3004, 'Python',      'Python and ML pipeline work',          '2026-04-01 10:03:00', '2026-04-01 10:03:00'),
    (3005, 'Frontend',    'Frontend development and interaction', '2026-04-01 10:04:00', '2026-04-01 10:04:00');

INSERT IGNORE INTO project (project_id, teacher_id, category_id, title, description, required_skills, topic_area, max_students, current_agreed_count, project_status, publish_date, close_date, created_at, updated_at) VALUES
    (4001, 1002, 2001, 'Intelligent Project Matching',        'Build a matching engine for project applications.',                         'Java, SQL, Spring Boot',      'Recommendation Systems',    2, 0, 'AVAILABLE', '2026-04-02 10:00:00', NULL,                   '2026-04-02 10:00:00', '2026-04-02 10:00:00'),
    (4002, 1002, 2002, 'Research Project Portal',             'Develop a project management portal for teachers and students.',            'Java, REST API, HTML',        'Web Platform',              3, 0, 'AVAILABLE', '2026-04-03 11:00:00', NULL,                   '2026-04-03 11:00:00', '2026-04-03 11:00:00'),
    (4003, 1002, 2004, 'Microservice Observability Dashboard','Create a dashboard for tracing and service health monitoring.',              'Java, Spring Boot, SQL',      'Distributed Systems',       2, 0, 'AVAILABLE', '2026-04-04 09:00:00', NULL,                   '2026-04-04 09:00:00', '2026-04-04 09:00:00'),
    (4004, 1002, 2003, 'Academic Data Warehouse',             'Design ETL flows and analytics views for project records.',                 'SQL, Java',                   'Data Engineering',          2, 0, 'AVAILABLE', '2026-04-05 09:00:00', NULL,                   '2026-04-05 09:00:00', '2026-04-05 09:00:00'),
    (4005, 1002, 2002, 'Supervisor Meeting Scheduler',        'Build a scheduling system for weekly student supervision meetings.',        'Java, Spring Boot, Frontend', 'Workflow Systems',          2, 0, 'AVAILABLE', '2026-04-06 09:00:00', NULL,                   '2026-04-06 09:00:00', '2026-04-06 09:00:00'),
    (4006, 1004, 2001, 'LLM Feedback Analyzer',               'Analyze student feedback using NLP and large language models.',             'Python, SQL',                 'Natural Language Processing', 2, 0, 'AVAILABLE', '2026-04-07 10:00:00', NULL,                '2026-04-07 10:00:00', '2026-04-07 10:00:00'),
    (4007, 1004, 2001, 'Course Recommendation Assistant',     'Recommend elective courses according to student profiles and interests.',   'Python, SQL',                 'ML Recommendation',         2, 1, 'AGREED',    '2026-04-08 10:00:00', NULL,                   '2026-04-08 10:00:00', '2026-04-18 16:00:00'),
    (4008, 1004, 2003, 'Lab Usage Forecasting',               'Forecast computing lab occupancy and usage peaks.',                         'Python, SQL',                 'Time Series Forecasting',   2, 0, 'AVAILABLE', '2026-04-09 10:00:00', NULL,                   '2026-04-09 10:00:00', '2026-04-09 10:00:00'),
    (4009, 1004, 2004, 'Edge Deployment Monitor',             'Monitor edge deployments and failure signals across small devices.',        'Java, Systems',               'Edge Systems',              1, 0, 'CLOSED',    '2026-04-10 10:00:00', '2026-04-20 18:00:00',  '2026-04-10 10:00:00', '2026-04-20 18:00:00'),
    (4010, 1004, 2001, 'Vision-based Attendance Support',     'Use simple vision pipelines to support attendance verification.',           'Python, Frontend',            'Computer Vision',           2, 0, 'AVAILABLE', '2026-04-11 10:00:00', NULL,                   '2026-04-11 10:00:00', '2026-04-11 10:00:00'),
    (4011, 1005, 2002, 'Project Showcase Website',            'Create a public showcase website for research projects.',                   'Frontend, Java',              'Web Experience',            3, 0, 'AVAILABLE', '2026-04-12 14:00:00', NULL,                   '2026-04-12 14:00:00', '2026-04-12 14:00:00'),
    (4012, 1005, 2002, 'Interactive Topic Explorer',          'Build an explorer for browsing topics, tags and project history.',          'Frontend, Spring Boot',       'Information Architecture',  2, 0, 'AVAILABLE', '2026-04-13 14:00:00', NULL,                   '2026-04-13 14:00:00', '2026-04-13 14:00:00'),
    (4013, 1005, 2003, 'Analytics Export Service',            'Export project analytics into CSV and dashboard snapshots.',                'Java, SQL, Spring Boot',      'Analytics Tooling',         2, 0, 'AVAILABLE', '2026-04-14 14:00:00', NULL,                   '2026-04-14 14:00:00', '2026-04-14 14:00:00'),
    (4014, 1005, 2004, 'API Gateway Policy Demo',             'Demonstrate routing, throttling and access policies in a gateway layer.',   'Java, Spring Boot',           'Platform Engineering',      2, 0, 'ARCHIVED',  '2026-04-15 14:00:00', '2026-04-22 10:00:00',  '2026-04-15 14:00:00', '2026-04-22 10:00:00'),
    (4015, 1005, 2002, 'Student Portfolio Builder',           'Develop a portfolio builder for student project outcomes.',                 'Frontend, Java, SQL',         'Student Services',          3, 0, 'AVAILABLE', '2026-04-16 14:00:00', NULL,                   '2026-04-16 14:00:00', '2026-04-16 14:00:00');

INSERT IGNORE INTO project_tag (project_id, tag_id) VALUES
    (4001, 3001), (4001, 3002), (4001, 3003),
    (4002, 3001), (4002, 3005),
    (4003, 3001), (4003, 3002),
    (4004, 3001), (4004, 3003),
    (4005, 3001), (4005, 3002), (4005, 3005),
    (4006, 3004), (4006, 3003),
    (4007, 3004), (4007, 3003),
    (4008, 3004), (4008, 3003),
    (4009, 3001),
    (4010, 3004), (4010, 3005),
    (4011, 3005), (4011, 3001),
    (4012, 3005), (4012, 3002),
    (4013, 3001), (4013, 3003),
    (4014, 3001), (4014, 3002),
    (4015, 3001), (4015, 3003), (4015, 3005);

INSERT IGNORE INTO project_request (request_id, project_id, student_id, reviewed_by, preference_rank, notes, request_status, decision_comment, submitted_at, reviewed_at, withdrawn_at, updated_at) VALUES
    (5001, 4001, 1003, NULL, 1, 'I want to work on recommendation algorithms.',             'PENDING',   NULL,                                '2026-04-05 14:00:00', NULL,                   NULL,                   '2026-04-05 14:00:00'),
    (5002, 4002, 1006, 1002, 1, 'Interested in portal backend APIs.',                        'ACCEPTED',  'Strong backend background.',         '2026-04-06 10:00:00', '2026-04-07 09:00:00', NULL,                   '2026-04-07 09:00:00'),
    (5003, 4003, 1007, 1002, 2, 'Want to learn observability tooling.',                      'REJECTED',  'Need stronger Java background.',     '2026-04-06 12:00:00', '2026-04-08 09:30:00', NULL,                   '2026-04-08 09:30:00'),
    (5004, 4004, 1008, NULL, 1, 'Data pipeline experience from coursework.',                 'PENDING',   NULL,                                '2026-04-07 15:00:00', NULL,                   NULL,                   '2026-04-07 15:00:00'),
    (5005, 4006, 1009, 1004, 1, 'Interested in NLP and LLM evaluation.',                    'ACCEPTED',  'Good match for the topic.',          '2026-04-08 16:00:00', '2026-04-10 11:00:00', NULL,                   '2026-04-10 11:00:00'),
    (5006, 4007, 1003, 1004, 2, 'I can help with recommendation metrics.',                   'REJECTED',  'Position already filled.',           '2026-04-09 09:00:00', '2026-04-12 09:30:00', NULL,                   '2026-04-12 09:30:00'),
    (5007, 4011, 1006, NULL, 1, 'Frontend portfolio and React experience.',                  'PENDING',   NULL,                                '2026-04-10 14:00:00', NULL,                   NULL,                   '2026-04-10 14:00:00'),
    (5008, 4012, 1007, NULL, 1, 'Interested in interaction design and search UX.',           'PENDING',   NULL,                                '2026-04-11 10:30:00', NULL,                   NULL,                   '2026-04-11 10:30:00'),
    (5009, 4013, 1008, NULL, 2, 'Want to work on analytics export pipeline.',                'PENDING',   NULL,                                '2026-04-11 11:15:00', NULL,                   NULL,                   '2026-04-11 11:15:00'),
    (5010, 4015, 1009, NULL, 1, 'I am interested in student services products.',             'WITHDRAWN', NULL,                                '2026-04-12 09:45:00', NULL,                   '2026-04-13 17:00:00', '2026-04-13 17:00:00');

INSERT IGNORE INTO request_status_history (history_id, request_id, old_status, new_status, changed_by, remark, changed_at) VALUES
    (6001, 5001, NULL,        'PENDING',   1003, 'Student submitted the request.',                 '2026-04-05 14:00:00'),
    (6002, 5002, NULL,        'PENDING',   1006, 'Student submitted the request.',                 '2026-04-06 10:00:00'),
    (6003, 5002, 'PENDING',   'ACCEPTED',  NULL, 'Strong backend background.',   '2026-04-07 09:00:00'),
    (6004, 5003, NULL,        'PENDING',   1007, 'Student submitted the request.',                 '2026-04-06 12:00:00'),
    (6005, 5003, 'PENDING',   'REJECTED',  NULL, 'Need stronger Java background.', '2026-04-08 09:30:00'),
    (6006, 5004, NULL,        'PENDING',   1008, 'Student submitted the request.',                 '2026-04-07 15:00:00'),
    (6007, 5005, NULL,        'PENDING',   1009, 'Student submitted the request.',                 '2026-04-08 16:00:00'),
    (6008, 5005, 'PENDING',   'ACCEPTED',  NULL, 'Good match for the topic.',    '2026-04-10 11:00:00'),
    (6009, 5006, NULL,        'PENDING',   1003, 'Student submitted the request.',                 '2026-04-09 09:00:00'),
    (6010, 5006, 'PENDING',   'REJECTED',  NULL, 'Position already filled.',     '2026-04-12 09:30:00'),
    (6011, 5007, NULL,        'PENDING',   1006, 'Student submitted the request.',                 '2026-04-10 14:00:00'),
    (6012, 5008, NULL,        'PENDING',   1007, 'Student submitted the request.',                 '2026-04-11 10:30:00'),
    (6013, 5009, NULL,        'PENDING',   1008, 'Student submitted the request.',                 '2026-04-11 11:15:00'),
    (6014, 5010, NULL,        'PENDING',   1009, 'Student submitted the request.',                 '2026-04-12 09:45:00'),
    (6015, 5010, 'PENDING',   'WITHDRAWN', 1009, 'Student withdrew the request.',                 '2026-04-13 17:00:00');
