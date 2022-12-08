package game;

/**
 *Cette classe nous a été fournie pour le mini projet "trajectoires et collisions"
 * Du coup pourquoi ne pas la réutiliser ici pour générer des valeurs aléatoires
 * Utilisation:
 *      - Random rand = new Random(0.0, 1.0) 
 *      - double alea = r.get() ou ici int alea = (int)r.get()
 * @author gladen
 */
public class Random {

    private double min;
    private double max;
    private double range;

    public Random(double min, double max) {
        setMinMax(min, max);
    }

    public void setMin(double min) {
        setMinMax(min, max);
    }

    public void setMax(double max) {
        setMinMax(min, max);
    }
    
    public void setMinMax(double min, double max) {
        if (min <= max) {
            this.min = min;
            this.max = max;
        } else {
            this.min = max;
            this.max = min;
        }
        range = (max - min) + 1;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getRange() {
        return range;
    }

    public double get() {
        return (Math.random() * range) + min;
    }

}
