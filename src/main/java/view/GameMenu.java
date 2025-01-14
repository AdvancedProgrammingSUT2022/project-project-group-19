package view;

import controller.Controller;
import model.*;
import model.building.Building;
import model.civilizations.City;
import model.civilizations.Civilization;
import model.land.Tile;
import model.resource.ResourceType;
import model.technology.Technology;
import model.unit.Unit;
import model.unit.UnitType;

import java.util.*;

public class GameMenu extends Menu {
    private final HashMap<String, Function> functions = new HashMap<>();
    private Tile selectedTile = null;
    private SelectedType selectedType = null;
    private Message message = Message.OK;
    private final Player player;
    private int nextTurnCounter = 0;

    public GameMenu(Player player) {
        this.player = player;
        String color = (player.getCivilization().equals(Database.getPlayers().get(0).getCivilization())) ? Color.BLUE : Color.RED;
        System.out.println(color + "Turn: " + player.getCivilization().getTurnCounter() + " - " + player.getUser().getNickname() + Color.RESET);
        System.out.println("select a tile first. for additional help information enter: 'help'");
        this.functions.putAll(basicFunctions);
        this.functions.put("^help$", this::help);
        this.functions.put("^info research$", this::infoResearch);
        this.functions.put("^info units$", this::infoUnits);
        this.functions.put("^info cities$", this::infoCities);
        this.functions.put("^info diplomacy$", this::infoDiplomacy);
        this.functions.put("^info victory$", this::infoVictory);
        this.functions.put("^info demographics$", this::infoDemographics);
        this.functions.put("^(info notifications|history)$", this::infoNotifications);
        this.functions.put("^info military$", this::infoMilitary);
        this.functions.put("^info economic$", this::infoEconomic);
        this.functions.put("^info diplomatic$", this::infoDiplomatic);
        this.functions.put("^info deals$", this::infoDeals);

        this.functions.put("^select tile (?<xPosition>[-]?\\d+) (?<yPosition>[-]?\\d+)$", this::selectTile);
        this.functions.put("^unselect$", () -> message = Message.OK);
        this.functions.put("^unit move to (?<xPosition>[-]?\\d+) (?<yPosition>[-]?\\d+)$", this::unitMove);
        this.functions.put("^unit free move to (?<xPosition>[-]?\\d+) (?<yPosition>[-]?\\d+)$", this::unitFreeMove);


        this.functions.put("^unit sleep$", this::unitSleep);
        this.functions.put("^unit wake$", this::unitWake);
        this.functions.put("^unit alert$", this::unitAlert);
        this.functions.put("^unit fortify$", this::unitFortify);
        this.functions.put("^unit fortify heal$", this::unitFortifyHeal);
        this.functions.put("^unit garrison$", this::unitGarrison);
        this.functions.put("^unit setup$", this::unitSetup);
        this.functions.put("^unit attack$", this::unitAttack);
        this.functions.put("^unit found$", this::unitFound);
        this.functions.put("^unit cancel$", this::unitCancel);

        this.functions.put("^unit delete$", this::unitDelete);
        this.functions.put("^unit build road$", this::unitBuildRoad);
        this.functions.put("^unit build railroad$", this::unitBuildRailroad);
        this.functions.put("^unit remove route$", this::unitRemoveRoute);

        this.functions.put("^next turn$", this::nextTurn);
        this.functions.put("^next turn --force$", () -> message = Message.NEXT_TURN);
        this.functions.put("^next turn (?<number>\\d+)$", this::nextTurnWithNumber);

        this.functions.put("^unit build (?<improvement>farm|mine|trading post|lumber mill|pasture|camp|plantation|quarry)$", this::unitBuildImprovement);

        this.functions.put("^unit remove feature$", this::removeFeature);
        this.functions.put("^unit repair$", this::unitRepair);

        this.functions.put("^map show (?<xPosition>[-]?\\d+) (?<yPosition>[-]?\\d+)$", this::mapShowPosition);
        this.functions.put("^map show (?<cityName>.+)$", this::mapShowName);
        this.functions.put("^map move right (?<NumberOfMoves>\\d+)$", this::moveRight);
        this.functions.put("^map move left (?<NumberOfMoves>\\d+)$", this::moveLeft);
        this.functions.put("^map move up (?<NumberOfMoves>\\d+)$", this::moveUp);
        this.functions.put("^map move down (?<NumberOfMoves>\\d+)$", this::moveDown);
        this.functions.put("^increase gold (?<NumberOfGolds>\\d+)$", this::increaseGold);
        this.functions.put("^kill all other units$", this::killEnemyUnits);
        this.functions.put("^increase move point (?<amount>\\d+)$", this::increaseMovePoint);
        this.functions.put("^get all techs$", this::getAllTechs);
        this.functions.put("^increase happiness$", this::increaseHappiness);
        this.functions.put("^increase cup of science (?<amount>\\d+)$", this::increaseCupOfScience);
        this.functions.put("^make the world visible$", this::removeFogsOfWar);
    }

