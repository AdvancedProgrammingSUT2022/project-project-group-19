package model.gameModels.resources.strategicsources;

import model.gameModels.Building;
import model.gameModels.people.MilitaryPersonnel;
import model.gameModels.resources.Source;

import java.util.ArrayList;

public class StrategicSource extends Source {
    private String technologyLevel;
    private ArrayList<MilitaryPersonnel> militaryPersons;
    private ArrayList<Building> buildings;

    public StrategicSource(String name, int food, int production, int gold, String technologyLevel) {
        super(name, food, production, gold);
        this.technologyLevel = technologyLevel;
        this.militaryPersons = new ArrayList<>();
        this.buildings = new ArrayList<>();
    }

    public String getTechnologyLevel() {
        return technologyLevel;
    }

    public ArrayList<MilitaryPersonnel> getMilitaryPersons() {
        return militaryPersons;
    }

    public void setMilitaryPersons(ArrayList<MilitaryPersonnel> militaryPersons) {
        this.militaryPersons = militaryPersons;
    }

    public ArrayList<Building> getBuildings() {
        return buildings;
    }

    public void setBuildings(ArrayList<Building> buildings) {
        this.buildings = buildings;
    }
}
