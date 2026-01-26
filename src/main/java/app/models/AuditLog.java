package app.models;

import java.time.LocalDateTime;

public class AuditLog {
    private int idLog;
    private int idUser;
    private String userIdentifier;
    private String action;
    private String ipSource;
    private String userAgent;
    private String details;
    private LocalDateTime createdAt;

    // 1. Constructor vacío (Esencial para frameworks y listas)
    public AuditLog() {}

    public AuditLog(int idUser, String userIdentifier, String action, String ipSource, String userAgent, String details) {
        this.idUser = idUser;
        this.userIdentifier = userIdentifier;
        this.action = action;
        this.ipSource = ipSource;
        this.userAgent = userAgent;
        this.details = details;
    }

    // --- GETTERS Y SETTERS (Mantén los que ya tenías) ---
    public int getIdLog() { return idLog; }
    public void setIdLog(int idLog) { this.idLog = idLog; }
    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }
    public String getUserIdentifier() { return userIdentifier; }
    public void setUserIdentifier(String userIdentifier) { this.userIdentifier = userIdentifier; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getIpSource() { return ipSource; }
    public void setIpSource(String ipSource) { this.ipSource = ipSource; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}