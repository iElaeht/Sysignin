DROP DATABASE IF EXISTS Sysignin;
CREATE DATABASE IF NOT EXISTS Sysignin;
USE Sysignin;

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
    Token VARCHAR(20) DEFAULT NULL,
    TokenExpiration DATETIME DEFAULT NULL,
    TokenAttempts INT DEFAULT 0,
    LoginAttempts INT DEFAULT 0,
    DateRegistration DATETIME DEFAULT CURRENT_TIMESTAMP,
    LastLogin DATETIME DEFAULT NULL,
    PenaltyTime DATETIME DEFAULT NULL,
    
    UNIQUE INDEX idx_Social_Auth (SocialId, AuthProvider)
) ENGINE=INNODB;

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

CREATE TABLE SecurityTokens (
    IdToken INT PRIMARY KEY AUTO_INCREMENT,
    UserUuid VARCHAR(36) NOT NULL,
    TokenType ENUM('EMAIL_CHANGE', 'PWD_CHANGE', 'PWD_RECOVERY', 'RECOVERY_UPDATE') NOT NULL,
    TokenCode VARCHAR(20) NOT NULL,
    NewValue VARCHAR(255),
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    ExpiresAt DATETIME NOT NULL,
    IsUsed BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_security_user FOREIGN KEY (UserUuid) REFERENCES Users(UuidUser) ON DELETE CASCADE
);

SELECT * FROM AuditLogs;
SELECT * FROM SecurityTokens;
SELECT * FROM Notifications;
SELECT * FROM UserSessions;
SELECT * FROM Users;