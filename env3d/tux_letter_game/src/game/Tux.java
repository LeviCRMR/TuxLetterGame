package game;

import env3d.Env;
import env3d.advanced.EnvNode;
import java.util.ArrayList;
import org.lwjgl.input.Keyboard;

/**
 * Gère tout ce qui concerne le personnage du jeu : le pingouin Tux
 * placement dans l'environnement, échelle, collisions
 * @author Damien TORNAMBE
 * @author Levi CORMIER
 * 
 */
public class Tux extends EnvNode {

    private Env env;
    private Room room;
    private double hauteurMaxSaut;
    private final double palierDeplacementHauteur = 1.0;
    private boolean estEnAscension;
    private double hauteurAuSolDeTux; // Obligé de faire ça car la valeur de getY() != getScale*1.1
    
    
    public Tux(Env env, Room room) {
        this.env = env;
        this.room = room;
        setScale(4.0);
        setX(0.0 + this.getScale());// positionnement tout à gauche de la largeur
        setY(getScale() * 1.1); // positionnement en hauteur basé sur la taille de Tux
        setZ(room.getDepth() - this.getScale()); // positionnement de tout en bas (proche de l'utilisateur) de la profondeur
        this.setRotateY(90); // Regarde vers la droite
        setTexture("models/tux/tux_special.png");
        setModel("models/tux/tux.obj");
        estEnAscension = false;
        this.hauteurAuSolDeTux = this.getY();
        this.hauteurMaxSaut = this.getY() + 20.0;
    }

    /**
     * Déplace le personnage Tux en fonction des touches sur lequelles le joueur appuie :
     *  flèche haut / z 
     *  flèche bas / s 
     *  flèche gauche / q 
     *  flèche droite / d 
     *  barre espace : saut 
     * 
     * et vérifie s'il entre en collision avec les murs ou des lettres 
     * pour l'empêcher d'avancer si c'est le cas
     * @param l correspond à la liste des lettres présentes sur le plateau de jeu et avec lesquelles on teste la collision en plus de la pièce
     */
    public void deplace(ArrayList<Letter> l) {

        double taillePersonnage = getScale() + 1.0;

        if (env.getKeyDown(Keyboard.KEY_Z) || env.getKeyDown(Keyboard.KEY_UP)) { // Fleche 'haut' ou Z
            // Haut
            this.setRotateY(180);
            if (!testRoomCollision(this.getX(), this.getZ() - taillePersonnage)) {
                if (!testLetterCollision(this.getX(), this.getZ() - taillePersonnage, l) || this.getY() >= 5.0) {
                    this.setZ(this.getZ() - 1.0);
                }
            }
        }
        if (env.getKeyDown(Keyboard.KEY_Q) || env.getKeyDown(Keyboard.KEY_LEFT)) { // Fleche 'gauche' ou Q
            // Gauche
            this.setRotateY(270);
            if (!testRoomCollision(this.getX() - taillePersonnage, this.getZ())) {
                if (!testLetterCollision(this.getX() - taillePersonnage, this.getZ(), l) || this.getY() >= 5.0) {
                    this.setX(this.getX() - 1.0);
                }
            }
        }
        if (env.getKeyDown(Keyboard.KEY_S) || env.getKeyDown(Keyboard.KEY_DOWN)) { // Fleche 'bas' ou S
            // Bas
            this.setRotateY(0);
            if (!testRoomCollision(this.getX(), this.getZ() + taillePersonnage)) {
                if (!testLetterCollision(this.getX(), this.getZ() + taillePersonnage, l) || this.getY() >= 5.0) {
                    this.setZ(this.getZ() + 1.0);
                }
            }
        }

        if (env.getKeyDown(Keyboard.KEY_D) || env.getKeyDown(Keyboard.KEY_RIGHT)) { // Fleche 'droite' ou D
            // Droite
            this.setRotateY(90);
            if (!testRoomCollision(this.getX() + taillePersonnage, this.getZ())) {
                if (!testLetterCollision(this.getX() + taillePersonnage, this.getZ(), l) || this.getY() >= 5.0) {
                    this.setX(this.getX() + 1.0);
                }
            }
        }

        // Quand on appuie sur espace, si le caractère est au sol on l'élève d'un rang et set la variable boolean qui dit qu'il est en ascension
        if (env.getKeyDown(Keyboard.KEY_SPACE) && this.getY() == this.hauteurAuSolDeTux) {
            this.setY(this.getY() + palierDeplacementHauteur);
            this.estEnAscension = true;
        }

        // Si le personnage n'est pas sur le sol, et qu'il est en ascension et que sa hauteur est < hauteur max
        if ((this.getY() != this.hauteurAuSolDeTux) && estEnAscension() && (this.getY() < this.hauteurMaxSaut)) {
            this.setY(this.getY() + this.palierDeplacementHauteur);
            // Si sa nouvelle hauteur == hauteur max, on passe le boolean ascension à false
            if (this.getY() >= this.hauteurMaxSaut) {
                this.estEnAscension = false;
            }
        }

        boolean auDessusLettre = false;
        // Si le personne à atteint/dépacer la hauteur max, on le fait redescendre 
        if ((this.getY() > this.hauteurAuSolDeTux) && !estEnAscension()) {
            auDessusLettre = false;
            // si il se trouve au dessus d'une lettre on ne veut pas le faire redescendre
            for (Letter let : l) {
                if (this.distance(let) <= let.getScale() + let.getY() + 1) {
                    auDessusLettre = true;
                }
            }
            // sinon on le fait redescendre
            if (!auDessusLettre) {
                this.setY(this.getY() - this.palierDeplacementHauteur);
                if (this.getY() <= this.hauteurAuSolDeTux) {
                    this.setY(this.hauteurAuSolDeTux);
                }
            }
        }
        
        // permet de sauter à nouveau si on est au dessus d'une lettre
        if (auDessusLettre && env.getKeyDown(Keyboard.KEY_SPACE)) {
            this.setY(this.getY() + palierDeplacementHauteur);
            this.estEnAscension = true;
        }
    }
  
