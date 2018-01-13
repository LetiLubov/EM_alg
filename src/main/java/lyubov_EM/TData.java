package lyubov_EM;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TData {

    public int N = 0; //количество объектов
    public int M = 0; //количество полей объекта
    public double[][] values; //данные по объекту
    private double[] Min, Max; //вспомогательные

    public TData(String Name) {
        List<double[]> SData = new ArrayList(); //здесь хранится вся инфа из файла (список из распарсенных строк)
        try {
            FileInputStream f = new FileInputStream(Name);
            BufferedReader br = new BufferedReader(new InputStreamReader(f));
            String line;

            while ((line = br.readLine()) != null) {
                String[] SS = line.split("\t");
                M++;

                double[] D = new double[SS.length];

                for (int n = 0; n < SS.length; n++) {
                    D[n] = Double.parseDouble(SS[n]);
                }

                SData.add(D);
            }

            N = SData.get(0).length;

            values = new double[M][N]; //запишем теперь в матрицу все значения
            Min = new double[N];
            Max = new double[N];

            for (int n = 0; n < N; n++) {
                //попутно ищем максимальные и минимальные значения (для преобразования и сведения числовых значений к диапозону 0-1)
                Min[n] = SData.get(0)[n];
                Max[n] = SData.get(0)[n];

                for (int m = 0; m < M; m++) {
                    values[m][n] = (SData.get(m))[n];

                    if (values[m][n] < Min[n]) {
                        Min[n] = values[m][n];
                    }

                    if (values[m][n] > Max[n]) {
                        Max[n] = values[m][n];
                    }
                }
            }
            //доп обработка данных , чтобы они были в диапозоне от 0 до 1
            for (int n = 0; n < N; n++) {
                for (int m = 0; m < M; m++) {
                    if (Max[n] - Min[n] != 0) {
                        values[m][n] = (values[m][n] - Min[n]) / (Max[n] - Min[n]);
                    } else {
                        values[m][n] = 0;
                    }
                }
            }

            System.out.println();

        } catch (IOException ex) {
            System.out.println("Ошибка чтения из файла.");
            ex.printStackTrace();
        }

    }

}
