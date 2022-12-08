package game;

import java.io.IOException;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Gère l'environnement de jeu de Tux : 
 * textures, taille (profondeur, longueur, largeur)
 * project properties JDK 18 => JDK 8
 * @author Damien TORNAMBE
 * @author Levi CORMIER
 * 
 * 
 */
public class Room {

    private int depth;
    private int height;
    private int width;
    private String textureBottom;
    private String textureNorth;
    private String textureEast;
    private String textureWest;
    private String textureTop;
    private String textureSouth;

    /**
     * Parse des instances xml liées au schéma plateau.xsd en utilisant des chemins xpath
     * @param filename
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException 
     */
    public Room(String filename) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        String fullPath = "src/data/xml/" + filename;
        Document doc = builder.parse(fullPath);

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        xpath.setNamespaceContext(new NamespaceContext() {

            @Override
            public Iterator getPrefixes(String arg0) {
                return null;
            }

            @Override
            public String getPrefix(String arg0) {
                return null;
            }

            @Override
            public String getNamespaceURI(String arg0) {
                if ("room".equals(arg0)) {
                    return "http://myGame/tux";
                }
                return null;
            }
        });

        this.depth = Integer.parseInt((String) xpath.evaluate("room:plateau/room:dimensions/room:depth/text()", doc, XPathConstants.STRING));
        this.height = Integer.parseInt((String) xpath.evaluate("room:plateau/room:dimensions/room:height/text()", doc, XPathConstants.STRING));
        this.width = Integer.parseInt((String) xpath.evaluate("room:plateau/room:dimensions/room:width/text()", doc, XPathConstants.STRING));

        this.textureBottom = (String) xpath.evaluate("room:plateau/room:mapping/room:textureBottom/text()", doc, XPathConstants.STRING);
        this.textureEast = (String) xpath.evaluate("room:plateau/room:mapping/room:textureEast/text()", doc, XPathConstants.STRING);
        this.textureNorth = (String) xpath.evaluate("room:plateau/room:mapping/room:textureNorth/text()", doc, XPathConstants.STRING);
        this.textureWest = (String) xpath.evaluate("room:plateau/room:mapping/room:textureWest/text()", doc, XPathConstants.STRING);
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getTextureBottom() {
        return textureBottom;
    }

    public void setTextureBottom(String textureBottom) {
        this.textureBottom = textureBottom;
    }

    public String getTextureNorth() {
        return textureNorth;
    }

    public void setTextureNorth(String textureNorth) {
        this.textureNorth = textureNorth;
    }

    public String getTextureEast() {
        return textureEast;
    }

    public void setTextureEast(String textureEast) {
        this.textureEast = textureEast;
    }

    public String getTextureWest() {
        return textureWest;
    }

    public void setTextureWest(String textureWest) {
        this.textureWest = textureWest;
    }

    public String getTextureTop() {
        return textureTop;
    }

    public void setTextureTop(String textureTop) {
        this.textureTop = textureTop;
    }

    public String getTextureSouth() {
        return textureSouth;
    }

    public void setTextureSouth(String textureSouth) {
        this.textureSouth = textureSouth;
    }

}
