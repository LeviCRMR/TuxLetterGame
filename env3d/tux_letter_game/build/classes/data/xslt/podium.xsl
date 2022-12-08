<?xml version="1.0" encoding="UTF-8"?>



<!--
@auteurs Damien TORNAMBE Levi CORMIER

Transforme les meilleurs scores au format XML en meilleurs scores au format HTML

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:pod="http://myGame/tux">
    <xsl:output method="html"/>


    <xsl:template match="/">
        <html>
            <head>
                <title>Meilleurs Scores</title>
            </head>
            <body>
                <table style="width:40%" border="1">

                    <xsl:apply-templates select="pod:podium/pod:joueur"/>

                </table>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template match="pod:joueur">
        <tr>
            <th>
                <b>
                    <xsl:value-of select="pod:nom"/>
                </b>
            </th>
            <th>
                <b>
                    <xsl:value-of select="pod:date"/>
                </b>
            </th>
            <th>
                <b>
                    <xsl:value-of select="pod:mot"/>
                </b>
            </th>
            <th>
                <b>
                    <xsl:value-of select="pod:niveau"/>
                </b>
            </th>
            <th>
                <b>
                    <xsl:value-of select="pod:temps"/>
                </b>
            </th>
            <th>
                <b>
                    <xsl:value-of select="pod:score"/>
                </b>
            </th>
        </tr>
    </xsl:template>

</xsl:stylesheet>

