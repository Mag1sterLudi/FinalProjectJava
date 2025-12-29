package shortlink;

import shortlink.model.Link;
import shortlink.model.User;
import shortlink.service.LinkService;
import shortlink.util.Config;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        LinkService service = new LinkService(new Config());
        Scanner sc = new Scanner(System.in);
        User currentUser = new User();

        System.out.println("--- Привет! Это твой личный сокращатель ссылок ---");
        System.out.println("Твой временный ID: " + currentUser.getId() + " (запомни его для входа)");

        while (true) {
            System.out.println("\nЧто хочешь сделать?");
            System.out.println("1 - Создать короткую ссылку");
            System.out.println("2 - Перейти по ссылке");
            System.out.println("3 - Посмотреть мои ссылки");
            System.out.println("4 - Удалить ссылку");
            System.out.println("5 - Изменить лимит");
            System.out.println("6 - Сменить пользователя");
            System.out.println("0 - Выйти");

            System.out.print("> ");
            String cmd = sc.nextLine();

            try {
                if (cmd.equals("1")) {
                    System.out.print("Вставь длинную ссылку: ");
                    String url = sc.nextLine();
                    System.out.print("Сколько переходов разрешить? ");
                    int lim = Integer.parseInt(sc.nextLine());
                    Link l = service.createLink(url, lim, currentUser);
                    System.out.println("Готово! Твой код: " + l.getId());

                } else if (cmd.equals("2")) {
                    System.out.print("Введи код ссылки: ");
                    String code = sc.nextLine();
                    service.useLink(code);
                    System.out.println("Открываю браузер...");

                } else if (cmd.equals("3")) {
                    var links = service.findLinksByUser(currentUser);
                    if (links.isEmpty()) System.out.println("У тебя пока нет ссылок.");
                    for (Link l : links) {
                        System.out.println(l.getId() + " -> " + l.getOriginalUrl() + " (Клики: " + l.getCurrentHits() + "/" + l.getMaxHits() + ")");
                    }

                } else if (cmd.equals("4")) {
                    System.out.print("Какой код удалить? ");
                    String code = sc.nextLine();
                    if (service.removeLink(code, currentUser)) System.out.println("Удалено.");
                    else System.out.println("Нельзя удалить (ссылка не твоя или её нет).");

                } else if (cmd.equals("5")) {
                    System.out.print("Код ссылки: ");
                    String code = sc.nextLine();
                    System.out.print("Новый лимит: ");
                    int lim = Integer.parseInt(sc.nextLine());
                    service.updateLimit(code, lim, currentUser);
                    System.out.println("Лимит обновлен.");

                } else if (cmd.equals("6")) {
                    System.out.print("Введи свой старый ID или нажми Enter для нового: ");
                    String id = sc.nextLine();
                    currentUser = id.isEmpty() ? new User() : new User(id);
                    System.out.println("Теперь ты зашел как: " + currentUser.getId());

                } else if (cmd.equals("0")) {
                    break;
                }
            } catch (Exception e) {
                System.out.println("Упс, ошибка: " + e.getMessage());
            }
        }
    }
}
