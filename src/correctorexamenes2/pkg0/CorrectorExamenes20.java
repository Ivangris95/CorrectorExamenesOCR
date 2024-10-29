package correctorexamenes2.pkg0;

import static correctorexamenes2.pkg0.Utility.calcularNota;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class CorrectorExamenes20 {
    
    private static char LetraDNI;
    private static char LetraNIF;
    private static final ArrayList<Integer> listaPixelesLetrasIdentificador = new ArrayList<>();
    private static final ArrayList<ArrayList<Integer>> listaDePixelesPorGruposIdentificador = new ArrayList<>();
    private static final ArrayList<ArrayList<Integer>> listaDePixelesPorGrupos = new ArrayList<>();// Lista de listas para almacenar los píxeles negros en grupos de 4
    private static final ArrayList<Character> resultadoAlumno = new ArrayList<>();  // ArrayList global con el resultado del alumno
    private static final ArrayList<Character> resultadoCorrecto = new ArrayList<>(
            Arrays.asList('A', 'B', 'C', 'C', 'C', 'D', 'B', 'C', 'A', 'D',
                    'A', 'A', 'B', 'B', 'A', 'A', 'C', 'D', 'C', 'B',
                    'B', 'D', 'C', 'B', 'A', 'A', 'B', 'C', 'D', 'B',
                    'D', 'C', 'B', 'B', 'B', 'C', 'D', 'B', 'A', 'B')
    );

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);  // Cargar la librería nativa de OpenCV
    }

    public static void main(String[] args) {

        String rutaImagenExamen = "src\\correctorexamenes2\\pkg0\\Plantilla2.0.jpg";  // Ruta de la imagen

        Mat imagenOriginal = cargarImagen(rutaImagenExamen);
        if (imagenOriginal == null) {
            return;
        }

        Mat imagenProcesada = procesarImagen(imagenOriginal);

        List<Rect> listaContornosPrincipales = detectarYOrdenarCincoContornosPrincipales(imagenProcesada);

        procesarSeccionesExamen(imagenOriginal, listaContornosPrincipales);

        // Llamar al método para crear las respuestas
        crearRespuestaSegunLaRespeustaDeElAlumno();
        
        // Imprimir las respuestas del alumno
        System.out.println("Respuestas del alumno: " + resultadoAlumno);

        // Opcional: comparar con las respuestas correctas
        System.out.println("Respuestas correctas: " + resultadoCorrecto);
        
        System.out.println(Utility.calcularNota(resultadoCorrecto, resultadoAlumno));
        
        System.out.println(Utility.crearNumeroDeIdentificacion(listaDePixelesPorGruposIdentificador));
        
        LetraDNI = Utility.obtenerLetra(listaPixelesLetrasIdentificador.subList(26, listaPixelesLetrasIdentificador.size()));
        
        LetraNIF = Utility.obtenerLetra(listaPixelesLetrasIdentificador.subList(0, 26));
        
        mostrarResultados();
    }

    private static Mat cargarImagen(String ruta) {
        Mat imagen = Imgcodecs.imread(ruta);
        if (imagen.empty()) {
            System.out.println("No se pudo cargar la imagen.");
            return null;
        }
        return imagen;
    }

    private static Mat procesarImagen(Mat imagen) {

        Mat imagenConBlur = Utility.setAñadirBlurALaImagen(imagen);
        Mat imagenEscalaGrises = Utility.setConvertirEscalaGrises(imagenConBlur);
        return Utility.setAplicarCanny(imagenEscalaGrises);
    }

    // Función para detectar y ordenar contornos
    private static List<Rect> detectarYOrdenarCincoContornosPrincipales(Mat imagenConBordes) {

        List<MatOfPoint> listaContornos = new ArrayList<>();

        Imgproc.findContours(imagenConBordes, listaContornos, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Rect> listaRectangulosContornos = new ArrayList<>();
        List<Rect> listaRectangulos = new ArrayList<>();

        for (MatOfPoint contorno : listaContornos) {

            Rect rectanguloContorno = Imgproc.boundingRect(contorno);

            listaRectangulosContornos.add(rectanguloContorno);

        }

        listaRectangulosContornos.sort((rect1, rect2) -> Double.compare(rect2.area(), rect1.area()));

        for (int i = 0; i < 6; i++) {
            listaRectangulos.add(listaRectangulosContornos.get(i));

        }

        listaRectangulos.sort((rect1, rect2) -> {
            int compararCordenadaY = Integer.compare(rect1.y, rect2.y); // De arriba hacia abajo
            if (compararCordenadaY != 0) {
                return compararCordenadaY;
            }

            return Integer.compare(rect1.x, rect2.x); // De izquierda a derecha
        });

        return listaRectangulos;
    }

    private static void procesarSeccionesExamen(Mat imagenOriginal, List<Rect> contornosPrincipales) {

        // Procesar sección de respuestas (índices 2-5)
        for (int i = 2; i < 6; i++) {

            procesarSeccionRespuestas(imagenOriginal, contornosPrincipales.get(i));

        }

        // Procesar identificador (índice 0)
        procesarSeccionIdentificador(imagenOriginal, contornosPrincipales.get(0));
        
        // Procesar número de examen (índice 1)
        procesarSeccionNumeroExamen(imagenOriginal, contornosPrincipales.get(1));
    }

    private static void procesarSeccionRespuestas(Mat imagen, Rect rectangulo) {

        Mat imagenRecortada = imagen.submat(rectangulo);
        

        Mat imagenProcesada = preprocesadoDeImagen(imagenRecortada);

        List<Rect> subRectangulos = detectarSubrectangulos(imagenProcesada,false);
       

        for (int j = 0; j < 10; j++) {

            Rect subRectangulo = subRectangulos.get(j);

            Imgproc.rectangle(imagenRecortada, subRectangulo.tl(), subRectangulo.br(), new Scalar(0, 255, 0), 2);

            Mat subimagenRercortada = imagenRecortada.submat(subRectangulo);

//            Mat subimagenBlur = Utility.setAñadirBlurALaImagen(subimagenRercortada);
            Mat subiamgenRecortadaGris = Utility.setConvertirEscalaGrises(subimagenRercortada);

            Mat subimagenBinarizada = Utility.setBinarizarImagen(subiamgenRecortadaGris);

            detectarCirculos(subimagenBinarizada, imagenRecortada, subRectangulo);
        }

        HighGui.imshow("Imagenes recortadas de respuestas", imagenRecortada);
        HighGui.waitKey();

    }
    
    private static void procesarSeccionIdentificador(Mat imagenOriginal, Rect rectanguloPrincipal) {
        
        Mat imagenRecortada = imagenOriginal.submat(rectanguloPrincipal);
  
        Mat imagenEnEscalaGris = Utility.setConvertirEscalaGrises(imagenRecortada);
        
        Mat imagenBinarizada = Utility.setBinarizarImagen(imagenEnEscalaGris);

        List<Rect> listaDeSubRectangulos = detectarSubrectangulos(imagenBinarizada, true);
        
        System.out.println("Lista: " + listaDeSubRectangulos);

        // Dibujar los sub-rectángulos en la subimagen
        for (int j = 0; j < 1; j++) {

            Rect subRectangulo = listaDeSubRectangulos.get(j);

            // Dibujar el subrectángulo en la subimagen
            Imgproc.rectangle(imagenRecortada, subRectangulo.tl(), subRectangulo.br(), new Scalar(0, 255, 0), 2);

            // Recortar el subrectángulo
            Mat subImagenRecortada = imagenRecortada.submat(subRectangulo);
            
            Mat subImagenEscalaGrises = Utility.setConvertirEscalaGrises(subImagenRecortada);

            detectarCirculosIdentificador(subImagenEscalaGrises, imagenRecortada, subRectangulo, false);
            HighGui.imshow("imagenes Recortadas" + j, subImagenRecortada);
            HighGui.waitKey();
        }

        for (int j = 1; j < 3; j++) {
            Rect subRectangulo = listaDeSubRectangulos.get(j);

            // Dibujar el subrectángulo en la subimagen
            Imgproc.rectangle(imagenRecortada, subRectangulo.tl(), subRectangulo.br(), new Scalar(0, 255, 0), 2);

            // Recortar el subrectángulo
            Mat subImagenRecortada = imagenRecortada.submat(subRectangulo);

            Mat subImagenEscalaGrises = Utility.setConvertirEscalaGrises(subImagenRecortada);

            detectarCirculosIdentificador(subImagenEscalaGrises, imagenRecortada, subRectangulo, true);
            HighGui.imshow("imagenes Recortadas" + j, subImagenRecortada);
            HighGui.waitKey();
        }
        HighGui.imshow("imagenes recortadas ", imagenRecortada);

    }
    
    private static void procesarSeccionNumeroExamen(Mat imagen, Rect rectangulo) {
        
    }
    
    private static List<Rect> detectarSubrectangulos(Mat bordesSubimagen, Boolean esIdentificador) {
        
        Boolean identificador = esIdentificador;
        // Detectar contornos
        List<MatOfPoint> contornosSubimagen = new ArrayList<>();
        Imgproc.findContours(bordesSubimagen, contornosSubimagen, new Mat(),
                Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        // Convertir contornos a rectángulos y filtrar por tamaño
        List<Rect> rectangulos = new ArrayList<>();

        for (MatOfPoint contorno : contornosSubimagen) {
            
            Rect rect = Imgproc.boundingRect(contorno);
            
            if (rect.width > 5 && rect.height > 5) {
                boolean estaEnMismaPosicion = false;

                // Verificar si ya existe un rectángulo en la misma posición
                for (Rect rectanguloExistente : rectangulos) {

                    if (rectanguloExistente.tl().x == rect.tl().x && rectanguloExistente.tl().y == rect.tl().y) {
                        estaEnMismaPosicion = true;
                        break;
                    }
                }

                // Si no está en la misma posición, lo agregamos
                if (!estaEnMismaPosicion) {

                    rectangulos.add(rect);
                    

                }
            }
        }
        rectangulos.sort((rect1, rect2) -> Double.compare(rect2.area(), rect1.area()));
        // Ordenar la lista final
        rectangulos.sort((rect1, rect2) -> compararRectangulos(rect1, rect2, identificador));
        
        return rectangulos;
    }
    
    private static int compararRectangulos(Rect rect1, Rect rect2, Boolean esIdentificador) {
        
        boolean rect1CumpleCriterio;
        boolean rect2CumpleCriterio;
        
        if(esIdentificador){
            rect1CumpleCriterio = rect1.width < rect1.height;
            rect2CumpleCriterio = rect2.width < rect2.height;
        } else {
            rect1CumpleCriterio = rect1.width > rect1.height;
            rect2CumpleCriterio = rect2.width > rect2.height;
        }

        if (rect1CumpleCriterio && !rect2CumpleCriterio) {

            return -1;

        } else if (!rect1CumpleCriterio && rect2CumpleCriterio) {

            return 1;
        }

        double area1 = rect1.area();
        double area2 = rect2.area();

        int compruebaArea = Double.compare(area2, area1);
        if (compruebaArea != 0) {

            return compruebaArea;
        }

        int compararCordenadaY = Double.compare(rect1.tl().y, rect2.tl().y);

        if (compararCordenadaY != 0) {

            return compararCordenadaY;
        }
        return Double.compare(rect2.tl().x, rect1.tl().x);
        
    }

    private static void detectarCirculos(Mat imagenGris, Mat imagenColor, Rect rect) {
        // Aplicar un preprocesamiento más robusto
        Mat imagenBinarizada = Utility.setBinarizarImagen(imagenGris);

        Mat circulos = new Mat();
        // Ajustar parámetros de HoughCircles para mejor detección

        Imgproc.HoughCircles(imagenBinarizada, circulos,Imgproc.HOUGH_GRADIENT,
                1, imagenGris.cols() / 10,100, 12, 8, 11);

        // Estructuras para almacenar los círculos detectados
        List<Circulos> circulosDetectados = new ArrayList<>();

        // Procesar todos los círculos detectados
        for (int i = 0; i < 4; i++) {

            double[] circ = circulos.get(0, i);

            if (circ != null) {

                Point centro = new Point(circ[0] + rect.x, circ[1] + rect.y);

                int radio = (int) Math.round(circ[2]);

                circulosDetectados.add(new Circulos(centro, radio));
            }
        }

        // Ordenar los círculos por coordenada X
        circulosDetectados.sort((c1, c2) -> Double.compare(c1.centro.x, c2.centro.x));

        // Procesar los círculos ordenados
        ArrayList<Integer> pixelesPorCirculo = new ArrayList<>();

        for (Circulos circulo : circulosDetectados) {
            // Dibujar el círculo
            Imgproc.circle(imagenColor, circulo.centro, circulo.radio, new Scalar(0, 0, 255), 2);

            // Calcular el área del círculo para análisis
            Rect rectCirculo = new Rect(
                    (int) (circulo.centro.x - circulo.radio),
                    (int) (circulo.centro.y - circulo.radio),
                    circulo.radio * 2,
                    circulo.radio * 2
            );

            // Asegurar que el rectángulo está dentro de los límites
            rectCirculo = new Rect(
                    Math.max(0, rectCirculo.x),
                    Math.max(0, rectCirculo.y),
                    Math.min(imagenColor.cols() - rectCirculo.x, rectCirculo.width),
                    Math.min(imagenColor.rows() - rectCirculo.y, rectCirculo.height)
            );

            // Procesar y contar píxeles negros
            int pixelesNegros = Utility.procesarCirculos(imagenColor, rectCirculo);
            pixelesPorCirculo.add(pixelesNegros);
        }

        // Solo añadir el grupo si tenemos exactamente 4 círculos procesados
        if (pixelesPorCirculo.size() == 4) {
            listaDePixelesPorGrupos.add(new ArrayList<>(pixelesPorCirculo));

        }
    }
   
    
    private static void detectarCirculosIdentificador(Mat imagenGris, Mat imagenColor, Rect rect, Boolean sonLasLetrasIdentificador) {
        
        ArrayList<Integer> listaTemporal = new ArrayList<>();
        Mat circulos = new Mat();
        Imgproc.HoughCircles(imagenGris,circulos,Imgproc.HOUGH_GRADIENT,1,
                imagenGris.cols() / 10,300,15,8,10);

        // Lista para almacenar todos los círculos
        List<Circulos> todosLosCirculos = new ArrayList<>();

        // Recolectar todos los círculos
        for (int i = 0; i < circulos.cols(); i++) {
            double[] circ = circulos.get(0, i);
            if (circ != null) {
                Point centro = new Point(circ[0] + rect.x, circ[1] + rect.y);
                int radio = (int) Math.round(circ[2]);

                todosLosCirculos.add(new Circulos(centro, radio));
            }
        }
        
        if (sonLasLetrasIdentificador) {
            todosLosCirculos.sort((c1, c2) -> {
            int comparacionY = Double.compare(c1.centro.y, c2.centro.y);
            return (comparacionY != 0) ? comparacionY : Double.compare(c1.centro.x, c2.centro.x);
            });
        }else {
            todosLosCirculos.sort((c1, c2) -> Double.compare(c1.centro.x, c2.centro.x));
        }
        
        if (sonLasLetrasIdentificador) {
            // Procesar cada círculo
            for (Circulos circulo : todosLosCirculos) {
                // Dibujar el círculo
                Imgproc.circle(imagenColor, circulo.centro, circulo.radio,
                        new Scalar(0, 0, 255), 2);

                // Calcular el rectángulo que contiene el círculo
                int x = (int) (circulo.centro.x - circulo.radio);
                int y = (int) (circulo.centro.y - circulo.radio);
                int width = circulo.radio * 2;
                int height = circulo.radio * 2;

                // Asegurar que el rectángulo está dentro de los límites
                Rect rectCirculo = new Rect(
                        Math.max(0, x),
                        Math.max(0, y),
                        Math.min(imagenColor.cols() - x, width),
                        Math.min(imagenColor.rows() - y, height)
                );

                // Extraer y procesar la subimagen
                if (rectCirculo.width > 0 && rectCirculo.height > 0) {
                    int imagenProcesada = Utility.procesarCirculos(imagenColor, rectCirculo);

                    listaPixelesLetrasIdentificador.add(imagenProcesada); 
                }
            }
        }else{
        // Crear lista de listas para agrupar los círculos en grupos de 10
            List<List<Circulos>> gruposDeCirculos = new ArrayList<>();

            // Agrupar círculos en sublistas de 10 elementos y ordenar cada grupo por Y
            for (int i = 0; i < todosLosCirculos.size(); i += 10) {
                List<Circulos> grupo = new ArrayList<>();
                // Añadir hasta 10 círculos al grupo actual
                for (int j = i; j < Math.min(i + 10, todosLosCirculos.size()); j++) {
                    grupo.add(todosLosCirculos.get(j));
                }

                // Ordenar este grupo específico por coordenada Y
                grupo.sort((c1, c2) -> Double.compare(c1.centro.y, c2.centro.y));

                // Añadir el grupo ordenado a la lista de grupos
                gruposDeCirculos.add(grupo);
            }

            for (List<Circulos> grupo : gruposDeCirculos) {

                for (Circulos circulo : grupo) {
                    // Dibujar el círculo
                    Imgproc.circle(imagenColor, circulo.centro, circulo.radio,
                            new Scalar(0, 0, 255), 2);

                    // Calcular el rectángulo que contiene el círculo
                    int x = (int) (circulo.centro.x - circulo.radio);
                    int y = (int) (circulo.centro.y - circulo.radio);
                    int width = circulo.radio * 2;
                    int height = circulo.radio * 2;

                    // Asegurar que el rectángulo está dentro de los límites
                    Rect rectCirculo = new Rect(
                            Math.max(0, x),
                            Math.max(0, y),
                            Math.min(imagenColor.cols() - x, width),
                            Math.min(imagenColor.rows() - y, height)
                    );

                    // Extraer y guardar la subimagen
                    if (rectCirculo.width > 0 && rectCirculo.height > 0) {

                        int imagenProcesada = Utility.procesarCirculos(imagenColor, rectCirculo);

                        listaTemporal.add(imagenProcesada);

                        if (listaTemporal.size() == 10) {
                            listaDePixelesPorGruposIdentificador.add(new ArrayList<>(listaTemporal));
                            listaTemporal.clear();
                        }
                    }
                }
            }
        }
    }

    private static void crearRespuestaSegunLaRespeustaDeElAlumno() {
        // Recorrer cada sublista en blackPixelsList
        for (int i = 0; i < listaDePixelesPorGrupos.size(); i++) {
            ArrayList<Integer> grupo = listaDePixelesPorGrupos.get(i);

            int maxPixeles = 0;
            int indiceMayor = -1;

            // Recorrer cada número en el grupo
            for (int j = 0; j < grupo.size(); j++) {
            if (grupo.get(j) > maxPixeles) {
                maxPixeles = grupo.get(j);
                indiceMayor = j;
            }
        }
            
            final int UMBRAL_RESPUESTA_MARCADA = 210;
            
            char letraAsignada;
            
            if (maxPixeles < UMBRAL_RESPUESTA_MARCADA) {
                letraAsignada = 'X';
            }else {
                
                switch (indiceMayor) {
                    case 0 -> letraAsignada = 'A';
                    case 1 -> letraAsignada = 'B';
                    case 2 -> letraAsignada = 'C';
                    case 3 -> letraAsignada = 'D';
                    default -> letraAsignada = 'X';
                    }
            }
            resultadoAlumno.add(letraAsignada);
        }
    }
    
    private static Mat preprocesadoDeImagen(Mat imagen) {

        Mat subimagenBlur = Utility.setAñadirBlurALaImagen(imagen);

        Mat subimagenGris = Utility.setConvertirEscalaGrises(subimagenBlur);

        Mat subimagenBinarizada = Utility.setBinarizarImagen(subimagenGris);

        Mat subimagenCanny = Utility.setAplicarCanny(subimagenBinarizada);

        return subimagenCanny;

    }
    
    private static void mostrarResultados() {
        crearRespuestaSegunLaRespeustaDeElAlumno();
        String numeroIdentificador = Utility.crearNumeroDeIdentificacion(listaDePixelesPorGruposIdentificador);
        Double nota = Utility.calcularNota(resultadoCorrecto, resultadoAlumno);
        
        System.out.println("El alumno con identificador: " + LetraNIF + "-" + 
                          numeroIdentificador + "-" + LetraDNI + 
                          " ha obtenido una calificación de: " + nota);
    }

}
