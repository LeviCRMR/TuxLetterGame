package game;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Gère les parties du jeu : 
 * pourcentage de réussite, score, mise sous la forme d'élément XML à l'aide du parsing DOM
 * @author Damien TORNAMBE
 * @author Levi CORMIER
 * 
 */
public class Partie {
    private String date;
    private String mot;
    private int niveau;
    private int trouvé;
    private int temps;

    public Partie(String date, String mot, int niveau) {
        this.date = date;
        this.mot = mot;
        this.niveau = niveau;
        this.trouvé = 0;
        this.temps = 0;
    }

    public Partie(String date, String mot, int niveau, int trouvé, int temps) {
        this.date = date;
        this.mot = mot;
        this.niveau = niveau;
        this.trouvé = trouvé;
        this.temps = temps;
    }

    /**
     * Utilisé pour permettre la construction et la réinitialisation d'une Partie déjà faite et issue du XML.
     * Ce constructeur prend comme paramètre un élément DOM correspondant à une partie ; 
     * cet élément est obtenu par parsing du document DOM ; 
     * le constucteur utilisera cet élément pour récupérer les bonnes valeurs 
     * et initialiser la partie
     * @param partieElt correspond à un élément XML partie que l'on souhaite instancier et initialiser en Java
     */
    public Partie(Element partieElt) {
        String xmlDate = partieElt.getAttribute("date");
        date = Profil.xmlDateToProfileDate(xmlDate);
        trouvé = Integer.parseInt(partieElt.getAttribute("trouvé"));
        temps = Integer.parseInt(partieElt.getElementsByTagName("prof:temps").item(0).getTextContent());
        Element niveauNode = (Element) partieElt.getElementsByTagName("prof:niveau").item(0);
        niveau = Integer.parseInt(niveauNode.getAttribute("valeur"));
        mot = niveauNode.getElementsByTagName("prof:mot").item(0).getTextContent();
    }

    public String getDate() {
        return date;
    }

    public int getTrouvé() {
        return trouvé;
    }

    public int getTemps() {
        return temps;
    }
    
   
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Calcule un pourcentage en fonction du nombre de lettres trouvées
     * @param nbDeLettresRestantes correspond au nombre de lettres qu'il reste au joueur à trouver
     */
    public void setTrouvé(int nbDeLettresRestantes) {
        if (nbDeLettresRestantes == 0) {
            trouvé = 100;
        } else {
            int nbDeLettresTrouvees = mot.length() - nbDeLettresRestantes;
            // produit en croix pour obtenir le pourcentage
            this.trouvé = 100 * nbDeLettresTrouvees / mot.length();
        }
    }

    public void setTemps(int temps) {
        this.temps = temps;
    }

    public int getNiveau() {
        return niveau;
    }

    public String getMot() {
        return mot;
    }

    public void setMot(String mot) {
        this.mot = mot;
    }

    /**
     * Vérifie si la date est au format XML : yyyy-MM-dd
     * @param date correspond à la date que l'on veut vérifier
     * @return estDateXML boolean qui vaut true quand la date est au format XML, false sinon
     */
    public static boolean estDateXML(String date) {
        boolean estDateXML = false;
        if (date.charAt(4) == '-' && date.charAt(7) == '-') {
            estDateXML = true;
        }
        return estDateXML;
    }


    /**
     * Cette méthode crée le bloc XML représentant une partie à partir du paramètre doc
     * (pour créer les éléments du XML) et renvoie ce bloc en tant que Element.
     * @param doc correspond au document sur lequel on veut créer l'élément partie 
     * @param estDernierePartie indique si c'est effectivement la première partie (true, false sinon)
     * @return partieElt Element correspond à la partie créée à partir de l'instance partie courante
     */
    public Element getPartie(Document doc, boolean estDernierePartie) {
        Element partieElt = doc.createElement("prof:partie");
        //date
        //
        if (!estDernierePartie && !estDateXML(date)) {
            String xmlDate = Profil.profileDateToXmlDate(date);
            partieElt.setAttribute("date", xmlDate);
        } else {
            partieElt.setAttribute("date", date);
        }
        //trouvé 100%? 
        partieElt.setAttribute("trouvé", Integer.toString(trouvé) + "%");

        Element tempsElt = doc.createElement("prof:temps");
        if (trouvé == 100) {
            tempsElt.setTextContent(Integer.toString(temps));
            partieElt.appendChild(tempsElt);

        } else {
            int chrono = Chronometre.getLimite();
            tempsElt.setTextContent(Integer.toString(chrono));
            partieElt.appendChild(tempsElt);
        }

        Element niveauElt = doc.createElement("prof:niveau");
        niveauElt.setAttribute("valeur", Integer.toString(niveau));

        Element motElt = doc.createElement("prof:mot");
        motElt.setTextContent(mot);

        //ajout de mot en tant qu'élément fils de l'élément niveau
        niveauElt.appendChild(motElt);
        //ajout de niveau (qui contient mot) en tant qu'élément fils de l'élément partie
        partieElt.appendChild(niveauElt);
        //ajout de niveau (qui contient mot) en tant qu'élément fils de l'élément partie
        return partieElt;
    }

    /**
     * Indique quelle partie est la meilleure en fonction du calcul du score : (niveau x 100)/temps 
     * @param p correspond à la partie à laquelle on veut comparer la partie actuelle 
     * @return renvoie 1 si les parties sont égales en termes de score, 
     * renvoie 2 si la partie passée en paramètre est plus grande (son score) que celle à laquelle elle est comparée
     * renvoie 3 si la partie passé en paramètre est plus petite (son score) que celle à laquelle elle est comparée
     * renvoie -1 s'il y a une erreur
     */
    public int compare(Partie p) {
        double scoreP = (double) (p.niveau*100)/p.temps; 
        double scoreThis = (double) (niveau*100)/temps;
        if(scoreP == scoreThis){
            
            return 1;
        } else if (scoreP > scoreThis){
            return 2;
        } else if (scoreP < scoreThis){
            return 3;
        } else {
            return -1;
            //erreur
        }
    }
    
   /**
    * calcule le score de la partie courante
    * @return 
    */
    public int calScore() {
        if (temps == 0 || trouvé != 100) {
            return 0;
        } else {
            return (100 * niveau) / temps;
        }
    }


    @Override
    public String toString() {
        String s = "";
        // 1. partie du DATE : MOT de niveau NIVEAU trouvé à TRIOUVE%
        s += "Partie du " + date + " : " + mot + " de niveau " + niveau + " trouvé à " + trouvé + "%" + " en " + temps + " sec";
        return s;
    }

}
