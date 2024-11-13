package api;

import app.mensajeEmergente;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class GuardarRespuestasTest {

    private static final String SEPARADOR = ",";
    private static final String NUEVA_LINEA = "\n";

    // Verifica si un examen con el código especificado ya existe en el archivo.
    public static boolean existeExamen(String codigoExamen, String nombreArchivo) {
        try {
            File archivo = new File(nombreArchivo);
            if (!archivo.exists()) {
                return false;
            }

            BufferedReader br = new BufferedReader(new FileReader(archivo));
            String linea;

            // Saltamos la primera línea (encabezados)
            br.readLine();

            while ((linea = br.readLine()) != null) {

                String[] datos = linea.split(",");

                if (datos.length > 0 && datos[0].equals(codigoExamen)) {
                    br.close();
                    return true;
                }
            }
            br.close();
            return false;

        } catch (IOException e) {
            mensajeEmergente.mostrarMensaje("""
                                            Error al leer el archivo
                                            Error: """ + e.getMessage());
            return false;
        }
    }

    public static void guardarResultados(String codigoExamen, ArrayList<Character> resultados, String nombreArchivo) {
        System.out.println("___ Inicio de guardarResultados ___");
        System.out.println("Código Examen: " + codigoExamen);
        System.out.println("Resultados: " + resultados);
        System.out.println("Tamaño resultados: " + resultados.size());
        System.out.println("Nombre Archivo: " + nombreArchivo);

        // Validaciones iniciales
        if (codigoExamen == null || codigoExamen.trim().isEmpty()) {
            mensajeEmergente.mostrarMensaje("El código de examen no puede estar vacío");
            return;
        }

        if (resultados == null || resultados.isEmpty()) {
            mensajeEmergente.mostrarMensaje("No hay resultados para guardar");
            return;
        }

        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            mensajeEmergente.mostrarMensaje("El nombre del archivo no puede estar vacío");
            return;
        }

        BufferedWriter bw = null;
        try {
            // Verificar si el examen ya existe
            if (existeExamen(codigoExamen, nombreArchivo)) {
                mensajeEmergente.mostrarMensaje("El examen ya existe en el sistema");
                return;
            }

            File archivo = new File(nombreArchivo);
            boolean existeArchivo = archivo.exists();

            // Verificar si se puede escribir en el directorio
            File directorio = archivo.getParentFile();
            if (directorio != null && !directorio.exists()) {
                boolean dirCreado = directorio.mkdirs();
                if (!dirCreado) {
                    mensajeEmergente.mostrarMensaje("No se pudo crear el directorio para el archivo");
                    return;
                }
            }

            System.out.println("Archivo existe: " + existeArchivo);
            System.out.println("Ruta absoluta: " + archivo.getAbsolutePath());

            // Abrir el archivo para escritura
            FileWriter fw = new FileWriter(archivo, true);
            bw = new BufferedWriter(fw);

            // Escribir encabezados si el archivo es nuevo
            if (!existeArchivo) {
                bw.write("Codigo_Examen,Resultados");
                bw.write(NUEVA_LINEA);
            }

            // Preparar y escribir la línea de resultados
            StringBuilder linea = new StringBuilder();
            linea.append(codigoExamen).append(SEPARADOR);

            for (Character resultado : resultados) {
                linea.append(resultado);
            }

            System.out.println("Línea a escribir: " + linea.toString());

            bw.write(linea.toString());
            bw.write(NUEVA_LINEA);
            bw.flush();

            mensajeEmergente.mostrarMensaje("Guardado con éxito");
            System.out.println("___ Fin de guardarResultados ___");

        } catch (IOException e) {
            System.err.println("Error al guardar: " + e.getMessage());
            e.printStackTrace();
            mensajeEmergente.mostrarMensaje("Error al guardar: " + e.getMessage());
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar el archivo: " + e.getMessage());
                }
            }
        }
    }

    public static String obtenerRespuestas(String codigoExamen, String rutaArchivo) {
        String respuestas = null;
        String linea;

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {

            // Leer la primera línea (encabezados)
            String encabezados = br.readLine();

            // Leer el resto del archivo
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                // Asumiendo que el código del examen está en la primera columna
                // y las respuestas en la segunda columna
                if (datos[0].trim().equals(codigoExamen.trim())) {
                    respuestas = datos[1].trim();
                    break;
                }
            }
        } catch (IOException e) {
            mensajeEmergente.mostrarMensaje("Error al lectura");
        }

        return respuestas;
    }
}
