package game;

import game.utils.XMLUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import java.io.File;
import org.w3c.dom.DOMException;

/**
 * Gère le profil: 
 * création/charge, sauvegarde, parties.
 * @author Damien TORNAMBE
 * @author Levi CORMIER
 *
 */
public class Profil {

    private String nom;
    private String avatar;
    private String dateNaissance;
    private ArrayList<Partie> parties;
    public Document _doc;

    // profil temporaire
    Profil() {
        nom = "Bob";
        avatar = "bobby.jpg";
        dateNaissance = "1111-11-11";
        parties = new ArrayList<Partie>();
    }

    // 
    /**
     * initialise un profil à partir du nom et de la date de naissance du joueur passés en paramètres 
     * @param nom correspond au pseudo du joueur
     * @param dateNaissance correspond à la date de naissance du joueur
     */
    public Profil(String nom, String dateNaissance) {
        this.nom = nom;
        this.dateNaissance = dateNaissance;
        parties = new ArrayList<>();
        // a modif plus tard
        this.avatar = nom + ".jpg";
    }

    // Cree un DOM à partir d'un fichier XML
    /*
   
     */
    /**
     * Lit un fichier XML, parse le document et utilise le document DOM pour extraire les données nécessaires
     * à la récupération des valeurs du profil et des parties existantes.
     * @param nomFichier correspond au nom du fichier à partir duquel charger un profil
     */
    public Profil(String nomFichier) {
        _doc = fromXML(nomFichier);
        //récupération du nom du joueur 
        Element nomElt = (Element) _doc.getElementsByTagName("prof:nom").item(0);
        nom = nomElt.getTextContent();
        //récupération du nom de fichier de l'avatar du joueur 
        Element avatarElt = (Element) _doc.getElementsByTagName("prof:avatar").item(0);
        avatar = avatarElt.getTextContent();
        // récupération de la date de naissance située dans l'élément anniversaire
        Element anniversaireElt = (Element) _doc.getElementsByTagName("prof:anniversaire").item(0);
        dateNaissance = anniversaireElt.getTextContent();
        // récupère le nombre de parties dans le fichier
        parties = new ArrayList<Partie>();
        setParties(_doc, parties);
    }

    public String getNom() {
        return nom;
    }

    public ArrayList<Partie> getParties() {
        return parties;
    }
    
    /**
     * Récupère une partie dans la liste des parties, située à l'index donné en paramètre
     * @param index correspond à l'index à partir duquel on veut récupérer une partie
     * @return p Partie qui est la partie correspondante 
     */
    public Partie getPartie(int index) {
        //date mot niveau trouvé temps
        String date = parties.get(index).getDate();
        String mot = parties.get(index).getMot();
        int niveau = parties.get(index).getNiveau();
        int trouvé = parties.get(index).getTrouvé();
        int temps = parties.get(index).getTemps();
        Partie p = new Partie(date, mot, niveau, trouvé, temps);
        return p;
    }

   
    /* pour chaque partie dans la liste 
    faire un String qui contient toutes les données d'une partie*/
    /**
     * Fait une ArrayList de String composée de l'affichage des parties en utilisant la méthode toString()
     * @return partiesStr ArrayList de String correspond à la liste des affichages de chaque partie de la liste de parties
     */
    public ArrayList<String> partiesToString() {
        ArrayList<String> partiesStr = new ArrayList<String>(parties.size());
        for (Partie p : parties) {
            partiesStr.add(p.toString());
        }
        return partiesStr;
    }

    /**
     * A partir d'un autre ArrayList, set l'attribut parties courant
     * @param parties correspond à la liste des parties censée remplacer la liste actuelle
     */
    public void setParties(ArrayList<Partie> parties) {
        this.parties = parties;
    }

