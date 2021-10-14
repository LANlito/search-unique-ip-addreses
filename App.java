// ЦЕЛЬ: необходимо найти количество уникальных IP адресов в текстовом файле (одна строка - один адрес)
// 
// Для экономии памяти и будем использовать битовый массив длиной 2^32, 
// в котором IP адрес, преобразованный в целое число, будет являться индексом в этом массиве.

// При считывании IP адреса, преобразуем его в LONG
// Если значение бита по адресу LONG = 0, то увеличим счетчик уникальных IP адресов на 1 и установим значение этого бита в 1
// в противном случае уменьшим значение счетчика уникальных IP адресов (счетчик не может быть меньше 0)
// 

import java.io.*;
//import java.util.HexFormat;
import java.util.concurrent.FutureTask;


public class App {
    public static void main(String[] args) throws Exception {

        long UniqueIPcounter = 0;           // счетчик уникальных IP адресов
        long lineCounter = 0;               // счетчик считанных из файла строк
        final int ARRAYSIZE = 0xffffffff;
        
        //int a1 = Integer.MIN_VALUE - Integer.MAX_VALUE - 1;
        //int a2 = Integer.MAX_VALUE;
        //boolean a3 = a1 > 0;
        //String hexL = Integer.toHexString( a1 );

        BitArray IPcheckArray = new BitArray();             // битовый массив - значение True означает уже встречавшийся IP адрес
        BitArray counterDecrArray = new BitArray();         // битовый массив - значение True означает, что счетчик уникальных адресов был декрементирован
        int b[] = {0, 0, 0, 0};    // байтовый массив получаемый из IP адреса
        byte bi = 0;                // номер кварты из ip адреса
        long l;                     // в нем будет целое число преобразуемое из IP адреса

        FileReader reader = new FileReader("d:\\ip_addresses\\ip_addresses.txt");
        FileWriter fileresult = new FileWriter("d:\\ip_addresses\\result.txt");

        int c;                      // считанный символ
        //byte digits[] = [0, 0, 0];  // в этот массив будем складывать считанные цифры
        int number = 0;            // это у нас будет число, сформированное из digits
        byte razr = 0;              // счетчик разрядов считанного

        final int LN=0x0A;                  // символ конца строки
        final int DOT = 0x2E;               // символ точки
        final Double ONEPERCENT = 80000000.0;
        Double percent = 0.0;
        long counterMb = 0;

        while((c=reader.read())!=-1) {
            
            // если считана цифра, то добавляем ее к числу
            if (c>=0x30 & c<=0x39) {
                if (razr>0) { number=number*10; }
                number = number + (c - 0x30);
                razr++;
                }
            // если считана точка, то преобразуем число в int и обнуляем счетчик разрядности
            else if (c==DOT) {
                    b[bi] = number;
                    number=0;
                    razr=0;
                    bi++;
                }
            // если считан конец строки, то оформляем IP адрес в l
            else if (c==LN) { 
                    
                b[bi] = number;
                
                // преобразование IP адреса в 0xFFFFFFFF
                // оставим это преобразование в теле для увеличения скорости операций
                l = ((0xFF & b[0]) << 24) | ((0xFF & b[1]) << 16) |
                        ((0xFF & b[2]) << 8) | (0xFF & b[3]);
                
                //hexL = Integer.toHexString((int) l);

                // если IP адрес уже встречался, 
                if (IPcheckArray.getBit((int) l)==true) {
                    // но счетчик уникальных не был уменьшен
                    if (counterDecrArray.getBit((int) l)==false) {
                        UniqueIPcounter--;                          // уменьшаем счетчик уникальных адресов на 1
                        counterDecrArray.setBit((int)l, true);      // устанавливаем бит в 1
                    };
                }
                else {
                    // если этот IP адрес встречен в первый раз
                    UniqueIPcounter++;                  // увеличиваем счетчик уникальных адресов на 1
                    IPcheckArray.setBit((int)l, true);  // ставим метку об уже встреченном IP адресе
                }

                // обнуляем счетчики
                bi=0;
                b[0]=0; b[1]=0; b[2]=0; b[3]=0;
                razr=0;
                number=0;    
                lineCounter++;      // счетчик считанных строк
                counterMb++;    // счетчик считанных строк в Mb
                if (counterMb > ONEPERCENT/10) {
                    
                    percent = lineCounter/ONEPERCENT;
                    counterMb = 0;
                    
                    //Вывод на экран информации о ходе обработки
                    System.out.printf( "Progress: %.2f", percent );
                    System.out.println( "%. Прочитано IP адресов: " + lineCounter  + " Найдено уникальных IP адресов " + UniqueIPcounter);
                    
                    // дублируем вывод в файл
                    fileresult.write("Progress: " + percent);
                    fileresult.write("%. Прочитано IP адресов: " + lineCounter  + " Найдено уникальных IP адресов " + UniqueIPcounter + "\n");
                }
            }
            
        }
        System.out.print("Всего считано " + lineCounter + "IP адресов. Уникальных " + UniqueIPcounter);
        fileresult.write("Всего считано " + lineCounter + "IP адресов. Уникальных " + UniqueIPcounter + "\n");
        fileresult.close();

    }
}

// описываем класс битового массива с размерностью 32 бита (до 0xffffffff включительно)
final class BitArray {

    final private int BORDER = Integer.MAX_VALUE;
    private static final int ALL_ONES = 0xFFFFFFFF;
    private static final int WORD_SIZE = 32;
    private int WORDS = BORDER / WORD_SIZE + 1;
    private int bits1[] = null;
    private int bits2[] = null;
    private int size1, size2 = 0;

    public BitArray() {
        bits1 = new int[WORDS];
        bits2 = new int[WORDS];
    }

    public boolean getBit(int pos) {
        boolean f = false;
        if (pos>=0) {
            f = (bits1[pos / WORD_SIZE] & (1 << (pos % WORD_SIZE))) != 0;
            return f;
        } else {
            pos = pos - BORDER - 1;
            f = (bits2[pos / WORD_SIZE] & (1 << (pos % WORD_SIZE))) != 0;
            return f;
        }
    }

    public void setBit(int pos, boolean b) {
        if (pos>=0) {
            int word = bits1[pos / WORD_SIZE];
            int posBit = 1 << (pos % WORD_SIZE);
            if (b) {
                word |= posBit;
            } else {
                word &= (ALL_ONES - posBit);
            }
            bits1[pos / WORD_SIZE] = word;
        } else {
            pos = pos - BORDER - 1;
            int word = bits2[pos / WORD_SIZE];
            int posBit = 1 << (pos % WORD_SIZE);
            if (b) {
                word |= posBit;
            } else {
                word &= (ALL_ONES - posBit);
            }
            bits2[pos / WORD_SIZE] = word;
        }
    }

}