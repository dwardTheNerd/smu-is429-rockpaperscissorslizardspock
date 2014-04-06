CREATE DATABASE IF NOT EXISTS is429;

USE is429;

CREATE TABLE IF NOT EXISTS bot (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(20) NOT NULL,
  code TEXT NOT NULL,
  language ENUM('JAVASCRIPT', 'PYTHON', 'RUBY') NOT NULL,
  hasWon TINYINT(1) NOT NULL,
  level INT UNSIGNED NULL,
  PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS game_session (
  id VARCHAR(36) NOT NULL,
  playerBotId INT NOT NULL,
  aiBotId INT NOT NULL,
  roundNo TINYINT(2) NOT NULL,
  playerMove ENUM('ROCK', 'PAPER', 'SCISSORS', 'LIZARD', 'SPOCK') NOT NULL,
  aiMove ENUM('ROCK', 'PAPER', 'SCISSORS', 'LIZARD', 'SPOCK') NOT NULL,
  hasPlayerWon TINYINT(1) NOT NULL,
  PRIMARY KEY(id, roundNo),
  FOREIGN KEY(playerBotId) REFERENCES bot(id),
  FOREIGN KEY(aiBotId) REFERENCES bot(id)
);

INSERT INTO bot(name, code, language, hasWon) VALUES('Stupid Bot', 'def play_game():\\n return \'ROCK\'', 'PYTHON', 1);