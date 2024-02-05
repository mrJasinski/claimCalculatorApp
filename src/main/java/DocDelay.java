import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

class DocDelay
{
    public void docsDelayMenu(Scanner scanner)
    {
        var menu = new MainMenu();

        while(true)
        {
            System.out.println("""
                Wybierz rodzaj działania:
                1 nowa kalkulacja
                2 otwórz archiwalną
                0 powrót do menu""");


            var type = scanner.nextInt();
            scanner.nextLine();
//TODO
            switch (type)
            {
                case 1: calculateDelay(scanner);
                case 2:
                case 0: menu.showMainMenu();
            }
        }
    }

    void calculateDelay(Scanner scanner)
    {
        var menu = "Proszę podać kraj obsługujący dokumenty pojazdu:\n";
        for (CarCountry c : CarCountry.values())
            menu += c + " (" + c.getName() + ")" + System.lineSeparator();

        System.out.println(menu);

        var country = CarCountry.valueOf(scanner.nextLine());
//            inicjalizacja na potrzeby testu
        LocalDate docsDeliveredDate;
        LocalDate docsStartDate;
        var isRem = false;
        int carPrice;

        var dateRequestHeadliner = switch (country)
        {
            case SE -> modifyDateRequestHeadliner("przesłania cmr przez klienta");
            case NL -> modifyDateRequestHeadliner("wyrejestrowania auta");
            case IT -> modifyDateRequestHeadliner("wyjścia auta z lokalnego LC");
            case ES, OTHER -> modifyDateRequestHeadliner("opłacenia auta");
            default -> throw new IllegalStateException("Unexpected value: " + country);
        };

        System.out.println(dateRequestHeadliner);
        docsStartDate = LocalDate.parse(scanner.nextLine(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        System.out.println(modifyDateRequestHeadliner("dostarczenia dokumentów"));
        docsDeliveredDate = LocalDate.parse(scanner.nextLine(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
//        TODO jeśli się mieści to przerwanie
        if (checkIfDocsWereDeliveredInTime(docsDeliveredDate, docsStartDate, country, null))
            System.out.println("Dokumenty dostarczone w terminie. Brak rekompensaty.");
        else
        {
            System.out.println("""
                        Czy auto pochodzi z rem?
                        1 tak
                        2 nie""");

            if (scanner.nextInt() == 1)
                isRem = true;

//            TODO jeśli się mieści to przerwanie
            if (checkIfDocsWereDeliveredInTime(docsDeliveredDate, docsStartDate, country, null))
                System.out.println("Dokumenty dostarczone w terminie. Brak rekompensaty.");
            {
                scanner.nextLine();
                System.out.println("Proszę podać cenę auta w euro:");
                carPrice = scanner.nextInt();
                scanner.nextLine();

                var docDelay = new DocDelay();

                System.out.print(docDelay.calculateCompensation(docsStartDate, docsDeliveredDate, isRem, country, carPrice));
                System.out.println();
            }
        }

        System.out.println();
        System.out.println("Czy zapisać kalkulację?");
        System.out.println("1 tak");
        System.out.println("2 nie");

        var save = scanner.nextInt();

        if (save == 1)
            System.out.println("Proszę podać nr wew pod którym ma zostać zapisana kalkulacja: ");

        var internal = scanner.nextLine();




//            TODO
//        pytanie o zapis kalkulacji
//        jeśli tak to pytanie o nr wewnętrzny auta
//        zapis w formacie
//        nr wew
//        data opłacenia/wyrejestrowania
//        typ daty?
//        data dostarczenia
//        data rozpoczęcia liczenia opóźnienia?
//        czyRem
//        cena auta
//        ilość dni/miesięcy opóźnienia
//        próg cenowy?
//        rekompensata
    }

    String modifyDateRequestHeadliner(String type)
    {
        return String.format("Proszę podać datę %s w formacie DD.MM.RRRR: ", type);
    }


    String calculateCompensation(LocalDate docsStartDate, LocalDate docsDeliveredDate, boolean isRem, CarCountry country, int carPrice)
    {
        var delayStartDate = calculateDelayStartDate(docsStartDate, country, isRem);

        var delay = calculateDelayInMonths(calculateDelayWorkingDays(delayStartDate, docsDeliveredDate));

        var comp =  setMonthCompByCarPrice(carPrice);

        var result = roundToFive(delay * comp);

        if (result < 25)
            return "Brak rekompensaty. Potencjalna rekompensata poniżej minimalnego vouchera";


        return "Rekompensata za opoźnienie dokumentów wynosi: " + result;
    }

    boolean checkIfDocsWereDeliveredInTime(LocalDate docsDeliveredDate, LocalDate docsStartDate, CarCountry country, Boolean isRem)
    {
        var delayStartDate = calculateDelayStartDate(docsStartDate, country, isRem);

        return docsDeliveredDate.isBefore(delayStartDate);
//            return ;
    }

    int setMonthCompByCarPrice(int carPrice)
    {
        var comp = 100;

        if (carPrice <= 1000)
            comp = 25;
        else if (carPrice > 1000 && carPrice <= 2000)
            comp = 50;
        else if (carPrice > 2000 && carPrice <= 4000)
            comp = 75;

        return comp;
    }

    int roundToFive(float toRound)
    {
        return 5*(Math.round(toRound/5));
    }

    int calculateDelayWorkingDays(LocalDate startDate, LocalDate docsDeliveredDate)
    {
        var days = 0;

        var actualDate = startDate;

//        counting weekdays between docs delivery date and date when delivery delay starts
//        bank holidays not taken into account due to its minor influence

        while(actualDate.isBefore(docsDeliveredDate) || actualDate.isEqual(docsDeliveredDate))
        {
            if (!((actualDate.getDayOfWeek()).equals(DayOfWeek.SATURDAY)) && !((actualDate.getDayOfWeek()).equals(DayOfWeek.SUNDAY)))
                days++;

            actualDate = actualDate.plusDays(1);
        }

        return days;
    }

    float calculateDelayInMonths(int daysDelay)
    {
//        standard month has 21 weekdays
        return (float)daysDelay / 21;
    }
    //prośba o datę opłacenia/wyrejstrowania/cmr w UI na podstawie kraju obsługującego dokumenty
    LocalDate calculateDelayStartDate(LocalDate date, CarCountry country, Boolean isRem)
    {
        int weeksToAdd;

        switch (country)
        {
            case ES -> weeksToAdd = 5;
            case IT, NL -> weeksToAdd = 2;
            default -> weeksToAdd = 1;
        }

        var startDate = date.plusWeeks(weeksToAdd);

        if (isRem != null && isRem)
            startDate = startDate.plusMonths(1);

        return startDate;
    }
}
