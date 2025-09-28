-- Insert test users for development
-- Password is BCrypt hash of 'password123'
INSERT INTO users (
    username, 
    email, 
    first_name, 
    last_name, 
    password, 
    status, 
    user_type, 
    is_verified, 
    is_active, 
    role,
    address,
    city,
    state,
    country
) VALUES (
    'testuser',
    'text@example.com',
    'Test',
    'User',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', -- password123
    'ACTIVE',
    'HOUSEHOLD',
    true,
    true,
    'ROLE_USER',
    '123 Test Street',
    'Lagos',
    'Lagos',
    'Nigeria'
);

-- Insert admin user
INSERT INTO users (
    username, 
    email, 
    first_name, 
    last_name, 
    password, 
    status, 
    user_type, 
    is_verified, 
    is_active, 
    role,
    address,
    city,
    state,
    country
) VALUES (
    'admin',
    'admin@smartwatts.ng',
    'Admin',
    'User',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', -- password123
    'ACTIVE',
    'HOUSEHOLD',
    true,
    true,
    'ROLE_ADMIN',
    '456 Admin Avenue',
    'Abuja',
    'FCT',
    'Nigeria'
); 