    private void removeFogsOfWar() {

        int[][] fogsMap = player.fogOfWar;
        int length = Database.numOfRows;
        int width = Database.numOfCols;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                fogsMap[i][j] = 1;
            }
        }
    }

    private void increaseCupOfScience() {
        int amount = Integer.parseInt(matcher.group("amount"));
        player.getCivilization().addCupOfScience(amount);
    }

    private void increaseHappiness() {
        player.getCivilization().setHappinessIndex(10);
    }

    private void getAllTechs() {
        player.getCivilization().setReachedTechs(Technology.VALUES);
    }

    private void unitFreeMove() {
        if (notSelectedTile())
            return;
        int x = Integer.parseInt(matcher.group("xPosition"));
        int y = Integer.parseInt(matcher.group("yPosition"));
        if (Controller.isInvalidCoordinate(x, y))
            System.out.println("Please enter a valid coordinate.");
        else {
            message = Message.NULL;
            switch (selectedType) {
                case CITY:
                    System.out.println("You can not move a city.");
                    message = Message.OK;
                    break;
                case CIVILIAN_UNIT:
                    message = selectedTile.getCivilianUnit().freeMove(x, y);
                    break;
                case MILITARY_UNIT:
                    message = selectedTile.getMilitaryUnit().freeMove(x, y);
                    break;
            }
            System.out.println(message.getErrorMessage());
        }
    }

    private void increaseGold() {
        int goldsNum = Integer.parseInt(matcher.group("NumberOfGolds"));
        player.getCivilization().increaseGold(goldsNum);
    }

    private void killEnemyUnits() {
        for (Player player1 : Database.getPlayers()) {
            if (player1.equals(player)) continue;
            Civilization civilization = player1.getCivilization();
            for (Unit unit : civilization.getUnits()) {
                if (unit.getPower() == 0) {
                    unit.getTile().setCivilianUnit(null);
                } else {
                    unit.getTile().setMilitaryUnit(null);
                }
                civilization.getUnits().remove(unit);
            }
        }
    }

    private void increaseMovePoint() {
        int amount = Integer.parseInt(matcher.group("amount"));
        for (Unit unit : player.getCivilization().getUnits()) {
            unit.setRemainMP(unit.getRemainMP() + amount);
        }
    }


    private void nextTurn() {
        if (Controller.aUnitNeedsOrder(player)) {
            System.out.println("Your units needs order.");
            System.out.println("for additional information enter: 'info units'");
            System.out.println("for ignore all units enter: 'next turn --force'");
            message = Message.invalidCommand;
        } else
            message = Message.NEXT_TURN;
    }

    private void nextTurnWithNumber() {
        nextTurnCounter = 2 * Integer.parseInt(matcher.group("number")) - 1;
        message = Message.NEXT_TURN;
    }

    private void unitRemoveRoute() {
        if (notSelectedTile())
            return;
        Unit worker = selectedTile.getCivilianUnit();
        if (worker == null || !worker.getType().equals(UnitType.WORKER))
            System.out.println("No worker in this tile");
        else {
            worker.destroyRoad();
            System.out.println("Route removed successfully");
        }
    }

    private void unitBuildImprovement() {
        if (notSelectedTile())
            return;
        message = Message.NULL;
        String improvement = matcher.group("improvement");
        Unit worker = selectedTile.getCivilianUnit();
        if (worker == null || !worker.getType().equals(UnitType.WORKER))
            System.out.println("No worker in this tile");
        else {
            switch (improvement) {
                case "farm":
                    message = worker.buildImprovement(Improvement.FARM);
                    break;
                case "mine":
                    message = worker.buildImprovement(Improvement.MINE);
                    break;
                case "trading post":
                    message = worker.buildImprovement(Improvement.TRADING_POST);
                    break;
                case "lumber mill":
                    message = worker.buildImprovement(Improvement.LUMBER_MILL);
                    break;
                case "pasture":
                    message = worker.buildImprovement(Improvement.PASTURE);
                    break;
                case "camp":
                    message = worker.buildImprovement(Improvement.CAMP);
                    break;
                case "plantation":
                    message = worker.buildImprovement(Improvement.FARMING);
                    break;
                case "quarry":
                    message = worker.buildImprovement(Improvement.STONE_MINE);
                    break;
            }
            System.out.println(message.getErrorMessage());
        }
    }

    private void help() {
        System.out.println("=======================HELP======================");
        System.out.println("Select a tile by it's coordination's. then select units or cities.");
        System.out.println("You can enter these commands:");
        System.out.println("=======================HELP======================");
        for (String string : functions.keySet()) {
            StringBuilder stringBuilder = new StringBuilder(string);
            stringBuilder.deleteCharAt(0);  //remove '^'
            stringBuilder.deleteCharAt(stringBuilder.length() - 1); //remove '$'
            System.out.println(stringBuilder);
        }
    }

    public Message runWithMessage() {
        getCommandOnce(functions);
        return message;
    }

    @Override
    protected void gotoMenu() {
        String nextMenuName = matcher.group("menuName");
        if (nextMenuName.equals("main menu"))
            loopFlag = false;
        else
            System.out.println("menu navigation is not possible");
    }

    private void selectTile() {
        int x = Integer.parseInt(matcher.group("xPosition"));
        int y = Integer.parseInt(matcher.group("yPosition"));
        if (Controller.isInvalidCoordinate(x, y)) { //invalid entered positions
            System.out.println("Invalid position. Please enter a valid X and Y");
            return;
        }
        selectedTile = Database.map[x][y];
        System.out.println("Tile selected successfully.");
    }

    private void infoResearch() {
        if (player.getCivilization().getCities().size() == 0) {
            System.out.println("Please found your first city to access this menu.");
            return;
        }
        System.out.println("========================================");
        System.out.println("============ Research info =============");
        System.out.println("========================================");
        System.out.println("Your curren Science: " + player.getCivilization().getCupOfScience() + " cups");
        System.out.println("Current Technology in research: " + player.getCivilization().getInStudyTech());
        System.out.println();
        System.out.println("1- Print technology tree.");
        System.out.println("2- Select a technology for research");
        System.out.println("3- print Reached Technologies of your civilization.");
        System.out.println("4- Research contract with other civilizations click here. [next Phase]");
        System.out.println();
        System.out.println("Enter a number or enter 'exit'.");
        while (true) {
            String input = scanner.nextLine();
            int number;
            try {
                number = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                if (input.equals("exit"))
                    break;
                System.out.println("Please enter a number or enter 'exit'.");
                continue;
            }
            if (number < 1 || number > 4) {
                System.out.println("Please enter a valid number.");
                continue;
            }
            switch (number) {
                case 1:
                    System.out.println("All technologies in the game:");
                    System.out.println("Technology name         cost     have       Prerequisites");
                    for (Technology technology : Technology.values())
                        System.out.format("%-20s   %4d        %s         %s\n", technology, technology.getCost(),
                                (player.getCivilization().getReachedTechs().contains(technology) ? "✓" : " "),
                                Arrays.toString(technology.getPrerequisiteTechs()));
                    break;
                case 2:
                    System.out.println("Last technology reached: " + player.getCivilization().getReachedTechs().get(player.getCivilization().getReachedTechs().size() - 1));
                    System.out.println("These technologies are available. Select a technology to start researching about it.");
                    System.out.println("   Technology name          time(turn)       Buildings                        resource");
                    int index = 1;
                    for (Technology technology : player.getCivilization().getAvailableForStudyTechs()) {
                        System.out.format(index + "- %-20s     %4d              %-30s    %-10s\n", technology, technology.getCost() / player.getCivilization().getCupOfScience() + 2,
                                Arrays.toString(new ArrayList[]{Building.getAllNeeds(technology)}), ResourceType.getRequire(technology));
                        index++;
                    }
                    index--;

                    while (true) {
                        input = scanner.nextLine();
                        try {
                            number = Integer.parseInt(input);
                        } catch (NumberFormatException e) {
                            if (input.equals("exit"))
                                break;
                            System.out.println("Please enter a number or enter 'exit'.");
                            continue;
                        }
                        if (number < 1 || number > index) {
                            System.out.println("Please enter a valid number.");
                            continue;
                        }
                        Technology tech = player.getCivilization().getAvailableForStudyTechs().get(number - 1);
                        player.getCivilization().studyTech(tech, tech.getCost() / player.getCivilization().getCupOfScience() + 2);
                        System.out.println("You selected " + tech + " for researching successfully.");
                        break;
                    }
                    System.out.println("returned to research menu.");
                    break;
                case 3:
                    List<Technology> techs = player.getCivilization().getReachedTechs();
                    System.out.println("You have reached " + techs.size() + " technology.");
                    for (Technology technology : techs)
                        System.out.println(technology);
                    System.out.println("=============");
                    break;
                case 4:
                    System.out.println("This feature is coming in next phase.");
                    break;
            }

        }


    }

    private void infoUnits() {
        int index = 1;
        for (Unit unit : player.getCivilization().getUnits()) {
            System.out.println("Unit No. " + index);
            System.out.println("type:              " + unit.getType());
            System.out.println("Position:          " + unit.getTile().getPositionI() + " " + unit.getTile().getPositionJ());
            System.out.println("Power/rangedPower: " + unit.getPower() + " / " + unit.getRangedPower());
            System.out.println("Work counter:      " + unit.getWorkCounter());
            System.out.println("Is sleep:          " + unit.isSleep());
            System.out.println("Remained MP:       " + unit.getRemainMP());
            System.out.println("=============================");
            index++;
        }
        index--;
        System.out.println("\nYou can select a unit here by entering it's number or enter 'exit'");
        while (true) {
            String input = "";
            int number;
            try {
                input = scanner.nextLine();
                number = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                if (input.equals("exit"))
                    break;
                System.out.println("Please enter a number.");
                continue;
            }
            if (number < 1 || number > index) {
                System.out.println("Please enter a valid number.");
                continue;
            }
            Unit unit = player.getCivilization().getUnits().get(number - 1);
            selectedTile = unit.getTile();
            selectedType = unit.isMilitary() ? SelectedType.MILITARY_UNIT : SelectedType.CIVILIAN_UNIT;
            break;
        }
        message = Message.NULL;
    }

    private void infoCities() {
        System.out.println("=============================================");
        System.out.println("   Cities info and economic overview menu");
        System.out.println("=============================================");
        System.out.println("Gold:             " + player.getCivilization().getGold());
        System.out.println("Science:          " + player.getCivilization().getCupOfScience());
        System.out.println("Total population: " + player.getCivilization().getPopulation());
        int index = 1;
        for (City city : player.getCivilization().getCities()) {
            System.out.println("City No. " + index);
            if (city.isCapital())
                System.out.println("   [IS CAPITAL]");
            System.out.println("name:            " + city.getName());
            System.out.println("gold:            " + city.getCityIncome());
            System.out.println("position:        " + city.getPositionI() + " / " + city.getPositionJ());
            System.out.println("food output:     " + city.getFood());
            System.out.println("population:      " + city.getPopulation());
            System.out.println("defencive power: " + city.getDefensivePower());
            System.out.println("in production:   " + city.getProduction());
            System.out.println("time remaining:  " + city.getProductionCounter() + " turns");
            System.out.println("=========================");
            index++;
        }
        System.out.println("You can select a city by entering it's number. or enter 'exit'");
        while (true) {
            String input = "";
            int number;
            try {
                input = scanner.nextLine();
                number = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                if (input.equals("exit"))
                    break;
                System.out.println("Please enter a number.");
                continue;
            }
            if (number < 1 || number > index) {
                System.out.println("Please enter a valid number.");
                continue;
            }
            City city = player.getCivilization().getCities().get(number - 1);
            selectedTile = Database.map[city.getPositionI()][city.getPositionJ()];
            selectedType = SelectedType.CITY;
            break;
        }
        message = Message.NULL;
    }

    private void infoDiplomacy() {
        System.out.println("============================");
        System.out.println("      Diplomacy menu");
        System.out.println("============================");
        System.out.println("total gold: " + player.getCivilization().getGold());
        System.out.println("Diplomatic relations will be available in next phase");
    }

    private void infoVictory() {
    }

    private void infoDemographics() {
        System.out.println("=============================");
        System.out.println("      Demographic menu");
        System.out.println("=============================");
        Civilization civil = player.getCivilization();
        System.out.println("Your civilization has " + civil.getCities().size() + " cities.");
        System.out.println("With total population " + civil.getPopulation() + " person.");
        System.out.println("You have " + civil.getGold() + " Gold in total.");
        System.out.println("You have " + civil.getMilitaryUnits().size() + " military units.");
        message = Message.NULL;
    }

    private void infoNotifications() {
        System.out.println("Last 10 notification log are:");
        List<String> logs = player.getCivilization().getNotification();
        for (int i = 0; i < logs.size(); i++) {
            System.out.println(logs.get(logs.size() - 1 - i));
            if (i == 10)
                break;
        }
        System.out.println("==================================");
        message = Message.NULL;
    }

    private void infoMilitary() {
        infoUnits();
    }

    private void infoEconomic() {
    }

    private void infoDiplomatic() {
    }

    private void infoDeals() {
    }

    private void unitMove() {
        if (notSelectedTile())
            return;
        int x = Integer.parseInt(matcher.group("xPosition"));
        int y = Integer.parseInt(matcher.group("yPosition"));
        if (Controller.isInvalidCoordinate(x, y))
            System.out.println("Please enter a valid coordinate.");
        else {
            message = Message.NULL;
            switch (selectedType) {
                case CITY:
                    System.out.println("You can not move a city.");
                    message = Message.OK;
                    break;
                case CIVILIAN_UNIT:
                    message = selectedTile.getCivilianUnit().moveOrder(x, y);
                    break;
                case MILITARY_UNIT:
                    message = selectedTile.getMilitaryUnit().moveOrder(x, y);
                    break;
            }
            System.out.println(message.getErrorMessage());
        }
    }

    private void unitSleep() {
        if (notSelectedTile())
            return;
        switch (selectedType) {
            case CITY:
                System.out.println("You can not sleep a city.");
                break;
            case CIVILIAN_UNIT:
                selectedTile.getCivilianUnit().setSleep(true);
                break;
            case MILITARY_UNIT:
                selectedTile.getMilitaryUnit().setSleep(true);
                break;
        }
        message = Message.OK;
    }

    private void unitWake() {
        if (notSelectedTile())
            return;
        switch (selectedType) {
            case CITY:
                System.out.println("You can not wake a city.");
                break;
            case CIVILIAN_UNIT:
                selectedTile.getCivilianUnit().setSleep(false);
                break;
            case MILITARY_UNIT:
                selectedTile.getMilitaryUnit().setSleep(false);
                break;
        }
        message = Message.OK;
    }

    private void unitAlert() {
        switch (selectedType) {
            case CITY:
                System.out.println("You can not set alert for city.");
                break;
            case CIVILIAN_UNIT:
                System.out.println("You can not set alert for civilian unit.");
                break;
            case MILITARY_UNIT:
                selectedTile.getMilitaryUnit().readyAndAlter();
                break;
        }
        message = Message.OK;
    }

    private void unitFortify() {
        switch (selectedType) {
            case CITY:
                System.out.println("You can not fortify a city.");
                break;
            case CIVILIAN_UNIT:
                System.out.println("You can not fortify civilian unit.");
                break;
            case MILITARY_UNIT:
                selectedTile.getMilitaryUnit().reinforcement();
                break;
        }
        message = Message.OK;
    }

    private void unitFortifyHeal() {
        switch (selectedType) {
            case CITY:
                System.out.println("You can not fortify a city.");
                break;
            case CIVILIAN_UNIT:
                System.out.println("You can not fortify civilian unit.");
                break;
            case MILITARY_UNIT:
                selectedTile.getMilitaryUnit().fullReinforcement();
                break;
        }
        message = Message.OK;
    }

    private void unitGarrison() {
        switch (selectedType) {
            case CITY:
                System.out.println("You can not do it for a city");
                break;
            case CIVILIAN_UNIT:
                System.out.println("You can not do it for civilian unit.");
                break;
            case MILITARY_UNIT:
                selectedTile.getMilitaryUnit().garrison();
                break;
        }
        message = Message.OK;
    }

    private void unitSetup() {
        switch (selectedType) {
            case CITY:
                System.out.println("You can not do it for a city");
                break;
            case CIVILIAN_UNIT:
                System.out.println("You can not do it for civilian unit.");
                break;
            case MILITARY_UNIT:
                selectedTile.getMilitaryUnit().garrison();
                break;
        }
        message = Message.OK;
    }

    private void unitAttack() {
        switch (selectedType) {
            case CITY:
                System.out.println("You can not do it for a city");
                break;
            case CIVILIAN_UNIT:
                System.out.println("You can not do it for civilian unit.");
                break;
            case MILITARY_UNIT:
                selectedTile.getMilitaryUnit().attack();
                break;
        }
        message = Message.OK;
    }

    private void unitFound() { // todo
        switch (selectedType) {
            case CITY:
                System.out.println("You can not do it for a city");
                break;
            case CIVILIAN_UNIT:
                System.out.println("You can not do it for civilian unit.");
                break;
            case MILITARY_UNIT:
                selectedTile.getMilitaryUnit().garrison();
                break;
        }
        message = Message.OK;
    }

    private void unitCancel() {
        switch (selectedType) {
            case CITY:
                System.out.println("You can not do it for a city");
                break;
            case CIVILIAN_UNIT:
                selectedTile.getCivilianUnit().cancel();
                break;
            case MILITARY_UNIT:
                selectedTile.getMilitaryUnit().cancel();
                break;
        }
        message = Message.OK;
    }

    private void unitDelete() {
        if (notSelectedTile())
            return;
        switch (selectedType) {
            case CITY:
                System.out.println("You can not delete a city");
                return;
            case CIVILIAN_UNIT:
                player.getCivilization().addGold(selectedTile.getCivilianUnit().getCost() / 10);
                player.getCivilization().deleteUnit(selectedTile.getCivilianUnit());
                selectedTile.setCivilianUnit(null);
                break;
            case MILITARY_UNIT:
                player.getCivilization().addGold(selectedTile.getMilitaryUnit().getCost() / 10);
                player.getCivilization().deleteUnit(selectedTile.getMilitaryUnit());
                selectedTile.setMilitaryUnit(null);
                break;
        }
        message = Message.OK;
        System.out.println("unit deleted successfully.");
    }

    private void unitBuildRoad() {
        Unit worker = getWorker();
        if (worker == null) return;
        worker.buildRoad();
        message = Message.OK;
    }

    private void unitBuildRailroad() {
        Unit worker = getWorker();
        if (worker == null) return;
        worker.buildRailRoad();
        message = Message.OK;
    }


    //    private void unitBuildFarm() {
