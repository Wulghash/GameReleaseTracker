INSERT INTO games (id, title, description, release_date, status, developer, publisher, shop_url, image_url, created_at, updated_at)
VALUES
    (
        'b0000001-0000-0000-0000-000000000001',
        'Saros',
        'A third-person action game from Housemarque set on an alien world trapped beneath an eternal eclipse, blending roguelite progression with intense bullet-hell combat.',
        '2026-04-30',
        'UPCOMING',
        'Housemarque',
        'Sony Interactive Entertainment',
        NULL,
        NULL,
        NOW(),
        NOW()
    ),
    (
        'b0000002-0000-0000-0000-000000000002',
        'World of Warcraft: Midnight',
        'The next major World of Warcraft expansion, returning to Quel''Thalas as darkness descends and the blood elves face an ancient evil threatening all of Azeroth.',
        '2026-03-02',
        'UPCOMING',
        'Blizzard Entertainment',
        'Blizzard Entertainment',
        NULL,
        NULL,
        NOW(),
        NOW()
    ),
    (
        'b0000003-0000-0000-0000-000000000003',
        'Diablo IV: Lord of Hatred',
        'The second expansion to Diablo IV. Mephisto, the Lord of Hatred, rises as the central antagonist in a new chapter of the eternal conflict between angels and demons.',
        '2026-04-28',
        'UPCOMING',
        'Blizzard Entertainment',
        'Blizzard Entertainment',
        NULL,
        NULL,
        NOW(),
        NOW()
    ),
    (
        'b0000004-0000-0000-0000-000000000004',
        'Resident Evil Requiem',
        'The ninth mainline Resident Evil entry. Leon S. Kennedy and Grace Ashcroft investigate a terrifying new bioterror threat in this continuation of the survival horror series.',
        '2026-02-27',
        'UPCOMING',
        'Capcom',
        'Capcom',
        NULL,
        NULL,
        NOW(),
        NOW()
    );

INSERT INTO game_platforms (game_id, platform)
VALUES
    ('b0000001-0000-0000-0000-000000000001', 'PS5'),

    ('b0000002-0000-0000-0000-000000000002', 'PC'),

    ('b0000003-0000-0000-0000-000000000003', 'PC'),
    ('b0000003-0000-0000-0000-000000000003', 'PS5'),
    ('b0000003-0000-0000-0000-000000000003', 'XBOX'),

    ('b0000004-0000-0000-0000-000000000004', 'PC'),
    ('b0000004-0000-0000-0000-000000000004', 'PS5'),
    ('b0000004-0000-0000-0000-000000000004', 'XBOX');
