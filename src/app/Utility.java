
package app;

import java.awt.Dimension;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;


public class Utility {
    
    public static void SetImageLabelPorRuta(JLabel labelName, String root, Dimension dimension) {
        
        ImageIcon image = new ImageIcon(root);
        Icon icon;

        icon = new ImageIcon(image.getImage().getScaledInstance(dimension.width, dimension.height, Image.SCALE_DEFAULT));

        labelName.setIcon(icon);

        labelName.repaint();
    }

    public static void SetImageLabel(JLabel labelName,Image imagen, Dimension dimension) {
        
        ImageIcon image = new ImageIcon(imagen);
        Icon icon;

        icon = new ImageIcon(image.getImage().getScaledInstance(dimension.width, dimension.height, Image.SCALE_DEFAULT));

        labelName.setIcon(icon);

        labelName.repaint();
    }
    
    // Función para convertir Mat a Image y mostrarlo en el label
    public static void mostrarMatEnLabel(Mat mat, JLabel label, Dimension dimension) {
        try {
            // Convertir Mat a bytes JPEG
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".jpg", mat, buffer);
            
            // Convertir bytes a Image
            ByteArrayInputStream bis = new ByteArrayInputStream(buffer.toArray());
            Image imagen = ImageIO.read(bis);
            
            
            SetImageLabel(label, imagen, dimension);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