    /**
     * A partir d'un document XML, récupère les valeurs de chaque partie et set l'attribut parties courant en conséquence
     * @param doc correspond au document dans lequel les parties vont être piochées 
     * @param parties correspond à un buffer qui va contenir les parties avant de les mettre dans l'attribut parties
     */
    public void setParties(Document doc, ArrayList<Partie> parties) {
        int nbParties = doc.getElementsByTagName("prof:partie").getLength();
        for (int i = 0; i < nbParties; i++) {
            Element partieElt = (Element) doc.getElementsByTagName("prof:partie").item(i);
            String date = partieElt.getAttribute("date");
            //String date = xmlDate;
            // mettre la date au format jj/mm/aaaa
            if (Partie.estDateXML(date)) {
                date = xmlDateToProfileDate(date);
            }

            //Element tempsElt = (Element) partieElt.getElementsByTagName("prof:temps").item(0);
            //int temps = Integer.parseInt(tempsElt.getTextContent());
            Element motElt = (Element) partieElt.getElementsByTagName("prof:mot").item(0);
            String mot = motElt.getTextContent();

            Element niveauElt = (Element) partieElt.getElementsByTagName("prof:niveau").item(0);
            int niveau = Integer.parseInt(niveauElt.getAttribute("valeur"));

            // tester s'il y a un temps dans les balises 
            int temps = 60;
            Element tempsElt = (Element) partieElt.getElementsByTagName("prof:temps").item(0);
            if (tempsElt != null) {
                temps = Integer.parseInt(tempsElt.getTextContent());
            }
            // String avec le 100% il faut faire sauter le "%"
            String trouvéAvecPourcent = partieElt.getAttribute("trouvé");
            //creating a constructor of StringBuffer class  
            StringBuffer trouvéSansPourcent = new StringBuffer(trouvéAvecPourcent);
            //invoking the method and verifu if there's a %
            if (trouvéSansPourcent.charAt(trouvéSansPourcent.length() - 1) == '%') {
                trouvéSansPourcent.deleteCharAt(trouvéSansPourcent.length() - 1);
            }
            String trouvéStr = trouvéSansPourcent.toString();
            int trouvé = Integer.parseInt(trouvéStr);
            //int trouvé = Integer.parseInt();
            // mettre le temps parsé mais sans le "%" (var String et pop le dernier character)
            //int trouvé = Integer.parseInt(partieElt.getAttribute("trouvé"));
            Partie p = new Partie(date, mot, niveau, trouvé, temps);
            parties.add(p);
        }
        this.parties = parties;
    }

