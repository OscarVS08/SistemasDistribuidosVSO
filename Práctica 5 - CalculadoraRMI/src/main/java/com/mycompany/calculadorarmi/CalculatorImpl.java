package com.mycompany.calculadorarmi;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

public class CalculatorImpl extends UnicastRemoteObject implements Calculator {
    protected CalculatorImpl() throws RemoteException {
        super();
    }

    public double add(double a, double b) throws RemoteException {
        System.out.println("📩 Cliente solicitó SUMA: " + a + " + " + b);
        return a + b;
    }

    public double subtract(double a, double b) throws RemoteException {
        System.out.println("📩 Cliente solicitó RESTA: " + a + " - " + b);
        return a - b;
    }

    public double multiply(double a, double b) throws RemoteException {
        System.out.println("📩 Cliente solicitó MULTIPLICACIÓN: " + a + " * " + b);
        return a * b;
    }

    public double divide(double a, double b) throws RemoteException {
        if (b == 0) {
            System.out.println("⚠️ Cliente intentó dividir por cero.");
            throw new ArithmeticException("No se puede dividir entre cero");
        }
        System.out.println("📩 Cliente solicitó DIVISIÓN: " + a + " / " + b);
        return a / b;
    }
}
