INSERT INTO services
    (name, slug, category, description, price, duration_minutes, image_url, is_active)
VALUES
(
    'Dreadlock Installation',
    'dreadlock-installation',
    'OTHER',
    'Professional dreadlock installation tailored to your preferred style and hair length.',
    2500.00, 180, NULL, TRUE
),
(
    'Dreadlock Retouch',
    'dreadlock-retouch',
    'OTHER',
    'Professional retouching to restore neatness and maintain your dreadlocks.',
    1500.00, 120, NULL, TRUE
),
(
    'Dreadlock Styling',
    'dreadlock-styling',
    'OTHER',
    'Creative styling for existing dreadlocks for events, occasions or everyday wear.',
    1000.00, 60, NULL, TRUE
);
-- Correct the original three services
UPDATE services
SET category = 'DREADLOCKS'
WHERE slug IN (
    'dreadlock-installation',
    'dreadlock-retouch',
    'dreadlock-styling'
);

-- Add the remaining DREADLOCK services
INSERT INTO services
    (name, slug, category, description, price, duration_minutes, image_url, is_active)
VALUES
(
    'Dreadlock Uninstallation',
    'dreadlock-uninstallation',
    'DREADLOCKS',
    'Professional removal of existing dreadlocks.',
    0.00, 180, NULL, TRUE
),
(
    'Dreadlock Repair',
    'dreadlock-repair',
    'DREADLOCKS',
    'Repair and restoration of damaged or weak dreadlocks.',
    0.00, 90, NULL, TRUE
),
(
    'Dreadlock Detox',
    'dreadlock-detox',
    'DREADLOCKS',
    'Deep cleaning treatment for existing dreadlocks.',
    0.00, 90, NULL, TRUE
),
(
    'Dreadlock Coloring',
    'dreadlock-coloring',
    'DREADLOCKS',
    'Professional coloring service for dreadlocks.',
    0.00, 120, NULL, TRUE
),

-- HAIR
(
    'Hair Wash',
    'hair-wash',
    'HAIR',
    'Professional hair washing and cleansing service.',
    0.00, 30, NULL, TRUE
),
(
    'Hair Treatment',
    'hair-treatment',
    'HAIR',
    'Hair and scalp treatment to improve hair condition and appearance.',
    0.00, 60, NULL, TRUE
),
(
    'Hair Styling',
    'hair-styling',
    'HAIR',
    'Professional styling for different occasions.',
    0.00, 60, NULL, TRUE
),
(
    'Blow Dry',
    'blow-dry',
    'HAIR',
    'Professional hair blow-drying and finishing.',
    0.00, 45, NULL, TRUE
),
(
    'Hair Coloring',
    'hair-coloring',
    'HAIR',
    'Professional hair coloring service.',
    0.00, 120, NULL, TRUE
),

-- NAILS
(
    'Manicure',
    'manicure',
    'NAILS',
    'Basic manicure and nail care service.',
    0.00, 60, NULL, TRUE
),
(
    'Pedicure',
    'pedicure',
    'NAILS',
    'Basic pedicure and foot nail care service.',
    0.00, 60, NULL, TRUE
),
(
    'Gel Manicure',
    'gel-manicure',
    'NAILS',
    'Gel polish manicure service.',
    0.00, 90, NULL, TRUE
),
(
    'Gel Pedicure',
    'gel-pedicure',
    'NAILS',
    'Gel polish pedicure service.',
    0.00, 90, NULL, TRUE
),
(
    'Nail Extensions',
    'nail-extensions',
    'NAILS',
    'Professional nail extension service.',
    0.00, 120, NULL, TRUE
),
(
    'Nail Art',
    'nail-art',
    'NAILS',
    'Creative nail art and decorative designs.',
    0.00, 60, NULL, TRUE
),
(
    'Nail Removal',
    'nail-removal',
    'NAILS',
    'Professional removal of artificial or gel nails.',
    0.00, 45, NULL, TRUE
),

-- BEAUTY
(
    'Facial',
    'facial',
    'BEAUTY',
    'Basic facial cleansing and skincare treatment.',
    0.00, 60, NULL, TRUE
),
(
    'Eyebrow Shaping',
    'eyebrow-shaping',
    'BEAUTY',
    'Professional eyebrow shaping and grooming.',
    0.00, 30, NULL, TRUE
),
(
    'Eyebrow Tinting',
    'eyebrow-tinting',
    'BEAUTY',
    'Eyebrow tinting service.',
    0.00, 30, NULL, TRUE
),
(
    'Eyelash Extensions',
    'eyelash-extensions',
    'BEAUTY',
    'Professional eyelash extension application.',
    0.00, 120, NULL, TRUE
),
(
    'Eyelash Removal',
    'eyelash-removal',
    'BEAUTY',
    'Professional eyelash extension removal.',
    0.00, 30, NULL, TRUE
),
(
    'Makeup Application',
    'makeup-application',
    'BEAUTY',
    'Professional makeup application for events and special occasions.',
    0.00, 60, NULL, TRUE
),

-- TATTOO
(
    'Tattoo',
    'tattoo',
    'TATTOO',
    'Tattoo service. Final pricing depends on design, size and complexity.',
    0.00, 120, NULL, FALSE
),
(
    'Tattoo Touch-up',
    'tattoo-touch-up',
    'TATTOO',
    'Touch-up service for existing tattoos.',
    0.00, 60, NULL, FALSE
),
(
    'Tattoo Consultation',
    'tattoo-consultation',
    'TATTOO',
    'Consultation for tattoo design, placement and pricing.',
    0.00, 30, NULL, FALSE
),

-- PIERCING
(
    'Ear Piercing',
    'ear-piercing',
    'PIERCING',
    'Professional ear piercing service.',
    0.00, 30, NULL, FALSE
),
(
    'Nose Piercing',
    'nose-piercing',
    'PIERCING',
    'Professional nose piercing service.',
    0.00, 30, NULL, FALSE
),
(
    'Body Piercing',
    'body-piercing',
    'PIERCING',
    'Professional body piercing service.',
    0.00, 45, NULL, FALSE
);

