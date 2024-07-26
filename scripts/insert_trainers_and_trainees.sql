-- insert fcm token

INSERT INTO fcm_token
    (token, created_at, modified_at)
VALUES ('some-token-value-1', '2024-07-01 00:00:00', '2024-07-01 00:00:00'),
       ('some-token-value-2', '2024-07-02 00:00:00', '2024-07-02 00:00:00'),
       ('some-token-value-3', '2024-07-03 00:00:00', '2024-07-03 00:00:00'),
       ('some-token-value-4', '2024-07-04 00:00:00', '2024-07-04 00:00:00'),
       ('some-token-value-5', '2024-07-05 00:00:00', '2024-07-05 00:00:00'),
       ('some-token-value-6', '2024-07-06 00:00:00', '2024-07-06 00:00:00'),
       ('some-token-value-7', '2024-07-07 00:00:00', '2024-07-07 00:00:00'),
       ('some-token-value-8', '2024-07-08 00:00:00', '2024-07-08 00:00:00'),
       ('some-token-value-9', '2024-07-09 00:00:00', '2024-07-09 00:00:00'),
       ('some-token-value-10', '2024-07-10 00:00:00', '2024-07-10 00:00:00'),
       ('some-token-value-11', '2024-07-11 00:00:00', '2024-07-11 00:00:00'),
       ('some-token-value-12', '2024-07-12 00:00:00', '2024-07-12 00:00:00'),
       ('some-token-value-13', '2024-07-13 00:00:00', '2024-07-13 00:00:00'),
       ('some-token-value-14', '2024-07-14 00:00:00', '2024-07-14 00:00:00'),
       ('some-token-value-15', '2024-07-15 00:00:00', '2024-07-15 00:00:00'),
       ('some-token-value-16', '2024-07-16 00:00:00', '2024-07-16 00:00:00'),
       ('some-token-value-17', '2024-07-17 00:00:00', '2024-07-17 00:00:00'),
       ('some-token-value-18', '2024-07-18 00:00:00', '2024-07-18 00:00:00'),
       ('some-token-value-19', '2024-07-19 00:00:00', '2024-07-19 00:00:00'),
       ('some-token-value-20', '2024-07-20 00:00:00', '2024-07-20 00:00:00');

-- insert trainers      

INSERT INTO trainer
(unread_notification,
 fcm_token_id,
 modified_at,
 created_at,
 email,
 name,
 password,
 role)
