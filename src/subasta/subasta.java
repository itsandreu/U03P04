package subasta;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class subasta {

    private static String[] usuarios = {"admin"};
    private static String[] passwords = {"subas"};
    private static String descripcion = null;
    private static Date limite = null;
    private static List<String> ofertasUsuarios = new ArrayList<>();
    private static List<Double> ofertas = new ArrayList<>();
    private static int usuarioActual = -1; 
    private static long ultimaAccion = System.currentTimeMillis();

    public static void main(String[] args) {
        cargarDatos();
        mostrarMenu();
    }
    private static void verificarSesion() {
        if (usuarioActual != -1 && System.currentTimeMillis() - ultimaAccion > 60000) {
            System.out.println("Sesión expirada.");
            usuarioActual = -1;
        }
        ultimaAccion = System.currentTimeMillis();
    }

    private static void mostrarMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            verificarSesion();
            System.out.println("\nSUBASTAS\n********");
            System.out.println("1. " + (usuarioActual == -1 ? "Login" : "Logout(" + usuarios[usuarioActual] + ")"));
            System.out.println("2. Hacer una oferta.");
            System.out.println("3. Modificar datos.");
            System.out.println("4. Listado de ofertas.");
            System.out.println("5. Salir");
            System.out.print("Escoja una opción (1-5): ");

            int opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {
                case 1:
                    loginLogout(scanner);
                    break;
                case 2:
                    hacerOferta(scanner);
                    break;
                case 3:
                    modificarDatos(scanner);
                    break;
                case 4:
                    listarOfertas();
                    break;
                case 5:
                    guardarDatos();
                    System.out.println("Saliendo del programa...");
                    return;
                default:
                    System.out.println("Opción no válida.");
            }
        }
    }

    private static void loginLogout(Scanner scanner) {
        if (usuarioActual != -1) {
            usuarioActual = -1;
            System.out.println("Sesión cerrada.");
            return;
        }

        System.out.print("Ingrese usuario (NIF/NIE o 'admin'): ");
        String usuario = scanner.nextLine();

        if ("admin".equals(usuario)) {
            System.out.print("Ingrese password: ");
            String password = scanner.nextLine();

            if (password.equals(passwords[0])) {
                usuarioActual = 0;
                System.out.println("Sesión iniciada como admin.");
            } else {
                System.out.println("Password incorrecto.");
            }
            return;
        }

        if (!esNifNieValido(usuario)) {
            System.out.println("NIF/NIE no válido.");
            return;
        }

        int pos = buscarUsuario(usuario);
        if (pos == -1) {
            usuarios = Arrays.copyOf(usuarios, usuarios.length + 1);
            passwords = Arrays.copyOf(passwords, passwords.length + 1);
            usuarios[usuarios.length - 1] = usuario;

            System.out.print("Cree un password: ");
            String password = scanner.nextLine();
            while (password.isBlank() || password.contains(" ")) {
                System.out.println("Password no puede estar vacío ni contener espacios.");
                System.out.print("Cree un password: ");
                password = scanner.nextLine();
            }
            passwords[passwords.length - 1] = password;
            usuarioActual = usuarios.length - 1;
            System.out.println("Usuario registrado e iniciado sesión.");
        } else {
            System.out.print("Ingrese password: ");
            String password = scanner.nextLine();

            if (password.equals(passwords[pos])) {
                usuarioActual = pos;
                System.out.println("Sesión iniciada.");
            } else {
                System.out.println("Usuario o password incorrecto.");
            }
        }
    }

    private static void hacerOferta(Scanner scanner) {
        if (usuarioActual <= 0) {
            System.out.println("Debe iniciar sesión con un NIF/NIE para hacer una oferta.");
            return;
        }

        if (descripcion == null || limite == null) {
            System.out.println("Servicio o fecha límite pendientes de definir.");
            return;
        }

        if (new Date().after(limite)) {
            System.out.println("El plazo de presentación ha expirado.");
            return;
        }

        System.out.println("Descripción: " + descripcion);
        System.out.println("Límite: " + limite);
        System.out.print("Ingrese su oferta (€): ");

        try {
            double oferta = scanner.nextDouble();
            scanner.nextLine();
            int pos = ofertasUsuarios.indexOf(usuarios[usuarioActual]);
            if (pos != -1) {
                ofertas.set(pos, oferta);
            } else {
                ofertasUsuarios.add(usuarios[usuarioActual]);
                ofertas.add(oferta);
            }
            System.out.println("Oferta registrada.");
        } catch (InputMismatchException e) {
            System.out.println("Entrada no válida.");
            scanner.nextLine();
        }
    }

    private static void modificarDatos(Scanner scanner) {
        if (usuarioActual != 0) {
            System.out.println("Debe ser 'admin' para modificar datos.");
            return;
        }

        System.out.println("Descripción actual: " + (descripcion == null ? "" : descripcion));
        System.out.print("Nueva descripción (Enter no cambia): ");
        String nuevaDescripcion = scanner.nextLine();
        if (!nuevaDescripcion.isBlank()) {
            descripcion = nuevaDescripcion;
        }

        System.out.print("Nuevo password de admin (Enter no cambia): ");
        String nuevoPassword = scanner.nextLine();
        if (!nuevoPassword.isBlank()) {
            passwords[0] = nuevoPassword;
        }
        System.out.print("Nueva fecha límite (dd/MM/yyyy HH:mm:ss, Enter no cambia): ");
        String nuevaFecha = scanner.nextLine();
        if (!nuevaFecha.isBlank()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                limite = sdf.parse(nuevaFecha);
            } catch (ParseException e) {
                System.out.println("Formato de fecha no válido.");
            }
        }
    }

    private static void listarOfertas() {
        if (usuarioActual != 0 || (limite != null && new Date().before(limite))) {
            System.out.println("Debe ser 'admin' y haber vencido el límite para listar ofertas.");
            return;
        }

        System.out.println("\nSUBASTAS-LISTADO DE OFERTAS\n***************************");
        System.out.println("Descripción: " + descripcion);
        System.out.println("Límite: " + limite);

        System.out.println("+-----------+--------------+");
        System.out.println("| NIF/NIE   | OFERTA (€)   |");
        System.out.println("+-----------+--------------+");
        for (int i = 0; i < ofertasUsuarios.size(); i++) {
            System.out.printf("| %-9s | %12.2f |\n", ofertasUsuarios.get(i), ofertas.get(i));
        }
        System.out.println("+-----------+--------------+");
    }

    private static boolean esNifNieValido(String nif) {
        if (nif == null || nif.length() != 9) {
            return false;
        }

        String letrasControl = "TRWAGMYFPDXBNJZSQVHLCKE";
        
        if (nif.charAt(0) == 'X' || nif.charAt(0) == 'Y' || nif.charAt(0) == 'Z') {
            int primerDigito = nif.charAt(0) == 'X' ? 0 : (nif.charAt(0) == 'Y' ? 1 : 2);
            nif = primerDigito + nif.substring(1);
        }


        if (!nif.substring(0, 8).matches("\\d{8}")) {
            return false;
        }

        int numero = Integer.parseInt(nif.substring(0, 8));

        char letraControl = nif.charAt(8);

        char letraCalculada = letrasControl.charAt(numero % 23);

        return letraControl == letraCalculada;
    }


    private static int buscarUsuario(String usuario) {
        for (int i = 0; i < usuarios.length; i++) {
            if (usuarios[i].equals(usuario)) {
                return i;
            }
        }
        return -1;
    }
    private static void cargarDatos() {
        try (BufferedReader br = new BufferedReader(new FileReader("subasta.datos"))) {
            String linea = br.readLine();
            if (linea == null || linea.isBlank()) return;

            String[] partes = linea.split(";");
            usuarios = partes[0].split(",");
            passwords = partes[1].split(",");
            descripcion = partes[2].equals("null") ? null : partes[2];
            limite = partes[3].equals("null") ? null : new Date(Long.parseLong(partes[3]));

            ofertasUsuarios.clear();
            ofertas.clear();

            if (partes.length > 4) {
                String[] usuariosOfertas = partes[4].split(",");
                String[] valoresOfertas = partes[5].split(",");

                for (int i = 0; i < usuariosOfertas.length; i++) {
                    ofertasUsuarios.add(usuariosOfertas[i]);
                    ofertas.add(Double.parseDouble(valoresOfertas[i]));
                }
            }
        } catch (IOException e) {
            System.out.println("No se pudo cargar el archivo de datos.");
        }
    }

    private static void guardarDatos() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("subasta.datos"))) {
            StringBuilder datos = new StringBuilder();
            datos.append(String.join(",", usuarios)).append(";");
            datos.append(String.join(",", passwords)).append(";");
            datos.append(descripcion == null ? "null" : descripcion).append(";");
            datos.append(limite == null ? "null" : limite.getTime()).append(";");

            if (!ofertasUsuarios.isEmpty()) {
                datos.append(String.join(",", ofertasUsuarios)).append(";");
                for (int i = 0; i < ofertas.size(); i++) {
                    datos.append(i > 0 ? "," : "").append(ofertas.get(i));
                }
            }

            pw.println(datos);
        } catch (IOException e) {
            System.out.println("No se pudo guardar el archivo de datos.");
        }
    }
}