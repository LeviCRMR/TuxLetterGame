<?xml version="1.0" encoding="UTF-8"?>
<!--
@auteurs Damien TORNAMBE Levi CORMIER

Transforme un profil au format XML en profil au format HTML

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:tux="http://myGame/tux">
    <xsl:output method="html"/>

    <xsl:template match="/">
        <html>
            <head>
                <title>Profil du joueur</title>
            </head>
            <body>
                <h1> 
                    <xsl:value-of select="tux:profil/tux:nom"/>
                </h1>
                <!-- Définition de la balise html img src="image.jpg" alt="erreur 42" pour afficher l'avatar du joueur-->
                <xsl:element name="img">
                    <xsl:attribute name="src">
                        <xsl:value-of select="tux:profil/tux:avatar"/>
                    </xsl:attribute>
                    <xsl:attribute name="alt">
                        erreur 42
                    </xsl:attribute>
                </xsl:element>      
                <h2>Anniversaire : <xsl:value-of select="tux:profil/tux:anniversaire"/></h2> 
                <table border="1">
                    <xsl:call-template name="header"/>
                    <xsl:apply-templates select="tux:profil/tux:parties/tux:partie">
                        <xsl:sort select="@date"/>
                    </xsl:apply-templates>
                </table>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template name="header">
        <tr>
            <th colspan="5" align="center"> Parties </th>
        </tr>
        <tr>  
            <th> Date </th>
            <th> Temps (sec) </th>
            <th> Mot </th>
            <th> Niveau </th>
            <th> Trouvé (%age) </th>
        </tr>
    </xsl:template>
    
    <xsl:template match="tux:partie">
        <tr>
            <td> <xsl:value-of select="@date"/> </td>
            <td> <xsl:value-of select="tux:temps/text()"/> </td>
            <td> <xsl:value-of select="tux:niveau/tux:mot/text()"/> </td>
            <td> <xsl:value-of select="tux:niveau/@valeur"/> </td>
            <td> <xsl:value-of select="@trouvé"/> </td>
        </tr> 

    </xsl:template>

</xsl:stylesheet>