VALUES (false,
        (SELECT id FROM fcm_token WHERE token = 'some-token-value-1'),
        '2024-07-01 00:00:00',
        '2024-07-01 00:00:00',
        'trainer1@gmail.com',
        '김영수',
        '$2a$10$eOXP7fCx08uUH1OCtWAo..uYjvIIZMh08pV2huGCK8M0kRvvQ/1DK',
        'TRAINER'),
       (false,
        (SELECT id FROM fcm_token WHERE token = 'some-token-value-2'),
        '2024-07-02 00:00:00',
        '2024-07-02 00:00:00',
        'trainer2@gmail.com',
        '박민수',
        '$2a$10$eOXP7fCx08uUH1OCtWAo..uYjvIIZMh08pV2huGCK8M0kRvvQ/1DK',
        'TRAINER'),
       (false,
        (SELECT id FROM fcm_token WHERE token = 'some-token-value-3'),
        '2024-07-03 00:00:00',
        '2024-07-03 00:00:00',
        'trainer3@gmail.com',
        '이영희',
        '$2a$10$eOXP7fCx08uUH1OCtWAo..uYjvIIZMh08pV2huGCK8M0kRvvQ/1DK',
        'TRAINER'),
       (false,
        (SELECT id FROM fcm_token WHERE token = 'some-token-value-4'),
        '2024-07-04 00:00:00',
        '2024-07-04 00:00:00',
        'trainer4@gmail.com',
        '최지훈',
        '$2a$10$eOXP7fCx08uUH1OCtWAo..uYjvIIZMh08pV2huGCK8M0kRvvQ/1DK',
        'TRAINER'),
       (false,
        (SELECT id FROM fcm_token WHERE token = 'some-token-value-5'),
        '2024-07-05 00:00:00',
        '2024-07-05 00:00:00',
        'trainer5@gmail.com',
        '윤서진',
        '$2a$10$eOXP7fCx08uUH1OCtWAo..uYjvIIZMh08pV2huGCK8M0kRvvQ/1DK',
        'TRAINER'),
       (false,
        (SELECT id FROM fcm_token WHERE token = 'some-token-value-6'),
        '2024-07-06 00:00:00',
        '2024-07-06 00:00:00',
        'trainer6@gmail.com',
        '강민지',
        '$2a$10$eOXP7fCx08uUH1OCtWAo..uYjvIIZMh08pV2huGCK8M0kRvvQ/1DK',
        'TRAINER'),
       (false,
        (SELECT id FROM fcm_token WHERE token = 'some-token-value-7'),
        '2024-07-07 00:00:00',
        '2024-07-07 00:00:00',
        'trainer7@gmail.com',
        '정재훈',
        '$2a$10$eOXP7fCx08uUH1OCtWAo..uYjvIIZMh08pV2huGCK8M0kRvvQ/1DK',
        'TRAINER'),
       (false,
        (SELECT id FROM fcm_token WHERE token = 'some-token-value-8'),
        '2024-07-08 00:00:00',
        '2024-07-08 00:00:00',
        'trainer8@gmail.com',
        '한유진',
        '$2a$10$eOXP7fCx08uUH1OCtWAo..uYjvIIZMh08pV2huGCK8M0kRvvQ/1DK',
        'TRAINER'),
       (false,
        (SELECT id FROM fcm_token WHERE token = 'some-token-value-9'),
        '2024-07-09 00:00:00',
        '2024-07-09 00:00:00',
        'trainer9@gmail.com',
        '최민호',
        '$2a$10$eOXP7fCx08uUH1OCtWAo..uYjvIIZMh08pV2huGCK8M0kRvvQ/1DK',
        'TRAINER'),
       (false,
        (SELECT id FROM fcm_token WHERE token = 'some-token-value-10'),
        '2024-07-10 00:00:00',
        '2024-07-10 00:00:00',
        'trainer10@gmail.com',
        '김민정',
        '$2a$10$eOXP7fCx08uUH1OCtWAo..uYjvIIZMh08pV2huGCK8M0kRvvQ/1DK',
        'TRAINER');

-- insert trainees

INSERT INTO trainee
(unread_notification,
 fcm_token_id,
 modified_at,
 created_at,
 email,
 name,
 password,
 role,
 height,
 birth_date,
 target_type,
 target_value,
 target_reward,
 gender)
