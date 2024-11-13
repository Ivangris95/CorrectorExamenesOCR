package api;

import app.mensajeEmergente;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class CorrectorExamenesApi {

    public static final ArrayList<ArrayList<Integer>> listaDePixelesPorGruposNumeroExamen = new ArrayList<>();  // Lista de listas para almacenar los píxeles negros en grupos de 10
    public static final ArrayList<Integer> listaPixelesLetrasIdentificador = new ArrayList<>();                 //Lista para almacenar los pixeles negros de las letras
    public static final ArrayList<ArrayList<Integer>> listaDePixelesPorGruposIdentificador = new ArrayList<>(); // Lista de listas para almacenar los píxeles negros en grupos de 10
    public static final ArrayList<ArrayList<Integer>> listaDePixelesPorGrupos = new ArrayList<>();              // Lista de listas para almacenar los píxeles negros en grupos de 4

    static {
        OpenCVLoader.loadOpenCV();                                              // Cargar la librería nativa de OpenCV
    }

    public static Mat cargarImagen(String ruta) {
        Mat imagen = Imgcodecs.imread(ruta);

        //verifica si la imagen esta vacia
        if (imagen.empty()) {
            return null;
        }
        return imagen;
    }

    public static Mat procesarImagen(Mat imagen) {

        Mat imagenEscalaGrises = UtilityApi.setConvertirEscalaGrises(imagen);
        Mat imagenConBlur = UtilityApi.setAñadirBlurALaImagen(imagenEscalaGrises);
        Mat imagenBinarizada = UtilityApi.setBinarizarImagen(imagenConBlur);

        return UtilityApi.setAplicarCanny(imagenBinarizada);
    }

    // Función para detectar y ordenar contornos 
    public static List<Rect> detectarYOrdenarCincoContornosPrincipales(Mat imagenConBordes) {
        List<MatOfPoint> listaContornos = new ArrayList<>();
        List<Rect> listaRectangulosContornos = new ArrayList<>();
        List<Rect> listaRectangulos = new ArrayList<>();

        // Primera detección de contornos
        Imgproc.findContours(imagenConBordes, listaContornos, new Mat(),
                Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Convertir contornos a rectángulos
        for (MatOfPoint contorno : listaContornos) {
            Rect rectanguloContorno = Imgproc.boundingRect(contorno);
            listaRectangulosContornos.add(rectanguloContorno);
        }

        // Ordenar por área (mayor a menor)
        listaRectangulosContornos.sort((rect1, rect2) -> Double.compare(rect2.area(), rect1.area()));

        // Verificar si hay rectángulos detectados
        if (listaRectangulosContornos.isEmpty()) {
            return listaRectangulos;
        }

        double areaDeLaImagenCompleta = imagenConBordes.rows() * imagenConBordes.cols();
        double areaDeElPrimerRectangulo = listaRectangulosContornos.get(0).area();

        if (areaDeElPrimerRectangulo > (areaDeLaImagenCompleta / 2)) {
            // Obtener el rectángulo más grande
            Rect rectanguloMasGrande = listaRectangulosContornos.get(0);

            // Crear submat del rectángulo más grande
            Mat submat = imagenConBordes.submat(rectanguloMasGrande);

            // Detectar contornos directamente en el submat (ya está procesado)
            List<MatOfPoint> subContornos = new ArrayList<>();
            Imgproc.findContours(submat.clone(), subContornos, new Mat(),
                    Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            
//            subContornos.remove(0);

            // Convertir los nuevos contornos a rectángulos y ajustar coordenadas
            List<Rect> subRectangulos = new ArrayList<>();

            for (MatOfPoint contorno : subContornos) {
                Rect rect = Imgproc.boundingRect(contorno);
                // Ajustar coordenadas relativas al rectángulo original
                rect.x += rectanguloMasGrande.x;
                rect.y += rectanguloMasGrande.y;
                subRectangulos.add(rect);
            }

            // Ordenar por área
            subRectangulos.sort((rect1, rect2) -> Double.compare(rect2.area(), rect1.area()));
            System.out.println("Contornos: "  + subRectangulos);

            // Tomar los primeros 6 rectángulos
            for (int i = 0; i < Math.min(6, subRectangulos.size()); i++) {
                listaRectangulos.add(subRectangulos.get(i));
            }
            
            listaRectangulos.subList(1, 5).sort((rect1, rect2) -> Integer.compare(rect1.x, rect2.x));
            System.out.println("Lista rectangulos grande: " + listaRectangulos);
            return listaRectangulos;
            
        } else {
            // Usar los primeros 6 rectángulos originales
            for (int i = 0; i < Math.min(6, listaRectangulosContornos.size()); i++) {
                listaRectangulos.add(listaRectangulosContornos.get(i));
            }
            
           listaRectangulos.subList(1, 5).sort((rect1, rect2) -> Integer.compare(rect1.x, rect2.x));
           System.out.println("Lista rectangulos: " + listaRectangulos);
           return listaRectangulos;
           
        } 
    }

    public static void procesarSeccionRespuestas(Mat imagen, Rect rectanguloPadre, List<Character> respuestasCorrectas, List<Character> respuestasAlumno, int seccion) {
        Mat imagenRecortada = imagen.submat(rectanguloPadre);

        int alturaSubRectangulo = rectanguloPadre.height / 10;
        int anchoSubRectangulo = rectanguloPadre.width;
        int indiceBase = (seccion - 1) * 10;

        try {
            for (int i = 0; i < 10; i++) {
                Rect subRectangulo = new Rect(
                        0,
                        i * alturaSubRectangulo,
                        anchoSubRectangulo,
                        alturaSubRectangulo
                );

                int indiceActual = indiceBase + i;
                Scalar color;
                if (indiceActual < respuestasAlumno.size() && indiceActual < respuestasCorrectas.size()) {
                    if (respuestasAlumno.get(indiceActual) == 'X') {
                        color = new Scalar(0, 255, 255);
                    } else if (respuestasAlumno.get(indiceActual).equals(respuestasCorrectas.get(indiceActual))) {
                        color = new Scalar(0, 255, 0);
                    } else {
                        color = new Scalar(0, 0, 255);
                    }
                } else {
                    color = new Scalar(0, 255, 255);
                }

                Imgproc.rectangle(imagenRecortada,
                        subRectangulo.tl(),
                        subRectangulo.br(),
                        color,
                        2);

                Mat subimagenRecortada = imagenRecortada.submat(subRectangulo);
                Mat subimagenRecortadaGris = UtilityApi.setConvertirEscalaGrises(subimagenRecortada);
                Mat subimagenBinarizada = UtilityApi.setBinarizarImagen(subimagenRecortadaGris);

                detectarCirculos(subimagenBinarizada, imagenRecortada, subRectangulo);
            }
        } catch (Exception e) {
            return;
        }
    }

    private static void detectarCirculos(Mat imagenGris, Mat imagenColor, Rect rect) throws Exception {
        Mat imagenBinarizada = UtilityApi.setBinarizarImagen(imagenGris);
        Mat circulos = new Mat();
        Imgproc.HoughCircles(imagenBinarizada, circulos, Imgproc.HOUGH_GRADIENT,
                1, imagenGris.cols() / 15, 100, 9, 6, 10);

        List<Circulos> circulosDetectados = new ArrayList<>();

        // Procesar los primeros 4 círculos detectados
        for (int i = 0; i < 4; i++) {
            double[] circ = circulos.get(0, i);
            if (circ != null) {
                Point centro = new Point(circ[0] + rect.x, circ[1] + rect.y);
                int radio = (int) Math.round(circ[2]);
                circulosDetectados.add(new Circulos(centro, radio));
            } else {
                mensajeEmergente.mostrarMensaje("Error al procesar las opciones.\nPor favor introduce una imagen más clara.");
                throw new Exception(); // Lanza una excepción para detener el proceso
            }
        }

        circulosDetectados.sort((c1, c2) -> Double.compare(c1.centro.x, c2.centro.x));
        ArrayList<Integer> pixelesPorCirculo = new ArrayList<>();

        for (Circulos circulo : circulosDetectados) {
            Imgproc.circle(imagenColor, circulo.centro, circulo.radio, new Scalar(0, 0, 0), 1);
            Rect rectCirculo = new Rect(
                    (int) (circulo.centro.x - circulo.radio),
                    (int) (circulo.centro.y - circulo.radio),
                    circulo.radio * 2,
                    circulo.radio * 2
            );

            rectCirculo = new Rect(
                    Math.max(0, rectCirculo.x),
                    Math.max(0, rectCirculo.y),
                    Math.min(imagenColor.cols() - rectCirculo.x, rectCirculo.width),
                    Math.min(imagenColor.rows() - rectCirculo.y, rectCirculo.height)
            );

            int pixelesNegros = UtilityApi.procesarCirculos(imagenColor, rectCirculo);
            pixelesPorCirculo.add(pixelesNegros);
        }

        if (pixelesPorCirculo.size() == 4) {
            listaDePixelesPorGrupos.add(new ArrayList<>(pixelesPorCirculo));
        } else {
            mensajeEmergente.mostrarMensaje("No se detectaron los circulos necesarios.\nPor favor, asegúrate que los círculos sean visibles y la imagen sea clara.");
            throw new Exception(); // Lanza una excepción para detener el proceso
        }
    }

    public static void procesarSeccionIdentificador(Mat imagenOriginal, Rect rectanguloPrincipal) {

        Mat imagenRecortada = imagenOriginal.submat(rectanguloPrincipal);

        Mat imagenEnEscalaGris = UtilityApi.setConvertirEscalaGrises(imagenRecortada);

        Mat imagenBinarizada = UtilityApi.setBinarizarImagen(imagenEnEscalaGris);

        List<Rect> listaDeSubRectangulos = detectarSubrectangulos(imagenBinarizada, true);

        // Dibujar los sub-rectángulos en la subimagen
        for (int j = 0; j < 1; j++) {

            Rect subRectangulo = listaDeSubRectangulos.get(j);

            // Dibujar el subrectángulo en la subimagen
            Imgproc.rectangle(imagenRecortada, subRectangulo.tl(), subRectangulo.br(), new Scalar(0, 255, 0), 2);

            // Recortar el subrectángulo
            Mat subImagenRecortada = imagenRecortada.submat(subRectangulo);

            Mat subImagenEscalaGrises = UtilityApi.setConvertirEscalaGrises(subImagenRecortada);

            detectarCirculosIdentificador(subImagenEscalaGrises, imagenRecortada, subRectangulo, false, false);
        }

        for (int j = 1; j < 3; j++) {
            Rect subRectangulo = listaDeSubRectangulos.get(j);

            // Dibujar el subrectángulo en la subimagen
            Imgproc.rectangle(imagenRecortada, subRectangulo.tl(), subRectangulo.br(), new Scalar(0, 255, 0), 2);

            // Recortar el subrectángulo
            Mat subImagenRecortada = imagenRecortada.submat(subRectangulo);

            Mat subImagenEscalaGrises = UtilityApi.setConvertirEscalaGrises(subImagenRecortada);

            detectarCirculosIdentificador(subImagenEscalaGrises, imagenRecortada, subRectangulo, true, false);
        }
    }

    public static void procesarSeccionNumeroExamen(Mat imagen, Rect rectangulo) {

        Mat imagenRecortada = imagen.submat(rectangulo);

        Mat imagenProcesada = procesarImagen(imagenRecortada);

        List<Rect> subRectangulos = detectarSubrectangulos(imagenProcesada, true);

        Rect rectNumeroExamen = subRectangulos.get(0);

        Imgproc.rectangle(imagenRecortada, rectNumeroExamen.tl(), rectNumeroExamen.br(), new Scalar(0, 255, 0), 2);

        Mat subimagenRecortada = imagenRecortada.submat(rectNumeroExamen);

        Mat subimagenEscalaGrises = UtilityApi.setConvertirEscalaGrises(subimagenRecortada);

        detectarCirculosIdentificador(subimagenEscalaGrises, imagenRecortada, rectNumeroExamen, false, true);
    }

    private static List<Rect> detectarSubrectangulos(Mat bordesSubimagen, Boolean esIdentificador) {

        Boolean identificador = esIdentificador;

        List<MatOfPoint> contornosSubimagen = new ArrayList<>();

        Imgproc.findContours(bordesSubimagen, contornosSubimagen, new Mat(),
                Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

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

        rectangulos.sort((rect1, rect2) -> compararRectangulos(rect1, rect2, identificador));

        return rectangulos;
    }

    private static int compararRectangulos(Rect rect1, Rect rect2, Boolean esIdentificador) {

        boolean rect1CumpleCriterio;                                            // Variables para almacenar si cada rectángulo cumple el criterio de dimensión
        boolean rect2CumpleCriterio;

        if (esIdentificador) {                                                  // Para identificadores, el ancho debe ser menor que la altura

            rect1CumpleCriterio = rect1.width < rect1.height;
            rect2CumpleCriterio = rect2.width < rect2.height;

        } else {                                                                // Para no identificadores, el ancho debe ser mayor que la altura

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

        int compruebaArea = Double.compare(area2, area1);                 // Si ambos cumplen el criterio, compara por área
        if (compruebaArea != 0) {

            return compruebaArea;
        }

        int compararCordenadaY = Double.compare(rect1.tl().y, rect2.tl().y);// Si las áreas son iguales, compara por coordenada Y

        if (compararCordenadaY != 0) {

            return compararCordenadaY;
        }

        return Double.compare(rect2.tl().x, rect1.tl().x);                // Si todo lo anterior es igual, compara por coordenada X 

    }

    private static void detectarCirculosIdentificador(Mat imagenGris, Mat imagenColor, Rect rect, Boolean sonLasLetrasIdentificador, Boolean esElNumeroExamen) {

        ArrayList<Integer> listaTemporal = new ArrayList<>();                   // Lista temporal para almacenar los píxeles procesados

        Mat circulos = new Mat();                                               // Matriz para almacenar los círculos detectados

        Imgproc.HoughCircles(imagenGris, circulos, Imgproc.HOUGH_GRADIENT, 1,
                imagenGris.cols() / 15, 100, 12, 6, 11);

        List<Circulos> todosLosCirculos = new ArrayList<>();                    // Lista para almacenar todos los círculos

        // Recolectar todos los círculos
        for (int i = 0; i < circulos.cols(); i++) {

            double[] circ = circulos.get(0, i);

            if (circ != null) {

                // Crear punto central ajustando las coordenadas
                Point centro = new Point(circ[0] + rect.x, circ[1] + rect.y);
                int radio = (int) Math.round(circ[2]);

                todosLosCirculos.add(new Circulos(centro, radio));
            } else {
                mensajeEmergente.mostrarMensaje("Error al procesar las opciones.\nPor favor introduce una imagen más clara.");
                break;
            }
        }

        // Ordenar círculos según el tipo de procesamiento
        if (sonLasLetrasIdentificador) {

            todosLosCirculos.sort((c1, c2) -> {                                 // Para letras: ordenar primero por Y, luego por X

                int comparacionY = Double.compare(c1.centro.y, c2.centro.y);
                return (comparacionY != 0) ? comparacionY : Double.compare(c1.centro.x, c2.centro.x);
            });

        } else {

            todosLosCirculos.sort((c1, c2) -> Double.compare(c1.centro.x, c2.centro.x));  // Para números: ordenar solo por X
        }

        if (sonLasLetrasIdentificador) {

            // Procesar cada círculo
            for (Circulos circulo : todosLosCirculos) {

                Imgproc.circle(imagenColor, circulo.centro, circulo.radio,
                        new Scalar(0, 0, 0), 1);

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

                // Procesar subimagen si el rectángulo es válido
                if (rectCirculo.width > 0 && rectCirculo.height > 0) {
                    int imagenProcesada = UtilityApi.procesarCirculos(imagenColor, rectCirculo);

                    listaPixelesLetrasIdentificador.add(imagenProcesada);

                }
            }

        } else {

            List<List<Circulos>> gruposDeCirculos = new ArrayList<>();          // Crear lista de listas para agrupar los círculos en grupos de 10

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

                        int imagenProcesada = UtilityApi.procesarCirculos(imagenColor, rectCirculo);

                        listaTemporal.add(imagenProcesada);

                        if (listaTemporal.size() < 10) {

                            System.out.println(listaTemporal.size());

                        } else if (listaTemporal.size() == 10) {
                            if (esElNumeroExamen) {
                                listaDePixelesPorGruposNumeroExamen.add(new ArrayList<>(listaTemporal));

                                listaTemporal.clear();
                            } else {
                                listaDePixelesPorGruposIdentificador.add(new ArrayList<>(listaTemporal));

                                listaTemporal.clear();
                            }
                        }
                    }
                }
            }
        }
    }

    public static ArrayList<Character> crearRespuestas() {

        ArrayList<Character> resultado = new ArrayList<>();                     // Lista para almacenar las respuestas procesadas
        final int UMBRAL_RESPUESTA_MARCADA = 220;                               // Umbral mínimo de píxeles negros para considerar una respuesta como marcada
        int contadorDeRespuestasVacias = 0;                                     // Contador para respuestas vacías o inválidas

        // Procesar cada grupo de respuestas
        for (int i = 0; i < listaDePixelesPorGrupos.size(); i++) {

            ArrayList<Integer> grupo = listaDePixelesPorGrupos.get(i);
            int maxPixeles = 0;                                                 // Cantidad máxima de píxeles negros encontrados
            int indiceMayor = -1;                                               // Índice de la opción con más píxeles negros
            int respuestasMarcadas = 0;                                         // Contador para respuestas que superan el umbral

            // Primero contar cuántas respuestas superan el umbral
            for (int j = 0; j < grupo.size(); j++) {
                if (grupo.get(j) >= UMBRAL_RESPUESTA_MARCADA) {
                    respuestasMarcadas++;
                }

                // Encontrar la opción con más píxeles negros
                if (grupo.get(j) > maxPixeles) {
                    maxPixeles = grupo.get(j);
                    indiceMayor = j;
                }
            }

            char letraAsignada;

            if (respuestasMarcadas > 1) {                                       // Si hay más de una opción marcada, se considera inválida
                letraAsignada = 'X';
                contadorDeRespuestasVacias++;

            } else if (maxPixeles < UMBRAL_RESPUESTA_MARCADA) {                 // Si ninguna opción supera el umbral, se considera sin respuesta
                letraAsignada = 'X';
                contadorDeRespuestasVacias++;

            } else {

                // Asignar la letra correspondiente según la opción más marcada
                switch (indiceMayor) {
                    case 0 ->
                        letraAsignada = 'A';
                    case 1 ->
                        letraAsignada = 'B';
                    case 2 ->
                        letraAsignada = 'C';
                    case 3 ->
                        letraAsignada = 'D';
                    default ->
                        letraAsignada = 'X';
                }
            }
            resultado.add(letraAsignada);                                     // Agregar la respuesta procesada al resultado
        }
        if (contadorDeRespuestasVacias > 0) {
            mensajeEmergente.mostrarMensaje("Se encontraron " + contadorDeRespuestasVacias + " respuestas sin contestar.");
        }
        System.out.println("lista pixeles: " + listaDePixelesPorGrupos);
        return resultado;
    }
}
