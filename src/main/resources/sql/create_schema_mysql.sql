-- MySQL schema creation script for PelisApp
-- Crea todas las tablas necesarias para las entidades JPA del proyecto
-- Engine: InnoDB, charset utf8mb4

SET FOREIGN_KEY_CHECKS = 0;

-- Tabla usuario (User entity)
CREATE TABLE IF NOT EXISTS `usuario` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(100) NOT NULL,
  `email` VARCHAR(200) DEFAULT NULL,
  `password` VARCHAR(255) NOT NULL,
  `display_name` VARCHAR(255) DEFAULT NULL,
  `registered_at` DATETIME DEFAULT NULL,
  `critic_level` INT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `usuario_username_key` (`username`),
  UNIQUE KEY `usuario_email_key` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Roles
CREATE TABLE IF NOT EXISTS `role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `description` TEXT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `role_name_key` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tags / Achievements (insignias)
CREATE TABLE IF NOT EXISTS `tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(100) NOT NULL,
  `name` VARCHAR(200) NOT NULL,
  `description` TEXT,
  `icon_url` TEXT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `tag_code_key` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Achievements master (Archivement entity)
CREATE TABLE IF NOT EXISTS `archivement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(200) NOT NULL,
  `name` VARCHAR(255) DEFAULT NULL,
  `description` TEXT,
  `icon_url` TEXT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `archivement_code_key` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Movies
CREATE TABLE IF NOT EXISTS `movie` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `tmdb_id` BIGINT DEFAULT NULL,
  `title` VARCHAR(1024) NOT NULL,
  `description` TEXT,
  `release_date` DATE DEFAULT NULL,
  `runtime_minutes` INT DEFAULT NULL,
  `poster_path` VARCHAR(2048) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `movie_tmdb_id_key` (`tmdb_id`),
  KEY `idx_movie_title` (`title`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Actors
CREATE TABLE IF NOT EXISTS `actor` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `tmdb_id` BIGINT DEFAULT NULL,
  `name` VARCHAR(512) NOT NULL,
  `profile_path` VARCHAR(2048) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `actor_tmdb_id_key` (`tmdb_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Directors
