package api;

import app.mensajeEmergente;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class OpenCVLoader {

    //Este método extrae y carga el archivo DLL necesario para OpenCV.
    public static void loadOpenCV() {
        try {
            String dllName = "opencv_java490.dll";                              // Nombre del archivo DLL de OpenCV
            String libPath = "/lib/x64/" + dllName;                             // Ruta donde se encuentra el DLL dentro de los recursos

            // Obtener directorio temporal del sistema y crear subdirectorio para OpenCV
            File tempDir = new File(System.getProperty("java.io.tmpdir") + "/opencv_lib");
            if (!tempDir.exists()) {
                tempDir.mkdir();
            }

            // Crear referencia al archivo temporal donde se extraerá el DLL
            File dllTemp = new File(tempDir.getAbsolutePath() + "/" + dllName);

            // Si el DLL ya existe en el directorio temporal, intentar cargarlo
            if (dllTemp.exists()) {
                System.load(dllTemp.getAbsolutePath());
                return;
            }

            // Si el DLL no existe, extraerlo del JAR usando streams
            try (InputStream in = OpenCVLoader.class.getResourceAsStream(libPath); OutputStream out = new FileOutputStream(dllTemp)) {

                // Verificar si se encontró el DLL en los recursos
                if (in == null) {
                    throw new RuntimeException("No se pudo encontrar el archivo DLL en: " + libPath);
                }

                // Copiar el DLL desde los recursos al archivo temporal
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }

            // Cargar el DLL
            System.load(dllTemp.getAbsolutePath());
            dllTemp.deleteOnExit();

            System.out.println("OpenCV cargado correctamente");

        } catch (Exception e) {
            mensajeEmergente.mostrarMensaje("Error al cargar OpenCV.");
        }
    }
}
