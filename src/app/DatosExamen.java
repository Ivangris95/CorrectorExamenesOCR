
package app;

public class DatosExamen {
    
    private String nota;
    private String numeroExamen;
    private char letraDNI;
    private char letraNIE;
    private String numeroIdentificador;
    
    public DatosExamen() {
        this.nota = "";
        this.numeroExamen = "";
        this.letraDNI = ' ';
        this.letraNIE = ' ';
        this.numeroIdentificador = "";
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public String getNumeroExamen() {
        return numeroExamen;
    }

    public void setNumeroExamen(String numeroExamen) {
        this.numeroExamen = numeroExamen;
    }

    public char getLetraDNI() {
        return letraDNI;
    }

    public void setLetraDNI(char letraDNI) {
        this.letraDNI = letraDNI;
    }

    public char getLetraNIE() {
        return letraNIE;
    }

    public void setLetraNIE(char letraNIE) {
        this.letraNIE = letraNIE;
    }

    public String getNumeroIdentificador() {
        return numeroIdentificador;
    }

    public void setNumeroIdentificador(String numeroIdentificador) {
        this.numeroIdentificador = numeroIdentificador;
    }
    
    public void limpiarDatos() {
        this.nota = "";
        this.numeroExamen = "";
        this.letraDNI = ' ';
        this.letraNIE = ' ';
        this.numeroIdentificador = "";
    }
    
    public void limpiarLetrasIdentificador(){
        this.letraDNI = ' ';
        this.letraNIE = ' ';
    }
    
}
