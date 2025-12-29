package shortlink.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shortlink.model.Link;
import shortlink.model.User;
import shortlink.util.Config;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LinkServiceTest {

    private LinkService service;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Каждый раз создаем чистый сервис для тестов
        service = new LinkService(new Config());
        testUser = new User("test");
    }

    @Test
    void testCreateLink() {
        Link link = service.createLink("https://google.com", 5, testUser);
        assertNotNull(link.getId(), "ID ссылки должен создаться");
        assertEquals("https://google.com", link.getOriginalUrl());
    }

    @Test
    void testUniqueIdsForSameUrl() {
        // Если один человек дважды сокращает одну ссылку - ID должны быть разные
        Link l1 = service.createLink("https://ya.ru", 5, testUser);
        Link l2 = service.createLink("https://ya.ru", 5, testUser);
        assertNotEquals(l1.getId(), l2.getId());
    }

    @Test
    void testInvalidUrlThrowsError() {
        try {
            service.createLink("просто текст", 5, testUser);
            fail("Программа должна была выдать ошибку на плохой URL");
        } catch (IllegalArgumentException e) {
            // Если мы тут - тест пройден
            assertTrue(e.getMessage().contains("http"));
        }
    }

    @Test
    void testUseLinkIncrementsHits() {
        Link link = service.createLink("https://ya.ru", 10, testUser);
        service.useLink(link.getId());
        assertEquals(1, link.getCurrentHits(), "Счетчик кликов должен вырасти");
    }

    @Test
    @SuppressWarnings("unused")
    void testLimitBlocking() {
        Link link = service.createLink("https://ya.ru", 1, testUser);
        service.useLink(link.getId()); // Первый раз можно

        try {
            service.useLink(link.getId()); // Второй раз - лимит!
            fail("Должна быть ошибка лимита");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("лимит"));
        }
    }

    @Test
    void testFindUserLinks() {
        service.createLink("https://site1.com", 5, testUser);
        service.createLink("https://site2.com", 5, testUser);

        List<Link> links = service.findLinksByUser(testUser);
        assertEquals(2, links.size());
    }

    @Test
    void testUserIsolation() {
        service.createLink("https://my-site.com", 5, testUser);

        User otherUser = new User("other");
        List<Link> otherLinks = service.findLinksByUser(otherUser);

        assertEquals(0, otherLinks.size(), "Чужой юзер не должен видеть мои ссылки");
    }

    @Test
    void testRemoveLinkSuccess() {
        Link link = service.createLink("https://to-delete.com", 5, testUser);
        boolean deleted = service.removeLink(link.getId(), testUser);

        assertTrue(deleted);
        // Проверяем, что ссылки больше нет
        try {
            service.useLink(link.getId());
            fail("Ссылка должна быть удалена из базы");
        } catch (RuntimeException e) {

        }
    }

    @Test
    void testRemoveLinkForeignFail() {
        Link link = service.createLink("https://my.com", 5, testUser);
        User hacker = new User("hacker");

        boolean deleted = service.removeLink(link.getId(), hacker);
        assertFalse(deleted, "Хакер не может удалить чужую ссылку");
    }

    @Test
    void testUpdateLimitByOwner() {
        Link link = service.createLink("https://test.com", 5, testUser);
        service.updateLimit(link.getId(), 100, testUser);
        assertEquals(100, link.getMaxHits());
    }

    @Test
    void testUpdateLimitByHacker() {
        Link link = service.createLink("https://test.com", 5, testUser);
        User hacker = new User("hacker");

        try {
            service.updateLimit(link.getId(), 100, hacker);
            fail("Должна быть ошибка прав доступа");
        } catch (RuntimeException e) {
            assertEquals(5, link.getMaxHits(), "Лимит не должен был измениться");
        }
    }

    @Test
    void testShortIdLength() {
        Link link = service.createLink("https://github.com", 5, testUser);
        assertEquals(6, link.getId().length(), "ID ссылки должен быть 6 символов");
    }

    @Test
    void testUserIdentity() {
        User u1 = new User("1234");
        User u2 = new User("1234");
        assertEquals(u1, u2, "Пользователи с одинаковым ID должны считаться одним и тем же объектом");
    }

    @Test
    void testConfigDefaultValues() {
        Config config = new Config();
        // Проверяем, что если ключа нет, возвращается дефолт
        assertEquals(99, config.getInt("fake.key", 99));
    }

    @Test
    void testLinkNotExpiredAtStart() {
        Link link = service.createLink("https://test.com", 5, testUser);
        assertFalse(link.isExpired(), "Новая ссылка не должна быть сразу просрочена");
    }
}
