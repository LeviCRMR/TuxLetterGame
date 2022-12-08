package game;

import game.utils.XMLUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Permet de manipuler un dictionnaire : 
 * Utilisation des parseurs SAX et DOM
 * Lecture, Ecriture et modification du dictionnaire
 * @author Damien TORNAMBE
 * @author Levi CORMIER
 *
 */
public class Dico extends DefaultHandler {

    /**
     * Partie SAX pour signifier dans quel noeud on se situe, on fait une
     * énumeration avec les noms de ceux-ci + autre et début pour quand on est
     * pas encore ou plus dans les noeuds du document XML
     */
    enum Etats {
        DEBUT,
        DICTIONNAIRE,
        NIVEAU,
        MOT,
        AUTRE
    }

    private ArrayList<String> listeNiveau1;
    private ArrayList<String> listeNiveau2;
    private ArrayList<String> listeNiveau3;
    private ArrayList<String> listeNiveau4;
    private ArrayList<String> listeNiveau5;
    private String cheminFichierDico;
    private String buffer; //pour le parsing SAX
    private Etats etat; //pour le parsing SAX
    private int niveau; // pour le parsing SAX
    private Document doc;

    public Dico(String cheminFichierDico) {
        super();
        listeNiveau1 = new ArrayList<String>();
        listeNiveau2 = new ArrayList<String>();
        listeNiveau3 = new ArrayList<String>();
        listeNiveau4 = new ArrayList<String>();
        listeNiveau5 = new ArrayList<String>();
        this.cheminFichierDico = cheminFichierDico;
    }

    /* -----------------------------------------------------------------------------------------------------------------------*/
    /*                            PARSING SAX
    /* ----------------------------------------------------------------------------------------------------------------------- */
    /**
     * Début du parsing SAX : le parseur entre dans le document, mais n'est pas
     * encore dans la racine du document
     *
     * @throws SAXException
     */
    @Override
    public void startDocument() throws SAXException {
        etat = Etats.DEBUT;
    }

    /**
     * Suite du parsing SAX : Dès qu'un élément est rencontré, on vérifie le nom
     * de la balise et l'état est actualisé en conséquence. Quand il se trouve dans le niveau, il actualise l'attribut niveau qui servira à classer les mots
     *
     * @param uri
     * @param localName
     * @param qName
     * @param attributes
     * @throws SAXException
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (etat) {
            case DEBUT:
                if (qName.equals("dico:dictionnaire")) {
                    etat = Etats.DICTIONNAIRE;
                }
                break;
            case DICTIONNAIRE:
                if (qName.equals("dico:niveau")) {
                    etat = Etats.NIVEAU;
                    try {
                        niveau = Integer.parseInt(attributes.getValue("valeur"));
                    } catch (Exception e) {
                        throw new SAXException(e); // erreur, le contenu de valeur n'est pas un entier
                    }
                }
                break;
            case NIVEAU:
                if (qName.equals("dico:mot")) {
                    etat = Etats.MOT;
                }
                break;
            default:
                break;
        }
    }

    /**
     * Suite du parsing SAX : dès que le parseur sort d'un élément, l'état est actualisé en conséquence
     * @param uri
     * @param localName
     * @param qName
     * @throws SAXException
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (etat) {
            case DICTIONNAIRE:
                etat = Etats.AUTRE;
                break;
            case NIVEAU:
                etat = Etats.DICTIONNAIRE;
                break;
            case MOT:
                etat = Etats.NIVEAU;
                break;
            default:
                break;
        }
    }

    /**
     * Suite du parsing SAX : Le parseur récupère le texte situé dans le mot et ajoute le mot dans la bonne liste suivant son niveau 
     *
     * @param ch
     * @param start
     * @param length
     * @throws SAXException
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        switch (etat) {
            case MOT:
                //buffer = new String(ch, start, length);

                for (int i = start; i < start + length; i++) {
                    buffer += ch[i];
                }
                switch (niveau) {
                    case 1:
                        listeNiveau1.add(buffer);
                        buffer = "";
                        break;
                    case 2:
                        listeNiveau2.add(buffer);
                        buffer = "";
                        break;
                    case 3:
                        listeNiveau3.add(buffer);
                        buffer = "";
                        break;
                    case 4:
                        listeNiveau4.add(buffer);
                        buffer = "";
                        break;
                    case 5:
                        listeNiveau5.add(buffer);
                        buffer = "";
                        break;
                    default:
                        throw new AssertionError();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Fin du parsing SAX : Le parseur sort du document on vérifie sur l'output que toutes les listes sont bien initialisées avec les bonnes valeurs
     * @throws SAXException
     */
    @Override
    public void endDocument() throws SAXException {

        System.out.println("Liste de mots de niveau 1");
        for (String mot : listeNiveau1) {
            System.out.println(mot);
        }
        System.out.println("Liste de mots de niveau 2");
        for (String mot : listeNiveau2) {
            System.out.println(mot);
        }
        System.out.println("Liste de mots de niveau 3");
        for (String mot : listeNiveau3) {
            System.out.println(mot);
        }
        System.out.println("Liste de mots de niveau 4");
        for (String mot : listeNiveau4) {
            System.out.println(mot);
        }
        System.out.println("Liste de mots de niveau 5");
        for (String mot : listeNiveau5) {
            System.out.println(mot);
        }
    }

