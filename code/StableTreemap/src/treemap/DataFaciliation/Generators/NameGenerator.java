package treemap.DataFaciliation.Generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author max
 */
public class NameGenerator {

    private static final ArrayList<String> herbList = new ArrayList<>(Arrays.asList(
            "Akudjura",
            "Alexanders",
            "Allspice",
            "Angelica",
            "Anise",
            "Anise Hyssop",
            "Aniseed myrtle",
            "Annatto",
            "Apple mint",
            "Artemisia",
            "Asafoetida",
            "Asarabacca",
            "Avens",
            "Avocado leaf",
            "Barberry",
            "Bay leaf",
            "Bee balm",
            "Boldo",
            "Borage",
            "Caper",
            "Caraway",
            "Cardamom",
            "Cassia",
            "Catnip",
            "Cayenne pepper",
            "Celery leaf",
            "Celery seed",
            "Chervil",
            "Chicory",
            "Chili pepper",
            "Chives",
            "Cinnamon myrtle",
            "Clove",
            "Coriander seed",
            "Costmary",
            "Cubeb pepper",
            "Cudweed",
            "Cumin",
            "Curry leaf",
            "Curry plant",
            "Dill herb or weed",
            "Dill seed",
            "Elderflower",
            "Epazote",
            "Fennel",
            "Fenugreek",
            "Galingale",
            "Garlic chives",
            "Ginger",
            "Grains of paradise",
            "Horseradish",
            "Houttuynia cordata",
            "Hyssop",
            "Jasmine flowers",
            "Jiaogulan",
            "Jimbu",
            "Juniper berry",
            "Kala zeera",
            "Kawakawa seeds",
            "Kokam seed",
            "Koseret leaves",
            "Lavender",
            "Lemon balm",
            "Lemon ironbark",
            "Lemon myrtle",
            "Lemon verbena",
            "Lemongrass",
            "Leptotes bicolor",
            "Lesser calamint",
            "Lovage",
            "Mace",
            "Marjoram",
            "Mastic",
            "Mountain horopito",
            "Nutmeg",
            "Olida",
            "Oregano",
            "Orris root",
            "Paprika",
            "Paracress",
            "Parsley",
            "Peppermint",
            "Peppermint gum leaf",
            "Peruvian pepper",
            "Quassia",
            "Rice paddy herb",
            "Rosemary",
            "Rue",
            "Saffron",
            "Sage",
            "Saigon cinnamon",
            "Salad burnet",
            "Salep",
            "Sassafras",
            "Shiso",
            "Sorrel",
            "Spearmint",
            "Spikenard",
            "Star anise",
            "Sumac",
            "Sweet woodruff",
            "Tarragon",
            "Thyme",
            "Turmeri",
            "Vanilla",
            "Wasabi",
            "Watercress",
            "Wattleseed",
            "Wild thyme",
            "Willow herb",
            "Wintergreen",
            "Woodruff",
            "Yarrow",
            "Za'atar",
            "Zedoary"
    ));

    public static String getUniqueName(List<String> names, Random randomizer) {
        ArrayList<String> remainingNames = (ArrayList<String>) herbList.clone();
        remainingNames.removeAll(names);
        if (!remainingNames.isEmpty()) {
            return remainingNames.get(0);
        }

        return generateNewUniqueName(names, randomizer);

    }

    private static String generateNewUniqueName(List<String> names, Random randomizer) {
        //perform pointwise mutation on one of the names in the list
        String nameToChange = herbList.get(randomizer.nextInt(herbList.size() - 2) + 1);
        int length = nameToChange.length();
        while (names.contains(nameToChange)) {
            int index = randomizer.nextInt(length - 1) + 1;
            char randomletter = (char) (randomizer.nextInt(26) + 'a');
            nameToChange = nameToChange.substring(0, index) + randomletter + nameToChange.substring(index + 1);
        }
        return nameToChange;
    }
}
