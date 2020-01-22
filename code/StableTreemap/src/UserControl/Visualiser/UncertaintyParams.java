/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UserControl.Visualiser;

/**
 *
 * @author msondag
 */
public class UncertaintyParams {

    double baseSep;
    double baseWidth;
    double addWidth;
    double baseLum;
    double addLum;
    String uncertaintyType;
    
    public UncertaintyParams(double baseSep,double baseWidth, double addWidth, double baseLum, double addLum, String uncertaintyType) {
        this.baseSep = baseSep;
        this.baseWidth = baseWidth;
        this.addWidth = addWidth;
        this.baseLum = baseLum;
        this.addLum = addLum;
        this.uncertaintyType = uncertaintyType;
    }

}
