package fileio;

import java.util.ArrayList;

public final class CardInput {
    private int mana;
    private int attackDamage;
    private int health;
    private String description;
    private ArrayList<String> colors;
    private String name;

    private boolean isFrozen;

    private boolean hasAttacked;

    public boolean isFrozen() {
        return isFrozen;
    }

    public void setFrozen(final boolean isFrozen) {
        this.isFrozen = isFrozen;
    }

    public boolean hasAttacked() {
        return hasAttacked;
    }

    public void setHasAttacked(final boolean hasAttacked) {
        this.hasAttacked = hasAttacked;
    }

    public CardInput() {
    }

    public CardInput(final int mana, final int attackDamage, final int health,
                     final String description, final ArrayList<String> colors,
                     final String name, final boolean isFrozen, final boolean hasAttacked) {
        this.mana = mana;
        this.attackDamage = attackDamage;
        this.health = health;
        this.description = description;
        this.colors = colors;
        this.name = name;
        this.isFrozen = isFrozen;
        this.hasAttacked = hasAttacked;
    }

    public CardInput(final CardInput cardInput) {
        this(cardInput.getMana(), cardInput.getAttackDamage(), cardInput.getHealth(),
                cardInput.getDescription(), cardInput.getColors(), cardInput.getName(),
                cardInput.isFrozen(), cardInput.hasAttacked());
    }

    public int getMana() {
        return mana;
    }

    public void setMana(final int mana) {
        this.mana = mana;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public void setAttackDamage(final int attackDamage) {
        this.attackDamage = attackDamage;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(final int health) {
        this.health = health;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public ArrayList<String> getColors() {
        return colors;
    }

    public void setColors(final ArrayList<String> colors) {
        this.colors = colors;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return "CardInput{"
                +  "mana="
                + mana
                +  ", attackDamage="
                + attackDamage
                + ", health="
                + health
                +  ", description='"
                + description
                + '\''
                + ", colors="
                + colors
                + ", name='"
                +  ""
                + name
                + '\''
                + '}';
    }
}