CREATE TABLE IF NOT EXISTS `director` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `tmdb_id` BIGINT DEFAULT NULL,
  `name` VARCHAR(512) NOT NULL,
  `profile_path` VARCHAR(2048) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `director_tmdb_id_key` (`tmdb_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Category entity
CREATE TABLE IF NOT EXISTS `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `category_name_key` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Country (minimal as in entity)
CREATE TABLE IF NOT EXISTS `country` (
  `id` BIGINT NOT NULL,
  `name` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Review
CREATE TABLE IF NOT EXISTS `review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `movie_id` BIGINT NOT NULL,
  `text` VARCHAR(2000) NOT NULL,
  `stars` INT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT NULL,
  `likes_count` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_review_movie` (`movie_id`),
  KEY `idx_review_user` (`user_id`),
  CONSTRAINT `fk_review_user` FOREIGN KEY (`user_id`) REFERENCES `usuario` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_review_movie` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ReviewLike
CREATE TABLE IF NOT EXISTS `review_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `review_id` BIGINT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_review_like_user_review` (`user_id`,`review_id`),
  CONSTRAINT `fk_review_like_user` FOREIGN KEY (`user_id`) REFERENCES `usuario` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_review_like_review` FOREIGN KEY (`review_id`) REFERENCES `review` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ComentaryModeration (one-to-one with review)
CREATE TABLE IF NOT EXISTS `comentary_moderation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `review_id` BIGINT NOT NULL,
  `ai_score` DOUBLE DEFAULT NULL,
  `ai_decision` VARCHAR(100) DEFAULT NULL,
  `ai_reason` TEXT DEFAULT NULL,
  `human_reviewed` TINYINT(1) DEFAULT 0,
  `human_decision` VARCHAR(100) DEFAULT NULL,
  `human_notes` TEXT DEFAULT NULL,
  `reviewed_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_comentary_moderation_review` (`review_id`),
  CONSTRAINT `fk_comentary_moderation_review` FOREIGN KEY (`review_id`) REFERENCES `review` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Score (valoración numérica por película)
CREATE TABLE IF NOT EXISTS `score` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `movie_id` BIGINT NOT NULL,
  `value` INT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_score_user_movie` (`user_id`,`movie_id`),
  KEY `idx_score_movie` (`movie_id`),
  KEY `idx_score_user` (`user_id`),
  CONSTRAINT `fk_score_user` FOREIGN KEY (`user_id`) REFERENCES `usuario`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_score_movie` FOREIGN KEY (`movie_id`) REFERENCES `movie`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Following (usuario sigue a usuario)
CREATE TABLE IF NOT EXISTS `following` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `follower_id` BIGINT NOT NULL,
  `followed_id` BIGINT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_following_pair` (`follower_id`,`followed_id`),
  CONSTRAINT `fk_following_follower` FOREIGN KEY (`follower_id`) REFERENCES `usuario` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_following_followed` FOREIGN KEY (`followed_id`) REFERENCES `usuario` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Usuario_Archivement (usuario_archivement)
CREATE TABLE IF NOT EXISTS `usuario_archivement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `usuario_id` BIGINT NOT NULL,
  `archivement_id` BIGINT NOT NULL,
  `awarded_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `pinned_to_profile` TINYINT(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_usuario_archivement` (`usuario_id`,`archivement_id`),
  CONSTRAINT `fk_usuario_archivement_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_usuario_archivement_archivement` FOREIGN KEY (`archivement_id`) REFERENCES `archivement`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Usuario_Role (join table)
CREATE TABLE IF NOT EXISTS `usuario_role` (
  `usuario_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`usuario_id`,`role_id`),
  CONSTRAINT `fk_usuario_role_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_usuario_role_role` FOREIGN KEY (`role_id`) REFERENCES `role`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Usuario_Tag (join table)
CREATE TABLE IF NOT EXISTS `usuario_tag` (
  `usuario_id` BIGINT NOT NULL,
  `tag_id` BIGINT NOT NULL,
  PRIMARY KEY (`usuario_id`,`tag_id`),
  CONSTRAINT `fk_usuario_tag_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_usuario_tag_tag` FOREIGN KEY (`tag_id`) REFERENCES `tag`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Movie <-> Actor join
CREATE TABLE IF NOT EXISTS `movie_actor` (
  `movie_id` BIGINT NOT NULL,
  `actor_id` BIGINT NOT NULL,
  PRIMARY KEY (`movie_id`,`actor_id`),
  CONSTRAINT `fk_movie_actor_movie` FOREIGN KEY (`movie_id`) REFERENCES `movie`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_movie_actor_actor` FOREIGN KEY (`actor_id`) REFERENCES `actor`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Movie <-> Director join
CREATE TABLE IF NOT EXISTS `movie_director` (
  `movie_id` BIGINT NOT NULL,
  `director_id` BIGINT NOT NULL,
  PRIMARY KEY (`movie_id`,`director_id`),
  CONSTRAINT `fk_movie_director_movie` FOREIGN KEY (`movie_id`) REFERENCES `movie`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_movie_director_director` FOREIGN KEY (`director_id`) REFERENCES `director`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Movie <-> Category join
CREATE TABLE IF NOT EXISTS `movie_category` (
  `movie_id` BIGINT NOT NULL,
  `category_id` BIGINT NOT NULL,
  PRIMARY KEY (`movie_id`,`category_id`),
  CONSTRAINT `fk_movie_category_movie` FOREIGN KEY (`movie_id`) REFERENCES `movie`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_movie_category_category` FOREIGN KEY (`category_id`) REFERENCES `category`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes adicionales
CREATE INDEX IF NOT EXISTS `idx_review_user` ON `review` (`user_id`);
CREATE INDEX IF NOT EXISTS `idx_review_movie` ON `review` (`movie_id`);
CREATE INDEX IF NOT EXISTS `idx_score_movie` ON `score` (`movie_id`);
CREATE INDEX IF NOT EXISTS `idx_score_user` ON `score` (`user_id`);

SET FOREIGN_KEY_CHECKS = 1;

-- Optional: seed roles and some tags (uncomment to insert default roles)
-- INSERT INTO role (name, description) VALUES ('ROLE_USER','Default user role') ON DUPLICATE KEY UPDATE name=name;
-- INSERT INTO role (name, description) VALUES ('ROLE_CRITIC','Critic role based on likes') ON DUPLICATE KEY UPDATE name=name;
-- INSERT INTO role (name, description) VALUES ('ROLE_TOP_CRITIC','Top critic role') ON DUPLICATE KEY UPDATE name=name;

-- END
