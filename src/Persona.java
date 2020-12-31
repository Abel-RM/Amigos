import java.time.LocalDate;
import java.util.Objects;

public class Persona {
    String nombre;
    String apellido;
    Genero sexo;
    LocalDate fechaNacimiento;

    public Persona(String nombre, String apellido, LocalDate fechaNacimiento,Genero sexo) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
    }

    @Override
    public String toString() {
        return "{"+nombre + " , "+ apellido + " , " + sexo +
                " , " + fechaNacimiento.getDayOfMonth()+"-"+fechaNacimiento.getMonthValue()+"-"+fechaNacimiento.getYear()+"}";

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Persona persona = (Persona) o;
        return nombre.equals(persona.nombre) &&
                apellido.equals(persona.apellido) &&
                sexo == persona.sexo &&
                fechaNacimiento.equals(persona.fechaNacimiento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, apellido, sexo, fechaNacimiento);
    }

    public String getNombre() {
        return nombre;
    }
}
