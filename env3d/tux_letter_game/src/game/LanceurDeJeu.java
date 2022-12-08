package game;

/**
 * Classe main ou le jeu Tux Letter Game est exécuté 
 * @author Damien TORNAMBE
 * @author Levi CORMIER
 * 
 */
public class LanceurDeJeu {

    public static void main(String[] args) throws Exception {
        // Declare un Jeu
        JeuDevineLeMotOrdre jeu;
        //Instancie un nouveau jeu
        jeu = new JeuDevineLeMotOrdre();
        //Execute le jeu
        jeu.execute();
    }
}