VALUES (false,
        (SELECT id FROM fcm_token WHERE token = 'some-token-value-11'),
        '2024-07-01 00:00:00',
        '2024-07-01 00:00:00',
        'trainee1@gmail.com',
        '김영수',
        '$2a$10$eOXP7fCx08uUH1OCtWAo..uYjvIIZMh08pV2huGCK8M0kRvvQ/1DK',
        'TRAINEE',
        170,
        '2000-01-01',
        'TARGET_BODY_FAT_PERCENTAGE',
        10,
        'PT+1',
        'MALE'),
       (false,
        (SELECT id FROM fcm_token WHERE token = 'some-token-value-12'),
        '2024-07-02 00:00:00',
        '2024-07-02 00:00:00',
        'trainee2@gmail.com',
        '김영자',
        '$2a$10$eOXP7fCx08uUH1OCtWAo..uYjvIIZMh08pV2huGCK8M0kRvvQ/1DK',
        'TRAINEE',
        160,
        '1999-01-01',
        'TARGET_BODY_FAT_PERCENTAGE',
        10,
        'PT+1',
        'FEMALE'),
       (false,
        (SELECT id FROM fcm_token WHERE token = 'some-token-value-13'),
        '2024-07-03 00:00:00',
        '2024-07-03 00:00:00',
        'trainee3@gmail.com',
        '박철수',
        '$2a$10$eOXP7fCx08uUH1OCtWAo..uYjvIIZMh08pV2huGCK8M0kRvvQ/1DK',
        'TRAINEE',
        175,
        '2001-02-15',
        'TARGET_BODY_FAT_PERCENTAGE',
        15,
        'PT+2',
        'MALE'),
       (false,
        (SELECT id FROM fcm_token WHERE token = 'some-token-value-14'),
        '2024-07-04 00:00:00',
        '2024-07-04 00:00:00',
        'trainee4@gmail.com',
        '이영희',
        '$2a$10$eOXP7fCx08uUH1OCtWAo..uYjvIIZMh08pV2huGCK8M0kRvvQ/1DK',
        'TRAINEE',
        165,
        '1998-03-10',
        'TARGET_SKELETAL_MUSCLE_MASS',
        30,
        'PT+3',
        'FEMALE'),
       (false,
        (SELECT id FROM fcm_token WHERE token = 'some-token-value-15'),
        '2024-07-05 00:00:00',
        '2024-07-05 00:00:00',
        'trainee5@gmail.com',
        '최민호',
        '$2a$10$eOXP7fCx08uUH1OCtWAo..uYjvIIZMh08pV2huGCK8M0kRvvQ/1DK',
        'TRAINEE',
        180,
        '2002-05-20',
        'TARGET_SKELETAL_MUSCLE_MASS',
        40,
        'PT+4',
        'MALE'),
       (false,
        (SELECT id FROM fcm_token WHERE token = 'some-token-value-16'),
        '2024-07-06 00:00:00',
        '2024-07-06 00:00:00',
        'trainee6@gmail.com',
        '김민정',
        '$2a$10$eOXP7fCx08uUH1OCtWAo..uYjvIIZMh08pV2huGCK8M0kRvvQ/1DK',
        'TRAINEE',
        155,
        '2000-04-18',
        'TARGET_SKELETAL_MUSCLE_MASS',
        30,
        'PT+5',
        'FEMALE'),
       (false,
        (SELECT id FROM fcm_token WHERE token = 'some-token-value-17'),
        '2024-07-07 00:00:00',
        '2024-07-07 00:00:00',
        'trainee7@gmail.com',
        '윤상현',
        '$2a$10$eOXP7fCx08uUH1OCtWAo..uYjvIIZMh08pV2huGCK8M0kRvvQ/1DK',
        'TRAINEE',
        178,
        '1997-06-25',
        'TARGET_WEIGHT',
        60,
        'PT+6',
        'MALE'),
       (false,
        (SELECT id FROM fcm_token WHERE token = 'some-token-value-18'),
        '2024-07-08 00:00:00',
        '2024-07-08 00:00:00',
        'trainee8@gmail.com',
        '정민주',
        '$2a$10$eOXP7fCx08uUH1OCtWAo..uYjvIIZMh08pV2huGCK8M0kRvvQ/1DK',
        'TRAINEE',
        162,
        '1996-08-14',
        'TARGET_WEIGHT',
        40,
        'PT+7',
        'FEMALE'),
       (false,
        (SELECT id FROM fcm_token WHERE token = 'some-token-value-19'),
        '2024-07-09 00:00:00',
        '2024-07-09 00:00:00',
        'trainee9@gmail.com',
        '한재민',
        '$2a$10$eOXP7fCx08uUH1OCtWAo..uYjvIIZMh08pV2huGCK8M0kRvvQ/1DK',
        'TRAINEE',
        172,
        '1998-11-22',
        'TARGET_WEIGHT',
        50,
        'PT+8',
        'MALE'),
       (false,
        (SELECT id FROM fcm_token WHERE token = 'some-token-value-20'),
        '2024-07-10 00:00:00',
        '2024-07-10 00:00:00',
        'trainee10@gmail.com',
        '박진아',
        '$2a$10$eOXP7fCx08uUH1OCtWAo..uYjvIIZMh08pV2huGCK8M0kRvvQ/1DK',
        'TRAINEE',
        168,
        '2001-12-01',
        'TARGET_WEIGHT',
        60,
        'PT+9',
        'FEMALE');
