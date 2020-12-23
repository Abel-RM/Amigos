import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Amigos {
    final static String rutaArchivoEntrada = "entrada.txt";
    final static String rutaArchivoSalida = "ignorado.txt";

    final static String formatoPersona = "(\\{[a-zA-Z]+,[a-zA-Z]+,(MASCULINO|FEMENINO),\\d{2}-\\d{2}-\\d{4}\\})|[0-9]+";
    final static String formatoregistro = "(\\{[a-zA-Z]+,[a-zA-Z]+,(MASCULINO|FEMENINO),\\d{2}-\\d{2}-\\d{4}\\})";
    final static String formatoAmistad = "("+formatoPersona+") amigo ("+formatoPersona+")";
    final static String formatoEliminar = "("+formatoPersona+") eliminar ("+formatoPersona+")";
    final static String formatoConexion = "("+formatoPersona+") conexion ("+formatoPersona+")";
    final static String formatoListaAmigos ="(amigos ("+formatoPersona+"),[0-9]+)/(amigos [0-9]+,("+formatoPersona+"))";
    final static String patternPersona = "(\\{[a-zA-Z]+,[a-zA-Z]+,(MASCULINO|FEMENINO),\\d{2}-\\d{2}-\\d{4}\\})";

    static ArrayList<Persona> catalogo = new ArrayList<>();
    static LinkedHashMap<Persona, ArrayList<Persona>> red = new LinkedHashMap<>();

    public static void main(String[] args) {
        leerArchivo();
        System.out.println(catalogo);

    }
    public static void leerArchivo() {
        String ignoradas = "";
        String nombre = "", apellido = "";
        Genero sexo;
        Calendar fecNac;

        try {
            File archivoEntrada = new File(rutaArchivoEntrada);
            Scanner scIn = new Scanner(archivoEntrada);
            String linea = "";

            while (scIn.hasNextLine()) {
                linea = scIn.nextLine();

                if (linea.matches(formatoregistro)) {
                    //System.out.println("formato registro");
                    insertarPersonaCatalogo(crearPersona(linea));
                }
                else
                    if (linea.matches(formatoAmistad)) {
                        //System.out.println("formato amistad");
                        String persona1 = linea.split(" amigo ")[0];
                        String persona2 = linea.split(" amigo ")[1];

                        Persona p1 = null,p2=null;
                        if (persona1.matches(patternPersona)){
                            p1 = crearPersona(persona1);
                            insertarPersonaCatalogo(p1);
                        }else
                            if(Integer.parseInt(persona1) >= 0 && catalogo.size() > Integer.parseInt(persona1) ){
                                p1 = catalogo.get(Integer.parseInt(persona1));
                            }

                        if (persona2.matches(patternPersona)){
                            p2 = crearPersona(persona2);
                            insertarPersonaCatalogo(p2);
                        }
                        else
                            if(Integer.parseInt(persona2) >= 0 && catalogo.size() > Integer.parseInt(persona2) ){
                                p2 = catalogo.get(Integer.parseInt(persona2));
                            }

                        if (p1 != null && p2 != null)
                            insertarPersonaRed(p1,p2);

                    }
                    else
                        if (linea.matches(formatoEliminar)) {
                            //System.out.println("formato eliminar");

                        }
                        else
                            if (linea.matches(formatoConexion)) {
                                //System.out.println("formato conexion");

                            }
                            else
                                if (linea.matches(formatoListaAmigos)) {
                                    //System.out.println("formato lista amiga");

                                }
                                else{
                                    //System.out.println("ignorar");
                                    if (linea.length() != 0 )
                                        ignoradas += linea+"\n";
                                }

            }
            scIn.close();
        } catch (FileNotFoundException e) {
            System.out.println("Ocurrió un error al leer un archivo.");
        }

        //escribir las lineas ignoradas
        try (FileWriter fw = new FileWriter(rutaArchivoSalida)){
            fw.write(ignoradas);
            fw.flush();

        }catch (IOException e){
            System.out.println("Ocurrió un error al escribir en el archivo.");
        }
    }

    private static void insertarPersonaCatalogo(Persona p1) {
        if (!catalogo.contains(p1))
            catalogo.add(p1);
    }

    private static void insertarPersonaRed(Persona p1, Persona p2) {
        if (red.get(p1) != null)
            red.get(p1).add(p2);
        else
            red.put(p1,new ArrayList<>(Arrays.asList(p2)));

        if (red.get(p2) != null)
            red.get(p2).add(p1);
        else
            red.put(p2,new ArrayList<>(Arrays.asList(p1)));


    }

    private static Persona crearPersona(String persona) {
        String nombre = persona.replaceAll("(,[a-zA-Z]+,(MASCULINO|FEMENINO),\\d{2}-\\d{2}-\\d{4}\\})","").replaceAll("\\{","");
        String apellido = persona.replaceFirst("(\\{[a-zA-Z]+,)","").replaceAll("(,(MASCULINO|FEMENINO),\\d{2}-\\d{2}-\\d{4}\\})","");
        String sexo = persona.replaceAll("(\\{[a-zA-Z]+,[a-zA-Z]+,)","").replaceAll("(,\\d{2}-\\d{2}-\\d{4}\\})","");
        String fecha = persona.replaceAll("(\\{[a-zA-Z]+,[a-zA-Z]+,(MASCULINO|FEMENINO),)","").replaceAll("(\\})","");

        LocalDate fechaNac = LocalDate.of(
                Integer.parseInt(fecha.substring(6,10)),
                Integer.parseInt(fecha.substring(3,5)),
                Integer.parseInt(fecha.substring(0,2)));

        if (sexo.equals("MASCULINO"))
            return new Persona(nombre,apellido,fechaNac,Genero.MASCULINO);
        else
            return new Persona(nombre,apellido,fechaNac,Genero.FEMENINO);
    }

    static String incluirFecha(String linea){
        Pattern patternFecha = Pattern.compile("\\d{2}-\\d{2}-\\d{4}", Pattern.CASE_INSENSITIVE);
        //validar que la fecha sea valida
        String fecha = "01-01-1900";
        Matcher matcher = patternFecha.matcher(linea);
        if (matcher.find())
            fecha = matcher.group();
        if (!validarFecha(fecha))
            return linea+"\n";
        return "";
    }

    public static boolean validarFecha(String fecha) {
        fecha = fecha.replaceAll("-","/");
        try {
            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
            formatoFecha.setLenient(false);
            formatoFecha.parse(fecha);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }
}
