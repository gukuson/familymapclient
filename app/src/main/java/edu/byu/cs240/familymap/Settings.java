package edu.byu.cs240.familymap;

public class Settings {
    private boolean lifeStoryLines;
    private boolean familyTreeLines;
    private boolean spouseLines;
    private boolean fatherSide;
    private boolean motherSide;
    private boolean maleEvents;
    private boolean femaleEvents;

    public Settings(boolean lifeStoryLines, boolean familyTreeLines, boolean spouseLines, boolean fatherSide, boolean motherSide, boolean maleEvents, boolean femaleEvents) {
        this.lifeStoryLines = lifeStoryLines;
        this.familyTreeLines = familyTreeLines;
        this.spouseLines = spouseLines;
        this.fatherSide = fatherSide;
        this.motherSide = motherSide;
        this.maleEvents = maleEvents;
        this.femaleEvents = femaleEvents;
    }

    public boolean isLifeStoryLines() {
        return lifeStoryLines;
    }

    public void setLifeStoryLines(boolean lifeStoryLines) {
        this.lifeStoryLines = lifeStoryLines;
    }

    public boolean isFamilyTreeLines() {
        return familyTreeLines;
    }

    public void setFamilyTreeLines(boolean familyTreeLines) {
        this.familyTreeLines = familyTreeLines;
    }

    public boolean isSpouseLines() {
        return spouseLines;
    }

    public void setSpouseLines(boolean spouseLines) {
        this.spouseLines = spouseLines;
    }

    public boolean isFatherSide() {
        return fatherSide;
    }

    public void setFatherSide(boolean fatherSide) {
        this.fatherSide = fatherSide;
    }

    public boolean isMotherSide() {
        return motherSide;
    }

    public void setMotherSide(boolean motherSide) {
        this.motherSide = motherSide;
    }

    public boolean isMaleEvents() {
        return maleEvents;
    }

    public void setMaleEvents(boolean maleEvents) {
        this.maleEvents = maleEvents;
    }

    public boolean isFemaleEvents() {
        return femaleEvents;
    }

    public void setFemaleEvents(boolean femaleEvents) {
        this.femaleEvents = femaleEvents;
    }
}
