package gui;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;
import game.Card;

import java.awt.*;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by Andrew on 28/12/13.
 */
public class ImageManager {
    private static final URL[] spadeURLs = new URL[13];
    private static final URL[] clubURLs = new URL[13];
    private static final URL[] diamondURLs = new URL[13];
    private static final URL[] heartURLs = new URL[13];
    private static final String urlBase = "/images/";
    private static final SVGUniverse universe = new SVGUniverse();

    static {
        loadRankURIs(spadeURLs, urlBase + "spades/");
        loadRankURIs(clubURLs, urlBase + "clubs/");
        loadRankURIs(diamondURLs, urlBase + "diamonds/");
        loadRankURIs(heartURLs, urlBase + "hearts/");
        loadSVGs(spadeURLs);
        loadSVGs(clubURLs);
        loadSVGs(diamondURLs);
        loadSVGs(heartURLs);
    }

    public static void renderSVG(Card card, Graphics2D g) {

        try {
            SVGDiagram diagram;
            if (card == null) {
                diagram = universe.getDiagram(ImageManager.class.getResource(urlBase + "blank.svg").toURI());

            } else {
                diagram = universe.getDiagram(ImageManager.class.getResource(urlBase + card.getSuit().name().toLowerCase() + "/" + card.getRank().ordinal() + ".svg").toURI());
            }
            diagram.setIgnoringClipHeuristic(true);
            diagram.render(g);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (SVGException e) {
            e.printStackTrace();
        }

    }

    private static void loadSVGs(URL[] URLs) {
        for (int i = 0; i < 13; i++) {
            universe.loadSVG(URLs[i]);
        }
    }

    private static void loadRankURIs(URL[] URLs, String urlBase) {
        for (int i = 0; i < 13; i++) {
            URLs[i] = ImageManager.class.getResource(urlBase + i + ".svg");
        }
    }


    public static void renderBlank(Graphics2D g, boolean blue) {
        try {
            SVGDiagram diagram = universe.getDiagram(ImageManager.class.getResource(urlBase + (blue ? "blue.svg" : "red.svg")).toURI());
            diagram.setIgnoringClipHeuristic(true);
            diagram.render(g);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (SVGException e) {
            e.printStackTrace();
        }
    }
}