    /**
     * Sauvegarde un DOM en XML
     * @param nomFichier correspond au nom de fichier xml que l'on veut créer
     */
    public void toXML(String nomFichier) {
        try {
            XMLUtil.DocumentTransform.writeDoc(_doc, nomFichier);
        } catch (Exception ex) {
            Logger.getLogger(Profil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Crée un DOM à partir d'un fichier XML
     * @param nomFichier correspond au nom du fichier à partir duquel on veut créer un DOM
     * @return 
     */
    public Document fromXML(String nomFichier) {
        try {
            return XMLUtil.DocumentFactory.fromFile(nomFichier);
        } catch (Exception ex) {
            Logger.getLogger(Profil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Permet de rajouter à la liste des parties une Partie instanciée
     * @param p correspond à la partie que l'on souhaite ajouter
     */
    public void ajouterPartie(Partie p) {
        parties.add(p);
    }

    /**
     * Permet de remplacer une partie instanciée dans la liste des parties
     * @param p correspond à la partie que l'on veut mettre à la place d'une autre dans la liste des parties
     * @param index correspond à l'index auquel on veut remplacer la partie par celle en paramètre
     */
    public void remplacerPartie(Partie p, int index) {
        p.setDate(Jeu.dateDuJour());
        parties.set(index, p);
    }

    public int getDernierNiveau() {
        return 0;
    }

    public String getDateNaissance() {
        return dateNaissance;
    }
    
    /**
     * Permet de sauvegarder le document DOM dans un fichier XML
     * Parse le fichier et écrit dedans le profil et les parties
     * @param filename correspond fichier XMLà sauvegarder 
     * @throws ParserConfigurationException 
     */
    public void sauvegarder(String filename) throws ParserConfigurationException  {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        _doc = builder.newDocument();

        // création de l'élément racine profil avec ses attributs (espaces de nom) 
        Element profilElt = _doc.createElement("prof:profil");
        String xmlnsXsi = "http://www.w3.org/2001/XMLSchema-instance";
        profilElt.setAttribute("xmlns:xsi", xmlnsXsi);
        String xmlnsProf = "http://myGame/tux";
        profilElt.setAttribute("xmlns:prof", xmlnsProf);
        String xsiSchemaLocation = "http://myGame/tux ../xsd/profil.xsd";
        profilElt.setAttribute("xsi:schemaLocation", xsiSchemaLocation);
        _doc.appendChild(profilElt);

        // création de l'élément nom avec son contenu 
        Element nomElt = _doc.createElement("prof:nom");
        nomElt.setTextContent(getNom());
        profilElt.appendChild(nomElt);

        // création de l'élément avatar avec son contenu (chemin de fichier)
        Element avatarElt = _doc.createElement("prof:avatar");
        avatarElt.setTextContent(avatar);
        profilElt.appendChild(avatarElt);

        // création de l'élément anniversaire avec son contenu (date)
        Element anniversaireElt = _doc.createElement("prof:anniversaire");
        anniversaireElt.setTextContent(dateNaissance);
        profilElt.appendChild(anniversaireElt);

        //création de l'élément parties
        Element partiesElt = _doc.createElement("prof:parties");
        profilElt.appendChild(partiesElt);
        boolean estDernierePartie = false;
        for (int i = 0; i < parties.size() - 1; i++) {
            Element partieElt = parties.get(i).getPartie(_doc, estDernierePartie);
            partiesElt.appendChild(partieElt);
        }
        // date de la derniere partie deja au format xml 
        estDernierePartie = true;
        if (parties != null && parties.size() > 0) {
            Element dernierePartieElt = parties.get(parties.size() - 1).getPartie(_doc, estDernierePartie);
            partiesElt.appendChild(dernierePartieElt);
        }
        String s = "src/data/profil/" + filename;
        toXML(s);
    }

    /**
     * Charge un profil XML après avoir vérifié dans la liste de fichiers dans profil que le profil existe bel et bien
     * @param nom correspond au nom du profil XML à charger
     * @return exist boolean renvoie true si fichier profil existe, false sinon
     */
    public boolean charge(String nom) {
        String s = nom + ".xml";
        File file = new File("src/data/profil/");
        File[] files = file.listFiles();
        boolean exist = false;
        if (files != null) {
            int i = 0;
            while (i < files.length && !exist) {
                if (s.equals(files[i].getName())) {
                    exist = true;
                }
                i++;
            }
        }
        return exist;
    }

    public static String xmlDateToProfileDate(String xmlDate) {
        String date;
        // récupérer le jour
        date = xmlDate.substring(xmlDate.lastIndexOf("-") + 1, xmlDate.length());
        date += "/";
        // récupérer le mois
        date += xmlDate.substring(xmlDate.indexOf("-") + 1, xmlDate.lastIndexOf("-"));
        date += "/";
        // récupérer l'année
        date += xmlDate.substring(0, xmlDate.indexOf("-"));
        return date;
    }

    public static String profileDateToXmlDate(String profileDate) {
        String date;
        // Récupérer l'année
        date = profileDate.substring(profileDate.lastIndexOf("/") + 1, profileDate.length());
        date += "-";
        // Récupérer  le mois
        date += profileDate.substring(profileDate.indexOf("/") + 1, profileDate.lastIndexOf("/"));
        date += "-";
        // Récupérer le jour
        date += profileDate.substring(0, profileDate.indexOf("/"));
        return date;
    }

}
