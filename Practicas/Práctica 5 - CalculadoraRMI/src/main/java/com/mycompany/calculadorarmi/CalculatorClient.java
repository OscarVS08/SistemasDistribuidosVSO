package com.mycompany.calculadorarmi;

import java.rmi.Naming;
import java.util.Scanner;

public class CalculatorClient {
    public static void main(String[] args) {
        try {
            // Conectar con el servidor RMI
            Calculator calc = (Calculator) Naming.lookup("//localhost/CalculatorService");
            
            Scanner scanner = new Scanner(System.in);
            double num1, num2;
            int opcion;
            
            while (true) {
                // Mostrar el menú de operaciones
                System.out.println("\n--- Calculadora RMI ---");
                System.out.println("1. Suma");
                System.out.println("2. Resta");
                System.out.println("3. Multiplicación");
                System.out.println("4. División");
                System.out.println("5. Salir");
                System.out.print("Seleccione una opción: ");
                opcion = scanner.nextInt();
                
                if (opcion == 5) {
                    System.out.println("Saliendo de la calculadora...");
                    break;
                }
                
                // Pedir números al usuario
                System.out.print("Ingrese el primer número: ");
                num1 = scanner.nextDouble();
                System.out.print("Ingrese el segundo número: ");
                num2 = scanner.nextDouble();
                
                // Realizar la operación elegida
                switch (opcion) {
                    case 1:
                        System.out.println("Resultado: " + calc.add(num1, num2));
                        break;
                    case 2:
                        System.out.println("Resultado: " + calc.subtract(num1, num2));
                        break;
                    case 3:
                        System.out.println("Resultado: " + calc.multiply(num1, num2));
                        break;
                    case 4:
                        if (num2 == 0) {
                            System.out.println("Error: No se puede dividir entre cero.");
                        } else {
                            System.out.println("Resultado: " + calc.divide(num1, num2));
                        }
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            }
            
            scanner.close();
        } catch (Exception e) {
            System.err.println("Error en el cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
