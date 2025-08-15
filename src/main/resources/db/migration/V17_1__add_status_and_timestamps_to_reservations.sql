ALTER TABLE `reservations`
  ADD COLUMN `status` VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  ADD COLUMN `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  ADD COLUMN `updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);

-- Index utiles
CREATE INDEX idx_res_rep ON reservations(representation_id);
CREATE INDEX idx_res_user ON reservations(user_id);
CREATE INDEX idx_res_status ON reservations(status);
