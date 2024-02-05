import java.util.Scanner;

class MainMenu
{
    public void showMainMenu()
    {
        var docDelay = new DocDelay();

        while(true)
        {
            System.out.println("""
                Wybierz rodzaj operacji
                1 opóźnienie dokumentów
                0 zakończ
                """);

            var scanner = new Scanner(System.in);
            var type = scanner.nextInt();
            scanner.nextLine();

            switch (type)
            {
                case 1: docDelay.docsDelayMenu(scanner);
                case 2:
                case 0: System.exit(0);
            }
        }
    }
}
