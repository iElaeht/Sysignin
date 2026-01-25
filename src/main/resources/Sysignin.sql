DROP DATABASE IF EXISTS Sysignin;
CREATE DATABASE IF NOT EXISTS Sysignin;
USE Sysignin;

-- 1. TABLA PRINCIPAL: Users
CREATE TABLE Users (
    IdUser INT AUTO_INCREMENT PRIMARY KEY,
    UuidUser VARCHAR(36) NOT NULL UNIQUE,
    Username VARCHAR(40) NOT NULL UNIQUE,
    Password VARCHAR(255) DEFAULT NULL,
    Email VARCHAR(80) NOT NULL UNIQUE,
    BackupEmail VARCHAR(80),
    PhoneNumber VARCHAR(20),
    BirthDate DATE,
    Gender ENUM('Male', 'Female', 'Other', 'Not Specified') DEFAULT 'Not Specified',
    
    -- Ubicación y Auditoría (NetUtils)
    RegistrationIp VARCHAR(60) NOT NULL,
    LastIp VARCHAR(60),
    Country VARCHAR(100) DEFAULT 'Unknown',
    City VARCHAR(100) DEFAULT 'Unknown',
    District VARCHAR(100) DEFAULT NULL,
    
    -- Configuración y Social
    Roles ENUM('User', 'Admin', 'Support', 'Moderator') NOT NULL DEFAULT 'User',
    SocialId VARCHAR(255) DEFAULT NULL,
    AuthProvider VARCHAR(50) DEFAULT NULL,
    ProfilePicture VARCHAR(500) DEFAULT NULL,
    PreferredTheme VARCHAR(20) DEFAULT 'light',
    Languages VARCHAR(5) DEFAULT 'es',
    
    -- Seguridad y Estado
    TwoFactorEnabled BOOLEAN DEFAULT FALSE,
    IsDeleted BOOLEAN DEFAULT FALSE,
    State ENUM('Active', 'Inactive', 'Ban', 'Suspicious') NOT NULL DEFAULT 'Inactive',
    
    -- Tokens y Tiempos
    Token VARCHAR(9) DEFAULT NULL,
    TokenExpiration DATETIME DEFAULT NULL,
    TokenAttempts INT DEFAULT 0,
    DateRegistration DATETIME DEFAULT CURRENT_TIMESTAMP,
    LastLogin DATETIME DEFAULT NULL,
    PenaltyTime DATETIME DEFAULT NULL,
    
    UNIQUE INDEX idx_Social_Auth (SocialId, AuthProvider)
) ENGINE=INNODB;

-- 2. TABLA: UserSessions (Control de sesiones activas)
CREATE TABLE UserSessions (
    IdSession INT AUTO_INCREMENT PRIMARY KEY,
    UserUuid VARCHAR(36) NOT NULL,
    DeviceInfo VARCHAR(255),
    DeviceType ENUM('Desktop', 'Mobile', 'Tablet', 'Unknown') DEFAULT 'Unknown',
    IpAddress VARCHAR(60),
    Country VARCHAR(100),
    City VARCHAR(100),
    IsTrusted BOOLEAN DEFAULT FALSE,
    LoginTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    LastActivity DATETIME DEFAULT CURRENT_TIMESTAMP,
    IsActive BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (UserUuid) REFERENCES Users(UuidUser) ON DELETE CASCADE
) ENGINE=INNODB;

-- 3. TABLA: Notifications (Sistema de alertas escalable)
CREATE TABLE Notifications (
    IdNotification INT AUTO_INCREMENT PRIMARY KEY,
    UserUuid VARCHAR(36) NOT NULL,
    Type ENUM('Security', 'System', 'Social', 'Alert') DEFAULT 'System',
    Title VARCHAR(100) NOT NULL,
    Message TEXT NOT NULL,
    IsRead BOOLEAN DEFAULT FALSE,
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (UserUuid) REFERENCES Users(UuidUser) ON DELETE CASCADE
) ENGINE=INNODB;

-- 4. TABLA: AuditLogs (Historial de acciones críticas)
CREATE TABLE AuditLogs (
    IdLog INT AUTO_INCREMENT PRIMARY KEY,
    IdUser INT,
    UserIdentifier VARCHAR(80),
    Action VARCHAR(100) NOT NULL,
    IpSource VARCHAR(60),
    UserAgent VARCHAR(255),
    Details TEXT,
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (IdUser) REFERENCES Users(IdUser) ON DELETE SET NULL
) ENGINE=INNODB;