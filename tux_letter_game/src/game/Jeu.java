package game;

import env3d.Env;
import game.utils.XMLUtil;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.lwjgl.input.Keyboard;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Met en place et gère le jeu : 
 * menus : affichage et toutes les actions possibles du joueur
 * partie : execute, joue, collisions, etc
 * @author Damien TORNAMBE
 * @author Levi CORMIER
 */
public abstract class Jeu {

    enum MENU_VAL {
        MENU_SORTIE, MENU_CONTINUE, MENU_JOUE
    }

    private final Env env;
    private Tux tux;
    private final Room mainRoom;
    private final Room menuRoom;
    private ArrayList<Letter> lettres;
    private Profil profil;
    private final Dico dico;
    protected EnvTextMap menuText;                         //text (affichage des texte du jeu)
    private ArrayList<Coordonnee> positionLettres;

    /**
     * Crée un nouvel environnement, instancie une Room, instancie et calcule les positions possibles des lettres en fonction de la taille de la room de jeu 
     * (On considère qu'on aura pas de mot ayant plus de 25 lettres), instancie une autre Room pour les menus, règle la camera, désactive les contrôles par défaut,
     * instancie un profil par défaut, puis remplit le dictionnaire à l'aide du parseur DOM, instancie le menu de texte EnvTextMap et une liste de lettres pour le mot qui sera joué
     * 
     * @throws Exception 
     */
    public Jeu() throws Exception {

        // Crée un nouvel environnement
        env = new Env();

        // Instancie une Room
        mainRoom = new Room("plateau.xml");

        /* Instancie et calcule les positions possibles des lettres en fonction de la taille de la room de jeu
           On considère qu'on aura pas de mot ayant plus de 25 lettres
         */
        positionLettres = new ArrayList<Coordonnee>();;
        initListPositionLetter();

        profil = new Profil();

        // Instancie une autre Room pour les menus
        menuRoom = new Room("menuRoom.xml");
        menuRoom.setTextureEast("textures/black.png");
        menuRoom.setTextureWest("textures/black.png");
        menuRoom.setTextureNorth("textures/black.png");
        menuRoom.setTextureBottom("textures/black.png");

        // Règle la camera
        env.setCameraXYZ(50, 60, 175);
        env.setCameraPitch(-20);

        // Désactive les contrôles par défaut
        env.setDefaultControl(false);

        // Instancie un profil par défaut
        //profil = new Profil();
        // Dictionnaire
        dico = new Dico("src/data/xml/");

        // Remplit le dico de mot depuis un fichier xml
        dico.lireDictionnaireDOM(dico.getCheminFichierDico(), "dico.xml");

        // Contiendra la liste des lettres du mot joué
        lettres = new ArrayList<Letter>();

        // instancie le menuText
        menuText = new EnvTextMap(env);

        // Textes affichés à l'écran
        menuText.addText("Voulez vous ?", "Question", 200, 300);
        menuText.addText("1. Commencer une nouvelle partie ?", "Jeu1", 250, 280);
        menuText.addText("2. Charger une partie existante ?", "Jeu2", 250, 260);
        menuText.addText("3. Sortir de ce jeu ?", "Jeu3", 250, 240);
        menuText.addText("4. Quitter le jeu ?", "Jeu4", 250, 220);
        menuText.addText("2. Sortir de ce jeu?", "sortirNJ", 250, 260);
        menuText.addText("3. Quitter le jeu ?", "quitterNJ", 250, 240);

        menuText.addText("Choisissez un nom de joueur : ", "NomJoueur", 200, 300);
        menuText.addText("Veuillez entrer votre année de naissance : ", "AnnéeDeNaissance", 200, 300);
        menuText.addText("Le mois : ", "MoisDeNaissance", 200, 300);
        menuText.addText("Le jour : ", "JourDeNaissance", 200, 300);
        menuText.addText("Date de naissance invalide", "ErreurDateNaissance", 200, 300);
        menuText.addText("1. Entrer à nouveau une date de naissance ?", "EntrerDateNaissanceANouveau", 250, 280);
        menuText.addText("2. Retourner au menu principal ?", "RetourMenuPrincipal", 250, 260);

        menuText.addText("1. Charger un profil de joueur existant ?", "Principal1", 250, 280);
        menuText.addText("2. Créer un nouveau joueur ?", "Principal2", 250, 260);
        menuText.addText("5. Sortir du jeu ?", "Principal5", 250, 200);
        menuText.addText("3. Ajouter un mot au dictionnaire?", "Principal3", 250, 240);
        menuText.addText("4. Voir le podium des meilleurs scores", "Principal4", 250, 220);

        menuText.addText("Choix de la difficulté du mot (compris entre 1 et et 5) : ", "ChoixDuNiveau", 100, 300);
        menuText.addText("Valeur de niveau incorrect", "ErreurChoixDuNiveau", 100, 300);

        menuText.addText("niveau : ", "niv", 250, 300);

        menuText.addText("mot (seulement des lettres minuscules): ", "mot", 250, 300);
    }

    /**
     * Ajoute du texte au menu de texte EnvTextMap avec les options de parties que le joueur pourra refaire pour améliorer son score
     */
    public void menuTextInitJeu2() {
        ArrayList<String> parties = profil.partiesToString();
        int y = 280;
        int taille = parties.size();
        for (int i = taille - 1; i >= 0; i--) {
            y -= 20;
            String cle = "partie" + Integer.toString(i);
            int numero = taille - i;
            //numero--;
            menuText.addText(Integer.toString(numero) + ". " + parties.get(i).toString(), cle, 100, y);
        }
        menuText.addText("Appuyer sur 6 pour quitter le menu", "sortie", 200, y - 20);
    }


