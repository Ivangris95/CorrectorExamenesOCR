package api;

import app.mensajeEmergente;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class UtilityApi {

    //Función para añadir blur
    public static Mat setAñadirBlurALaImagen(Mat imagen) {
        Mat imagenConBlur = new Mat();

        // Aplicar un desenfoque a la imagen original
        Imgproc.GaussianBlur(imagen, imagenConBlur, new Size(5, 5), 0);

        return imagenConBlur;
    }

    // Función para convertir a escala de grises
    public static Mat setConvertirEscalaGrises(Mat imagenOriginal) {

        // Crear una Mat para la imagen en escala de grises
        Mat imagenEscalaGrises = new Mat();

        Imgproc.cvtColor(imagenOriginal, imagenEscalaGrises, Imgproc.COLOR_BGR2GRAY);

        return imagenEscalaGrises;
    }

    // Función para aplicar Canny
    public static Mat setAplicarCanny(Mat imagenEscalaGrises) {

        Mat imagenConBordes = new Mat();
        Imgproc.Canny(imagenEscalaGrises, imagenConBordes, 100, 200);
        return imagenConBordes;
    }

    public static Mat setBinarizarImagen(Mat imagenGris) {

        Mat imagenBinarizada = new Mat();

        Imgproc.adaptiveThreshold(
                imagenGris,
                imagenBinarizada,
                255,
                Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY,
                11,
                2
        );

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1));

        Mat imagenLimpia = new Mat();
        Imgproc.morphologyEx(imagenBinarizada, imagenLimpia, Imgproc.MORPH_OPEN, kernel, new Point(0, 0), 1);

        Mat imagenFinal = new Mat();
        Imgproc.morphologyEx(imagenBinarizada, imagenFinal, Imgproc.MORPH_CLOSE, kernel);

        return imagenFinal;
    }

    public static int procesarCirculos(Mat imagenColor, Rect rect) {

        Mat circuloRecortado = imagenColor.submat(rect);

        Mat circuloEscalaDeGrises = new Mat();
        Imgproc.cvtColor(circuloRecortado, circuloEscalaDeGrises, Imgproc.COLOR_BGR2GRAY);

        // Aplicar blur para reducir ruido
        Imgproc.GaussianBlur(circuloEscalaDeGrises, circuloEscalaDeGrises, new org.opencv.core.Size(3, 3), 0);

        Mat circuloBinarizado = new Mat();
        Imgproc.threshold(circuloEscalaDeGrises, circuloBinarizado, 128, 255, Imgproc.THRESH_BINARY_INV);

        return contarPixelesNegros(circuloBinarizado);
    }

    // Método para contar píxeles negros en la imagen usando la máscara
    private static int contarPixelesNegros(Mat imagenBinarizada) {

        int count = 0;

        for (int i = 0; i < imagenBinarizada.rows(); i++) {

            for (int j = 0; j < imagenBinarizada.cols(); j++) {

                double[] pixel = imagenBinarizada.get(i, j);

                if (pixel[0] == 255) {
                    count++;
                }
            }
        }
        return count;
    }

    public static String calcularNota(ArrayList<Character> respuestasCorrectas, ArrayList<Character> respuestasAlumno) {

        int A = 0; // Respuestas correctas
        int E = 0; // Respuestas erróneas
        int k = 4; // Total de opciones
        int totalPreguntas = respuestasCorrectas.size();

        // Comparar respuestas
        for (int i = 0; i < totalPreguntas; i++) {

            if (respuestasAlumno.get(i) == 'X') {
                continue;
            }

            if (Objects.equals(respuestasAlumno.get(i), respuestasCorrectas.get(i))) {

                A++;

            } else {

                E++;
            }
        }

        double nota = A - (double) E / (k - 1);

        double notaBase10 = (nota / totalPreguntas) * 10.0;

        String notaFinal = String.format("%.2f", notaBase10);

        return notaFinal;
    }

    public static String crearNumeroDeIdentificacion(ArrayList<ArrayList<Integer>> lista, boolean esNie) {
    StringBuilder codigoNumeros = new StringBuilder();
    final int UMBRAL_MINIMO = 190;
    System.out.println("umbral: " + lista);

    if (esNie) {
        // Para NIE: empezamos directamente desde el índice 1, sin verificar el primer array
        for (int i = 1; i < lista.size(); i++) {
            ArrayList<Integer> grupo = lista.get(i);
            int indiceMayor = 0;
            int valorMayor = grupo.get(0);
            
            for (int j = 1; j < grupo.size(); j++) {
                if (grupo.get(j) > valorMayor) {
                    valorMayor = grupo.get(j);
                    indiceMayor = j;
                }
            }
            
            if (valorMayor > UMBRAL_MINIMO) {
                codigoNumeros.append((char) ('0' + indiceMayor));
            } else {
                mensajeEmergente.mostrarMensaje("Error: El identificador está incompleto.\nPor favor, verifica que contenga todos los números requeridos.");
                return null;
            }
        }
    } else {
        // Lógica para no NIE: todos los arrays deben superar el umbral
        for (ArrayList<Integer> grupo : lista) {
            int indiceMayor = 0;
            int valorMayor = grupo.get(0);
            
            for (int j = 1; j < grupo.size(); j++) {
                if (grupo.get(j) > valorMayor) {
                    valorMayor = grupo.get(j);
                    indiceMayor = j;
                }
            }
            
            if (valorMayor > UMBRAL_MINIMO) {
                codigoNumeros.append((char) ('0' + indiceMayor));
            } else {
                mensajeEmergente.mostrarMensaje("Error: El identificador está incompleto.\nPor favor, verifica que contenga todos los números requeridos.");
                return null;
            }
        }
    }

    if (codigoNumeros.length() == 0) {
        mensajeEmergente.mostrarMensaje("Error: El identificador está incompleto.\nPor favor, verifica que contenga todos los números requeridos.");
        return null;
    }

    return codigoNumeros.toString();
}

    public static char obtenerLetra(List<Integer> conteoPixeles) {

        int maxPixeles = -1;
        int indiceMaxPixeles = -1;

        for (int i = 0; i < conteoPixeles.size(); i++) {

            if (conteoPixeles.get(i) > maxPixeles) {

                maxPixeles = conteoPixeles.get(i);
                indiceMaxPixeles = i;
            }
        }

        final int UMBRAL_MINIMO_PIXELES = 200;

        if (maxPixeles < UMBRAL_MINIMO_PIXELES) {

            return ' ';
        }

        return (char) ('A' + indiceMaxPixeles);
    }

    public static boolean contieneLetraX(ArrayList<Character> lista) {
        return lista.contains('X');
    }

}
