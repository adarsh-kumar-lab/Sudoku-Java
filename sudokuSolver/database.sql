-- =============================================================
--  SUDOKU GAME DATABASE SETUP
--  Run this in MySQL Workbench or MySQL CLI BEFORE running app
--  Project: Sudoku Solver + Validator + Game with Levels
--  Group 1 | B.Tech CSE Sec-E | Centurion University
-- =============================================================

CREATE DATABASE IF NOT EXISTS sudoku_db;
USE sudoku_db;

-- ── Table 1: Stores every game session ──────────────────────
CREATE TABLE IF NOT EXISTS game_sessions (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    player_name   VARCHAR(100)  DEFAULT 'Player',
    difficulty    VARCHAR(10)   NOT NULL,   -- easy/medium/hard/expert
    mode          VARCHAR(20)   NOT NULL,   -- 'game' or 'validator'
    is_solved     TINYINT(1)    DEFAULT 0,  -- 1=solved, 0=not yet
    time_taken    INT           DEFAULT 0,  -- seconds
    mistakes      INT           DEFAULT 0,
    hints_used    INT           DEFAULT 0,
    started_at    DATETIME      DEFAULT NOW(),
    finished_at   DATETIME      NULL
);

-- ── Table 2: Stores every validation attempt ────────────────
CREATE TABLE IF NOT EXISTS validation_history (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    board_snapshot  TEXT          NOT NULL,  -- 81-digit string
    is_valid        TINYINT(1)    NOT NULL,  -- 1=valid, 0=invalid
    result_message  VARCHAR(500)  NOT NULL,
    validated_at    DATETIME      DEFAULT NOW()
);

-- ── Table 3: Leaderboard for completed games ────────────────
CREATE TABLE IF NOT EXISTS leaderboard (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    player_name   VARCHAR(100)  NOT NULL,
    difficulty    VARCHAR(10)   NOT NULL,
    time_taken    INT           NOT NULL,   -- seconds
    mistakes      INT           DEFAULT 0,
    hints_used    INT           DEFAULT 0,
    played_at     DATETIME      DEFAULT NOW()
);

-- Verify setup
SELECT 'Database setup complete!' AS status;
SHOW TABLES;
