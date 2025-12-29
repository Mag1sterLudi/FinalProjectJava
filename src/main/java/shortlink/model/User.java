package shortlink.model;

import java.util.UUID;

public class User {
    // Храним ID как строку, чтобы можно было легко вводить четырёхзначный формат
    private final String id;

    // Конструктор для нового пользователя (генерирует ID)
    public User() {
        // Берем случайный UUID, превращаем в строку и отрезаем первые 4 символа
        this.id = UUID.randomUUID().toString().substring(0, 4);
    }

    // Конструктор для ВХОДА (используем введенный ID)
    public User(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    // Переопределяем equals и hashCode,
    // чтобы LinkService понимал, что User("a1b2") — это тот же пользователь, что и владелец ссылки
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