    /**
     * Appel du parsing SAX : le parseur est instancié et parse le document donné en paramètre
     * @param cheminFichierDico correspond au chemin relatif jusqu'au répertoire qui contient le fichier à parser
     * @param filename correspond au nom du fichier à parser
     */
    public void lireDictionnaireSAX(String cheminFichierDico, String filename) {
        String fullPath = cheminFichierDico + filename;
        try {
            // création d'une fabrique de parseurs SAX 
            SAXParserFactory fabrique = SAXParserFactory.newInstance();

            // création d'un parseur SAX 
            SAXParser parseur = fabrique.newSAXParser();

            // lecture d'un fichier XML avec un DefaultHandler 
            File fichier = new File(fullPath);
            Dico dico = new Dico(fullPath);
            parseur.parse(fichier, dico);

        } catch (ParserConfigurationException pce) {
            System.out.println("Erreur de configuration du parseur");
            System.out.println("Lors de l'appel à newSAXParser()");
        } catch (SAXException se) {
            System.out.println("Erreur de parsing");
            System.out.println("Lors de l'appel à parse()");
        } catch (IOException ioe) {
            System.out.println("Erreur d'entrée/sortie");
            System.out.println("Lors de l'appel à parse()");
        }
    }

    /* -----------------------------------------------------------------------------------------------------------------------*/
    /*                            PARSING DOM
    /* ----------------------------------------------------------------------------------------------------------------------- */
    
    /**
     * Parse avec DOM et lit le dictionnaire pour ajouter les mots qu'il contient en fonction de leur niveau dans les listes de mots 
     * @param path correspond au chemin relatif jusqu'au répertoire qui contient le fichier à parser
     * @param filename correspond au nom du fichier à parser
     * @throws Exception 
     */
    public void lireDictionnaireDOM(String path, String filename) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = factory.newDocumentBuilder();

        String fullPath = path + filename;

        doc = builder.parse(fullPath);

        // On commence par récupérer la liste des noeuds fils de la racine qui sont les noeuds niveaux
        NodeList postList = doc.getElementsByTagName("dico:niveau");
        // On compte combien on a de noeuds niveau
        int nbNodeNiveau = postList.getLength();

