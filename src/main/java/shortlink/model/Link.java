package shortlink.model;

import java.time.LocalDateTime;

public class Link {
    private String id;           // Короткий код
    private String originalUrl;  // Настоящий URL
    private int maxHits;         // Лимит переходов
    private int currentHits;     // Текущие переходы
    private LocalDateTime expiryTime; // Когда ссылка исчезнет
    private User owner;          // Кто создал

    public Link(String id, String originalUrl, int maxHits, int ttlHours, User owner) {
        this.id = id;
        this.originalUrl = originalUrl;
        this.maxHits = maxHits;
        this.currentHits = 0;
        this.expiryTime = LocalDateTime.now().plusHours(ttlHours);
        this.owner = owner;
    }

    public void incrementHits() {
        this.currentHits++;
    }

    // Проверка на протухание ссылки
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }

    // Проверка на исчерпание лимитов
    public boolean isLimitReached() {
        return currentHits >= maxHits;
    }

    public String getId() { return id; }
    public String getOriginalUrl() { return originalUrl; }
    public int getMaxHits() { return maxHits; }
    public int getCurrentHits() { return currentHits; }
    public User getOwner() { return owner; }
    public void setMaxHits(int maxHits) { this.maxHits = maxHits; }
}
