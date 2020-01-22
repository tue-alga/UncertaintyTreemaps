package UserControl.Visualiser;

import java.awt.Color;
import treemap.dataStructure.TreeMap;

/**
 *
 * @author Max Sondag
 */
public class ColorScheme {

    public static Color getColor(double aspectRatio) {
        //picked using colorBrewer
        float red, green, blue;
        if (aspectRatio < 2) {
            red = 254f;
            green = 247f;
            blue = 188f;
        } else if (aspectRatio < 4) {
            red = 254f;
            green = 196f;
            blue = 79f;
        } else {
            red = 222f;
            green = 45f;
            blue = 38f;
        }
        red = red / 255f;
        green = green / 255f;
        blue = blue / 255f;
        return Color.BLACK;
//        return new Color(red, green, blue);

    }

    public static Color getColor(TreeMap tm) {
        double aspectRatio = tm.getRectangle().getAspectRatio();
        return getColor(aspectRatio);

    }

}
