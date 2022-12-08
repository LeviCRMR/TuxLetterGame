package game;


/**
 * Gère l'occupation des cases pour la génération des lettres dans l'environnement de jeu
 * @author Damien TORNAMBE
 * @author Levi CORMIER
 * 
 */
public class Coordonnee {

    private double depth;
    private double width;
    boolean estOccupé;

    public Coordonnee(double width, double depth) {
        this.depth = depth;
        this.width = width;
        estOccupé = false;
    }

    
    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }


    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public boolean estOccupé() {
        return estOccupé;
    }

    public void setEstOccupé(boolean estOccupé) {
        this.estOccupé = estOccupé;
    }

}
