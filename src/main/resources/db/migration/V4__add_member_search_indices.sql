-- Add indices for member search optimization
-- These indices improve performance for the omni-search functionality

-- Index on member_id for quick lookup
CREATE INDEX IF NOT EXISTS idx_member_member_id ON member(member_id);

-- Index on name for search queries
CREATE INDEX IF NOT EXISTS idx_member_name ON member(name);

-- Index on id_card for search queries
CREATE INDEX IF NOT EXISTS idx_member_id_card ON member(id_card);

-- Composite index for active members search
CREATE INDEX IF NOT EXISTS idx_member_active_name ON member(is_active, name);
