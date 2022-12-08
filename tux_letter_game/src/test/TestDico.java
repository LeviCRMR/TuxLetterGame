/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import game.Dico;
import java.io.File;

/**
 *
 * @author tornambe
 */
public class TestDico {

    public static void main(String[] args) throws Exception {

        Dico dico = new Dico("src/data/xml/");
        dico.lireDictionnaireDOM("src/data/xml/", "dico.xml");

        System.out.println("Mot de niveau 1 tiré: " + dico.getMotDepuisListeNiveaux(1));
        System.out.println("Mot de niveau 1 tiré: " + dico.getMotDepuisListeNiveaux(1));
        System.out.println("Mot de niveau 1 tiré: " + dico.getMotDepuisListeNiveaux(1));
        System.out.println("Mot de niveau 1 tiré: " + dico.getMotDepuisListeNiveaux(1));
        System.out.println("Mot de niveau 1 tiré: " + dico.getMotDepuisListeNiveaux(1));
        System.out.println("Mot de niveau 1 tiré: " + dico.getMotDepuisListeNiveaux(1));
        System.out.println("Mot de niveau 2 tiré: " + dico.getMotDepuisListeNiveaux(2));
        System.out.println("Mot de niveau 2 tiré: " + dico.getMotDepuisListeNiveaux(2));
        System.out.println("Mot de niveau 2 tiré: " + dico.getMotDepuisListeNiveaux(2));
        System.out.println("Mot de niveau 2 tiré: " + dico.getMotDepuisListeNiveaux(2));
        System.out.println("Mot de niveau 3 tiré: " + dico.getMotDepuisListeNiveaux(3));
        System.out.println("Mot de niveau 3 tiré: " + dico.getMotDepuisListeNiveaux(3));
        System.out.println("Mot de niveau 3 tiré: " + dico.getMotDepuisListeNiveaux(3));
        System.out.println("Mot de niveau 3 tiré: " + dico.getMotDepuisListeNiveaux(3));
        System.out.println("Mot de niveau 4 tiré: " + dico.getMotDepuisListeNiveaux(4));
        System.out.println("Mot de niveau 4 tiré: " + dico.getMotDepuisListeNiveaux(4));
        System.out.println("Mot de niveau 4 tiré: " + dico.getMotDepuisListeNiveaux(4));
        System.out.println("Mot de niveau 4 tiré: " + dico.getMotDepuisListeNiveaux(4));
        System.out.println("Mot de niveau 5 tiré: " + dico.getMotDepuisListeNiveaux(5));
        System.out.println("Mot de niveau 5 tiré: " + dico.getMotDepuisListeNiveaux(5));
        System.out.println("Mot de niveau 5 tiré: " + dico.getMotDepuisListeNiveaux(5));
        System.out.println("Mot de niveau 5 tiré: " + dico.getMotDepuisListeNiveaux(5));
        System.out.println("Mot de niveau 6 tiré: " + dico.getMotDepuisListeNiveaux(6));
        System.out.println("Mot de niveau -1 tiré: " + dico.getMotDepuisListeNiveaux(-1));
        System.out.println("Mot de niveau -120 tiré: " + dico.getMotDepuisListeNiveaux(-120));
        System.out.println("Mot de niveau 247 tiré: " + dico.getMotDepuisListeNiveaux(247));

        Dico dicoSAX = new Dico("src/data/xml/");
        dicoSAX.lireDictionnaireSAX("src/data/xml/", "dico.xml");

        dico.ecrireDOM("test.xml");
        dico.ajouterMot("testajoutmot", 1);

        // ------------------------------------------------------------------------------------------------------------------------------
        System.out.println("début traitement : ");
        // Creates an array in which we will store the names of files and directories
        String[] pathnames;

        // Creates a new File instance by converting the given pathname string
        // into an abstract pathname
        File f = new File("src/data/profil");

        // Populates the array with names of files and directories
        pathnames = f.list();

        // For each pathname in the pathnames array
        for (String pathname : pathnames) {
            if(pathname.contains(".xml")){
            // Print the names of xml files
            System.out.println(pathname);
            }
        }
    }
}