    /**
     * Récupère les 5 meilleurs scores tous profils et toutes parties confondues et crée un fichier podium.xml avec les résultats
     * @return podium ArrayList de Profil correspond à la liste des meilleurs profils
     * @throws ParserConfigurationException 
     */
    private ArrayList<Profil> getPodium() throws ParserConfigurationException {
        // le tableau est rempli des noms des fichiers .xml dans le répertoire profil
        String[] fichiersXMLProfil = getProfilsFileList();

        // Ici on récupère tous les profils associés au fichiers xml dans le répertoire profil
        ArrayList<Profil> profils = getArrayListProfils(fichiersXMLProfil);

        // On veut au max un top 5
        ArrayList<String> lesNomAssociésAuMeilleursScores = new ArrayList<String>();
        ArrayList<Integer> lesMeilleursScores = new ArrayList<Integer>();

        // pour chaque profil récupéré
        for (Profil profil : profils) {
            int minMScore = 999999999;
            int indiceMinMScore = 0;

            // cherche le plus petit score récupérer dans la liste des meilleurs scores
            for (int i = 0; i < lesMeilleursScores.size(); i++) {

                if (lesMeilleursScores.get(i) < minMScore) {
                    minMScore = lesMeilleursScores.get(i);
                    indiceMinMScore = i;
                }
            }

            // pour chaque partie dans le profil en cours
            for (Partie partie : profil.getParties()) {
                // Si la liste des meilleurs partie est < 5, on ajoute simplement le score et le nom de la première partie joué qu'on trouve
                if (lesMeilleursScores.size() < 5) {
                    lesNomAssociésAuMeilleursScores.add(profil.getNom());
                    lesMeilleursScores.add(partie.calScore());
                } else {
                    /* on compare le score de chaque partie au plus petit enregistré dans la liste des meilleurs scores
                        s'il est supérieur, on supprime le nom et le score à l'indice du score min et on ajoute cette partie en changeant indiceMin et le score min
                     */
                    if (partie.calScore() > minMScore) {
                        // supprime
                        lesNomAssociésAuMeilleursScores.remove(indiceMinMScore);
                        lesMeilleursScores.remove(indiceMinMScore);
                        // ajoute
                        lesNomAssociésAuMeilleursScores.add(profil.getNom());
                        lesMeilleursScores.add(partie.calScore());
                        // recalcule le min
                        for (int i = 0; i < lesMeilleursScores.size(); i++) {
                            if (lesMeilleursScores.get(i) < minMScore) {
                                minMScore = lesMeilleursScores.get(i);
                                indiceMinMScore = i;
                            }
                        }
                    }
                }
            }
        }

        /* Normalement arrivé ici on a deux listes avec des noms et des scores associés     
            on va maintenant créer un profil pour chaque score/nom dans la liste 
         */
        ArrayList<Profil> podium = new ArrayList<>();

        String maxSNom = "";
        int maxScore = 0;
        int indiceMaxScore = 0;
        /* Tant qu'il reste une valeur dans la liste */
        while (!lesMeilleursScores.isEmpty()) {
            int taille = lesMeilleursScores.size();
            maxScore = 0;

            for (int i = 0; i < taille; i++) {
                if (maxScore < lesMeilleursScores.get(i)) {
                    maxScore = lesMeilleursScores.get(i);
                    maxSNom = lesNomAssociésAuMeilleursScores.get(i);
                    indiceMaxScore = i;
                }
            }
            // ici on a trouvé le plus grand score parmis les meilleurs
            int j = 0;

            while (!maxSNom.equals(profils.get(j).getNom())) {
                j++;
            }
            Profil p = profils.get(j);
            Profil profTmp = new Profil(p.getNom(), p.getDateNaissance());

            int k = 0;
            while (p.getPartie(k).calScore() != maxScore) {
                k++;
            }

            Partie par = p.getPartie(k);
            Partie partTmp = new Partie(par.getDate(), par.getMot(), par.getNiveau(), par.getTrouvé(), par.getTemps());
            // on ajoute cette partie au profil temporaire
            profTmp.ajouterPartie(partTmp);

            podium.add(profTmp);

            lesMeilleursScores.remove(indiceMaxScore);
            lesNomAssociésAuMeilleursScores.remove(indiceMaxScore);

        }
        
        
        // Au passage on crée un fichier xml contenant les highscores
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.newDocument();

        // création de l'élément racine profil avec ses attributs (espaces de nom) 
        Element podiumElt = doc.createElement("pod:podium");
        String xmlnsXsi = "http://www.w3.org/2001/XMLSchema-instance";
        podiumElt.setAttribute("xmlns:xsi", xmlnsXsi);

        String xmlnsTux = "http://myGame/tux";
        podiumElt.setAttribute("xmlns:pod", xmlnsTux);
        doc.appendChild(podiumElt);
        /*      <th><b>Joueur</b></th>
                <th><b>Date</b></th>
                <th><b>Mot</b></th>
                <th><b>Niveau</b></th>
                <th><b>Temps</b></th>
                <th><b>Score</b></th> */

        for (Profil p: podium) {
            Element joueurElt = doc.createElement("pod:joueur");
            podiumElt.appendChild(joueurElt);
            
            Element nomJoueurElt = doc.createElement("pod:nom");
            nomJoueurElt.setTextContent(p.getNom());
            joueurElt.appendChild(nomJoueurElt);
            
            Element dateElt = doc.createElement("pod:date");
            dateElt.setTextContent(p.getParties().get(0).getDate());
            joueurElt.appendChild(dateElt);
            
            Element motElt = doc.createElement("pod:mot");
            motElt.setTextContent(p.getParties().get(0).getMot());
            joueurElt.appendChild(motElt);
            
            Element niveauElt = doc.createElement("pod:niveau");
            niveauElt.setTextContent(Integer.toString(p.getParties().get(0).getNiveau()));
            joueurElt.appendChild(niveauElt);
            
            Element tempsElt = doc.createElement("pod:temps");
            tempsElt.setTextContent(Integer.toString(p.getParties().get(0).getTemps()));
            joueurElt.appendChild(tempsElt);
            
            Element scoreElt = doc.createElement("pod:score");
            scoreElt.setTextContent(Integer.toString(p.getParties().get(0).calScore()));
            joueurElt.appendChild(scoreElt);
            
        }
              
        try {
            XMLUtil.DocumentTransform.writeDoc(doc, "src/data/xml/podium.xml");
        } catch (Exception ex) {
            System.out.println(ex);
        }
       
        
        return podium;
    }

