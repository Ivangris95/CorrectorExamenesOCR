
package app;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;


public class mensajeEmergente {
    
    public static void mostrarMensaje(String mensaje) {
    JOptionPane optionPane = new JOptionPane(
        mensaje,
        JOptionPane.INFORMATION_MESSAGE
    );
    
    JDialog dialogo = optionPane.createDialog(null, "Mensaje");
    
    // Añadir listener para el cierre de la ventana
    dialogo.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            dialogo.dispose();
        }
    });
    
    dialogo.setAlwaysOnTop(true);
    dialogo.setVisible(true);
}
}
