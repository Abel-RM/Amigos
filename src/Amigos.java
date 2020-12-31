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
        System.out.println("-------------------Catalogo------------------");
        System.out.println(catalogo);
        System.out.println("--------------------Red----------------------");
        System.out.println(red);
        System.out.println("---------------------------------------------");

    }
    public static void leerArchivo() {
        StringBuilder ignoradas = new StringBuilder();

        try {
            File archivoEntrada = new File(rutaArchivoEntrada);
            Scanner scIn = new Scanner(archivoEntrada);
            String linea;

            while (scIn.hasNextLine()) {
                linea = scIn.nextLine();

                if (linea.matches(formatoregistro)) {

                    if (linea.matches(patternPersona)){
                        if (validarFecha(linea))
                            insertarPersonaCatalogo(crearPersona(linea,true));
                        else
                            ignoradas.append(linea+"\n");
                    }
                    else
                        insertarPersonaCatalogo(crearPersona(linea,true));

                }
                else
                    if (linea.matches(formatoAmistad)) {
                        String persona1 = linea.split(" amigo ")[0];
                        String persona2 = linea.split(" amigo ")[1];
                        Persona p1;
                        Persona p2;

                        if (validarFecha(persona1) && validarFecha(persona2)){

                            p1 = obtenerPersona(persona1,true);
                            p2 = obtenerPersona(persona2,true);

                            insertarPersonaRed(p1,p2);
                        }
                        else
                            ignoradas.append(linea+"\n");

                    }
                    else
                        if (linea.matches(formatoEliminar)) {
                            String persona1 = linea.split(" eliminar ")[0];
                            String persona2 = linea.split(" eliminar ")[1];

                            Persona p1 = obtenerPersona(persona1,false);
                            Persona p2 = obtenerPersona(persona2,false);

                            if(p1 != null && p2 != null){
                                red.get(p1).remove(p2);
                                red.get(p2).remove(p1);
                            }
                            else
                                ignoradas.append(linea+"\n");


                        }
                        else
                            if (linea.matches(formatoConexion)) {
                                String persona1 = linea.split(" conexion ")[0];
                                String persona2 = linea.split(" conexion ")[1];

                                Persona p1 = obtenerPersona(persona1,false);
                                Persona p2 = obtenerPersona(persona2,false);
                                if (p1 == null || p2 == null)
                                    ignoradas.append(linea+"\n");
                                else if (sonAmigos(p1,p2))
                                    System.out.println(p1.getNombre() +" y "+ p2.getNombre() +" son amigos");
                                else
                                    System.out.println(p1.getNombre() +" y "+ p2.getNombre() +" no son amigos");
                            }
                            else
                                if (linea.matches(formatoListaAmigos)) {
                                    int inicio = linea.indexOf("{");
                                    int fin = linea.indexOf("}");
                                    String persona1 = linea.substring(inicio,fin+1);
                                    Persona p1 = obtenerPersona(persona1,false);

                                    int niv = Integer.parseInt(linea.split(patternPersona+",")[1]);

                                    if (p1 == null)
                                        System.out.println(persona1 +" no esta en el catalogo");
                                    else{
                                        System.out.println("\n");
                                        System.out.println("-----------------Amigos nivel "+niv+"------------------------");
                                        System.out.println(listarAmigosNivel(p1,niv));
                                        System.out.println("-----------------Amigos nivel "+niv+"------------------------");
                                        System.out.println("\n");
                                    }


                                }
                                else{
                                    if (linea.length() != 0 )
                                        ignoradas.append(linea).append("\n");
                                }

            }
            scIn.close();
        } catch (FileNotFoundException e) {
            System.out.println("Ocurrió un error al leer un archivo.");
        }

        //escribir las lineas ignoradas
        try (FileWriter fw = new FileWriter(rutaArchivoSalida)){
            fw.write(ignoradas.toString());
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
            red.put(p1,new ArrayList<>(Collections.singletonList(p2)));

        if (red.get(p2) != null)
            red.get(p2).add(p1);
        else
            red.put(p2,new ArrayList<>(Collections.singletonList(p1)));


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

        }else if(Integer.parseInt(persona) >= 0 && catalogo.size() > Integer.parseInt(persona) ){
                p = catalogo.get(Integer.parseInt(persona));
            }

        return p;
    }

    public static boolean validarFecha(String linea){
        Pattern patternFecha = Pattern.compile("\\d{2}-\\d{2}-\\d{4}", Pattern.CASE_INSENSITIVE);
        //validar que la fecha sea valida
        String fecha="01-01-1900";
        Matcher matcher = patternFecha.matcher(linea);
        if (matcher.find())
            fecha = matcher.group();

        return existeFecha(fecha);
    }

    public static boolean existeFecha(String fecha){
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

        return  listarAmigosNivel(persona,nivel,0,new HashSet<>(), new ArrayList<>());
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