//    }
//
//    private void unitBuildMine() {
//    }
//
//    private void unitBuildTradingPost() {
//    }
//
//    private void unitBuildLumberMill() {
//    }
//
//    private void unitBuildPasture() {
//    }
//
//    private void unitBuildCamp() {
//    }
//
//    private void unitBuildPlantation() {
//    }
//
//    private void unitBuildQuarry() {
//    }
    private Unit getWorker() {
        if (selectedTile == null) {
            System.out.println("select a tile first");
            message = Message.invalidCommand;
            return null;
        }
        Unit unit = selectedTile.getCivilianUnit();
        if (unit == null) {
            System.out.println("there is no unit in this tile.");
            message = Message.invalidCommand;
            return null;
        }
        if (!unit.getType().equals(UnitType.WORKER)) {
            System.out.println("there is no worker unit in this tile.");
            message = Message.invalidCommand;
            return null;
        }
        return unit;
    }

    private void removeFeature() {
        if (!selectedType.equals(SelectedType.CIVILIAN_UNIT) || !selectedTile.getCivilianUnit().getType().equals(UnitType.WORKER)) {
            System.out.println("You can only use workers for this operation.");
            return;
        }
        Unit worker = getWorker();
        if (worker == null)
            return;
        message = worker.destroyFeature();
        System.out.println(message);
    }

    private void unitRepair() {
        if (notSelectedTile())
            return;
        Unit worker = selectedTile.getCivilianUnit();
        if (worker == null || !worker.getType().equals(UnitType.WORKER))
            System.out.println("No worker in this tile");
        else
            message = worker.repair();
        System.out.println(message.getErrorMessage());
    }

    private void mapShowPosition() {
    }

    private void mapShowName() {
    }

    private void moveRight() {
    }

    private void moveLeft() {
    }

    private void moveUp() {
    }

    private void moveDown() {
    }

    public Message selectUnitOrCity() {
        HashMap<SelectedType, String> selectableMap = new HashMap<>();
        boolean haveCity = false;
        boolean haveUnit = false;
        if (this.selectedTile.isCityCenter() && player.getCivilization().getCities().contains(this.selectedTile.getCity()))
            selectableMap.put(SelectedType.CITY, "Select City '" + this.selectedTile.getCity().getName() + "'");
        if (this.selectedTile.getMilitaryUnit() != null && player.getCivilization().getUnits().contains(this.selectedTile.getMilitaryUnit()))
            selectableMap.put(SelectedType.MILITARY_UNIT, "Select Military Unit " + this.selectedTile.getMilitaryUnit().getType());
        if (this.selectedTile.getCivilianUnit() != null && player.getCivilization().getUnits().contains(this.selectedTile.getCivilianUnit()))
            selectableMap.put(SelectedType.CIVILIAN_UNIT, "Select Civilian Unit " + this.selectedTile.getCivilianUnit().getType());

        if (selectableMap.size() == 0) {
            System.out.println("There is not any selectable unit in this tile.");
            return Message.noUnit;
        } else if (selectableMap.size() == 1) {
            selectedType = (SelectedType) selectableMap.keySet().toArray()[0];
            System.out.println("The " + selectedType.getName() + " is selected.");
            //debug
            if (!selectedType.equals(SelectedType.CITY)) {
                Unit unit;
                if (selectedType.equals(SelectedType.CIVILIAN_UNIT))
                    unit = selectedTile.getCivilianUnit();
                else
                    unit = selectedTile.getMilitaryUnit();
                System.out.println("type:         " + unit.getType());
                System.out.println("is sleep:     " + unit.isSleep());
                System.out.println("remain MP:    " + unit.getRemainMP());
                System.out.println("work counter: " + unit.getWorkCounter());
            }
            //
            return Message.OK;
        } else {
            System.out.println("Which one do you want to select? Please enter it's number:");
            int index = 1;
            for (String string : selectableMap.values()) {
                System.out.println(index + "- " + string);
                index++;
            }
            index--;
            while (true) {
                int choose;
                try {
                    choose = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a number.");
                    continue;
                }
                if (choose < 1 || choose > index) {
                    System.out.println("Please enter a valid number. your number is out of range.");
                    continue;
                }
                selectedType = (SelectedType) selectableMap.keySet().toArray()[choose - 1];
                System.out.println("You have selected the " + selectedType.getName() + ".");
                switch (selectedType) {
                    case CITY:
                        break;
                    case CIVILIAN_UNIT:
                    case MILITARY_UNIT:
                        Unit unit;
                        if (selectedType.equals(SelectedType.CIVILIAN_UNIT))
                            unit = selectedTile.getCivilianUnit();
                        else
                            unit = selectedTile.getMilitaryUnit();
                        System.out.println("type:         " + unit.getType());
                        System.out.println("is sleep:     " + unit.isSleep());
                        System.out.println("remain MP:    " + unit.getRemainMP());
                        System.out.println("work counter: " + unit.getWorkCounter());
                        break;
                }
                return Message.OK;
            }
        }
    }

    private boolean notSelectedTile() {
        if (selectedTile == null) {
            message = Message.invalidCommand;
            System.out.println("You must select a tile and a city/unit first.");
            return true;
        }
        return false;
    }

    public Tile getSelectedTile() {
        return selectedTile;
    }

    public SelectedType getSelectedType() {
        return selectedType;
    }

    public void setSelectedTile(Tile selectedTile) {
        this.selectedTile = selectedTile;
    }

    public void setSelectedType(SelectedType selectedType) {
        this.selectedType = selectedType;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public int getNextTurnCounter() {
        return nextTurnCounter;
    }

    public void setNextTurnCounter(int nextTurnCounter) {
        this.nextTurnCounter = nextTurnCounter;
    }
}


