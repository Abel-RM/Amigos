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
    final static String formatoListaAmigos ="(amigos ("+formatoPersona+"),[0-9]+)|(amigos [0-9]+,[0-9]+)";
    final static String patternPersona = "(\\{[a-zA-Z]+,[a-zA-Z]+,(MASCULINO|FEMENINO),\\d{2}-\\d{2}-\\d{4}\\})";

    static ArrayList<Persona> catalogo = new ArrayList<>();
    static LinkedHashMap<Persona, ArrayList<Persona>> red = new LinkedHashMap<>();

    public static void main(String[] args) {
        leerArchivo();
        System.out.println("-----------------------");
        //System.out.println(catalogo);
        System.out.println("-----------------------");
        System.out.println(red);
        System.out.println("-----------------------");

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
                    insertarPersonaCatalogo(crearPersona(linea,true));
                }
                else
                    if (linea.matches(formatoAmistad)) {
                        //System.out.println("formato amistad");
                        String persona1 = linea.split(" amigo ")[0];
                        String persona2 = linea.split(" amigo ")[1];

                        Persona p1 = obtenerPersona(persona1,true);
                        Persona p2 = obtenerPersona(persona2,true);

                        if (p1 == null && p2 != null)
                            System.out.println(persona1 +" no estan en el catalogo");
                        else if(p1 != null && p2 == null)
                            System.out.println(persona2 +" no estan en el catalogo");
                        else if (p1 == null && p2 == null)
                            System.out.println(persona1 +" y "+ persona2 +" no estan en el catalogo");
                        else if (p1 != null && p2 != null && p1 != p2)
                            insertarPersonaRed(p1,p2);

                    }
                    else
                        if (linea.matches(formatoEliminar)) {
                            String persona1 = linea.split(" eliminar ")[0];
                            String persona2 = linea.split(" eliminar ")[1];

                            Persona p1 = obtenerPersona(persona1,false);
                            Persona p2 = obtenerPersona(persona2,false);

                            if(p1 != null && p2 != null){
                                if (red.get(p1) != null && red.get(p1).contains(p2))
                                    red.get(p1).remove(red.get(p1).indexOf(p2));

                                if (red.get(p2) != null && red.get(p2).contains(p1))
                                    red.get(p2).remove(red.get(p2).indexOf(p1));
                            }


                        }
                        else
                            if (linea.matches(formatoConexion)) {
                                String persona1 = linea.split(" conexion ")[0];
                                String persona2 = linea.split(" conexion ")[1];

                                Persona p1 = obtenerPersona(persona1,false);
                                Persona p2 = obtenerPersona(persona2,false);
                                if (p1 == null && p2 != null)
                                    System.out.println(persona1 +" no esta en el catalogo");
                                else if(p1 != null && p2 == null)
                                    System.out.println(persona2 +" no esta en el catalogo");
                                else if (p1 == null && p2 == null)
                                    System.out.println(persona1 +" y "+ persona2 +" no estan en el catalogo");
                                else if (sonAmigos(p1,p2))
                                    System.out.println(p1.getNombre() +" y "+ p2.getNombre() +" son amigos");
                                else
                                    System.out.println(p1.getNombre() +" y "+ p2.getNombre() +" no son amigos");
                            }
                            else
                                if (linea.matches(formatoListaAmigos)) {
                                    String persona1 = linea.split("amigos ")[1].replaceAll("(,[0-9]+)","");
                                    Persona p1 = obtenerPersona(persona1,false);
                                    System.out.println("aqui");
                                    System.out.println(listarAmigosNivel(p1,4));
                                    System.out.println("es");

                                }
                                else{
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

    private static boolean sonAmigos(Persona p1, Persona p2){
        return red.get(p1).contains(p2);
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

    private static Persona crearPersona(String persona, boolean crear) {
        String nombre = persona.replaceAll("(,[a-zA-Z]+,(MASCULINO|FEMENINO),\\d{2}-\\d{2}-\\d{4}\\})","").replaceAll("\\{","");
        String apellido = persona.replaceFirst("(\\{[a-zA-Z]+,)","").replaceAll("(,(MASCULINO|FEMENINO),\\d{2}-\\d{2}-\\d{4}\\})","");
        String sexo = persona.replaceAll("(\\{[a-zA-Z]+,[a-zA-Z]+,)","").replaceAll("(,\\d{2}-\\d{2}-\\d{4}\\})","");
        String fecha = persona.replaceAll("(\\{[a-zA-Z]+,[a-zA-Z]+,(MASCULINO|FEMENINO),)","").replaceAll("(\\})","");

        LocalDate fechaNac = LocalDate.of(
                Integer.parseInt(fecha.substring(6,10)),
                Integer.parseInt(fecha.substring(3,5)),
                Integer.parseInt(fecha.substring(0,2)));
        Persona p;
        if (sexo.equals("MASCULINO"))
            p = new Persona(nombre,apellido,fechaNac,Genero.MASCULINO);
        else
            p = new Persona(nombre,apellido,fechaNac,Genero.FEMENINO);

        if (catalogo.contains(p))
            return catalogo.get(catalogo.indexOf(p));
        else if (crear)
            return p;
        else
            return null;
    }

    public static Persona obtenerPersona(String persona, boolean crear){
        Persona p = null;
        if (persona.matches(patternPersona)){
            p = crearPersona(persona,crear);
            if (crear)
                insertarPersonaCatalogo(p);
        }else
            if(Integer.parseInt(persona) >= 0 && catalogo.size() > Integer.parseInt(persona) ){
                p = catalogo.get(Integer.parseInt(persona));
            }

        return p;
    }

    public static String incluirFecha(String linea){
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

    public static HashSet<Persona> listarAmigosNivel(Persona persona, int nivel){
        ArrayList<Persona> visitados = new ArrayList<>(Arrays.asList(persona));
        return  listarAmigosNivel(persona,nivel,0,new HashSet<>(), visitados);
    }

    public static HashSet<Persona> listarAmigosNivel(Persona persona, int nivel, int nivelActual, HashSet<Persona> listaAmigos, ArrayList<Persona> nodosVisitados){
        nodosVisitados.add(persona);
        if (nivelActual == nivel){
            listaAmigos.add(persona);
            return listaAmigos;
        }

        for (Persona p: red.get(persona)){
            if (!nodosVisitados.contains(p)){
                nodosVisitados.add(p);
                listaAmigos.addAll(listarAmigosNivel(p,nivel,(1+nivelActual),listaAmigos,nodosVisitados));
            }

        }
        return listaAmigos;
    }
}
