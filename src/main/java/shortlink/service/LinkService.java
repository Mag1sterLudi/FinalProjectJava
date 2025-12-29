package shortlink.service;

import shortlink.model.Link;
import shortlink.model.User;
import shortlink.util.Config;

import java.awt.Desktop;
import java.net.URI;
import java.util.*;

public class LinkService {
    // Хранилище ссылок (ID - Объект ссылки)
    private final Map<String, Link> storage = new HashMap<>();
    private final Config config;

    public LinkService(Config config) {
        this.config = config;
    }

    public Link createLink(String longUrl, int limit, User user) {
        // Самая простая проверка URL
        if (!longUrl.startsWith("http")) {
            throw new IllegalArgumentException("Ошибка: Ссылка должна начинаться с http или https");
        }

        // Генерируем короткий ID (просто берем кусок UUID)
        String shortId = UUID.randomUUID().toString().substring(0, 6);

        // Берем время жизни из конфига
        int ttl = config.getInt("link.ttl.hours", 24);

        Link newLink = new Link(shortId, longUrl, limit, ttl, user);
        storage.put(shortId, newLink);

        return newLink;
    }

    public Link useLink(String id) {
        Link link = storage.get(id);

        if (link == null) {
            throw new RuntimeException("Такой ссылки не существует!");
        }

        // Проверка по времени (удаляем если протухла)
        if (link.isExpired()) {
            storage.remove(id);
            throw new RuntimeException("Время жизни ссылки вышло, она удалена.");
        }

        // Проверка по лимиту
        if (link.isLimitReached()) {
            throw new RuntimeException("По этой ссылке больше нельзя переходить (лимит исчерпан).");
        }

        // Если всё ок - считаем переход
        link.incrementHits();

        // Открываем браузер
        try {
            Desktop.getDesktop().browse(new URI(link.getOriginalUrl()));
        } catch (Exception e) {
            System.out.println("Не удалось открыть браузер, но переход засчитан.");
        }

        return link;
    }

    // Поиск ссылок конкретного пользователя через цикл
    public List<Link> findLinksByUser(User user) {
        List<Link> result = new ArrayList<>();
        for (Link l : storage.values()) {
            if (l.getOwner().equals(user)) {
                result.add(l);
            }
        }
        return result;
    }

    public boolean removeLink(String id, User user) {
        Link link = storage.get(id);
        if (link != null && link.getOwner().equals(user)) {
            storage.remove(id);
            return true;
        }
        return false;
    }

    public void updateLimit(String id, int newLimit, User user) {
        Link link = storage.get(id);
        if (link != null && link.getOwner().equals(user)) {
            link.setMaxHits(newLimit);
        } else {
            throw new RuntimeException("У вас нет прав или ссылка не найдена.");
        }
    }
}
