package com.jccv.tuprivadaapp.utils;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Component
public class DateFormats {

    public String getMonthAndYear(String fecha) {
        try {
            // Crear un formato de entrada para la fecha en formato yyyy-MM-dd
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd");
            // Parsear la fecha del string
            Date date = formatoEntrada.parse(fecha);

            // Crear un formato de salida para el mes (en texto) y el año
            SimpleDateFormat formatoMes = new SimpleDateFormat("MMMM", new Locale("es", "ES"));
            SimpleDateFormat formatoAno = new SimpleDateFormat("yyyy");

            // Obtener el mes y el año
            String mes = formatoMes.format(date);
            String ano = formatoAno.format(date);

            // Retornar el resultado en el formato deseado
            return mes + " de " + ano;
        } catch (Exception e) {
            e.printStackTrace();
            return "Formato de fecha inválido";
        }}
}