    /**
     * Indique si Tux est dans les airs ou au sol
     * @return estEnAscension boolean qui vaut true s'il est en l'air, false s'il est au sol
     */
    public boolean estEnAscension() {
        return estEnAscension;
    }    
    
    /**
     * Vérifie si Tux est en collision avec un mur de la pièce
     * @param x correspond aux coordonnées x 
     * @param y correspond aux coordonnées y 
     * @return rentreDansUnMur boolean qui renvoie true s'il y a collision et false sinon
     */
    private boolean testRoomCollision(double x, double y) {
        boolean rentreDansUnMur = x >= room.getWidth() || y >= room.getDepth() || x <= 0 || y <= 0;
        return rentreDansUnMur;
    }

    /**
     * Vérifie si Tux est en collision avec une lettre présente dans l'environnement de jeu
     * @param x correspond aux coordonnées x
     * @param y correspond aux coordonnées y
     * @param lettres correspond à la liste de lettres présentes sur le plateau
     * @return rentreDansUneLettre boolean qui renvoie true s'il y a collision, false sinon
     */
    private boolean testLetterCollision(double x, double y, ArrayList<Letter> lettres) {
        boolean rentreDansUneLettre = false;
        // pour que le tux puisse bouger quand meme dès qu'il entre en collision
        EnvNode tmp = new EnvNode();
        tmp.setX(x);
        tmp.setZ(y);
        tmp.setScale(this.getScale());
        for (Letter l : lettres) {
            double distance = tmp.distance(l) - (tmp.getScale() / 2 + l.getScale() / 2);
            if (distance <= 3.0) {
                rentreDansUneLettre = true;
            }
        }
        return rentreDansUneLettre;
    }

}
