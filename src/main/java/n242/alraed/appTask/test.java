package n242.alraed.appTask;

import java.io.File;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.swing.text.DateFormatter;

public class test {
    public static void main(String[] args) {

//        Date dateTime = new Date("1647343431970");
        DateFormat df = new SimpleDateFormat( "yy/MM/dd" );
        try {
            System.out.println( new DateFormatter( df ).valueToString( 1647343431970.0 ) );
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
