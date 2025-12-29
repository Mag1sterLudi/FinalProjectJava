package shortlink.util;

import java.io.FileInputStream;
import java.util.Properties;

public class Config {
    private Properties properties = new Properties();

    public Config() {
        // Программа будет искать файл application.properties в корне папки проекта
        // (там же, где лежит файл pom.xml)
        try (FileInputStream fis = new FileInputStream("application.properties")) {
            properties.load(fis);
        } catch (Exception e) {
            // Если файла нет или случилась ошибка, просто выведем текст.
            // Программа не закроется, а будет использовать значения "по умолчанию".
            System.out.println("Подсказка: Файл application.properties не найден, использую стандартные лимиты.");
        }
    }

    // Этот метод достает число из файла. Если ключа нет или там текст вместо числа -
    // вернет то, что мы передали в defaultValue.
    public int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            // Если в файле написали, например, "десять" вместо "10"
            return defaultValue;
        }
    }
}