    /**
     * Récupère tous les fichiers xml situés dans le répertoire profil
     * @return listeFichierXML String[] composé de tous les noms fichiers xml  
     */
    private String[] getProfilsFileList() {
        String[] listeFichiersXML;
        File f = new File("src/data/profil");
        listeFichiersXML = f.list();
        for (String fichier : listeFichiersXML) {
            if (!fichier.contains(".xml")) {

            }
        }
        return listeFichiersXML;
    }


    /**
     * Instancie et initialise une arrayList de profils en fonction des fichiers contenus dans le répertoire profil
     * @param fileList correspond à la liste de fichiers trouvés par la méthode getProfilsFileList();
     * @return profils ArrayList de Profil correspondant à tous les profils trouvés dans le répertoire profil
     */
    private ArrayList<Profil> getArrayListProfils(String[] fileList) {
        String chemin = "src/data/profil/";
        ArrayList<Profil> profils = new ArrayList<>();
        if (fileList == null) {
            // a voir 
        } else {
            //parcourir tous les profils de la liste de profils et créer puis ajouter un profil à la liste
            for (String fichier : fileList) {
                Profil profil = new Profil(chemin + fichier);
                profils.add(profil);
            }
        }
        return profils;
    }

    /**
     * Gère le menu principal et appelle la méthode menuPrincipal(), fait en sorte qu'il s'affiche tant que l'on ne souahite pas en sortir
     *
     * @throws java.lang.Exception
     */
    public void execute() throws Exception {
        MENU_VAL mainLoop;
        mainLoop = MENU_VAL.MENU_SORTIE;
        do {
            mainLoop = menuPrincipal();
        } while (mainLoop != MENU_VAL.MENU_SORTIE);
        // C'est ici qu'on sort du jeu donc le meilleur moment pour écrire le profil xml du joueur
        String filenameProfil = profil.getNom() + ".xml";
        profil.sauvegarder(filenameProfil);

        this.env.setDisplayStr("Au revoir !", 300, 30);
        env.exit();
    }

