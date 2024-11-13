package app;

import api.CorrectorExamenesApi;
import static api.CorrectorExamenesApi.listaDePixelesPorGruposNumeroExamen;
import static api.CorrectorExamenesApi.procesarSeccionIdentificador;
import static api.CorrectorExamenesApi.procesarSeccionNumeroExamen;
import static api.CorrectorExamenesApi.procesarSeccionRespuestas;
import api.GuardarRespuestasTest;
import api.UtilityApi;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class CorrectorExamenesApp extends javax.swing.JFrame {

    private String rutaPlantilla;
    private String rutaExamen;
    public static ArrayList<Character> resultadoAlumno = new ArrayList<>();  // ArrayList global con el resultado del alumno
    public static ArrayList<Character> resultadoCorrecto = new ArrayList<>();
    private DatosExamen datosExamen;

    public CorrectorExamenesApp() {

        initComponents();

        datosExamen = new DatosExamen();

        añadirPlantilla.addActionListener((java.awt.event.ActionEvent evt) -> {

            rutaPlantilla = "";
            datosExamen.limpiarDatos();
            resultadoCorrecto.clear();

            mostrarExploradorArchivos("Seleccionar Plantilla", true, examenLabel);

            Mat imagenOriginal = CorrectorExamenesApi.cargarImagen(rutaPlantilla);

            if (imagenOriginal == null) {
                mensajeEmergente.mostrarMensaje("Archivo no encontrado");
            }

            try {

                Mat imagenEscalada = procesarImagenBase(imagenOriginal);

                Mat imagenProcesada = CorrectorExamenesApi.procesarImagen(imagenEscalada);

                List<Rect> listaContornos = CorrectorExamenesApi.detectarYOrdenarCincoContornosPrincipales(imagenProcesada);

                for (int i = 1; i < 5; i++) {
                    procesarSeccionRespuestas(imagenEscalada, listaContornos.get(i), resultadoCorrecto, resultadoAlumno, i);
                }

                String numeroExamen;

                // Procesar número de examen (índice 1)
                procesarSeccionNumeroExamen(imagenEscalada, listaContornos.get(5));

                numeroExamen = UtilityApi.crearNumeroDeIdentificacion(CorrectorExamenesApi.listaDePixelesPorGruposNumeroExamen, false);

                datosExamen.setNumeroExamen(numeroExamen);

                resultadoCorrecto = CorrectorExamenesApi.crearRespuestas();

                Boolean existeAlgunaRepuetaVacia = false;

                for (int i = 0; i < resultadoCorrecto.size(); i++) {
                    if (resultadoCorrecto.get(i) == 'X') {
                        existeAlgunaRepuetaVacia = true;
                    }
                }

                if (!existeAlgunaRepuetaVacia) {

                    GuardarRespuestasTest.guardarResultados(numeroExamen, resultadoCorrecto, "src//api//Plantillas.csv");

                } else {
                    mensajeEmergente.mostrarMensaje("Error. No se puede guardar un examen con respuestas vacías.");
                    return;
                }

                imagenProcesada.release();

                listaContornos.clear();

            } catch (Exception e) {

                mensajeEmergente.mostrarMensaje("Error al procesar la plantilla");
                return;
            }

            CorrectorExamenesApi.listaDePixelesPorGruposNumeroExamen.clear();
            CorrectorExamenesApi.listaDePixelesPorGrupos.clear();
        });

        añadirExamen.addActionListener((java.awt.event.ActionEvent evt) -> {

            datosExamen.limpiarDatos();
            resultadoAlumno.clear();
            rutaExamen = "";

            mostrarExploradorArchivos("Seleccionar Examen", false, examenLabel);

            Mat imagenOriginal = CorrectorExamenesApi.cargarImagen(rutaExamen);

            if (imagenOriginal == null) {

                mensajeEmergente.mostrarMensaje("Archivo no encontrado");
            }

            Mat imagenEscalada = procesarImagenBase(imagenOriginal);

            Mat imagenProcesada = CorrectorExamenesApi.procesarImagen(imagenEscalada);

            List<Rect> listaContornos = CorrectorExamenesApi.detectarYOrdenarCincoContornosPrincipales(imagenProcesada);

            for (int i = 1; i < 5; i++) {

                procesarSeccionRespuestas(imagenEscalada, listaContornos.get(i), resultadoCorrecto, resultadoAlumno, i);
            }

            // Procesar identificador (índice 0)
            procesarSeccionIdentificador(imagenEscalada, listaContornos.get(0));

            // Procesar número de examen (índice 1)
            procesarSeccionNumeroExamen(imagenEscalada, listaContornos.get(5));
            
            char LetraDNI = UtilityApi.obtenerLetra(CorrectorExamenesApi.listaPixelesLetrasIdentificador.subList(26, 52));
            System.out.println("Letra DNI: "+ LetraDNI);
            char LetraNIE = UtilityApi.obtenerLetra(CorrectorExamenesApi.listaPixelesLetrasIdentificador.subList(0, 25));
            
            boolean esNie = false;
            
            if(LetraNIE != ' ') {
                esNie = true;
            }
            
            String numeroIdentificador = UtilityApi.crearNumeroDeIdentificacion(CorrectorExamenesApi.listaDePixelesPorGruposIdentificador, esNie);
            datosExamen.setNumeroIdentificador(numeroIdentificador);

            if (datosExamen.getNumeroIdentificador() == null) {
                
                datosExamen.limpiarLetrasIdentificador();
                corregirExamen.setEnabled(false);
            } else {
                
                datosExamen.setLetraDNI(LetraDNI);
                datosExamen.setLetraNIE(LetraNIE);
                
            }

            String numeroExamen = UtilityApi.crearNumeroDeIdentificacion(listaDePixelesPorGruposNumeroExamen, false);

            datosExamen.setNumeroExamen(numeroExamen);

            codigoExamenLabel.setText(datosExamen.getNumeroExamen());
            
            
            identificadorLabel.setText(datosExamen.getLetraNIE() + " " + datosExamen.getNumeroIdentificador() + " " + datosExamen.getLetraDNI());

            resultadoAlumno = CorrectorExamenesApi.crearRespuestas();

            CorrectorExamenesApi.listaDePixelesPorGruposIdentificador.clear();
            CorrectorExamenesApi.listaPixelesLetrasIdentificador.clear();
            CorrectorExamenesApi.listaDePixelesPorGrupos.clear();
            CorrectorExamenesApi.listaDePixelesPorGruposNumeroExamen.clear();

        });

        corregirExamen.addActionListener((java.awt.event.ActionEvent evt) -> {

            resultadoCorrecto.clear();

            CorrectorExamenesApi.listaDePixelesPorGrupos.clear();

            String resultadoCorrectoFinal = GuardarRespuestasTest.obtenerRespuestas(datosExamen.getNumeroExamen(), "src//api//Plantillas.csv");

            Mat imagenOriginal = CorrectorExamenesApi.cargarImagen(rutaExamen);

            if (imagenOriginal != null) {

                Mat imagenEscalada = procesarImagenBase(imagenOriginal);

                Mat imagenProcesada = CorrectorExamenesApi.procesarImagen(imagenEscalada);

                if (resultadoCorrectoFinal == null || resultadoCorrectoFinal.isEmpty()) {

                    mensajeEmergente.mostrarMensaje("No se encontro la plantilla en la base de datos");
                    return;
                }

                for (int i = 0; i < resultadoCorrectoFinal.length(); i++) {
                    resultadoCorrecto.add(resultadoCorrectoFinal.charAt(i));
                }

                List<Rect> listaContornos = CorrectorExamenesApi.detectarYOrdenarCincoContornosPrincipales(imagenProcesada);

                for (int i = 1; i < 5; i++) {

                    procesarSeccionRespuestas(imagenEscalada, listaContornos.get(i), resultadoCorrecto, resultadoAlumno, i);
                }

                Dimension dimension = new Dimension(308, 426);
                Utility.mostrarMatEnLabel(imagenEscalada, examenLabel, dimension);

                String nota = UtilityApi.calcularNota(resultadoCorrecto, resultadoAlumno);
                datosExamen.setNota(nota);

                resultadoAlumno.clear();

                resultadoLabel.setText(datosExamen.getNota());

                imagenProcesada.release();

                listaContornos.clear();
            }
        });
    }

    private void mostrarExploradorArchivos(String titulo, final boolean esPlantilla, JLabel label) {

        try {

            Dimension dimension = new Dimension(308, 426);

            // Usar JFileChooser directamente en lugar de ExploradorArchivos
            final JFileChooser fileChooser = new JFileChooser();
            String userHome = System.getProperty("user.home");
            File desktop = new File(userHome, "Desktop");
            fileChooser.setCurrentDirectory(desktop.exists() ? desktop : new File(userHome));

            // Configurar el diálogo
            final JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(label), titulo, true);

            // Agregar el FileChooser al diálogo
            dialog.setContentPane(fileChooser);
            dialog.pack();
            dialog.setLocationRelativeTo(null);

            // Agregar el ActionListener
            fileChooser.addActionListener((ActionEvent evt) -> {
                if (evt.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
                    File file = fileChooser.getSelectedFile();

                    if (esPlantilla) {
                        rutaPlantilla = file.getAbsolutePath();
                        System.out.println("Ruta plantilla: " + rutaPlantilla);
                        Utility.SetImageLabelPorRuta(label, rutaPlantilla, dimension);

                    } else {
                        rutaExamen = file.getAbsolutePath();
                        System.out.println("Ruta examen: " + rutaExamen);
                        Utility.SetImageLabelPorRuta(label, rutaExamen, dimension);
                    }
                }
                dialog.dispose();
            });

            dialog.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            // Mostrar un mensaje de error al usuario
            JOptionPane.showMessageDialog(null,
                    "Error al abrir el explorador de archivos: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Mat procesarImagenBase(Mat imagenOriginal) {

        Mat imagenBlur = UtilityApi.setAñadirBlurALaImagen(imagenOriginal);
        Mat imagenEscalada = new Mat();

        // Escalar imagen
        Size sz = new Size(724, 1024);
        Imgproc.resize(imagenBlur, imagenEscalada, sz, Imgproc.INTER_LANCZOS4);

        // Liberar recursos que ya no necesitamos
        imagenOriginal.release();
        imagenBlur.release();

        return imagenEscalada;
    }

    public String getRutaPlantilla() {
        return rutaPlantilla;
    }

    public String getRutaExamen() {
        return rutaExamen;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem1 = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        añadirPlantilla = new javax.swing.JButton();
        añadirExamen = new javax.swing.JButton();
        corregirExamen = new javax.swing.JButton();
        examenLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        codigoExamenLabel = new javax.swing.JLabel();
        identificadorLabel = new javax.swing.JLabel();
        resultadoLabel = new javax.swing.JLabel();

        jMenuItem1.setText("jMenuItem1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        añadirPlantilla.setFont(new java.awt.Font("Fira Code Medium", 1, 18)); // NOI18N
        añadirPlantilla.setText("AÑADIR PLANTILLA");

        añadirExamen.setFont(new java.awt.Font("Fira Code Medium", 1, 18)); // NOI18N
        añadirExamen.setText("AÑADIR EXAMEN");

        corregirExamen.setFont(new java.awt.Font("Fira Code Medium", 1, 18)); // NOI18N
        corregirExamen.setText("CORREGIR EXAMEN");

        examenLabel.setBackground(new java.awt.Color(255, 255, 255));
        examenLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        examenLabel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, null, new java.awt.Color(204, 204, 204), null, new java.awt.Color(204, 204, 204)));

        jLabel4.setFont(new java.awt.Font("Fira Code SemiBold", 0, 14)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel4.setText("Codigo Examen");

        jLabel5.setFont(new java.awt.Font("Fira Code SemiBold", 0, 14)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel5.setText("Nota");

        jLabel6.setFont(new java.awt.Font("Fira Code SemiBold", 0, 14)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel6.setText("DNI/NIE");

        codigoExamenLabel.setFont(new java.awt.Font("Fira Code Medium", 0, 12)); // NOI18N
        codigoExamenLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        codigoExamenLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));

        identificadorLabel.setFont(new java.awt.Font("Fira Code Medium", 0, 12)); // NOI18N
        identificadorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        identificadorLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));

        resultadoLabel.setFont(new java.awt.Font("Fira Code Medium", 0, 12)); // NOI18N
        resultadoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        resultadoLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(añadirPlantilla)
                .addGap(47, 47, 47)
                .addComponent(añadirExamen, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
                .addGap(43, 43, 43)
                .addComponent(corregirExamen)
                .addGap(28, 28, 28))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(87, 87, 87)
                .addComponent(examenLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 318, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(121, 121, 121)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(resultadoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(identificadorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(codigoExamenLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(82, 82, 82)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(añadirExamen, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(añadirPlantilla, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(corregirExamen, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 123, Short.MAX_VALUE)
                        .addComponent(examenLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 436, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(139, 139, 139))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(183, 183, 183)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(codigoExamenLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(60, 60, 60)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(60, 60, 60)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(identificadorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(resultadoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 970, 840));

        pack();
    }// </editor-fold>//GEN-END:initComponents
    public static void main(String args[]) {
        System.out.println("OpenCV Version: " + Core.VERSION);
        /* Establecer el look and feel */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CorrectorExamenesApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Crear y mostrar el formulario */
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                CorrectorExamenesApp app = new CorrectorExamenesApp();
                app.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton añadirExamen;
    private javax.swing.JButton añadirPlantilla;
    private javax.swing.JLabel codigoExamenLabel;
    private javax.swing.JButton corregirExamen;
    private javax.swing.JLabel examenLabel;
    private javax.swing.JLabel identificadorLabel;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel resultadoLabel;
    // End of variables declaration//GEN-END:variables
}
