package game;

import env3d.advanced.EnvNode;

/**
 * Modélise une lettre pour l'environnement (valeur, texture, échelle, position..)
 * @author Damien TORNAMBE
 * @author Levi CORMIER
 * 
 */
public class Letter extends EnvNode {

    private char letter;
    /**
     * Modélise une lettre pour l'environnement (valeur, texture, échelle, position..)
     * @param letter une lettre du mot que l'on souhaite mettre dans l'environnement 
     * @param x correspond à la coordonnée x
     * @param y correspond à la coordonnée y
     */
    public Letter(char letter, double x, double y) {
        this.letter = letter;
        setScale(4.0);
        setX(x);
        setY(getScale());
        setZ(y);
        if (letter == ' ') {
            setModel("models/letter/cube.obj");
            setTexture("models/letter/cube.png");
        } else {
            setTexture("models/letter/" + letter + ".png");
            setModel("models/letter/cube.obj");
        }
    }
}
