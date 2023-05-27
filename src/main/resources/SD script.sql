-- MySQL Script generated by MySQL Workbench
-- Sat May 27 11:18:37 2023
-- Model: New Model    Version: 1.0
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema libreria
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS libreria;

-- -----------------------------------------------------
-- Schema libreria
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS libreria;
USE libreria;

-- -----------------------------------------------------
-- Table libreria.Libro
-- -----------------------------------------------------
DROP TABLE IF EXISTS libreria.Libro;

CREATE TABLE IF NOT EXISTS libreria.Libro (
  ISBN VARCHAR(42) NOT NULL,
  Existencias INT NOT NULL,
  PRIMARY KEY (ISBN))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table libreria.Prestamo
-- -----------------------------------------------------
DROP TABLE IF EXISTS libreria.Prestamo ;

CREATE TABLE IF NOT EXISTS libreria.Prestamo (
  idUsuario VARCHAR(45) NOT NULL,
  Libro_ISBN VARCHAR(45) NOT NULL,
  fechaInicio DATE NOT NULL,
  fechaFin DATE NOT NULL,
  vigente TINYINT NOT NULL,
  sede INT NOT NULL,
  INDEX fk_Prestamo_Libro_idx (Libro_ISBN ASC) VISIBLE,
  PRIMARY KEY (idUsuario, Libro_ISBN),
  CONSTRAINT fk_Prestamo_Libro
    FOREIGN KEY (Libro_ISBN)
    REFERENCES libreria.Libro (ISBN)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
