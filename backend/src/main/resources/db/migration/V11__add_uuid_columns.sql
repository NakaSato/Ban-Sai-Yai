-- Add UUID column to existing tables that extend BaseEntity
-- This migration adds UUID support for security (prevent ID enumeration)

-- Add UUID column to payment_notification table
ALTER TABLE payment_notification 
ADD COLUMN IF NOT EXISTS uuid UUID;

-- Generate UUIDs for existing records
UPDATE payment_notification 
SET uuid = gen_random_uuid() 
WHERE uuid IS NULL;

-- Make UUID column NOT NULL and UNIQUE
ALTER TABLE payment_notification 
ALTER COLUMN uuid SET NOT NULL,
ADD CONSTRAINT payment_notification_uuid_unique UNIQUE (uuid);

-- Create index on UUID for performance
CREATE INDEX IF NOT EXISTS idx_payment_notification_uuid ON payment_notification(uuid);

-- Add UUID column to collateral table (for loan documents)
ALTER TABLE collateral 
ADD COLUMN IF NOT EXISTS uuid UUID;

-- Generate UUIDs for existing records
UPDATE collateral 
SET uuid = gen_random_uuid() 
WHERE uuid IS NULL;

-- Make UUID column NOT NULL and UNIQUE
ALTER TABLE collateral 
ALTER COLUMN uuid SET NOT NULL,
ADD CONSTRAINT collateral_uuid_unique UNIQUE (uuid);

-- Create index on UUID for performance
CREATE INDEX IF NOT EXISTS idx_collateral_uuid ON collateral(uuid);

-- Add comments
COMMENT ON COLUMN payment_notification.uuid IS 'External UUID identifier for API responses (security)';
COMMENT ON COLUMN collateral.uuid IS 'External UUID identifier for API responses (security)';