    /**
     * Gère les affichages et nettoyages du menu principal soit affiche avant que le joueur fasse un choix
     * 1. Charger un profil de joueur existant ? 
     * 2. Créer un nouveau joueur ?
     * 3. Ajouter un mot au dictionnaire ?
     * 4. Voir le podium des meilleurs scores ? 
     * 5. Sortir du jeu ?
     * 
     * puis redirige vers menuJeu() pour la suite des options que le joueur peut avoir à choisir avant de commencer un partie
     * 
     * @return MENU_VAL enum qui correspond à l'état du menu dans lequel on se trouve : MENU_SORTIE, MENU_CONTINUE, MENU_JOUE
     * @throws Exception 
     */
    private MENU_VAL menuPrincipal() throws Exception {

        MENU_VAL choix = MENU_VAL.MENU_CONTINUE;
        String nomJoueur;

        // restaure la room du menu
        env.setRoom(menuRoom);

        menuText.getText("Question").display();
        menuText.getText("Principal1").display();
        menuText.getText("Principal2").display();
        menuText.getText("Principal3").display();
        menuText.getText("Principal4").display();
        menuText.getText("Principal5").display();

        // vérifie qu'une touche 1, 2 ou 3 est pressée
        int touche = 0;
        while (!(touche == Keyboard.KEY_1 || touche == Keyboard.KEY_2 || touche == Keyboard.KEY_3 || touche == Keyboard.KEY_4 || touche == Keyboard.KEY_5)) {
            touche = env.getKey();
            env.advanceOneFrame();
        }

        menuText.getText("Question").clean();
        menuText.getText("Principal1").clean();
        menuText.getText("Principal2").clean();
        menuText.getText("Principal3").clean();
        menuText.getText("Principal4").clean();
        menuText.getText("Principal5").clean();

        // et décide quoi faire en fonction de la touche pressée
        switch (touche) {
            // -------------------------------------
            // Touche 1 : Charger un profil existant
            // -------------------------------------
            case Keyboard.KEY_1:
                // demande le nom du joueur existant
                nomJoueur = getNomJoueur();
                String path = "src/data/profil/";
                String fullPath = path + nomJoueur + ".xml";
                // charge le profil de ce joueur si possible
                if (profil.charge(nomJoueur)) {
                    profil = new Profil(fullPath);
                    choix = menuJeu(false);
                } else {
                    // demande sa date de naissance
                    String dateNaissance = getDateNaissanceJoueur();
                    // crée un profil avec le nom d'un nouveau joueur
                    if ("RetourMenuPrincipal".equals(dateNaissance)) {
                        choix = MENU_VAL.MENU_CONTINUE;
                    } else {
                        profil = new Profil(nomJoueur, dateNaissance);
                        choix = menuJeu(true);
                    }
                }
                break;
            // -------------------------------------
            // Touche 2 : Créer un nouveau joueur
            // -------------------------------------
            case Keyboard.KEY_2:
                // demande le nom du nouveau joueur
                nomJoueur = getNomJoueur();
                //vérifie si un profil existe ou pas
                if (profil.charge(nomJoueur)) {
                    choix = MENU_VAL.MENU_CONTINUE;
                } else {
                    // demande sa date de naissance
                    String dateNaissance = getDateNaissanceJoueur();
                    // crée un profil avec le nom d'un nouveau joueur
                    if ("RetourMenuPrincipal".equals(dateNaissance)) {
                        choix = MENU_VAL.MENU_CONTINUE;
                    } else {
                        profil = new Profil(nomJoueur, dateNaissance);
                        choix = menuJeu(true);
                    }
                }
                break;
            // -------------------------------------
            // Touche 3 : Ajouter un mot au dictionnaire
            // -------------------------------------
            case Keyboard.KEY_3:
                dico.lireDictionnaireDOM(dico.getCheminFichierDico(), "dico.xml");
                int niveau = getNiveau();
                String mot = getMot();
                dico.ajouterMot(mot, niveau);
                break;

            // -------------------------------------
            // Touche 4 : tableau des meilleurs scores
            // -------------------------------------
            case Keyboard.KEY_4:
                // ajout de l'option meilleurs scores dans le menuText
                ArrayList<Profil> podium = getPodium();
                int y = 280;
                int taille = podium.size();
                for (int i = 0; i < taille; i++) {
                    y -= 20;
                    String cle = "profil" + Integer.toString(i);
                    int numero = taille - i;
                    menuText.addText(Integer.toString(numero) + ". " + podium.get(i).getNom() + " - Score = " + podium.get(i).getPartie(0).calScore(), cle, 100, y);
                }
                menuText.addText("Appuyer sur 6 pour quitter le menu", "sortie", 200, y - 20);

                int nbProfils = podium.size();
                if (nbProfils > 5) {
                    for (int i = nbProfils; i > nbProfils - 5; i--) {
                        int numero = i;
                        numero--;
                        String cle = "profil" + Integer.toString(numero);
                        menuText.getText(cle).display();
                    }
                } else {
                    for (int i = 0; i < nbProfils; i++) {
                        String cle = "profil" + Integer.toString(i);
                        menuText.getText(cle).display();
                    }

                }
                menuText.getText("sortie").display();
                //verifie qu'une touche a été pressée 
                int key = 0;
                while (!(key == Keyboard.KEY_6)) {
                    key = env.getKey();
                    env.advanceOneFrame();
                }
                // nettoie l'environnement du texte
                for (int i = 0; i < nbProfils; i++) {
                    String cle = "profil" + Integer.toString(i);
                    menuText.getText(cle).clean();
                }
                menuText.getText("sortie").clean();
                // et décide quoi faire en fonction de la touche pressée
                switch (key) {
                    case Keyboard.KEY_6:
                        // peut être key
                        choix = MENU_VAL.MENU_CONTINUE;
                        break;
                }
                break;
            // -------------------------------------
            // Touche 5 : Sortir du jeu 
            // -------------------------------------
            case Keyboard.KEY_5:
                choix = MENU_VAL.MENU_SORTIE;
        }
        return choix;
    }

    /**
     * Récupère la date de naissance du joueur dans le cas d'un nouveau profil
     * Dans le cas ou le joueur entre un mauvais format de date, il a le choix de réessayer ou de retourner au menu principal. 
     * Si il choisit de retourner au menu principal la fonction renvoie "RetourMenuPrincipal", sinon elle renvoie un string format "yyyy-mm-dd"
     * @return dateNaissance String qui correspond à la date de naissance du joueur
     */
    private String getDateNaissanceJoueur() {
        String dateNaissance = "";
        boolean estValide = false;
        while (!estValide) {
            menuText.getText("AnnéeDeNaissance").display();
            dateNaissance = menuText.getText("AnnéeDeNaissance").lire(true);
            menuText.getText("AnnéeDeNaissance").clean();
            dateNaissance += "-";

            menuText.getText("MoisDeNaissance").display();
            dateNaissance += menuText.getText("MoisDeNaissance").lire(true);
            menuText.getText("MoisDeNaissance").clean();
            dateNaissance += "-";

            menuText.getText("JourDeNaissance").display();
            dateNaissance += menuText.getText("JourDeNaissance").lire(true);
            menuText.getText("JourDeNaissance").clean();

            estValide = formatDateValide(dateNaissance);

            if (!estValide) {
                menuText.getText("ErreurDateNaissance").display();
                menuText.getText("EntrerDateNaissanceANouveau").display();
                menuText.getText("RetourMenuPrincipal").display();

                int touche = 0;
                while (!(touche == Keyboard.KEY_1 || touche == Keyboard.KEY_2)) {
                    touche = env.getKey();
                    env.advanceOneFrame();
                }
                menuText.getText("ErreurDateNaissance").clean();
                menuText.getText("EntrerDateNaissanceANouveau").clean();
                menuText.getText("RetourMenuPrincipal").clean();

                if (touche == Keyboard.KEY_2) {
                    dateNaissance = "RetourMenuPrincipal";
                    estValide = true;
                }
            }
        }
        return dateNaissance;
    }

