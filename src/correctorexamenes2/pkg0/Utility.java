
package correctorexamenes2.pkg0;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


public class Utility {
    
    //Función para añadir blur
    public static Mat setAñadirBlurALaImagen(Mat imagen) {
        Mat imagenConBlur = new Mat();

        // Aplicar un desenfoque Gaussian a la imagen original
        Imgproc.GaussianBlur(imagen, imagenConBlur, new Size(3, 3), 0);
        
        return imagenConBlur;
    }
    
    // Función para convertir a escala de grises
    public static Mat setConvertirEscalaGrises(Mat imagenOriginal) {
        
        // Crear una Mat para la imagen en escala de grises
        Mat imagenEscalaGrises = new Mat();

        // Convertir la imagen desenfocada a escala de grises
        Imgproc.cvtColor(imagenOriginal, imagenEscalaGrises, Imgproc.COLOR_BGR2GRAY);

        return imagenEscalaGrises;
    }
    
    
    // Función para aplicar Canny
    public static Mat setAplicarCanny(Mat imagenEscalaGrises) {
        Mat imagenConBordes = new Mat();
        Imgproc.Canny(imagenEscalaGrises, imagenConBordes, 125, 200);
        return imagenConBordes;
    }

    public static Mat setBinarizarImagen(Mat imagenGris) {

        Mat imagenBinarizada = new Mat();

        Imgproc.adaptiveThreshold(imagenGris, imagenBinarizada, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 2);

        return imagenBinarizada;
    }
    
    public static int procesarCirculos(Mat imagenColor, Rect rect) {

        Mat circuloRecortado = imagenColor.submat(rect);

        // Aplicar un preprocesamiento más robusto
        Mat circuloEscalaDeGrises = new Mat();
        Imgproc.cvtColor(circuloRecortado, circuloEscalaDeGrises, Imgproc.COLOR_BGR2GRAY);

        // Aplicar blur para reducir ruido
        Imgproc.GaussianBlur(circuloEscalaDeGrises, circuloEscalaDeGrises, new org.opencv.core.Size(5, 5), 0);

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
    
    public static double calcularNota(ArrayList<Character> respuestasCorrectas, ArrayList<Character> respuestasAlumno) {
        int A = 0; // Respuestas correctas
        int E = 0; // Respuestas erróneas
        int k = 4; // Total de respuestas
        int totalPreguntas = respuestasCorrectas.size();

        // Comparar respuestas
        for (int i = 0; i < totalPreguntas; i++) {
            
            if(respuestasAlumno.get(i) == 'X'){
                continue;
            }
            
            if (Objects.equals(respuestasAlumno.get(i), respuestasCorrectas.get(i))) {

                A++; // Incrementar respuestas correctas

            } else {

                E++; // Incrementar respuestas erróneas
            }
        }

        // Calcular la nota usando la fórmula
        double nota = A - (double) E / (k - 1);

        double notaBase10 = (nota / totalPreguntas) * 10.0;

        return notaBase10;
    }
    
    
    
    public static String crearNumeroDeIdentificacion(ArrayList<ArrayList<Integer>> lista) {
        // Recorrer cada sublista en blackPixelsList
        StringBuilder codigoNumeros = new StringBuilder();

        // Recorrer cada sublista en blackPixelsList
        for (ArrayList<Integer> grupo : lista) {
            int indiceMayor = 0;

            // Encontrar el índice del número más grande en el grupo
            for (int j = 1; j < grupo.size(); j++) {
                if (grupo.get(j) > grupo.get(indiceMayor)) {
                    indiceMayor = j;  // Actualizamos el índice si encontramos un número mayor
                }
            }

            // Convertir el índice mayor a carácter numérico y agregarlo al StringBuilder
            codigoNumeros.append((char) ('0' + indiceMayor));
        }

        // Convertir el StringBuilder a String y devolverlo
        return codigoNumeros.toString();
    }
    
    public static char obtenerLetra(List<Integer> conteoPixeles) {
    // Encontrar el índice con más píxeles
    int maxPixeles = -1;
    int indiceMaxPixeles = -1;

    for (int i = 0; i < conteoPixeles.size(); i++) {
        if (conteoPixeles.get(i) > maxPixeles) {
            maxPixeles = conteoPixeles.get(i);
            indiceMaxPixeles = i;
        }
    }

    // Definir un umbral mínimo de píxeles para considerar que una letra está marcada
    final int UMBRAL_MINIMO_PIXELES = 210;

    // Si el máximo número de píxeles está por debajo del umbral, significa que no se marcó ninguna letra
    if (maxPixeles < UMBRAL_MINIMO_PIXELES) {
        return ' '; // Retorna un espacio en blanco si no se detecta ninguna letra marcada
    }

    // Si se superó el umbral, convertir el índice a letra (A=0, B=1, C=2, etc.)
    return (char) ('A' + indiceMaxPixeles);
}
    
    
}

