INSERT INTO reserved_items (id, order_id, item_id, quantity, status, reserved_at,
                            created, created_by, updated, updated_by, version)
VALUES ('0619fc6a-9a01-43c2-9ccc-ceede34308e7', '06b58c2c-79ab-4b5a-9ff4-05dc368d8388',
        'a93383c5-565b-4f7e-b8e3-ad5511d8b261', 2, 'RESERVED', now(), now(),
        'SYSTEM', now(), 'SYSTEM', 0),
       ('bac8611b-b592-4ce0-96e0-cf38263c2014', '06b58c2c-79ab-4b5a-9ff4-05dc368d8388',
        'a93383c5-565b-4f7e-b8e3-ad5511d8b261', 5, 'RESERVED', now(), now(),
        'SYSTEM', now(), 'SYSTEM', 0),
       ('bba8dec4-cd61-4ad7-9f88-b3ae1764abb3', '06b58c2c-79ab-4b5a-9ff4-05dc368d8388',
        'c1daddc1-23fb-40f8-b908-34b490b0e7b9', 1, 'CANCELLED', now(), now(),
        'SYSTEM', now(), 'SYSTEM', 0),
       ('713712e4-07f0-47d2-8a29-51b01c3b0090', '02ec61cd-2c89-45aa-aa6d-eed6ffcbf1e5',
        '6cd2e2f7-0043-4055-9387-1c1b5155cbfd', 3, 'RESERVED', now(), now(),
        'SYSTEM', now(), 'SYSTEM', 0),
       ('faa70a43-61ef-491f-86f5-d291d4fb2414', '02ec61cd-2c89-45aa-aa6d-eed6ffcbf1e5',
        '4bebc928-bbce-4874-8873-c70d76fe4476', 4, 'FAILED', now(), now(),
        'SYSTEM', now(), 'SYSTEM', 0)
;
