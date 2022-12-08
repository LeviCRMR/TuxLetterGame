<?xml version="1.0" encoding="UTF-8"?>

<!-- 
@auteurs Damien TORNAMBE Levi CORMIER
Ce fichier XSLT s'applique sur le fichier dico.xml. 
Il permet la création d'un tableau qui regroupe:
    - Un header contenant Niveau + numéroNiveau (dans l'ordre croissant en commençant par ). Sachant
      Qu'on a actuellement 5 niveaux de difficulté.
    - Un nombre n de ligne basé sur le nombre n de mots par niveau (le même dans chaque niveau). 
      Sur chaque ligne i (allant de 1 à n), on placera le ième mot présent dans les balises niveaux, dans la colonne associée
      à son attribut valeur du niveau.
Pour ce faire, nous avons 2 templates nommées "pourChaqueMot" et
"pourChaqueNiveau". Le principe est que l'on va rappeller récursivement ces deux templates
("pourChaqueNiveau" est imbriquée dans la template "pourChaqueMot"), pour placer au bon endroit chaque mot de chaque niveau
un par un. 
Dans les templates récursives, les push/navigations/pull sont séparés de l'appel récursif, pour plus de 
lisibilité.-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:tux="http://myGame/tux">
    
    <xsl:output method="html"/>
    <!-- On se permet une petite simplicité en créant des variables globales pour le nombre de niveau
    le nombre de mot par niveau (tous les niveaux ont le même nombre de mot) -->
    <xsl:variable name="nbNiveau" select="count(/tux:dictionnaire/tux:niveau)"/>
    <xsl:variable name="nbMot" select="count(/tux:dictionnaire/tux:niveau[1]/tux:mot)"/>   
     
    <xsl:template match="/">
        <html>
            <head>
                <title>Dictionnaire du jeu</title>
            </head>
            <body>
                <table border="1">
                    <tr>
                        <xsl:call-template name="header"> <!-- Gère l'écriture du header du tableau -->
                            <xsl:with-param name="i" select="1"/>
                        </xsl:call-template>
                    </tr>
                    <xsl:call-template name="pourChaqueMot"> <!-- Point de départ de l'écriture des autres lignes du tableau -->
                        <xsl:with-param name="incrMot" select="1"/> 
                        <xsl:with-param name="countMot" select="$nbMot"/>
                    </xsl:call-template>
                </table>
            </body>
        </html>
    </xsl:template>
    
    <!-- Définition de la template header qui prend 1 paramètre:
    - i: entier qui va de 1 à nbNiveau -->
    <xsl:template name="header">
        <xsl:param name="i"/>
        <!-- Ecriture des colonnes du header du tableau (tant que i <= nbNiveau) -->
        <xsl:if test="$i &lt;= $nbNiveau"> 
            <th> Niveau <xsl:value-of select="$i"/> </th>
        </xsl:if>
        <!-- Appel récursif sur cette template en incrémentant de 1 le paramètre i (tant que i <= nbNiveau) -->
        <xsl:if test="$i &lt;= $nbNiveau"> 
            <xsl:call-template name="header">
                <xsl:with-param name="i">
                    <xsl:value-of select="$i + 1"/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <!-- Définition de la template pourChaqueMot qui prend 1 paramètres:
    - incrMot: un entier qui va de 1 à nbMot -->
    <xsl:template name="pourChaqueMot">
        <xsl:param name="incrMot"/> 
        <!-- Pour chaque mot présent dans un niveau (et par pour l'ensemble des mots du dictionnaire), on appelle la template 
        'pourChaqueNiveau' qui va naviguer sur tous les niveaux -->
        <xsl:if test="$incrMot &lt;= $nbMot">
            <tr>
                <xsl:call-template name="pourChaqueNiveau">
                    <xsl:with-param name="incrNiveau" select="1"/>
                    <xsl:with-param name="incrMot" select="$incrMot"/>
                </xsl:call-template>
            </tr>
        </xsl:if>
        <!-- Appel récursif sur cette template en incrémentant de 1 le paramètre incrMot (tant que incrMot <= nbMot) -->
        <xsl:if test="$incrMot &lt;= $nbMot">
            <xsl:call-template name="pourChaqueMot">
                <xsl:with-param name="incrMot">
                    <xsl:value-of select="$incrMot + 1"/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>            
    </xsl:template> 
    
    <!-- Définition de la template pourChaqueNiveau qui prend 2 paramètres:
    - incNiveau: un entier qui va de 1 à nbNiveau.
    - incrMot: un entier qui va de 1 à nbMot. -->
    <xsl:template name="pourChaqueNiveau">
        <xsl:param name="incrNiveau"/>
        <xsl:param name="incrMot"/>
        <!-- Appel à la template tux:niveau (tant que incrNiveau <= nbNiveau) -->
        <xsl:if test="$incrNiveau &lt;= $nbNiveau">
            <xsl:apply-templates select="tux:dictionnaire/tux:niveau">
                <xsl:with-param name="incrMot" select="$incrMot"/>
                <xsl:with-param name="incrNiveau" select="$incrNiveau"/>
                <xsl:sort select="@valeur"/>           
            </xsl:apply-templates>  
        </xsl:if>
        
        <!-- Appel récursif sur cette template en incrémentant de 1 le paramètre incrNiveau (tant que incrNiveau <= nbNiveau) -->
        <xsl:if test="$incrNiveau &lt;= $nbNiveau">
            <xsl:call-template name="pourChaqueNiveau">
                <xsl:with-param name="incrNiveau">
                    <xsl:value-of select="$incrNiveau + 1"/>
                </xsl:with-param>
                <xsl:with-param name="incrMot">
                    <xsl:value-of select="$incrMot"/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>            
    </xsl:template> 

    <!-- Définition de la template niveau qui prend 2 paramètres:
       - incNiveau: un entier qui va de 1 à nbNiveau.
       - incrMot: un entier qui va de 1 à nbMot.
    Si on se trouve sur le noeud niveau dont la valeur position() est égale au paramètre incrNiveau, on 
    appelle la template tux:mot -->
    <xsl:template match="tux:niveau">
        <xsl:param name="incrMot"/>
        <xsl:param name="incrNiveau"/>
        <xsl:if test="position() = $incrNiveau">
            <td>
                <xsl:apply-templates select="tux:mot">
                    <xsl:with-param name="incrMot" select="$incrMot"/>
                    <xsl:sort select="text()"/>
                </xsl:apply-templates>
            </td>
        </xsl:if>
    </xsl:template>
    
    <!-- Définition de la template mot qui prend 1 paramètre:
       - incrMot: un entier qui va de 1 à nbMot.
    Si on se trouve sur le noeud mot dont la valeur position() est égale au paramètre incrMot, on
    pull sa valeur -->
    <xsl:template match="tux:mot">
        <xsl:param name="incrMot"/>
        <xsl:if test="position() = $incrMot"> 
            <xsl:value-of select="text()"/> 
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>