        for (int i = 0; i < nbNodeNiveau; i++) {

            /* On récupère le ième noeud */
            Element nodeNiveau = (Element) doc.getElementsByTagName("dico:niveau").item(i);
            // On récupère la valeur de son attribut valeur, autrement dit le niveau de ses mots
            int valueAttribute = Integer.parseInt(nodeNiveau.getAttribute("valeur"));
            // Récupération de la liste des mots du ième niveau 
            postList = nodeNiveau.getElementsByTagName("dico:mot");
            // on compte combien il a de mot dans le ième niveau niveau
            int nbNodeMot = postList.getLength();
            for (int j = 0; j < nbNodeMot; j++) {

                String motAAjouter = postList.item(j).getTextContent();
                switch (valueAttribute) {
                    case 1:
                        listeNiveau1.add(motAAjouter);
                        break;
                    case 2:
                        listeNiveau2.add(motAAjouter);
                        break;
                    case 3:
                        listeNiveau3.add(motAAjouter);
                        break;
                    case 4:
                        listeNiveau4.add(motAAjouter);
                        break;
                    case 5:
                        listeNiveau5.add(motAAjouter);
                        break;
                    default:
                        System.out.println("Erreur lors de l'ajout de mots dans le dictionnaire depuis le fichier dico.xml");
                        break;
                }
            }

        }
    }

    /**
     * 
     * Si le niveau n'est pas dans l'intervalle [1-5], on selectionne par défaut un mot de niveau 1 (on utilise la méthode vérifieNiveau() pour ça)
     * @param niveau correspond au niveau du mot que l'on veut tirer au hasard
     * @return motChoisi String correspond au mot tiré au hasard depuis la liste du niveau choisi
     */
    public String getMotDepuisListeNiveaux(int niveau) {
        if (!vérifieNiveau(niveau)) {
            niveau = 1;
        }
        String motChoisi = "";
        switch (niveau) {
            case 1:
                motChoisi = getMotDepuisListe(listeNiveau1);
                break;
            case 2:
                motChoisi = getMotDepuisListe(listeNiveau2);
                break;
            case 3:
                motChoisi = getMotDepuisListe(listeNiveau3);
                break;
            case 4:
                motChoisi = getMotDepuisListe(listeNiveau4);
                break;
            default: // cas niveau = 5
                motChoisi = getMotDepuisListe(listeNiveau5);
        }
        return motChoisi;
    }

    /**
     * Extrait un mot au hasard depuis une liste donnée. Dans le cas ou la liste de mot est vide, on renvoie le mot "erreur";
     * @param list correspond à la liste depuis laquelle on veut tirer au hasard un mot
     * @return motChoisi String qui correspond au mot tiré au hasard 
     */
    private String getMotDepuisListe(ArrayList<String> list) {
        int nbDeMots = list.size();
        String motChoisi = "erreur";
        if (nbDeMots != 0) {
            Random r = new Random(0.0, (double) (nbDeMots - 1));
            int roll = (int) r.get();
            motChoisi = list.get(roll);
        }

        return motChoisi;
    }
    
    /**
     * Ajoute un mot à la liste des mots, donc au dictionnaire
     * Le niveau est vérifié avec la méthode vérifieNiveau(int niveau). 
     * Si le niveau donné n'est pas dans l'intervalle [1-5] le mot n'est pas ajouté
     * @param niveau correspond au niveau du mot que l'on veut ajouter 
     * @param mot correspond au mot que l'on veut ajouter dans le dictionnaire
     */
    public void ajouteMotADico(int niveau, String mot) {
        if (vérifieNiveau(niveau)) {
            switch (niveau) {
                case 1:
                    listeNiveau1.add(mot);
                    break;
                case 2:
                    listeNiveau2.add(mot);
                    break;
                case 3:
                    listeNiveau3.add(mot);
                    break;
                case 4:
                    listeNiveau4.add(mot);
                    break;
                default: // cas niveau = 5
                    listeNiveau5.add(mot);
            }
        }
    }

    public String getCheminFichierDico() {
        return cheminFichierDico;
    }
    
    /**
     * Vérifie que le niveau est bien dans l'intervalle [1-5]
     * @param niveau correspond au niveau à vérifier
     * @return estInclus boolean true si le niveau donnée est dans l'intervalle [1-5] sinon false
     */
    private boolean vérifieNiveau(int niveau) {
        boolean estInclus = true;
        if (niveau < 1 || niveau > 5) {
            estInclus = false;
        }
        return estInclus;
    }
    /**
     * Sauvegarde le dictionnaire en XML à l'aide du parseur DOM et appelle la méthode getNiveauElt(Document doc, int niveau) qui génère les éléments 
     * @param filename correspond au nom du fichier que l'on veut sauvegarder/créer en xml 
     * @throws ParserConfigurationException 
     */
    public void ecrireDOM(String filename) throws ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.newDocument();

        // création de l'élément racine profil avec ses attributs (espaces de nom) 
        Element dicoElt = doc.createElement("dico:dictionnaire");
        String xmlnsXsi = "http://www.w3.org/2001/XMLSchema-instance";
        dicoElt.setAttribute("xmlns:xsi", xmlnsXsi);

        String xmlnsDico = "http://myGame/tux";
        dicoElt.setAttribute("xmlns:dico", xmlnsDico);

        String xsiSchemaLocation = "http://myGame/tux ../xsd/dico.xsd";
        dicoElt.setAttribute("xsi:schemaLocation", xsiSchemaLocation);

        doc.appendChild(dicoElt);

        dicoElt.appendChild(getNiveauElt(doc, 1));
        dicoElt.appendChild(getNiveauElt(doc, 2));
        dicoElt.appendChild(getNiveauElt(doc, 3));
        dicoElt.appendChild(getNiveauElt(doc, 4));
        dicoElt.appendChild(getNiveauElt(doc, 5));

        String s = "src/data/xml/" + filename;
        toXML(s);
    }
    
    
    /**
     * Crée un élément niveau et récupère tous les mots de son niveau pour les créer et les ajouter ensuite à celui-ci
     * Utilisé par la méthode ecrireDOM(String filename) pour écrire le document XML du dictionnaire
     * @param doc
     * @param niveau
     * @return niveauElt Element
     */
    public Element getNiveauElt(Document doc, int niveau) {
        if (vérifieNiveau(niveau)) {
            // création de l'élément niveau avec son contenu 
            Element niveauElt = doc.createElement("dico:niveau");
            niveauElt.setAttribute("valeur", Integer.toString(niveau));
            switch (niveau) {
                case 1:
                    // création des éléments mot avec leur contenu
                    for (String mot : listeNiveau1) {
                        Element motElt = doc.createElement("dico:mot");
                        motElt.setTextContent(mot);
                        niveauElt.appendChild(motElt);
                    }
                    break;
                case 2:
                    // création des éléments mot avec leur contenu
                    for (String mot : listeNiveau2) {
                        Element motElt = doc.createElement("dico:mot");
                        motElt.setTextContent(mot);
                        niveauElt.appendChild(motElt);
                    }
                    break;
                case 3:
                    // création des éléments mot avec leur contenu
                    for (String mot : listeNiveau3) {
                        Element motElt = doc.createElement("dico:mot");
                        motElt.setTextContent(mot);
                        niveauElt.appendChild(motElt);
                    }
                    break;
                case 4:
                    // création des éléments mot avec leur contenu
                    for (String mot : listeNiveau4) {
                        Element motElt = doc.createElement("dico:mot");
                        motElt.setTextContent(mot);
                        niveauElt.appendChild(motElt);
                    }
                    break;
                case 5:
                    // création des éléments mot avec leur contenu
                    for (String mot : listeNiveau5) {
                        Element motElt = doc.createElement("dico:mot");
                        motElt.setTextContent(mot);
                        niveauElt.appendChild(motElt);
                    }
                    break;
                default:
                    break;
            }
            return niveauElt;
        } else {
            // création de l'élément niveau 1 avec son contenu 
            Element niveauElt = doc.createElement("dico:niveau");
            niveauElt.setAttribute("valeur", "1");
            // création des éléments mot avec leur contenu
            for (String mot : listeNiveau1) {
                Element motElt = doc.createElement("dico:mot");
                motElt.setTextContent(mot);
                niveauElt.appendChild(motElt);
            }
            return niveauElt;
        }
    }

    /**
     * Crée un document xml
     * @param nomFichier correspond au nom du fichier que l'on veut créer
     */
    public void toXML(String nomFichier) {
        try {
            XMLUtil.DocumentTransform.writeDoc(doc, nomFichier);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    /**
     * Ajoute un mot au dictionnaire XML seulement s'il n'existe pas encore
     * @param mot correspond au mot que l'on souhaite ajouter
     * @param niveau correspond au niveau du mot
     */
    public void ajouterMot(String mot, int niveau) {
        if (vérifieNiveau(niveau)) {
            int i = 0;
            switch (niveau) {
                case 1:
                    for (String motDansListe : listeNiveau1) {
                        if (motDansListe.equals(mot)) {
                            i++;
                        }
                    }
                    break;
                case 2:
                    for (String motDansListe : listeNiveau2) {
                        if (motDansListe.equals(mot)) {
                            i++;
                        }
                    }
                    break;
                case 3:
                    for (String motDansListe : listeNiveau3) {
                        if (motDansListe.equals(mot)) {
                            i++;
                        }
                    }
                    break;
                case 4:
                    for (String motDansListe : listeNiveau4) {
                        if (motDansListe.equals(mot)) {
                            i++;
                        }
                    }
                    break;
                case 5:
                    for (String motDansListe : listeNiveau5) {
                        if (motDansListe.equals(mot)) {
                            i++;
                        }
                    }
                    break;
                default:
                    break;
            }
            if (i == 0) {
                Element motElt = doc.createElement("dico:mot");
                motElt.setTextContent(mot);
                Element niveauElt = (Element) doc.getElementsByTagName("dico:niveau").item(niveau - 1);
                niveauElt.appendChild(motElt);
                String s = "src/data/xml/dico.xml";
                toXML(s);
            }
        }
    }

}