    /**
     * Récupère le niveau que le joueur rentre au moment de donner ce critère (ajout de mot dans le dictionnaire, niveau du mot qu'il veut jouer)
     * S'il ce n'est pas un nombre alors il est mis à 1 par défaut 
     * S'il n'est pas compris entre ]0-6[ alors il doit le rentrer à nouveau
     * @return niveau correspond au niveau que le joueur à rentré dans le menu 
     */
    private int getNiveau() {
        boolean estValide = false;
        int niveau = 0;
        while (!estValide) {
            menuText.getText("niv").display();

            try {
                niveau = Integer.parseInt(menuText.getText("niv").lire(true));
                if (niveau > 0 && niveau < 6) {
                    estValide = true;
                }

            } catch (NumberFormatException e) {
                niveau = 1;
            }
            menuText.getText("niv").clean();

        }
        return niveau;
    }

    /**
     * Récupère le mot que le joueur choisit au moment d'ajouter un mot au dictionnaire
     * @return mot String correspond au mot que le joueur ajoute au dictionnaire
     */
    private String getMot() {
        int taille;
        int i;
        boolean motValide;
        String mot = "";

        do {   
            menuText.getText("mot").display();
            mot = menuText.getText("mot").lire(true);
            menuText.getText("mot").clean();

            motValide = true;
            taille = mot.length();
            i = 0;

            while (i < taille && motValide) {
                if (mot.charAt(i) < 'a' || mot.charAt(i) > 'z') {
                    motValide = false;
                }
                i++;
            }
        } while (!motValide);

        return mot;
    }

