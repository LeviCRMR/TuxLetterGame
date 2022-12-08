package game;

/**
 * Gère le jeu Tux Letter Game spécifiquement :
 * règles, démarrage et fin de partie, nombre de lettres restantes, ...
 * @author Damien TORNAMBE
 * @author Levi CORMIER
 * 
 */
public class JeuDevineLeMotOrdre extends Jeu {

    private int nbLettresRestantes;
    private Chronometre chrono;

    public JeuDevineLeMotOrdre() throws Exception {
        super();
        chrono = new Chronometre(); // Ici on setup le temps limite pour trouver le mot
    }

    /**
     * Gère si le personnage Tux trouve une lettre en rentrant en collision avec elle 
     * S'il reste au moins une lettre dans la liste des lettres à trouver on vérifie s'l rentre en collision avec elle
     * @return boolean true/false correspond à si Tux à trouvé la lettre en question, donc s'il rentre en collision avec elle
     */
    private boolean tuxTrouveLettre() {
        if (!getLettres().isEmpty()) { // Si il reste au moins une lettre dans la liste des lettres à trouvé on vérifie si il rentre en collision
            return super.collision(super.getLettres().get(0));
        } else { 
            System.out.println("Erreur: appel de la méthode tuxTrouveLettre alors qu'il n'y a plus de lettre sur le plateau");
            return false;
        }
    }

    public int getNbLettresRestantes() {
        return nbLettresRestantes;
    }

    public Chronometre getTemps() {
        return chrono;
    }


    
    /**
     * Démarre une partie en commençant le chronomètre, puis récupère le nombre de lettres que le joueur doit trouver
     * 
     * Le chronomètre est initialisé en début de partie (démarrePartie), puis utilisé pour arrêter le jeu au bout d'un temps limité, 
     * si le mot n'est pas entièrement trouvé avant (appliqueRègles). 
     * Si le mot est déterminé avant le temps limité, alors le temps qui a été nécessaire pour le trouver est enregistré dans la partie en cours 
     * (terminePartie).
     * 
     * Le nombre de lettres restantes soit initialisé avec la longueur du mot choisi dans le dictionnaire (démarrePartie). Lorsque tux trouve 
     * les bonnes lettres dans l'ordre, le nombre de lettres restantes diminue et la partie est gagnée si le nombre de lettres restantes est nul(appliqueRègles). 
     * En fin de partie, on calcule le pourcentage de lettres trouvées (compris entre 0 et 100%) et on l'enregistre dans la partie courante (terminePartie).
     * @param partie 
     */
    @Override
    protected void démarrePartie(Partie partie) {
        // démarre le chrono
        chrono.start();
        // Nombre de lettres restantes initialisé au nombre de lettre du mot
        nbLettresRestantes = super.getLettres().size();

    }

    /**
     * Définit les règles d'une partie comme étant les suivantes 
     *  s'il ne reste plus de temps, on vide la liste de lettre pour sortir de la boucle de jeu
     *  si on a trouvé la première lettre du mot, on la supprime de l'environnement
     * @param partie 
     */
    @Override
    protected void appliqueRegles(Partie partie) {
        
        if (!chrono.remainsTime()) { // Si il ne reste plus de temps, on vide la liste de lettre pour sortir de la boucle de jeu
            resetListDeLettres();
        } else if (tuxTrouveLettre()) { // Si on a trouvé la première lettre du mot, on la supprime de l'environnement
            removeFirstLettres();
            nbLettresRestantes--;
        }
    }

    /**
     * Termine une partie de la façon suivante : 
     * arrête le chronomètre
     * définit si le mot à effectivement été trouvé et change la valeur de 'trouvé' en conséquence
     * @param partie 
     */
    @Override
    protected void terminePartie(Partie partie) {
        chrono.stop();
        if (chrono.remainsTime()) {
            partie.setTemps(chrono.getSeconds());
        }
        partie.setTrouvé(nbLettresRestantes);    
    }
}
