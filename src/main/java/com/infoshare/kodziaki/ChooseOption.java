package com.infoshare.kodziaki;

        import java.io.FileNotFoundException;
        import java.io.FileReader;
        import java.util.InputMismatchException;
        import java.util.List;
        import java.util.Optional;
        import java.util.Scanner;

        import static com.infoshare.kodziaki.Menu.mainMenu;

        import static com.infoshare.kodziaki.Properties.displayProperties;
        import static com.infoshare.kodziaki.ViewPlaceAds.viewPlaceAds;


public class ChooseOption {



    public static void chooseOption() throws FileNotFoundException {

        int option = 0;
        Scanner scannerOption = new Scanner(System.in);
        boolean optionCorrect = false;

        do {
            try {
                option = scannerOption.nextInt();

                if (option >= 0 && option <= 3){
                    optionCorrect = true;
                    break;
                } else {
                    System.out.println("Oj! Zły format. Wybierz 1 lub 2");
                }
            } catch (InputMismatchException e) {
                System.out.println("Oj! Zły format. Wybierz 1 lub 2");
                scannerOption.nextLine();
            }
        } while (!optionCorrect);

        switch (option) {
            case 1:
                List<Place> adsList = CsvReader.readFile(new FileReader(Properties.getAdsFilePath()));
                GetUserPreferences getUserPreferences = new GetUserPreferences();
                UserPreferences userPreferences = getUserPreferences.getUserPreferences(adsList);

                FilterRepositoryByPreferences filterRepository = new FilterRepositoryByPreferences();
                Optional<List<Place>> filteredAdsList = filterRepository.filterRepositoryByPreferences(adsList, userPreferences);

                if (filteredAdsList.isPresent()) {
                    viewPlaceAds(filteredAdsList.get());
                } else {
                    System.out.println("Niestety nie znaleźliśmy ogłoszeń o podanych parametrach :(");
                }
                mainMenu();
                chooseOption();
                break;
            case 2:
                AddingAnnouncement newAd = new AddingAnnouncement(Properties.getAdsFilePath());
                newAd.adding();
                mainMenu();
                chooseOption();
                break;
            case 3:
                displayProperties();
                mainMenu();
                chooseOption();
                break;
            case 0:
                System.out.println("PAPArtments!");
                break;
        }
    }
}