    /**
     * Essaie de mettre la date entrée en paramètre au format aaaa-MM-jj et si c'est possible alors un boolean passe à true,
     * et indique que la date à un format valide sinon il reste à false; 
     * @param dateString
     * @return true/false boolean démendament de si la date est valide 
     */
    public static boolean formatDateValide(String dateString) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setLenient(false);
            Date d = format.parse(dateString);
            return format.format(d).equals(dateString);
        } catch (Exception e) {
            return false;
        }
    }
    /**
     * vérifie que l'index de la partie cherchée dans un profil est bien compris entre le 0 et le nombre de parties qu'il à joué
     * @param index correspond à l'index à valider
     * @return estValide boolean qui indique true si l'index est contenu dans les parties du profil du joueur, false sinon
     */
    public boolean estIndexValide(int index) {
        boolean estValide = false;
        if (index > 0 && index < profil.getParties().size()) {
            estValide = true;
        }
        return estValide;
    }

    /**
     * Charge une partie du joueur en utilisant la methode getPartie(index) et  joue(p, true, index)
     * @param index correspond à l'index de la partie que le joueur veut charger
     * @throws Exception 
     */
    public void chargePartie(int index) throws Exception {
        Partie p = profil.getPartie(index);
        // joue
        joue(p, true, index);
    }

    /**
     * Charge la dernière partie jouée spécifiquement
     * @throws Exception 
     */
    public void chargeDernierePartie() throws Exception {
        if (profil.getParties() != null) {
            int index = profil.getParties().size() - 1;
            Partie p = profil.getPartie(index);
            // joue
            joue(p, true, index);
        } else {
            // partie par défaut 
            Partie p = new Partie(dateDuJour(), "alors", 1);
            joue(p, false, -1);
        }

    }

    /**
     * Gère les menus suivants, après le choix dans le menu principal : 
     * 1 : Commencer une nouvelle partie
     * 2 : Charger une partie existante
     *      1 : charger la derniere partie
     *      2 : Charger la derniere partie -1
     *      3 : Charger la derniere partie -2
     *      4 : Charger la derniere partie -3
     *      5 : Charger la derniere partie -4
     * 3 : Sortie de ce jeu
     * 4 : Quitter le jeu
     * 
     * @param estNouveauJoueur correspond à la valeur qui va autoriser ou non de charger une partie existante car un nouveau joueur n'en a pas 
     * @return
     * @throws Exception 
     */
    private MENU_VAL menuJeu(boolean estNouveauJoueur) throws Exception {
        boolean modifiePartie = false;
        int index = -1;
        MENU_VAL playTheGame;
        playTheGame = MENU_VAL.MENU_JOUE;
        Partie partie;
        do {
            // restaure la room du menu
            env.setRoom(menuRoom);
            // affiche menu
            menuText.getText("Question").display();
            menuText.getText("Jeu1").display();
            if (!estNouveauJoueur) {
                menuText.getText("Jeu2").display();
                menuText.getText("Jeu3").display();
                menuText.getText("Jeu4").display();
            } else {
                // 2 Sortir
                // 3 quitter
                menuText.getText("sortirNJ").display();
                menuText.getText("quitterNJ").display();
            }

            // vérifie qu'une touche 1, 2, 3 ou 4 est pressée
            int touche = 0;
            while (!(touche == Keyboard.KEY_1 || touche == Keyboard.KEY_2 || touche == Keyboard.KEY_3 || touche == Keyboard.KEY_4)) {
                touche = env.getKey();
                env.advanceOneFrame();
            }

            // nettoie l'environnement du texte
            menuText.getText("Question").clean();
            menuText.getText("Jeu1").clean();
            menuText.getText("Jeu2").clean();
            menuText.getText("Jeu3").clean();
            menuText.getText("Jeu4").clean();
            menuText.getText("sortirNJ").clean();
            menuText.getText("quitterNJ").clean();

            // et décide quoi faire en fonction de la touche pressée
            switch (touche) {
                // -----------------------------------------
                // Touche 1 : Commencer une nouvelle partie
                // -----------------------------------------                
                case Keyboard.KEY_1: // choisit un niveau et charge un mot depuis le dico
                    // Demande au joueur le niveau du mot qu'il veut
                    int niveauChoisi = getNiveauDuMot();
                    // Pioche au hasard un mot de ce niveau
                    String motJoué = dico.getMotDepuisListeNiveaux(niveauChoisi);
                    // Affichage pour le joueur du mot qu'il va devoir deviner
                    afficheMotJoué(motJoué);
                    // crée un nouvelle partie
                    partie = new Partie(dateDuJour(), motJoué, niveauChoisi);
                    // joue
                    joue(partie, modifiePartie, index);
                    // enregistre la partie dans le profil --> enregistre le profil
                    playTheGame = MENU_VAL.MENU_JOUE;
                    break;

                // -----------------------------------------
                // Touche 2 : Charger une partie existante
                // -----------------------------------------   
                // permettre de charger la partie correspondante 
                case Keyboard.KEY_2:
                    if (estNouveauJoueur || profil.getParties() == null || profil.getParties().size() == 0) {
                        playTheGame = MENU_VAL.MENU_CONTINUE;
                    } else {
                        // ajout de l'option charger une partie dans le texte des menus
                        menuTextInitJeu2();
                        // choisir laquelle (afficher mots, temps, trouvé ...)
                        menuText.getText("Question").display();
                        int nbParties = profil.partiesToString().size();
                        // affiche les 5 dernières parties jouées 
                        if (nbParties > 5) {
                            for (int i = nbParties; i > nbParties - 5; i--) {
                                int numero = i;
                                numero--;
                                String cle = "partie" + Integer.toString(numero);
                                menuText.getText(cle).display();
                            }
                        } else {
                            for (int i = 0; i < nbParties; i++) {
                                String cle = "partie" + Integer.toString(i);
                                menuText.getText(cle).display();
                            }

                        }
                        menuText.getText("sortie").display();
                        //verifie qu'une touche a été pressée 
                        int choix = 0;
                        while (!(choix == Keyboard.KEY_1
                                || choix == Keyboard.KEY_2
                                || choix == Keyboard.KEY_3
                                || choix == Keyboard.KEY_4
                                || choix == Keyboard.KEY_5
                                || choix == Keyboard.KEY_6)) {
                            choix = env.getKey();
                            env.advanceOneFrame();
                        }
                        // nettoie l'environnement du texte
                        menuText.getText("Question").clean();
                        for (int i = 0; i < nbParties; i++) {
                            String cle = "partie" + Integer.toString(i);
                            menuText.getText(cle).clean();
                        }
                        menuText.getText("sortie").clean();
                        // et décide quoi faire en fonction de la touche pressée
                        switch (choix) {
                            // -----------------------------------------
                            // Touche 1 : charger la derniere partie
                            // -----------------------------------------                
                            case Keyboard.KEY_1:
                                //appel a une methode qui charge la partie avec le bon index
                                index = profil.getParties().size() - 1;
                                if (estIndexValide(index)) {
                                    chargePartie(index);
                                } else {
                                    chargeDernierePartie();
                                }
                                index = -1;
                                playTheGame = MENU_VAL.MENU_JOUE;
                                break;
                            // -----------------------------------------
                            // Touche 2 : Charger la derniere partie -1
                            // -----------------------------------------                
                            case Keyboard.KEY_2:
                                index = profil.getParties().size() - 2;
                                if (estIndexValide(index)) {
                                    chargePartie(index);
                                } else {
                                    chargeDernierePartie();
                                }
                                index = -1;
                                playTheGame = MENU_VAL.MENU_JOUE;
                                break;

                            // -----------------------------------------
                            // Touche 3 : Charger la derniere partie -2
                            // -----------------------------------------                
                            case Keyboard.KEY_3:
                                index = profil.getParties().size() - 3;
                                if (estIndexValide(index)) {
                                    chargePartie(index);
                                } else {
                                    chargeDernierePartie();
                                }
                                index = -1;
                                playTheGame = MENU_VAL.MENU_JOUE;
                                break;

                            // -----------------------------------------
                            // Touche 4 : Charger la derniere partie -3
                            // -----------------------------------------                
                            case Keyboard.KEY_4:
                                index = profil.getParties().size() - 4;
                                if (estIndexValide(index)) {
                                    chargePartie(index);
                                } else {
                                    chargeDernierePartie();
                                }
                                index = -1;
                                playTheGame = MENU_VAL.MENU_JOUE;
                                break;
                            // -----------------------------------------
                            // Touche 5 : Charger la derniere partie -4
                            // -----------------------------------------                
                            case Keyboard.KEY_5:
                                index = profil.getParties().size() - 5;
                                if (estIndexValide(index)) {
                                    chargePartie(index);
                                } else {
                                    chargeDernierePartie();
                                }
                                index = -1;
                                playTheGame = MENU_VAL.MENU_JOUE;
                                break;
                            case Keyboard.KEY_6:
                                playTheGame = MENU_VAL.MENU_CONTINUE;
                                break;
                        }
                    }

                    break;

                // -----------------------------------------
                // Touche 3 : Sortie de ce jeu
                // -----------------------------------------                
                case Keyboard.KEY_3:
                    if (estNouveauJoueur) {
                        playTheGame = MENU_VAL.MENU_SORTIE;
                    } else {
                        playTheGame = MENU_VAL.MENU_CONTINUE;
                    }
                    break;
                // -----------------------------------------
                // Touche 4 : Quitter le jeu
                // -----------------------------------------                
                case Keyboard.KEY_4:
                    playTheGame = MENU_VAL.MENU_SORTIE;
            }
        } while (playTheGame == MENU_VAL.MENU_JOUE);

        return playTheGame;
    }

    /**
     * Gère l'affichage pendant un certain temps du mot que le joueur devra trouver ensuite lors de sa partie effective
     * @param motJoué correspond au mot qu'il doit trouver 
     * @throws InterruptedException 
     */
    private void afficheMotJoué(String motJoué) throws InterruptedException  {
        String s = "Vous devez trouver le mot : " + motJoué;
        menuText.addText(s, "MotATrouver", 100, 300);
        menuText.getText("MotATrouver").display();
        env.advanceOneFrame();
        menuText.getText("MotATrouver").clean();
        Thread.sleep(3000);

    }

    /**
     * Récupère à l'écran le niveau que le joueur à choisi pour sa partie et vérifie qu'il est bien entre [1-5]
     * @return s int correspond au niveau du mot choisi par le joueur 
     * @throws InterruptedException 
     */
    private int getNiveauDuMot() throws InterruptedException {
        String s = "";
        int niveau = -1;

        while (niveau < 1 || niveau > 5) {
            menuText.getText("ChoixDuNiveau").display();
            s = menuText.getText("ChoixDuNiveau").lire(true);
            env.advanceOneFrame();
            menuText.getText("ChoixDuNiveau").clean();

            try {
                niveau = Integer.parseInt(s);
                if (niveau < 1 || niveau > 5) {
                    menuText.getText("ErreurChoixDuNiveau").display();
                    env.advanceOneFrame();
                    Thread.sleep(1000);
                    menuText.getText("ErreurChoixDuNiveau").clean();
                }
            } catch (Exception e) {
                menuText.getText("ErreurChoixDuNiveau").display();
                env.advanceOneFrame();
                Thread.sleep(1000);
                menuText.getText("ErreurChoixDuNiveau").clean();
            }

            //env.advanceOneFrame();
        }
        return Integer.parseInt(s);

    }

    /**
     * Crée l'environnement (pièce, personnage, lettres...) de jeu dans lequel le joueur joue sa partie 
     * @param partie correspond à la partie qu'il a choisi de jouer
     * @param modifiePartie correspond à s'il modifie une partie existante :true ou false si c'est une nouvelle partie 
     * @param index correspond à l'index de la partie que le joueur refait s'il en refait une, sinon, on ne s'en sert pas
     * @throws Exception 
     */
    public void joue(Partie partie, boolean modifiePartie, int index) throws Exception {

        // restaure la room du jeu
        env.setRoom(mainRoom);

        // Instancie un Tux
        tux = new Tux(env, mainRoom);
        env.addObject(tux);

        /* Dans le cas ou il rejoue une partie, il faut remettre à false la variable estOccupé pour chaque coordonnée dans positionLettres
        avant d'ajouter les nouvelles lettres du nouveau mot */
        for (int i = 0; i < positionLettres.size(); i++) {
            positionLettres.get(i).setEstOccupé(false);
        }
        // Ajoute les lettres du mot pioché au hasard dans la liste des lettres et les places sur le terrain 
        AjouteLettresDuMot(env, partie.getMot());

        // Ici, on peut initialiser des valeurs pour une nouvelle partie
        démarrePartie(partie);

        // Boucle de jeu
        Boolean finished;
        finished = false;

        while (!lettres.isEmpty() && !finished) {

            // Contrôles globaux du jeu (sortie, ...)
            //1 is for escape key
            if (env.getKey() == 1) {
                finished = true;
            }

            // Contrôles des déplacements de Tux (gauche, droite, ...)
            tux.deplace(lettres);

            // Ici, on applique les regles
            appliqueRegles(partie);

            // Fait avancer le moteur de jeu (mise à jour de l'affichage, de l'écoute des événements clavier...)
            env.advanceOneFrame();
        }

        // Ici on peut calculer des valeurs lorsque la partie est terminée
        terminePartie(partie);
        if (modifiePartie) {
            //remplacer partie
            profil.remplacerPartie(partie, index);
        } else {
            // On ajoute la partie dans la liste des parties du joueur
            profil.ajouterPartie(partie);
        }
        /* Dans le cas ou la personne à cliqué sur échap au cours d'un partie et qu'il en commence une nouvelle derrière, il faut 
        il faut penser à vider la liste des lettres du mot précédent */
        resetListDeLettres();
    }

    public ArrayList<Letter> getLettres() {
        return lettres;
    }

    private String getNomJoueur() {
        String nomJoueur = "";
        menuText.getText("NomJoueur").display();
        nomJoueur = menuText.getText("NomJoueur").lire(true);
        menuText.getText("NomJoueur").clean();
        return nomJoueur;
    }
    
    /**
     * Enlève les premières lettres quand le joueur trouve les lettres dans l'ordre 
     */
    protected void removeFirstLettres() {
        // Si la liste de lettres n'est pas vide(Normalement on l'appelle que quand il reste des lettres mais on est jamais trops prudent
        if (!lettres.isEmpty()) {
            removeLetterFromEnv(lettres.get(0));
            lettres.remove(0);
        }
    }

    protected void resetListDeLettres() {
        lettres.clear();
    }

    /**
     * Supprime le cube associé à une lettre le plus proche de la position du personnage
     * @param l correspond à la lettre à enlever de l'environnement de jeu
     */
    private void removeLetterFromEnv(Letter l) {
        env.removeObject(l);
    }

    /**
     *  Remplit la liste positionLettres de 25 emplacements possibles de lettres. 
     */
    public void initListPositionLetter() {
        // On part du principe que le plateau de jeu fait 100 en largeur et 100 en profondeur. Il faudra adapter cette génération si on change le standard
        double widthStep = mainRoom.getWidth() / 6.0;
        double depthStep = mainRoom.getDepth() / 6.0;
        double larg;
        double prof;
        int k = 0;
        while (k < 25) {
            larg = widthStep;
            prof = depthStep;
            while (k >= 0 && k <= 4) {
                Coordonnee c = new Coordonnee(larg, prof);
                positionLettres.add(c);
                larg += widthStep;
                k++;
            }

            larg = widthStep;
            prof += depthStep;
            while (k >= 5 && k <= 9) {
                Coordonnee c = new Coordonnee(larg, prof);
                positionLettres.add(c);
                larg += widthStep;
                k++;
            }

            larg = widthStep;
            prof += depthStep;
            while (k >= 10 && k <= 14) {
                Coordonnee c = new Coordonnee(larg, prof);
                positionLettres.add(c);
                larg += widthStep;
                k++;
            }

            larg = widthStep;
            prof += depthStep;
            while (k >= 15 && k <= 19) {
                Coordonnee c = new Coordonnee(larg, prof);
                positionLettres.add(c);
                larg += widthStep;
                k++;
            }

            larg = widthStep;
            prof += depthStep;
            while (k >= 20 && k <= 24) {
                Coordonnee c = new Coordonnee(larg, prof);
                positionLettres.add(c);
                larg += widthStep;
                k++;
            }
        }
    }


    /**
     * Ajoute les caractères du mot dans la liste puis sur le terrain à une position aléatoire (en évitant les collisions) 
     * @param env correspond à l'environnement du jeu
     * @param mot correspond au mot que le joueur va chercher
     */
    private void AjouteLettresDuMot(Env env, String mot) {
        // Variables aléatoires qui roll en prenant en compte le scaling d'un cube 
        Random rand = new Random(0.0, 24.0);

        // Pour chaque caractère du mot 
        for (int i = 0; i < mot.length(); i++) {
            boolean flag = true;
            int roll;

            // Tant que la lettre n'est pas à un emplacement valide
            while (flag) {
                roll = (int) rand.get(); // roll un nombre entre 0 et 26
                /* si l'emplacement est libre dans le tableau des positions possibles des lettres, alors on place cette lettre à cette position
                et on passe le boolean estOccupé à true */
                if (!positionLettres.get(roll).estOccupé()) {
                    positionLettres.get(roll).setEstOccupé(true);
                    Letter lettre = new Letter(mot.charAt(i), positionLettres.get(roll).getWidth(), positionLettres.get(roll).getDepth()); // Bon là ça les pose dans l'ordre cote à cote donc naze
                    lettres.add(lettre);
                    flag = false;
                }
            }
        }

        for (Letter l : lettres) {
            env.addObject(l);
        }
    }

    /**
     * Utilise la méthode native à Env3D pour calculer la distance entre les lettres et Tux
     * @param letter correspond à la lettre à partir de laquelle on veut calculer la distance par rapport au personnage Tux
     * @return distance double qui correspond à la distance effective entre la lettre et Tux
     */
    protected double distance(Letter letter) {
        // √ (x1 − x2 )2 + (y1 − y2 )2 = distance entre 2 points
        //double d = Math.sqrt(Math.pow(tux.getX() - letter.getX(), 2) + Math.pow(tux.getY() - letter.getY(), 2)); 
        return tux.distance(letter) - (tux.getScale() / 2 + letter.getScale() / 2);
    }

    /**
     * Vérifie si le personnage entre en collision avec la lettre
     * @param letter correspond à la lettre à partir de laquelle on veut calculer la collision par rapport au personnage Tux
     * @return estEnCollision boolean qui renvoie true s'il y a collision avec la lettre, false sinon
     */
    protected boolean collision(Letter letter) {
        boolean estEnCollision = false;
        double distance = tux.distance(letter);
        double taille = (letter.getScale() + 1) / 2 + (tux.getScale() + 1) / 2;
        double marge = 0.5;
        // La collision ne peut se faire que si le personnage est dans les airs au dessus de la lettre
        if (tux.getY() > (tux.getScale() * 1.1) + 1) {
            if ((tux.getY() >= letter.getScale() + letter.getY() + 1) && (distance <= letter.getScale() + tux.getScale() + marge)) {
                estEnCollision = true;
            }
        }

        return estEnCollision;
    }

    /**
     * Donne la date du jour au format yyyy-mm-dd
     * @return la date du jour au format yyyy-mm-dd
     */
    public static String dateDuJour() {
        LocalDate dateObj = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = dateObj.format(formatter);
        return date;
    }

    protected abstract void démarrePartie(Partie partie);

    protected abstract void appliqueRegles(Partie partie);

    protected abstract void terminePartie(Partie partie);

}
