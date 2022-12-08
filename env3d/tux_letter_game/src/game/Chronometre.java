package game;

/**
 * Gère le temps dans le jeu 
 * @author Damien TORNAMBE
 * @author Levi CORMIER
 */
public class Chronometre {
    
    private long begin;
    private long end;
    private long current;
    private static final int limite = 60;

    public Chronometre() {
    }

    public static int getLimite() {
        return limite;
    }
    
    public void start(){
        begin = System.currentTimeMillis();
    }
 
    public void stop(){
        end = System.currentTimeMillis() - begin;
    }
 
    public long getTime() {
        return end - begin;
    }
 
    public long getMilliseconds() {
        return end - begin;
    }
 
    public int getSeconds() {
        return (int) (end / 1000.0);
    }
 
    public double getMinutes() {
        return end / 60000.0;
    }
 
    public double getHours() {
        return end / 3600000.0;
    }
    
    /**
    * Permet de savoir s'il reste du temps.
    * @return limite - (current - begin) = le temps restant:
    * cas valeur positive: il reste du temps on renvoie true
    * cas valeur = 0 ou négative: temps écoulé renvoie false
    */
    public boolean remainsTime() {
        current = System.currentTimeMillis();
        int timeSpent;
        /* current - begin = temps écoulé depuis le début
        /* 
        */
        timeSpent = (int) ((limite - (current - begin)/1000.0));
        return (timeSpent > 0);
    }
}
