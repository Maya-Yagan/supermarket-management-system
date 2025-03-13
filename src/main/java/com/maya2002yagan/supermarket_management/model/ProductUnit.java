package com.maya2002yagan.supermarket_management.model;

/**
 * Enum representing the units in which products can be measured.
 * 
 * @author Maya Yagan
 */
public enum ProductUnit {
    PIECES("Piece", "pcs"),
    KILOGRAMS("Kilogram", "kg"),
    GRAMS("Gram", "gr"),
    LITERS("Liter", "L");
    
    private final String fullName;
    private final String shortName;

    /**
     * Constructor for ProductUnit.
     * @param fullName the full name of the unit
     * @param shortName the short name of the unit
     */
    private ProductUnit(String fullName, String shortName) {
        this.fullName = fullName;
        this.shortName = shortName;
    }
    
     /**
     * Gets the short name of the unit.
     * @return the short name (e.g. "kg", "gr")
     */
    public String getShortName() {
        return shortName;
    }
    
    /**
     * Gets the full name of the unit
     * @return the full name (e.g. "kilogram", "gram")
     */
    public String getFullName(){
        return fullName;
    }
}
