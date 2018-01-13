package lyubov_EM;


import java.io.FileWriter;
import java.io.IOException;
//Запуск алгоритма EM и вывод результатов
public class Main {

    public static void main(String[] args) {
        String name;
        name = "src/main/java/lyubov_EM/source/minBirds.txt";
        TData data ;
        data = new TData(name);

        //Алгоритм представлен в двух вариантах
        //Далее метод 1 и 2 предоставляют два разных варианта реализации алгоритма EM.

        System.out.println("\n\n\n\n ***** \n\n\n\nМЕТОД 1 \n\n\n\n ***** \n\n\n\n");
        try {
            //Инициализация и запуск расчета
            Tem EM = new Tem(data, 3, 10);

            //Вывод результатов
            FileWriter f = new FileWriter("out.txt", false);
            //Вывод по объектам и их принадлженость к кажому кластеру
            int[] indexOfMaxValue = new int[data.M];
            for (int m = 0; m < data.M; m++) {
                System.out.println("- Принадлежность объекта " + String.valueOf(m + 1) + " к кластерам - ");
                indexOfMaxValue[m] = 0;
                double max = EM.G()[0][m];
                for (int k = 0; k < EM.clustersNumber; k++) {
                    f.append(String.valueOf(EM.G()[k][m]));
                    System.out.println("кластер "+String.valueOf(k+1) + ", вероятность = " + EM.G()[k][m]);
                    if (EM.G()[k][m] > max) {
                        max = EM.G()[k][m];
                        indexOfMaxValue[m] = k;
                    }

                }
                System.out.println("\n");
            }
            //Вывод по кластерам с объектами, которые им принадлежат.
            System.out.println("\n\n **** \n\n Сводная таблица \n\n **** \n\n ");
            for (int k = 0; k < EM.clustersNumber; k++) {
                System.out.println("Принадлежность к кластеру " + String.valueOf(k+1) + " (объекты считаем с 1):");
                for (int m = 0; m < data.M; m++) {
                    if (indexOfMaxValue[m] == k) {
                        System.out.println("Объект " + String.valueOf(m + 1) + ", вероятность = " + EM.G()[k][m]);
                    }
                }
            }


        } catch (IOException ex) {
            System.out.println("Проблемы с файлом");
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
       name = "src/main/java/lyubov_EM/source/t.txt";
        // String name = "src/main/java/lyubov_EM/source/minBirds.txt";
        // String name = "src/main/java/lyubov_EM/source/middleBirds.txt";
         data = new TData(name);


        System.out.println("\n\n\n\n ***** \n\n\n\nМЕТОД 2 \n\n\n\n ***** \n\n\n\n");
        try {
            //Инициализация и запуск расчета
            TemClust EM = new TemClust(data);
            EM.Run(3, 2, 10);

            //Вывод результатов
            FileWriter f = new FileWriter("out.txt", false);
            //Вывод по объектам и их принадлженость к кажому кластеру
            int[] indexOfMaxValue = new int[data.M];
            for (int m = 0; m < data.M; m++) {
                indexOfMaxValue[m] = 0;
                double max = EM.x[m][0];
                System.out.println("Принадлежность объекта " + String.valueOf(m + 1) + " к кластерам 1 - " + EM.K);
                for (int k = 0; k < EM.K; k++) {
                    if (EM.x[m][k] > max) {
                        max = EM.x[m][k];
                        indexOfMaxValue[m] = k;
                    }
                    f.append(String.valueOf(EM.x[m][k]));
                    System.out.println("кластер "+String.valueOf(k+1) + ", вероятность = " + EM.x[m][k]);
                }
            }
            //Вывод по кластерам с объектами, которые им принадлежат.
            System.out.println("\n\n **** \n\n Сводная таблица \n\n **** \n\n ");
            for (int k = 0; k < EM.K; k++) {
                System.out.println("Принадлежность к кластеру " + String.valueOf(k+1) + " (объекты считаем с 1):");
                for (int m = 0; m < data.M; m++) {
                    if (indexOfMaxValue[m] == k) {
                        System.out.println("Объект " + String.valueOf(m + 1)+ ", вероятность = " +  EM.x[m][k]);
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("Проблемы с файлом");
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }



    }

}
