// ===================== INTERFACES =====================
// NAME OF AUTHOR : TATENDA MACHIRORI CS3354
// Flyable Interface
interface Flyable {
    void fly();
    void land();
}

// Swimmable Interface
interface Swimmable {
    void swim();
    void stopSwimming();
}

// Teleportable Interface
interface Teleportable {
    void teleport(int x, int y);
}

// ===================== BASE CLASS =====================

 class GameCharacter {
    private String name;
    private int level;
    private int health;

    public GameCharacter(String name, int level, int health) {
        this.name = name;
        this.level = level;
        this.health = health;
    }

    // Getters
    public String getName()   { return name; }
    public int    getLevel()  { return level; }
    public int    getHealth() { return health; }

    // Setters
    public void setName(String name)    { this.name = name; }
    public void setLevel(int level)     { this.level = level; }
    public void setHealth(int health)   { this.health = health; }

    public void attack() {
        System.out.println(name + " is attacking!");
    }

    public void defend() {
        System.out.println(name + " is defending!");
    }
}

// ===================== DERIVED CLASSES =====================

// Wizard: extends GameCharacter, implements Flyable and Teleportable
class Wizard extends GameCharacter implements Flyable, Teleportable {
    private int mana;

    public Wizard(String name, int level, int health, int mana) {
        super(name, level, health);
        this.mana = mana;
    }

    public int  getMana()       { return mana; }
    public void setMana(int m)  { this.mana = m; }

    public void castSpell() {
        System.out.println(getName() + " is casting a spell!");
    }

    @Override
    public void fly() {
        System.out.println(getName() + " is flying on a broomstick!");
    }

    @Override
    public void land() {
        System.out.println(getName() + " has landed gracefully!");
    }

    @Override
    public void teleport(int x, int y) {
        System.out.println(getName() + " has teleported to (" + x + ", " + y + ")!");
    }
}

// Mermaid: extends GameCharacter, implements Swimmable
class Mermaid extends GameCharacter implements Swimmable {
    private double finLength;

    public Mermaid(String name, int level, int health, double finLength) {
        super(name, level, health);
        this.finLength = finLength;
    }

    public double getFinLength()           { return finLength; }
    public void   setFinLength(double fl)  { this.finLength = fl; }

    public void sing() {
        System.out.println(getName() + " is singing a beautiful melody!");
    }

    @Override
    public void swim() {
        System.out.println(getName() + " is swimming swiftly through the ocean!");
    }

    @Override
    public void stopSwimming() {
        System.out.println(getName() + " has stopped swimming and is floating peacefully!");
    }
}

// Superhero: extends GameCharacter, implements Flyable, Swimmable, Teleportable
class Superhero extends GameCharacter implements Flyable, Swimmable, Teleportable {
    private String superPower;

    public Superhero(String name, int level, int health, String superPower) {
        super(name, level, health);
        this.superPower = superPower;
    }

    public String getSuperPower()          { return superPower; }
    public void   setSuperPower(String sp) { this.superPower = sp; }

    public void saveTheDay() {
        System.out.println(getName() + " is saving the day with the power of " + superPower + "!");
    }

    @Override
    public void fly() {
        System.out.println(getName() + " is soaring through the sky!");
    }

    @Override
    public void land() {
        System.out.println(getName() + " has landed heroically!");
    }

    @Override
    public void swim() {
        System.out.println(getName() + " is swimming at superhuman speed!");
    }

    @Override
    public void stopSwimming() {
        System.out.println(getName() + " has stopped swimming!");
    }

    @Override
    public void teleport(int x, int y) {
        System.out.println(getName() + " has teleported to (" + x + ", " + y + ")!");
    }
}

// ===================== MAIN / TEST CASES =====================

class Main {

    // Test Case 4 helper: accepts any GameCharacter, uses instanceof to test abilities
    static void demonstrateCharacter(GameCharacter character) {
        System.out.println("--- Demonstrating: " + character.getName() + " ---");
        character.attack();
        character.defend();

        if (character instanceof Flyable flyable) {
            flyable.fly();
            flyable.land();
        }
        if (character instanceof Swimmable swimmable) {
            swimmable.swim();
            swimmable.stopSwimming();
        }
        if (character instanceof Teleportable teleportable) {
            teleportable.teleport(10, 20);
        }
        System.out.println();
    }

    public static void main(String[] args) {

        // ---- Test Case 1: Wizard Actions ----
        System.out.println("========== Test Case 1: Wizard Actions ==========");
        Wizard wizard = new Wizard("Merlin", 10, 80, 200);
        wizard.castSpell();
        wizard.attack();
        wizard.fly();
        wizard.teleport(5, 15);
        wizard.land();
        System.out.println();

        // ---- Test Case 2: Mermaid Actions ----
        System.out.println("========== Test Case 2: Mermaid Actions ==========");
        Mermaid mermaid = new Mermaid("Ariel", 7, 90, 1.5);
        mermaid.sing();
        mermaid.swim();
        mermaid.defend();
        mermaid.stopSwimming();
        System.out.println();

        // ---- Test Case 3: Superhero Actions ----
        System.out.println("========== Test Case 3: Superhero Actions ==========");
        Superhero superhero = new Superhero("Superman", 20, 150, "super strength");
        superhero.saveTheDay();
        superhero.attack();
        superhero.fly();
        superhero.teleport(100, 200);
        superhero.land();
        superhero.swim();
        superhero.stopSwimming();
        System.out.println();

        // ---- Test Case 4: Polymorphism and Interface Application ----
        System.out.println("========== Test Case 4: Polymorphism & Interface Application ==========");
        demonstrateCharacter(wizard);
        demonstrateCharacter(mermaid);
        demonstrateCharacter(superhero);
    }
}
