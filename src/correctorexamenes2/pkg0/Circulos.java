
package correctorexamenes2.pkg0;

import org.opencv.core.Point;


public class Circulos {
    
    public Point centro;
    public int radio;

    public Circulos(Point centro, int radio) {
        this.centro = centro;
        this.radio = radio;
    }

    public Point getCentro() {
        return centro;
    }

    public void setCentro(Point centro) {
        this.centro = centro;
    }

    public int getRadio() {
        return radio;
    }

    public void setRadio(int radio) {
        this.radio = radio;
    }
    
    
}
